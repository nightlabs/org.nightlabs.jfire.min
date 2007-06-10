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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

/**
 * This is the CallbackHandler for the JAAS Login.
 * It does no user interaction it rather takes username and password
 * from the Login class.
 * @author Alexander Bieber
 */
public class LoginCallbackHandler implements CallbackHandler {

	/* (non-Javadoc)
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		try {
			for (int i = 0; i < callbacks.length; i++) {
				// user interaction has been done in implementations of ILoginHandler
				// so here we just pass the values
				if (callbacks[i] instanceof NameCallback) 
							((NameCallback)callbacks[i]).setName(Login.getLogin(false).getLoginContext().getUsername());			
				if (callbacks[i] instanceof PasswordCallback) 
								((PasswordCallback)callbacks[i]).setPassword(Login.getLogin(false).getLoginContext().getPassword().toCharArray());			
			}
		} catch (LoginException x) {
			throw new RuntimeException("Login.getLogin(false) should never throw this exception!", x); //$NON-NLS-1$
		}
			// TODO Maybe more data has to be set ??
	}

}
