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

package org.nightlabs.jfire.servermanager.j2ee;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.security.SecurityAssociation;
import org.nightlabs.jfire.base.JFireServerLoginModule;
import org.nightlabs.jfire.security.SecurityReflector;



/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class SecurityReflectorJBoss extends SecurityReflector
{
	private static final long serialVersionUID = 1L;

	public UserDescriptor _getUserDescriptor()
	{
		String principalName = SecurityAssociation.getPrincipal().getName();
		String[] parts = JFireServerLoginModule.SPLIT_USERNAME_PATTERN.split(principalName);
		if (parts.length != 2 && parts.length != 3)
			throw new IllegalStateException("principal.name '"+principalName+"' does not match format 'userID@organisationID/sessionID' (where sessionID is optional)!");

		return new UserDescriptor(
				parts[1], parts[0],
				(parts.length < 3 || "".equals(parts[2])) ? (parts[1] + '_' + parts[0]) : parts[2]);
	}

	public InitialContext _createInitialContext() {
		try {
			return new InitialContext();
		} catch (NamingException e) {
			throw new RuntimeException();
		}
	}

	public Properties _getInitialContextProperties() {
		return null; // TODO null should be a valid argument (i.e. new InitialContext(null) is legal), but maybe its error prone for other users - maybe an empty map would be better?! Is that possible???
	}

}
