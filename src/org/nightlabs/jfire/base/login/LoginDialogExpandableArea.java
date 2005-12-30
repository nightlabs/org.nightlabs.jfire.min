/*
 * Created 	on Oct 5, 2004
 * 					by Alexander Bieber
 *
 */
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
		
		GridData gridData11 = new GridData();
		GridData gridData10 = new GridData();
		GridData gridData9 = new GridData();
//		GridData gridData8 = new GridData();
		GridData gridData7 = new GridData();
		GridLayout gridLayoutExpand = new GridLayout();
		labelOrganisationID = new Label(this, SWT.NONE);
		textOrganisationID = new Text(this, SWT.BORDER);
		labelServerURL = new Label(this, SWT.NONE);
		textServerURL = new Text(this, SWT.BORDER);
		labelInitialContextFactory = new Label(this, SWT.NONE);
		textInitialContextFactory = new Text(this, SWT.BORDER);
//		labelSecurityProtocol = new Label(this, SWT.NONE);
//		textSecurityProtocol = new Text(this, SWT.BORDER);
		checkBoxSaveSettings = new Button(this, SWT.CHECK);

		this.setLayout(gridLayoutExpand);
		gridLayoutExpand.numColumns = 2;
		labelOrganisationID.setText(JFireBasePlugin.getResourceString("login.label.organisation"));
		gridData7.grabExcessHorizontalSpace = true;
		gridData7.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		textOrganisationID.setLayoutData(gridData7);
//		labelSecurityProtocol.setText(JFireBasePlugin.getResourceString("login.label.securityProtocol"));
//		gridData8.grabExcessHorizontalSpace = true;
//		gridData8.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
//		textSecurityProtocol.setLayoutData(gridData8);
		labelServerURL.setText(JFireBasePlugin.getResourceString("login.label.serverURL"));
		gridData9.grabExcessHorizontalSpace = true;
		gridData9.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		textServerURL.setLayoutData(gridData9);
//		gridData10.horizontalSpan = 2;
		gridData10.grabExcessHorizontalSpace = false;
		gridData10.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		checkBoxSaveSettings.setLayoutData(gridData10);
		checkBoxSaveSettings.setText(JFireBasePlugin.getResourceString("login.label.saveSettings"));
		labelInitialContextFactory.setText(JFireBasePlugin.getResourceString("login.label.initialContext"));
		gridData11.grabExcessHorizontalSpace = true;
		gridData11.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		textInitialContextFactory.setLayoutData(gridData11);
//		setSize(new org.eclipse.swt.graphics.Point(250,143));
	}
	
	private void initValues(){
		if (loginConfigModule != null){
			getTextOrganisationID().setText(loginConfigModule.getOrganisationID());
			getTextServerURL().setText(loginConfigModule.getServerURL());
			getTextInitialContextFactory().setText(loginConfigModule.getInitialContextFactory());
//			getTextSecurityProtocol().setText(loginConfigModule.getSecurityProtocol());
		}
	}
	
	
	public Button getCheckBoxSaveSettings() {
		return checkBoxSaveSettings;
	}
	public Label getLabelInitialContextFactory() {
		return labelInitialContextFactory;
	}
	public Label getLabelOrganisationID() {
		return labelOrganisationID;
	}
//	public Label getLabelSecurityProtocol() {
//		return labelSecurityProtocol;
//	}
	public Label getLabelServerURL() {
		return labelServerURL;
	}
	public Text getTextServerURL() {
		return textServerURL;
	}
	public Text getTextInitialContextFactory() {
		return textInitialContextFactory;
	}
	public Text getTextOrganisationID() {
		return textOrganisationID;
	}
//	public Text getTextSecurityProtocol() {
//		return textSecurityProtocol;
//	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
