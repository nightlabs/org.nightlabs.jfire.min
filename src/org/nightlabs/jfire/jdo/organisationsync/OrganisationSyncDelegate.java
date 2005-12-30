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

package org.nightlabs.jfire.jdo.organisationsync;

import java.util.Collection;

import javax.jdo.PersistenceManager;

public abstract class OrganisationSyncDelegate
{
	private OrganisationSyncManagerFactory organisationSyncManagerFactory;

	public OrganisationSyncDelegate() { }

	/**
	 * This method is called once upon creation.
	 *
	 * @param factory
	 */
	public void init(OrganisationSyncManagerFactory factory)
	{
		this.organisationSyncManagerFactory = factory;
	}

	public OrganisationSyncManagerFactory getOrganisationSyncManagerFactory()
	{
		return organisationSyncManagerFactory;
	}

	/**
	 * This method is called by
	 * {@link org.nightlabs.jfire.jdo.organisationsync.OrganisationSyncManagerFactory#processDirtyObjects()}
	 * in order to notify the remote organisation.
	 *
	 * @param organisationID The organisationID that needs to be notified.
	 * @param dirtyObjectIDCarriers Instances of {@link DirtyObjectIDCarrier}.
	 * @throws Exception TODO
	 */
	public abstract void notifyDirtyObjectIDs(PersistenceManager persistenceManager, String organisationID, Collection dirtyObjectIDCarriers) throws Exception;
}
