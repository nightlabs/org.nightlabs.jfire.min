package org.nightlabs.jfire.prop.search;

import java.util.Collection;

import org.nightlabs.jdo.search.ISearchFilterItem;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Interface for search filter items that search within the {@link DataField}s of a {@link PropertySet}.
 * Implementors have to return the IDs of the {@link StructField}s whose {@link DataField}s should be
 * matched by this filter item in the methods {@link #getStructFieldID()} or {@link #getStructFieldIDs()}.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public interface IStructFieldSearchFilterItem
extends ISearchFilterItem
{
	/**
	 * Returns the ID of the {@link StructField} whose {@link DataField} value is matched against the search entry in this filter item.
	 * @return the ID of the {@link StructField} whose {@link DataField} value is matched against the search entry in this filter item.
	 */
	public StructFieldID getStructFieldID();

	/**
	 * Returns a collection of the IDs of the {@link StructField}s whose {@link DataField} values
	 * are to be matched against the search entry in this filter item.
	 * @return the ID of the {@link StructField} whose {@link DataField} value is matched against the search entry in this filter item.
	 */
	public Collection<StructFieldID> getStructFieldIDs();

	/**
	 * Returns the class of the {@link DataField} type that this {@link SearchFilterItem} can restrict within a search.<p>
	 * This information is used later to specify the type of the {@link DataField} parameter in the actual JDO query.
	 * 
	 * @return the class of the {@link DataField} type that this {@link SearchFilterItem} can restrict within a search.
	 */
	public Class<? extends DataField> getDataFieldClass();

}