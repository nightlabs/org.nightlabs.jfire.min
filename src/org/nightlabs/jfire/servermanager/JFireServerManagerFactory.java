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

package org.nightlabs.jfire.servermanager;

import java.util.List;

import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.ServerCf;


/**
 * @author marco
 */
public interface JFireServerManagerFactory
{
	public static final String JNDI_NAME = "java:/jfire/system/JFireServerManagerFactory";

	public JFireServerManager getJFireServerManager();

	public JFireServerManager getJFireServerManager(JFirePrincipal jfirePrincipal);

//	public String getInitialContextFactory(String j2eeServerTypeRemote, boolean throwExceptionIfUnknownServerType);

	public String getLoginInitialContextFactory(String j2eeServerTypeRemote, String protocol, boolean throwExceptionIfNotFound);

	public String getAnonymousInitialContextFactory(String j2eeServerTypeRemote, String protocol, boolean throwExceptionIfNotFound);

	public List<J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> getJ2eeRemoteServers();
	public J2eeServerTypeRegistryConfigModule.J2eeRemoteServer getJ2eeRemoteServer(String j2eeServerType);

	public ServerCf getLocalServer();

	public boolean isUpAndRunning();

	public boolean isShuttingDown();

	public boolean containsOrganisation(String organisationID);
}
