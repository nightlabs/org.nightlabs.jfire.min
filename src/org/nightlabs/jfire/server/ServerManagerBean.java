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

package org.nightlabs.jfire.server;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.nightlabs.ModuleException;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.xml.ModuleDef;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/ServerManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/ServerManager"
 *	type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 **/
public abstract class ServerManagerBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	private static final long serialVersionUID = 1L;

	@Override
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@Override
	public String ping(String message) {
		return super.ping(message);
	}

	/**
	 * @return Whether or not this server is new and needs setup.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public boolean isNewServerNeedingSetup()
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.isNewServerNeedingSetup();
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.permission unchecked="true"
	 */
	@Override
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @return Returns a clone of the main config module.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_ServerAdmin_"
	 */
	public JFireServerConfigModule getJFireServerConfigModule()
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.getJFireServerConfigModule();
		} finally {
			ism.close();
		}
	}

	/**
	 * @throws ConfigException If the configuration is obviously wrong - not all errors are detected, however!
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_ServerAdmin_"
	 */
	public void setJFireServerConfigModule(JFireServerConfigModule cfMod)
	throws ConfigException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			ism.setJFireServerConfigModule(cfMod);
		} finally {
			ism.close();
		}
	}

	/**
	 * Configures the server using the currently configured {@link ServerConfigurator} and
	 * shuts it down if necessary.
	 *
	 * @param delayMSec In case shutdown is necessary, how long to delay it (this method will return immediately).
	 *		This is necessary for having a few secs left to return the client a new web page.
	 * @return <code>true</code>, if the server configuration was changed in a way that requires reboot.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_ServerAdmin_"
	 */
	public boolean configureServerAndShutdownIfNecessary(long delayMSec)
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.configureServerAndShutdownIfNecessary(delayMSec);
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<ModuleDef> getModules(ModuleType moduleType)
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.getModules(moduleType);
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void flushModuleCache()
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			ism.flushModuleCache();
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> getJ2eeRemoteServers()
	{
		return getJFireServerManagerFactory().getJ2eeRemoteServers();
	}

	/**
	 * Get the server's current time. This can - of course - not be used for synchronizing a client,
	 * because it is unknown how long the response travels to the client (latency), but sufficient
	 * for a rough guess whether the server's and client's time are too far off.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public Date getServerTime()
	{
		return new Date();
	}
}
