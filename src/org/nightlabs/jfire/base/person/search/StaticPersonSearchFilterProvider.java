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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jdo.ui.search.EarlySearchFilterProvider;
import org.nightlabs.jdo.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.search.TextPropSearchFilterItem;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class StaticPersonSearchFilterProvider implements
		EarlySearchFilterProvider {

	private StaticPersonSearchFilterProviderComposite searchFilterProviderComposite;
	private boolean createOwnSearchButton;
	private boolean createFilterProviderCompositeSearchButton;
	private XComposite wrapper;
	private Button searchButton;
	private SearchResultFetcher resultFetcher;
	
	private SelectionListener searchListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			if (resultFetcher != null) {
				resultFetcher.searchTriggered(StaticPersonSearchFilterProvider.this);
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};
	
	/**
	 * Create a new static person SearchFilterProvider.
	 * 
	 * @param resultFetcher A ResultFetcher to be triggered on search.
	 * @param createOwnSearchButton Whether to create an own search button, or to use the default one of {@link StaticPersonSearchFilterProviderComposite}.
	 */
	public StaticPersonSearchFilterProvider(SearchResultFetcher resultFetcher, boolean createOwnSearchButton) {
		this.resultFetcher = resultFetcher;
		this.createOwnSearchButton = createOwnSearchButton;
		this.createFilterProviderCompositeSearchButton = false;
	}
	
	/**
	 * Create a new static person SearchFilterProvider.
	 * 
	 * @param resultFetcher A ResultFetcher to be triggered on search.
	 * @param createOwnSearchButton Whether to create an own search button, or to use the default one of {@link StaticPersonSearchFilterProviderComposite}.
	 * @param createFilterProviderCompositeSearchButton Whether to create the search button in the filter provider composite.
	 */
	public StaticPersonSearchFilterProvider(SearchResultFetcher resultFetcher, boolean createOwnSearchButton, boolean createFilterProviderCompositeSearchButton) {
		this.resultFetcher = resultFetcher;
		this.createOwnSearchButton = createOwnSearchButton;
		this.createFilterProviderCompositeSearchButton = createFilterProviderCompositeSearchButton;
	}
	
	/**
	 * @see org.nightlabs.jdo.ui.search.SearchFilterProvider#getComposite(org.eclipse.swt.widgets.Composite)
	 */	
	public Composite createComposite(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		
		searchFilterProviderComposite = new StaticPersonSearchFilterProviderComposite(wrapper, SWT.NONE, createFilterProviderCompositeSearchButton);
		if (createOwnSearchButton) {
			searchButton = new Button(searchFilterProviderComposite, SWT.PUSH);
			searchButton.setText(Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProvider.searchButton.text")); //$NON-NLS-1$
			searchButton.addSelectionListener(searchListener);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.END;
			gd.widthHint = 80;
			searchButton.setLayoutData(gd);
		}
		if (createFilterProviderCompositeSearchButton) {
			searchFilterProviderComposite.getSearchButton().addSelectionListener(searchListener);
		}
			
		searchFilterProviderComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return wrapper;
	}
	
	public Composite getComposite() {
		return wrapper;
	}

	/**
	 * @see org.nightlabs.jdo.ui.search.SearchFilterProvider#getPersonSearchFilter()
	 */
	public SearchFilter getSearchFilter() {
		return buildPersonSearchFilter();
	}
	
	public static class ParsedNameCriteria {
		public String company;
		public String name;
		public String firstName;
		public long personID = -1;
		public String completeString;
	}
	
	public static Collection<String> parseNameNeedles(String needle) {
		String[] toks = needle.split("[:;,. ]+"); //$NON-NLS-1$
		Collection<String> result = new ArrayList<String>(toks.length);
		for (int i = 0; i < toks.length; i++) {
			result.add(toks[i]);
		}
		return result;
	}
	
	public static ParsedNameCriteria parseNameNeedle(String needle) {
//		String text = searchFilterProviderComposite.getControlName().getTextControl().getText();
		// sTok will return Delims
		ParsedNameCriteria result = new ParsedNameCriteria();
		String[] toks = needle.split("[:;,. ]+"); //$NON-NLS-1$
		result.completeString = needle;
		for (int i = 0; i < toks.length; i++) {
			try {
				long tmpLong = Long.parseLong(toks[i]);
				result.personID = tmpLong;
				result.completeString.replace(toks[i], ""); //$NON-NLS-1$
			} catch (NumberFormatException e) {}
		} 
		switch (toks.length) {
			case 3:
				result.company = toks[0];
				result.name = toks[1];
				result.firstName = toks[2];
				break;
			case 2: 
				result.company = ""; //$NON-NLS-1$
				result.name = toks[0];
				result.firstName = toks[1];
				break;
			case 1:
				if (needle.indexOf(":") > 0 || needle.indexOf(";") > 0) { //$NON-NLS-1$ //$NON-NLS-2$
					result.company = toks[0];
					result.name = ""; //$NON-NLS-1$
				}
				else {
					result.company = ""; //$NON-NLS-1$
					result.name = toks[0];
				}
				result.firstName = ""; //$NON-NLS-1$
				break;
			default:
				if (toks.length != 0) {
					// TODO: think about this
					result.company = toks[0];
					result.name = toks[1];
					result.firstName = toks[toks.length-1];
				}
				break;
		}
		return result;
	}
	
	
	protected PropSearchFilter createSearchFilter() {
		return new PropSearchFilter();
	}
	
	public void setQuickSearchText(ParsedNameCriteria nameCriteria) {
		searchFilterProviderComposite.getControlName().getTextControl().setText(nameCriteria.completeString);
		// TODO: add personID criteria
	}
	
	protected PropSearchFilter buildPersonSearchFilter() {
		// new filter
		PropSearchFilter filter = createSearchFilter();
		
		filter.setConjunction(SearchFilter.CONJUNCTION_AND);
		
//		ParsedNameCriteria nameCriteria = parseNameNeedle(searchFilterProviderComposite.getControlName().getTextControl().getText());
		
		Collection<String> needles = parseNameNeedles(searchFilterProviderComposite.getControlName().getTextControl().getText());
		
		
		StructFieldID[] nameCriteriaFieldIDs = new StructFieldID[] {
			PersonStruct.PERSONALDATA_COMPANY,
			PersonStruct.PERSONALDATA_NAME,
			PersonStruct.PERSONALDATA_FIRSTNAME
		};
	
		for (String needle : needles) {
			filter.addSearchFilterItem(new TextPropSearchFilterItem(nameCriteriaFieldIDs, SearchFilterItem.MATCHTYPE_CONTAINS, needle));
		}
		
//		if (nameCriteria.company != null || !"".equals(nameCriteria.company))
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(nameCriteriaFieldIDs, SearchFilterItem.MATCHTYPE_CONTAINS, nameCriteria.company));
//		if (nameCriteria.name != null || !"".equals(nameCriteria.name))
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(nameCriteriaFieldIDs, SearchFilterItem.MATCHTYPE_CONTAINS, nameCriteria.name));
//		if (nameCriteria.firstName != null || !"".equals(nameCriteria.firstName))
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(nameCriteriaFieldIDs, SearchFilterItem.MATCHTYPE_CONTAINS, nameCriteria.firstName));
		
		// add items if neccessary
//		if (!searchFilterProviderComposite.getControlName().getTextControl().getText().equals("")) {
////			TextPersonSearchFilterItem nameSearchFilterItem = new TextPersonSearchFilterItem();
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(PersonStruct.PERSONALDATA_NAME,SearchFilterItem.MATCHTYPE_CONTAINS,searchFilterProviderComposite.getControlName().getTextControl().getText()));
//		}
//		if (!searchFilterProviderComposite.getControlCompany().getTextControl().getText().equals(""))
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(PersonStruct.PERSONALDATA_COMPANY,SearchFilterItem.MATCHTYPE_CONTAINS,searchFilterProviderComposite.getControlCompany().getTextControl().getText()));
		if (!searchFilterProviderComposite.getControlAddress().getTextControl().getText().equals("")) //$NON-NLS-1$
			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.POSTADDRESS_ADDRESS,SearchFilterItem.MATCHTYPE_CONTAINS,searchFilterProviderComposite.getControlAddress().getTextControl().getText()));
		if (!searchFilterProviderComposite.getControlCity().getTextControl().getText().equals("")) //$NON-NLS-1$
			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.POSTADDRESS_CITY,SearchFilterItem.MATCHTYPE_CONTAINS,searchFilterProviderComposite.getControlCity().getTextControl().getText()));
		if (!searchFilterProviderComposite.getControlPostCode().getTextControl().getText().equals("")) //$NON-NLS-1$
			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.POSTADDRESS_POSTCODE,SearchFilterItem.MATCHTYPE_CONTAINS,searchFilterProviderComposite.getControlPostCode().getTextControl().getText()));
//		if (!searchFilterProviderComposite.getControlPhonePre().getTextControl().getText().equals(""))
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(PersonStruct.PHONE_AREACODE,SearchFilterItem.MATCHTYPE_CONTAINS,searchFilterProviderComposite.getControlPhonePre().getTextControl().getText()));
//		if (!searchFilterProviderComposite.getControlPhone().getTextControl().getText().equals(""))
//			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.PHONE_LOCALNUMBER,SearchFilterItem.MATCHTYPE_CONTAINS,searchFilterProviderComposite.getControlPhone().getTextControl().getText()));
		if (!searchFilterProviderComposite.getControlEmail().getTextControl().getText().equals("")) //$NON-NLS-1$
			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.INTERNET_EMAIL,SearchFilterItem.MATCHTYPE_CONTAINS,searchFilterProviderComposite.getControlEmail().getTextControl().getText()));
		
		return filter;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEarlySearchText(String earlySearchText) {
		searchFilterProviderComposite.getControlName().getTextControl().setText(earlySearchText);
	}
	
}
