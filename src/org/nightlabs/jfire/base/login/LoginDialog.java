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

import java.util.LinkedList;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.XComboComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * @author Alexander Bieber
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class LoginDialog extends TitleAreaDialog 
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(LoginDialog.class);
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	protected static LoginDialog sharedInstance = null;
	
	private LoginConfigModule persistentLoginModule = null;
	private LoginConfigModule runtimeLoginModule = null;
	private Login.AsyncLoginResult loginResult = null;	
	private JFireLoginContext loginContext = null;
	
	protected static final int DETAILS_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int DELETE_BUTTON_ID = IDialogConstants.CLIENT_ID+2;

	/**
	 * Used to set the details area visible or invisible 
	 * by setting heightHint to 0 or SWT.DEFAULT.
	 */
	private GridData detailsAreaGridData = null;

	private XComboComposite<LoginConfiguration> recentLoginConfigs;
	
	
	private Text textUserID = null;
	private Text textPassword = null;
	private Text textOrganisationID = null;
	private Text textServerURL = null;
	private Button checkBoxSaveSettings = null;
	private Text textInitialContextFactory = null;
	private Text textWorkstationID = null;
	private Text textIdentityName = null;
	private Button deleteButton = null;
	
	private Group loginInfoGroup = null;
	
	private boolean contentCreated = false;
	
	/**
	 * This is only used to be able to initially show details
	 */
	private boolean initiallyShowDetails = false;
	
	
	private boolean manuallyUpdating = false;
	private ModifyListener loginDataModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (!manuallyUpdating) {
				recentLoginConfigs.setSelection(-1);
				deleteButton.setEnabled(false);
			}
		}
	};
		
	/**
	 * Create a new LoginDialog.
	 * @param parent The dialogs parent
	 */
	public LoginDialog(Shell parent) 
	{
		super(parent);
		setShellStyle(getShellStyle()|SWT.RESIZE);
		try {
			persistentLoginModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @deprecated What is this constructor used for?
	 */
	public LoginDialog(Shell parent, Login.AsyncLoginResult loginResult, LoginConfigModule loginModule)
	{
		this(parent);
		loginResult.reset();
		this.loginResult = loginResult;
		this.runtimeLoginModule = loginModule;
//		setLoginResult(loginResult);
//		setLoginModule(loginModule);
	}

	public LoginDialog(Shell parent, Login.AsyncLoginResult loginResult, LoginConfigModule loginModule, JFireLoginContext loginContext)
	{
		this(parent);
		this.loginResult = loginResult;
		this.runtimeLoginModule = loginModule;
		this.loginContext = loginContext;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) 
	{
		super.configureShell(shell);
		shell.setText(Messages.getString("login.LoginDialog.labellogin")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		contentCreated = false;
		Control control = super.createContents(parent);
		contentCreated = true;
		initializeWidgetValues();
		setSmartFocus();
		showDetails(initiallyShowDetails);
		return control;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) 
	{
		Composite area = (Composite)super.createDialogArea(parent); 

		createMainArea(area);
		createDetailsArea(area);
		
		setTitle(Messages.getString("login.LoginDialog.titleAreaTitle")); //$NON-NLS-1$
		// TODO: information icon only because of redraw bug:
		setInfoMessage(Messages.getString("login.LoginDialog.titleAreaMessage")); //$NON-NLS-1$
		
		return dialogArea;
	}
	
	protected Control createMainArea(Composite parent)
	{
		Composite mainArea = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainArea.setLayoutData(gd);
		
		GridLayout gridLayoutStatic = new GridLayout();
		
		LabelProvider loginConfigLabelProv = new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LoginConfiguration) {
					LoginConfiguration loginConfig = (LoginConfiguration) element;
					if (loginConfig == runtimeLoginModule.getLatestLoginConfiguration()) {
						return Messages.getString("login.LoginDialog.currentIdentityMarker"); //$NON-NLS-1$
					}
					return loginConfig.toString();
				} else
					return ""; //$NON-NLS-1$
			}
		};
		
		Label labelRecentLoginConfigs = new Label(mainArea, SWT.NONE);
		labelRecentLoginConfigs.setText(Messages.getString("login.LoginDialog.recentLoginsComboLabel")); //$NON-NLS-1$
		recentLoginConfigs = new XComboComposite<LoginConfiguration>(mainArea, SWT.READ_ONLY);
		recentLoginConfigs.setLabelProvider(loginConfigLabelProv);		
		recentLoginConfigs.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateUIWithLoginConfiguration(recentLoginConfigs.getSelectedElement());
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		new Label(mainArea, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(gd);
		
		Label labelUserID = new Label(mainArea, SWT.NONE);
		textUserID = new Text(mainArea, SWT.BORDER);
		Label labelPassword = new Label(mainArea, SWT.NONE);
		textPassword = new Text(mainArea, SWT.BORDER);
		
		mainArea.setLayout(gridLayoutStatic);
		gridLayoutStatic.numColumns = 2;
		labelUserID.setText(Messages.getString("login.LoginDialog.labeluser")); //$NON-NLS-1$
		textUserID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelPassword.setText(Messages.getString("login.LoginDialog.labelpass")); //$NON-NLS-1$
		textPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textPassword.setEchoChar('*');
		
		textUserID.addModifyListener(loginDataModifyListener);
		
		return mainArea;
	}
	
	private void updateUIWithLoginConfiguration(LoginConfiguration loginConfiguration) {
		manuallyUpdating = true;
		textUserID.setText(loginConfiguration.getUserID());
		textOrganisationID.setText(loginConfiguration.getOrganisationID());
		textServerURL.setText(loginConfiguration.getServerURL());
		textInitialContextFactory.setText(loginConfiguration.getInitialContextFactory());
		textWorkstationID.setText(loginConfiguration.getWorkstationID());
		textPassword.setText("");
		if (runtimeLoginModule.getLatestLoginConfiguration() != loginConfiguration) {
			textIdentityName.setText(loginConfiguration.getName());
			deleteButton.setEnabled(true);
		}	else {
			textIdentityName.setText(""); //$NON-NLS-1$
			deleteButton.setEnabled(false);
		}
		
		manuallyUpdating = false;
	}
	
	protected Control createDetailsArea(Composite parent)
	{
		Composite detailsArea = new Composite(parent, SWT.NONE);
		
		detailsAreaGridData = new GridData(GridData.FILL_HORIZONTAL);
		detailsArea.setLayoutData(detailsAreaGridData);
				
		GridLayout gridLayoutExpand = new GridLayout();
		Label labelWorkstationID = new Label(detailsArea, SWT.NONE);
		textWorkstationID = new Text(detailsArea, SWT.BORDER);
		Label labelOrganisationID = new Label(detailsArea, SWT.NONE);
		textOrganisationID = new Text(detailsArea, SWT.BORDER);
		Label labelServerURL = new Label(detailsArea, SWT.NONE);
		textServerURL = new Text(detailsArea, SWT.BORDER);
		Label labelInitialContextFactory = new Label(detailsArea, SWT.NONE);
		textInitialContextFactory = new Text(detailsArea, SWT.BORDER);

		detailsArea.setLayout(gridLayoutExpand);
		gridLayoutExpand.numColumns = 2;
		
		labelOrganisationID.setText(Messages.getString("login.LoginDialog.labelorganisation")); //$NON-NLS-1$
		textOrganisationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
		labelWorkstationID.setText(Messages.getString("login.LoginDialog.labelworkstation")); //$NON-NLS-1$
		textWorkstationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelServerURL.setText(Messages.getString("login.LoginDialog.labelserverURL")); //$NON-NLS-1$
		textServerURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelInitialContextFactory.setText(Messages.getString("login.LoginDialog.labelinitialContext")); //$NON-NLS-1$
		textInitialContextFactory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		GridData spanHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		spanHorizontalGD.horizontalSpan = 2;

		new Label(detailsArea, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(spanHorizontalGD);
//		checkBoxSaveSettings.setLayoutData(spanHorizontalGD);
		
		checkBoxSaveSettings = new Button(detailsArea, SWT.CHECK);
		checkBoxSaveSettings.setSelection(false);
		checkBoxSaveSettings.setText(Messages.getString("login.LoginDialog.labelsaveSettings")); //$NON-NLS-1$
		checkBoxSaveSettings.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				textIdentityName.setEnabled(checkBoxSaveSettings.getSelection());
				checkIdentityName();
			}
		});
		
		XComposite wrapper = new XComposite(detailsArea, SWT.READ_ONLY, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		new Label(wrapper, SWT.NONE).setText(Messages.getString("login.LoginDialog.identityNameTextLabel")); //$NON-NLS-1$
		textIdentityName = new Text(wrapper, SWT.BORDER);
		textIdentityName.setEnabled(false);
		textIdentityName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textIdentityName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkIdentityName();
			}
		});
		
		textWorkstationID.addModifyListener(loginDataModifyListener);
		textOrganisationID.addModifyListener(loginDataModifyListener);
		textServerURL.addModifyListener(loginDataModifyListener);
		textInitialContextFactory.addModifyListener(loginDataModifyListener);
						
		detailsAreaGridData.heightHint = 0;
		
		return detailsArea;
	}
	
	private void checkIdentityName() {
		String name = textIdentityName.getText();
		if (checkBoxSaveSettings.getSelection() && (name == null || "".equals(name))) { //$NON-NLS-1$
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			setErrorMessage(Messages.getString("login.LoginDialog.validNameMissingErrorMessage")); //$NON-NLS-1$
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			if (checkBoxSaveSettings.getSelection() && runtimeLoginModule.hasConfigWithName(name))
				setWarningMessage(Messages.getString("login.LoginDialog.loginConfigurationAlreadyExists")); //$NON-NLS-1$
			else {
				setWarningMessage(null);
				setErrorMessage(null);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) 
	{
		deleteButton = createButton(parent, DELETE_BUTTON_ID, Messages.getString("login.LoginDialog.deleteButtonLabel"), false); //$NON-NLS-1$
		deleteButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				LoginConfiguration toBeDeleted = recentLoginConfigs.getSelectedElement();
				if (toBeDeleted != null) {
					runtimeLoginModule.deleteSavedLoginConfiguration(toBeDeleted);
					try {
						BeanUtils.copyProperties(persistentLoginModule, runtimeLoginModule);
					} catch (Exception e1) {
						e1.printStackTrace();
						throw new RuntimeException(e1);
					}
					persistentLoginModule.setChanged();
					recentLoginConfigs.removeAllSelected();
					recentLoginConfigs.setSelection(-1);
					textIdentityName.setText(""); //$NON-NLS-1$
				}
			}
		});
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("login.LoginDialog.labelbutton.login"), true); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("login.LoginDialog.labelbutton.offline"), false); //$NON-NLS-1$
		createButton(parent, DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);		
	}
	
	private void initializeWidgetValues()	{
		LinkedList<LoginConfiguration> loginConfigurations = new LinkedList<LoginConfiguration>(runtimeLoginModule.getSavedLoginConfigurations());		
		LoginConfiguration latestLoginConfiguration = runtimeLoginModule.getLatestLoginConfiguration();
		
		if (latestLoginConfiguration != null)
			loginConfigurations.addFirst(latestLoginConfiguration);
		
		recentLoginConfigs.setInput(loginConfigurations);
		
		if (latestLoginConfiguration != null) {
			recentLoginConfigs.setSelection(latestLoginConfiguration);
			updateUIWithLoginConfiguration(latestLoginConfiguration);
		} else {
			LoginConfiguration loginConfiguration = new LoginConfiguration();
			loginConfiguration.init();
			updateUIWithLoginConfiguration(loginConfiguration);
		}
	}
	
	
	private void setSmartFocus()
	{
		textPassword.setFocus();
		if(EMPTY_STRING.equals(textWorkstationID.getText())) {
			showDetails(true);
			textWorkstationID.setFocus();
		}
		else if(EMPTY_STRING.equals(textOrganisationID.getText())) {
			showDetails(true);
			textOrganisationID.setFocus();
		}
		else if(EMPTY_STRING.equals(textServerURL.getText())) {
			showDetails(true);
			textServerURL.setFocus();
		}
		else if(EMPTY_STRING.equals(textInitialContextFactory.getText())) {
			showDetails(true);
			textInitialContextFactory.setFocus();
		}
		if(EMPTY_STRING.equals(textUserID.getText()))
			textUserID.setFocus();
	}
	
	public void storeUserInput()
	{
		loginContext.setCredentials(
				textUserID.getText(),
				textOrganisationID.getText(),
				textPassword.getText()
		);
		
		runtimeLoginModule.setLatestLoginConfiguration(textUserID.getText(), textWorkstationID.getText(), textOrganisationID.getText(),
				textServerURL.getText(), textInitialContextFactory.getText(), null, textIdentityName.getText());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId)
	{
		super.buttonPressed(buttonId);
		if(buttonId == DETAILS_ID)
			detailsPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() 
	{
		if(!checkUserInput())
			return;
		checkLogin();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() 
	{
		loginResult.setSuccess(false);
		loginResult.setWorkOffline(true);
		close();
	}

	/**
	 * Called when the "Details..." button was pressed.
	 */
	protected void detailsPressed() 
	{
		showDetails(detailsAreaGridData.heightHint == 0);
	}
	
	private boolean checkUserInput()
	{
		// check entries
		String errorMessage = null;
		if (textUserID.getText().equals(EMPTY_STRING))
			errorMessage = Messages.getString("login.LoginDialog.errormissingUserID"); //$NON-NLS-1$
		else if (textWorkstationID.getText().equals(EMPTY_STRING))
			errorMessage = Messages.getString("login.LoginDialog.errormissingWorkstationID"); //$NON-NLS-1$
		else if (textOrganisationID.getText().equals(EMPTY_STRING))
			errorMessage = Messages.getString("login.LoginDialog.errormissingOrganisationID"); //$NON-NLS-1$
		else if (textInitialContextFactory.getText().equals(EMPTY_STRING))
			errorMessage = Messages.getString("login.LoginDialog.errormissingContextFactory"); //$NON-NLS-1$
		else if (textServerURL.getText().equals(EMPTY_STRING))
			errorMessage = Messages.getString("login.LoginDialog.errormissingServerURL"); //$NON-NLS-1$
		if(errorMessage != null) {
			setErrorMessage(errorMessage);
			setSmartFocus();
			return false;
		}
		return true;
	}

	private void checkLogin()
	{
		boolean hadError = true;
		setInfoMessage(Messages.getString("login.LoginDialog.tryingLogin")); //$NON-NLS-1$
		enableDualogUI(false);
		try {

			// use entries and log in
			storeUserInput();
			final boolean saveSettings = checkBoxSaveSettings.getSelection();

			Job job = new Job(Messages.getString("login.LoginDialog.authentication")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor arg0)
				{
					Login.AsyncLoginResult testResult = Login.testLogin(loginContext);
					testResult.copyValuesTo(loginResult);
					
					try {
						if (testResult.isSuccess()) {
							runtimeLoginModule.makeLatestFirst();
							
							if (saveSettings)
								runtimeLoginModule.saveLatestConfiguration();
						}

						BeanUtils.copyProperties(persistentLoginModule, runtimeLoginModule);
						persistentLoginModule.setChanged();
					} catch (Exception e) {
						logger.error(Messages.getString("login.LoginDialog.errorSaveConfig"), e); //$NON-NLS-1$
					}

					Display.getDefault().asyncExec(new Runnable() {
						public void run()
						{
							enableDualogUI(true);
							updateUIAfterLogin();
						}
					});

					return Status.OK_STATUS;
				}
			};
			job.schedule();

			hadError = false;
		} finally {
			if (hadError)
				enableDualogUI(true);
		}
	}
	
	/**
	 * Helper methods that make the use of JFace message methods consistent. 
	 */
	private void setWarningMessage(String message) {
		setMessage(message, IMessageProvider.WARNING);
	}	
	private void setInfoMessage(String message) {
		setMessage(message, IMessageProvider.INFORMATION);
	}
		
	/**
	 * Enable or disable all dialog UI elements.
	 * @param enable <code>true</code> To enable all elements -
	 * 		<code>false</code> otherwise.
	 */
	private void enableDualogUI(boolean enable)
	{
		getShell().setEnabled(enable);
	}

	private void updateUIAfterLogin()
	{
		// verify login done 
		if ((!loginResult.isWasAuthenticationErr()) && (loginResult.isSuccess())) {
			close();
		} else {
			// login failed
			if (loginResult.isWasAuthenticationErr()) {
				setErrorMessage(Messages.getString("login.LoginDialog.errorauthenticationFailed")); //$NON-NLS-1$
			}
			else if (loginResult.isWasCommunicationErr()) {
				Throwable error = loginResult.getException();
				while (error.getLocalizedMessage() == null && error.getCause() != null) {
					error = ExceptionUtils.getCause(error);
				}
				setErrorMessage(String.format(Messages.getString("login.LoginDialog.errorcommunicatinError"), error.getLocalizedMessage())); //$NON-NLS-1$
			}
			else {
				String message = loginResult.getMessage();
				if (loginResult.getException() != null) {
					message += String.format(Messages.getString("login.LoginDialog.errorAppend"), loginResult.getException().getClass().getName(), loginResult.getException().getLocalizedMessage()); //$NON-NLS-1$
					Throwable cause = loginResult.getException();
					while ( cause != null ) {
						message += String.format(Messages.getString("login.LoginDialog.errorAppend"), cause.getClass().getName(), cause.getLocalizedMessage()); //$NON-NLS-1$
						cause = cause.getCause();
					}
					loginResult.getException().printStackTrace();
				}
				setErrorMessage(message);
				
			}
			// show a message to the user
		}
	}
	
	/**
	 * Show or hide the details area.
	 * @param visible <code>true</code> if the details should be shown
	 * 		<code>false</code> otherwise.
	 */
	protected void showDetails(boolean visible) 
	{
		System.out.println("show details"); //$NON-NLS-1$
		if(!contentCreated) {
			System.out.println("show details: content is not yet created"); //$NON-NLS-1$
			initiallyShowDetails = true;
			return;
		}
		System.out.println("show details: content is already created"); //$NON-NLS-1$
		Point windowSize = getShell().getSize();
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Button detailsButton = getButton(DETAILS_ID);
		if(visible) {
			detailsAreaGridData.heightHint = SWT.DEFAULT;
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
		} else {
			detailsAreaGridData.heightHint = 0;
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		}
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if(getShell().isVisible())
			getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
	}
	
	public static void registerSharedInstance(LoginDialog dialog) 
	{
		sharedInstance = dialog;
	}
	
	public static void deregisterSharedInstance() 
	{
		sharedInstance = null;
	}

	public static LoginDialog getSharedInstace() 
	{
		return sharedInstance;
	}
}
