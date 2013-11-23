/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * objectVersion 2.1 of the License, or (at your option) any later objectVersion.          *
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

package org.nightlabs.jfire.jdo.notification;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;

/**
 * Instances of this class are used for notifying interested listeners
 * (including client-sided listeners - both implicit and explicit - as well as
 * server-sided internal listeners)
 * <p>
 * This class implements {@link Comparable}: Its natural order is sorted
 * ascendingly by its serial (see {@link #getSerial()}).
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DirtyObjectID
implements Serializable, Comparable<DirtyObjectID>
{
	private static final Logger logger = Logger.getLogger(DirtyObjectID.class);
	private static final long serialVersionUID = 1L;

	private JDOLifecycleState jdoLifecycleState;
//	private long createDT;
//	private long changeDT;
	private Object objectID;
	private String objectClassName;
	private Object objectVersion;
	private Set<String> sourceSessionIDs;
	private long serial;

	public DirtyObjectID(JDOLifecycleState jdoLifecycleState, Object objectID, String objectClass, Object objectVersion, long serial)
	{
		this.jdoLifecycleState = jdoLifecycleState;
//		this.createDT = System.currentTimeMillis();
//		this.changeDT = System.currentTimeMillis();
		this.objectID = objectID;
		this.objectClassName = objectClass;
		this.objectVersion = objectVersion;
		this.serial = serial;
		this.sourceSessionIDs = new HashSet<String>(1); // it's usually only one, hence this might be more efficient than the default constructor

		if (!(objectID instanceof ObjectID)) {
			if (logger.isDebugEnabled())
				logger.debug("objectID does not implement " + ObjectID.class.getName() + "! It is an instance of " + (objectID == null ? null : objectID.getClass().getName()) + ": " + objectID, new Exception());
		}
	}

//	/**
//	 * @param objectID The jdo object id.
//	 * @param sourceSessionID The session which caused this object to be dirty.
//	 * @param jdoLifecycleState What happened (new, dirty, deleted)
//	 * @param serial A serial obtained by {@link CacheManagerFactory#nextDirtyObjectIDSerial()}. See {@link #getSerial()}.
//	 */
//	public DirtyObjectID(Object objectID, String sourceSessionID, JDOLifecycleState jdoLifecycleState, long serial)
//	{
//		this.jdoLifecycleState = jdoLifecycleState;
////		this.createDT = System.currentTimeMillis();
////		this.changeDT = System.currentTimeMillis();
//		this.objectID = objectID;
//		this.serial = serial;
//		this.sourceSessionIDs = new HashSet<String>(1);
//		this.sourceSessionIDs.add(sourceSessionID);
//	}

	public JDOLifecycleState getLifecycleState()
	{
		return jdoLifecycleState;
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
//		this.changeDT = System.currentTimeMillis();
	}
	public void addSourceSessionIDs(Collection<String> _sourceSessionIDs)
	{
		this.sourceSessionIDs.addAll(_sourceSessionIDs);
//		this.changeDT = System.currentTimeMillis();
	}
	public Object getObjectID()
	{
		return objectID;
	}

	/**
	 * @return The fully qualified class name (as returned by {@link Object#getClass()}) of the JDO object which is
	 *		referenced by {@link #getObjectID()}. This might be <code>null</code>, if its a synthetic <code>DirtyObjectID</code>
	 *		that is generated on the client-side in order to notify changes to the cache that do not reference JDO objects.
	 */
	public String getObjectClassName()
	{
		return objectClassName;
	}

	/**
	 * @return The JDO object version of the JDO object referenced by this instance of <code>DirtyObjectID</code> or <code>null</code>,
	 *		if the JDO object has not been tagged to enable JDO versioning.
	 */
	public Object getObjectVersion()
	{
		return objectVersion;
	}
	/**
	 * The serial is used to find out which {@link DirtyObjectID} is newer when comparing two of them.
	 * It is not the ID of a <code>DirtyObjectID</code> instance. For example, it will be replaced by
	 * a new one, when multiple changes happened and are merged into one <code>DirtyObjectID</code> instance
	 * (in this case, the highest = newest number will be used).
	 *
	 * @return Returns the serial that has been passed to the constructor or set via {@link #setSerial(long)}.
	 */
	public long getSerial()
	{
		return serial;
	}
	public void setSerial(long serial)
	{
		this.serial = serial;
	}

	public void setObjectVersion(Object objectVersion) {
		this.objectVersion = objectVersion;
	}

//	public long getCreateDT()
//	{
//		return createDT;
//	}
//	public long getChangeDT()
//	{
//		return changeDT;
//	}

	/**
	 * This method compares the serial of this {@link DirtyObjectID}
	 * with another <code>DirtyObjectID</code>'s serial. The order is
	 * ascending (i.e. the newest <code>DirtyObjectID</code> is last).
	 * <p>
	 * Note, that the {@link #equals(Object)} method does not correspond
	 * to this method!
	 * </p>
	 * <p>
	 * This method takes the {@link #getObjectID()} and the {@link #getLifecycleState()} into account, as well, if the serial
	 * of both compared objects is the same. This might happen in the client
	 * when <code>DirtyObjectID</code>s are created synthetically with all of them
	 * having the same ID.
	 * </p>
	 *
	 * @see #getSerial()
	 * @see Comparable#compareTo(Object)
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(DirtyObjectID o)
	{
		int res = this.serial < o.serial ? -1 : (this.serial == o.serial ? 0 : 1);

		// because we generate "synthetic" DirtyObjectIDs in the client, which all use the same serial,
		// we loose DirtyObjectIDs when putting them into a SortedSet. That's why, we need to take the other
		// fields into account as well.
		if (res == 0) {
			if (this.objectID instanceof Comparable && o.objectID instanceof Comparable)
				res = ((Comparable)this.objectID).compareTo(o.objectID);
			else
				res = String.valueOf(this.objectID).compareTo(String.valueOf(o.objectID));
		}

		if (res == 0) {
			res = this.jdoLifecycleState.compareTo(o.jdoLifecycleState);
		}

		return res;
	}

	@Override
	public String toString()
	{
		return this.getClass().getName() + '[' + serial + ',' + jdoLifecycleState + ',' + objectID + ',' + objectClassName +',' + objectVersion+ ',' + sourceSessionIDs + ']';
	}
}
