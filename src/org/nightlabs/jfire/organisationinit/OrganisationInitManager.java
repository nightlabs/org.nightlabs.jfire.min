package org.nightlabs.jfire.organisationinit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.datastructure.PrefixTree;
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.init.AbstractInitManager;
import org.nightlabs.jfire.init.DependencyCycleException;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStep;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;
import org.nightlabs.jfire.servermanager.ra.ManagedConnectionFactoryImpl;
import org.nightlabs.jfire.servermanager.xml.JarEntryHandler;

public class OrganisationInitManager
extends AbstractInitManager<OrganisationInit, OrganisationInitDependency>
{
	private static final Logger logger = Logger.getLogger(OrganisationInitManager.class);

	private boolean canPerformInit = false;

	/**
	 * All found organisation inits.
	 */
	private List<OrganisationInit> inits = new ArrayList<OrganisationInit>();

	public OrganisationInitManager(JFireServerManagerFactoryImpl jfsmf, ManagedConnectionFactoryImpl mcf, J2EEAdapter j2eeAdapter) throws OrganisationInitException
	{
		OrganisationInitJarEntryHandler organisationInitJarEntryHandler = new OrganisationInitJarEntryHandler();
		scan(
				mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory(),
				new String[] { "META-INF/organisation-init.xml" },
				new JarEntryHandler[] { organisationInitJarEntryHandler }
		);
		this.inits = organisationInitJarEntryHandler.getInits();
		final PrefixTree<OrganisationInit> initTrie = organisationInitJarEntryHandler.getInitTrie();
		// Now all meta data files have been read.

		// substitute the temporary dependency definitions by links to the actual inits
		try {
			establishDependencies(inits, initTrie);
		} catch (InitException e1) {
			throw new OrganisationInitException("Datastore initialisation failed: " + e1.getMessage());
		}

		// Now all inits have references of their required and dependent inits.
		try {
			inits = resolveDependencies(inits, new OrganisationInitComparator());
		} catch (DependencyCycleException e) {
			throw new OrganisationInitException(e + "Information regarding the cycle: "+ e.getCycleInfo());
		}
		canPerformInit = true;

		if (logger.isDebugEnabled()) {
			logger.debug("************************************************");
			logger.debug("Organisation Inits in execution order:");
			printInits(inits, logger);
			logger.debug("************************************************");
		}
	}

	public List<OrganisationInit> getInits()
	{
		return Collections.unmodifiableList(inits);
	}

	@Override
	protected String[] getTriePath(OrganisationInitDependency dependency) {
		String[] fields = new String[4];
		fields[0] = dependency.getModule();
		fields[1] = dependency.getArchive();
		fields[2] = dependency.getBean();
		fields[3] = dependency.getMethod();

		List<String> toReturn = new ArrayList<String>(fields.length);

		for (int i = 0; i < fields.length; i++) {
			if (fields[i] == null || fields[i].equals(""))
				break;
			toReturn.add(fields[i]);
		}

		return toReturn.toArray(new String[toReturn.size()]);
	}

	public void initialiseOrganisation(JFireServerManagerFactory ismf, ServerCf localServer, String organisationID, String systemUserPassword) throws OrganisationInitException
	{
		initialiseOrganisation(ismf, localServer, organisationID, systemUserPassword, null);
	}

	public void initialiseOrganisation(JFireServerManagerFactory ismf, ServerCf localServer, String organisationID, String systemUserPassword, CreateOrganisationProgress createOrganisationProgress) throws OrganisationInitException
	{
		if (!canPerformInit) {
			logger.error("Organisation initialisation can not be performed due to errors above.");
			return;
		}

		try {
			Properties props = InvokeUtil.getInitialContextProperties(ismf, UserID.create(organisationID, User.USER_ID_SYSTEM), systemUserPassword);
			InitialContext initCtx = new InitialContext(props);
			try {
				for (OrganisationInit init : inits) {
					logger.info("Invoking OrganisationInit: " + init);

					if (createOrganisationProgress != null)
						createOrganisationProgress.addCreateOrganisationStatus(
								new CreateOrganisationStatus(CreateOrganisationStep.DatastoreInitManager_initialiseDatastore_begin, init.getName()));

					try {
						// we force a new (nested) transaction by using a delegate-ejb with the appropriate tags
						Object delegateBean = initCtx.lookup(InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + "org.nightlabs.jfire.organisationinit.OrganisationInitDelegateRemote");
						Method beanMethod = delegateBean.getClass().getMethod("invokeOrganisationInitInNestedTransaction", OrganisationInit.class);
						beanMethod.invoke(delegateBean, init);

						if (createOrganisationProgress != null)
							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(CreateOrganisationStep.DatastoreInitManager_initialiseDatastore_endWithSuccess, init.getName()));
					} catch (Exception x) {
						logger.error("Init failed! " + init, x);

						if (createOrganisationProgress != null)
							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(CreateOrganisationStep.DatastoreInitManager_initialiseDatastore_endWithError, x));
					}
				}
			} finally {
				initCtx.close();
			}
		} catch (Exception x) {
			throw new OrganisationInitException(x);
		}
	}
}
