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

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.security.AuthorizedObjectRef"
 *		detachable="true"
 *		table="JFireBase_UserSecurityGroupRef"
 *
 * @jdo.inheritance strategy="superclass-table"
 * @!jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UserSecurityGroupRef")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class UserSecurityGroupRef extends AuthorizedObjectRef
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UserSecurityGroupRef.class);

	/**
	 * @deprecated Only for JDO!
	 */
	protected UserSecurityGroupRef() { }

	public UserSecurityGroupRef(Authority _authority, AuthorizedObject _authorizedObject, boolean _visible)
	{
		super(_authority, _authorizedObject, _visible);
		if (!(_authorizedObject instanceof UserSecurityGroup))
			throw new IllegalArgumentException("authorizedObject must be an instance of UserSecurityGroup!");
	}

	@Override
	public UserSecurityGroup getAuthorizedObject() {
		UserSecurityGroup result = (UserSecurityGroup) super.getAuthorizedObject();

		if (logger.isDebugEnabled())
			logger.debug("getAuthorizedObject: this=" +  this + " result=" + result);

		return result;
	}

	@Override
	protected void _addRole(Role role, int incRefCount)
	{
		super._addRole(role, incRefCount);

		if (logger.isDebugEnabled())
			logger.debug("_addRole: this=" +  this + " role=" + role + " incRefCount=" + incRefCount);

		// Why do I get a class cast exception in the following line? Maybe a jpox bug?
		// I think this bug has been solved a long while ago... has it? Marco. 2008-05-30
		UserSecurityGroup userSecurityGroup = (UserSecurityGroup)getAuthorizedObject();

		// We need to add this role to all UserRefs belonging to the Users of this group.
		for (AuthorizedObject member : userSecurityGroup.getMembers()) {
			AuthorizedObjectRef ref = member.getAuthorizedObjectRef(getAuthorityID());
			if (ref == null)
				throw new IllegalStateException(member.toString() + " is member of \"" + this.toString() + "\", which has a UserSecurityGroupRef in " + getAuthority() + ", but there is no AuthorizedObjectRef for this User in this Authority!");
			else
				ref._addRole(role, incRefCount);
		}
	}

	@Override
	protected void _removeRole(Role role, int decRefCount)
	{
		UserSecurityGroup userSecurityGroup = (UserSecurityGroup)getAuthorizedObject();

		// We need to remove this role from all UserRefs belonging to the Users of this group.
		for (AuthorizedObject member : userSecurityGroup.getMembers()) {
			AuthorizedObjectRef ref = member.getAuthorizedObjectRef(getAuthorityID());
			if (ref == null)
				throw new IllegalStateException(member.toString()+" is member of \"" + this.toString() + "\", which has a UserSecurityGroupRef in " + getAuthorityID() + ", but there is no AuthorizedObjectRef for this User in this Authority!");
			else
				ref._removeRole(role, decRefCount);
		}

		super._removeRole(role, decRefCount);
	}

}
