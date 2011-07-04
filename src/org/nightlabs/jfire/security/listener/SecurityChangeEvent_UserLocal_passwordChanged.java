package org.nightlabs.jfire.security.listener;

import org.nightlabs.jfire.security.User;

/**
 * Instances of this event class are propagated to
 * {@link SecurityChangeListener#on_UserLocal_passwordChanged(SecurityChangeEvent_UserLocal_passwordChanged))}.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class SecurityChangeEvent_UserLocal_passwordChanged extends SecurityChangeEvent{
	
	/**
	 * {@link User} which has new password set
	 */
	private User user;
	
	/**
	 * New password as plain text, so please be careful when using this field
	 */
	private String newPassword;
	
	/**
	 * Constructs new {@link SecurityChangeEvent_UserLocal_passwordChanged} object with given {@link User} and new password.
	 * 
	 * @param user The {@link User} object which has new password set
	 * @param newPassword The new password as a plin text
	 */
	public SecurityChangeEvent_UserLocal_passwordChanged(User user, String newPassword) {
		this.user = user;
		this.newPassword = newPassword;
	}
	
	/**
	 * Get User.
	 * 
	 * @return {@link User} which has new password set
	 */
	public User getUser() {
		return user;
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
