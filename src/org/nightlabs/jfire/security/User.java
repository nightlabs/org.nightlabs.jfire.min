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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

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
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;

/**
 * @author alex
 * @author nick
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.UserID"
 *		detachable="true"
 *		table="JFireBase_User"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, userID"
 *		include-imports="id/UserID.imports.inc"
 *		include-body="id/UserID.body.inc"
 *
 * @jdo.query
 *		name="getUsersByType"
 *		query="SELECT
 *			WHERE this.userType == paramUserType &&
 *            this.userID != paramSystemUserID
 *			PARAMETERS String paramUserType, String paramSystemUserID
 *			import java.lang.String"
 *
 * @jdo.fetch-group name="User.userLocal" fields="userLocal"
 * @jdo.fetch-group name="User.person" fields="person"
 * @jdo.fetch-group name="User.name" fields="name"
 *
 * @jdo.fetch-group name="AuthorizedObject.name" fields="name"
 * @jdo.fetch-group name="AuthorizedObject.description" fields="description"
 */
@PersistenceCapable(
	objectIdClass=UserID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_User")
@FetchGroups({
	@FetchGroup(
		name=User.FETCH_GROUP_USER_LOCAL,
		members=@Persistent(name="userLocal")),
	@FetchGroup(
		name=User.FETCH_GROUP_PERSON,
		members=@Persistent(name="person")),
	@FetchGroup(
		name=User.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name="AuthorizedObject.name",
		members=@Persistent(name="name")),
	@FetchGroup(
		name="AuthorizedObject.description",
		members=@Persistent(name="description"))
})
@Queries(
	@javax.jdo.annotations.Query(
		name="getUsersByType",
		value="SELECT WHERE this.userType == paramUserType && this.userID != paramSystemUserID PARAMETERS String paramUserType, String paramSystemUserID import java.lang.String")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class User
implements Serializable, Comparable<User>, AttachCallback, DetachCallback, StoreCallback
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(User.class);

	public static final String FETCH_GROUP_PERSON = "User.person";
	public static final String FETCH_GROUP_NAME = "User.name";
	public static final String FETCH_GROUP_USER_LOCAL = "User.userLocal";

	public static final char SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID = '@';
	public static final char SEPARATOR_BETWEEN_ORGANISATION_ID_AND_REST = '?';

	/**
	 * A <code>User</code> with this type (see {@link #getUserType()}) is a
	 * normal local user (not another organisation).
	 */
	public static final String USER_TYPE_USER = "User";
	/**
	 * @deprecated Use {@link #USER_TYPE_USER} instead
	 */
	@Deprecated
	public static final String USERTYPE_USER = USER_TYPE_USER;
	/**
	 * A <code>User</code> with this type (see {@link #getUserType()}) is another
	 * organisation. The other organisation authenticates to the local organisation
	 * with a user-id composed of {@link #USER_ID_PREFIX_TYPE_ORGANISATION} and
	 * its organisation-id.
	 * <p>
	 * See the wiki article
	 * <a href="https://www.jfire.org/modules/phpwiki/index.php/Framework%20CrossOrganisationCommunication">Framework CrossOrganisationCommunication</a>
	 * for more information.
	 * </p>
	 */
	public static final String USER_TYPE_ORGANISATION = "Organisation";
	/**
	 * @deprecated Use {@link #USER_TYPE_ORGANISATION} instead
	 */
	@Deprecated
	public static final String USERTYPE_ORGANISATION = USER_TYPE_ORGANISATION;

	public static final String USER_ID_PREFIX_TYPE_ORGANISATION = "$";
	/**
	 * @deprecated Use {@link #USER_ID_PREFIX_TYPE_ORGANISATION} instead
	 */
	@Deprecated
	public static final String USERID_PREFIX_TYPE_ORGANISATION = USER_ID_PREFIX_TYPE_ORGANISATION;

	public static final Pattern PATTERN_SPLIT_LOGIN = LoginData.PATTERN_SPLIT_LOGIN;

	/**
	 * The user <tt>_Other_</tt> is used to assign rolegroups within
	 * an <tt>Authority</tt> to all users that are not registered within
	 * this <tt>Authority</tt>.
	 * <p>
	 * The user <tt>_Other_</tt> has no password assigned. This means,
	 * it is a kind of pseudo user who
	 * never logs into the system and never performs any action.
	 */
	public static final String USER_ID_OTHER = "_Other_";
	/**
	 * @deprecated Use {@link #USER_ID_OTHER} instead
	 */
	@Deprecated
	public static final String USERID_OTHER = USER_ID_OTHER;

	/**
	 * The user <tt>_System_</tt> represents the system itself. For
	 * example the initialization of datastores is done by this user.
	 * Because it does not have a password, no one can login as <tt>_System_</tt>.
	 * To allow the system itself to internally authenticate as this
	 * user, the <tt>JFireServerManager</tt> and the <tt>JFireServerLoginModule</tt>
	 * have a mechanism to create a temporary session password for this
	 * user.
	 * <p>
	 * This user has all access rights! Including <tt>_ServerAdmin_</tt> and all
	 * the roles that are registered in the datastore.
	 */
	public static final String USER_ID_SYSTEM = "_System_";
	/**
	 * @deprecated Use {@link #USER_ID_SYSTEM} instead
	 */
	@Deprecated
	public static final String USERID_SYSTEM = USER_ID_SYSTEM;

	/**
	 * The anonymous user is a workaround for guaranteeing that we have a user when the main thread logs out (to restore this identity).
	 * This user does not have any right (not even _Guest_) and therefore cannot do anything.
	 *
	 * You should never try to login as this user (it will succeed without error, but you cannot execute any EJB method).
	 */
	public static final String USER_ID_ANONYMOUS = "_Anonymous_";
	/**
	 * @deprecated Use {@link #USER_ID_ANONYMOUS} instead
	 */
	@Deprecated
	public static final String USERID_ANONYMOUS = USER_ID_ANONYMOUS;

	/**
	 * Get the <code>User</code> object of the currently authenticated user. This method uses
	 * the {@link SecurityReflector} to find this information out.
	 *
	 * @param pm the door to the datastore.
	 * @return the <code>User</code> object corresponding to the currently logged-in user.
	 */
	public static User getUser(PersistenceManager pm) {
		UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		return getUser(pm, userDescriptor.getOrganisationID(), userDescriptor.getUserID());
	}

	/**
	 * Get the <code>User</code> object of the specified <code>principal</code>, which is the API provided
	 * by EJBs to tell the currently authenticated user.
	 *
	 * @param pm the door to the datastore.
	 * @param principal the principal identifying the user for which to look up the <code>User</code> object.
	 * @return the <code>User</code> object corresponding to specified <code>principal</code>.
	 */
	public static User getUser(PersistenceManager pm, JFireBasePrincipal principal) {
		return getUser(pm, principal.getOrganisationID(), principal.getUserID());
	}

	/**
	 * Get a <code>User</code> object for a certain primary key.
	 *
	 * @param pm the door to the datastore.
	 * @param organisationID the identifier of the organisation to which the user logs in (1st part of the primary key).
	 * @param userID the local identifier of the user within the namespace of the organisation (2nd part of the primary key).
	 * @return the <code>User</code> object with the specified primary key.
	 */
	public static User getUser(PersistenceManager pm, String organisationID, String userID) {
		pm.getExtent(User.class);
		return (User) pm.getObjectById(UserID.create(organisationID, userID), true);
	}

	/**
	 * This is the organisationID to which the user belongs. Within one organisation,
	 * all the users have their organisation's ID stored here, thus it's the same
	 * value for all of them. Even if the User object represents another organisation,
	 * this member is the organisationID to which the user logs in.
	 *
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
	 * @jdo.field persistence-modifier="persistent" mapped-by="user" dependent="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="user",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private UserLocal userLocal;

	/**
	 * This field is set to {@link UserID} of the acting user (= principal, who is currently logged-in)
	 * in {@link #jdoPostDetach(Object)}, if the field has been detached but
	 * this user does not have the right to see the data in {@link #userLocal}. It leads to {@link #getUserLocal()}
	 * throw a {@link SecurityException}, if it is executed on the detached instance.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private UserID userLocalAccessDeniedToPrincipal = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String userType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=255)
	private String name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="clob"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="clob")
	private String description;

	/**
	 * @jdo.field persistence-modifier="persistent" load-fetch-group="all"
	 */
	@Persistent(
		loadFetchGroup="all",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Person person = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date changeDT;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean autogenerateName = true;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private Locale locale;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected User() { }

	public User(String _organisationID, String _userID) {
		Organisation.assertValidOrganisationID(_organisationID);
		ObjectIDUtil.assertValidIDString(_userID, "userID");

		this.organisationID = _organisationID;
		this.userID = _userID;
		if (userID.startsWith(USER_ID_PREFIX_TYPE_ORGANISATION))
			this.userType = USER_TYPE_ORGANISATION;
		else
			this.userType = USER_TYPE_USER;

		changeDT = new Date();

		setNameAuto();
//		locale = Locale.getDefault();
	}

	/**
	 * Get the organisation-identifier of the user.
	 *
	 * @return the identifier of the organisation to which the user logs in (1st part of the primary key).
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Get the user-identifier within the namespace of his organisation.
	 *
	 * @return the local identifier of the user within the namespace of the organisation (2nd part of the primary key).
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * Get the <code>UserLocal</code> holding local information for this <code>User</code>. According to our
	 * <a href="https://www.jfire.org/modules/phpwiki/index.php/Design%20Pattern%20XyzLocal">Design Pattern XyzLocal</a>,
	 * such local objects are normally not transferred between organisations.
	 * <p>
	 * <b>Important:</b> The <code>UserLocal</code> is an exception to our rule: Because an organisation needs to know
	 * which access rights it has as a user to another organisation, the <code>UserLocal</code> of an organisation-user
	 * is copied across organisations - to be more precise: it is copied to exactly that one client-organisation so that
	 * it knows its own data managed by the business partner.
	 * </p>
	 * <p>
	 * Because there is only exactly one instance of <code>UserLocal</code> for every <code>User</code> (despite of the
	 * extended primary key), this method returns exactly this one <code>UserLocal</code> - even when in a foreign datastore.
	 * </p>
	 *
	 * @return the <code>UserLocal</code> corresponding to this <code>User</code> or <code>null</code>.
	 */
	public UserLocal getUserLocal() {
		if (userLocalAccessDeniedToPrincipal != null) { // we throw a SecurityException with a text that can be parsed by our
			throw new MissingRoleException(
					userLocalAccessDeniedToPrincipal,
					AuthorityID.create(
							userLocalAccessDeniedToPrincipal.organisationID,
							Authority.AUTHORITY_ID_ORGANISATION
					),
					RoleConstants.accessRightManagement
			);
		}

		return userLocal;
	}

	protected void setUserLocal(UserLocal userLocal) {
		this.userLocal = userLocal;
	}

	/**
	 * Get the type of the user. This can be one of {@link #USER_TYPE_ORGANISATION} or
	 * {@link #USER_TYPE_USER}.
	 *
	 * @return Returns the user-type.
	 */
	public String getUserType() {
		return userType;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
		changeDT = new Date();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
		changeDT = new Date();
	}

	/**
	 * @return Returns the person.
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * Checks whether the flag {@link #autogenerateName} is set and if so, a name for this user is generated.
	 */
	public void setNameAuto() {
		if (autogenerateName) {
			if (person == null || person.getDisplayName() == null || person.getDisplayName().length() == 0) {
				this.name = userID;
			} else {
				this.name = person.getDisplayName() + " (" + userID + ")";
			}
		}
	}

	/**
	 * @param person The person to set.
	 */
	public void setPerson(Person person) {
		this.person = person;
		setNameAuto();
		changeDT = new Date();
	}

	/**
	 * @return Returns the changeDT.
	 */
	public Date getChangeDT() {
		return changeDT;
	}

	public void setAutogenerateName(boolean autogenerateName) {
		this.autogenerateName = autogenerateName;
	}

	public boolean isAutogenerateName() {
		return autogenerateName;
	}

	public String getCompleteUserID() {
		return userID + SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + organisationID;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + userID + ']';
	}

//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append(this.getClass().getName());
//		sb.append('[');
//		sb.append(userID);
//		sb.append(SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID);
//		sb.append(organisationID);
//		sb.append(']');
//		return sb.toString();
//	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof User))
			return false;

		User other = (User) obj;
		return this.getOrganisationID().equals(other.getOrganisationID()) && this.getUserID().equals(other.getUserID());
	}

	@Override
	public int hashCode() {
		return 31 * this.getOrganisationID().hashCode() ^ this.getUserID().hashCode();
	}

	/**
	 * @deprecated should not be used anymore
	 */
	@Deprecated
	public static UserSearchResult searchUsers(PersistenceManager pm, String userType, String searchStr, boolean exact, int itemsPerPage,
			int pageIndex, int userIncludeMask) throws SecurityException {
		try {
			if ("".equals(searchStr))
				searchStr = null;

			if (itemsPerPage <= 0) {
				itemsPerPage = Integer.MAX_VALUE;
				pageIndex = 0;
			}

			if (pageIndex < 0)
				pageIndex = 0;

			Query query = pm.newQuery(pm.getExtent(User.class, true));
			query.declareImports("import java.lang.String");
			query.declareParameters("String userType, String searchStr");
			StringBuffer filter = new StringBuffer();
			if (userType != null)
				filter.append("this.userType == userType");

			if (userType != null && searchStr != null)
				filter.append(" && ");

			if (searchStr != null) {
				searchStr = searchStr.toLowerCase();
				if (exact)
					filter.append("this.userID.toLowerCase() == searchStr");
				else
					filter.append("this.userID.toLowerCase().indexOf(searchStr) >= 0");
			}
			query.setFilter(filter.toString());
			query.setOrdering("this.organisationID ascending, this.userID ascending");
			Collection<User> c = (Collection<User>) query.execute(userType, searchStr);
			int itemsFound = c.size();
			Iterator<User> it = c.iterator();

			List<User> items = new ArrayList<User>();
			int idx = 0;
			int firstIdx = 0;
			int lastIdx = Integer.MAX_VALUE;
			if (pageIndex >= 0)
				firstIdx = itemsPerPage * pageIndex;
			lastIdx = firstIdx + itemsPerPage - 1;

			while (it.hasNext()) {
				User user = it.next();
				if (idx >= firstIdx)
					items.add(user);

				++idx;
				if (idx > lastIdx)
					break;
			} // while (it.hasNext()) {
			return new UserSearchResult(itemsFound, itemsPerPage, pageIndex, items);
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * Returns all users of the given type exluding the user with the given
	 * systemUserID
	 *
	 * @param pm PersistenceManager to use
	 * @param userType The userType to search for
	 * @param systemUserID The userID to exclude
	 * @return All users of the given type exluding the user with the given systemUserID
	 */
	public static Collection<User> getUsersByType(PersistenceManager pm, String userType, String systemUserID) {
		Query q = pm.newNamedQuery(User.class, "getUsersByType");
		return (Collection<User>) q.execute(userType, systemUserID);
	}

	public int compareTo(User o) {
		return this.userID.compareTo(o.getUserID());
	}

	@Override
	public void jdoPreDetach() {
		// nothing to do
	}

	private static final ThreadLocal<Boolean> disableDetachUserLocalAccessRightCheck = new ThreadLocal<Boolean>();

	public static void disableDetachUserLocalAccessRightCheck(boolean disable)
	{
		if (disable)
			disableDetachUserLocalAccessRightCheck.set(Boolean.TRUE);
		else
			disableDetachUserLocalAccessRightCheck.remove();
	}

	protected static boolean isDisableDetachUserLocalAccessRightCheck() {
		return Boolean.TRUE.equals(disableDetachUserLocalAccessRightCheck.get());
	}
	
	private static final ThreadLocal<Boolean> disableAttachUserLocalCheck = new ThreadLocal<Boolean>();
	public static void disableAttachUserLocalCheck(boolean disable)
	{
		if (disable)
			disableAttachUserLocalCheck.set(Boolean.TRUE);
		else
			disableAttachUserLocalCheck.remove();
	}

	protected static boolean isDisableAttachUserLocalCheck() {
		return Boolean.TRUE.equals(disableAttachUserLocalCheck.get());
	}
	
	@Override
	public void jdoPostDetach(Object o)
	{
		if (!Boolean.TRUE.equals(disableDetachUserLocalAccessRightCheck.get())) {
			User detached = this;
			User attached = (User) o;

			boolean checkAccessToUserLocal;
			try {
				detached.getUserLocal();
				checkAccessToUserLocal = true;
			} catch (JDODetachedFieldAccessException x) {
				// the field userLocal is not detached => no need to check access rights.
				checkAccessToUserLocal = false;
			}

			if (checkAccessToUserLocal) {
				PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
				UserID principalUserID = SecurityReflector.getUserDescriptor().getUserObjectID();
				// check authorization via the organisation-authority
				if (!Authority.getOrganisationAuthority(pm).containsRoleRef(principalUserID, RoleConstants.accessRightManagement)) {
					// clear the confidential data
					detached.userLocal = null;
					// keep silent until someone tries to access the field - see getUserLocal()
					detached.userLocalAccessDeniedToPrincipal = principalUserID;
				}
			}
		}
	}

	@Override
	public void jdoPreAttach() {
		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();

		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);

		// we ensure that a UserLocal object is not accidentally copied between datastores.
		if (!Boolean.TRUE.equals(disableAttachUserLocalCheck.get())) {
			if (!NLJDOHelper.exists(pm, this)) {
				UserLocal ul = null;
				try {
//					ul = this.getUserLocal(); // this throws an exception when the principal who has read the object from another datastore was not allowed to read it, which is a problem in cross-datastore-synchronisation of users
					ul = this.userLocal;
				} catch (JDODetachedFieldAccessException x) {
					// it's not detached - that's fine
				}


				if (ul != null) { // alternatively, we could just null the field (it would be assigned by restoreUserLocal), but it's good to see that something was made wrong.
					// we are allowed to replicate the userLocal of the querying organisation in cross-organisation sync
					// see https://www.jfire.org/modules/bugs/view.php?id=562
					if (!this.getUserID().equals(User.USER_ID_PREFIX_TYPE_ORGANISATION + localOrganisation.getOrganisationID()))
						throw new IllegalStateException("When copying a User across datastores, you must not detach the UserLocal object!");
				}
			}
		}

		if (this.getOrganisationID().equals(localOrganisation.getOrganisationID())) {
			restoreFieldsWithMissingAccessRights(pm);
			restoreUserLocal(pm);
		}
	}

	private void restoreFieldsWithMissingAccessRights(PersistenceManager pm) {
		// If the currently logged-in user is allowed to store users, we don't restore any fields and return instead.
		UserID principalUserID = SecurityReflector.getUserDescriptor().getUserObjectID();
		if (Authority.getOrganisationAuthority(pm).containsRoleRef(principalUserID, RoleConstants.storeUser))
			return;

		// The current principal does not have sufficient rights to modify any of this => restore the fields,
		// if they were detached.
		boolean restorePerson;
		try {
			this.getPerson();
			restorePerson = true;
		} catch (JDODetachedFieldAccessException x) {
			restorePerson = false;
		}

		boolean restoreUserLocal;
		try {
			this.getUserLocal();
			restoreUserLocal = true;
		} catch (JDODetachedFieldAccessException x) {
			restoreUserLocal = false;
		}

		if (restorePerson || restoreUserLocal) {
			User attachedUser = (User) pm.getObjectById(UserID.create(organisationID, userID));

			if (restorePerson)
				this.person = attachedUser.person;

			if (restoreUserLocal)
				this.userLocal = attachedUser.userLocal;
		}
	}

	private void restoreUserLocal(PersistenceManager pm) {
		try {
			this.getUserLocal();
		} catch (JDODetachedFieldAccessException x) {
			// was not detached - no need to handle this field.
			return;
		}

		pm.getExtent(UserLocal.class);
		try {
			if (this.userLocal == null) // the field may have been nulled during jdoPreDetach => re-assign
				this.userLocal = (UserLocal) pm.getObjectById(UserLocalID.create(organisationID, userID, organisationID));
		} catch (JDOObjectNotFoundException x) {
			// ignore
		}
	}

	@Override
	public void jdoPostAttach(Object detached) {
		_setNameAutoOnStore();
	}

	private void _setNameAutoOnStore() {
		try {
//			if (person != null) {
//				PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//				person.setDisplayName(null, StructLocal.getStructLocal(Person.class, StructLocal.DEFAULT_SCOPE, pm));
//			}

			setNameAuto();
		} catch (JDOObjectNotFoundException x) {
			// silently ignore - maybe with a warning:
			logger.warn("_setNameAutoOnStore: failed probably due to a JPOX bug.", x);

			// TODO JPOX WORKAROUND: When copying between datastores, this happens and requires this workaround:
			//			Caused by: javax.jdo.JDOObjectNotFoundException: No such database row
			//			FailedObject:jdo/org.nightlabs.jfire.security.id.UserID?organisationID=chezfrancois.jfire.org&userID=%24reseller.jfire.org
			//			        at org.jpox.store.rdbms.request.FetchRequest.execute(FetchRequest.java:194)
			//			        at org.jpox.store.rdbms.table.ClassTable.fetch(ClassTable.java:2552)
			//			        at org.jpox.store.StoreManager.fetch(StoreManager.java:941)
			//			        at org.jpox.state.StateManagerImpl.loadNonDFGFields(StateManagerImpl.java:1734)
			//			        at org.jpox.state.StateManagerImpl.isLoaded(StateManagerImpl.java:2034)
			//			        at org.nightlabs.jfire.security.User.jdoGetperson(User.java)
			//			        at org.nightlabs.jfire.security.User._setNameAutoOnStore(User.java:668)
			//			        at org.nightlabs.jfire.security.User.jdoPreStore(User.java:683)
			//			        at org.jpox.state.JDOCallbackHandler.preStore(JDOCallbackHandler.java:281)
			//			        at org.jpox.state.StateManagerImpl.flush(StateManagerImpl.java:4918)
			//			        at org.jpox.AbstractPersistenceManager.flush(AbstractPersistenceManager.java:3233)
			//			        at org.jpox.store.rdbms.RDBMSManagedTransaction.getConnection(RDBMSManagedTransaction.java:172)
			//			        at org.jpox.store.rdbms.AbstractRDBMSTransaction.getConnection(AbstractRDBMSTransaction.java:97)
			//			        at org.jpox.resource.JdoTransactionHandle.getConnection(JdoTransactionHandle.java:246)
			//			        at org.jpox.store.rdbms.RDBMSManager.getConnection(RDBMSManager.java:426)
			//			        at org.jpox.store.rdbms.request.FetchRequest.execute(FetchRequest.java:168)
			//			        at org.jpox.store.rdbms.table.ClassTable.fetch(ClassTable.java:2552)
			//			        at org.jpox.store.StoreManager.fetch(StoreManager.java:941)
			//			        at org.jpox.state.StateManagerImpl.loadNonDFGFields(StateManagerImpl.java:1734)
			//			        at org.jpox.state.StateManagerImpl.isLoaded(StateManagerImpl.java:2034)
			//			        at org.nightlabs.jfire.security.User.jdoGetperson(User.java)
			//			        at org.nightlabs.jfire.security.User._setNameAutoOnStore(User.java:668)
			//			        at org.nightlabs.jfire.security.User.jdoPreStore(User.java:683)
			//			        at org.jpox.state.JDOCallbackHandler.preStore(JDOCallbackHandler.java:281)
			//			        at org.jpox.state.StateManagerImpl.internalMakePersistent(StateManagerImpl.java:3661)
			//			        at org.jpox.state.StateManagerImpl.makePersistent(StateManagerImpl.java:3646)
			//			        at org.jpox.state.StateManagerImpl.attachCopy(StateManagerImpl.java:4327)
			//			        at org.jpox.AbstractPersistenceManager.attachCopy(AbstractPersistenceManager.java:1431)
			//			        at org.jpox.resource.PersistenceManagerImpl.attachCopy(PersistenceManagerImpl.java:1012)
			//			        at org.jpox.state.AttachFieldManager.storeObjectField(AttachFieldManager.java:175)
			//			        at org.jpox.state.StateManagerImpl.providedObjectField(StateManagerImpl.java:2771)
			//			        at org.nightlabs.jfire.trade.Order.jdoProvideField(Order.java)
			//			        at org.nightlabs.jfire.trade.Order.jdoProvideFields(Order.java)
			//			        at org.jpox.state.StateManagerImpl.provideFields(StateManagerImpl.java:3115)
			//			        at org.jpox.state.StateManagerImpl.internalAttachCopy(StateManagerImpl.java:4389)
			//			        at org.jpox.state.StateManagerImpl.attachCopy(StateManagerImpl.java:4331)
			//			        at org.jpox.AbstractPersistenceManager.attachCopy(AbstractPersistenceManager.java:1431)
			//			        at org.jpox.AbstractPersistenceManager.internalMakePersistent(AbstractPersistenceManager.java:1161)
			//			        at org.jpox.AbstractPersistenceManager.makePersistent(AbstractPersistenceManager.java:1277)
			//			        at org.jpox.resource.PersistenceManagerImpl.makePersistent(PersistenceManagerImpl.java:738)
			//			        at org.nightlabs.jfire.store.ProductTypeActionHandler.importCrossTradeNestedProducts(ProductTypeActionHandler.java:499)
			//			        ... 50 more
		}
	}

	@Override
	public void jdoPreStore() {
		_setNameAutoOnStore();
	}

	public String getPrimaryKey() {
		return organisationID + '/' + userID;
	}

//	/**
//	 * Return the locale.
//	 * @return the locale
//	 */
//	public Locale getLocale() {
//		return locale;
//	}
//
//	/**
//	 * Sets the locale.
//	 * @param locale the locale to set
//	 */
//	public void setLocale(Locale locale) {
//		this.locale = locale;
//	}

}
