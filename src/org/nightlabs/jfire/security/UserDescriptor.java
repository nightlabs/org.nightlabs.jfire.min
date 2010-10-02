package org.nightlabs.jfire.security;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.util.Util;

/**
	 * An instance of this class describes the current user including his session-identifier.
	 *
	 * @author marco schulze - marco at nightlabs dot de
	 */
	public class UserDescriptor
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