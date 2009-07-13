package org.nightlabs.jfire.security.listener;

import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.UserSecurityGroup;

/**
 * Instances of this event class are propagated to
 * {@link SecurityChangeListener#pre_UserSecurityGroup_addMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember)},
 * {@link SecurityChangeListener#post_UserSecurityGroup_addMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember)},
 * {@link SecurityChangeListener#pre_UserSecurityGroup_removeMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember)}
 * and
 * {@link SecurityChangeListener#post_UserSecurityGroup_removeMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember)}.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class SecurityChangeEvent_UserSecurityGroup_addRemoveMember
extends SecurityChangeEvent
{
	private UserSecurityGroup userSecurityGroup;
	private AuthorizedObject member;

	public SecurityChangeEvent_UserSecurityGroup_addRemoveMember(
			UserSecurityGroup userSecurityGroup,
			AuthorizedObject member
	)
	{
		if (userSecurityGroup == null)
			throw new IllegalArgumentException("userSecurityGroup is null!");

		if (member == null)
			throw new IllegalArgumentException("member is null!");

		this.userSecurityGroup = userSecurityGroup;
		this.member = member;
	}

	public UserSecurityGroup getUserSecurityGroup() {
		return userSecurityGroup;
	}
	public AuthorizedObject getMember() {
		return member;
	}
}
