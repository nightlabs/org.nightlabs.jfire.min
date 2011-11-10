package org.nightlabs.jfire.security.integration;

/**
 * This exception is thrown if some problem occurs while synchronizing data
 * between JFire and {@link UserManagementSystem} in both directions.  
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class UserManagementSystemSyncException extends Exception {

	/**
	 * serialVersionUID for this class 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * {@inheritDoc}
	 */
	public UserManagementSystemSyncException(){
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public UserManagementSystemSyncException(String msg){
		super(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public UserManagementSystemSyncException(String msg, Throwable t){
		super(msg, t);
	}

	/**
	 * {@inheritDoc}
	 */
	public UserManagementSystemSyncException(Throwable t){
		super(t);
	}

}
