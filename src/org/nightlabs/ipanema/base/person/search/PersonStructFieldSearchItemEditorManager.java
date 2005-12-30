/*
 * Created 	on Dec 17, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.search;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.ipanema.person.AbstractPersonStructField;
import org.nightlabs.jdo.search.SearchFilterItem;

/**
 * A concrete PersonStructFieldSearchItemEditorHelper that
 * serves as a manager for other PersonStructFieldSearchItemEditorHelper.
 * It searches for PersonStructFieldSearchItemEditorHelper in
 * the PersonSearchFilterItemEditorHelperRegistry liked to
 * a class of PersonStructFields.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonStructFieldSearchItemEditorManager extends
		PersonStructFieldSearchItemEditorHelper {

	/**
	 * 
	 */
	public PersonStructFieldSearchItemEditorManager() {
		super();
	}

	/**
	 * @param personStructField
	 */
	public PersonStructFieldSearchItemEditorManager(
			AbstractPersonStructField personStructField) {
		super(personStructField);
	}

	
	private PersonSearchFilterItemEditorHelper helper;
	private Control helperControl;
	
	/**
	 * This searches for the right helper,
	 * gets and remembers a new instance of it
	 * and the Control it returned.
	 * 
	 * @see org.nightlabs.ipanema.base.person.search.PersonSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control getControl(Composite parent) {
		if (helper != null)
			if (helperControl != null)
				return helperControl;
			
		PersonSearchFilterItemEditorHelperRegistry registry = PersonSearchFilterItemEditorHelperRegistry.sharedInstance();
		if (personStructField == null)			
			throw new IllegalStateException("Member personStructField is null. init(personStructField) might not have been called.");
		
		try {
			helper = registry.getEditorHelper(personStructField.getClass());
		} catch (PersonSearchFilterItemEditorHelperNotFoundException e) {
			IllegalStateException ill = new IllegalStateException("No helper found for class "+personStructField.getClass().getName());
			ill.initCause(e);
			throw ill;
		}
		helper = helper.newInstance();
		if (helper instanceof PersonStructFieldSearchItemEditorHelper)
			((PersonStructFieldSearchItemEditorHelper)helper).init(this.personStructField);
//		helper.
		helperControl = helper.getControl(parent); 
		return helperControl;
	}

	/**
	 * Delegates to the helper from the registry.
	 * @see #getControl(Composite)	
	 * @see org.nightlabs.ipanema.base.person.search.PersonSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	public SearchFilterItem getSearchFilterItem() {
		if (helper == null)
			throw new IllegalStateException("SearchItemEditorHelper is null and can not be asked for the SearchFilterItem");
		
		return helper.getSearchFilterItem();
	}

	/**
	 * @see org.nightlabs.ipanema.base.person.search.PersonSearchFilterItemEditorHelper#close()
	 */
	public void close() {
	}

}
