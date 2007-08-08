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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.SelectableComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jdo.search.SearchFilterProvider;
import org.nightlabs.jdo.search.SearchResultFetcher;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.search.PropSearchFilter;

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
public class PropertySetQuickSearch implements SearchFilterProvider {
	/**
	 * LOG4J logger used by this class
	 */
//	private static final Logger logger = Logger.getLogger(PropertySetQuickSearch.class);
	
	private XComposite wrapperComposite;
//	private Button quickButton;
	private String buttonText;
	private PropSearchFilter personSearchFilter;
	private SearchResultFetcher resultFetcher;
	
	protected PropertyManager propManager;
	
	/**
	 * Construct a PropertySetQuickSearch with buttonText as label for the Button,
	 * @param buttonText
	 */
	public PropertySetQuickSearch(String buttonText) {
		this.buttonText = buttonText;
	}
	
	/**
	 * Construct a PropertySetQuickSearch with button-text and an optional (null possible)
	 * PersonSearchResultFetcher for callback on user interaction.
	 * 
	 * @param buttonText
	 * @param resultFetcher
	 */
	public PropertySetQuickSearch(String buttonText, SearchResultFetcher resultFetcher) {
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
		label.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent arg0) {
			}
			public void mouseDown(MouseEvent arg0) {
			}
			public void mouseUp(MouseEvent arg0) {
				quickSearchButtonPressed(null);
			}
		});
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
	protected PropSearchFilter getSearchFilter(boolean refresh) {
		if (refresh || (personSearchFilter == null)) {
			personSearchFilter = new PropSearchFilter();
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
		
		resultFetcher.searchTriggered(this);
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
		return getSearchFilter(true);
	}
	

}
