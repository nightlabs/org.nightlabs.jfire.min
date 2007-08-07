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

/**
 * This registry holds ProperySetSearchFilterItemEditorHelper 
 * linked to classes of PersonStructFields.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropertySetSearchFilterItemEditorHelperRegistry {

	/**
	 * key: Class AbstractPersonStructFieldClass<br/>
	 * value: ProperySetSearchFilterItemEditorHelper personSearchFilterItemEditorHelper<br/>
	 */
	private Map<Class, ProperySetSearchFilterItemEditorHelper> itemEditorHelpers = new HashMap<Class, ProperySetSearchFilterItemEditorHelper>();
	
	/**
	 * Adds a ProperySetSearchFilterItemEditorHelper linked to the
	 * given class name to the registry. 
	 * 
	 * @param itemClassName
	 * @param itemEditor
	 */
	public void addItemEditor(Class structFieldClass, ProperySetSearchFilterItemEditorHelper editorHelper) {
		itemEditorHelpers.put(structFieldClass, editorHelper);
	}
	
	/**
	 * Removes the ProperySetSearchFilterItemEditorHelper from the
	 * registry.
	 * 
	 * @param itemClassName
	 */
	public void removeItemEditor(Class structFieldClass) {
		if (!itemEditorHelpers.containsKey(structFieldClass))
			return;
		itemEditorHelpers.remove(structFieldClass);
	}
	
	
	/**
	 * Returns a new instance of a ProperySetSearchFilterItemEditorHelper.
	 * 
	 * @param searchFieldClass
	 * @return
	 * @throws SearchFilterItemEditorNotFoundException
	 */
	public ProperySetSearchFilterItemEditorHelper getEditorHelper(Class structFieldClass) 
	throws PropertySetSearchFilterItemEditorHelperNotFoundException {
		ProperySetSearchFilterItemEditorHelper editorHelper = (ProperySetSearchFilterItemEditorHelper)itemEditorHelpers.get(structFieldClass);
		if (editorHelper != null)
			return editorHelper.newInstance();
		else
			throw new PropertySetSearchFilterItemEditorHelperNotFoundException("Registry does not contain an entry for "+structFieldClass.getName());
	}
	
	
	private static PropertySetSearchFilterItemEditorHelperRegistry sharedInstance;
	
	public static PropertySetSearchFilterItemEditorHelperRegistry sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new PropertySetSearchFilterItemEditorHelperRegistry();
		}
		return sharedInstance;
	}

}
