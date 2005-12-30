/*
 * Created 	on Dec 15, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.search;

import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.util.PersonSearchFilter;
import org.nightlabs.jfire.person.util.PersonSearchFilterItem;
import org.nightlabs.jfire.person.util.TextPersonSearchFilterItem;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jdo.search.SearchResultFetcher;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonStartsWithQuickSearch extends PersonQuickSearch {

	
	private String startWithNeedle;
	
	/**
	 * Construct a PersonQuickSearch for persons begining with startWithNeedle.<br/>
	 * The resultFetcher will be called when the quick-button is pressed.<br/>
	 * If buttonText is not null or an empty string this will be the Buttons text,
	 * otherwise the startWithNeedle will be used. 
	 * 
	 * @param buttonText
	 * @param resultFetcher
	 * @param startWithNeedle
	 */
	public PersonStartsWithQuickSearch(String buttonText, SearchResultFetcher resultFetcher, String startWithNeedle) {
		super(buttonText, resultFetcher);
		this.startWithNeedle = startWithNeedle;
		if ((buttonText == null) || buttonText.equals("")) {
			setButtonText(startWithNeedle);
		}
	}
	
	/**
	 * Construct a PersonQuickSearch for persons begining with startWithNeedle.<br/>
	 * The resultFetcher will be called when the quick-button is pressed.
	 *  
	 * @param resultFetcher
	 * @param startWithNeedle
	 */
	public PersonStartsWithQuickSearch(SearchResultFetcher resultFetcher, String startWithNeedle) {
		super(startWithNeedle,resultFetcher);
		this.startWithNeedle = startWithNeedle;
	}
	
	/**
	 * Overrides and adds TextPersonSearchFilter for PersonalData/Name and PersonalData/Company to
	 * begin with startWithNeedle.
	 * 
	 * @see org.nightlabs.jdo.search.SearchFilterProvider#getPersonSearchFilter()
	 */
	public SearchFilter getSearchFilter() {
		PersonSearchFilter filter =  super.getPersonSearchFilter(false);
		// add Name filter
		PersonSearchFilterItem item = new TextPersonSearchFilterItem(PersonStruct.PERSONALDATA_NAME,SearchFilterItem.MATCHTYPE_BEGINSWITH,startWithNeedle);
		filter.addSearchFilterItem(item);
		// add Company filter
		item = new TextPersonSearchFilterItem(PersonStruct.PERSONALDATA_COMPANY,SearchFilterItem.MATCHTYPE_BEGINSWITH,startWithNeedle);
		filter.addSearchFilterItem(item);
		return filter;
	}
}
