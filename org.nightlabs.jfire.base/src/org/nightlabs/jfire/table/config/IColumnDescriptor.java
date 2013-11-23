package org.nightlabs.jfire.table.config;


/**
 * An interface defining the (configurable) information for use particularly in a table-column;
 * both the layout information, as well as the (extended) content-related information.
 *
 * @author khaireel at nightlabs dot de
 */
public interface IColumnDescriptor {
	/**
	 * @return the title-name of the column.
	 */
	public String getName();

	/**
	 * @return the tooltip text description of this column.
	 */
	public String getTooltipDescription();

	/**
	 * @return the (relative) width of the column (with respect to other columns in the same table).
	 */
	public int getWeight();

	/**
	 * @return the SWT style of column; e.g. SWT.LEFT, etc.
	 */
	public int getStyle();

	/**
	 * @return true if the column should be set movable on creation.
	 */
	public boolean isMovable();

	/**
	 * @return true if the column should be set for resizable (or resizability?) :P
	 */
	public boolean isResizable();
}
