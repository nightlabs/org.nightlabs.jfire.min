/*
 * Created 	on Dec 16, 2004
 * 					by alex
 *
 */
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
