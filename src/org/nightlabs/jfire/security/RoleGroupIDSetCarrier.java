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
import java.util.Set;

import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author marco schulze - marco at nightlabs dot de
 */
public class RoleGroupIDSetCarrier implements Serializable
{
	private static final long serialVersionUID = 1L;

//	public RoleGroupIDSetCarrier()
//	{
//		excluded = new HashSet<RoleGroupID>();
//		assignedToUser = new HashSet<RoleGroupID>();
//		assignedToUserGroups = new HashSet<RoleGroupID>();
//	}

	public RoleGroupIDSetCarrier(
			UserID userID,
			AuthorityID authorityID,
			Set<RoleGroupID> allInAuthority,
			Set<RoleGroupID> assignedToUser,
			Set<RoleGroupID> assignedToUserGroups,
			Set<RoleGroupID> assignedToOtherUser,
			boolean inAuthority,
			boolean controlledByOtherUser
	)
	{
		if (userID == null)
			throw new IllegalArgumentException("userID must not be null!");

		if (authorityID == null)
			throw new IllegalArgumentException("authorityID must not be null!");

		if (allInAuthority == null)
			throw new IllegalArgumentException("allInAuthority must not be null!");

		if (assignedToUser == null)
			throw new IllegalArgumentException("assignedToUser must not be null!");

		if (assignedToUserGroups == null)
			throw new IllegalArgumentException("assignedToUserGroups must not be null!");

		if (assignedToOtherUser == null)
			throw new IllegalArgumentException("assignedToOtherUser must not be null!");

		this.userID = userID;
		this.authorityID = authorityID;
		this.allInAuthority = allInAuthority;
		this.assignedToUser = assignedToUser;
		this.assignedToUserGroups = assignedToUserGroups;
		this.assignedToOtherUser = assignedToOtherUser;
		this.inAuthority = inAuthority;
		this.controlledByOtherUser = controlledByOtherUser;
	}

	private UserID userID;
	private AuthorityID authorityID;
	private Set<RoleGroupID> allInAuthority;
	private Set<RoleGroupID> assignedToUser;
	private Set<RoleGroupID> assignedToUserGroups;
	private Set<RoleGroupID> assignedToOtherUser;
	private boolean inAuthority;
	private boolean controlledByOtherUser;

	/**
	 * Get the {@link UserID} of the {@link User} for which this <code>RoleGroupIDSetCarrier</code> has been created.
	 * @return the user-id.
	 */
	public UserID getUserID() {
		return userID;
	}
	/**
	 * Get the {@link AuthorityID} of the {@link AuthorityID} for which this <code>RoleGroupIDSetCarrier</code> has been created.
	 * @return the authority-id.
	 */
	public AuthorityID getAuthorityID() {
		return authorityID;
	}
	/**
	 * Get all <code>RoleGroupID</code>s within the current <code>Authority</code> (i.e. the ones defined in the <code>AuthorityType</code>).
	 * @return the role-group-ids of all role-groups within the current authority.
	 */
	public Set<RoleGroupID> getAllInAuthority() {
		return allInAuthority;
	}
	/**
	 * Get all <code>RoleGroupID</code>s which are <b>directly</b> assigned to the <code>User</code> within the current <code>Authority</code>.
	 * If {@link #getUserID()} references an {@link UserGroup}, this still reflects all <b>directly</b> assigned rights.
	 *
	 * @return the directly assigned rights.
	 */
	public Set<RoleGroupID> getAssignedToUser() {
		return assignedToUser;
	}
	/**
	 * Get all <code>RoleGroupID</code>s which are <b><u>in</u>directly</b> assigned to the user via one or more of its {@link UserGroup}s.
	 *
	 * @return the indirectly (via user-groups) assigned rights.
	 */
	public Set<RoleGroupID> getAssignedToUserGroups() {
		return assignedToUserGroups;
	}
	/**
	 * Get all <code>RoleGroupID</code>s which are <b><u>in</u>directly</b> assigned to the user via the other user (see {@link User#USERID_OTHER}),
	 * but only if the current <code>User</code> is neither directly in the authority, nor one of its <code>UserGroup</code>s.
	 * As soon as one of its <code>UserGroup</code>s or the <code>User</code> itself is member of this <code>Authority</code>, this
	 * <code>Set</code> is empty.
	 *
	 * @return the rights assigned to the other user ({@link User#USERID_OTHER}).
	 */
	public Set<RoleGroupID> getAssignedToOtherUser() {
		return assignedToOtherUser;
	}
	/**
	 * Find out whether the user is himself in the <code>Authority</code>. A user who is not in an authority directly, can still be managed 
	 * in the authority indirectly via its user-groups. If no {@link UserGroup} containing the user is in the Authority, the "other" user {@link User#USERID_OTHER}
	 * defines the rights.
	 *
	 * @return <code>true</code> if the user is directly in the <code>Authority</code> (and thus can have individual rights assigned).
	 */
	public boolean isInAuthority() {
		return inAuthority;
	}
	/**
	 * This is <code>true</code>, if the user is neither directly in an {@link Authority} nor via one of its {@link UserGroup}s.
	 * If this <code>RoleGroupIDSetCarrier</code> has been created for the other user, this flag is <code>false</code>.
	 *
	 * @return <code>true</code> if the user is neither directly in the authority, nor one of its user-groups.
	 */
	public boolean isControlledByOtherUser() {
		return controlledByOtherUser;
	}
}
