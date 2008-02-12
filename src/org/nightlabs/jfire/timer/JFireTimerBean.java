package org.nightlabs.jfire.timer;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;

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
public abstract class JFireTimerBean
extends BaseSessionBeanImpl
implements SessionBean, TimedObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireTimerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

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
					logger.error("JFireServerManagerFactory is not (yet?) in JNDI! Cannot do anything.");
					return;
				}

				if (!ismf.isUpAndRunning()) {
					logger.info("Server is not yet up and running - will not do anything.");
					return;
				}

				JFireServerManager ism = ismf.getJFireServerManager();
				try {
					LoginContext loginContext = new LoginContext(
							"jfire", new AuthCallbackHandler(ism, timerParam.organisationID, User.USERID_SYSTEM));

					loginContext.login();
					try {

						TimerManagerLocal timerManagerLocal = TimerManagerUtil.getLocalHome().create();
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

	/**
	 * @throws ModuleException 
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 **/
	public void startTimer()
	throws ModuleException
	{
		String property_JFireTimerStart_key = JFireTimer.class.getName() + ".start";
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
				logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				logger.warn("There are " + tasks.size() + " Tasks currently marked with executing=true! This is impossible! Will clear that flag now:");
				for (Task task : tasks) {
					task.setExecuting(false);
					logger.warn("  cleared Task.executing: " + JDOHelper.getObjectId(task));
				}
			}

			long timeout = 60 * 1000; // call once every minute
	
			// We want the timer to start as exactly as possible at the starting of the minute (at 00 sec).
			long start = System.currentTimeMillis();
			start = start + timeout - (start % timeout);
	
	//		try {
	//			if (wait > 0)
	//				Thread.sleep(wait);
	//		} catch (InterruptedException e) {
	//			// ignore
	//		}
	
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
