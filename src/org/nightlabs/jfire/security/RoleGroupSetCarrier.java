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

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author marco schulze - marco at nightlabs dot de
 */
public class RoleGroupSetCarrier implements Serializable
{
	private static final long serialVersionUID = 1L;

	private AuthorizedObject authorizedObject;
	private Authority authority;
	private Set<RoleGroup> allInAuthority;
	private Set<RoleGroup> assignedToUser;
	private Set<RoleGroup> assignedToUserGroups;
	private Set<RoleGroup> assignedToOtherUser;
	private boolean inAuthority;
	private boolean controlledByOtherUser;

	public AuthorizedObject getAuthorizedObject() {
		return authorizedObject;
	}
	public void setAuthorizedObject(AuthorizedObject user) {
		this.authorizedObject = user;
	}
	public Authority getAuthority() {
		return authority;
	}
	public void setAuthority(Authority authority) {
		this.authority = authority;
	}
	public Set<RoleGroup> getAllInAuthority() {
		return allInAuthority;
	}
	public void setAllInAuthority(Set<RoleGroup> allInAuthority) {
		this.allInAuthority = allInAuthority;
	}
	public Set<RoleGroup> getAssignedToUser() {
		return assignedToUser;
	}
	public void setAssignedToUser(Set<RoleGroup> assignedToUser) {
		this.assignedToUser = assignedToUser;
	}
	public Set<RoleGroup> getAssignedToUserGroups() {
		return assignedToUserGroups;
	}
	public void setAssignedToUserGroups(Set<RoleGroup> assignedToUserGroups) {
		this.assignedToUserGroups = assignedToUserGroups;
	}
	public Set<RoleGroup> getAssignedToOtherUser() {
		return assignedToOtherUser;
	}
	public void setAssignedToOtherUser(Set<RoleGroup> assignedToOtherUser) {
		this.assignedToOtherUser = assignedToOtherUser;
	}
	public boolean isInAuthority() {
		return inAuthority;
	}
	public void setInAuthority(boolean inAuthority) {
		this.inAuthority = inAuthority;
	}
	public boolean isControlledByOtherUser() {
		return controlledByOtherUser;
	}
	public void setControlledByOtherUser(boolean controlledByOtherUser) {
		this.controlledByOtherUser = controlledByOtherUser;
	}
}
