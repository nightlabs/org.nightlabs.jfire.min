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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.UserLocalID"
 *		detachable="true"
 *		table="JFireBase_UserLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, userID, localOrganisationID"
 *		add-interfaces="org.nightlabs.jfire.security.id.AuthorizedObjectID"
 *		include-body="id/UserLocalID.body.inc"
 *
 * @jdo.fetch-group name="User.userLocal" fields="user"
 * @jdo.fetch-group name="UserLocal.user" fields="user"
 *
 * @jdo.fetch-group name="UserLocal.authorizedObjectRefs" fields="authorizedObjectRefs"
 * @jdo.fetch-group name="UserLocal.userSecurityGroups" fields="userSecurityGroups[-1]"
 *
 * @jdo.fetch-group name="AuthorizedObject.name" fields="user"
 * @jdo.fetch-group name="AuthorizedObject.description" fields="user"
 */
@PersistenceCapable(
	objectIdClass=UserLocalID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UserLocal")
@FetchGroups({
	@FetchGroup(
		name="User.userLocal",
		members=@Persistent(name="user")),
	@FetchGroup(
		name=UserLocal.FETCH_GROUP_USER,
		members=@Persistent(name="user")),
	@FetchGroup(
		name=UserLocal.FETCH_GROUP_AUTHORIZED_OBJECT_REFS,
		members=@Persistent(name="authorizedObjectRefs")),
	@FetchGroup(
		name=UserLocal.FETCH_GROUP_USER_SECURITY_GROUPS,
		members=@Persistent(
			name="userSecurityGroups",
			recursionDepth=-1)),
	@FetchGroup(
		name="AuthorizedObject.name",
		members=@Persistent(name="user")),
	@FetchGroup(
		name="AuthorizedObject.description",
		members=@Persistent(name="user"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class UserLocal
extends AuthorizedObject
implements DetachCallback, AttachCallback
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(UserLocal.class);

	public static final String FETCH_GROUP_USER = "UserLocal.user";
	public static final String FETCH_GROUP_AUTHORIZED_OBJECT_REFS = "UserLocal.authorizedObjectRefs";
	public static final String FETCH_GROUP_USER_SECURITY_GROUPS = "UserLocal.userSecurityGroups";

	public static final int MIN_PASSWORD_LENGTH = 4;

	/**
	 * The alphabet used for machine password creation. Lazily created
	 * in {@link #createMachinePassword(int, int)}.
	 */
	private static CharSequence machinePasswordAlphabet = null;

	/**
	 * The alphabet used for human password creation. Lazily created
	 * in {@link #createHumanPassword(int, int)}.
	 */
	private static CharSequence humanPasswordAlphabet = null;

	public static Collection<? extends UserLocal> getLocalUserLocals(PersistenceManager pm)
	{
		Query q1 = pm.newQuery(UserLocal.class);
		q1.setFilter("this.organisationID == :localOrganisationID && this.localOrganisationID == :localOrganisationID");
		Collection<? extends UserLocal> userLocals = CollectionUtil.castCollection(
				(Collection<?>) q1.execute(LocalOrganisation.getLocalOrganisation(pm).getOrganisationID())
		);
		return userLocals;
	}

	/**
	 * This method generates a random password. The generated
	 * password is intended for machine usage. It may contain
	 * characters that may not be typeable on a common keyboard.
	 *
	 * @param minLen The minimum length (included).
	 * @param maxLen The maximum length (included).
	 * @return The newly created random password
	 * @deprecated Use {@link #createMachinePassword(int, int)} instead.
	 */
	@Deprecated
	public static String generatePassword(int minLen, int maxLen)
	{
		return createMachinePassword(minLen, maxLen);
	}

	/**
	 * This method generates a random password. The generated
	 * password is intended for machine usage. It may contain
	 * characters that may not be typeable on a common keyboard.
	 *
	 * @param minLen The minimum length (included).
	 * @param maxLen The maximum length (included).
	 * @return The newly created random password
	 * @deprecated Use {@link #createMachinePassword(int, int)} instead.
	 */
	@Deprecated
	public static String createPassword(int minLen, int maxLen)
	{
		return createMachinePassword(minLen, maxLen);
	}

	/**
	 * This method generates a random password. The generated
	 * password is intended for machine usage. It may contain
	 * characters that may not be typeable on a common keyboard.
	 *
	 * @param minLen The minimum length (included).
	 * @param maxLen The maximum length (included).
	 * @return The newly created random password
	 */
	public static String createMachinePassword(int minLen, int maxLen)
	{
		if(machinePasswordAlphabet == null) {
			StringBuffer sb = new StringBuffer();
			for(char c=40; c<=125; c++)
				sb.append(c);
			machinePasswordAlphabet = sb.toString();
		}
		return Util.createRandomString(machinePasswordAlphabet, minLen, maxLen);
	}

	/**
	 * This method generates a random password. The generated
	 * password is intended for human usage. It contains
	 * only characters that are typeable on a common keyboard
	 * and avoids usage of characters that might be confusing
	 * (like '1', 'i', 'I', 'l' or '0', 'o', 'O').
	 *
	 * @param minLen The minimum length (included).
	 * @param maxLen The maximum length (included).
	 * @return The newly created random password
	 */
	public static String createHumanPassword(int minLen, int maxLen)
	{
		if(humanPasswordAlphabet == null)
			humanPasswordAlphabet = "abcdefghkmnpqrstuvwABCDEFGHKLMNPQRSTUVW23456789";
		return Util.createRandomString(humanPasswordAlphabet, minLen, maxLen);
	}

	/**
	 * Encrypt a password using SHA algorithm and Base64 encoding.
	 * The password is converted to UTF-8 encoding before applying SHA on it.
	 * @param password The plain text password.
	 * @return The encrypted password
	 */
	public static String encryptPassword(String password)
	{
	  if(password == null)
	    return null;

		try {
			byte raw[] = Util.hash(password.getBytes(IOUtil.CHARSET_NAME_UTF_8), Util.HASH_ALGORITHM_SHA);
			return new String(org.apache.commons.codec.binary.Base64.encodeBase64(raw));
//			return (new BASE64Encoder()).encode(raw);
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e); // should never happen => RuntimeException
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e); // should never happen => RuntimeException
		}
	}

	/**
	 * Returns a boolean indicating whether the given password is a valid password.
	 * @param password The password to be checked.
	 * @return a boolean indicating whether the given password is a valid password.
	 */
	public static boolean isValidPassword(String password) {
		if (password == null)
			return false;
		if (password.length() < MIN_PASSWORD_LENGTH)
			return false;

		return true;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String userID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String localOrganisationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User user;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=255)
	private String password;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private String newPassword;

	/**
	 * key: String userID (UserGroup extends User, thus userGroupID = userID)<br/>
	 * value: UserGroup userGroup
	 * <br/><br/>
	 * UserGroup (m) - (n) User
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="UserSecurityGroup"
	 *		table="JFireBase_UserLocal_userSecurityGroups"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireBase_UserLocal_userSecurityGroups",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<UserSecurityGroup> userSecurityGroups;

	/**
	 * key: String authorityID<br/>
	 * value: AuthorizedObjectRef authorizedObjectRef
	 * <br/><br/>
	 * User (1) - (n) AuthorizedObjectRef
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="AuthorizedObjectRef"
	 *		mapped-by="userLocal"
	 *		@!mapped-by="authorizedObject" TODO DATANUCLEUS WORKAROUND: reactivate this and remove field AuthorizedObjectRef.userLocal!
	 *
	 * @jdo.key mapped-by="authorityID"
	 */
	@Persistent(
		mappedBy="userLocal",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="authorityID")
	private Map<String, AuthorizedObjectRef> authorizedObjectRefs;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected UserLocal() {
		if (logger.isDebugEnabled())
			logger.debug("default constructor: this=" +  this);
	}

	/**
	 * Create a new UserLocal object for the given user.
	 * @param user The underlying user.
	 */
	public UserLocal(User user)
	{
		if (logger.isDebugEnabled())
			logger.debug("constructor: this=" +  this + " user=" + user);

		this.user = user;
		this.organisationID = user.getOrganisationID();
		this.userID = user.getUserID();
		this.localOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		if (!localOrganisationID.equals(organisationID))
			throw new IllegalStateException("It is currently not supported to have a UserLocal in foreign organisations which are managed by these foreign organisations! organisationID=" + organisationID + " localOrganisationID=" + localOrganisationID);

		PersistenceManager pm = JDOHelper.getPersistenceManager(user);
		UserLocal ul;
		if (pm == null) {
			userSecurityGroups = new HashSet<UserSecurityGroup>(); // only necessary if we don't immediately persist, since during persisting, these are replaced by proxies anyway
			authorizedObjectRefs = new HashMap<String, AuthorizedObjectRef>();
			ul = this;
		}
		else
			ul = pm.makePersistent(this);

		user.setUserLocal(ul);
	}

	/**
	 * Get the organisationID.
	 * @return the organisationID
	 */
	@Override
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public String getUserID()
	{
		return userID;
	}

	/**
	 * Get the local organisationID. In contrast to {@link #getOrganisationID()}, this is
	 * the id of that organisation which manages this <code>UserLocal</code> instance.
	 * At the moment, this is always equal to {@link #getOrganisationID()}.
	 *
	 * @return the local organisationID.
	 */
	public String getLocalOrganisationID() {
		return localOrganisationID;
	}

	/**
	 * Get the password.
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * Returns the new password if it has been changed or <code>null</code> otherwise.
	 * @return the new password if it has been changed or <code>null</code> otherwise.
	 * @see #setNewPassword(String)
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * Sets the new password.
	 * <p>
	 * In order to change an existing password, this value should be set in the client to the NOT encrypted form of the new
	 * password. This will cause the method <code>JFireSecurityManager.storeUser(...)</code> to pass this value to
	 * {@link #setPasswordPlain(String)}, if {@link #getNewPassword()} does not return <code>null</code>. If <code>null</code>
	 * is returned, no change happens.
	 * </p>
	 *
	 * @param newPassword the new password.
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	/**
	 * Get the user.
	 * @return the user
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * This method checks, whether a plain password matches the password set
	 * for this user. Therefore, it encrypts the plain password and checks equality
	 * of the hashes.
	 * <p>
	 * This method is used by <code>JFireServerManagerImpl.login(...)</code>.
	 * </p>
	 * @param plainPassword The password in NOT encrypted form to check.
	 * 		Note that a <code>null</code> password always causes this
	 * 		method to return <code>false</code> - even if this
	 * 		{@link UserLocal} does not have a password assigned.
	 * 		The same applies to any password failing the
	 * 		{@link #isValidPassword(String)} test.
	 * @return <code>true</code> if the password matches, <code>false</code> otherwise.
	 */
	public boolean checkPassword(String plainPassword)
	{
		if (!isValidPassword(plainPassword))
			return false;

		return encryptPassword(plainPassword).equals(getPassword());
	}

	/**
	 * Set a new password.
	 * @param password The password in NOT encrypted form to set.
	 * @throws SecurityException
	 */
	public void setPasswordPlain(String password)
	{
		setPassword(encryptPassword(password));
	}

	/**
	 * Set a new password.
	 * @param password The password in ENCRYPTED form to set.
	 */
	public void setPassword(String password)
	{
	  this.password = password;
	}

	@Override
	public Collection<? extends AuthorizedObjectRef> getAuthorizedObjectRefs() {
		// TODO WORKAROUND DATANUCLEUS - need to force loading - otherwise objects are lost
		authorizedObjectRefs.entrySet().iterator();
		// WORKAROUND END

		return Collections.unmodifiableCollection(authorizedObjectRefs.values());
	}

	@Override
	protected void _addAuthorizedObjectRef(AuthorizedObjectRef userRef) {
		if (userRef == null)
			throw new IllegalArgumentException("authorizedObjectRef must not be null!");

		if (!this.equals(userRef.getAuthorizedObject()))
			throw new IllegalArgumentException("authorizedObjectRef.authorizedObject does not point back to me!");

		String authorityID = userRef.getAuthorityID();

		// TODO WORKAROUND DATANUCLEUS - need to force loading - otherwise objects are lost
		authorizedObjectRefs.entrySet().iterator();
		// WORKAROUND END

		if (logger.isDebugEnabled())
			logger.debug("_addAuthorizedObjectRef: this=" +  this + " authorityID=" + authorityID + " userRef=" + userRef + " this.authorizedObjectRefs=" + (authorizedObjectRefs == null ? null : authorizedObjectRefs.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(authorizedObjectRefs))));

		authorizedObjectRefs.put(authorityID, userRef);
	}

	@Override
	protected void _removeAuthorizedObjectRef(AuthorizedObjectRef authorizedObjectRef) {
		if (logger.isDebugEnabled())
			logger.debug("_removeAuthorizedObjectRef: this=" +  this + " authorizedObjectRef=" + authorizedObjectRef  + " this.authorizedObjectRefs=" + (authorizedObjectRefs == null ? null : authorizedObjectRefs.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(authorizedObjectRefs))));

		// TODO WORKAROUND DATANUCLEUS - need to force loading - otherwise changes are lost
		authorizedObjectRefs.entrySet().iterator();
		// WORKAROUND END

		authorizedObjectRefs.remove(authorizedObjectRef.getAuthorityID());
	}

	@Override
	public AuthorizedObjectRef getAuthorizedObjectRef(String authorityID) {
		// TODO WORKAROUND DATANUCLEUS - need to force loading - otherwise objects are lost
		authorizedObjectRefs.entrySet().iterator();
		// WORKAROUND END

		AuthorizedObjectRef result = authorizedObjectRefs.get(authorityID);

		if (logger.isDebugEnabled())
			logger.debug("getAuthorizedObjectRef: this=" +  this + " authorityID=" + authorityID + " result=" + result + " this.authorizedObjectRefs=" + (authorizedObjectRefs == null ? null : authorizedObjectRefs.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(authorizedObjectRefs))) + " this.authorizedObjectRefs.size=" + this.authorizedObjectRefs.size());

		return result;
	}

	@Override
	protected void _addUserSecurityGroup(UserSecurityGroup userSecurityGroup) {
		if (userSecurityGroup == null)
			throw new IllegalArgumentException("userGroup must not be null!");

		// TODO WORKAROUND DATANUCLEUS - need to force loading - otherwise changes are lost
		userSecurityGroups.iterator();
		// WORKAROUND END

		userSecurityGroups.add(userSecurityGroup);
	}

	/**
	 * @param userSecurityGroup Because UserGroup extends User, the userGroupID is the userID.
	 */
	@Override
	protected void _removeUserSecurityGroup(UserSecurityGroup userSecurityGroup) {
		// TODO WORKAROUND DATANUCLEUS - need to force loading - otherwise changes are lost
		userSecurityGroups.iterator();
		// WORKAROUND END

		userSecurityGroups.remove(userSecurityGroup);
	}

	@Override
	public Set<UserSecurityGroup> getUserSecurityGroups() {
		// TODO DATANUCLEUS WOKRAROUND - need to ensure data to be loaded - otherwise this.getUserSecurityGroups().contains(...) sometimes returns false, even though the object is present.
		userSecurityGroups.iterator();

		return Collections.unmodifiableSet(userSecurityGroups);
	}

	@Override
	public String getDescription() {
		return user.getDescription();
	}

	@Override
	public String getName() {
		return user.getName();
	}

	@Override
	public void setDescription(String description) {
		user.setDescription(description);
	}

	@Override
	public void setName(String name) {
		user.setName(name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final UserLocal other = (UserLocal) obj;
		return Util.equals(other.organisationID, this.organisationID) && Util.equals(other.userID, this.userID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + userID + ']';
	}

	/* (non-Javadoc)
	 * @see javax.jdo.listener.DetachCallback#jdoPostDetach(java.lang.Object)
	 */
	@Override
	public void jdoPostDetach(Object o)
	{
		UserLocal detached = this;
		UserLocal attached = (UserLocal) o;

		// We clear the password. It is never needed in the client.
		try {
			detached.password = null;
		} catch (JDODetachedFieldAccessException e) {
			// silently ignore
		}

		// Because for example User.jdoPostDetach(...) nulls out the field User.userLocal, it doesn't matter, what
		// we do here. Since this callback is likely called even though the data is never sent out to the client (as it's nulled),
		// we can not throw a blocking exception here. But we do log a warning, because even though the client should
		// succeed (we have deferred the exception - see code in User), we should at least see in the server's log that
		// a client detaches too much (and should specify more restrictive fetch-groups).

		// If somebody tries to detach the userLocal without setting User.isDisableDetachUserLocalAccessRightCheck(true),
		// we clear some more sensitive data, if this user is not having enough access rights.
		if (!User.isDisableDetachUserLocalAccessRightCheck()) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
			UserID principalUserID = SecurityReflector.getUserDescriptor().getUserObjectID();
			// check authorization via the organisation-authority
			if (!Authority.getOrganisationAuthority(pm).containsRoleRef(principalUserID, RoleConstants.accessRightManagement)) {
				logger.warn(
						"Somebody tried to detach userLocal "
						+ this
						+ " without explicitly setting User.isDisableDetachUserLocalAccessRightCheck(true) and without having the necessary access rights!!!",
						new Exception()
				);

				// Clear some more sensitive information.
				try {
					authorizedObjectRefs = null;
				} catch (JDODetachedFieldAccessException e) {
					// silently ignore
				}

				try {
					userSecurityGroups = null;
				} catch (JDODetachedFieldAccessException e) {
					// silently ignore
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.jdo.listener.DetachCallback#jdoPreDetach()
	 */
	@Override
	public void jdoPreDetach() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see javax.jdo.listener.AttachCallback#jdoPostAttach(java.lang.Object)
	 */
	@Override
	public void jdoPostAttach(Object o) {
		String myOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		String myOrganisationUserID = User.USER_ID_PREFIX_TYPE_ORGANISATION + myOrganisationID;
//		if (!myOrganisationID.equals(localOrganisationID) && !myOrganisationID.equals(organisationID) && !myOrganisationUserID.equals(userID)) {
		if (!myOrganisationID.equals(localOrganisationID) && !myOrganisationUserID.equals(userID)) {
//			logger.error("Attached invalid userLocal "+this+" from different organisation in my organisation "+myOrganisationID+" datastore!!!",
//					new Exception());
			// I think it's fine to make this a blocking exception. Marco.
			throw new IllegalStateException("Attached invalid userLocal "+this+" from different organisation in my organisation "+myOrganisationID+" datastore!!!");
		}
	}

	/* (non-Javadoc)
	 * @see javax.jdo.listener.AttachCallback#jdoPreAttach()
	 */
	@Override
	public void jdoPreAttach() {
		// Since we null the field 'password' in jdoPostDetach(...), we must ensure here
		// that this doesn't erase the password in the database.
		boolean illegallyDetachedInstance = false;
		try {
			if (authorizedObjectRefs == null)
				illegallyDetachedInstance = true;
		} catch (JDODetachedFieldAccessException e) {
			// silently ignore
		}

		try {
			if (userSecurityGroups == null)
				illegallyDetachedInstance = true;
		} catch (JDODetachedFieldAccessException e) {
			// silently ignore
		}

		if (illegallyDetachedInstance)
			throw new IllegalStateException("Cannot attach this instance of UserLocal because it was detached with insufficient access rights and therefore some data was erased from it: " + this);

		try {
			if (password == null); // just trying to access this field
		} catch (JDODetachedFieldAccessException e) {
			// Fine, the password field was not detached and we therefore do not need to do anything => return.
			return;
		}

		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();
		UserLocalID userLocalID = (UserLocalID) JDOHelper.getObjectId(this);
		if (userLocalID == null)
			throw new IllegalStateException("How can it be that a detached object has no object-id assigned?!");

		UserLocal attached;
		try {
			attached = (UserLocal) pm.getObjectById(userLocalID);
		} catch (JDOObjectNotFoundException x) {
			// Fine, the object doesn't exist. No need to restore the password.
			attached = null;
		}

		if (attached != null)
			this.password = attached.password;
	}

}
