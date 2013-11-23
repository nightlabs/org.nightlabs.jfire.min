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
import java.util.Iterator;

import javax.jdo.PersistenceManager;

import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.server.id.LocalServerID;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author marco
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.server.id.LocalServerID"
 *		detachable="true"
 *		table="JFireBase_LocalServer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 */
@PersistenceCapable(
	objectIdClass=LocalServerID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_LocalServer")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class LocalServer implements Serializable
{
	private static final long serialVersionUID = 2L;

	public static LocalServer getLocalServer(PersistenceManager pm)
	{
		Iterator<LocalServer> it = pm.getExtent(LocalServer.class).iterator();
		if (!it.hasNext())
			throw new IllegalStateException("LocalServer undefined in datastore!");
		return it.next();
	}

	public LocalServer() { }

	public LocalServer(Server _server) {
		this.serverID = _server.getServerID();
		this.server = _server;
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String serverID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Server server;

	public String getServerID() {
		return serverID;
	}

	/**
	 * @return Returns the server.
	 */
	public Server getServer() {
		return server;
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
		final LocalServer other = (LocalServer) obj;
		return Util.equals(this.serverID, other.serverID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + serverID + ']';
	}
}
