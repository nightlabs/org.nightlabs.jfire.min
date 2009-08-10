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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;
import org.nightlabs.config.Initializable;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class J2eeServerTypeRegistryConfigModule extends ConfigModule
{
	private static final long serialVersionUID = 1L;

	public static class J2eeLocalServer
	implements Initializable, Serializable
	{
		private static final long serialVersionUID = 1L;

		private J2eeServerTypeRegistryConfigModule cfMod;

		private String j2eeServerType;
		private String j2eeVendorAdapterClassName;
		private List<J2eeRemoteServer> j2eeRemoteServers;

		private Map<String, J2eeRemoteServer> j2eeRemoteServersByServerType = null;

		@Override
		public void init() throws InitException
		{
			if (j2eeRemoteServers == null) {
				j2eeRemoteServers = new ArrayList<J2eeRemoteServer>();
				if (cfMod != null) cfMod.setChanged();
			}
			else {

				for (J2eeRemoteServer server : j2eeRemoteServers) {
					if (server.cfMod != cfMod && cfMod != null) {
						server.cfMod = cfMod;
						cfMod.setChanged();
					}
					server.init();
				}

			}
		}

		/**
		 * @return Returns the j2eeRemoteServers.
		 */
		public List<J2eeRemoteServer> getJ2eeRemoteServers()
		{
			return j2eeRemoteServers;
		}
		/**
		 * @param remoteServers The j2eeRemoteServers to set.
		 */
		public void setJ2eeRemoteServers(List<J2eeRemoteServer> remoteServers)
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
				Map<String, J2eeRemoteServer> m = new HashMap<String, J2eeRemoteServer>();

				for (Iterator<J2eeRemoteServer> it = j2eeRemoteServers.iterator(); it.hasNext(); ) {
					J2eeRemoteServer s = it.next();
					m.put(s.getJ2eeServerType(), s);
				}

				j2eeRemoteServersByServerType = m;
			}

			return j2eeRemoteServersByServerType.get(j2eeServerType);
		}
	}

	public static class J2eeRemoteServer
	implements Initializable, Serializable
	{
		private static final long serialVersionUID = 2L;

		private J2eeServerTypeRegistryConfigModule cfMod;

		private String j2eeServerType;
		/**
		 * This initialContextFactory is used for normal (authenticated) communication.
		 * @deprecated Replaced by {@link #protocol2loginInitialContextFactory}.
		 */
		@Deprecated
		private String initialContextFactory;
		/**
		 * This initialContextFactory is used for anonymous (not-authenticated) communication
		 * during organisation-handshake.
		 *
		 * @deprecated Replaced by {@link #protocol2anonymousInitialContextFactory}.
		 */
		@Deprecated
		private String anonymousInitialContextFactory;

		private Map<String, String> protocol2anonymousInitialContextFactory;

		private Map<String, String> protocol2loginInitialContextFactory;

		public void init() throws InitException
		{
//			if (anonymousInitialContextFactory == null)
//				setAnonymousInitialContextFactory("org.jnp.interfaces.NamingContextFactory");

			if (protocol2anonymousInitialContextFactory == null) {
				protocol2anonymousInitialContextFactory = new HashMap<String, String>();
				setChanged();
			}

			if (protocol2loginInitialContextFactory == null) {
				protocol2loginInitialContextFactory = new HashMap<String, String>();
				setChanged();
			}

			if (getAnonymousInitialContextFactory(Server.PROTOCOL_JNP, false) == null) {
				setAnonymousInitialContextFactory(Server.PROTOCOL_JNP, "org.jnp.interfaces.NamingContextFactory");

				// TODO BEGIN downward compatibility
				if (anonymousInitialContextFactory != null)
					setAnonymousInitialContextFactory(Server.PROTOCOL_JNP, anonymousInitialContextFactory);
				// END downward compatibility
			}

			if (getAnonymousInitialContextFactory(Server.PROTOCOL_HTTPS, false) == null)
				setAnonymousInitialContextFactory(Server.PROTOCOL_HTTPS, "org.nightlabs.unifiedjndi.jboss.client.UnifiedNamingContextFactory");

			if (getAnonymousInitialContextFactory(Server.PROTOCOL_HTTP, false) == null)
				setAnonymousInitialContextFactory(Server.PROTOCOL_HTTP, "org.nightlabs.unifiedjndi.jboss.client.UnifiedNamingContextFactory");

			if (getLoginInitialContextFactory(Server.PROTOCOL_JNP, false) == null) {
//				setLoginInitialContextFactory(Server.PROTOCOL_JNP, "org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory");
				setLoginInitialContextFactory(Server.PROTOCOL_JNP, "org.nightlabs.unifiedjndi.jboss.client.UnifiedNamingContextFactory");

				// TODO BEGIN downward compatibility
				if (initialContextFactory != null)
					setLoginInitialContextFactory(Server.PROTOCOL_JNP, initialContextFactory);
				// END downward compatibility
			}

			if (getLoginInitialContextFactory(Server.PROTOCOL_HTTPS, false) == null)
				setLoginInitialContextFactory(Server.PROTOCOL_HTTPS, "org.nightlabs.unifiedjndi.jboss.client.UnifiedNamingContextFactory");

			if (getLoginInitialContextFactory(Server.PROTOCOL_HTTP, false) == null)
				setLoginInitialContextFactory(Server.PROTOCOL_HTTP, "org.nightlabs.unifiedjndi.jboss.client.UnifiedNamingContextFactory");

			anonymousInitialContextFactory = null;
			initialContextFactory = null;
		}

		public Map<String, String> getProtocol2anonymousInitialContextFactory() {
			return protocol2anonymousInitialContextFactory;
		}
		public void setProtocol2anonymousInitialContextFactory(Map<String, String> protocol2anonymousInitialContextFactory) {
			this.protocol2anonymousInitialContextFactory = protocol2anonymousInitialContextFactory;
		}

		public Map<String, String> getProtocol2loginInitialContextFactory() {
			return protocol2loginInitialContextFactory;
		}
		public void setProtocol2loginInitialContextFactory(Map<String, String> protocol2loginInitialContextFactory) {
			this.protocol2loginInitialContextFactory = protocol2loginInitialContextFactory;
		}

		public String getAnonymousInitialContextFactory(String protocol, boolean throwExceptionIfNotFound)
		{
			if (protocol == null)
				throw new IllegalArgumentException("protocol must not be null!");

			if (protocol.isEmpty())
				throw new IllegalArgumentException("protocol must not be an empty string!");

			String result = protocol2anonymousInitialContextFactory.get(protocol);
			if (result == null && throwExceptionIfNotFound)
				throw new IllegalArgumentException("There is no anonymousInitialContextFactory registered for protocol \"" + protocol + "\"!!!");

			return result;
		}

		public void setAnonymousInitialContextFactory(String protocol, String initialContextFactory)
		{
			if (protocol == null)
				throw new IllegalArgumentException("protocol must not be null!");

			if (protocol.isEmpty())
				throw new IllegalArgumentException("protocol must not be an empty string!");

			if (initialContextFactory == null || initialContextFactory.isEmpty()) {
				if (protocol2anonymousInitialContextFactory.remove(protocol) != null)
					setChanged();
			}
			else {
				if (!Util.equals(initialContextFactory, protocol2anonymousInitialContextFactory.get(protocol))) {
					protocol2anonymousInitialContextFactory.put(protocol, initialContextFactory);
					setChanged();
				}
			}
		}

		public String getLoginInitialContextFactory(String protocol, boolean throwExceptionIfNotFound)
		{
			if (protocol == null)
				throw new IllegalArgumentException("protocol must not be null!");

			if (protocol.isEmpty())
				throw new IllegalArgumentException("protocol must not be an empty string!");

			String result = protocol2loginInitialContextFactory.get(protocol);
			if (result == null && throwExceptionIfNotFound)
				throw new IllegalArgumentException("There is no loginInitialContextFactory registered for protocol \"" + protocol + "\"!!!");

			return result;
		}

		public void setLoginInitialContextFactory(String protocol, String initialContextFactory)
		{
			if (protocol == null)
				throw new IllegalArgumentException("protocol must not be null!");

			if (protocol.isEmpty())
				throw new IllegalArgumentException("protocol must not be an empty string!");

			if (initialContextFactory == null || initialContextFactory.isEmpty()) {
				if (protocol2loginInitialContextFactory.remove(protocol) != null)
					setChanged();
			}
			else {
				if (!Util.equals(initialContextFactory, protocol2loginInitialContextFactory.get(protocol))) {
					protocol2loginInitialContextFactory.put(protocol, initialContextFactory);
					setChanged();
				}
			}
		}


		/**
		 * @return the fully qualified class name of the initialContextFactory that is used for normal
		 *		(authenticated) communication between organisations.
		 *
		 * @deprecated Replaced by {@link #getLoginInitialContextFactory(String, boolean)}
		 */
		@Deprecated
		public String getInitialContextFactory()
		{
			return initialContextFactory;
		}
		/**
		 * @param initialContextFactory The initialContextFactory to set.
		 * @deprecated
		 */
		@Deprecated
		public void setInitialContextFactory(String initialContextFactory)
		{
			this.initialContextFactory = initialContextFactory;
			setChanged();
		}
		/**
		 * @return the fully qualified class name of the context factory that is used for anonymous communication
		 *		during organisation-handshake.
		 *
		 * @deprecated Replaced by {@link #getAnonymousInitialContextFactory(String, boolean)}.
		 */
		@Deprecated
		public String getAnonymousInitialContextFactory()
		{
			return anonymousInitialContextFactory;
		}
		/**
		 * @deprecated
		 */
		@Deprecated
		public void setAnonymousInitialContextFactory(String anonymousInitialContextFactory)
		{
			this.anonymousInitialContextFactory = anonymousInitialContextFactory;
			setChanged();
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
			setChanged();
		}

		private void setChanged() {
			if (cfMod != null)
				cfMod.setChanged();
		}
	}

	private List<J2eeLocalServer> j2eeLocalServers;

	@Override
	public void init() throws InitException
	{
		if (j2eeLocalServers == null) {
			j2eeLocalServers = new ArrayList<J2eeLocalServer>();

			J2eeLocalServer localServer;
			J2eeRemoteServer remoteServer;

//			// default config for local jboss 3.2x
//			localServer = new J2eeLocalServer();
//			localServer.cfMod = this;
//			localServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS32X);
//			localServer.setJ2eeVendorAdapterClassName("org.nightlabs.jfire.jboss.j2ee.J2EEAdapterJBoss");
//			localServer.init();
//
//			remoteServer = new J2eeRemoteServer();
//			remoteServer.cfMod = this;
//			remoteServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS32X);
////			remoteServer.setInitialContextFactory("org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory");
////			remoteServer.setAnonymousInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
//			remoteServer.init();
//			localServer.addJ2eeRemoteServer(remoteServer);
//
//			remoteServer = new J2eeRemoteServer();
//			remoteServer.cfMod = this;
//			remoteServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS40X);
////			remoteServer.setInitialContextFactory("org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory");
////			remoteServer.setAnonymousInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
//			remoteServer.init();
//			localServer.addJ2eeRemoteServer(remoteServer);
//
//			j2eeLocalServers.add(localServer);


			// default config for local jboss 4.0x
			localServer = new J2eeLocalServer();
			localServer.cfMod = this;
			localServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS40X);
			localServer.setJ2eeVendorAdapterClassName("org.nightlabs.jfire.jboss.j2ee.J2EEAdapterJBoss");
			localServer.init();

//			remoteServer = new J2eeRemoteServer();
//			remoteServer.cfMod = this;
//			remoteServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS32X);
////			remoteServer.setInitialContextFactory("org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory");
////			remoteServer.setAnonymousInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
//			remoteServer.init();
//			localServer.addJ2eeRemoteServer(remoteServer);

			remoteServer = new J2eeRemoteServer();
			remoteServer.cfMod = this;
			remoteServer.setJ2eeServerType(Server.J2EESERVERTYPE_JBOSS40X);
//			remoteServer.setInitialContextFactory("org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory");
//			remoteServer.setAnonymousInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
			remoteServer.init();
			localServer.addJ2eeRemoteServer(remoteServer);

			j2eeLocalServers.add(localServer);

			setChanged();
		}
		else {
			for (J2eeLocalServer server : j2eeLocalServers) {
				if (server.cfMod != this) {
					server.cfMod = this;
					setChanged();
				}
				server.init();
			}
		}
	}

	/**
	 * @return Returns the j2eeLocalServers.
	 */
	public List<J2eeLocalServer> getJ2eeLocalServers()
	{
		return j2eeLocalServers;
	}
	/**
	 * @param localServers The j2eeLocalServers to set.
	 */
	public void setJ2eeLocalServers(List<J2eeLocalServer> localServers)
	{
		j2eeLocalServers = localServers;
	}
}
