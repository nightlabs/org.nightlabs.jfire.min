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
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jdo.search.SearchFilterProvider;
import org.nightlabs.jdo.search.SearchResultFetcher;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.search.TextPropSearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class StaticPersonSearchFilterProvider implements
		SearchFilterProvider {

	private StaticPersonSearchFilterProviderComposite criteriaBuilderComposite;
	private boolean createOwnSearchButton;
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
	}
	
	/**
	 * @see org.nightlabs.jdo.search.SearchFilterProvider#getComposite(org.eclipse.swt.widgets.Composite)
	 */	
	public Composite createComposite(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		
		criteriaBuilderComposite = new StaticPersonSearchFilterProviderComposite(wrapper, SWT.NONE, !createOwnSearchButton);
		if (createOwnSearchButton) {
			searchButton = new Button(criteriaBuilderComposite, SWT.PUSH);
			searchButton.setText("&Search");
			searchButton.addSelectionListener(searchListener);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.END;
			gd.widthHint = 80;
			searchButton.setLayoutData(gd);
		}
		else
			criteriaBuilderComposite.getSearchButton().addSelectionListener(searchListener);
			
		criteriaBuilderComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return wrapper;
	}
	
	public Composite getComposite() {
		return wrapper;
	}

	/**
	 * @see org.nightlabs.jdo.search.SearchFilterProvider#getPersonSearchFilter()
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
		String[] toks = needle.split("[:;,. ]+");
		Collection<String> result = new ArrayList<String>(toks.length);
		for (int i = 0; i < toks.length; i++) {
			result.add(toks[i]);
		}
		return result;
	}
	
	public static ParsedNameCriteria parseNameNeedle(String needle) {
//		String text = criteriaBuilderComposite.getControlName().getTextControl().getText();
		// sTok will return Delims
		ParsedNameCriteria result = new ParsedNameCriteria();
		String[] toks = needle.split("[:;,. ]+");
		result.completeString = needle;
		for (int i = 0; i < toks.length; i++) {
			try {
				long tmpLong = Long.parseLong(toks[i]);
				result.personID = tmpLong;
				result.completeString.replace(toks[i], "");
			} catch (NumberFormatException e) {}
		} 
		switch (toks.length) {
			case 3:
				result.company = toks[0];
				result.name = toks[1];
				result.firstName = toks[2];
				break;
			case 2: 
				result.company = "";
				result.name = toks[0];
				result.firstName = toks[1];
				break;
			case 1:
				if (needle.indexOf(":") > 0 || needle.indexOf(";") > 0) {
					result.company = toks[0];
					result.name = "";
				}
				else {
					result.company = "";
					result.name = toks[0];
				}
				result.firstName = "";
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
	
	protected PropSearchFilter createPersonSearchFilter() {
		return new PropSearchFilter();
	}
	
	public void setQuickSearchText(ParsedNameCriteria nameCriteria) {
		criteriaBuilderComposite.getControlName().getTextControl().setText(nameCriteria.completeString);
		// TODO: add personID criteria
	}
	
	protected PropSearchFilter buildPersonSearchFilter() {
		// new filter
		PropSearchFilter filter = createPersonSearchFilter();
		
		filter.setConjunction(SearchFilter.CONJUNCTION_AND);
		
//		ParsedNameCriteria nameCriteria = parseNameNeedle(criteriaBuilderComposite.getControlName().getTextControl().getText());
		
		Collection<String> needles = parseNameNeedles(criteriaBuilderComposite.getControlName().getTextControl().getText());
		
		
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
//		if (!criteriaBuilderComposite.getControlName().getTextControl().getText().equals("")) {
////			TextPersonSearchFilterItem nameSearchFilterItem = new TextPersonSearchFilterItem();
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(PersonStruct.PERSONALDATA_NAME,SearchFilterItem.MATCHTYPE_CONTAINS,criteriaBuilderComposite.getControlName().getTextControl().getText()));
//		}
//		if (!criteriaBuilderComposite.getControlCompany().getTextControl().getText().equals(""))
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(PersonStruct.PERSONALDATA_COMPANY,SearchFilterItem.MATCHTYPE_CONTAINS,criteriaBuilderComposite.getControlCompany().getTextControl().getText()));
		if (!criteriaBuilderComposite.getControlAddress().getTextControl().getText().equals(""))
			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.POSTADDRESS_ADDRESS,SearchFilterItem.MATCHTYPE_CONTAINS,criteriaBuilderComposite.getControlAddress().getTextControl().getText()));
		if (!criteriaBuilderComposite.getControlCity().getTextControl().getText().equals(""))
			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.POSTADDRESS_CITY,SearchFilterItem.MATCHTYPE_CONTAINS,criteriaBuilderComposite.getControlCity().getTextControl().getText()));
		if (!criteriaBuilderComposite.getControlPostCode().getTextControl().getText().equals(""))
			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.POSTADDRESS_POSTCODE,SearchFilterItem.MATCHTYPE_CONTAINS,criteriaBuilderComposite.getControlPostCode().getTextControl().getText()));
//		if (!criteriaBuilderComposite.getControlPhonePre().getTextControl().getText().equals(""))
//			filter.addSearchFilterItem(new TextPersonSearchFilterItem(PersonStruct.PHONE_AREACODE,SearchFilterItem.MATCHTYPE_CONTAINS,criteriaBuilderComposite.getControlPhonePre().getTextControl().getText()));
//		if (!criteriaBuilderComposite.getControlPhone().getTextControl().getText().equals(""))
//			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.PHONE_LOCALNUMBER,SearchFilterItem.MATCHTYPE_CONTAINS,criteriaBuilderComposite.getControlPhone().getTextControl().getText()));
		if (!criteriaBuilderComposite.getControlEmail().getTextControl().getText().equals(""))
			filter.addSearchFilterItem(new TextPropSearchFilterItem(PersonStruct.INTERNET_EMAIL,SearchFilterItem.MATCHTYPE_CONTAINS,criteriaBuilderComposite.getControlEmail().getTextControl().getText()));
		
		return filter;
	}
	
}
