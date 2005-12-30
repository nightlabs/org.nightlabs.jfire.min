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

package org.nightlabs.jfire.base;

import java.security.Principal;

import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.security.RoleSet;


/**
 * @author nick@nightlabs.de
 */
public class JFirePrincipal
	extends JFireBasePrincipal
	implements Principal
{
	protected Lookup lookup;

	public JFirePrincipal(String _userID, String _organisationID, String _sessionID, boolean _userIsOrganisation, Lookup _lookup, RoleSet _roleSet)
	{
		super(_userID, _organisationID, _sessionID, _userIsOrganisation, _roleSet);

		if (_lookup == null)
			throw new NullPointerException("lookup must not be null!");
		this.lookup = _lookup;
		this.lookup.setJFirePrincipal(this);
	}

	public Lookup getLookup()
	{
		return lookup;
	}

}
