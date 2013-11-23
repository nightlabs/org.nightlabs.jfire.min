package org.nightlabs.jfire.security;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.util.CollectionUtil;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.security.id.UndeployedRoleGroupAuthorityUserRecordID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.UndeployedRoleGroupAuthorityUserRecordID"
 *		detachable="true"
 *		table="JFireBase_UndeployedRoleGroupAuthorityUserRecord"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="roleGroupID, organisationID, authorityID, authorizedObjectID"
 *
 * @jdo.query name="getUndeployedRoleGroupAuthorityUserRecordForRoleGroupID" query="SELECT WHERE this.roleGroupID == :roleGroupID"
 */
@PersistenceCapable(
	objectIdClass=UndeployedRoleGroupAuthorityUserRecordID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UndeployedRoleGroupAuthorityUserRecord")
@Queries(
	@javax.jdo.annotations.Query(
		name="getUndeployedRoleGroupAuthorityUserRecordForRoleGroupID",
		value="SELECT WHERE this.roleGroupID == :roleGroupID")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class UndeployedRoleGroupAuthorityUserRecord
{
	public static Collection<UndeployedRoleGroupAuthorityUserRecord> getUndeployedRoleGroupAuthorityUserRecordForRoleGroup(PersistenceManager pm, RoleGroup roleGroup)
	{
		return getUndeployedRoleGroupAuthorityUserRecordForRoleGroupID(pm, roleGroup.getRoleGroupID());
	}
	public static Collection<UndeployedRoleGroupAuthorityUserRecord> getUndeployedRoleGroupAuthorityUserRecordForRoleGroupID(PersistenceManager pm, String roleGroupID)
	{
		Query q = pm.newNamedQuery(UndeployedRoleGroupAuthorityUserRecord.class, "getUndeployedRoleGroupAuthorityUserRecordForRoleGroupID");
		Collection<UndeployedRoleGroupAuthorityUserRecord> res = CollectionUtil.castCollection((Collection<?>)q.execute(roleGroupID));
		return res;
	}

	public static void createRecordsForRoleGroup(PersistenceManager pm, RoleGroup roleGroup) {
		Query q = pm.newQuery(RoleGroupRef.class);
		q.setFilter("this.roleGroupID == :roleGroupID");
		Collection<?> c = (Collection<?>) q.execute(roleGroup.getRoleGroupID());
		for (Object o : c) {
			RoleGroupRef roleGroupRef = (RoleGroupRef) o;

			for (AuthorizedObjectRef authorizedObjectRef : roleGroupRef.getAuthorizedObjectRefs()) {
				pm.makePersistent(
						new UndeployedRoleGroupAuthorityUserRecord(
								roleGroupRef.getRoleGroup(),
								roleGroupRef.getAuthority(),
								authorizedObjectRef.getAuthorizedObject()
						)
				);
			}
		}
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String roleGroupID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String authorityID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="255"
	 */
	@PrimaryKey
	@Column(length=255)
	private String authorizedObjectID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Authority authority;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IAuthorizedObject authorizedObject;

	protected UndeployedRoleGroupAuthorityUserRecord() { }

	public UndeployedRoleGroupAuthorityUserRecord(RoleGroup roleGroup, Authority authority, AuthorizedObject authorizedObject)
	{
		if (!authority.getOrganisationID().equals(authorizedObject.getOrganisationID()))
			throw new IllegalStateException("authority.organisationID != authorizedObject.organisationID :: " + JDOHelper.getObjectId(authority) + " " + JDOHelper.getObjectId(authorizedObject));

		this.roleGroupID = roleGroup.getRoleGroupID();
		this.authority = authority;
		this.organisationID = authority.getOrganisationID();
		this.authorityID = authority.getAuthorityID();
		this.authorizedObjectID = JDOHelper.getObjectId(authorizedObject).toString();
		this.authorizedObject = authorizedObject;
	}

	public String getRoleGroupID() {
		return roleGroupID;
	}
	public String getOrganisationID() {
		return organisationID;
	}
	public String getAuthorityID() {
		return authorityID;
	}
	public String getAuthorizedObjectID() {
		return authorizedObjectID;
	}

	public Authority getAuthority() {
		return authority;
	}
	public AuthorizedObject getAuthorizedObject() {
//		if (authorizedObject == null) {
//
//			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//			if (pm == null)
//				throw new IllegalStateException("Cannot obtain PersistenceManager!");
//
//			AuthorizedObjectID authorizedObjectID = (AuthorizedObjectID) ObjectIDUtil.createObjectID(this.authorizedObjectID);
//			try {
//				authorizedObject = (IAuthorizedObject) pm.getObjectById(authorizedObjectID);
//				Logger.getLogger(UndeployedRoleGroupAuthorityUserRecord.class).warn("", new IllegalStateException(""));
//			} catch (JDOObjectNotFoundException x) {
//				// silently ignore
//			}
//		}
		return (AuthorizedObject) authorizedObject;
	}
}
