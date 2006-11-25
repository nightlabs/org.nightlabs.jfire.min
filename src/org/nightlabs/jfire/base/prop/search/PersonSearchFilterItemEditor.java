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

package org.nightlabs.jfire.base.prop.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jdo.search.SearchFilterItemEditor;
import org.nightlabs.jfire.base.prop.StructLocalDAO;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.TextStructField;

/**
 * Concrete SearchFilterItemEditor that represents a
 * criteria for the search of persons.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonSearchFilterItemEditor extends SearchFilterItemEditor implements SelectionListener{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PersonSearchFilterItemEditor.class);

	private XComposite wrapper;
	private List searchFieldList;
	private Combo comboSearchField;
	
	/**
	 * @see org.nightlabs.jdo.search.SearchFilterItemEditor#getControl(org.eclipse.swt.widgets.Composite, int)
	 */
	public Control getControl(Composite parent) {
		if (wrapper == null) {
			wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
			GridLayout wrapperLayout = (GridLayout)wrapper.getLayout();
			wrapperLayout.numColumns = 2;
			wrapperLayout.makeColumnsEqualWidth = false;
			
			comboSearchField = new Combo(wrapper, SWT.DROP_DOWN | SWT.READ_ONLY);
			GridData gdCombo = new GridData();
			gdCombo.grabExcessHorizontalSpace = false;
			gdCombo.horizontalAlignment = GridData.FILL;
			comboSearchField.setLayoutData(gdCombo);
			
			if (searchFieldList == null) {
				try {
					searchFieldList = buildSearchFieldList();
				}
				catch (Throwable t) {
					searchFieldList = null;
					wrapper.dispose();
					wrapper = null;
					throw new RuntimeException(t);
				}
			}
			  
			for (int i = 0; i<searchFieldList.size()-1; i++) {
				PropSearchFilterItemEditorHelper helper = (PropSearchFilterItemEditorHelper) searchFieldList.get(i);
				comboSearchField.add(helper.getDisplayName());
			}
			comboSearchField.addSelectionListener(this);
			// TODO: temporär -> ExtensionPoint
			PersonSearchFilterItemEditorHelperRegistry.sharedInstance().addItemEditor(TextStructField.class, new TextStructFieldSearchItemEditorHelper());
			comboSearchField.select(0);
			onComboChange();
		}
		return wrapper;
	}
	
	/**
	 * Builds a list of PropSearchFilterItemEditorHelper
	 * that are used to build the contents of the search field combo
	 * and the right part of the editor.
	 * 
	 * @return
	 */
	protected List buildSearchFieldList() {
		List helperList = new ArrayList();
		for (Iterator iter = StructLocalDAO.sharedInstance().getStructLocal(Person.class.getName()).getStructBlocks().iterator(); iter.hasNext();) {
			StructBlock structBlock = (StructBlock) iter.next();
			for (Iterator iterator = structBlock.getStructFields().iterator(); iterator.hasNext();) {
				AbstractStructField structField = (AbstractStructField) iterator.next();
				helperList.add(new PropStructFieldSearchItemEditorManager(structField));
			}
		}
		return helperList;
	}

	/**
	 * Delegates to the current PropSearchFilterItemEditorHelper.
	 * 
	 * @see org.nightlabs.jdo.search.SearchFilterItemEditor#getSearchFilterItem()
	 */
	public SearchFilterItem getSearchFilterItem() {
		return getCurrentHelper().getSearchFilterItem();
	}

	
	private PropSearchFilterItemEditorHelper lastHelper;
	private int lastIdx = -1;
	
	private PropSearchFilterItemEditorHelper getCurrentHelper() {
		int idx = comboSearchField.getSelectionIndex();
		if ((idx < 0) || (idx >= searchFieldList.size()))
			throw new ArrayIndexOutOfBoundsException("Selection index of search field combo is out of range of searchFieldList.S");
		return (PropSearchFilterItemEditorHelper) searchFieldList.get(idx);
	}
	
	private void onComboChange() {
		int idx = comboSearchField.getSelectionIndex();
		if (idx == lastIdx)
			return;
		if (idx < 0)
			return;			
		PropSearchFilterItemEditorHelper helper = getCurrentHelper();
		if (lastHelper != null) {
			lastHelper.close();
			try {
				lastHelper.getControl(null).dispose();
			} catch (Throwable t) {
				logger.error("Error disposing helper control.",t);
			}				
		}
		helper.getControl(wrapper);
		wrapper.layout();
		lastIdx = idx;
		lastHelper = helper;
	}
	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent evt) {
		if (evt.getSource().equals(comboSearchField)) {
			onComboChange();
		}
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	/**
	 * @see org.nightlabs.jdo.search.SearchFilterItemEditor#close()
	 */
	public void close() {
		comboSearchField.removeSelectionListener(this);
	}

}
