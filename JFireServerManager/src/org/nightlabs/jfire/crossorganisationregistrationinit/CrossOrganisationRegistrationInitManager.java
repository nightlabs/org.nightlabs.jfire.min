package org.nightlabs.jfire.crossorganisationregistrationinit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.init.InvocationInitJarEntryHandler;
import org.nightlabs.jfire.init.InvocationInitManager;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.ServerCf;

public class CrossOrganisationRegistrationInitManager
extends InvocationInitManager<CrossOrganisationRegistrationInit, CrossOrganisationRegistrationInitDependency>
//extends AbstractInitManager<CrossOrganisationRegistrationInit, OrganisationInitDependency>
{
	private static final Logger logger = Logger.getLogger(CrossOrganisationRegistrationInitManager.class);

	/**
	 * Holds instances of type <tt>Init</tt>.
	 */
	private List<CrossOrganisationRegistrationInit> inits = new ArrayList<CrossOrganisationRegistrationInit>();

	public CrossOrganisationRegistrationInitManager(JFireServerManager jfsm)
	throws InitException
	{
		super(jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());
	}

	public void initialiseOrganisation(
			JFireServerManagerFactory ismf, ServerCf localServer, String organisationID, String systemUserPassword, Context context) throws InitException
	{
		try {
			Properties props = InvokeUtil.getInitialContextProperties(ismf, UserID.create(organisationID, User.USER_ID_SYSTEM), systemUserPassword);
			InitialContext initCtx = new InitialContext(props);
			try {
				Throwable firstInitException = null;

				for (CrossOrganisationRegistrationInit init : inits) {
					logger.info("Invoking CrossOrganisationRegistrationInit: " + init);

					try {
						// we force a new (nested) transaction by using a delegate-ejb with the appropriate tags
//						Object delegateBean = InvokeUtil.createBean(initCtx, "jfire/ejb/JFireBaseBean/OrganisationInitDelegate");
						Object delegateBean = initCtx.lookup(InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + "org.nightlabs.jfire.organisationinit.OrganisationInitDelegateRemote");
						Method beanMethod = delegateBean.getClass().getMethod("invokeCrossOrganisationRegistrationInitInNestedTransaction", CrossOrganisationRegistrationInit.class, Context.class);
						beanMethod.invoke(delegateBean, init, context);
//						InvokeUtil.removeBean(delegateBean);
					} catch (Throwable x) { // we catch this in order to execute all inits before escalating
						if (firstInitException == null)
							firstInitException = x;

						logger.error("CrossOrganisationRegistrationInit failed! " + init, x);
					}
				}

				// We escalate the first exception that occured in order to ensure the inits to be retried.
				// Since this method is called by an Async-Invocation, it's retried a few times and afterwards, an administrator
				// has the possibility to initiate further retries.
				if (firstInitException != null)
					throw firstInitException;

			} finally {
		   	initCtx.close();
			}
		} catch (Throwable x) {
			throw new InitException(x);
		}
	}

	@Override
	protected InvocationInitJarEntryHandler<CrossOrganisationRegistrationInit, CrossOrganisationRegistrationInitDependency> createInvocationInitJarEntryHandler() {
		return new CrossOrganisationRegistrationInitJarEntryHandler();
	}

}
