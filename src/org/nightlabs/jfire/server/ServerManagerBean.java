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
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;

import org.nightlabs.ModuleException;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/ServerManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/ServerManager"
 *	type="Stateless"
 *
 * @ejb.util generate = "physical" 
 **/
public abstract class ServerManagerBean
	extends BaseSessionBeanImpl
	implements SessionBean 
{

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
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
//		try
//		{
//			System.out.println("ServerManagerBean by " + this.getPrincipalString());
//		}
//		catch (Exception e)
//		{
//			throw new CreateException(e.getMessage());
//		}
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
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
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
			return (JFireServerConfigModule) ism.getJFireServerConfigModule();
		} finally {
			ism.close();
		}
	}

	/**
	 * @return Returns the main config module.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_ServerAdmin_"
	 */
	public void setJFireServerConfigModule(JFireServerConfigModule cfMod)
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			ism.setJFireServerConfigModule(cfMod);
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public List getModules(ModuleType moduleType)
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
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			ism.flushModuleCache();
		} finally {
			ism.close();
		}
	}

//	/**
//	 * @return The instance of the local host.
//	 * 
//	 * @ejb.interface-method
//	 */
//	public LocalServer getLocalServer()
//	{
//		throw new UnsupportedOperationException("NYI");
//		if (localServer != null) return localServer;
//		
//		PersistenceManager pm = sysPMF.getPersistenceManager();
//		Iterator it = pm.getExtent(LocalServer.class, false).iterator();
//		if (!it.hasNext()) 
//			throw new IllegalStateException("There is no server registered as localServer! Thus, I don't know who I am!");
//		
//		localServer = (LocalServer) it.next();
//		
//		if (it.hasNext()) 
//			throw new IllegalStateException("There is more than one server registered as localServer! Thus, I don't know who I am!");
//		
//		pm.retrieve(localServer.getServer());
//		pm.makeTransient(localServer);
//		pm.makeTransient(localServer.getServer());		
//		
//		pm.close();		
//		return localServer;
//	}
	
//	/** 
//	 * @param server
//	 * @return true, if the given server is the local machine
//	 * 
//	 * @ejb.interface-method 
//	 */
//	public boolean isLocalhost(Server server)
//	{
//		return isLocalhost(server.getServerID());
//	}
//	
//	/** 
//	 * @param serverId
//	 * @return true, if the given server is the local machine
//	 * 
//	 * @ejb.interface-method 
//	 */
//	public boolean isLocalhost(String serverId)
//	{
//		if (serverId == null)
//			throw new NullPointerException("Param serverId must not be null!");
//
//		Server server = getLocalServer().getServer();
//		if (server == null)
//			throw new NullPointerException("localServer.server must not be null!");
//
//		return serverId.equals(server.getServerID());
//	}

}
