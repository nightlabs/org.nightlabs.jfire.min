/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.prop;

import java.util.Locale;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Table Composite that displays a configurable set of {@link StructField}s
 * of a list of {@link PropertySet}s.
 * <p>
 * The {@link StructFieldID}s to display have to be passed to the constructor
 * of the table.
 * </p>
 * <p>
 * To use this as a table for 
 * </p>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PropertySetTable<ProperySetType> extends AbstractTableComposite<ProperySetType> {
	
	private class LabelProvider extends TableLabelProvider {
		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			PropertySet propertySet = getPropertySetFromElement(element);
			if (propertySet != null) {
				return getStructFieldText(propertySet, columnIndex);
			} else {
				if (element instanceof String && columnIndex == 0) {
					return String.valueOf(element);
				}
			}
			return ""; //$NON-NLS-1$
		}
	}	

	private IStruct struct;
	private StructFieldID[] structFieldIDs;
	private StructField[] structFields;
	
	
	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 */
	public PropertySetTable(Composite parent, int style, IStruct struct, StructFieldID[] structFieldIDs) {
		super(parent, style, false);
		this.struct = struct;
		this.structFieldIDs = structFieldIDs;
		initTable();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates the field columns {@link #createStructFieldColumns(TableViewer, Table)}
	 * and applies a table layout {@link #applyTableLayout(Table)}.
	 * </p>
	 */
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		createStructFieldColumns(tableViewer, table);
		applyTableLayout(table);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Set a default {@link TableContentProvider} and 
	 * the internal {@link LabelProvider}.
	 * </p>
	 */
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new LabelProvider());
	}
	
	/**
	 * Creates the StructFieldColumns columns for this table.
	 * This method is called by {@link #createTableColumns(TableViewer, Table)}
	 * if not overridden or can be used in custom implementations.
	 *  
	 * @param tableViewer The {@link TableViewer} of this table.
	 * @param table The {@link Table} of this table.
	 */
	protected void createStructFieldColumns(TableViewer tableViewer, Table table) {
		structFields = new StructField[structFieldIDs.length];
		for (int i = 0; i < structFieldIDs.length; i++) {
			StructField structField = null;
			try {
				structField = struct.getStructField(structFieldIDs[i]);
			} catch (PropertyException e) {
				throw new RuntimeException(e);
			}
			structFields[i] = structField;
		}
		for (StructField structField : structFields) {
			new TableColumn(table, SWT.LEFT).setText(structField.getName().getText());
		}
	}
	
	/**
	 * Applies a weighted table layout with the same weight for
	 * all present Columns in this table.
	 * This method is called by {@link #createTableColumns(TableViewer, Table)}
	 * if not overridden or might be used in custom implementations.
	 * 
	 * @param table The {@link Table} of this table.
	 */
	protected void applyTableLayout(Table table) {
		int[] weights = new int[table.getColumnCount()];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 1;
		}		
		table.setLayout(new WeightedTableLayout(weights));
		// TODO: Better use SWT TableLayout ?
	}

	
	/**
	 * This method may be overridden to extract the {@link PropertySet}
	 * from a single table element (element set as input).
	 * <p>
	 * The method should return <code>null</code> if it can not extract
	 * a PropertySet from the given element.
	 * </p>
	 * <p>
	 * The default implementation returns the element if it is an 
	 * instance of {@link PropertySet}. 
	 * </p>
	 * 
	 * @param element The element to extract the {@link PropertySet} from.
	 * @return The {@link PropertySet} extracted from the given Element.
	 */
	protected PropertySet getPropertySetFromElement(Object element) {
		if (element instanceof PropertySet)
			return (PropertySet) element;
		return null;
	}
	
	/**
	 * Used by the {@link LabelProvider} of this table to display the
	 * value of a StructField an may be used in custom implementations.
	 * 
	 * @param propertySet The {@link PropertySet} to get the field value from. 
	 * @param structFieldIdx The index of the {@link StructFieldID} to get. (Array passed in the constructor).
	 * @return The String representation of the {@link StructField} value for the given {@link PropertySet}.
	 */
	protected String getStructFieldText(PropertySet propertySet, int structFieldIdx) {
		if (structFieldIdx >= 0 && structFieldIdx < structFieldIDs.length) {
			AbstractDataField dataField = propertySet.getPersistentDataField(structFieldIDs[structFieldIdx], 0);
			if (dataField != null && dataField instanceof II18nTextDataField) {
				return ((II18nTextDataField) dataField).getText(Locale.getDefault());
			} else
				return ""; //$NON-NLS-1$
		} else
			return ""; //$NON-NLS-1$
	}
}
