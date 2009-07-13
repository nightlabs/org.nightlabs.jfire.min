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

package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.Properties;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.util.Util;

/**
 * An implementation of <code>SecurityReflector</code> provides API to find out who the current user is
 * (see {@link #getUserDescriptor()}). The purpose of this is (1) to be able to find out at every code location
 * who the current user is and (2) to have the same API for this no matter where the
 * code is executed - i.e. the JFire server, the JFire RCP client or another client.
 * <p>
 * Additionally, the <code>SecurityReflector</code> API can be used to obtain the initial context properties needed to obtain
 * an EJB (see {@link #getInitialContextProperties()}) or to query the presence of access rights
 * (see {@link #authorityContainsRoleRef(AuthorityID, RoleID)}).
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class SecurityReflector
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String JNDI_NAME = "java:/jfire/system/SecurityReflector";

	public static final String PROPERTY_KEY_SECURITY_REFLECTOR_CLASS = "org.nightlabs.jfire.security.SecurityReflector";

	/**
	 * This is used, if we're not using JNDI, but a System property (i.e. in the client)
	 */
	private static SecurityReflector sharedInstance = null;

	/**
	 * This method calls {@link #lookupSecurityReflector(InitialContext)} with initialContext == null.
	 */
	public static SecurityReflector sharedInstance()
	{
		return lookupSecurityReflector(null);
	}

	public static SecurityReflector lookupSecurityReflector(InitialContext initialContext)
	{
		createLocalVMSharedInstance:
		if (sharedInstance == null) {
			String className = System.getProperty(PROPERTY_KEY_SECURITY_REFLECTOR_CLASS);
			if (className == null)
				break createLocalVMSharedInstance;

			Class<?> clazz;
			try {
				//clazz = Class.forName(className);
				// use the class loader of this class instead the context class loader:
				clazz = Class.forName(className, true, SecurityReflector.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			try {
				sharedInstance = (SecurityReflector) clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		if (sharedInstance != null)
			return sharedInstance;

		boolean closeInitialContext = initialContext == null;
		try {
			if (closeInitialContext)
				initialContext = new InitialContext();

			try {
				return (SecurityReflector) initialContext.lookup(JNDI_NAME);
			} finally {
				if (closeInitialContext)
					initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException("The SecurityReflector has neither been specified by the system property \""+PROPERTY_KEY_SECURITY_REFLECTOR_CLASS+"\" nor is it bound to JNDI!", x);
		}
	}

	/**
	 * An instance of this class describes the current user including his session-identifier.
	 *
	 * @author marco schulze - marco at nightlabs dot de
	 */
	public static class UserDescriptor
	implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String organisationID;
		private String userID;
		private String workstationID;
		private String sessionID;
		private transient UserID userObjectID = null; // lazy creation

		public UserDescriptor(LoginData loginData)
		{
			this(loginData.getOrganisationID(), loginData.getUserID(), loginData.getWorkstationID(), loginData.getSessionID());
		}

		public UserDescriptor(String organisationID, String userID, String workstationID, String sessionID)
		{
			this.organisationID = organisationID;
			this.userID = userID;
			this.workstationID = workstationID;
			this.sessionID = sessionID;
		}

		/**
		 * @return the organisationID.
		 */
		public String getOrganisationID()
		{
			return organisationID;
		}
		/**
		 * @return the userID.
		 */
		public String getUserID()
		{
			return userID;
		}
		/**
		 * @return the workstationID or <code>null</code> if not set.
		 */
		public String getWorkstationID() {
			return workstationID;
		}
		/**
		 * @return The sessionID of the user.
		 */
		public String getSessionID()
		{
			return sessionID;
		}

		/**
		 * @return The user's {@link UserID}.
		 */
		public UserID getUserObjectID() {
			if (userObjectID == null)
				userObjectID = UserID.create(organisationID, userID);

			return userObjectID;
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
			return this.getClass().getName() + '[' + organisationID + ',' + userID + ',' + sessionID + ',' + workstationID + ']';
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
			result = prime * result + ((userID == null) ? 0 : userID.hashCode());
			result = prime * result + ((workstationID == null) ? 0 : workstationID.hashCode());
			result = prime * result + ((sessionID == null) ? 0 : sessionID.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final UserDescriptor other = (UserDescriptor) obj;
			return (
					Util.equals(sessionID, other.sessionID) &&
					Util.equals(workstationID, other.workstationID) &&
					Util.equals(userID, other.userID) &&
					Util.equals(organisationID, other.organisationID)
			);
		}

		/**
		 * This method concatenates <code>userID</code> and <code>organisationID</code>. Its result can be parsed by
		 * {@link #parseLogin(String)}, but the <code>sessionID</code> will be simply the login as there's
		 * no <code>sessionID</code> included in this result.
		 *
		 * @return the complete user name including the organisationID in the form user@organisation
		 * @see User#SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID
		 * @see #getLogin()
		 */
		public String getCompleteUserID()
		{
			return userID + User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + organisationID ;
		}

		public static UserDescriptor parseLogin(String encodedlogin)
		{
			return new UserDescriptor(new LoginData(encodedlogin, null));
//			String[] txt = User.PATTERN_SPLIT_LOGIN.split(encodedlogin);
//			if(txt.length != 2 && txt.length != 3)
//				throw new IllegalArgumentException("Invalid login - not two or three parts (use user@organisation?sessionID=xxx&moreParams=yyy&..., session is optional): " + encodedlogin);
//			if(txt[0].length() == 0 || txt[1].length() == 0)
//				throw new IllegalArgumentException("Invalid login - empty userID or empty organisationID (use user@organisation?sessionID=xxx&moreParams=yyy&..., session is optional): " + encodedlogin);
//			String userID = txt[0];
//			String organisationID = txt[1];
//
//			String sessionID;
//			if (txt.length < 3 || "".equals(txt[2]))
//				sessionID = userID + '!' + organisationID;
//			else {
//				sessionID = new ParameterMap(txt[2]).get(LoginData.SESSION_ID);
//			}
//
//			return new UserDescriptor(organisationID, userID, sessionID);
		}
	}

	public abstract UserDescriptor _getUserDescriptor() throws NoUserException;

	protected abstract Properties _getInitialContextProperties() throws NoUserException;

	protected abstract InitialContext _createInitialContext() throws NoUserException;

//	protected abstract AuthorityID _resolveSecuringAuthorityID(SecuredObject securedObject) throws NoUserException;

	/**
	 * Get a {@link Set} containing the object-ids of all {@link Role}s that are granted to the current
	 * user in specified authority.
	 *
	 * @param authorityID an authority's identifier - must not be <code>null</code>!.
	 */
	protected abstract Set<RoleID> _getRoleIDs(AuthorityID authorityID) throws NoUserException;

	/**
	 * Get a descriptor telling who's the current user calling this method.
	 *
	 * @return an instance of <code>UserDescriptor</code> describing who the current user is.
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	public static UserDescriptor getUserDescriptor() throws NoUserException
	{
		return sharedInstance()._getUserDescriptor();
	}

	/**
	 * Get a {@link Properties} instance which can be used for creating an {@link InitialContext} in order
	 * to communicate with a JFire server as the current user. This might be <code>null</code>, if the current
	 * situation does not require any <code>Properties</code> to be passed when creating an EJB.
	 * <p>
	 * Since authentication is bound in JFire to an JNDI context, you can directly use the returned <code>Properties</code>
	 * for creation of an EJB (via its generated Util class) - authentication is done implicitly.
	 * </p>
	 *
	 * @return an instance of <code>Properties</code> ready to create an {@link InitialContext} or <code>null</code>, if
	 *		there is no <code>Properties</code> instance needed in the current situation (e.g. inside the server, this is usually the case).
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	public static Properties getInitialContextProperties() throws NoUserException
	{
		return sharedInstance()._getInitialContextProperties();
	}

	/**
	 * Get an instance of {@link InitialContext} with the properties that are created by {@link #getInitialContextProperties()}.
	 * This <code>InitialContext</code> can be used directly to communicate with the server as the current user. Authentication
	 * is done implicitly.
	 * <p>
	 * This method never returns <code>null</code>.
	 * </p>
	 *
	 * @return an instance of <code>InitialContext</code> ready to communicate with the server as the current user.
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	public static InitialContext createInitialContext() throws NoUserException
	{
		return sharedInstance()._createInitialContext();
	}

	/**
	 * Find out whether the current user has a certain access right (i.e. a role) in a certain authority.
	 *
	 * @param authorityID the identifier of the {@link Authority} within which to check for the presence of a role-reference or <code>null</code> to point to the organisation-authority (see {@link Authority#AUTHORITY_ID_ORGANISATION}).
	 * @param roleID the identifier of the {@link Role} for which a reference should be searched.
	 * @return <code>true</code> if the role is present for the current user within the specified authority; <code>false</code> otherwise.
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	public static boolean authorityContainsRoleRef(AuthorityID authorityID, RoleID roleID) throws NoUserException
	{
		if (roleID == null)
			throw new IllegalArgumentException("roleID must not be null!");

		SecurityReflector securityReflector = sharedInstance();
		UserDescriptor userDescriptor = securityReflector._getUserDescriptor();

		if (authorityID == null)
			authorityID = AuthorityID.create(userDescriptor.getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION);
		else {
			if (!authorityID.organisationID.equals(userDescriptor.getOrganisationID()))
				throw new IllegalArgumentException("authorityID.organisationID != user.organisationID");
		}

		return securityReflector._getRoleIDs(authorityID).contains(roleID);
	}

	/**
	 * Get a {@link Set} containing the object-ids of all {@link Role}s that are granted to the current
	 * user in specified authority.
	 *
	 * @param authorityID the identifier of the {@link Authority} within which to check for the presence of a role-reference or <code>null</code> to point to the organisation-authority (see {@link Authority#AUTHORITY_ID_ORGANISATION}).
	 * @return all permissions granted to the current user in the specified authority.
	 * @throws NoUserException if there is no user currently authenticated.
	 */
	public static Set<RoleID> getRoleIDs(AuthorityID authorityID) throws NoUserException
	{
		SecurityReflector securityReflector = sharedInstance();
		UserDescriptor userDescriptor = securityReflector._getUserDescriptor();

		if (authorityID == null)
			authorityID = AuthorityID.create(userDescriptor.getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION);
		else {
			if (!authorityID.organisationID.equals(userDescriptor.getOrganisationID()))
				throw new IllegalArgumentException("authorityID.organisationID != user.organisationID");
		}

		return securityReflector._getRoleIDs(authorityID);
	}

//	public static boolean resolvedSecuringAuthorityContainsRoleRef(SecuredObject securedObject, RoleID roleID) throws NoUserException
//	{
//		AuthorityID securingAuthorityID = sharedInstance()._resolveSecuringAuthorityID(securedObject);
//		return authorityContainsRoleRef(securingAuthorityID, roleID);
////		SecurityReflector securityReflector = sharedInstance();
////		UserDescriptor userDescriptor = securityReflector._getUserDescriptor();
////
////		// TODO begin temporary code (ensuring this always fails for this type of securedObject - this block should be removed soon
////		if (securedObject instanceof Authority)
////			throw new UnsupportedOperationException("NYI"); // see the code in Authority.resolveSecuringAuthority(...) - this needs to be re-modelled here - but note that it is not allowed to everyone to retrieve every authority! requires further thoughts!
////		// end temporary code
////
////		AuthorityID securingAuthorityID = securedObject.getSecuringAuthorityID();
////		if (securingAuthorityID == null) {
////			if (securedObject instanceof Authority)
////				throw new UnsupportedOperationException("NYI"); // see the code in Authority.resolveSecuringAuthority(...) - this needs to be re-modelled here - but note that it is not allowed to everyone to retrieve every authority! requires further thoughts!
////
////			securingAuthorityID = AuthorityID.create(userDescriptor.getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION);
////		}
//	}
}
