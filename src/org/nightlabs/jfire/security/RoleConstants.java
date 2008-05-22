package org.nightlabs.jfire.security;

import org.nightlabs.jfire.security.id.RoleID;

public class RoleConstants {
	private RoleConstants() {}

	public static final RoleID securityManager_getRoleGroupIDSetCarrier = RoleID.create(JFireSecurityManager.class.getName() + "#getRoleGroupIDSetCarrier");
	public static final RoleID securityManager_removeRoleGroupFromUser = RoleID.create(JFireSecurityManager.class.getName() + "#removeRoleGroupFromUser");
	public static final RoleID securityManager_addRoleGroupToUser = RoleID.create(JFireSecurityManager.class.getName() + "#addRoleGroupToUser");
}
