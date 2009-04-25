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

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.BaseSessionBeanImplEJB3;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/ServerManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/ServerManager"
 *	type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 **/
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class ServerManagerBean
	extends BaseSessionBeanImplEJB3
	implements ServerManagerRemote
{
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.server.ServerManagerRemote#ping(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public String ping(String message) {
		return super.ping(message);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.server.ServerManagerRemote#isNewServerNeedingSetup()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public boolean isNewServerNeedingSetup()
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.isNewServerNeedingSetup();
		} finally {
			ism.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.server.ServerManagerRemote#getJFireServerConfigModule()
	 */
	@RolesAllowed("_ServerAdmin_")
	@Override
	public JFireServerConfigModule getJFireServerConfigModule()
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.getJFireServerConfigModule();
		} finally {
			ism.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.server.ServerManagerRemote#setJFireServerConfigModule(org.nightlabs.jfire.servermanager.config.JFireServerConfigModule)
	 */
	@RolesAllowed("_ServerAdmin_")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.server.ServerManagerRemote#configureServerAndShutdownIfNecessary(long)
	 */
	@RolesAllowed("_ServerAdmin_")
	@Override
	public boolean configureServerAndShutdownIfNecessary(long delayMSec) throws ServerConfigurationException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.configureServerAndShutdownIfNecessary(delayMSec);
		} finally {
			ism.close();
		}
	}

//	/* (non-Javadoc)
//	 * @see org.nightlabs.jfire.server.ServerManagerRemote#getModules(org.nightlabs.jfire.module.ModuleType)
//	 */
//	@RolesAllowed("_Guest_")
//	@Override
//	public List<ModuleDef> getModules(ModuleType moduleType) throws XMLReadException
//	{
//		JFireServerManager ism = getJFireServerManager();
//		try {
//			return ism.getModules(moduleType);
//		} finally {
//			ism.close();
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see org.nightlabs.jfire.server.ServerManagerRemote#flushModuleCache()
//	 */
//	@RolesAllowed("_Guest_")
//	@Override
//	public void flushModuleCache()
//	{
//		JFireServerManager ism = getJFireServerManager();
//		try {
//			ism.flushModuleCache();
//		} finally {
//			ism.close();
//		}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.server.ServerManagerRemote#getJ2eeRemoteServers()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> getJ2eeRemoteServers()
	{
		return getJFireServerManagerFactory().getJ2eeRemoteServers();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.server.ServerManagerRemote#getServerTime()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Date getServerTime()
	{
		return new Date();
	}
}
