/**
 *
 */
package org.nightlabs.jfire.timer.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.TimerManager;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class TaskDAO extends BaseJDOObjectDAO<TaskID, Task> {


	public static final String[] DEFAULT_FETCH_GROUP = { FetchPlan.DEFAULT,
		Task.FETCH_GROUP_NAME, Task.FETCH_GROUP_DESCRIPTION,
		Task.FETCH_GROUP_USER
	};

	/**
	 *
	 */
	protected TaskDAO() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Collection<Task> retrieveJDOObjects(Set<TaskID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception {
		TimerManager timerManager = JFireEjbUtil.getBean(TimerManager.class, SecurityReflector.getInitialContextProperties());
		return timerManager.getTasks(objectIDs, fetchGroups, maxFetchDepth);
	}

	public Task getTask(TaskID taskID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		return getJDOObject(null, taskID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Task> getTasks(
			Collection<TaskID> taskIDs,
			String[] fetchGroups, ProgressMonitor monitor) 
	{
		return getJDOObjects(null, taskIDs, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	public List<Task> getTasks(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			TimerManager timerManager = JFireEjbUtil.getBean(TimerManager.class, SecurityReflector.getInitialContextProperties());
			try {
				List<TaskID> promoterIDs = timerManager.getTaskIDs();

				return getTasks(promoterIDs, fetchGroups, monitor);
			} finally {
				timerManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public synchronized List<Task> getTasks(String taskTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			TimerManager timerManager = JFireEjbUtil.getBean(TimerManager.class, SecurityReflector.getInitialContextProperties());
			try {
				List<TaskID> promoterIDs = timerManager.getTaskIDs(taskTypeID);

				return getTasks(promoterIDs, fetchGroups, monitor);
			} finally {
				timerManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	public synchronized Task storeTask(Task task, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			TimerManager m = JFireEjbUtil.getBean(TimerManager.class, SecurityReflector.getInitialContextProperties());
			Task newTask = m.storeTask(task, get, fetchGroups, maxFetchDepth);
			if (newTask != null)
				getCache().put(null, newTask, fetchGroups, maxFetchDepth);
			return newTask;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static TaskDAO sharedInstance;

	public static TaskDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new TaskDAO();
		return sharedInstance;
	}

}
