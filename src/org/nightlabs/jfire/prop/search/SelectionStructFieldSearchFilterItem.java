package org.nightlabs.jfire.prop.search;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;

/**
 * A {@link SearchFilterItem} to search for the different values of a {@link SelectionStructField}.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class SelectionStructFieldSearchFilterItem
extends AbstractStructFieldSearchFilterItem
{
	public static final EnumSet<MatchType> SUPPORTED_MATCH_TYPES = EnumSet.of(MatchType.EQUALS);
	
	private static final long serialVersionUID = 1L;
	private String selectedStructFieldValueID;
	
	public SelectionStructFieldSearchFilterItem(StructFieldID structFieldID, String structFieldValueID) {
		super(structFieldID, MatchType.EQUALS);
		
		if (structFieldValueID == null)
			throw new IllegalArgumentException("structFieldValueID must be non-null.");
		
		this.selectedStructFieldValueID = structFieldValueID;
	}
	
	private boolean addedParams = false;
	
	@Override
	public void appendSubQuery(int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {
		final String paramName = "value"+itemIndex; //$NON-NLS-1$
		
		if (!addedParams) {
			params.append(", "); //$NON-NLS-1$
			params.append(String.class.getName() + " " + paramName); //$NON-NLS-1$
			paramMap.put(paramName, selectedStructFieldValueID);
			
			imports.add(JDOHelper.class);
			addedParams = true;
		}
		
		filter.append(PropSearchFilter.QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex + ".structFieldValueID == " + paramName);
	}
	
	@Override
	public Class<? extends DataField> getDataFieldClass() {
		return SelectionDataField.class;
	}

	public EnumSet<MatchType> getSupportedMatchTypes() {
		return SUPPORTED_MATCH_TYPES;
	}
	
	@Override
	public boolean isConstraint() {
		return selectedStructFieldValueID != null && !selectedStructFieldValueID.isEmpty();
	}
}
