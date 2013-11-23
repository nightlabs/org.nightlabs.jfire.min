package org.nightlabs.jfire.security.integration;

/**
 * This exception is thrown when we can't communicate to UserManagementSystem
 * due to some connection problems.  
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class UserManagementSystemCommunicationException extends Exception{

	private static final long serialVersionUID = 1L;
	
	/**
	 * {@inheritDoc}
	 */
	public UserManagementSystemCommunicationException(){
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public UserManagementSystemCommunicationException(String msg){
		super(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public UserManagementSystemCommunicationException(String msg, Throwable t){
		super(msg, t);
	}

	/**
	 * {@inheritDoc}
	 */
	public UserManagementSystemCommunicationException(Throwable t){
		super(t);
	}

}
