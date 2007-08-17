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

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerHome;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.search.PropSearchFilter;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropertySetSearchDialog extends Dialog implements SearchResultFetcher{

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PropertySetSearchDialog.class);

	/**
	 * @param parentShell
	 */
	public PropertySetSearchDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE);
	}


	private DynamicPropertySetSearchFilterProvider filterProvider;
	private PropertySetSearchResultTable resultTable;

	protected Control createDialogArea(Composite parent) {

		XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		filterProvider = new DynamicPropertySetSearchFilterProvider(new PropertySetSearchFilterItemListMutator());
		filterProvider.setSearchResultFetcher(this);
		Composite providerComp = filterProvider.createComposite(wrapper);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 550;
		gd.heightHint = 200;
		providerComp.setLayoutData(gd);

		resultTable = new PropertySetSearchResultTable(wrapper,SWT.NONE	);		
		GridData tgd = new GridData(GridData.FILL_BOTH);
		tgd.heightHint = 300;
		resultTable.setLayoutData(tgd);

		return wrapper;
	}

	/**
	 * @see org.nightlabs.jdo.ui.search.SearchResultFetcher#searchTriggered(org.nightlabs.jdo.ui.search.SearchFilterProvider, org.nightlabs.j2ee.InitialContextProvider)
	 */
	public void searchTriggered(SearchFilterProvider filterProvider) {
		logger.debug("Search triggered, getting PersonManager");
		PropertyManagerHome home = null;
		PropertyManager personManager = null;
		try {
			// TODO: find reason of IncompatibleClassChangeError when accessing login
//			home = PersonManagerUtil.getHome(login.getInitialContextProperties());
			home = PropertyManagerUtil.getHome(Login.getLogin().getInitialContextProperties());
			personManager = home.create();
		} catch (Exception e) {
			logger.error("Error creating PersonManagerUtil.",e);
			throw new RuntimeException(e);
		}
		logger.debug("Have PersonManager searching");

		PropSearchFilter searchFilter = (PropSearchFilter)filterProvider.getSearchFilter();
		searchFilter.clearResultFields();
		searchFilter.addResultStructFieldID(TextDataField.class, PersonStruct.PERSONALDATA_COMPANY);
		searchFilter.addResultStructFieldID(TextDataField.class, PersonStruct.PERSONALDATA_NAME);
		searchFilter.addResultStructFieldID(TextDataField.class, PersonStruct.PERSONALDATA_FIRSTNAME);
//		searchFilter.addResultStructFieldID(TextDataField.class, PersonStruct.PHONE_LOCALNUMBER);

		try {
			long start = System.currentTimeMillis();
			Collection persons = personManager.searchProperty(searchFilter, new String[] {FetchPlan.ALL}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			logger.debug("Person search for "+persons.size()+" entries took "+(System.currentTimeMillis()-start)/1000+" s.");
			start = System.currentTimeMillis();
			resultTable.setInput(persons);
			logger.debug("Setting results to table for "+persons.size()+" entries took "+(System.currentTimeMillis()-start)/1000+" s.");
		} catch (Exception e) {
			logger.error("Error searching person.",e);
			throw new RuntimeException(e);
		}

	}
}
