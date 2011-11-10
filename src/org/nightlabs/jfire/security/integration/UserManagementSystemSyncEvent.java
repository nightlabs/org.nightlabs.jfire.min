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
	interface SyncEventType{
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
		
		FETCH("FETCH"),					// get data from UserManagementSystem and store in JFire
		SEND("SEND"),					// send data to store in UserManagementSystem
		SEND_DELETE("SEND_DELETE"),		// delete data in UserManagementSystem after JFire entry was deleted
		FETCH_DELETE("FETCH_DELETE");	// delete JFire object after UserManagementSystem entry was deleted
        
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
