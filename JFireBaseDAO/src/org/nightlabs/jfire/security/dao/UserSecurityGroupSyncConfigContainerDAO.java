package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.integration.UserManagementSystemManagerRemote;
import org.nightlabs.jfire.security.integration.UserSecurityGroupSyncConfig;
import org.nightlabs.jfire.security.integration.UserSecurityGroupSyncConfigContainer;
import org.nightlabs.jfire.security.integration.id.UserSecurityGroupSyncConfigContainerID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Get {@link UserSecurityGroupSyncConfigContainer} JDO objects using the JFire client cache.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class UserSecurityGroupSyncConfigContainerDAO extends BaseJDOObjectDAO<UserSecurityGroupSyncConfigContainerID, UserSecurityGroupSyncConfigContainer>{

	private static UserSecurityGroupSyncConfigContainerDAO sharedInstance = null;

	/**
	 * Get the lazily created shared instance.
	 * @return The shared instance
	 */
	public static UserSecurityGroupSyncConfigContainerDAO sharedInstance(){
		if (sharedInstance == null) {
			synchronized (UserSecurityGroupSyncConfigContainerDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new UserSecurityGroupSyncConfigContainerDAO();
			}
		}
		return sharedInstance;
	}

	private UserSecurityGroupSyncConfigContainerDAO(){
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<UserSecurityGroupSyncConfigContainer> retrieveJDOObjects(Set<UserSecurityGroupSyncConfigContainerID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception{
		monitor.beginTask("Fetching "+objectIDs.size()+" user security groups sync configs container data", 1);
		try{
			UserManagementSystemManagerRemote remoteBean = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			Collection<UserSecurityGroupSyncConfigContainer> syncConfigs = remoteBean.getSyncConfigContainers(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			return syncConfigs;
		}catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("Failed fetching user security group sync configs container data!", e);
		}finally{
			monitor.done();
		}
	}

	/**
	 * Get all {@link UserSecurityGroupSyncConfigContainer}s.
	 * 
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return {@link List} of all persistent {@link UserSecurityGroupSyncConfig}s
	 */
	public synchronized List<UserSecurityGroupSyncConfigContainer> getAllSyncConfigContainers(String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor){
		monitor.beginTask("Load all user security group sync configs containers", 1);
		try{
			UserManagementSystemManagerRemote remoteBean = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			Collection<UserSecurityGroupSyncConfigContainerID> ids = remoteBean.getAllSyncConfigContainersIDs();
			monitor.worked(1);
			return getJDOObjects(null, ids, fetchgroups, maxFetchDepth, new SubProgressMonitor(monitor, ids.size()));
		}catch(Exception e){
			monitor.setCanceled(true);
			throw new RuntimeException("Failed to load all user security group sync configs containers!", e);
		}finally{
			monitor.done();
		}
	}
	
	/**
	 * Get a single {@link UserSecurityGroupSyncConfigContainer} by its ID. Simply delegates to {@link #getJDOObject(String, UserSecurityGroupSyncConfigContainerID, String[], int, ProgressMonitor)}.
	 * 
	 * @param syncConfigContainerID The ID of the {@link UserSecurityGroupSyncConfigContainer} to get
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return The requested {@link UserSecurityGroupSyncConfigContainer} object
	 */
	public synchronized UserSecurityGroupSyncConfigContainer getSyncConfigContainer(UserSecurityGroupSyncConfigContainerID syncConfigContainerID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		return getJDOObject(null, syncConfigContainerID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get {@link UserSecurityGroupSyncConfigContainer} objects by their IDs. Simply delegates to {@link #getJDOObjects(String, Collection, String[], int, ProgressMonitor)}
	 * 
	 * @param syncConfigsIDs The IDs of the {@link UserSecurityGroupSyncConfigContainer}s to get
	 * @param fetchgroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return The requested {@link UserSecurityGroupSyncConfigContainer} objects
	 */
	public synchronized List<UserSecurityGroupSyncConfigContainer> getSyncConfigContainers(Set<UserSecurityGroupSyncConfigContainerID> syncConfigsIDs, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor){
		return getJDOObjects(null, syncConfigsIDs, fetchgroups, maxFetchDepth, monitor);
	}

	/**
	 * Stores a {@link UserSecurityGroupSyncConfigContainer} on the server.
	 * 
	 * @param syncConfigContainer The {@link UserSecurityGroupSyncConfigContainer} to store, will throw {@link IllegalArgumentException} if <code>null</code>
	 * @param get If stored object should be detached and returned
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return Stored detached {@link UserSecurityGroupSyncConfigContainer} object if <code>get</code> is <code>true</code> or <code>null</code> otherwise
	 */
	public synchronized UserSecurityGroupSyncConfigContainer storeSyncConfigContainer(UserSecurityGroupSyncConfigContainer syncConfigContainer, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(syncConfigContainer == null){
			throw new IllegalArgumentException("UserSecurityGroupSyncConfigContainer to store must not be null!");
		}

		monitor.beginTask("Storing UserSecurityGroupSyncConfigContainer", 3);
		try{
			UserManagementSystemManagerRemote um = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			monitor.worked(1);

			UserSecurityGroupSyncConfigContainer result = um.storeSyncConfigContainer(syncConfigContainer, get, fetchGroups, maxFetchDepth);
			if (result != null){
				getCache().put(null, result, fetchGroups, maxFetchDepth);
			}

			monitor.worked(1);

			return result;
		}catch (Exception e){
			monitor.setCanceled(true);
			throw new RuntimeException("Failed to store UserSecurityGroupSyncConfig!", e);
		}finally{
			monitor.done();
		}
	}

	/**
	 * Delete persistent {@link UserSecurityGroupSyncConfigContainer} instance by IDs.
	 * 
	 * @param syncConfigContainerID The ID of {@link UserSecurityGroupSyncConfig}s to delete
	 * @param monitor The progress monitor for this action
	 */
	public synchronized void removeUserSecurityGroupSyncConfigs(UserSecurityGroupSyncConfigContainerID syncConfigContainerID, ProgressMonitor monitor){
		monitor.beginTask("Deleting UserSecurityGroupSyncConfigsContainer", 1);
		try{
			UserManagementSystemManagerRemote um = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			um.deleteSyncConfigContainer(syncConfigContainerID);
			monitor.worked(1);
		}catch(Exception e){
			monitor.setCanceled(true);
			throw new RuntimeException("Failed to delete UserSecurityGroupSyncConfigContainer!", e);
		}finally{
			monitor.done();
		}
	}
	
	/**
	 * Get {@link UserSecurityGroupSyncConfigContainer} by given {@link UserSecurityGroupID}.
	 * 
	 * @param groupID The ID of {@link UserSecurityGroup} to get {@link UserSecurityGroupSyncConfigContainer} for
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return {@link UserSecurityGroupSyncConfigContainer} that relate to given {@link UserSecurityGroup}
	 */
	public synchronized UserSecurityGroupSyncConfigContainer getSyncConfigsContainerByGroupID(UserSecurityGroupID groupID, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor){
		monitor.beginTask("Load user security group sync configs container by user security group ID", 1);
		try{
			UserManagementSystemManagerRemote remoteBean = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			UserSecurityGroupSyncConfigContainerID id = remoteBean.getSyncConfigsContainerIDForGroup(groupID);
			monitor.worked(1);
			if (id != null){
				return getJDOObject(null, id, fetchgroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
			}
			return null;
		}catch(Exception e){
			monitor.setCanceled(true);
			throw new RuntimeException("Failed to load user security group sync configs container!", e);
		}finally{
			monitor.done();
		}
	}

}
