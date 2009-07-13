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
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author nick
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RoleSet implements Group, Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	private Set<Principal> members = new HashSet<Principal>();
	private static final String NAME = "Roles";

//	private String name;
	/**
	 * Create a new empty RoleSet.
	 */
	public RoleSet() { }
//	public RoleSet(String name)
//	{
//		this.name = name;
//	}

	/* (non-Javadoc)
	 * @see java.security.acl.Group#addMember(java.security.Principal)
	 */
	public boolean addMember(Principal user)
	{
		return members.add(user);
	}

	/* (non-Javadoc)
	 * @see java.security.acl.Group#isMember(java.security.Principal)
	 */
	public boolean isMember(Principal member)
	{
		return members.contains(member);
	}

	/* (non-Javadoc)
	 * @see java.security.acl.Group#removeMember(java.security.Principal)
	 */
	public boolean removeMember(Principal user)
	{
		return members.remove(user);
	}

	/* (non-Javadoc)
	 * @see java.security.Principal#getName()
	 */
	public String getName()
	{
		return NAME;
//		return name;
	}

	/* (non-Javadoc)
	 * @see java.security.acl.Group#members()
	 */
	public Enumeration<? extends Principal> members()
	{
		return Collections.enumeration(members);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Principal principal : members) {
			if (sb.length() > 0)
				sb.append(',');

			sb.append(principal);
		}

		return this.getClass().getName() + '[' + sb.toString() + ']';
	}
}
