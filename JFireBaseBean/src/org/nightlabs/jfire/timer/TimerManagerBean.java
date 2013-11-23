package org.nightlabs.jfire.timer;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.timepattern.TimePatternSetJDOImpl;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.cluster.ClusterNode;
import org.nightlabs.jfire.cluster.ClusterNodeIDSharedInstance;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.id.TaskID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class TimerManagerBean
extends BaseSessionBeanImpl
implements TimerManagerRemote, TimerManagerLocal
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(TimerManagerBean.class);

	private static final String[] FETCH_GROUPS_TASK = new String[] {
		FetchPlan.DEFAULT,
//		Task.FETCH_GROUP_USER,
		Task.FETCH_GROUP_TIME_PATTERN_SET,
		TimePatternSetJDOImpl.FETCH_GROUP_TIME_PATTERNS
	};


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerLocal#setExecutingIfActiveExecIDMatches(org.nightlabs.jfire.timer.id.TaskID, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public boolean setExecutingIfActiveExecIDMatches(TaskID taskID, String activeExecID)
	throws Exception
	{
		if (taskID == null)
			throw new IllegalArgumentException("taskID == null");

		if (activeExecID == null)
			throw new IllegalArgumentException("activeExecID == null");

		PersistenceManager pm = createPersistenceManager();
		try {
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {

				Task task = (Task) pm.getObjectById(taskID);
				if (!activeExecID.equals(task.getActiveExecID())) {
					logger.info("setExecutingIfActiveExecIDMatches(...): will not touch task with taskID=\""+taskID+"\", because activeExecID does not match: activeExecID()=\""+activeExecID+"\" task.getActiveExecID()=\""+task.getActiveExecID()+"\"");
					return false;
				}

//				task.setExecuting(true);
				task.setExecutingClusterNodeID(ClusterNodeIDSharedInstance.getClusterNodeID());
				return true;

			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}
		} finally {
			pm.close();
		}
	}


	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void cleanupTasksMarkedAsExecutingByDeadClusterNodes()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			logger.debug("cleanupTasksMarkedAsExecutingByDeadClusterNodes: entered");

			// Maybe we should make this configurable?! A node is considered dead, if its last
			// heartbeat-timestamp is older than this time in milliseconds.
			long minClusterNodeHeartbeatAgeMSecConsideredDead = 1000L * 60L * 5; // 5 minutes.

			Date oldestClusterNodeHeartbeatDateConsideredAlive = new Date(System.currentTimeMillis() - minClusterNodeHeartbeatAgeMSecConsideredDead);

			// Mark our own cluster node as alive (save the current timestamp).
			UUID myClusterNodeID = ClusterNodeIDSharedInstance.getClusterNodeID();
			ClusterNode myClusterNode = ClusterNode.createClusterNode(pm, myClusterNodeID);
			myClusterNode.setLastHeartbeatDate(new Date());
			pm.flush(); // Only necessary to see the correct timings, if debug logging is enabled.

			logger.debug("cleanupTasksMarkedAsExecutingByDeadClusterNodes: Registered my heartbeat timestamp.");


			// Clean up old cluster nodes (prevent them from piling up).
			long beginDeletingOldDeadClusterNodes = System.currentTimeMillis();
			List<? extends ClusterNode> veryOldDeadClusterNodes = ClusterNode.getClusterNodesWithLastHeartbeatDateBefore(
					pm,
					new Date(
							System.currentTimeMillis() - 1000L * 3600L * 24L // Make this configurable?
					)
			);
			pm.deletePersistentAll(veryOldDeadClusterNodes);
			pm.flush(); // Necessary to measure the correct timings.
			long durationDeletingOldDeadClusterNodes = System.currentTimeMillis() - beginDeletingOldDeadClusterNodes;
			if (durationDeletingOldDeadClusterNodes > 100)
				logger.warn("cleanupTasksMarkedAsExecutingByDeadClusterNodes: Deleting {} cluster nodes which disappeared longer than one day ago took {} msec, which is too long!!!.", veryOldDeadClusterNodes.size(), durationDeletingOldDeadClusterNodes);
			else
				logger.debug("cleanupTasksMarkedAsExecutingByDeadClusterNodes: Deleting {} cluster nodes which disappeared longer than one day ago took {} msec.", veryOldDeadClusterNodes.size(), durationDeletingOldDeadClusterNodes);


			// Find all tasks that are currently marked with executing=true, but not by this node.
			List<Task> tasks;
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {
				tasks = Task.getTasksExecutingByNotClusterNodeID(pm, myClusterNodeID);
			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}

			// Collect all tasks that need to be released in this list.
			List<Task> tasksMarkedAsExecutingByDeadClusterNodes = new LinkedList<Task>();

			// Cache all alive and dead nodes (so that we don't need to check the same node multiple times).
			Set<UUID> clusterNodeIDs_alive = new HashSet<UUID>();
			Set<UUID> clusterNodeIDs_dead = new HashSet<UUID>();

			// Iterate all tasks and decide whether they need to be released.
			for (Task task : tasks) {
				UUID executingClusterNodeID = task.getExecutingClusterNodeID();
				if (clusterNodeIDs_alive.contains(executingClusterNodeID))
					continue;

				if (clusterNodeIDs_dead.contains(executingClusterNodeID)) {
					tasksMarkedAsExecutingByDeadClusterNodes.add(task);
					continue;
				}

				ClusterNode clusterNode = ClusterNode.getClusterNode(pm, executingClusterNodeID);
				if (clusterNode == null) {
					logger.debug("cleanupTasksMarkedAsExecutingByDeadClusterNodes: ClusterNode {} assumed to be dead, because it was not found in the database.", executingClusterNodeID);
					clusterNodeIDs_dead.add(executingClusterNodeID);
					tasksMarkedAsExecutingByDeadClusterNodes.add(task);
				}
				else if (clusterNode.getLastHeartbeatDate().before(oldestClusterNodeHeartbeatDateConsideredAlive)) {
					logger.debug(
							"cleanupTasksMarkedAsExecutingByDeadClusterNodes: ClusterNode {} assumed to be dead, because its last heartbeat was seen {}.",
							new Object[] { executingClusterNodeID, clusterNode.getLastHeartbeatDate() }
					);
					clusterNodeIDs_dead.add(executingClusterNodeID);
					tasksMarkedAsExecutingByDeadClusterNodes.add(task);
				}
				else {
					logger.debug(
							"cleanupTasksMarkedAsExecutingByDeadClusterNodes: ClusterNode {} assumed to be alive, because its last heartbeat was seen {}.",
							new Object[] { executingClusterNodeID, clusterNode.getLastHeartbeatDate() }
					);
					clusterNodeIDs_alive.add(executingClusterNodeID);
				}
			}

			if (!tasksMarkedAsExecutingByDeadClusterNodes.isEmpty()) {
				logger.warn("cleanupTasksMarkedAsExecutingByDeadClusterNodes: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				logger.warn("cleanupTasksMarkedAsExecutingByDeadClusterNodes: There are " + tasks.size() + " Tasks currently marked with executing=true by nodes that are not alive anymore! Will clear that flag now:");
				for (Task task : tasks) {
					logger.warn("cleanupTasksMarkedAsExecutingByDeadClusterNodes:  * executingClusterNodeID={} taskID={}", new Object[] { task.getExecutingClusterNodeID(), JDOHelper.getObjectId(task) });
					task.setExecutingClusterNodeID(null);
					task.setActiveExecID(null);
				}
			}
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void execTasksToDo(TimerParam timerParam)
	throws Exception
	{
		if (!User.USER_ID_SYSTEM.equals(getUserID()))
			throw new SecurityException("This method can only be called by user " + User.USER_ID_SYSTEM);

		if (logger.isDebugEnabled())
			logger.debug("execTasksToDo: principal.organisationID=\""+getOrganisationID()+"\" timerParam.organisationID=\""+timerParam.getOrganisationID()+"\": begin");

		if (!getOrganisationID().equals(timerParam.getOrganisationID()))
			throw new IllegalStateException("principal.organisationID=\""+getOrganisationID()+"\" != timerParam.organisationID=\""+timerParam.getOrganisationID()+"\"");

		String activeExecID = UUID.randomUUID().toString();

		PersistenceManager pm = createPersistenceManager();
		try {
			Date now = new Date();

			List<Task> tasks;
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				pm.getFetchPlan().setGroups(FETCH_GROUPS_TASK);
				tasks = Task.getTasksToDo(pm, now);
			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}

			for (Iterator<Task> it = tasks.iterator(); it.hasNext(); ) {
				Task task = it.next();
				task.setActiveExecID(activeExecID);
				TimerAsyncInvoke.exec(task); // this method does not use the task instance later outside the current transaction (it only fetches the TaskID)
			}

			for (Iterator<Task> it = Task.getTasksToRecalculateNextExecDT(pm, now).iterator(); it.hasNext(); ) {
				Task task = it.next();
				task.calculateNextExecDT();
			}
		} finally {
			pm.close();
		}

		logger.debug("execTasksToDo: organisationID=\""+timerParam.getOrganisationID()+"\": end");
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.timer.TimerManagerRemote#getTaskIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<TaskID> getTaskIDs()
	{
		PersistenceManager pm = createPersistenceManager();
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
		PersistenceManager pm = createPersistenceManager();
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
		PersistenceManager pm = createPersistenceManager();
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
		PersistenceManager pm = createPersistenceManager();
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
