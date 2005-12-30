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

package org.nightlabs.jfire.base.person.edit.fieldbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nightlabs.jfire.person.id.PersonStructFieldID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonEditorStructFieldRegistry {

	/**
	 * Ordered registry for AbstractStructFields per editorType.<br/>
	 * key: String editorType<br/>
	 * value: List editorStructFieldIDs
	 * 		value: PersonStructFieldID editorStructFieldID
	 * 
	 * 
	 */
	private Map registry = new HashMap();
	
	/**
	 * Returns the list of PersonStructFieldIDs for
	 * a specific editorType.
	 * 
	 * @param editorType
	 * @return
	 */
	public List getStructFieldList(String editorType) {
		List list = (List)registry.get(editorType);
		if (list == null) {
			list = new ArrayList();
			registry.put(editorType,list);
		}
		return list;		
	}	
	
	public void addEditorStructFieldID(String editorType, PersonStructFieldID structFieldID) {
		List list = getStructFieldList(editorType);
		list.add(structFieldID);
	}
	
	public void addEditorStructFieldID(String editorType, int idx, PersonStructFieldID structFieldID) {
		List list = getStructFieldList(editorType);
		list.add(idx, structFieldID);
	}
	
	public void clearEditorStructFieldIDs(String editorType) {
		registry.remove(editorType);
	}

	private static PersonEditorStructFieldRegistry sharedInstance;
	
	public static PersonEditorStructFieldRegistry sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new PersonEditorStructFieldRegistry();
		return sharedInstance;
	}
	
}
