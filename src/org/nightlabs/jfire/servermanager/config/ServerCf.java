/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 14.06.2004
 */
package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.server.id.ServerID;

import org.nightlabs.jdo.ObjectIDUtil;

/**
 * @author marco
 */
public class ServerCf
implements Serializable, Cloneable
{
	public ServerCf() { }

  public ServerCf(String _serverID)
  {
  	if (!ObjectIDUtil.isValidIDString(_serverID))
			throw new IllegalArgumentException("serverID \""+_serverID+"\" is not a valid id!");
  	this.serverID = _serverID;
  }

  private String serverID = null;
	private String serverName = null;
	private String j2eeServerType = null;
//	private String initialContextFactory = null;
	private String initialContextURL = null;


	/**
	 * @return Returns the serverID.
	 */
	public String getServerID() {
		return serverID;
	}
	/**
	 * @param serverID The serverID to set.
	 */
	public void setServerID(String _serverID) {
		if (!ObjectIDUtil.isValidIDString(_serverID))
			throw new IllegalArgumentException("serverID \""+_serverID+"\" is not a valid id!");
		this.serverID = _serverID;
	}
	/**
	 * @return Returns the serverName.
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @param serverName The serverName to set.
	 */
	public void setServerName(String _serverName)
	{
		if (_serverName == null)
			throw new NullPointerException("serverName must not be null!");
		if ("".equals(_serverName))
			throw new IllegalArgumentException("serverName must not be an empty string!");
		this.serverName = _serverName;
	}

//	/**
//	 * @return Returns the initialContextFactory.
//	 */
//	public String getInitialContextFactory() {
//		return initialContextFactory;
//	}
//
//	/**
//	 * @param initialContextFactory The initialContextFactory to set.
//	 */
//	public void setInitialContextFactory(String initialContextFactory) {
//		this.initialContextFactory = initialContextFactory;
//	}

	/**
	 * @return Returns the initialContextURL.
	 */
	public String getInitialContextURL() {
		return initialContextURL;
	}

	/**
	 * @param initialContextURL The initialContextURL to set.
	 */
	public void setInitialContextURL(String initialContextURL) {
		this.initialContextURL = initialContextURL;
	}
	/**
	 * @return Returns the j2eeServerType.
	 */
	public String getJ2eeServerType()
	{
		return j2eeServerType;
	}
	/**
	 * @param serverType The j2eeServerType to set.
	 */
	public void setJ2eeServerType(String serverType)
	{
		j2eeServerType = serverType;
	}

	/**
	 * This method creates a JDO Server object with the given persistenceManager
	 * in case it does not yet exist.
	 *
	 * @param pm The PersistenceManager in which's datastore the Server should be
	 * 	created.
	 */
	public Server createServer(PersistenceManager pm)
	{
		// Initialize meta data.
		pm.getExtent(Server.class, true);

		// Fetch/create Server instance.
		Server server;
		try {
			server = (Server)pm.getObjectById(ServerID.create(getServerID()), true);
		} catch (JDOObjectNotFoundException x) {
			server = new Server(getServerID());
			server.setServerName(getServerName());
			server.setJ2eeServerType(getJ2eeServerType());
//			server.setInitialContextFactory(getInitialContextFactory());
			server.setInitialContextURL(getInitialContextURL());
			pm.makePersistent(server);
		}
		return server;
	}

	public void init()
	{
		if (serverID == null)
			serverID = "rename.me.NightLabs.de";

		if (serverName == null)
			serverName = "NightLabs Demo Server (please rename)";

		if (j2eeServerType == null)
			j2eeServerType = Server.J2EESERVERTYPE_JBOSS32X;

//		if (initialContextFactory == null)
//			initialContextFactory = "org.jboss.security.jndi.LoginInitialContextFactory"; // "org.jnp.interfaces.NamingContextFactory";

		if (initialContextURL == null)
			initialContextURL = "jnp://127.0.0.1:1099";
	}
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone()
	{
		ServerCf res = new ServerCf(this.serverID);
		res.serverName = this.serverName;
		res.j2eeServerType = this.j2eeServerType;
		res.initialContextURL = this.initialContextURL;
		return res; 
	}
}
