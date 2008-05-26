package org.nightlabs.jfire.security;

import org.nightlabs.jfire.security.id.RoleID;

public final class RoleConstants {
	private RoleConstants() {}

	/**
	 * This right entitles the user to generally have access to the right management at all.
	 * <p>
	 * Without this right, a user cannot edit any right anywhere - not even locally within an authority where he
	 * is granted the required rights. Note, that this right does not yet allow the user to actually edit anything.
	 * In order to edit access rights somewhere, it still needs the appropriate rights. This right can be seen as a
	 * global first door, through which a user has to go in order to get to other doors (of the local authorities).
	 * </p>
	 *
	 * @see RoleGroupConstants#securityManager_accessRightManagement
	 */
	public static final RoleID securityManager_accessRightManagement = RoleID.create(JFireSecurityManager.class.getName() + "#accessRightManagement");

	public static final RoleID securityManager_getRoleGroupIDSetCarrier = RoleID.create(JFireSecurityManager.class.getName() + "#getRoleGroupIDSetCarrier");

	public static final RoleID securityManager_setRoleGroupsOfUser = RoleID.create(JFireSecurityManager.class.getName() + "#setRoleGroupsOfUser");

	public static final RoleID securityManager_assignAuthority = RoleID.create(JFireSecurityManager.class.getName() + "#assignAuthority");
	public static final RoleID securityManager_setUsersOfAuthority = RoleID.create(JFireSecurityManager.class.getName() + "#setUsersOfAuthority");

	public static final RoleID securityManager_setUsersOfUserGroup = RoleID.create(JFireSecurityManager.class.getName() + "#setUsersOfUserGroup");

	public static final RoleID securityManager_storeAuthority = RoleID.create(JFireSecurityManager.class.getName() + "#storeAuthority");
}
