/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.ipanema.servermanager.j2ee;

import java.io.Serializable;

import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.ipanema.security.User;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class SecurityReflector
implements Serializable
{
	public static final String JNDI_NAME = "java:/ipanema/system/SecurityReflector";

	public static SecurityReflector lookupSecurityReflector(InitialContext initialContext)
	throws NamingException
	{
		return (SecurityReflector) initialContext.lookup(JNDI_NAME);
	}

	public static class UserDescriptor
	implements Serializable
	{
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
	}

	/**
	 * @return the UserDescriptor of the user who is authenticated in the current thread and executing this method.
	 */
	public abstract UserDescriptor whoAmI();

}
