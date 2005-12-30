/*
 * Created 	on Dec 16, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.search;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.jdo.search.SearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface PersonSearchFilterItemEditorHelper {

	/**
	 * Should return the GUI-representation of this helper.
	 * 
	 * @param parent
	 * @return
	 */
	public Control getControl(Composite parent);
	
	/**
	 * Will be called to return results within
	 * {@link org.nightlabs.jdo.search.SearchFilterItemEditor#getSearchFilterItem()}  
	 * @return
	 */
	public SearchFilterItem getSearchFilterItem();
	
	/**
	 * Should return a new instance of a PersonSearchFilterItemEditorHelper.
	 * @return
	 */
	public PersonSearchFilterItemEditorHelper newInstance();
	
	/**
	 * Should return a string that can be displayed 
	 * within the combo of PersonSearchFilterItemEditor.
	 * 
	 * @return
	 */
	public String getDisplayName();
	
	/**
	 * Will be called when the
	 * helper is closed. It should be
	 * used for cleanup (removing listeners), 
	 * not for disposing widgets.
	 */	
	public void close();
	
	
}
