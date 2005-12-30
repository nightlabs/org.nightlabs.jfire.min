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

package org.nightlabs.jfire.base.jdo.cache;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassMap;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class Carrier
{
	private static String referenceType = null;

	private Key key;
	private Reference ref;
	private Object object;

	/**
	 * @see #getObjectIDs()
	 */
	private Set objectIDs = null;

	/**
	 * When has this <tt>Carrier</tt> been created.
	 */
	private long createDT = System.currentTimeMillis();

	/**
	 * When has this Carrier been accessed (and therefore used) the last time.
	 */
	private long accessDT = System.currentTimeMillis();

	private CarrierContainer carrierContainer;

	public Carrier(Key key, Object pcObject, CarrierContainer carrierContainer)
	{
		if (key == null)
			throw new NullPointerException("Parameter key must not be null!");

		if (pcObject == null)
			throw new NullPointerException("Parameter pcObject must not be null!");

		if (carrierContainer == null)
			throw new NullPointerException("Parameter carrierContainer must not be null!");

		this.key = key;

		if (referenceType == null)
			referenceType = carrierContainer.getCache().getCacheCfMod().getReferenceType();

		if (CacheCfMod.REFERENCE_TYPE_SOFT.equals(referenceType)) {
			this.ref = new SoftReference(pcObject);
			this.object = null;
		}
		else if (CacheCfMod.REFERENCE_TYPE_HARD.equals(referenceType)) {
			this.ref = null;
			this.object = pcObject;
		}
		else
			throw new IllegalArgumentException("ReferenceType \""+referenceType+"\" configured in CacheCfMod is unknown!");

		this.carrierContainer = carrierContainer;
		carrierContainer.addCarrier(this);
	}

	/**
	 * @return Returns the key.
	 */
	public Key getKey()
	{
		return key;
	}

	/**
	 * @return Returns the object or <tt>null</tt> if the garbage collector has
	 *		removed it.
	 */
	public Object getObject()
	{
		if (ref == null)
			return object;

		return ref.get();
	}

	/**
	 * @return Returns the carrierContainer.
	 */
	public CarrierContainer getCarrierContainer()
	{
		return carrierContainer;
	}

	/**
	 * @param carrierContainer The carrierContainer to set. Currently, only <tt>null</tt>
	 *		is valid!
	 */
	public void setCarrierContainer(CarrierContainer carrierContainer)
	{
		if (carrierContainer != null)
			throw new IllegalArgumentException("Currently, carrierContainer must be null!");

		if (this.carrierContainer != null) {
			this.carrierContainer.removeCarrier(this.getKey());
		}

		this.carrierContainer = carrierContainer;
	}

	/**
	 * @return Returns the createDT.
	 */
	public long getCreateDT()
	{
		return createDT;
	}

	/**
	 * @return Returns the access time stamp, which is the time that this
	 *		<tt>Carrier</tt> has been used the last time - updated via
	 *		{@link #setAccessDT()}.
	 */
	public long getAccessDT()
	{
		return accessDT;
	}

	/**
	 * Sets the access timestamp to <tt>System.currentTimeMillis()</tt>.
	 * @see #getAccessDT()
	 */
	public void setAccessDT()
	{
		this.accessDT = System.currentTimeMillis();
	}

	/**
	 * @return Returns a <tt>Set</tt> with all JDO objectIDs of all
	 *		contained objects within the object graph (including the
	 *		objectID of the main object). If the object has already been
	 *		dumped by the garbage collector, before the objectIDs have been
	 *		collected (the first call to this method), this method will return
	 *		a Set which contains only the objectID of the main object!
	 */
	public Set getObjectIDs()
	throws IllegalArgumentException, IllegalAccessException
	{
		if (objectIDs == null) {
			Object object = getObject();
			Set set = new HashSet();
			set.add(key.getObjectID()); // this might be a special key (non-jdo-objectID) as soon as we support to store arbitrary objects into the cache.

			if (object != null)
				collectObjectIDs(new HashSet(), set, object);

			objectIDs = set;
		}

		return objectIDs;
	}

	private void collectObjectIDs(Set processedObjects, Set objectIDs, Object object)
	throws IllegalArgumentException, IllegalAccessException
	{
		if (processedObjects.contains(object))
			return;

		processedObjects.add(object);

		if (object instanceof Collection) {
			for (Iterator it = ((Collection)object).iterator(); it.hasNext(); )
				collectObjectIDs(processedObjects, objectIDs, it.next());

			return;
		}

		if (object instanceof Map) {
			for (Iterator it = ((Map)object).entrySet().iterator(); it.hasNext(); ) {
				Map.Entry me = (Map.Entry) it.next();
				collectObjectIDs(processedObjects, objectIDs, me.getKey());
				collectObjectIDs(processedObjects, objectIDs, me.getValue());
			}

			return;
		}

		if (object instanceof String)
			return;

		if (object instanceof Number)
			return;

		Object objectID = JDOHelper.getObjectId(object);
		if (objectID != null && !objectIDs.contains(objectID)) {
			objectIDs.add(objectID);

			JDOObjectID2PCClassMap.sharedInstance().initPersistenceCapableClass(
					objectID, object.getClass());
		}

		Class clazz = object.getClass();
		while (clazz != Object.class) {
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; ++i) {
				Field field = fields[i];
				int modifiers = field.getModifiers();
				if ((Modifier.STATIC & modifiers) != 0)
					continue;

				field.setAccessible(true);
				Object fieldObject = field.get(object);
				if (fieldObject != null)
					collectObjectIDs(processedObjects, objectIDs, fieldObject);
			}

			clazz = clazz.getSuperclass();
		}
	}
}
