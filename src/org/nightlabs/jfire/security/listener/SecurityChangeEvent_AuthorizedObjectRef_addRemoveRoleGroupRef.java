package org.nightlabs.jfire.security.listener;

import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.RoleGroupRef;

/**
 * Instances of this event class are propagated to
 * {@link SecurityChangeListener#pre_AuthorizedObjectRef_addRoleGroupRef(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef)},
 * {@link SecurityChangeListener#post_AuthorizedObjectRef_addRoleGroupRef(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef)},
 * {@link SecurityChangeListener#pre_AuthorizedObjectRef_removeRoleGroupRef(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef)}
 * and
 * {@link SecurityChangeListener#post_AuthorizedObjectRef_removeRoleGroupRef(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef)}.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef
extends SecurityChangeEvent
{
	private AuthorizedObjectRef authorizedObjectRef;
	private RoleGroupRef roleGroupRef;

	public SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef(
			AuthorizedObjectRef authorizedObjectRef,
			RoleGroupRef roleGroupRef
	)
	{
		if (authorizedObjectRef == null)
			throw new IllegalArgumentException("authorizedObjectRef is null!");

		if (roleGroupRef == null)
			throw new IllegalArgumentException("roleGroupRef is null!");

		this.authorizedObjectRef = authorizedObjectRef;
		this.roleGroupRef = roleGroupRef;
	}

	public AuthorizedObjectRef getAuthorizedObjectRef() {
		return authorizedObjectRef;
	}
	public RoleGroupRef getRoleGroupRef() {
		return roleGroupRef;
	}

}
