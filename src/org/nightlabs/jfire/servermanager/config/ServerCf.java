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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.server.id.ServerID;
import org.nightlabs.util.Util;

/**
 * @author marco
 */
public class ServerCf implements Serializable, Cloneable {
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 2L;

	public ServerCf() {
	}

	public ServerCf(String _serverID) {
		if (!Server.isValidServerID(_serverID))
			throw new IllegalArgumentException("serverID \"" + _serverID + "\" is not valid!");
		this.serverID = _serverID;
	}

	private String serverID = null;
	private String serverName = null;
	private String j2eeServerType = null;

	/**
	 * @deprecated Has been replaced by {@link #protocol2initialContextURL}.
	 */
	@Deprecated
	private String initialContextURL = null;

	private Map<String, String> protocol2initialContextURL = null;
	private String dataCentreID = null;

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
			throw new IllegalArgumentException("serverID \"" + _serverID + "\" is not a valid id!");
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
	public void setServerName(String _serverName) {
		if (_serverName == null)
			throw new NullPointerException("serverName must not be null!");
		if ("".equals(_serverName))
			throw new IllegalArgumentException("serverName must not be an empty string!");
		this.serverName = _serverName;
	}

	/**
	 * @return Returns the initialContextURL.
	 * @deprecated Replaced by {@link #getProtocol2initialContextURL()} with {@link Server#PROTOCOL_JNP}.
	 */
	@Deprecated
	public String getInitialContextURL() {
		return initialContextURL;
	}

	/**
	 * @param initialContextURL The initialContextURL to set.
	 * @deprecated @deprecated Replaced by {@link #getProtocol2initialContextURL()} with {@link Server#PROTOCOL_JNP}.
	 */
	@Deprecated
	public void setInitialContextURL(String initialContextURL) {
		this.initialContextURL = initialContextURL;
	}

	/**
	 * @return Returns the j2eeServerType.
	 */
	public String getJ2eeServerType() {
		return j2eeServerType;
	}

	/**
	 * @param serverType The j2eeServerType to set.
	 */
	public void setJ2eeServerType(String serverType) {
		j2eeServerType = serverType;
	}

	public String getDataCentreID() {
		return dataCentreID;
	}
	public void setDataCentreID(String dataCentreID) {
		this.dataCentreID = dataCentreID;
	}

	public String getDistinctiveDataCentreID() {
		if (dataCentreID != null)
			return dataCentreID;

		return serverID;
	}

	public Map<String, String> getProtocol2initialContextURL() {
		if (protocol2initialContextURL == null) {
			protocol2initialContextURL = new HashMap<String, String>();
		}
		return protocol2initialContextURL;
	}

	public void setProtocol2initialContextURL(Map<String, String> connectionType2initialContextURL)
	{
		this.protocol2initialContextURL = connectionType2initialContextURL;
	}

	public String getInitialContextURL(String protocol, boolean throwExceptionIfNotExisting)
	{
		if (protocol == null)
			throw new IllegalArgumentException("protocol must not be null!");

		String result = getProtocol2initialContextURL().get(protocol);

		if (result == null && throwExceptionIfNotExisting)
			throw new IllegalArgumentException("There is no initialContextURL for the protocol \"" + protocol + "\"!!!");

		return result;
	}

	public void setInitialContextURL(String protocol, String initialContextURL)
	{
		if (protocol == null)
			throw new IllegalArgumentException("protocol must not be null!");

		if (initialContextURL == null || initialContextURL.isEmpty())
			getProtocol2initialContextURL().remove(protocol);
		else
			getProtocol2initialContextURL().put(protocol, initialContextURL);
	}

	/**
	 * This method creates a JDO Server object with the given persistenceManager
	 * in case it does not yet exist.
	 *
	 * @param pm The PersistenceManager in which's datastore the Server should be
	 *          created.
	 */
	public Server createServer(PersistenceManager pm) {
		// Initialize meta data.
		pm.getExtent(Server.class, true);

		// Fetch/create Server instance.
		Server server;
		try {
			server = (Server) pm.getObjectById(ServerID.create(getServerID()), true);
		} catch (JDOObjectNotFoundException x) {
			server = new Server(getServerID());
			copyTo(server);
			server = pm.makePersistent(server);
		}
		return server;
	}

	/**
	 * Copy the values of this {@link ServerCf} instance into an instance of {@link Server}.
	 *
	 * @param server the destination into which data shall be written.
	 * @return <code>false</code>, if no change was required (i.e. the destination already contained the same data before).
	 * <code>true</code>, if at least one value was modified in the destination object.
	 * @throws IllegalArgumentException if the {@link ServerCf#getServerID() serverID} of this {@link ServerCf} does not match the {@link Server#getServerID() serverID} of the given {@link Server} instance.
	 */
	public boolean copyTo(Server server)
	throws IllegalArgumentException
	{
		boolean modified = false;

		if (!Util.equals(getServerID(), server.getServerID()))
			throw new IllegalArgumentException("ServerID does not match! this.serverID != server.serverID :: " + this.getServerID() + " != " + server.getServerID());

		if (!Util.equals(getServerName(), server.getServerName())) {
			modified = true;
			server.setServerName(getServerName());
		}

		if (!Util.equals(getJ2eeServerType(), server.getJ2eeServerType())) {
			modified = true;
			server.setJ2eeServerType(getJ2eeServerType());
		}

		if (!Util.equals(getDataCentreID(), server.getDataCentreID())) {
			modified = true;
			server.setDataCentreID(getDataCentreID());
		}

		// copy initial context URLs.
		for (Map.Entry<String, String> me : protocol2initialContextURL.entrySet()) {
			String protocol = me.getKey();
			if (protocol == null || protocol.isEmpty())
				continue;

			String initialContextURL = me.getValue();
			if (initialContextURL == null || initialContextURL.isEmpty())
				continue;

			String url = server.getInitialContextURL(protocol, false);
			if (!Util.equals(initialContextURL, url)) {
				modified = true;
				server.setInitialContextURL(protocol, initialContextURL);
			}
		}

		// and remove entries that shouldn't be there
		Map<String, String> serverICUMap = server.getProtocol2initialContextURL();
		if (serverICUMap.size() != protocol2initialContextURL.size()) {
			for (String protocol : new HashSet<String>(serverICUMap.keySet())) {
				String initialContextURL = protocol2initialContextURL.get(protocol);
				if (initialContextURL == null || initialContextURL.isEmpty()) {
					modified = true;
					server.setInitialContextURL(protocol, null);
				}
			}
		}

		return modified;
	}

	public boolean init() {
		boolean modified = false;

		if (serverID == null) {
			modified = true;
			serverID = Long.toString(System.currentTimeMillis(), 36) + ".server.jfire.org";
		}

		if (serverName == null) {
			modified = true;
			serverName = "JFire demo server (" + serverID + ")";
		}

		if (j2eeServerType == null) {
			modified = true;
			j2eeServerType = Server.J2EESERVERTYPE_JBOSS40X;
		}

		if (protocol2initialContextURL == null) {
			modified = true;
			protocol2initialContextURL = new HashMap<String, String>();
		}

		if (!protocol2initialContextURL.containsKey(Server.PROTOCOL_JNP)) {
			modified = true;
			protocol2initialContextURL.put(Server.PROTOCOL_JNP, "jnp://localhost:1099");

			// BEGIN downward compatibility
			if (initialContextURL != null && !initialContextURL.isEmpty())
				protocol2initialContextURL.put(Server.PROTOCOL_JNP, initialContextURL);
			// END downward compatibility
		}

		initialContextURL = null;

		if (!protocol2initialContextURL.containsKey(Server.PROTOCOL_HTTPS)) {
			modified = true;
			protocol2initialContextURL.put(Server.PROTOCOL_HTTPS, "https://localhost:8443/invoker/SSLJNDIFactory");
		}

		for (Iterator<Map.Entry<String, String>> it = protocol2initialContextURL.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, String> me = it.next();
			if (me.getKey() == null || me.getKey().isEmpty()) {
				modified = true;
				it.remove();
			}
		}

		return modified;
	}

	@Override
	public Object clone()
	{
		try {
			ServerCf c = (ServerCf) super.clone();
			c.protocol2initialContextURL = new HashMap<String, String>(c.protocol2initialContextURL);
			return c;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e); // should never happen since we implement clone()
		}
	}

	public void setInitialContextURL_HTTPS(String initialContextURL) {
		setInitialContextURL(Server.PROTOCOL_HTTPS, initialContextURL);
	}

	public String getInitialContextURL_HTTPS() {
		return getInitialContextURL(Server.PROTOCOL_HTTPS, false);
	}

	public void setInitialContextURL_JNP(String initialContextURL) {
		setInitialContextURL(Server.PROTOCOL_JNP, initialContextURL);
	}

	public String getInitialContextURL_JNP() {
		return getInitialContextURL(Server.PROTOCOL_JNP, false);
	}

}
