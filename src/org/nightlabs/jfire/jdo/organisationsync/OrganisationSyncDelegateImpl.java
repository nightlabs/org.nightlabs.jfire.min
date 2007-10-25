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

import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.jdo.JDOManager;
import org.nightlabs.jfire.jdo.JDOManagerUtil;

/**
 * This class must be defined in this project in order to be able to access the beans.
 * That's why its class name is hardcoded in
 * {@link org.nightlabs.jfire.jdo.organisationsync.OrganisationSyncManagerFactory}.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class OrganisationSyncDelegateImpl extends OrganisationSyncDelegate
{

	public OrganisationSyncDelegateImpl() { }

	/**
	 * @see org.nightlabs.jfire.jdo.organisationsync.OrganisationSyncDelegate#notifyDirtyObjectIDs(javax.jdo.PersistenceManager, java.lang.String, java.util.Collection)
	 */
	@Override
	public void notifyDirtyObjectIDs(
			PersistenceManager persistenceManager,
			String organisationID,
			Collection dirtyObjectIDCarriers)
	throws Exception
	{
		JDOManager jdoManager = JDOManagerUtil.getHome(
				Lookup.getInitialContextProperties(persistenceManager, organisationID)).create();
		jdoManager.notifyDirtyObjectIDs(dirtyObjectIDCarriers);
	}

}
