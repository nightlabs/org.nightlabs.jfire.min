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

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.nightlabs.base.composite.SelectableComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.person.PersonManager;
import org.nightlabs.jfire.person.util.PersonSearchFilter;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jdo.search.SearchFilterProvider;
import org.nightlabs.jdo.search.SearchResultFetcher;

/**
 * Basic SearchFilterProvider for persons that provides one Button
 * in its Composite and takes a SearchResultFetcher as parameter
 * for the constructor. This SearchResultFetcher will be
 * called when the user hits the button.
 * {@link #getSearchFilter()} will return an instance of
 * {@link org.nightlabs.jfire.base.person.util.PersonSearchFilter}
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonQuickSearch implements SearchFilterProvider {
	private static final Logger LOGGER = Logger.getLogger(PersonQuickSearch.class);

	
	private XComposite wrapperComposite;
	private Button quickButton;
	private String buttonText;
	private PersonSearchFilter personSearchFilter;
	private SearchResultFetcher resultFetcher;
	
	protected PersonManager personManager;
	
	/**
	 * Construct a PersonQuickSearch with buttonText as label for the Button,
	 * @param buttonText
	 */
	public PersonQuickSearch(String buttonText) {
		this.buttonText = buttonText;
	}
	
	/**
	 * Construct a PersonQuickSearch with button-text and an optional (null possible)
	 * PersonSearchResultFetcher for callback on user interaction.
	 * 
	 * @param buttonText
	 * @param resultFetcher
	 */
	public PersonQuickSearch(String buttonText, SearchResultFetcher resultFetcher) {
		this.buttonText = buttonText;
		this.resultFetcher = resultFetcher;
	}

	/**
	 * Default implementation creates a wrapper Composite with a Button.
	 * 
	 * @see org.nightlabs.jdo.search.SearchFilterProvider#createComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createComposite(Composite parent) {
		wrapperComposite = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
//			quickButton = new Button(wrapperComposite,SWT.TOGGLE);
//			quickButton.setText(buttonText);
//			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
//			gd.heightHint = 25;
//			quickButton.setLayoutData(gd);
//			
//			quickButton.addSelectionListener(
//				new SelectionListener() {
//					public void widgetSelected(SelectionEvent evt) {
//						quickSearchButtonPressed(evt);
//					}
//					public void widgetDefaultSelected(SelectionEvent evt) {
//						quickSearchButtonPressed(evt);
//					}
//				}
//			);
		SelectableComposite comp = new SelectableComposite(wrapperComposite,SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		Label label = new Label(comp,SWT.NONE);
		label.setText(buttonText);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 15;
		comp.setLayoutData(gd);
		return wrapperComposite;
	}
	
	public Composite getComposite() {
		return wrapperComposite;
	}

	/**
	 * Returns and optionally recreates a the PersonSearchFilter.
	 * 
	 * @param refresh
	 * @return
	 */
	protected PersonSearchFilter getPersonSearchFilter(boolean refresh) {
		if (refresh || (personSearchFilter == null)) {
			personSearchFilter = new PersonSearchFilter();
		}
		return personSearchFilter;
	}
	
	/**
	 * Calls the PersonSearchResultFetcher.
	 * 
	 * @param evt
	 */
	protected void quickSearchButtonPressed(SelectionEvent evt) {
		if (resultFetcher == null)
			return;
		
		Login login;
		try {
			login = Login.getLogin();
		} catch (LoginException e) {
			LOGGER.error("Could not log in, fetcher will not be triggered!",e);
			return;
		}
		
		resultFetcher.searchTriggered(this,login);
	}
	
	/**
	 * Set the buttonText. Has no affect to the Button
	 * when {@link #getComposite(Composite)} was called before.
	 * 
	 * @param txt
	 */
	public void setButtonText(String txt) {
		this.buttonText = txt;
	}

	/**
	 * Return the buttonText.
	 * 
	 * @return
	 */
	public String getButtonText() {
		return buttonText;
	}

	/**
	 * Default implementation will always return a fresh and empty
	 * PersonSearchFilter.
	 *  
	 * @see org.nightlabs.jdo.search.SearchFilterProvider#getSearchFilter()
	 */
	public SearchFilter getSearchFilter() {
		return getPersonSearchFilter(true);
	}
	

}
