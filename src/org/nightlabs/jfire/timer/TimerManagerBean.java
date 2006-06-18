package org.nightlabs.jfire.timer;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Timer;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.timepattern.TimePatternSetJDOImpl;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/TimerManager"
 *		jndi-name="jfire/ejb/JFireBaseBean/TimerManager"
 *		type="Stateless"
 *
 * @ejb.util generate = "physical"
 */
public abstract class TimerManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(TimerManagerBean.class);

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

	private static final String[] FETCH_GROUPS_TASK = new String[] {
		FetchPlan.DEFAULT,
		Task.FETCH_GROUP_USER,
		Task.FETCH_GROUP_TIME_PATTERN_SET,
		TimePatternSetJDOImpl.FETCH_GROUP_TIME_PATTERNS };

	/**
	 * Because the method {@link #ejbTimeout(Timer)} is called without authentication
	 * and thus accessing the datastore doesn't work properly, we use this method as
	 * a delegate.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="Required"
	 * @ejb.permission unchecked="true"
	 **/
	public void ejbTimeoutDelegate(TimerParam timerParam)
	throws Exception
	{
		if (!User.USERID_SYSTEM.equals(getUserID()))
			throw new SecurityException("This method can only be called by user " + User.USERID_SYSTEM);

		LOGGER.info("ejbTimeoutDelegate(...) for organisationID=\""+timerParam.organisationID+"\": begin");

		String activeExecID = ObjectIDUtil.makeValidIDString(null);

		List tasks;
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(FETCH_GROUPS_TASK);
			tasks = Task.getTasksToDo(pm, new Date());
			for (Iterator it = tasks.iterator(); it.hasNext(); ) {
				Task task = (Task) it.next();
				task.setExecuting(true);
				task.setActiveExecID(activeExecID);
			}
			tasks = (List) pm.detachCopyAll(tasks);
		} finally {
			pm.close();
		}

		for (Iterator it = tasks.iterator(); it.hasNext(); ) {
			Task task = (Task) it.next();
			TimerAsyncInvoke.exec(task);
		}

		LOGGER.info("ejbTimeoutDelegate(...) for organisationID=\""+timerParam.organisationID+"\": end");
	}

}
