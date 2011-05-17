package org.nightlabs.jfire.security.integration;
import java.util.Collection;
import java.util.List;

import javax.ejb.Remote;
import javax.resource.spi.IllegalStateException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.integration.UserManagementSystemManagerBean.ForbidUserCreationLyfecycleListener;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemTypeID;

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

	/**
	 * Get IDs of all persistent {@link UserManagementSystemType} objects.
	 * Returns an empty {@link Collection} if non of them exist. 
	 * 
	 * @return collection of {@link UserManagementSystemTypeID}s
	 */
	Collection<UserManagementSystemTypeID> getAllUserManagementSystemTypesIDs();

	/**
	 * Retrieves a {@link List} of detached {@link UserManagementSystemType}s by given {@link UserManagementSystemTypeID}s.
	 * 
	 * @param userManagementSystemIDs IDs of {@link UserManagementSystemType}s, will throw {@link IllegalArgumentException} if <code>null</code>
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @return list of detached {@link UserManagementSystemType} objects
	 */
	List<UserManagementSystemType<?>> getUserManagementSystemTypes(Collection<UserManagementSystemTypeID> userManagementSystemIDs, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Stores {@link UserManagementSystemType} object. Note that no new instance of {@link UserManagementSystemType} can be stored with 
	 * this method. It should update existent instances only. New {@link UserManagementSystemType} objects should be created by 
	 * specific {@link UserManagementSystem} implementation projects (see {@link UserManagementSystemType#createSingleInstance(javax.jdo.PersistenceManager, Class, String)}).
	 * 
	 * @param <T> Specific implementation type of {@link UserManagementSystemType}
	 * @param userManagementSystemType {@link UserManagementSystemType} to store, will return <code>null</code> with a warning if <code>null</code> was specified
	 * @param get If stored object should be detached and returned
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @return Stored detached {@link UserManagementSystemType} object if <code>get</code> is <code>true</code> or <code>null</code> otherwise 
	 * @throws IllegalStateException Is thrown if either no instance of this {@link UserManagementSystemType} exists in datastore or instance being stored has different ID than existent one 
	 */
	<T extends UserManagementSystemType<?>> T storeUserManagementSystemType(T userManagementSystemType, boolean get, String[] fetchGroups, int maxFetchDepth) throws IllegalStateException;

	/**
	 * Delete persistent {@link UserManagementSystem} instance by its ID.
	 * 
	 * @param userManagementSystemID The {@link UserManagementSystemID} of instance to delete
	 */
	void deleteUserManagementSystem(UserManagementSystemID userManagementSystem);
}
