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

package org.nightlabs.jfire.jboss.j2ee;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.security.SecurityAssociation;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.RoleRef;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserLocalID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class SecurityReflectorJBoss extends SecurityReflector
{
	private static final long serialVersionUID = 1L;

	@Override
	public UserDescriptor _getUserDescriptor() throws NoUserException
	{
		Principal principal = SecurityAssociation.getPrincipal();
		if (principal == null)
			throw new NoUserException("SecurityAssociation.getPrincipal() returned null! It seems, there is no user authenticated!");

		String principalName = principal.getName();
		return UserDescriptor.parseLogin(principalName);
//		String[] parts = JFireServerLoginModule.SPLIT_USERNAME_PATTERN.split(principalName);
//		if (parts.length != 2 && parts.length != 3)
//			throw new IllegalStateException("principal.name '"+principalName+"' does not match format 'userID@organisationID/sessionID' (where sessionID is optional)!");
//
//		return new UserDescriptor(
//				parts[1], parts[0],
//				(parts.length < 3 || "".equals(parts[2])) ? (parts[1] + '_' + parts[0]) : parts[2]);
	}

	@Override
	protected InitialContext _createInitialContext() throws NoUserException {
		try {
			return new InitialContext();
		} catch (NamingException e) {
			throw new RuntimeException();
		}
	}

	@Override
	protected Properties _getInitialContextProperties() throws NoUserException {
		return null; // null is a valid argument e.g. for new InitialContext(null) and it's documented in SecurityReflector.getInitialContextProperties() that null is a valid result
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Set<RoleID> _getRoleIDs(AuthorityID authorityID) throws NoUserException
	{
		UserDescriptor userDescriptor = _getUserDescriptor();

		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager(false);
		boolean closePM = false;
		try {
			if (pm == null) {
				pm = new Lookup(userDescriptor.getOrganisationID()).getPersistenceManager();
				closePM = true;
			}

			Query q = pm.newQuery(RoleRef.class);
			q.setResult("JDOHelper.getObjectId(this.role)");
			q.setFilter("this.authorizedObjectRef.authority == :authority &&");
			q.setFilter("this.authorizedObjectRef.authorizedObject == :userLocal");

			Authority authority = (Authority) pm.getObjectById(authorityID);
			UserLocal userLocal = (UserLocal) pm.getObjectById(UserLocalID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID()));

			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("authority", authority);
			params.put("userLocal", userLocal);

			return new HashSet<RoleID>((Collection<? extends RoleID>) q.executeWithMap(params));

//			AuthorizedObjectRef authorizedObjectRef = authority.getAuthorizedObjectRef(UserLocalID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID()));
//			authorizedObjectRef.getRoleRefs()
		} finally {
			if (closePM)
				pm.close();
		}
	}

}
