package org.nightlabs.jfire.timer;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Timer;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.timepattern.TimePatternSetJDOImpl;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.id.TaskID;

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
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(TimerManagerBean.class);

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
	 * @ejb.permission unchecked="true"
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
	 * This method is called by {@link TimerAsyncInvoke.TimerInvocation#invoke()}.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission unchecked="true"
	 **/
	public boolean setExecutingIfActiveExecIDMatches(TaskID taskID, String activeExecID)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Task task = (Task) pm.getObjectById(taskID);
			if (!activeExecID.equals(task.getActiveExecID())) {
				logger.info("setExecutingIfActiveExecIDMatches(...): will not touch task with taskID=\""+taskID+"\", because activeExecID does not match: activeExecID()=\""+activeExecID+"\" task.getActiveExecID()=\""+task.getActiveExecID()+"\"");
				return false;
			}

			task.setExecuting(true);
			return true;
		} finally {
			pm.close();
		}
	}

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

		logger.info("ejbTimeoutDelegate(...) for organisationID=\""+timerParam.organisationID+"\": begin");

		String activeExecID = ObjectIDUtil.makeValidIDString(null);

		List tasks;
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(FETCH_GROUPS_TASK);
			Date now = new Date();
			tasks = Task.getTasksToDo(pm, now);
			for (Iterator it = tasks.iterator(); it.hasNext(); ) {
				Task task = (Task) it.next();
				task.setActiveExecID(activeExecID);
			}
			tasks = (List) pm.detachCopyAll(tasks);

			for (Iterator it = Task.getTasksToRecalculateNextExecDT(pm, now).iterator(); it.hasNext(); ) {
				Task task = (Task) it.next();
				task.calculateNextExecDT();
				TimerAsyncInvoke.exec(task, true);
			}
		} finally {
			pm.close();
		}

		logger.info("ejbTimeoutDelegate(...) for organisationID=\""+timerParam.organisationID+"\": end");
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 **/
	@SuppressWarnings("unchecked")
	public Task getTask(TaskID taskID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				Task task = (Task) pm.getObjectById(taskID);
				task = (Task) pm.detachCopy(task);
				try {
					task.getUser().getUserLocal().setPassword("********");
				} catch (NullPointerException x) {
					// ignore
				} catch (JDODetachedFieldAccessException x) {
					// ignore
				}

				return task;
			} finally {
				pm.close();
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 **/
	@SuppressWarnings("unchecked")
	public List<TaskID> getTaskIDs()
	throws ModuleException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				return NLJDOHelper.getObjectIDList(
						(Collection)pm.newQuery(Task.class).execute());
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 **/
	@SuppressWarnings("unchecked")
	public List<Task> getTasks(
			Collection<TaskID> taskIDs, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				List<Task> tasks = NLJDOHelper.getDetachedObjectList(pm, taskIDs, Task.class, fetchGroups, maxFetchDepth);
				for (Task task : tasks) {
					try {
						task.getUser().getUserLocal().setPassword("********");
					} catch (NullPointerException x) {
						// ignore
					} catch (JDODetachedFieldAccessException x) {
						// ignore
					}
				}
				return tasks;
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}
}
