/*
 * Created 	on Jan 9, 2005
 * 					by alex
 *
 */
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
