package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.integration.UserManagementSystemManagerRemote;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Get {@link UserManagementSystemType} JDO objects using the JFire client cache.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class UserManagementSystemTypeDAO extends BaseJDOObjectDAO<UserManagementSystemTypeID, UserManagementSystemType<?>>{

	private static UserManagementSystemTypeDAO sharedInstance = null;

	/**
	 * Get the lazily created shared instance.
	 * @return The shared instance
	 */
	public static UserManagementSystemTypeDAO sharedInstance(){
		if (sharedInstance == null) {
			synchronized (UserManagementSystemTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new UserManagementSystemTypeDAO();
			}
		}
		return sharedInstance;
	}

	private UserManagementSystemTypeDAO(){
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<UserManagementSystemType<?>> retrieveJDOObjects(Set<UserManagementSystemTypeID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception{
		monitor.beginTask("Fetching "+objectIDs.size()+" user management system types data", 1);
		try{
			UserManagementSystemManagerRemote remoteBean = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			Collection<UserManagementSystemType<?>> userManagementSystemTypes = remoteBean.getUserManagementSystemTypes(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			return userManagementSystemTypes;
		}catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("Failed fetching user management system types data!", e);
		}finally{
			monitor.done();
		}
	}

	/**
	 * Get a single {@link UserManagementSystemType} by its ID. Simply delegates to {@link #getJDOObject(String, UserManagementSystemTypeID, String[], int, ProgressMonitor)}.
	 * 
	 * @param userManagementSystemTypeID The ID of the {@link UserManagementSystemType} to get
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return The requested {@link UserManagementSystemType} object
	 */
	public synchronized UserManagementSystemType<?> getUserManagementSystem(UserManagementSystemTypeID userManagementSystemTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		return getJDOObject(null, userManagementSystemTypeID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get {@link UserManagementSystemType} objects by their IDs. Simply delegates to {@link #getJDOObjects(String, Collection, String[], int, ProgressMonitor)}
	 * 
	 * @param userManagementSystemTypeIDs The IDs of the UserManagementSystemTypes to get
	 * @param fetchgroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return The requested {@link UserManagementSystemType} objects
	 */
	public synchronized List<UserManagementSystemType<?>> getUserManagementSystems(Set<UserManagementSystemTypeID> userManagementSystemTypeIDs, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor){
		return getJDOObjects(null, userManagementSystemTypeIDs, fetchgroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all {@link UserManagementSystemType}s.
	 * 
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return {@link List} of all persistent {@link UserManagementSystemType}s
	 */
	public synchronized List<UserManagementSystemType<?>> getAllUserManagementSystemTypes(String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor){
		monitor.beginTask("Load all user management system types", 1);
		try{
			UserManagementSystemManagerRemote remoteBean = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			Collection<UserManagementSystemTypeID> ids = remoteBean.getAllUserManagementSystemTypesIDs();
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
	 * Stores a {@link UserManagementSystemType} on the server.
	 * 
	 * @param <T> Specific implementation type of {@link UserManagementSystemType}
	 * @param userManagementSystemType The {@link UserManagementSystemType} to store, will throw {@link IllegalArgumentException} if <code>null</code>
	 * @param get If stored object should be detached and returned
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action
	 * @return Stored detached {@link UserManagementSystemType} object if <code>get</code> is <code>true</code> or <code>null</code> otherwise
	 */
	public synchronized <T extends UserManagementSystemType<?>> T storeUserManagementSystemType(T userManagementSystemType, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(userManagementSystemType == null){
			throw new IllegalArgumentException("UserManagementSystemType to store must not be null!");
		}

		monitor.beginTask("Storing UserManagementSystemType", 3);
		try{
			UserManagementSystemManagerRemote um = getEjbProvider().getRemoteBean(UserManagementSystemManagerRemote.class);
			monitor.worked(1);

			T result = um.storeUserManagementSystemType(userManagementSystemType, get, fetchGroups, maxFetchDepth);
			if (result != null){
				getCache().put(null, result, fetchGroups, maxFetchDepth);
			}

			monitor.worked(1);

			return result;
		}catch (Exception e){
			monitor.setCanceled(true);
			throw new RuntimeException("Failed to store UserManagementSystemType!", e);
		}finally{
			monitor.done();
		}
	}

}
