package org.nightlabs.jfire.prop.search;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

public class NumberStructFieldSearchFilterItem
extends AbstractStructFieldSearchFilterItem
{
	public static final EnumSet<MatchType> SUPPORTED_MATCH_TYPES = EnumSet.of(MatchType.EQUALS, MatchType.GREATER_THAN, MatchType.LESS_THAN, MatchType.NOTEQUALS);
	
	private int number;
	
	public NumberStructFieldSearchFilterItem(MatchType matchType, StructFieldID structFieldID, int number) {
		super(structFieldID, matchType);
		
		this.number = number;
	}

	private static final long serialVersionUID = 1L;
	
	private boolean addedParams = false;

	@Override
	public void appendSubQuery(int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {
		String numberVar = "number" + itemIndex;
		
		if (!addedParams) {
			params.append(", ").append("int ").append(numberVar);
			paramMap.put(numberVar, number);
			addedParams = true;
		}
		
		filter.append(PropSearchFilter.QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex+".intValue");
		switch(matchType) {
			case EQUALS:
				filter.append(" == ").append(numberVar);
				break;
			case NOTEQUALS:
				filter.append(" != ").append(numberVar);
				break;
			case LESS_THAN:
				filter.append(" < ").append(numberVar);
				break;
			case GREATER_THAN:
				filter.append(" > ").append(numberVar);
				break;
			default:
				throw new IllegalStateException("Illegal MatchType: '" + matchType + "', this should never happen.");
		}
	}

	@Override
	public Class<? extends DataField> getDataFieldClass() {
		return NumberDataField.class;
	}
	
	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return SUPPORTED_MATCH_TYPES;
	}
}
