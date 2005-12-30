/*
 * Created 	on Dec 16, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.search;

import org.nightlabs.jfire.person.util.PersonSearchFilter;
import org.nightlabs.j2ee.InitialContextProvider;
import org.nightlabs.jdo.search.ItemBasedSearchFilterProvider;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jdo.search.SearchFilterItemListMutator;
import org.nightlabs.jdo.search.SearchResultFetcher;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class DynamicPersonSearchFilterProvider extends
		ItemBasedSearchFilterProvider {

	/**
	 * @see DynamicPersonSearchFilterProvider#DynamicPersonSearchFilterProvider(SearchFilterItemListMutator)
	 * @param listMutator
	 */
	public DynamicPersonSearchFilterProvider(SearchFilterItemListMutator listMutator) {
		super(listMutator);
	}

	/**
	 * @see DynamicPersonSearchFilterProvider#DynamicPersonSearchFilterProvider(SearchFilterItemListMutator)
	 * @param listMutator
	 */
	public DynamicPersonSearchFilterProvider(SearchFilterItemListMutator listMutator, SearchResultFetcher resultFetcher, InitialContextProvider login) {
		super(listMutator);
		setSearchResultFetcher(resultFetcher, login);
	}
	
	/**
	 * @see org.nightlabs.jdo.search.ItemBasedSearchFilterProvider#createSearchFilter()
	 */
	protected SearchFilter createSearchFilter() {
		return new PersonSearchFilter();
	}	

}
