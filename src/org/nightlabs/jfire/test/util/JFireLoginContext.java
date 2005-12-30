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

package org.nightlabs.jfire.test.util;

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
