package org.nightlabs.jfire.table.config;

import java.util.Set;

/**
 * This specifies what goes into as the actual contents inside a particular table.
 *
 * @author khaireel at nightlabs dot de
 */
public interface IColumnContentDescriptor extends IColumnDescriptor {
	/**
	 * @return the necessary fetch-groups needed to display the (JDO) items in this column.
	 */
	public String[] getFetchGroups();

	/**
	 * @return the names of the fields/types from the JDOObject, which are essentially needed to display in the column's contents.
	 */
	public Set<String> getFieldNames();

	/**
	 * @return the content-property of the column. This can be either TEXT, IMAGE, or both.
	 */
	public ColumnContentProperty getContentProperty();
}
