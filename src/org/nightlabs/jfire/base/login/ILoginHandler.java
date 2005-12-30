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
