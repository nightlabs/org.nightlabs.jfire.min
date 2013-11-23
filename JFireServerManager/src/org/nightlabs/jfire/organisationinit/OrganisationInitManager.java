package org.nightlabs.jfire.organisationinit;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.init.InvocationInitJarEntryHandler;
import org.nightlabs.jfire.init.InvocationInitManager;
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

public class OrganisationInitManager
extends InvocationInitManager<OrganisationInit, OrganisationInitDependency>
{
	private static final Logger logger = Logger.getLogger(OrganisationInitManager.class);

	public OrganisationInitManager(JFireServerManagerFactoryImpl jfsmf, ManagedConnectionFactoryImpl mcf, J2EEAdapter j2eeAdapter) throws InitException
	{
		super(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());
	}

	public void initialiseOrganisation(JFireServerManagerFactory ismf, ServerCf localServer, String organisationID, String systemUserPassword) throws OrganisationInitException
	{
		initialiseOrganisation(ismf, localServer, organisationID, systemUserPassword, null);
	}

	public void initialiseOrganisation(JFireServerManagerFactory ismf, ServerCf localServer, String organisationID, String systemUserPassword, CreateOrganisationProgress createOrganisationProgress) throws OrganisationInitException
	{
		try {
			Properties props = InvokeUtil.getInitialContextProperties(ismf, UserID.create(organisationID, User.USER_ID_SYSTEM), systemUserPassword);
			InitialContext initCtx = new InitialContext(props);
			try {
				for (OrganisationInit init : getInits()) {
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

	@Override
	protected InvocationInitJarEntryHandler<OrganisationInit, OrganisationInitDependency> createInvocationInitJarEntryHandler() {
		return new OrganisationInitJarEntryHandler();
	}
}
