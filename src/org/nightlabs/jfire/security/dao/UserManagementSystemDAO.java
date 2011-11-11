package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemManagerRemote;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Get {@link UserManagementSystem} JDO objects using the JFire client cache.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class UserManagementSystemDAO extends BaseJDOObjectDAO<UserManagementSystemID, UserManagementSystem>{

	private static UserManagementSystemDAO sharedInstance = null;

	/**
	 * Get the lazily created shared instance.
	 * @return The shared instance
	 */
	public static UserManagementSystemDAO sharedInstance(){
		if (sharedInstance == null) {
			synchronized (UserManagementSystemDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new UserManagementSystemDAO();
			}
		}
		return sharedInstance;
	}

	private UserManagementSystemDAO(){
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<UserManagementSystem> retrieveJDOObjects(Set<UserManagementSystemID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception{
		monitor.beginTask("Fetching "+objectIDs.size()+" user management systems data", 1);
		try{
			UserManagementSystemManagerRemote remoteBean = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			Collection<UserManagementSystem> userManagementSystems = remoteBean.getUserManagementSystems(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			return userManagementSystems;
		}catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("Failed fetching user management systems data!", e);
		}finally{
			monitor.done();
		}
	}

	/**
	 * Get all {@link UserManagementSystem}s.
	 * 
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return {@link List} of all persistent {@link UserManagementSystem}s
	 */
	public synchronized List<UserManagementSystem> getAllUserManagementSystems(String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor){
		monitor.beginTask("Load all user management systems", 1);
		try{
			UserManagementSystemManagerRemote remoteBean = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			Collection<UserManagementSystemID> ids = remoteBean.getAllUserManagementSystemIDs();
			monitor.worked(1);
			return getJDOObjects(null, ids, fetchgroups, maxFetchDepth, new SubProgressMonitor(monitor, ids.size()));
		}catch(Exception e){
			monitor.setCanceled(true);
			throw new RuntimeException("Failed to load all UserManagementSystems!", e);
		}finally{
			monitor.done();
		}
	}
	
	/**
	 * Get a single {@link UserManagementSystem} by its ID. Simply delegates to {@link #getJDOObject(String, UserManagementSystemID, String[], int, ProgressMonitor)}.
	 * 
	 * @param userManagementSystemID The ID of the UserManagementSystem to get
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return The requested UserManagementSystem object
	 */
	public synchronized UserManagementSystem getUserManagementSystem(UserManagementSystemID userManagementSystemID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		return getJDOObject(null, userManagementSystemID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get {@link UserManagementSystem} objects by their IDs. Simply delegates to {@link #getJDOObjects(String, Collection, String[], int, ProgressMonitor)}
	 * 
	 * @param userManagementSystemIDs The IDs of the UserManagementSystems to get
	 * @param fetchgroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return The requested UserManagementSystem objects
	 */
	public synchronized List<UserManagementSystem> getUserManagementSystems(Set<UserManagementSystemID> userManagementSystemIDs, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor){
		return getJDOObjects(null, userManagementSystemIDs, fetchgroups, maxFetchDepth, monitor);
	}

	/**
	 * Stores a {@link UserManagementSystem} on the server.
	 * 
	 * @param <T> Specific implementation type of {@link UserManagementSystem}
	 * @param userManagementSystem The UserManagementSystem to store, will throw {@link IllegalArgumentException} if <code>null</code>
	 * @param get If stored object should be detached and returned
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return Stored detached {@link UserManagementSystem} object if <code>get</code> is <code>true</code> or <code>null</code> otherwise
	 */
	public synchronized <T extends UserManagementSystem> T storeUserManagementSystem(T userManagementSystem, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(userManagementSystem == null){
			throw new IllegalArgumentException("UserManagementSystem to store must not be null!");
		}

		monitor.beginTask("Storing UserManagementSystem", 3);
		try{
			UserManagementSystemManagerRemote um = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			monitor.worked(1);

			T result = um.storeUserManagementSystem(userManagementSystem, get, fetchGroups, maxFetchDepth);
			if (result != null){
				getCache().put(null, result, fetchGroups, maxFetchDepth);
			}

			monitor.worked(1);

			return result;
		}catch (Exception e){
			monitor.setCanceled(true);
			throw new RuntimeException("Failed to store UserManagementSystem!", e);
		}finally{
			monitor.done();
		}
	}

	/**
	 * Delete persistent {@link UserManagementSystem} instances by their IDs.
	 * 
	 * @param userManagementSystemIDs The {@link Collection} of IDs of {@link UserManagementSystem}s to delete
	 * @param monitor The progress monitor for this action
	 */
	public synchronized void removeUserManagementSystems(Collection<UserManagementSystemID> userManagementSystemIDs, ProgressMonitor monitor){
		monitor.beginTask("Deleting UserManagementSystem(s)", userManagementSystemIDs.size());
		try{
			UserManagementSystemManagerRemote um = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			for (UserManagementSystemID userManagementSystemID : userManagementSystemIDs){
				um.deleteUserManagementSystem(userManagementSystemID);
				monitor.worked(1);
			}
		}catch(Exception e){
			monitor.setCanceled(true);
			throw new RuntimeException("Failed to delete UserManagementSystem!", e);
		}finally{
			monitor.done();
		}
	}
}
