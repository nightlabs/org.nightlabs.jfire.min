package org.nightlabs.jfire.timer;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/JFireTimer"
 *		jndi-name="jfire/ejb/JFireBaseBean/JFireTimer"
 *		type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class JFireTimerBean
extends BaseSessionBeanImpl
implements TimedObject, JFireTimerRemote
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireTimerBean.class);

	@EJB
	TimerManagerLocal timerManagerLocal;

	@Override
	public void ejbTimeout(Timer timer)
	{
		try {
			TimerParam timerParam = (TimerParam) timer.getInfo();
			if (timerParam == null)
				throw new IllegalStateException("timer.getInfo() returned null! Should be an instance of TimerParam!!!");

			if(logger.isDebugEnabled())
				logger.debug("ejbTimeout: organisationID=" + timerParam.organisationID);

			// We are not authenticated here, thus we cannot access the persistence manager properly.
			// Therefore, we wrap this in an AsyncInvoke
			InitialContext initCtxNotAuthenticated = new InitialContext();
			try {
				JFireServerManagerFactory ismf = (JFireServerManagerFactory) initCtxNotAuthenticated.lookup(JFireServerManagerFactory.JNDI_NAME);

				if (ismf == null) {
					logger.error("ejbTimeout: JFireServerManagerFactory is not (yet?) in JNDI! Cannot do anything.");
					return;
				}

				if (ismf.isShuttingDown()) {
					logger.info("ejbTimeout: Server is shutting down - will not do anything.");
					return;
				}

				if (!ismf.isUpAndRunning()) {
					logger.info("ejbTimeout: Server is not yet up and running - will not do anything.");
					return;
				}

				J2EEAdapter j2eeAdapter = (J2EEAdapter) initCtxNotAuthenticated.lookup(J2EEAdapter.JNDI_NAME);

				JFireServerManager ism = ismf.getJFireServerManager();
				try {
//					LoginContext loginContext = new LoginContext(
//							LoginData.DEFAULT_SECURITY_PROTOCOL,
//							new AuthCallbackHandler(ism, timerParam.organisationID, User.USER_ID_SYSTEM)
//					);
					LoginContext loginContext = j2eeAdapter.createLoginContext(
							LoginData.DEFAULT_SECURITY_PROTOCOL,
							new AuthCallbackHandler(ism, timerParam.organisationID, User.USER_ID_SYSTEM)
					);

					loginContext.login();
					try {

//						TimerManagerLocal timerManagerLocal = JFireEjb3Factory.getLocalBean(TimerManagerLocal.class, null);
//						TimerManagerLocal timerManagerLocal = TimerManagerUtil.getLocalHome().create();
						if (timerManagerLocal == null)
							throw new IllegalStateException("Dependency injection for timerManagerLocal failed!");

						timerManagerLocal.ejbTimeoutDelegate(timerParam);

					} finally {
						loginContext.logout();
					}

				} finally {
					ism.close();
				}
			} finally {
				initCtxNotAuthenticated.close();
			}
		} catch (Throwable x) {
			logger.error("Timer failed!", x);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.JFireTimerRemote#startTimer()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void startTimer()
	{
		// TODO update the wiki page https://www.jfire.org/modules/phpwiki/index.php/System%20properties%20supported%20by%20the%20JFire%20Server
		// with the new system property name ("Local"-suffix). Marco.
		String property_JFireTimerStart_key = JFireTimerRemote.class.getName() + ".start";
		String property_JFireTimerStart_value = System.getProperty(property_JFireTimerStart_key);
		if ("false".equals(property_JFireTimerStart_value)) {
			logger.warn("The system property \"" + property_JFireTimerStart_key + "\" has been set to \"" + property_JFireTimerStart_value + "\"; the timer will *not* be started!");
			return;
		}

		TimerService timerService = sessionContext.getTimerService();

		PersistenceManager pm = getPersistenceManager();
		try {
			// before we start the timer, we clear all Task.executing flags (it's not possible that there's sth. executing before we start the timer)
			List<Task> tasks = Task.getTasksByExecuting(pm, true);
			if (!tasks.isEmpty()) {
				logger.warn("startTimer: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				logger.warn("startTimer: There are " + tasks.size() + " Tasks currently marked with executing=true! This is impossible! Will clear that flag now:");
				for (Task task : tasks) {
					task.setExecuting(false);
					logger.warn("startTimer:  cleared Task.executing: " + JDOHelper.getObjectId(task));
				}
			}

			long timeout = 60 * 1000; // call once every minute

			// We want the timer to start as exactly as possible at the starting of the minute (at 00 sec).
			long start = System.currentTimeMillis();
			start = start + timeout - (start % timeout);

			Date firstExecDate = new Date(start);
			timerService.createTimer(
					firstExecDate,
					timeout,
					new TimerParam(getOrganisationID()) // this object can be retrieved by Timer#getInfo()
					);
		} finally {
			pm.close();
		}
	}
}
