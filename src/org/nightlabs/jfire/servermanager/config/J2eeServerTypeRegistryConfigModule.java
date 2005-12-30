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

package org.nightlabs.jfire.servermanager.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.servermanager.j2ee.VendorAdapterJBoss;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;
import org.nightlabs.config.Initializable;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class J2eeServerTypeRegistryConfigModule extends ConfigModule
{
	public static class J2eeLocalServer
		implements Initializable
	{
		private J2eeServerTypeRegistryConfigModule cfMod;

		private String j2eeServerType;
		private String j2eeVendorAdapterClassName;
		private List j2eeRemoteServers;

		private Map j2eeRemoteServersByServerType = null;

		/**
		 * @see org.nightlabs.config.Initializable#init()
		 */
		public void init() throws InitException
		{
			if (j2eeRemoteServers == null) {
				j2eeRemoteServers = new ArrayList();
				if (cfMod != null) cfMod.setChanged();
			}
			else {

				for (Iterator it = j2eeRemoteServers.iterator(); it.hasNext(); ) {
					J2eeRemoteServer server = (J2eeRemoteServer)it.next();
					server.cfMod = cfMod;
					server.init();
				}

			}
		}
		
		/**
		 * @return Returns the j2eeRemoteServers.
		 */
		public List getJ2eeRemoteServers()
		{
			return j2eeRemoteServers;
		}
		/**
		 * @param remoteServers The j2eeRemoteServers to set.
		 */
		public void setJ2eeRemoteServers(List remoteServers)
		{
			j2eeRemoteServers = remoteServers;
			if (cfMod != null) cfMod.setChanged();
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
			if (cfMod != null) cfMod.setChanged();
		}
		/**
		 * @return Returns the j2eeVendorAdapterClassName.
		 */
		public String getJ2eeVendorAdapterClassName()
		{
			return j2eeVendorAdapterClassName;
		}
		/**
		 * @param vendorAdapterClassName The j2eeVendorAdapterClassName to set.
		 */
		public void setJ2eeVendorAdapterClassName(String vendorAdapterClassName)
		{
			j2eeVendorAdapterClassName = vendorAdapterClassName;
			if (cfMod != null) cfMod.setChanged();
		}
		public void addJ2eeRemoteServer(J2eeRemoteServer remoteServer)
		{
			j2eeRemoteServers.add(remoteServer);
			if (j2eeRemoteServersByServerType != null)
				j2eeRemoteServersByServerType.put(remoteServer.getJ2eeServerType(), remoteServer);

			if (cfMod != null) cfMod.setChanged();
		}

		public J2eeRemoteServer getJ2eeRemoteServer(String j2eeServerType)
		{
			if (j2eeRemoteServersByServerType == null) {
				Map m = new HashMap();

				for (Iterator it = j2eeRemoteServers.iterator(); it.hasNext(); ) {
					J2eeRemoteServer s = (J2eeRemoteServer) it.next();
					m.put(s.getJ2eeServerType(), s);
				}

				j2eeRemoteServersByServerType = m;
			}

			return (J2eeRemoteServer) j2eeRemoteServersByServerType.get(j2eeServerType);
		}
	}

	public static class J2eeRemoteServer
		implements Initializable
	{
		private J2eeServerTypeRegistryConfigModule cfMod;

		private String j2eeServerType;
		private String initialContextFactory;
		
		/**
		 * @see org.nightlabs.config.Initializable#init()
		 */
		public void init() throws InitException
		{
		}
		/**
		 * @return Returns the initialContextFactory.
		 */
		public String getInitialContextFactory()
		{
			return initialContextFactory;
		}
		/**
		 * @param initialContextFactory The initialContextFactory to set.
		 */
		public void setInitialContextFactory(String initialContextFactory)
		{
			this.initialContextFactory = initialContextFactory;
			if (cfMod != null) cfMod.setChanged();
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
			if (cfMod != null) cfMod.setChanged();
		}
	}

	private List j2eeLocalServers;
	
	/**
	 * @see org.nightlabs.config.ConfigModule#init()
	 */
	public void init() throws InitException
	{
		if (j2eeLocalServers == null) {
			j2eeLocalServers = new ArrayList();
			
			J2eeLocalServer localServer;
			J2eeRemoteServer remoteServer;

			// default config for local jboss 3.2x
			localServer = new J2eeLocalServer();
			localServer.cfMod = this;
			localServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS32X);
			localServer.setJ2eeVendorAdapterClassName(VendorAdapterJBoss.class.getName());
			localServer.init();

			remoteServer = new J2eeRemoteServer();
			remoteServer.cfMod = this;
			remoteServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS32X);
			remoteServer.setInitialContextFactory("org.nightlabs.authentication.jboss.LoginInitialContextFactory");
			remoteServer.init();
			localServer.addJ2eeRemoteServer(remoteServer);

			remoteServer = new J2eeRemoteServer();
			remoteServer.cfMod = this;
			remoteServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS40X);
			remoteServer.setInitialContextFactory("org.nightlabs.authentication.jboss.LoginInitialContextFactory");
			remoteServer.init();
			localServer.addJ2eeRemoteServer(remoteServer);

			j2eeLocalServers.add(localServer);


			// default config for local jboss 4.0x
			localServer = new J2eeLocalServer();
			localServer.cfMod = this;
			localServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS40X);
			localServer.setJ2eeVendorAdapterClassName(VendorAdapterJBoss.class.getName());
			localServer.init();

			remoteServer = new J2eeRemoteServer();
			remoteServer.cfMod = this;
			remoteServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS32X);
			remoteServer.setInitialContextFactory("org.nightlabs.authentication.jboss.LoginInitialContextFactory");
			remoteServer.init();
			localServer.addJ2eeRemoteServer(remoteServer);

			remoteServer = new J2eeRemoteServer();
			remoteServer.cfMod = this;
			remoteServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS40X);
			remoteServer.setInitialContextFactory("org.nightlabs.authentication.jboss.LoginInitialContextFactory");
			remoteServer.init();
			localServer.addJ2eeRemoteServer(remoteServer);

			j2eeLocalServers.add(localServer);


			setChanged();
		}
		else {

			for (Iterator it = j2eeLocalServers.iterator(); it.hasNext(); ) {
				J2eeLocalServer server= (J2eeLocalServer) it.next();
				server.cfMod = this;
				server.init();
			}
		}

	}
	
	/**
	 * @return Returns the j2eeLocalServers.
	 */
	public List getJ2eeLocalServers()
	{
		return j2eeLocalServers;
	}
	/**
	 * @param localServers The j2eeLocalServers to set.
	 */
	public void setJ2eeLocalServers(List localServers)
	{
		j2eeLocalServers = localServers;
	}
}
