package org.nightlabs.jfire.security.listener;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

/**
 * Instances of this event class are propagated to
 * {@link SecurityChangeListener#on_UserLocal_passwordChanged(SecurityChangeEvent_UserLocal_passwordChanged))}.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class SecurityChangeEvent_UserLocal_passwordChanged extends SecurityChangeEvent{
	
	/**
	 * {@link UserID} of a {@link User} which has new password set
	 */
	private UserID userID;
	
	/**
	 * New password as plain text, so please be careful when using this field
	 */
	private String newPassword;
	
	/**
	 * Constructs new {@link SecurityChangeEvent_UserLocal_passwordChanged} object with given {@link User} and new password.
	 * 
	 * @param userID The {@link UserID} of a {@link User} object which has new password set
	 * @param newPassword The new password as a plin text
	 */
	public SecurityChangeEvent_UserLocal_passwordChanged(UserID userID, String newPassword) {
		this.userID = userID;
		this.newPassword = newPassword;
	}
	
	/**
	 * Get UserID.
	 * 
	 * @return {@link UserID} of a {@link User} which has new password set
	 */
	public UserID getUserID() {
		return userID;
	}
	
	/**
	 * Get new password as plain text, so be careful when making use if this value.
	 * 
	 * @return new password as plain text
	 */
	public String getNewPassword() {
		return newPassword;
	}

}
