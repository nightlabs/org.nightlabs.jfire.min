package org.nightlabs.jfire.security.integration;

import javax.security.auth.login.LoginException;

/**
 * This interface represents a {@link UserManagementSystem} which is synchronizing with JFire data objects.
 * It is supposed that specific {@link UserManagementSystem} implementations will implement this simple interface
 * also having their own implementation if {@link UserManagementSystemSyncEvent}.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 * @param <T> sync event specific to some {@link UserManagementSystem} 
 */
public interface SynchronizableUserManagementSystem<T extends UserManagementSystemSyncEvent> {

	/**
	 * Starts synchronization process. It's driven by {@link UserManagementSystemSyncEvent} objects
	 * which are telling what to do: send data to {@link UserManagementSystem} or recieve it and store
	 * in JFire etc. These events could also contain pointers for the data to be synchronized 
	 * (i.e. a userId for a User object). So please try to keep all the sync-related data inside your
	 * implementations of {@link UserManagementSystemSyncEvent}.
	 * 
	 * @param syncEvent
	 * @throws UserManagementSystemCommunicationException should be thrown when there's some problem in communication/network layer 
	 * @throws LoginException should be thrown in case of authentication failure
	 * @throws UserManagementSystemSyncException is thrown in case any other problems in sync process
	 */
	void synchronize(T syncEvent) throws UserManagementSystemSyncException, LoginException, UserManagementSystemCommunicationException;

}
