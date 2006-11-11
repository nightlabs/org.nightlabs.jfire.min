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

package org.nightlabs.jfire.base;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class AuthCallbackHandler implements CallbackHandler
{
	private String organisationID;
	private String userID;
	private String sessionID;
	private String userName;
	private char[] password;

	public AuthCallbackHandler(JFireServerManager ism,
			String organisationID, String userID)
	{
		this(ism, organisationID, userID, ObjectIDUtil.makeValidIDString(null, true));
	}

	public AuthCallbackHandler(JFireServerManager ism,
			String organisationID, String userID, String sessionID)
	{
		this(
				organisationID, userID, sessionID,
				ism.jfireSecurity_createTempUserPassword(organisationID, userID).toCharArray());
	}

	public AuthCallbackHandler(String _organisationID, String _userID, String _sessionID, char[] password)
	{
		this.organisationID = _organisationID;
		this.userID = _userID;
		this.sessionID = _sessionID;
		this.userName = userID + '@' + organisationID + '/' + sessionID;
		this.password = password;
	}

	/**
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback[] callbacks)
	throws IOException,
			UnsupportedCallbackException
	{
//		if (callbacks.length != 2)
//			throw new IllegalArgumentException("callbacks.length != 2!");
//
		for (int i = 0; i < callbacks.length; ++i) {
			Callback cb = callbacks[i];
			if (cb instanceof NameCallback) {
				((NameCallback)cb).setName(userName);
			}
			else if (cb instanceof PasswordCallback) {
				((PasswordCallback)cb).setPassword(password);
			}
			else throw new UnsupportedCallbackException(cb);
		}
			
	}

}
