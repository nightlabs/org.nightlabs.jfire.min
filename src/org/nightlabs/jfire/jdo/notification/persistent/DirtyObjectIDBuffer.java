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

package org.nightlabs.jfire.jdo.notification.persistent;

import java.util.Collection;
import java.util.Map;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;

public interface DirtyObjectIDBuffer
{
	void init(PersistentNotificationManagerFactory persistentNotificationManagerFactory) throws DirtyObjectIDBufferException;

	/**
	 * @param objectIDs References to the new/modified/deleted JDO objectIDs. They reference
	 *		all modified (or re-attached) JDO objects and will be fetched during outgoing-notification-processing
	 *		via {@link #fetchDirtyObjectIDs()}.
	 */
	void addDirtyObjectIDs(Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs) throws DirtyObjectIDBufferException;

	/**
	 * @return Returns all data that has been added by {@link #addDirtyObjectIDs(Map)}. After these
	 *		{@link DirtyObjectID}s have been processed (means
	 *		distributed to the interested listeners), they are deleted by
	 *		{@link #clearFetchedDirtyObjectIDs()}. If an error occurs and they are not cleared,
	 *		but this method is instead called again, the same {@link DirtyObjectID}s will be returned
	 *		again plus all new ones (added in the meantime via {@link #addDirtyObjectIDs(Map)}).
	 */
	Collection<Map<JDOLifecycleState, Map<Object, DirtyObjectID>>> fetchDirtyObjectIDs() throws DirtyObjectIDBufferException;

	/**
	 * This method is called after {@link #fetchDirtyObjectIDs()} and must remove all
	 * {@link DirtyObjectID}s that have been returned (and marked) by {@link #fetchDirtyObjectIDs()}.
	 * If {@link #addDirtyObjectIDs(Map)} adds new objectIDs in the meantime, they
	 * are not marked and won't be deleted.
	 */
	void clearFetchedDirtyObjectIDs() throws DirtyObjectIDBufferException;
}
