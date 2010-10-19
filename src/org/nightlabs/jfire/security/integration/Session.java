package org.nightlabs.jfire.security.integration;

import java.util.UUID;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.util.Util;

/**
 * Descriptor identifying a successful login. One reason to keep such an object
 * is to be able to extend it later without breaking existing implementations of UserManagementSystem.
 * Additionally, the class might be subclassed to keep and track additional information as needed 
 * by a specific {@link UserManagementSystem}
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class Session {

	/**
	 * The unique identifier of this session
	 */
	private UUID sessionID;
	
	/**
	 * A copy of the original {@link LoginData} without password. 
	 */
	private LoginData loginData;
	
	/**
	 * Constructor clones input {@link LoginData} and creates sessionID from bytes of a string formed with
	 * userID and organisationID
	 * 
	 * @param loginData
	 */
	public Session(LoginData loginData){
		
		LoginData loginDataCopy = Util.cloneSerializable(loginData);
		loginDataCopy.setPassword("");
		this.loginData = loginDataCopy;
		
		this.sessionID = UUID.nameUUIDFromBytes(
				(this.loginData.getUserID()+LoginData.USER_ORGANISATION_SEPARATOR+this.loginData.getOrganisationID()).getBytes()
				);

	}
	
	/**
	 * 
	 * @return loginData without a password
	 */
	public LoginData getLoginData() {
		return loginData;
	}
	
	/**
	 * 
	 * @return unique session ID
	 */
	public UUID getSessionID() {
		return sessionID;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Session){
			return sessionID.equals(((Session) obj).getSessionID());
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loginData == null) ? 0 : loginData.hashCode());
		result = prime * result + ((sessionID == null) ? 0 : sessionID.hashCode());
		return result;
	}
	
}
