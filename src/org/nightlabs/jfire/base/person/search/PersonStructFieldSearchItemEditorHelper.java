/*
 * Created 	on Dec 17, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.search;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.jfire.person.AbstractPersonStructField;
import org.nightlabs.jdo.search.SearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class PersonStructFieldSearchItemEditorHelper implements
		PersonSearchFilterItemEditorHelper {

	
	protected AbstractPersonStructField personStructField;
	
	
	protected PersonStructFieldSearchItemEditorHelper() {
		super();
	}	
	
	/**
	 * Constructs a new PersonStructFieldSearchItemEditorHelper
	 * and calls {@link #init(AbstractPersonStructField)}.
	 * 
	 * @param personStructField 
	 */
	public PersonStructFieldSearchItemEditorHelper(AbstractPersonStructField _personStructField) {
		super();
		init(_personStructField);
	}
	
	
	public void init(AbstractPersonStructField personStructField) {
		this.personStructField = personStructField;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.search.PersonSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public abstract Control getControl(Composite parent);

	/**
	 * @see org.nightlabs.jfire.base.person.search.PersonSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	public abstract SearchFilterItem getSearchFilterItem();

	/**
	 * @see org.nightlabs.jfire.base.person.search.PersonSearchFilterItemEditorHelper#newInstance()
	 */
	public PersonSearchFilterItemEditorHelper newInstance() {
		PersonStructFieldSearchItemEditorHelper result;
		try {
			result = (PersonStructFieldSearchItemEditorHelper)this.getClass().newInstance();
		} catch (Throwable t) {
			IllegalStateException ill = new IllegalStateException("Error instatiating new PersonStructFieldSearchItemEditorHelper "+this);
			ill.initCause(t);
			throw ill;
		}
		result.init(this.personStructField);
		return result;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.search.PersonSearchFilterItemEditorHelper#getDisplayName()
	 */
	public String getDisplayName() {
		return personStructField.getPersonStructBlockID()+": "+personStructField.getPersonStructFieldID();
	}

}
