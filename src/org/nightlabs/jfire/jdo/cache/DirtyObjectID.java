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
import java.util.HashSet;
import java.util.Set;

public class DirtyObjectID
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static enum LifecycleStage {
		NEW,
		DIRTY,
		DELETED
	}

	private LifecycleStage lifecycleStage;
	private long createDT;
	private long changeDT;
	private Object objectID;
	private Set<String> sourceSessionIDs;

	/**
	 * @param objectID The jdo object id.
	 * @param sourceSessionID The session which caused this object to be dirty.
	 */
	public DirtyObjectID(Object objectID, String sourceSessionID, LifecycleStage lifecycleStage)
	{
		this.lifecycleStage = lifecycleStage;
		this.createDT = System.currentTimeMillis();
		this.changeDT = System.currentTimeMillis();
		this.objectID = objectID;
		this.sourceSessionIDs = new HashSet<String>(1);
		this.sourceSessionIDs.add(sourceSessionID);
	}

	public LifecycleStage getLifecycleType()
	{
		return lifecycleStage;
	}

	private Set<String> unmodifiableSourceSessionIDs = null;

	public Set<String> getSourceSessionIDs()
	{
		if (unmodifiableSourceSessionIDs == null)
			unmodifiableSourceSessionIDs = Collections.unmodifiableSet(sourceSessionIDs);

		return unmodifiableSourceSessionIDs;
	}
	public void addSourceSessionID(String _sourceSessionID)
	{
		this.sourceSessionIDs.add(_sourceSessionID);
		this.changeDT = System.currentTimeMillis();
	}
	public void addSourceSessionIDs(Collection<String> _sourceSessionIDs)
	{
		this.sourceSessionIDs.addAll(_sourceSessionIDs);
		this.changeDT = System.currentTimeMillis();
	}
	public Object getObjectID()
	{
		return objectID;
	}
	public long getCreateDT()
	{
		return createDT;
	}
	public long getChangeDT()
	{
		return changeDT;
	}
}
