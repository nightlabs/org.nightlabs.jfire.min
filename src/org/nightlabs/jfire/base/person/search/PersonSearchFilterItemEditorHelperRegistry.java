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

package org.nightlabs.jfire.base.person.search;

import java.util.HashMap;
import java.util.Map;

/**
 * This registry holds PersonSearchFilterItemEditorHelper 
 * linked to classes of PersonStructFields.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonSearchFilterItemEditorHelperRegistry {

	/**
	 * key: Class AbstractPersonStructFieldClass<br/>
	 * value: PersonSearchFilterItemEditorHelper personSearchFilterItemEditorHelper<br/>
	 */
	private Map itemEditorHelpers = new HashMap();
	
	/**
	 * Adds a PersonSearchFilterItemEditorHelper linked to the
	 * given class name to the registry. 
	 * 
	 * @param itemClassName
	 * @param itemEditor
	 */
	public void addItemEditor(Class structFieldClass, PersonSearchFilterItemEditorHelper editorHelper) {
		itemEditorHelpers.put(structFieldClass,editorHelper);
	}
	
	/**
	 * Removes the PersonSearchFilterItemEditorHelper from the
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
	 * Returns a new instance of a PersonSearchFilterItemEditorHelper.
	 * 
	 * @param searchFieldClass
	 * @return
	 * @throws SearchFilterItemEditorNotFoundException
	 */
	public PersonSearchFilterItemEditorHelper getEditorHelper(Class structFieldClass) 
	throws PersonSearchFilterItemEditorHelperNotFoundException {
		PersonSearchFilterItemEditorHelper editorHelper = (PersonSearchFilterItemEditorHelper)itemEditorHelpers.get(structFieldClass);
		if (editorHelper != null)
			return editorHelper.newInstance();
		else
			throw new PersonSearchFilterItemEditorHelperNotFoundException("Registry does not contain an entry for "+structFieldClass.getName());
	}
	
	
	private static PersonSearchFilterItemEditorHelperRegistry sharedInstance;
	
	public static PersonSearchFilterItemEditorHelperRegistry sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new PersonSearchFilterItemEditorHelperRegistry();
		}
		return sharedInstance;
	}

}
