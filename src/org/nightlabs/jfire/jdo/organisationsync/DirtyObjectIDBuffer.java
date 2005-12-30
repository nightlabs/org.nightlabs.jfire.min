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
import java.util.Set;

public interface DirtyObjectIDBuffer
{
	void init(OrganisationSyncManagerFactory organisationSyncManagerFactory) throws DirtyObjectIDBufferException;

	/**
	 * @param objectIDs JDO objectIDs which all implement {@link org.nightlabs.jdo.ObjectID}. They reference
	 *		all modified (or re-attached) JDO objects and will be fetched during outgoing-notification-processing
	 *		via {@link #fetchDirtyObjectIDs()}.
	 */
	void addDirtyObjectIDs(Collection objectIDs) throws DirtyObjectIDBufferException;

	/**
	 * @return Returns a Set with all dirty marked objectIDs. These records are marked
	 *		as being currently processed. After these objectIDs have been processed (means
	 *		distributed to the interested listeners), they are deleted by
	 *		{@link #clearFetchedDirtyObjectIDs()}.
	 */
	Set fetchDirtyObjectIDs() throws DirtyObjectIDBufferException;

	/**
	 * This method is called after {@link #fetchDirtyObjectIDs()} and must remove all
	 * objectIDs that have been returned (and marked) by {@link #fetchDirtyObjectIDs()}.
	 * If {@link #addDirtyObjectIDs(Collection)} adds new objectIDs in the meantime, they
	 * are not marked and won't be deleted.
	 */
	void clearFetchedDirtyObjectIDs() throws DirtyObjectIDBufferException;
}
