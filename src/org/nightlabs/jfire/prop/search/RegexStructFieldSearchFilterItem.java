package org.nightlabs.jfire.prop.search;

import java.util.Collection;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

public class RegexStructFieldSearchFilterItem extends AbstractTextBasedStructFieldSearchFilterItem {

	private static final long serialVersionUID = 20100125L;
	
	public RegexStructFieldSearchFilterItem(Collection<StructFieldID> collection, MatchType matchType, String searchText) {
		super(collection, matchType, searchText);
	}

	@Override
	public Class<? extends DataField> getDataFieldClass() {
		return RegexDataField.class;
	}
}
