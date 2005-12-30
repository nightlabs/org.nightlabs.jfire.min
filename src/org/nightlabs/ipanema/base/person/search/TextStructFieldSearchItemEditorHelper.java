/*
 * Created 	on Dec 19, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.nightlabs.ipanema.person.AbstractPersonStructField;
import org.nightlabs.ipanema.person.id.PersonStructFieldID;
import org.nightlabs.ipanema.person.util.TextPersonSearchFilterItem;
import org.nightlabs.jdo.JdoPlugin;
import org.nightlabs.jdo.search.SearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextStructFieldSearchItemEditorHelper 
		extends PersonStructFieldSearchItemEditorHelper {

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
			AbstractPersonStructField personStructField) {
		super(personStructField);
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
	 * @see org.nightlabs.ipanema.base.person.search.PersonSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
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
	 * @see org.nightlabs.ipanema.base.person.search.PersonSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	public SearchFilterItem getSearchFilterItem() {
		PersonStructFieldID id = PersonStructFieldID.create(
			personStructField.getPersonStructBlockOrganisationID(),
			personStructField.getPersonStructBlockID(),
			personStructField.getPersonStructFieldOrganisationID(),
			personStructField.getPersonStructFieldID()
		);
		int matchType = matchTypeOrder[comboMatchType.getSelectionIndex()].matchType;
		String needle = textNeedle.getText();
		TextPersonSearchFilterItem result = new TextPersonSearchFilterItem(
			id,
			matchType,
			needle
		);
		return result;
	}

	/**
	 * @see org.nightlabs.ipanema.base.person.search.PersonSearchFilterItemEditorHelper#close()
	 */
	public void close() {
	}

}
