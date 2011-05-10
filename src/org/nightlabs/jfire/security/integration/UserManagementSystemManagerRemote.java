package org.nightlabs.jfire.security.integration;
import java.util.Collection;
import java.util.List;

import javax.ejb.Remote;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.integration.UserManagementSystemManagerBean.ForbidUserCreationLyfecycleListener;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;

/**
 * Remote interface for UserManagementSystemManagerBean
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Remote
public interface UserManagementSystemManagerRemote {

	/**
	 * Registers a {@link ForbidUserCreationLyfecycleListener} during organisation-init.
	 */
	void initialise();

	/**
	 * Retrieves a {@link List} of detached {@link UserManagementSystem}s by given {@link UserManagementSystemID}s.
	 * 
	 * @param userManagementSystemIDs IDs of {@link UserManagementSystem}s, will throw {@link IllegalArgumentException} if <code>null</code>
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @return list of detached {@link UserManagementSystem} objects
	 */
	List<UserManagementSystem> getUserManagementSystems(Collection<UserManagementSystemID> userManagementSystemIDs, String[] fetchGroups, int maxFetchDepth);
	
	/**
	 * Get IDs of all persistent {@link UserManagementSystem} objects.
	 * Returns an empty {@link Collection} if non of them exist. 
	 * 
	 * @return collection of {@link UserManagementSystemID}s
	 */
	Collection<UserManagementSystemID> getAllUserManagementSystemIDs();
	
	/**
	 * Stores {@link UserManagementSystem} object.
	 * 
	 * @param <T> Specific implementation type of {@link UserManagementSystem}
	 * @param userManagementSystem {@link UserManagementSystem} to store, will return <code>null</code> with a warning if <code>null</code> was specified
	 * @param get If stored object should be detached and returned
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @return Stored detached {@link UserManagementSystem} object if <code>get</code> is <code>true</code> or <code>null</code> otherwise 
	 */
	<T extends UserManagementSystem> T storeUserManagementSystem(T userManagementSystem, boolean get, String[] fetchGroups, int maxFetchDepth);
}
