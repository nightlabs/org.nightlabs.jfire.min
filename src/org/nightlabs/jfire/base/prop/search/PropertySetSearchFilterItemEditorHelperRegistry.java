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

package org.nightlabs.jfire.base.prop.search;

import java.util.HashMap;
import java.util.Map;
import org.nightlabs.jfire.base.prop.search.PropertySetSearchFilterItemEditorHelperFactory;

/**
 * This registry holds PropertySetSearchFilterItemEditorHelper 
 * linked to classes of PersonStructFields.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropertySetSearchFilterItemEditorHelperRegistry {

	/**
	 * key: Class AbstractPersonStructFieldClass<br/>
	 * value: PropertySetSearchFilterItemEditorHelper personSearchFilterItemEditorHelper<br/>
	 */
	private Map<Class, PropertySetSearchFilterItemEditorHelperFactory<?>> helperFactories = new HashMap<Class, PropertySetSearchFilterItemEditorHelperFactory<?>>();
	
	/**
	 * Adds a PropertySetSearchFilterItemEditorHelper linked to the
	 * given class name to the registry. 
	 * 
	 * @param itemClassName
	 * @param itemEditor
	 */
	public void addEditorFactory(Class structFieldClass, PropertySetSearchFilterItemEditorHelperFactory<?> helperFactory) {
		helperFactories.put(structFieldClass, helperFactory);
	}
	
	/**
	 * Removes the PropertySetSearchFilterItemEditorHelper from the
	 * registry.
	 * 
	 * @param itemClassName
	 */
	public void removeEditorFactory(Class structFieldClass) {
//		if (!helperFactories.containsKey(structFieldClass))
//			return;
		helperFactories.remove(structFieldClass);
	}
	
	
	/**
	 * Returns a new instance of a PropertySetSearchFilterItemEditorHelper.
	 * 
	 * @param searchFieldClass
	 * @return
	 * @throws SearchFilterItemEditorNotFoundException
	 */
	public PropertySetSearchFilterItemEditorHelper createEditorHelper(Class structFieldClass) 
	throws PropertySetSearchFilterItemEditorHelperNotFoundException {
		PropertySetSearchFilterItemEditorHelperFactory<? extends PropertySetSearchFilterItemEditorHelper> factory = (PropertySetSearchFilterItemEditorHelperFactory<?>) helperFactories.get(structFieldClass);
		if (factory != null)
			return factory.createHelper();
		else
			throw new PropertySetSearchFilterItemEditorHelperNotFoundException("Registry does not contain an entry for "+structFieldClass.getName()); //$NON-NLS-1$
	}
	
	public boolean hasHelper(Class structFieldClass) {
		return helperFactories.containsKey(structFieldClass);
	}
	
	
	private static PropertySetSearchFilterItemEditorHelperRegistry sharedInstance;
	
	public static PropertySetSearchFilterItemEditorHelperRegistry sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new PropertySetSearchFilterItemEditorHelperRegistry();
		}
		return sharedInstance;
	}

}
