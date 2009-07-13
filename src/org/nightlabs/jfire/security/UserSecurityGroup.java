package org.nightlabs.jfire.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.UserSecurityGroupID"
 *		detachable="true"
 *		table="JFireBase_UserSecurityGroup"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, userSecurityGroupID"
 *		add-interfaces="org.nightlabs.jfire.security.id.AuthorizedObjectID"
 *
 * @jdo.fetch-group name="UserSecurityGroup.members" fields="members[-1]"
 *
 * @jdo.fetch-group name="AuthorizedObject.name" fields="name"
 * @jdo.fetch-group name="AuthorizedObject.description" fields="description"
 */
@PersistenceCapable(
	objectIdClass=UserSecurityGroupID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UserSecurityGroup")
@FetchGroups({
	@FetchGroup(
		name=UserSecurityGroup.FETCH_GROUP_MEMBERS,
		members=@Persistent(
			name="members",
			recursionDepth=-1)),
	@FetchGroup(
		name="AuthorizedObject.name",
		members=@Persistent(name="name")),
	@FetchGroup(
		name="AuthorizedObject.description",
		members=@Persistent(name="description"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class UserSecurityGroup
extends AuthorizedObject
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(UserSecurityGroup.class);

	public static final String FETCH_GROUP_MEMBERS = "UserSecurityGroup.members";

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
	private String userSecurityGroupID;

//	/**
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="collection"
//	 *		element-type="IAuthorizedObject"
//	 *		table="JFireBase_UserSecurityGroup_members"
//	 *
//	 * @jdo.join
//	 */
//	private Set<IAuthorizedObject> members;

// TODO DATANUCLEUS WORKAROUND - the above Set fails when deleting objects. We should remove the below one and use the above one as soon as possible

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="UserLocal"
	 *		table="JFireBase_UserSecurityGroup_userLocals"
	 *
	 * @jdo.join
	 */
@Join
@Persistent(
	table="JFireBase_UserSecurityGroup_userLocals",
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<UserLocal> members;

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
	 *		value-type="UserSecurityGroupRef"
	 *		mapped-by="userSecurityGroup"
	 *		@!mapped-by="authorizedObject" TODO DATANUCLEUS WORKAROUND: reactivate this and remove field AuthorizedObjectRef.userLocal!
	 *
	 * @jdo.key mapped-by="authorityID"
	 */
	@Persistent(
		mappedBy="userSecurityGroup",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="authorityID")
	private Map<String, UserSecurityGroupRef> userSecurityGroupRefs;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected UserSecurityGroup() { }

	public UserSecurityGroup(String organisationID, String userGroupID) {
		this.organisationID = organisationID;
		this.userSecurityGroupID = userGroupID;
//		members = new HashSet<IAuthorizedObject>();
		members = new HashSet<UserLocal>();
	}

	@Override
	public String getOrganisationID() {
		return organisationID;
	}
	public String getUserSecurityGroupID() {
		return userSecurityGroupID;
	}

	public void addMember(AuthorizedObject member)
	{
		if (member == null)
			throw new IllegalArgumentException("member must not be null!");

		if (member instanceof UserSecurityGroup)
			throw new IllegalArgumentException("A UserSecurityGroup cannot contain another UserSecurityGroup!");

		if (!member.getOrganisationID().equals(this.getOrganisationID()))
			throw new IllegalArgumentException("this.organisationID != member.organisationID");

		// TODO DATANUCLEUS WOKRAROUND - need to ensure data to be loaded - otherwise changes are sometimes lost
		members.iterator();

		if (members.contains(member))
			return;

		SecurityChangeController.getInstance().fireSecurityChangeEvent_pre_UserSecurityGroup_addMember(this, member);

		member._addUserSecurityGroup(this);
//		members.add(member);
		// TODO DATANUCLEUS WORKAROUND - need to use a set of UserLocal instead of IAuthorizedObject since deletion from it fails
		members.add((UserLocal) member);

		// Now all the roleRefs of the user must be updated.
		// Therefore, we need to find out first, which RoleGroupRefs are granted to this
		// user group. Thus, we iterate all UserRefs of this UserGroup.
		for (AuthorizedObjectRef userGroupRef : userSecurityGroupRefs.values()) {
			// To access rights for the user within an authority, we need a AuthorizedObjectRef instance.
			// If it does not yet exist, we create an invisible one.
			AuthorizedObjectRef userRef = userGroupRef.getAuthority()._createAuthorizedObjectRef(member, false);
			userRef.incrementUserSecurityGroupReferenceCount(1);

			// which RoleRefs do we have in the authority of this AuthorizedObjectRef?
			for (Iterator<RoleRef> itRoleRefs = userGroupRef.getRoleRefs().iterator(); itRoleRefs.hasNext(); ) {
				RoleRef roleRef = itRoleRefs.next();

				// Now, we need to add the reference count.
				userRef._addRole(roleRef.getRole(), roleRef.getReferenceCount());
			}
		}

		SecurityChangeController.getInstance().fireSecurityChangeEvent_post_UserSecurityGroup_addMember(this, member);
	}

	public void removeMember(AuthorizedObject member)
	{
		if (member == null)
			throw new IllegalArgumentException("authorizedObject must not be null!");

		// TODO DATANUCLEUS WOKRAROUND - need to ensure data to be loaded - otherwise changes are sometimes lost
		members.iterator();

		if (!members.contains(member))
			return;

		SecurityChangeController.getInstance().fireSecurityChangeEvent_pre_UserSecurityGroup_removeMember(this, member);

		// Now all the roleRefs of the user must be updated.
		// Therefore, we need to find out first, which RoleGroupRefs are granted to this
		// user group. Thus, we iterate all UserRefs of this UserGroup.
		for (AuthorizedObjectRef userGroupRef : userSecurityGroupRefs.values()) {
			// To access rights for the user within an authority, we need a AuthorizedObjectRef instance.
			// If it does not yet exist, we create an invisible one.
			AuthorizedObjectRef userRef = userGroupRef.getAuthority().getAuthorizedObjectRef(member);
			if(userRef == null)
				throw new IllegalArgumentException("Authority does not contain a AuthorizedObjectRef for this authorizedObject: " + member);

			// The authorizedObjectRef is not used by this group anymore, thus decrement reference count.
			userRef.decrementUserSecurityGroupReferenceCount(1);

			// which RoleRefs do we have in the authority of this AuthorizedObjectRef?
			for (Iterator<RoleRef> itRoleRefs = userGroupRef.getRoleRefs().iterator(); itRoleRefs.hasNext(); ) {
				RoleRef roleRef = itRoleRefs.next();

				// Now, we need to subtract the reference count, if the authorizedObjectRef exists.
				userRef._removeRole(roleRef.getRole(), roleRef.getReferenceCount());
			}

			// In case, the authorizedObjectRef exists only for this group, we can delete it now.
			userRef.getAuthority()._destroyAuthorizedObjectRef(member, false);
		}

		member._removeUserSecurityGroup(this);
//		if (!members.remove(member))
//			throw new IllegalStateException("member was not removed even though this.members.contains(member) returned true before! this=" + this + " member=" + member);

		// TODO DATANUCLEUS WORKAROUND - the above method seems to do sth. but does not return true. hence we try it now by comparing the size before and after
		int members_size_before = members.size();
		members.remove(member);
		if (members_size_before - 1 != members.size())
			throw new IllegalStateException("member was not removed even though this.members.contains(member) returned true before! this=" + this + " member=" + member);

		SecurityChangeController.getInstance().fireSecurityChangeEvent_post_UserSecurityGroup_removeMember(this, member);
	}

	@Override
	protected void _addAuthorizedObjectRef(AuthorizedObjectRef userGroupRef) {
		if (!(userGroupRef instanceof UserSecurityGroupRef))
			throw new IllegalArgumentException("userGroupRef is not an instance of UserSecurityGroupRef! " + userGroupRef);

		if (!this.equals(userGroupRef.getAuthorizedObject()))
			throw new IllegalArgumentException("userGroupRef.authorizedObject does not point back to me!");

		// TODO WORKAROUND DATANUCLEUS - ensure it's loaded. At this point here, I'm not 100% sure whether it's necessary, but I have the assumption. Marco.
		userSecurityGroupRefs.entrySet().iterator();

		userSecurityGroupRefs.put(userGroupRef.getAuthorityID(), (UserSecurityGroupRef) userGroupRef);

		// We need to create a AuthorizedObjectRef for all Users that are members of this group.
		// We do NOT need to give any rights, because the UserSecurityGroupRef does that already.
		for (IAuthorizedObject m : members) {
			AuthorizedObject member = (AuthorizedObject) m;
			AuthorizedObjectRef userRef = userGroupRef.getAuthority()._createAuthorizedObjectRef(member, false);
			userRef.incrementUserSecurityGroupReferenceCount(1);
		}
	}

	@Override
	protected void _removeAuthorizedObjectRef(AuthorizedObjectRef userSecurityGroupRef) {
		if (userSecurityGroupRef == null)
			throw new IllegalArgumentException("userGroupRef must not be null!");

		Authority authority = userSecurityGroupRef.getAuthority();

		for (IAuthorizedObject m : members) {
			AuthorizedObject member = (AuthorizedObject) m;
			AuthorizedObjectRef userRef = userSecurityGroupRef.getAuthority().getAuthorizedObjectRef(member);
			if (userRef == null)
				throw new IllegalStateException(this.toString() +" existed in "+ authority +", but the Authority does not contain a AuthorizedObjectRef for " + member + "!");

			// The authorizedObjectRef is not used by this group anymore, thus decrement reference count.
			userRef.decrementUserSecurityGroupReferenceCount(1);
			// In case, the authorizedObjectRef exists only for this group, we can delete it now.
			authority._destroyAuthorizedObjectRef(member, false);
		}

		// TODO WORKAROUND DATANUCLEUS - ensure it's loaded. At this point here, I'm not 100% sure whether it's necessary, but I have the assumption. Marco.
		userSecurityGroupRefs.entrySet().iterator();

		if (userSecurityGroupRefs.remove(userSecurityGroupRef.getAuthorityID()) == null)
			logger.warn("_removeAuthorizedObjectRef: this.userSecurityGroupRefs did not contain userSecurityGroupRef=" + userSecurityGroupRef + " this=" + this, new Exception("StackTrace"));
	}

	public Set<AuthorizedObject> getMembers()
	{
		// TODO DATANUCLEUS WOKRAROUND - need to ensure data to be loaded - otherwise this.getMembers().contains(...) sometimes returns false, even though the object is present.
		members.iterator();

		Set<AuthorizedObject> res = CollectionUtil.castSet(members);
		return Collections.unmodifiableSet(res);
	}

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

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((userSecurityGroupID == null) ? 0 : userSecurityGroupID.hashCode());
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
		final UserSecurityGroup other = (UserSecurityGroup) obj;
		return Util.equals(other.organisationID, this.organisationID) && Util.equals(other.userSecurityGroupID, this.userSecurityGroupID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + userSecurityGroupID + ']';
	}

	@Override
	protected void _addUserSecurityGroup(UserSecurityGroup userSecurityGroup) {
		throw new UnsupportedOperationException("Cannot put a UserSecurityGroup into another group!");
	}

	@Override
	protected void _removeUserSecurityGroup(UserSecurityGroup userSecurityGroup) {
		throw new UnsupportedOperationException("Cannot put a UserSecurityGroup into another group (and thus not remove either)!");
	}

	@Override
	public AuthorizedObjectRef getAuthorizedObjectRef(String authorityID) {
		// TODO WORKAROUND DATANUCLEUS - ensure it's loaded. At this point here, I'm not 100% sure whether it's necessary, but I have the assumption. Marco.
		userSecurityGroupRefs.entrySet().iterator();

		return userSecurityGroupRefs.get(authorityID);
	}

	@Override
	public Collection<? extends AuthorizedObjectRef> getAuthorizedObjectRefs() {
		return Collections.unmodifiableCollection(userSecurityGroupRefs.values());
	}

	@Override
	public Collection<UserSecurityGroup> getUserSecurityGroups() {
		return Collections.emptySet();
	}
}
