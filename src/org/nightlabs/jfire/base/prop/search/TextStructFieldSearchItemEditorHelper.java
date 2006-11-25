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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.jdo.JdoPlugin;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.search.TextPropSearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextStructFieldSearchItemEditorHelper 
		extends PropStructFieldSearchItemEditorHelper {

	private Composite helperComposite;
	private Combo comboMatchType;
	private Text textNeedle;

	/**
	 * 
	 */
	public TextStructFieldSearchItemEditorHelper() {
		super();
	}

	/**
	 * @param personStructField
	 */
	public TextStructFieldSearchItemEditorHelper(
			AbstractStructField structField) {
		super(structField);
	}

	protected class MatchTypeOrderEntry {
		int matchType;
		String displayName;
		public MatchTypeOrderEntry(int matchType, String displayName) {
			this.matchType = matchType;
			this.displayName = displayName;
		}
	}
	private MatchTypeOrderEntry[] matchTypeOrder = new MatchTypeOrderEntry[6];
	private MatchTypeOrderEntry setMatchTypeOrderEntry(int idx, int matchType, String displayName) {
		MatchTypeOrderEntry result = new MatchTypeOrderEntry(matchType,displayName);
		matchTypeOrder[idx] = result;
		return result;
	}
	
	/**
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control getControl(Composite parent) {
		if (helperComposite == null) {
			helperComposite = new Composite(parent,SWT.NONE);
			GridLayout wrapperLayout = new GridLayout();
			wrapperLayout.numColumns = 2;
			wrapperLayout.makeColumnsEqualWidth = true;
			helperComposite.setLayout(wrapperLayout);
			helperComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			comboMatchType = new Combo(helperComposite,SWT.READ_ONLY);
			comboMatchType.add(setMatchTypeOrderEntry(0,SearchFilterItem.MATCHTYPE_CONTAINS,JdoPlugin.getResourceString("search.matchType"+SearchFilterItem.MATCHTYPE_CONTAINS)).displayName);
			comboMatchType.add(setMatchTypeOrderEntry(1,SearchFilterItem.MATCHTYPE_NOTCONTAINS,JdoPlugin.getResourceString("search.matchType"+SearchFilterItem.MATCHTYPE_NOTCONTAINS)).displayName);
			comboMatchType.add(setMatchTypeOrderEntry(2,SearchFilterItem.MATCHTYPE_BEGINSWITH,JdoPlugin.getResourceString("search.matchType"+SearchFilterItem.MATCHTYPE_BEGINSWITH)).displayName);
			comboMatchType.add(setMatchTypeOrderEntry(3,SearchFilterItem.MATCHTYPE_ENDSWITH,JdoPlugin.getResourceString("search.matchType"+SearchFilterItem.MATCHTYPE_ENDSWITH)).displayName);
			comboMatchType.add(setMatchTypeOrderEntry(4,SearchFilterItem.MATCHTYPE_EQUALS,JdoPlugin.getResourceString("search.matchType"+SearchFilterItem.MATCHTYPE_EQUALS)).displayName);
			comboMatchType.add(setMatchTypeOrderEntry(5,SearchFilterItem.MATCHTYPE_NOTEQUALS,JdoPlugin.getResourceString("search.matchType"+SearchFilterItem.MATCHTYPE_NOTEQUALS)).displayName);
			
			GridData gdCombo = new GridData();
			gdCombo.grabExcessHorizontalSpace = true;
			gdCombo.horizontalAlignment = GridData.FILL;
			comboMatchType.setLayoutData(gdCombo);
			comboMatchType.select(SearchFilterItem.MATCHTYPE_DEFAULT-1);
			
			textNeedle = new Text(helperComposite,SWT.BORDER);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			textNeedle.setLayoutData(gd);
		}
			
		return helperComposite;
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	public SearchFilterItem getSearchFilterItem() {
		StructFieldID id = StructFieldID.create(
			personStructField.getStructBlockOrganisationID(),
			personStructField.getStructBlockID(),
			personStructField.getStructFieldOrganisationID(),
			personStructField.getStructFieldID()
		);
		int matchType = matchTypeOrder[comboMatchType.getSelectionIndex()].matchType;
		String needle = textNeedle.getText();
		TextPropSearchFilterItem result = new TextPropSearchFilterItem(
			id,
			matchType,
			needle
		);
		return result;
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#close()
	 */
	public void close() {
	}

}
