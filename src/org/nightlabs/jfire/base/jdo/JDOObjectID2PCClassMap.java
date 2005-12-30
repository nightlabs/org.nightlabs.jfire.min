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

package org.nightlabs.jfire.base.jdo;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.jdo.JDOManager;
import org.nightlabs.jfire.jdo.JDOManagerUtil;
import org.nightlabs.jdo.ObjectID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JDOObjectID2PCClassMap
{
	public static final Logger LOGGER = Logger.getLogger(JDOObjectID2PCClassMap.class);

	private static JDOObjectID2PCClassMap _sharedInstance = null;

	public static JDOObjectID2PCClassMap sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new JDOObjectID2PCClassMap();

		return _sharedInstance;
	}
	
	private JDOManager jdoManager = null;

	/**
	 * key: JDOObjectID objectID<br/>
	 * value: Class objectClass
	 */
	private Map objectID2PCClassMap = new HashMap();

	/**
	 * You should not use this method if you don't really need it. The
	 * method {@link #getPersistenceCapableClass(Object)} finds out the
	 * class itself by querying the server if necessary.
	 * <p>
	 * This method exists only for efficiency reasons and is called by the
	 * {@link org.nightlabs.jfire.base.jdo.cache.Cache} whenever a new instance
	 * is put into it.
	 * <p>
	 * This method silently returns without any action, if the <tt>objectID</tt>
	 * is already known. 
	 *
	 * @param objectID The ID of a persistance-capable JDO object.
	 * @param clazz The <tt>Class</tt> of the instance which is represented
	 *		by <tt>objectID</tt>.
	 */
	public synchronized void initPersistenceCapableClass(Object objectID, Class clazz)
	{
		if (!(objectID instanceof ObjectID)) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("initPersistenceCapableClass: Won't register objectID, because it does not implement "+ObjectID.class.getName()+": " +  objectID);

			return;
		}

		if (objectID2PCClassMap.containsKey(objectID)) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("initPersistenceCapableClass: Won't do anything, because there is already a mapping for this objectID: " +  objectID);

			return;
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Storing mapping on class \"" + clazz.getName() + "\" for objectID: " +  objectID);

		objectID2PCClassMap.put(objectID, clazz);
	}

	/**
	 * This method finds the class of the JDO object specified by the given
	 * <tt>objectID</tt>.
	 *
	 * @param objectID A JDO object ID pointing to a persistent object.
	 * @return The class of the persistent object.
	 */
	public synchronized Class getPersistenceCapableClass(Object objectID)
	{
		Class jdoObjectClass = (Class) objectID2PCClassMap.get(objectID);
		if (jdoObjectClass == null) {
			int retry = 0;
			String clazzName = null;
			while (clazzName == null && retry < 2) {
				try {
					if (jdoManager == null)
						jdoManager = JDOManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();

					clazzName = jdoManager.getPersistenceCapableClassName(objectID);
				} catch (Throwable t) {
					if (++retry >= 2)
						throw new RuntimeException(t);
					clazzName = null;
					jdoManager = null;
				}
			}

			if (clazzName == null)
				throw new IllegalStateException("How the hell can clazzName be null?");

			try {
				jdoObjectClass = Class.forName(clazzName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			objectID2PCClassMap.put(objectID, jdoObjectClass);
		} // if (jdoObjectClass == null) {

		return jdoObjectClass;
	}
}
