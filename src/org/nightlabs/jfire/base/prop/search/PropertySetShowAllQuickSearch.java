package org.nightlabs.jfire.base.prop.search;

import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jdo.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.prop.search.PropertySetQuickSearch;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.search.TextPropSearchFilterItem;

public class PropertySetShowAllQuickSearch extends PropertySetQuickSearch {

	public PropertySetShowAllQuickSearch(SearchResultFetcher resultFetcher) {
		super("*", resultFetcher);
	}
	
	@Override
	public SearchFilter getSearchFilter() {
		SearchFilter filter = getSearchFilter(false);
		filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.PERSONALDATA_NAME, SearchFilterItem.MATCHTYPE_NOTEQUALS, ""));
		return filter;
	}
}