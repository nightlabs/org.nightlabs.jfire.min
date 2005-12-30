/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.login;

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
