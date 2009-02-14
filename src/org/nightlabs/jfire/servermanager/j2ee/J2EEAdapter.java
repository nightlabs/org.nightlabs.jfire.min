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

package org.nightlabs.jfire.servermanager.j2ee;

import java.io.Serializable;

import javax.naming.InitialContext;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.transaction.UserTransaction;

import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;


/**
 * This interface defines an adapter for vendor specific functionality.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface J2EEAdapter extends Serializable {
	/**
	 * The appropriate {@link J2EEAdapter} will be bound to JNDI under this name.
	 * This is done by {@link JFireServerManagerFactoryImpl}.
	 */
	public static final String JNDI_NAME = "java:/jfire/system/J2EEAdapter";

	public void flushAuthenticationCache()
		throws J2EEAdapterException;

	/**
	 * This method is called by the <tt>JFireServerManagerFactoryImpl</tt> when
	 * it has been created. It must somehow make sure that the method
	 * <tt>ServerStartNotificationListener.serverStarted()</tt> is called when the
	 * J2EE server has deployed all modules that existed while booting it.
	 *
	 * @param listener
	 * @throws Exception
	 */
	public void registerNotificationListenerServerStarted(ServerStartNotificationListener listener)
		throws J2EEAdapterException;

	public UserTransaction getUserTransaction(InitialContext initialContext)
		throws J2EEAdapterException;

	public SecurityReflector getSecurityReflector();

	/**
	 * This method shuts the server down. It should do so nicely.
	 */
	public void shutdown();

	/**
	 * Just like {@link #shutdown()} this method initiates a shutdown, but signals that the server should be restarted by the service wrapper.
	 */
	public void reboot();

	LoginContext createLoginContext(String securityProtocol, CallbackHandler callbackHandler) throws LoginException;
}
