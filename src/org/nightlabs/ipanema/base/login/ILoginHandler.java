/*
 * Created 	on Oct 4, 2004
 * 					by Alexander Bieber
 *
 */
package org.nightlabs.ipanema.base.login;

import javax.security.auth.login.LoginException;
/**
 * Interface to handle client logins. This interface is used instead of 
 * {@link javax.security.auth.callback.CallbackHandler} to do the user interaction.
 * This way we can handle authentication failures and e.g. present the login screen
 * three times. 
 * @author Alexander Bieber
 */
public interface ILoginHandler {
	
	/** 
	 * Implementors are obliged to set correct values for 
	 * loginContext, loginConfigModule, and loginResult.</br>
	 * As the values are not needed before the first server interaction
	 * you may check the values by getting some bean before returning.
	 * This code is executed on the SWT-GUI-thread so any SWT graphical controls can be shown.
	 * 
	 * @param loginContext
	 * @param loginConfigModule
	 * @param loginResult
	 * @throws WorkOfflineException
	 * @see JFireLoginContext
	 * @see LoginConfigModule
	 * @see Login.AsyncLoginResult
	 */
	public void handleLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult) throws LoginException;
}
