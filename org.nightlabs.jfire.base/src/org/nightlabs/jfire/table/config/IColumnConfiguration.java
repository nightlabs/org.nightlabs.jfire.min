package org.nightlabs.jfire.table.config;

import java.util.List;

/**
 * Specifies the configuration of column-related config-module.
 *
 * @author khaireel at nightlabs dot de
 */
public interface IColumnConfiguration {
	/**
	 * @return the list of {@link IColumnDescriptor}s for this column-configuration.
	 */
	public List<? extends IColumnContentDescriptor> getColumnDescriptors();

	/**
	 * @return the collated Set of (non-repeated) fetch-group elements from all the {@link ColumnDescriptor}s within this column-configuration.
	 */
	public String[] getAllColumnFetchGroups();
}
