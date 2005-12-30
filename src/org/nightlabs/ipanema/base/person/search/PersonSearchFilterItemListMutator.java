/*
 * Created 	on Dec 16, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.search;

import org.nightlabs.jdo.search.SearchFilterItemList;
import org.nightlabs.jdo.search.SearchFilterItemListMutator;

/**
 * Small class to change PersonSearchFilterItemLists.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonSearchFilterItemListMutator implements
		SearchFilterItemListMutator {

	/**
	 * 
	 */
	public PersonSearchFilterItemListMutator() {
		super();
	}

	/**
	 * @see org.nightlabs.jdo.search.SearchFilterItemListMutator#addItemEditor(org.nightlabs.jdo.search.SearchFilterItemList)
	 */
	public void addItemEditor(SearchFilterItemList list) {
		list.addItemEditor(new PersonSearchFilterItemEditor());
	}

}
