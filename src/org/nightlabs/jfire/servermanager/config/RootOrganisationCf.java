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

package org.nightlabs.jfire.servermanager.config;

import java.util.Set;

/**
 * @author unascribed
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RootOrganisationCf extends OrganisationCf
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	private ServerCf server;

	public RootOrganisationCf()
	{
	}

	public RootOrganisationCf(String _organisationID, String _organisationName, ServerCf server)
	{
		super(_organisationID, _organisationName);
		setServer(server);
	}

	public ServerCf getServer()
	{
		return server;
	}

	public void setServer(ServerCf server)
	{
		this.server = server;
		setChanged();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.servermanager.config.OrganisationCf#setServerAdmins(java.util.Set)
	 */
	@Override
	public void setServerAdmins(Set<String> _serverAdmins)
	{
		super.setServerAdmins(null);
		setChanged();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.servermanager.config.OrganisationCf#getServerAdmins()
	 */
	@Override
	public Set<String> getServerAdmins()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.servermanager.config.OrganisationCf#addServerAdmin(java.lang.String)
	 */
	@Override
	public void addServerAdmin(String userID)
	{
		setChanged();
	}

	@Override
	public void init() {
		super.init();
		if (server != null) {
			if (server.init())
				setChanged();
		}
	}
}
