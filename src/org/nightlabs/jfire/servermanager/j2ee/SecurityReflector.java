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

package org.nightlabs.jfire.servermanager.j2ee;

import java.io.Serializable;

import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.User;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class SecurityReflector
implements Serializable
{
	public static final String JNDI_NAME = "java:/jfire/system/SecurityReflector";

	public static SecurityReflector lookupSecurityReflector(InitialContext initialContext)
	{
		try {
			return (SecurityReflector) initialContext.lookup(JNDI_NAME);
		} catch (NamingException x) {
			throw new RuntimeException(x);
		}
	}

	public static class UserDescriptor
	implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String organisationID;
		private String userID;
		private String sessionID;

		public UserDescriptor(String organisationID, String userID, String sessionID)
		{
			this.organisationID = organisationID;
			this.userID = userID;
			this.sessionID = sessionID;
		}

		/**
		 * @return Returns the organisationID.
		 */
		public String getOrganisationID()
		{
			return organisationID;
		}
		/**
		 * @return Returns the userID.
		 */
		public String getUserID()
		{
			return userID;
		}
		public String getSessionID()
		{
			return sessionID;
		}

		/**
		 * This is a convenience method which calls <tt>User.getUser(...)</tt>
		 *
		 * @param pm The PersistenceManager to use for accessing the datastore.
		 * @return Returns an instance of <tt>User</tt>
		 * @throws javax.jdo.JDOObjectNotFoundException in case the <tt>User</tt> does not exist in the datastore.
		 */
		public User getUser(PersistenceManager pm)
		{
			return User.getUser(pm, organisationID, userID);
		}

		@Override
		public String toString()
		{
			return this.getClass().getName() + '[' + organisationID + ',' + userID + ',' + sessionID + ']';
		}
	}

	/**
	 * @return the UserDescriptor of the user who is authenticated in the current thread and executing this method.
	 */
	public abstract UserDescriptor whoAmI();

	
	public static UserDescriptor getUserDescriptor()
	{
		try {
			InitialContext initCtx = null;
			try {
				initCtx = new InitialContext();
				return lookupSecurityReflector(initCtx).whoAmI();
			} finally {
				if (initCtx != null)
					initCtx.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException(x);
		}
	}
	
	public static Lookup getLookup() {
		return new Lookup(getUserDescriptor().getOrganisationID());
	}
	
}
