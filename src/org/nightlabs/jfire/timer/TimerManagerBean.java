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
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.timepattern.TimePatternSetJDOImpl;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.Authority;
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
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class TimerManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TimerManagerBean.class);

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
	 */
	public boolean setExecutingIfActiveExecIDMatches(TaskID taskID, String activeExecID)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			NLJDOHelper.setTransactionSerializeReadObjects(pm, true);

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
	 * Because the method {@link JFireTimerBean#ejbTimeout(javax.ejb.Timer)} is called without authentication
	 * and thus accessing the datastore doesn't work properly, we use this method as
	 * a delegate.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission unchecked="true"
	 */
	public void ejbTimeoutDelegate(TimerParam timerParam)
	throws Exception
	{
		if (!User.USER_ID_SYSTEM.equals(getUserID()))
			throw new SecurityException("This method can only be called by user " + User.USER_ID_SYSTEM);

		if (logger.isDebugEnabled())
			logger.debug("ejbTimeoutDelegate: principal.organisationID=\""+getOrganisationID()+"\" timerParam.organisationID=\""+timerParam.organisationID+"\": begin");

		if (!getOrganisationID().equals(timerParam.organisationID))
			throw new IllegalStateException("principal.organisationID=\""+getOrganisationID()+"\" != timerParam.organisationID=\""+timerParam.organisationID+"\"");

		String activeExecID = ObjectIDUtil.makeValidIDString(null);

		List<Task> tasks;
		PersistenceManager pm = getPersistenceManager();
		try {
			NLJDOHelper.setTransactionSerializeReadObjects(pm, true);

			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(FETCH_GROUPS_TASK);
			Date now = new Date();
			tasks = Task.getTasksToDo(pm, now);
			for (Iterator<Task> it = tasks.iterator(); it.hasNext(); ) {
				Task task = it.next();
				task.setActiveExecID(activeExecID);
				TimerAsyncInvoke.exec(task, true); // this method does not use the task instance later outside the current transaction (it only fetches the TaskID)
			}

			for (Iterator<Task> it = Task.getTasksToRecalculateNextExecDT(pm, now).iterator(); it.hasNext(); ) {
				Task task = it.next();
				task.calculateNextExecDT();
			}
		} finally {
			pm.close();
		}

		logger.info("ejbTimeoutDelegate: organisationID=\""+timerParam.organisationID+"\": end");
	}


	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<TaskID> getTaskIDs()
	throws ModuleException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				return NLJDOHelper.getObjectIDList((Collection<?>)pm.newQuery(Task.class).execute());
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}


	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<TaskID> getTaskIDs(String taskTypeID)
	throws ModuleException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				return NLJDOHelper.getObjectIDList(Task.getTasksByTaskTypeID(pm, taskTypeID));
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}


	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Task> getTasks(Collection<TaskID> taskIDs, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				List<Task> tasks = NLJDOHelper.getDetachedObjectList(pm, taskIDs, Task.class, fetchGroups, maxFetchDepth);
				return tasks;
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.timer.storeTask#own"
	 */
	public Task storeTask(Task task, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				User principalUser = User.getUser(pm, getPrincipal()); // do this before locking, because the user isn't changed in this transaction anyway - no need to lock it in the db

				NLJDOHelper.setTransactionSerializeReadObjects(pm, true);

				TaskID taskID = (TaskID) JDOHelper.getObjectId(task);
				Task persistentTask = null;
				if (taskID != null)
					persistentTask = (Task) pm.getObjectById(taskID);

				User taskOwnerToBeWritten = null;
				try {
					taskOwnerToBeWritten = task.getUser();
				} catch (JDODetachedFieldAccessException x) {
					// ignore - in this case the owner is not changed and the persistentTask.user will be checked
					if (persistentTask == null)
						throw new IllegalStateException("task.user is not a detached field, but the task is not existing in the datastore either! " + task);
				}

				if (
						(taskOwnerToBeWritten != null && !principalUser.equals(taskOwnerToBeWritten)) ||
						(persistentTask != null && !principalUser.equals(persistentTask.getUser()))
				)
				{
					NLJDOHelper.setTransactionSerializeReadObjects(pm, false); // no need to lock the Authority - better don't! Marco.

					// trying to manipulate a task where the current user is not the owner => check for RoleConstants.storeTask_all
					Authority.getOrganisationAuthority(pm).assertContainsRoleRef(getPrincipal(), RoleConstants.storeTask_all);
				}

				task = NLJDOHelper.storeJDO(pm, task, get, fetchGroups, maxFetchDepth);
				return task;
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}
}
