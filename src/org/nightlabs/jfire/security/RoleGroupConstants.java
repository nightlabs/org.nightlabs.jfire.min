package org.nightlabs.jfire.security;

import org.nightlabs.jfire.security.id.RoleGroupID;

public final class RoleGroupConstants {
	private RoleGroupConstants() { }

	/**
	 * This right entitles the user to generally have access to the right management at all.
	 * <p>
	 * Without this right, a user cannot edit any right anywhere - not even locally within an authority where he
	 * is granted the required rights. Note, that this right does not yet allow the user to actually edit anything.
	 * In order to edit access rights somewhere, it still needs the appropriate rights. This right can be seen as a
	 * global first door, through which a user has to go in order to get to other doors (of the local authorities).
	 * </p>
	 *
	 * @see RoleConstants#securityManager_accessRightManagement
	 */
	public static final RoleGroupID securityManager_accessRightManagement = RoleGroupID.create(JFireSecurityManager.class.getName() + "#accessRightManagement");

}
