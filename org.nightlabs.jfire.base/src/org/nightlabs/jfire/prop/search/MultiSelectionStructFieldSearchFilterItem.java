package org.nightlabs.jfire.prop.search;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.MultiSelectionDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class MultiSelectionStructFieldSearchFilterItem extends AbstractStructFieldSearchFilterItem
{
	public static final EnumSet<MatchType> SUPPORTED_MATCH_TYPES = EnumSet.of(MatchType.CONTAINS, MatchType.NOTCONTAINS);
	
	private static final long serialVersionUID = 20110902L;
	private Set<String> selectedStructFieldValueIDs;

	public MultiSelectionStructFieldSearchFilterItem(
			StructFieldID personStructFieldID, MatchType matchType, Set<String> structFieldValueIDs) {
		super(personStructFieldID, matchType);
		this.selectedStructFieldValueIDs = structFieldValueIDs;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.search.SearchFilterItem#appendSubQuery(int, int, java.util.Set, java.lang.StringBuffer, java.lang.StringBuffer, java.lang.StringBuffer, java.util.Map)
	 */
	@Override
	public void appendSubQuery(int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {
			int counter = 0;
			for (String selectedStructFieldValueID : selectedStructFieldValueIDs) {
				if (MatchType.CONTAINS == matchType) {
					filter.append(QUERY_DATAFIELD_VARNAME + itemIndex + ".structFieldValueIDs_correct.contains(\""+selectedStructFieldValueID+"\")");
					if (counter < selectedStructFieldValueIDs.size() - 1)
						filter.append(" || ");
				}
				else if (MatchType.NOTCONTAINS == matchType) {
					filter.append(QUERY_DATAFIELD_VARNAME + itemIndex + ".structFieldValueIDs_correct.contains(\""+selectedStructFieldValueID+"\") == false");
					if (counter < selectedStructFieldValueIDs.size() - 1)
						filter.append(" && ");
				}
				counter++;
			}
			imports.add(JDOHelper.class);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.search.SearchFilterItem#isConstraint()
	 */
	@Override
	public boolean isConstraint() {
		return selectedStructFieldValueIDs != null && selectedStructFieldValueIDs.isEmpty();
	}

	@Override
	public Class<? extends DataField> getDataFieldClass() {
		return MultiSelectionDataField.class;
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return SUPPORTED_MATCH_TYPES;
	}

}
