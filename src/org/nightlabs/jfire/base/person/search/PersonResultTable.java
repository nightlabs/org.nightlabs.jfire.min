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

package org.nightlabs.jfire.base.person.search;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.layout.WeightedTableLayout;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.base.table.TableContentProvider;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonResultTable extends AbstractTableComposite<Person> {
	
	private class LabelProvider implements ITableLabelProvider {

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			Person person = getPersonFromTableElement(element);
			if (person != null) {
				if (!person.isExploded()) {
					StructLocalDAO.sharedInstance().getStructLocal(Person.class, StructLocal.DEFAULT_SCOPE, new NullProgressMonitor()).explodePropertySet(person);
				}
				try {
					switch (columnIndex) {
						case 0: return ((TextDataField)person.getDataField(PersonStruct.PERSONALDATA_COMPANY)).getText();
						case 1: return ((TextDataField)person.getDataField(PersonStruct.PERSONALDATA_NAME)).getText();
						case 2: return ((TextDataField)person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME)).getText();
						case 3: return ((TextDataField)person.getDataField(PersonStruct.POSTADDRESS_ADDRESS)).getText();
						case 4: return ((TextDataField)person.getDataField(PersonStruct.POSTADDRESS_CITY)).getText();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				if (element instanceof String && columnIndex == 0) {
					return String.valueOf(element);
				}
			}
			return ""; //$NON-NLS-1$
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

	}	

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 */
	public PersonResultTable(Composite parent, int style) {
		super(parent, style, true);
	}

	/**
	 * @see org.nightlabs.base.table.AbstractTableComposite#createTableColumns(TableViewer, org.eclipse.swt.widgets.Table)
	 */
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		new TableColumn(table, SWT.LEFT).setText("Company");
		new TableColumn(table, SWT.LEFT).setText("Name");
		new TableColumn(table, SWT.LEFT).setText("First name");
		new TableColumn(table, SWT.LEFT).setText("Address");
		new TableColumn(table, SWT.LEFT).setText("City");
		table.setLayout(new WeightedTableLayout(new int[] {1,1,1,1,1}));
	}

	/**
	 * @see org.nightlabs.base.table.AbstractTableComposite#setTableProvider(org.eclipse.jface.viewers.TableViewer)
	 */
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new LabelProvider());
	}
	
	protected Person getPersonFromTableElement(Object element) {
		if (element instanceof Person)
			return (Person) element;
		return null;
	}
	
}
