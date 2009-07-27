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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class OrganisationConfigModule extends ConfigModule
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	protected List<OrganisationCf> organisations = null;

	/**
	 * @return Returns the organisations.
	 */
	public List<OrganisationCf> getOrganisations() {
		return organisations;
	}
	/**
	 * @param organisations The organisations to set.
	 */
	public void setOrganisations(List<OrganisationCf> _organisations) {
		if (this.organisations != null)
			throw new IllegalStateException("Cannot override organisations after init()!");

		this.organisations = _organisations;
		setChanged();
	}

	public OrganisationCf addOrganisation(
			String organisationID, String organisationCaption)
//			String masterOrganisationID) //, String jdoPersistenceManagerJNDIName)
	{
		OrganisationCf org = new OrganisationCf(
				organisationID,
				organisationCaption
		);
		org.setParentConfigModule(this);
		organisations.add(org);
		setChanged();
		return org;
	}

	public boolean removeOrganisation(String organisationID)
	{
		for (Iterator<OrganisationCf> it = organisations.iterator(); it.hasNext(); ) {
			if (organisationID.equals((it.next()).getOrganisationID())) {
				it.remove();
				setChanged();
				return true;
			}
		}

		return false;
	}

	@Override
	public void init() throws InitException {
		if (organisations == null)
			setOrganisations(new ArrayList<OrganisationCf>());

		for (OrganisationCf organisationCf : organisations) {
			if (organisationCf.getParentConfigModule() != this) {
				organisationCf.setParentConfigModule(this);
				setChanged();
			}

			organisationCf.init();
		}
	}
}
