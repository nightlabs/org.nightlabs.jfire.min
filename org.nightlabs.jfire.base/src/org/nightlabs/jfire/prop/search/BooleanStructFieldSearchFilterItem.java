package org.nightlabs.jfire.prop.search;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.BooleanDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * A {@link SearchFilterItem} to search on basis of {@link BooleanDataField}s.
 * 
 * @author abieber
 */
public class BooleanStructFieldSearchFilterItem extends AbstractStructFieldSearchFilterItem
{
	public static final EnumSet<MatchType> SUPPORTED_MATCH_TYPES = EnumSet.of(MatchType.EQUALS);
	
	private static final long serialVersionUID = 20110902L;
	private Boolean searchValue;

	public BooleanStructFieldSearchFilterItem(
			Collection<StructFieldID> personStructFieldIDs, MatchType matchType, Boolean searchValue) {
		super(personStructFieldIDs, matchType);
		this.searchValue = searchValue;
	}

	public BooleanStructFieldSearchFilterItem(
			StructFieldID personStructFieldID, MatchType matchType, Boolean searchValue) {
		this(Collections.singleton(personStructFieldID), matchType, searchValue);
	}
	
	@Override
	public void appendSubQuery(int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {
		if (searchValue != null) {
			filter.append(QUERY_DATAFIELD_VARNAME + itemIndex + ".value == " + Boolean.toString(searchValue));
		}
	}

	@Override
	public boolean isConstraint() {
		return searchValue != null;
	}

	@Override
	public Class<? extends DataField> getDataFieldClass() {
		return BooleanDataField.class;
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return SUPPORTED_MATCH_TYPES;
	}

}
