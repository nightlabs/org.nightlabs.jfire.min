/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 08.06.2004
 */
package org.nightlabs.jfire.servermanager.config;

import java.util.ArrayList;
import java.util.List;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author marco
 */
public class OrganisationConfigModule extends ConfigModule
{
	protected List organisations = null;

	/**
	 * @return Returns the organisations.
	 */
	public List getOrganisations() {
		return organisations;
	}
	/**
	 * @param organisations The organisations to set.
	 */
	public void setOrganisations(List _organisations) {
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
				organisationCaption);
//				masterOrganisationID);
//				jdoPersistenceManagerJNDIName);
		organisations.add(org);
		setChanged();
		return org;
	}

	/**
	 * @see org.nightlabs.config.Initializable#init()
	 */
	public void init() throws InitException {
		if (organisations == null)
			setOrganisations(new ArrayList());
	}
}
