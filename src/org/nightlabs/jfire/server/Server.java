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

import java.io.Serializable;
import java.util.regex.Pattern;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.NullValue;
import org.nightlabs.jfire.server.id.ServerID;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author marco
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.server.id.ServerID"
 *		detachable="true"
 *		table="JFireBase_Server"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 */
@PersistenceCapable(
	objectIdClass=ServerID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Server")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Server implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String J2EESERVERTYPE_JBOSS32X = "jboss_32x";
	public static final String J2EESERVERTYPE_JBOSS40X = "jboss_40x";

	/**
	 * This method checks whether a given unique identifier is valid to identify a server
	 * in the jfire-system. Since domain-style identifiers should be used, this method
	 * allows only the characters 'a'...'z', 'A'...'Z', '0'...'9', '.', '_' and '-'.
	 * <p>
	 * Note, that this is more restrictive than other IDs which are checked with
	 * {@link ObjectIDUtil#isValidIDString(String)}.
	 * </p>
	 * <p>
	 * It is wise to use a DNS host name as server-id. But you should keep in mind that
	 * neither a server-id, nor an organisation-id, nor any other ID can be changed later.
	 * Hence, even if you move your jfire-server to a new host, the <code>serverID</code> stays
	 * the same.
	 * </p>
	 * <p>
	 * It will - some day in the future - be possible to move an organisation from one jfire-server
	 * to another. Therefore, everything in the system is referenced via its organisationID - not via
	 * the serverID. The id of a server is only used for lookup (in order to communicate).
	 * </p>
	 *
	 * @param serverID The server-id to be checked for validity
	 * @return <code>true</code> if it contains solely valid characters, <code>false</code> if the given <code>serverID</code>
	 *		is <code>null</code> or contains illegal chars and must not be used.
	 * @see #assertValidServerID(String)
	 */
	public static boolean isValidServerID(String serverID)
	{
		if (serverID == null)
			return false;

		return validServerIDPattern.matcher(serverID).matches();
	}

	private static final Pattern validServerIDPattern = Pattern.compile("[a-zA-Z0-9\\._\\-]+\\.[a-zA-Z0-9\\._\\-]+");

	/**
	 * Validate a given server-id. This method calls {@link #isValidServerID(String)}
	 * and throws an {@link IllegalArgumentException}, if it returned <code>false</code>.
	 *
	 * @param serverID The server-id to be checked for validity
	 * @throws IllegalArgumentException if the given <code>serverID</code> is not valid.
	 */
	public static void assertValidServerID(String serverID)
	throws IllegalArgumentException
	{
		if (!isValidServerID(serverID))
			throw new IllegalArgumentException("serverID \""+serverID+"\" is not valid!");
	}


  public Server() { }

  public Server(String _serverID)
  {
  	assertValidServerID(_serverID);
  	this.serverID = _serverID;
  }

  /**
   * @jdo.field persistence-modifier="persistent" primary-key="true"
   * @jdo.column length="100"
   */
  @PrimaryKey
  @Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
  @Column(length=100)
  private String serverID;

	/**
	 * @return Returns the serverID.
	 */
	public String getServerID() {
		return serverID;
	}

	/**
	 * The name of the server to display.
	 * 
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String serverName;

	/**
	 * @return Returns the serverName.
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @param serverName The serverName to set.
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 * 
	 * @see #getJ2eeServerType()
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String j2eeServerType;
	
	/**
	 * @return Returns the j2eeServerType. The j2eeServerType specifies
	 * which implementation of j2ee the server is. This is necessary,
	 * because the various implementations differ in extensions that are
	 * not defined in the j2ee standard, but are necessary for JFire.
	 * Some of these extensions are coming from NightLabs and are more or
	 * less part of JFire.
	 * 
	 * @see #J2EESERVERTYPE_JBOSS32X
	 * @see #J2EESERVERTYPE_JBOSS40X
	 */
	public String getJ2eeServerType()
	{
		return j2eeServerType;
	}
	/**
	 * @param j2eeServerType The j2eeServerType to set.
	 * @see #getJ2eeServerType()
	 */
	public void setJ2eeServerType(String serverType)
	{
		this.j2eeServerType = serverType;
	}

	private String initialContextURL = "jnp://localhost:1099";

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serverID == null) ? 0 : serverID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Server other = (Server) obj;
		return Util.equals(this.serverID, other.serverID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + serverID + ']';
	}
}
