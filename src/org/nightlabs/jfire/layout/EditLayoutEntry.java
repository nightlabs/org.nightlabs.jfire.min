package org.nightlabs.jfire.layout;

import org.nightlabs.clientui.layout.GridData;

/**
 * Used to describe an entry in a Layout Grid:
 * <ul>
 * 	<li>The actual layout information via {@link #getGridData()}</li>
 * 	<li>The type of entry it is {@link #getEntryType()}</li>
 * 	<li>The object that is used to derive which UI element to build {@link #getObject()}</li>
 * </ul>
 *
 * Such a grid layout may be persisted like in a {@link AbstractEditLayoutConfigModule}.
 *
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 *
 * @param <T> The type of object that hold the information used to create the proper UI representation.
 */
public interface EditLayoutEntry<T>
{
	final String	ENTRY_TYPE_SEPARATOR	= "separator";

	/**
	 * Returns the GridData object holding the layout information for a SWT GridData used to when building the UI.
	 *
	 * @return the GridData object holding the layout information for a SWT GridData used to when building the UI.
	 */
	GridData getGridData();

	/**
	 * Sets the GridData object holding the layout information for a SWT GridData used to when building the UI.
	 *
	 * @param gridData The GridData object holding the layout information for a SWT GridData used to when building the UI.
	 */
	void setGridData(GridData gridData);

	/**
	 * Returns its entry type. This may be used to for whatever wanted (like a separator - {@link #ENTRY_TYPE_SEPARATOR}).
	 *
	 * @return its entry type. This may be used to for whatever wanted (like a separator - {@link #ENTRY_TYPE_SEPARATOR}).
	 */
	String getEntryType();

//	String getIdentifier();
//	void setIdentifier(String objectID);

	/**
	 * The information needed that (combined with the {@link #getEntryType()}) enables the creation of a UI for querying
	 * the aspect represented by this element.
	 *
	 * @return The information needed that (combined with the {@link #getEntryType()}) enables the creation of a UI for querying
	 * the aspect represented by this element.
	 */
	T getObject();

	/**
	 * Sets the information needed that (combined with the {@link #getEntryType()}) enables the creation of a UI for
	 * querying the aspect represented by this element.
	 *
	 * @param object The information needed that (combined with the {@link #getEntryType()}) enables the creation of a UI
	 *               for querying the aspect represented by this element.
	 */
	void setObject(T object);

}