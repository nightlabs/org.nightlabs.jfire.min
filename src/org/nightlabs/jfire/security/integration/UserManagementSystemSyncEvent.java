package org.nightlabs.jfire.security.integration;

/**
 * Implementations of this interface are used for managing the synchronization process between {@link UserManagementSystem}
 * and JFire. They are accepted by {@link UserManagementSystem#synchronize(UserManagementSystemSyncEvent)} method and are intended
 * to keep all the sync-related data inside like userId or any other information needed by specific UMS within sync process. 
 *  
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public interface UserManagementSystemSyncEvent {

	/**
	 * Get the event type. Generic event types are specified by {@link SyncEventGenericType} enum
	 * but you could always use your own event types inside specific implementation unless your enum
	 * with types will implement {@link SyncEventType}.
	 * 
	 * @return type of the event
	 */
	SyncEventType getEventType();
	
	
	/**
	 * Common interface for enums containig sync event types.
	 * If you are about to add your own sync event types please make sure
	 * that your enum implement this interface.
	 * 
	 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
	 *
	 */
	public interface SyncEventType{
		/**
		 * Get a {@link String} representing enum element. Currently used for logging and debug needs.
		 * @return some string value which represents enum element
		 */
		String stringValue();
	}

	/**
	 * Generic sync event types for {@link UserManagementSystemSyncEvent}.
	 * See {@link SyncEventType} if you want to add your own event types.
	 * 
	 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
	 *
	 */
	enum SyncEventGenericType implements SyncEventType{
		
		SEND_USER("SEND_USER"),										// send user-related data to store in external UserManagementSystem
		FETCH_USER("FETCH_USER"),									// get user-related data from UserManagementSystem and store in JFire
		UMS_REMOVE_USER("UMS_REMOVE_USER"),							// remove corresponding data in UserManagementSystem after JFire user entry (i.e. User or Person) was removed
		JFIRE_REMOVE_USER("JFIRE_REMOVE_USER"),						// remove JFire object (i.e. User or Person) after UserManagementSystem user-related entry was removed
		SEND_AUTHORIZATION("SEND_AUTHORIZATION"),					// send authorization-related data to store in UserManagementSystem
		FETCH_AUTHORIZATION("FETCH_AUTHORIZATION"),					// get authorization-related data from UserMangementSystem and store it in JFire
		UMS_REMOVE_AUTHORIZATION("UMS_REMOVE_AUTHORIZATION"),		// remove authorization-related data in UserManagementSystem after it was removed in JFire
		JFIRE_REMOVE_AUTHORIZATION("JFIRE_REMOVE_AUTHORIZATION");	// remove authorization-related data in JFire after it was removed in UserManagementSystem
        
		private String stringValue;
		
		private SyncEventGenericType(String stringValue){
        	this.stringValue = stringValue;
        }
        
		@Override
        public String stringValue(){
        	return stringValue;
        }
	}
	
}
