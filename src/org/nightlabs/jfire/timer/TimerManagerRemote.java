package org.nightlabs.jfire.timer;

import java.util.Collection;
import java.util.List;

import javax.ejb.Remote;

import org.nightlabs.jfire.timer.id.TaskID;

@Remote
public interface TimerManagerRemote 
{
	String ping(String message);

	List<TaskID> getTaskIDs();

	List<TaskID> getTaskIDs(String taskTypeID);

	List<Task> getTasks(Collection<TaskID> taskIDs, String[] fetchGroups,
			int maxFetchDepth);

	Task storeTask(Task task, boolean get, String[] fetchGroups,
			int maxFetchDepth);
}