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

package org.nightlabs.jfire.base.login;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.nightlabs.jfire.base.JFireBasePlugin;
/**
 * @author Alexander Bieber
 */
public class LoginDialogExpandableArea extends Composite {

	private Label labelOrganisationID = null;
	private Text textOrganisationID = null;
//	private Label labelSecurityProtocol = null;
//	private Text textSecurityProtocol = null;
	private Label labelServerURL = null;
	private Text textServerURL = null;
	private Button checkBoxSaveSettings = null;
	private Label labelInitialContextFactory = null;
	private Text textInitialContextFactory = null;
	private Label labelWorkstationID = null;
	private Text textWorkstationID = null;

	private LoginConfigModule loginConfigModule = null;
	
	public LoginDialogExpandableArea(
			Composite parent, 
			int style, 
			LoginConfigModule loginConfigModule
	) {
		super(parent, style);
		initialize();
		this.loginConfigModule = loginConfigModule;
		initValues();
	}
	
	private void initialize() {
		GridData thisGD = new GridData();
		thisGD.grabExcessHorizontalSpace = false;
		setLayoutData(thisGD);
				
		GridLayout gridLayoutExpand = new GridLayout();
		labelOrganisationID = new Label(this, SWT.NONE);
		textOrganisationID = new Text(this, SWT.BORDER);
		labelServerURL = new Label(this, SWT.NONE);
		textServerURL = new Text(this, SWT.BORDER);
		labelInitialContextFactory = new Label(this, SWT.NONE);
		textInitialContextFactory = new Text(this, SWT.BORDER);
		labelWorkstationID = new Label(this, SWT.NONE);
		textWorkstationID = new Text(this, SWT.BORDER);
//		labelSecurityProtocol = new Label(this, SWT.NONE);
//		textSecurityProtocol = new Text(this, SWT.BORDER);
		checkBoxSaveSettings = new Button(this, SWT.CHECK);

		this.setLayout(gridLayoutExpand);
		gridLayoutExpand.numColumns = 2;
		labelOrganisationID.setText(JFireBasePlugin.getResourceString("login.label.organisation"));		
		textOrganisationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
//		labelSecurityProtocol.setText(JFireBasePlugin.getResourceString("login.label.securityProtocol"));
//		gridData8.grabExcessHorizontalSpace = true;
//		gridData8.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
//		textSecurityProtocol.setLayoutData(gridData8);
		
		labelServerURL.setText(JFireBasePlugin.getResourceString("login.label.serverURL"));
		textServerURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		checkBoxSaveSettings.setLayoutData(gridData);
		checkBoxSaveSettings.setText(JFireBasePlugin.getResourceString("login.label.saveSettings"));
		
		labelInitialContextFactory.setText(JFireBasePlugin.getResourceString("login.label.initialContext"));
		textInitialContextFactory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelWorkstationID.setText(JFireBasePlugin.getResourceString("login.label.workstation"));
		textWorkstationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		setSize(new org.eclipse.swt.graphics.Point(250,143));
	}
	
	private void initValues(){
		if (loginConfigModule != null){
			getTextOrganisationID().setText(loginConfigModule.getOrganisationID());
			getTextServerURL().setText(loginConfigModule.getServerURL());
			getTextInitialContextFactory().setText(loginConfigModule.getInitialContextFactory());
			getTextWorkstationID().setText(loginConfigModule.getWorkstationID());
//			getTextSecurityProtocol().setText(loginConfigModule.getSecurityProtocol());
		}
	}
		
	public Button getCheckBoxSaveSettings() {
		return checkBoxSaveSettings;
	}
//	public Label getLabelInitialContextFactory() {
//		return labelInitialContextFactory;
//	}
//	public Label getLabelOrganisationID() {
//		return labelOrganisationID;
//	}
//	public Label getLabelSecurityProtocol() {
//		return labelSecurityProtocol;
//	}
//	public Label getLabelServerURL() {
//		return labelServerURL;
//	}
	public Text getTextServerURL() {
		return textServerURL;
	}
	public Text getTextInitialContextFactory() {
		return textInitialContextFactory;
	}
	public Text getTextOrganisationID() {
		return textOrganisationID;
	}
	public Text getTextWorkstationID() {
		return textWorkstationID;
	}
//	public Text getTextSecurityProtocol() {
//		return textSecurityProtocol;
//	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
