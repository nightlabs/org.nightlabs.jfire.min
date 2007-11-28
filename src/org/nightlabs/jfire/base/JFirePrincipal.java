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

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.security.RoleSet;


/**
 * @author nick@nightlabs.de
 *
 * TODO this needs refactoring. I don't understand, what the "getLookup()" method is good for. We should improve the API. Marco.
 */
public class JFirePrincipal
	extends JFireBasePrincipal
	implements Principal
{
	private static final long serialVersionUID = 1L;
	private transient Lookup lookup;

	public JFirePrincipal(LoginData loginData, boolean _userIsOrganisation, Lookup _lookup, RoleSet _roleSet)
	{
		super(loginData, _userIsOrganisation, _roleSet);

		if (_lookup == null)
			throw new NullPointerException("lookup must not be null!");
		this.lookup = _lookup;
		this.lookup.setJFirePrincipal(this);
	}

	public Lookup getLookup()
	{
		if (lookup == null) {
			lookup = new Lookup(getOrganisationID());
			lookup.setJFirePrincipal(this);
		}

		return lookup;
	}
}
