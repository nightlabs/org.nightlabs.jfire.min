package org.nightlabs.jfire.prop.search;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

public class PhoneNumberStructFieldSearchFilterItem extends AbstractStructFieldSearchFilterItem {

	private static final long serialVersionUID = 20110905L;
	
	private String searchString;
	
	public PhoneNumberStructFieldSearchFilterItem(Collection<StructFieldID> collection, MatchType matchType, String searchText) {
		super(collection, matchType);
		this.searchString = searchText;
	}

	@Override
	public Class<? extends DataField> getDataFieldClass() {
		return PhoneNumberDataField.class;
	}
	
	@Override
	public void appendSubQuery(int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {
		filter.append("((");
		appendSubQuery("countryCode", itemIndex, imports, vars, filter, params, paramMap);
		filter.append(" ) || ( ");
		appendSubQuery("areaCode", itemIndex, imports, vars, filter, params, paramMap);
		filter.append(" ) || ( ");
		appendSubQuery("localNumber", itemIndex, imports, vars, filter, params, paramMap);
		filter.append("))");
		
	}
	
	public void appendSubQuery(String phoneNumberPart, int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {
		String varName = PropSearchFilter.QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex + "." + phoneNumberPart;
//		filter.append(varName).append(" != null && ");
		filter.append(varName).append(".toLowerCase()");
		AbstractTextBasedStructFieldSearchFilterItem.appendTextSearchMethod(getMatchType(), itemIndex, filter, params, paramMap, searchString);
	}
	
	@Override
	public boolean isConstraint() {
		return searchString != null && !searchString.isEmpty();
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return AbstractTextBasedStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES;
	}	
}
