package org.nightlabs.jfire.security.integration;

public class UserManagementSystemCommunicationException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public UserManagementSystemCommunicationException(){
		super();
	}

	public UserManagementSystemCommunicationException(String msg){
		super(msg);
	}

	public UserManagementSystemCommunicationException(String msg, Throwable t){
		super(msg, t);
	}

	public UserManagementSystemCommunicationException(Throwable t){
		super(t);
	}

}
