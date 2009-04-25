package org.nightlabs.jfire.timer;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class TimerManagerBean
extends BaseSessionBeanImpl
implements TimerManagerRemote, TimerManagerLocal
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TimerManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerRemote#ping(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public String ping(String message) {
		return super.ping(message);
	}

	private static final String[] FETCH_GROUPS_TASK = new String[] {
		FetchPlan.DEFAULT,
		Task.FETCH_GROUP_USER,
		Task.FETCH_GROUP_TIME_PATTERN_SET,
		TimePatternSetJDOImpl.FETCH_GROUP_TIME_PATTERNS };


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerLocal#setExecutingIfActiveExecIDMatches(org.nightlabs.jfire.timer.id.TaskID, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public boolean setExecutingIfActiveExecIDMatches(TaskID taskID, String activeExecID)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {

				Task task = (Task) pm.getObjectById(taskID);
				if (!activeExecID.equals(task.getActiveExecID())) {
					logger.info("setExecutingIfActiveExecIDMatches(...): will not touch task with taskID=\""+taskID+"\", because activeExecID does not match: activeExecID()=\""+activeExecID+"\" task.getActiveExecID()=\""+task.getActiveExecID()+"\"");
					return false;
				}

				task.setExecuting(true);
				return true;

			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerLocal#ejbTimeoutDelegate(org.nightlabs.jfire.timer.TimerParam)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
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
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {

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
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}
		} finally {
			pm.close();
		}

		logger.info("ejbTimeoutDelegate: organisationID=\""+timerParam.organisationID+"\": end");
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerRemote#getTaskIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<TaskID> getTaskIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDList((Collection<?>)pm.newQuery(Task.class).execute());
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerRemote#getTaskIDs(java.lang.String)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<TaskID> getTaskIDs(String taskTypeID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDList(Task.getTasksByTaskTypeID(pm, taskTypeID));
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerRemote#getTasks(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<Task> getTasks(Collection<TaskID> taskIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			List<Task> tasks = NLJDOHelper.getDetachedObjectList(pm, taskIDs, Task.class, fetchGroups, maxFetchDepth);
			return tasks;
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerRemote#storeTask(org.nightlabs.jfire.timer.Task, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.timer.storeTask#own")
	@Override
	public Task storeTask(Task task, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User principalUser = User.getUser(pm, getPrincipal()); // do this before locking, because the user isn't changed in this transaction anyway - no need to lock it in the db
			Task persistentTask = null;

			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {

				TaskID taskID = (TaskID) JDOHelper.getObjectId(task);
				if (taskID != null)
					persistentTask = (Task) pm.getObjectById(taskID);

				// access a few fields to ensure the Task object is locked in the database
				// this should not be necessary anmore when http://www.jpox.org/servlet/jira/browse/NUCRDBMS-67 is fixed, but
				// it still is no harm.
				if (persistentTask != null) {
					persistentTask.getActiveExecID();
					persistentTask.getDescription();
				}

			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}

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
				// trying to manipulate a task where the current user is not the owner => check for RoleConstants.storeTask_all
				Authority.getOrganisationAuthority(pm).assertContainsRoleRef(getPrincipal(), RoleConstants.storeTask_all);
			}

			task = NLJDOHelper.storeJDO(pm, task, get, fetchGroups, maxFetchDepth);
			return task;
		} finally {
			pm.close();
		}
	}
}
