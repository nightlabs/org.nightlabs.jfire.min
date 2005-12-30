/*
 * Created 	on Oct 4, 2004
 * 					by Alexander Bieber
 *
 */
package org.nightlabs.jfire.base.login;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * This subclass of LoginContext additionally provides UserCredentials.
 * @author Alexander Bieber
 */
public class JFireLoginContext extends LoginContext {

	/**
	 * @param name
	 * @throws javax.security.auth.login.LoginException
	 */
	public JFireLoginContext(String name) throws LoginException {
		super(name);
	}

	/**
	 * @param name
	 * @param subject
	 * @throws javax.security.auth.login.LoginException
	 */
	public JFireLoginContext(String name, Subject subject)
			throws LoginException {
		super(name, subject);
	}

	/**
	 * @param name
	 * @param callbackHandler
	 * @throws javax.security.auth.login.LoginException
	 */
	public JFireLoginContext(String name, CallbackHandler callbackHandler)
			throws LoginException {
		super(name, callbackHandler);
	}

	/**
	 * @param name
	 * @param subject
	 * @param callbackHandler
	 * @throws javax.security.auth.login.LoginException
	 */
	public JFireLoginContext(String name, Subject subject,
			CallbackHandler callbackHandler) throws LoginException {
		super(name, subject, callbackHandler);
	}
	
	private String userID = null;
	private String organisationID = null;
	private String password = null;
	public static final String USERID_MIDFIX = "@";
	
	/**
	 * Sets the user credentials.
	 * @param username
	 * @param organisationID
	 * @param password
	 */
	public void setCredentials(
			String userID,
			String organisationID,
			String password
	){
		this.userID = userID;
		this.organisationID = organisationID;
		this.password = password;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	public String getPassword() {
		return password;
	}
	public String getUserID() {
		return userID;
	}
	
	/**
	 * Returns userID@organisationID if both set. If at least one these is not set null is returned
	 * @return
	 */
	public String getUsername(){
		if ((userID == null) || (organisationID == null))
			return null;
		return userID+USERID_MIDFIX+organisationID;
	}
}
