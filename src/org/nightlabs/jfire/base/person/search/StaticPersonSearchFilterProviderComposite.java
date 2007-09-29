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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.nightlabs.base.ui.composite.LabeledText;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class StaticPersonSearchFilterProviderComposite extends org.eclipse.swt.widgets.Composite {
	private Composite firstRow;
	private Composite secondRow;
	private Composite thirdRow;
//	private Composite fourthRow;
	
	private LabeledText controlName;
	private LabeledText controlPersonID;
	private LabeledText controlAddress;
	private LabeledText controlCity;
	private LabeledText controlPostCode;
	private LabeledText controlPhone;
	private LabeledText controlEmail;
	private Button searchButton;

	public StaticPersonSearchFilterProviderComposite(Composite parent, int style, boolean createSearchButton) {
		super(parent, style);
		GridLayout thisLayout = new GridLayout();
		thisLayout.verticalSpacing = 0;
		thisLayout.horizontalSpacing = 0;
		thisLayout.marginHeight = 0;
		thisLayout.marginWidth = 0;
		this.setLayout(thisLayout);
		
		firstRow = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);
		GridLayout firstRowLayout = ((GridLayout)firstRow.getLayout());
		firstRowLayout.numColumns = 2;
		firstRowLayout.makeColumnsEqualWidth = true;
		firstRowLayout.horizontalSpacing = 5;
		firstRowLayout.verticalSpacing = 1;
		
		GridData firstRowLData = new GridData();
		firstRowLData.grabExcessHorizontalSpace = true;
		firstRowLData.horizontalAlignment = GridData.FILL;
		firstRow.setLayoutData(firstRowLData);
//		firstRow.setLayout(firstRowLayout);

		controlName = new LabeledText(firstRow, Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProviderComposite.name.caption")); //$NON-NLS-1$
		GridData labeledTextComposite1LData = new GridData();
		labeledTextComposite1LData.horizontalAlignment = GridData.FILL;
		labeledTextComposite1LData.grabExcessHorizontalSpace = true;
		controlName.setLayoutData(labeledTextComposite1LData);

		controlPersonID = new LabeledText(firstRow, Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProviderComposite.personID.caption")); //$NON-NLS-1$
		GridData controlPersonIDLData = new GridData();
		controlPersonIDLData.horizontalAlignment = GridData.FILL;
		controlPersonIDLData.grabExcessHorizontalSpace = true;
		controlPersonID.setLayoutData(controlPersonIDLData);

		//		controlCompany = new LabeledTextComposite(firstRow, "Company", SWT.NONE);
//		GridData controlCompanyLData = new GridData();
//		controlCompanyLData.grabExcessHorizontalSpace = true;
//		controlCompanyLData.horizontalAlignment = GridData.FILL;
//		controlCompany.setLayoutData(controlCompanyLData);

		secondRow = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);
		GridLayout secondRowLayout = ((GridLayout)secondRow.getLayout());;		
		GridData secondRowLData = new GridData();		
		secondRowLData.grabExcessHorizontalSpace = true;
		secondRowLData.horizontalAlignment = GridData.FILL;
		secondRowLayout.numColumns = 3;
		secondRowLayout.horizontalSpacing = 5;
		secondRowLayout.verticalSpacing = 1;
		secondRow.setLayoutData(secondRowLData);
//		secondRow.setLayout(secondRowLayout);

		controlAddress = new LabeledText(secondRow, Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProviderComposite.address.caption")); //$NON-NLS-1$
		GridData controlAddressLData = new GridData();
		controlAddressLData.grabExcessHorizontalSpace = true;
		controlAddressLData.horizontalAlignment = GridData.FILL;
		controlAddress.setLayoutData(controlAddressLData);

		controlPostCode = new LabeledText(secondRow, Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProviderComposite.postcode.caption")); //$NON-NLS-1$
		GridData controlPostCodeLData = new GridData();
		controlPostCodeLData.widthHint = 90;
		controlPostCode.setLayoutData(controlPostCodeLData);

		controlCity = new LabeledText(secondRow, Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProviderComposite.city.caption")); //$NON-NLS-1$
		GridData controlCityLData = new GridData();
		controlCityLData.horizontalAlignment = GridData.FILL;
		controlCityLData.grabExcessHorizontalSpace = true;
		controlCity.setLayoutData(controlCityLData);

		
		thirdRow = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);
		GridLayout thirdRowLayout = ((GridLayout)thirdRow.getLayout());;
		if (createSearchButton)
			thirdRowLayout.numColumns = 3;
		else
			thirdRowLayout.numColumns = 2;
		thirdRowLayout.horizontalSpacing = 5;
		thirdRowLayout.verticalSpacing = 1;
		GridData thirdRowLData = new GridData();
		thirdRowLData.grabExcessHorizontalSpace = true;
		thirdRowLData.horizontalAlignment = GridData.FILL;
		thirdRow.setLayoutData(thirdRowLData);

		controlPhone = new LabeledText(thirdRow, Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProviderComposite.phone.caption")); //$NON-NLS-1$
		GridData controlPhoneLData = new GridData();
		controlPhoneLData.grabExcessHorizontalSpace = true;
		controlPhoneLData.horizontalAlignment = GridData.FILL;
//		controlPhoneLData.widthHint = 160;
		controlPhone.setLayoutData(controlPhoneLData);

		controlEmail = new LabeledText(thirdRow, Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProviderComposite.email.caption")); //$NON-NLS-1$
		GridData controlEmailLData = new GridData();
		controlEmailLData.grabExcessHorizontalSpace = true;
		controlEmailLData.horizontalAlignment = GridData.FILL;
//		controlPhoneLData.widthHint = 160;
		controlEmail.setLayoutData(controlEmailLData);

		if (createSearchButton) {
			searchButton = new Button(thirdRow, SWT.PUSH);
			searchButton.setText(Messages.getString("org.nightlabs.jfire.base.person.search.StaticPersonSearchFilterProviderComposite.searchButton.text")); //$NON-NLS-1$
			GridData searchButtonLData = new GridData(GridData.VERTICAL_ALIGN_END);
			searchButtonLData.grabExcessHorizontalSpace = false;
			searchButton.setLayoutData(searchButtonLData);
		}
		
		this.layout();
			
//		fourthRow = new TightWrapperComposite(this, SWT.NONE,false);
//		GridLayout fourthRowLayout = ((GridLayout)fourthRow.getLayout());;
//		fourthRowLayout.numColumns = 2;
//		fourthRowLayout.horizontalSpacing = 2;
//		fourthRowLayout.verticalSpacing = 1;
//		GridData fourthRowLData = new GridData();
//		fourthRowLData.grabExcessHorizontalSpace = true;
//		fourthRowLData.horizontalAlignment = GridData.FILL;
//		fourthRow.setLayoutData(fourthRowLData);
	}
	
	

	public LabeledText getControlAddress() {
		return controlAddress;
	}
	public LabeledText getControlCity() {
		return controlCity;
	}
	public LabeledText getControlName() {
		return controlName;
	}
	public LabeledText getControlPhone() {
		return controlPhone;
	}
	public LabeledText getControlPostCode() {
		return controlPostCode;
	}
	public LabeledText getControlEmail() {
		return controlEmail;
	}
	public Button getSearchButton() {
		return searchButton;
	}
}
