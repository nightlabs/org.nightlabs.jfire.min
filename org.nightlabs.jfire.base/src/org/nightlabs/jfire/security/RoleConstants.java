package org.nightlabs.jfire.security;

import org.nightlabs.jfire.security.id.RoleID;

/**
 * This class holds {@link RoleID} constants used within the security management.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public final class RoleConstants {
	private RoleConstants() {}

	/**
	 * This right entitles the user to generally have access to the right management at all.
	 * <p>
	 * For most viewing functionality, it is the only role that is necessary.
	 * </p>
	 * <p>
	 * Without this right, a user cannot edit any right anywhere - not even locally within an authority where he
	 * is granted the required rights. Note, that this right does not yet allow the user to actually edit anything
	 * (it's only read access).
	 * In order to edit access rights somewhere, he still needs the appropriate rights. This right can be seen as a
	 * global first door, through which a user has to go in order to get to other doors (of the local authorities).
	 * </p>
	 */
	public static final RoleID accessRightManagement = RoleID.create("org.nightlabs.jfire.security.accessRightManagement");

	public static final RoleID getRoleGroupIDSetCarrier = RoleID.create("org.nightlabs.jfire.security.getRoleGroupIDSetCarrier");

	public static final RoleID setGrantedRoleGroups = RoleID.create("org.nightlabs.jfire.security.setGrantedRoleGroups");

	public static final RoleID assignAuthority = RoleID.create("org.nightlabs.jfire.security.assignAuthority");

	public static final RoleID setMembersOfUserSecurityGroup = RoleID.create("org.nightlabs.jfire.security.setMembersOfUserSecurityGroup");

	public static final RoleID storeAuthority = RoleID.create("org.nightlabs.jfire.security.storeAuthority");

	public static final RoleID queryUsers = RoleID.create("org.nightlabs.jfire.security.queryUsers");

	public static final RoleID storeUser = RoleID.create("org.nightlabs.jfire.security.storeUser");

	/**
	 * This is a special role that never gets persisted to the datastore, because it is a per-server-setting
	 * (configured in a XML config module).
	 */
	public static final RoleID serverAdmin = RoleID.create("_ServerAdmin_");

	/**
	 * This is a special role that never gets persisted to the datastore. No normal user can ever have this role assigned!
	 * See <a href="https://www.jfire.org/modules/phpwiki/index.php/Internal%20System%20User%20And%20Temporary%20Passwords">Internal System User And Temporary Passwords</a>.
	 */
	public static final RoleID system = RoleID.create(User.USER_ID_SYSTEM);

	/**
	 * This is a special role that never gets persisted to the datastore. Every authenticated user has this role assigned.
	 */
	public static final RoleID guest = RoleID.create("_Guest_");
}
