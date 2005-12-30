/*
 * Created 	on Nov 22, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.login;

import org.eclipse.jface.action.IAction;

/**
 * LoginStateListeners are notified whenever the login state
 * of the RCP client changes.
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface LoginStateListener {
	
	/**
	 * Called whenever the login state changes to one of the following:
	 * <ul>
	 * 	<li>{@link Login#LOGINSTATE_LOGGED_IN} user has logged in.</li>
	 * 	<li>{@link Login#LOGINSTATE_LOGGED_OUT} user has logged out.</li>
	 * 	<li>{@link Login#LOGINSTATE_OFFLINE} user decided to work offline.</li>
	 * <ul>
	 * Note that the param action is likely to be null, depending on what did
	 * or didn't pass to {@link Login#addLoginStateListener(LoginStateListener)}
	 * or {@link Login#addLoginStateListener(LoginStateListener, IAction)}
	 * 
	 * @param loginState The login state the user switched to
	 * @param action A action associated to this listener
	 * 
	 * @see Login#addLoginStateListener(LoginStateListener)
	 * @see Login#addLoginStateListener(LoginStateListener, IAction)
	 */
	public void loginStateChanged(int loginState, IAction action);
}
