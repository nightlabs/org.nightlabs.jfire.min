package org.nightlabs.jfire.timer;

import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.asyncinvoke.AuthCallbackHandler;
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
 * @ejb.util generate = "physical"
 */
public abstract class JFireTimerBean
extends BaseSessionBeanImpl
implements SessionBean, TimedObject
{
	public static final Logger LOGGER = Logger.getLogger(JFireTimerBean.class);

	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
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

			// We are not authenticated here, thus we cannot access the persistence manager properly.
			// Therefore, we wrap this in an AsyncInvoke
			InitialContext initCtxNotAuthenticated = new InitialContext();
			try {
				JFireServerManagerFactory ismf = (JFireServerManagerFactory) initCtxNotAuthenticated.lookup(JFireServerManagerFactory.JNDI_NAME);

				if (ismf == null) {
					LOGGER.error("JFireServerManagerFactory is not (yet?) in JNDI! Cannot do anything.");
					return;
				}

				if (!ismf.isUpAndRunning()) {
					LOGGER.info("Server is not yet up and running - will not do anything.");
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
			LOGGER.error("Timer failed!", x);
		}
	}

	/**
	 * @throws ModuleException 
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission unchecked="true"
	 **/
	public void startTimer()
	throws ModuleException
	{
		if (!User.USERID_SYSTEM.equals(getUserID()))
			throw new SecurityException("This method can only be called by user " + User.USERID_SYSTEM);

		Date firstExecDate = new Date(); // start now
		long timeout = 60 * 1000; // call once every minute
		TimerService timerService = sessionContext.getTimerService();
		timerService.createTimer(
				firstExecDate,
				timeout,
				new TimerParam(getOrganisationID()) // this object can be retrieved by Timer#getInfo()
				);
	}
}
