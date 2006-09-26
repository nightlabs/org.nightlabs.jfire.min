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

package org.nightlabs.jfire.jdo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DirtyObjectIDContainer
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(DirtyObjectIDContainer.class);

	private long createDT = System.currentTimeMillis();
	private boolean closed = false;
	private DirtyObjectIDContainer master;

	protected void assertOpen()
	{
		if (closed)
			throw new IllegalStateException("This instance of DirtyObjectIDContainer (created " + createDT + ") is already closed!");
	}

	/**
	 * key: Object objectID<br/>
	 * value: DirtyObjectID dirtyObjectID
	 */
	private Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> lifecycleStage2dirtyObjectIDMap = null;
	private Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> _lifecycleStage2dirtyObjectIDMap = null;
	private static final Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>>());

	public DirtyObjectIDContainer(DirtyObjectIDContainer master)
	{
		this.master = master;
	}

	public long getCreateDT()
	{
		return createDT;
	}

	public void addDirtyObjectIDs(Collection<DirtyObjectID> newDirtyObjectIDs)
	{
		synchronized (this) {
			assertOpen();

			if (newDirtyObjectIDs.isEmpty()) {
				if (logger.isDebugEnabled())
					logger.debug("addDirtyObjectIDs: newDirtyObjectIDs is empty");

				return;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("addDirtyObjectIDs: newDirtyObjectIDs.size()=" + newDirtyObjectIDs.size());
				for (DirtyObjectID objectID : newDirtyObjectIDs) {
					logger.debug("addDirtyObjectIDs: => " + objectID);
				}
			}

			if (lifecycleStage2dirtyObjectIDMap == null)
				lifecycleStage2dirtyObjectIDMap = new HashMap<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>>(DirtyObjectID.LifecycleStage.values().length);

			for (DirtyObjectID newDirtyObjectID : newDirtyObjectIDs) {
				Object objectID = newDirtyObjectID.getObjectID();

				Map<Object, DirtyObjectID> dirtyObjectIDs = lifecycleStage2dirtyObjectIDMap.get(newDirtyObjectID.getLifecycleStage());
				if (dirtyObjectIDs == null) {
					dirtyObjectIDs = new HashMap<Object, DirtyObjectID>();
					lifecycleStage2dirtyObjectIDMap.put(newDirtyObjectID.getLifecycleStage(), dirtyObjectIDs);
				}

				DirtyObjectID dirtyObjectID = (DirtyObjectID) dirtyObjectIDs.get(objectID);
				// we add the old sourceSessionIDs to the new DirtyObjectID and then we overwrite the old one
				if (dirtyObjectID != null)
					newDirtyObjectID.addSourceSessionIDs(dirtyObjectID.getSourceSessionIDs());

				dirtyObjectIDs.put(objectID, newDirtyObjectID);
			}

			_lifecycleStage2dirtyObjectIDMap = null;
		} // synchronized (this) {

		// do this outside the synchronized block to prevent dead-locks
		if (master != null)
			master.addDirtyObjectIDs(newDirtyObjectIDs);
	}

	/**
	 * @return Returns a read-only copy of the internal map.
	 */
	public synchronized Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> getLifecycleStage2DirtyObjectIDMap()
	{
		if (closed) {
			logger.warn("getDirtyObjectIDs: DirtyObjectIDContainer has been closed already! Returning EMPTY_MAP.", new Exception("Debug stacktrace"));
			return EMPTY_MAP;
		}

		if (lifecycleStage2dirtyObjectIDMap == null)
			return EMPTY_MAP;

		if (_lifecycleStage2dirtyObjectIDMap == null) {
			Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> result = new HashMap<DirtyObjectID.LifecycleStage, Map<Object,DirtyObjectID>>(lifecycleStage2dirtyObjectIDMap);
			for (Map.Entry<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> me : lifecycleStage2dirtyObjectIDMap.entrySet())
				result.put(me.getKey(), Collections.unmodifiableMap(new HashMap<Object, DirtyObjectID>(me.getValue())));

			_lifecycleStage2dirtyObjectIDMap = Collections.unmodifiableMap(result);
		}

		return _lifecycleStage2dirtyObjectIDMap;
	}

	/**
	 * @param objectID The JDO object-id pointing to an instance of {@link DirtyObjectID}. 
	 * @return Returns either <code>null</code> or a <code>DirtyObjectID</code>.
	 */
	public synchronized DirtyObjectID getDirtyObjectID(DirtyObjectID.LifecycleStage lifecycleStage, Object objectID)
	{
		if (closed) {
			logger.warn("getDirtyObjectID: DirtyObjectIDContainer has been closed already! Returning null.", new Exception("Debug stacktrace"));
			return null;
		}

		if (lifecycleStage2dirtyObjectIDMap == null)
			return null;

		Map<Object, DirtyObjectID> m = lifecycleStage2dirtyObjectIDMap.get(lifecycleStage);
		if (m == null)
			return null;

		return (DirtyObjectID) m.get(objectID);
	}

	public void close()
	{
		synchronized (this) {
			closed = true;
		}

		// no need for synchronization, because we're already closed and no other method can change anything anymore
		// and we must not use synchronization because it cause deadlocks with the master's synchronized blocks
		if (master != null)
			master.removePart(this);

		// unnecessary, but why not help the gc a bit and forget these references
		_lifecycleStage2dirtyObjectIDMap = null;
		lifecycleStage2dirtyObjectIDMap = null;
	}

	private void removePart(DirtyObjectIDContainer part)
	{
		if (part.lifecycleStage2dirtyObjectIDMap == null) {
			if (logger.isDebugEnabled())
				logger.debug("removePart: part.lifecycleStage2dirtyObjectIDMap == null => won't remove anything");

			return;
		}

		synchronized (this) {
			for (Map.Entry<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> me1 : part.lifecycleStage2dirtyObjectIDMap.entrySet()) {
				DirtyObjectID.LifecycleStage lifecycleStage = me1.getKey();
				Map<Object, DirtyObjectID> part_dirtyObjectIDMap = me1.getValue();
				Map<Object, DirtyObjectID> master_dirtyObjectIDMap = lifecycleStage2dirtyObjectIDMap.get(lifecycleStage);
				if (master_dirtyObjectIDMap == null)
					throw new IllegalStateException("master_dirtyObjectIDMap == null for lifecycleStage == " + lifecycleStage);
	
				for (Map.Entry<Object, DirtyObjectID> me2 : part_dirtyObjectIDMap.entrySet()) {
					Object objectID = me2.getKey();
					DirtyObjectID part_dirtyObjectID = me2.getValue();
					DirtyObjectID master_dirtyObjectID = master_dirtyObjectIDMap.get(objectID);
					if (master_dirtyObjectID == null)
						throw new IllegalStateException("master_dirtyObjectID == null for part_dirtyObjectID == " + part_dirtyObjectID);
	
					if (master_dirtyObjectID.getSerial() == part_dirtyObjectID.getSerial()) {
						master_dirtyObjectIDMap.remove(objectID);
						if (logger.isDebugEnabled())
							logger.debug("removePart: removed DirtyObjectID from master: " + master_dirtyObjectID);
					}
					else {
						if (logger.isDebugEnabled())
							logger.debug("removePart: did NOT remove DirtyObjectID from master, because serial is different: part=" + part_dirtyObjectID + " master=" + master_dirtyObjectID);
					}
				}
			}
		} // synchronized (this) {
	}
}
