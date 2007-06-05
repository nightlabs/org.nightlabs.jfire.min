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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * @author Alexander Bieber
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LoginDialog extends Dialog 
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
	
	protected static final int BUTTON_LOGIN = IDialogConstants.CLIENT_ID + 1;
	protected static final int BUTTON_OFFLINE = IDialogConstants.CLIENT_ID + 2;
	protected static final int BUTTON_DETAILS = IDialogConstants.CLIENT_ID + 3;
	
	private Button loginButton;
	private Button offlineButton;
	private Button detailsButton;
	
	private Composite dialogArea;
	private Composite messageArea;
	private GridData messageAreaGridData;
	private Composite mainArea;
	private Composite detailsArea;
	private GridData detailsAreaGridData;

	private Label labelMessageIcon;
	private Label labelMessage;
	
	private Label labelUserID = null;
	private Text textUserID = null;
	private Label labelPassword = null;
	private Text textPassword = null;
	
	private Label labelOrganisationID = null;
	private Text textOrganisationID = null;
	private Label labelServerURL = null;
	private Text textServerURL = null;
	private Button checkBoxSaveSettings = null;
	private Label labelInitialContextFactory = null;
	private Text textInitialContextFactory = null;
	private Label labelWorkstationID = null;
	private Text textWorkstationID = null;
	
	
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
	
	public LoginDialog(Shell parent, Login.AsyncLoginResult loginResult, LoginConfigModule loginModule)
	{
		this(parent);
		loginResult.reset();
		setLoginResult(loginResult);
		setLoginModule(loginModule);
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
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) 
	{
		dialogArea = (Composite)super.createDialogArea(parent); 
		createMessageArea(dialogArea);
		createMainArea(dialogArea);
		createDetailsArea(dialogArea);
		
		initializeWidgetValues();
		setSmartFocus();
		
		return dialogArea;
	}
	
	protected Control createMessageArea(Composite parent)
	{
		messageArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayoutError = new GridLayout();
		gridLayoutError.numColumns = 2;
		messageArea.setLayout(gridLayoutError);
		messageAreaGridData = new GridData(GridData.FILL_HORIZONTAL);
		messageAreaGridData.heightHint = 0;
		messageArea.setLayoutData(messageAreaGridData);
		
		labelMessageIcon = new Label(messageArea,SWT.PUSH);
		
		labelMessage = new Label(messageArea,SWT.PUSH);
		labelMessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return messageArea;
	}
	
	protected Control createMainArea(Composite parent)
	{
		mainArea = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainArea.setLayoutData(gd);
		
		GridLayout gridLayoutStatic = new GridLayout();
		labelUserID = new Label(mainArea, SWT.NONE);
		textUserID = new Text(mainArea, SWT.BORDER);
		labelPassword = new Label(mainArea, SWT.NONE);
		textPassword = new Text(mainArea, SWT.BORDER);
		
		mainArea.setLayout(gridLayoutStatic);
		gridLayoutStatic.numColumns = 2;
		labelUserID.setText(Messages.getString("login.LoginDialog.labeluser")); //$NON-NLS-1$
		textUserID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelPassword.setText(Messages.getString("login.LoginDialog.labelpass")); //$NON-NLS-1$
		textPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textPassword.setEchoChar('*');
		
		return mainArea;
	}
	
	protected Control createDetailsArea(Composite parent)
	{
		detailsArea = new Composite(parent, SWT.NONE);
		
		detailsAreaGridData = new GridData(GridData.FILL_HORIZONTAL);
		detailsArea.setLayoutData(detailsAreaGridData);
				
		GridLayout gridLayoutExpand = new GridLayout();
		labelWorkstationID = new Label(detailsArea, SWT.NONE);
		textWorkstationID = new Text(detailsArea, SWT.BORDER);
		labelOrganisationID = new Label(detailsArea, SWT.NONE);
		textOrganisationID = new Text(detailsArea, SWT.BORDER);
		labelServerURL = new Label(detailsArea, SWT.NONE);
		textServerURL = new Text(detailsArea, SWT.BORDER);
		labelInitialContextFactory = new Label(detailsArea, SWT.NONE);
		textInitialContextFactory = new Text(detailsArea, SWT.BORDER);
		checkBoxSaveSettings = new Button(detailsArea, SWT.CHECK);

		detailsArea.setLayout(gridLayoutExpand);
		gridLayoutExpand.numColumns = 2;
		
		labelOrganisationID.setText(Messages.getString("login.LoginDialog.labelorganisation")); //$NON-NLS-1$
		textOrganisationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
		labelWorkstationID.setText(Messages.getString("login.LoginDialog.labelworkstation")); //$NON-NLS-1$
		textWorkstationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelServerURL.setText(Messages.getString("login.LoginDialog.labelserverURL")); //$NON-NLS-1$
		textServerURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		checkBoxSaveSettings.setText(Messages.getString("login.LoginDialog.labelsaveSettings")); //$NON-NLS-1$
		GridData checkBoxGridData = new GridData(GridData.FILL_HORIZONTAL);
		checkBoxGridData.horizontalSpan = 2;
		checkBoxSaveSettings.setLayoutData(checkBoxGridData);
		
		labelInitialContextFactory.setText(Messages.getString("login.LoginDialog.labelinitialContext")); //$NON-NLS-1$
		textInitialContextFactory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		detailsAreaGridData.heightHint = 0;
		
		return detailsArea;
	}
	
	
	
	
	
	public void setLoginResult(Login.AsyncLoginResult loginResult) 
	{
		this.loginResult = loginResult;
	}	
	
	public Login.AsyncLoginResult getLoginResult() 
	{
		return loginResult;
	}
	
	public void setLoginContext(JFireLoginContext loginContext)
	{
		this.loginContext = loginContext;
	}	
	
	public JFireLoginContext getLoginContext() 
	{
		return loginContext;
	}
	
	public void setLoginModule(LoginConfigModule loginConfigModule) 
	{
		this.runtimeLoginModule = loginConfigModule;
	}
	
	private void initializeWidgetValues()
	{
		textUserID.setText(runtimeLoginModule.getUserID());
		textOrganisationID.setText(runtimeLoginModule.getOrganisationID());
		textServerURL.setText(runtimeLoginModule.getServerURL());
		textInitialContextFactory.setText(runtimeLoginModule.getInitialContextFactory());
		textWorkstationID.setText(runtimeLoginModule.getWorkstationID());
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
		if(EMPTY_STRING.equals(textUserID))
			textUserID.setFocus();
	}
	
	public void setActualValues()
	{
		loginContext.setCredentials(
				textUserID.getText(),
				textOrganisationID.getText(),
				textPassword.getText()
		);
		
		runtimeLoginModule.setUserID(textUserID.getText());
		runtimeLoginModule.setOrganisationID(textOrganisationID.getText());
		runtimeLoginModule.setInitialContextFactory(textInitialContextFactory.getText());
		runtimeLoginModule.setServerURL(textServerURL.getText());
		runtimeLoginModule.setWorkstationID(textWorkstationID.getText());
	}

	
	private void createLoginButton(Composite parent) 
	{
		loginButton = new Button(parent, SWT.PUSH);
		loginButton.setText(Messages.getString("login.LoginDialog.labelbutton.login")); //$NON-NLS-1$
		loginButton.setData(new Integer(BUTTON_LOGIN));
		loginButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				loginPressed(event);
			}
		});
		Shell shell = parent.getShell();
		if (shell != null)
			shell.setDefaultButton(loginButton);
	}		
	
	private void createOfflineButton(Composite parent) 
	{
		offlineButton = new Button(parent, SWT.PUSH);
		offlineButton.setText(Messages.getString("login.LoginDialog.labelbutton.offline")); //$NON-NLS-1$
		offlineButton.setData(new Integer(BUTTON_OFFLINE));
		offlineButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				offlinePressed(event);
			}
		});
	}
	
	private void createDetailsButton(Composite parent) 
	{
		detailsButton = new Button(parent, SWT.PUSH);
		detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		detailsButton.setData(new Integer(BUTTON_DETAILS));
		detailsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				detailsPressed(event);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) 
	{
		GridLayout parentLayout = (GridLayout) parent.getLayout();
		parentLayout.numColumns = 2;
		parentLayout.verticalSpacing = 0;
		parentLayout.marginHeight = 0;
//		parentLayout.marginWidth = 0;
		Composite left = new Composite(parent,SWT.NONE);
		GridData leftData = new GridData(GridData.FILL_HORIZONTAL);
		leftData.grabExcessHorizontalSpace = true;
		left.setLayoutData(leftData);
	  Composite right = new Composite(parent,SWT.NONE);
		GridData rightData = new GridData(GridData.FILL_HORIZONTAL);
		right.setLayoutData(rightData);
		RowLayout layout = new RowLayout();
		right.setLayout(layout);
		createLoginButton(right);
		createOfflineButton(right);
		createDetailsButton(right);
	}

	private void showErrorMessage(String errorMessage)
	{
		showMessage(errorMessage, Dialog.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
	}

	private void showInfoMessage(String errorMessage)
	{
		showMessage(errorMessage, Dialog.getImage(Dialog.DLG_IMG_MESSAGE_INFO));
	}
	
	private void showMessage(String errorMessage, Image icon)
	{
		Point windowSize = getShell().getSize();
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if(errorMessage == null || EMPTY_STRING.equals(errorMessage)) {
			labelMessage.setText(EMPTY_STRING);
			messageAreaGridData.heightHint = 0;
		} else {
			labelMessageIcon.setImage(icon);
			labelMessage.setText(errorMessage);
			messageAreaGridData.heightHint = SWT.DEFAULT;
			labelMessage.redraw();
		}
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
	}
	
	protected void loginPressed(SelectionEvent event) 
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
			showErrorMessage(errorMessage);
			setSmartFocus();
			return;
		}

		boolean hadError = true;
		showInfoMessage(Messages.getString("login.LoginDialog.tryingLogin")); //$NON-NLS-1$
		enableAll(false);
		try {

			// use entries and log in
			setActualValues();		
			if (checkBoxSaveSettings.getSelection()) {
				try {
					BeanUtils.copyProperties(persistentLoginModule,runtimeLoginModule);
					persistentLoginModule.setChanged();
				} catch (Exception e) {
					logger.error(Messages.getString("login.LoginDialog.errorSaveConfig"), e); //$NON-NLS-1$
				}
			}

			Job job = new Job(Messages.getString("login.LoginDialog.authentication")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor arg0)
				{
					Login.AsyncLoginResult testResult = Login.testLogin(loginContext);
					testResult.copyValuesTo(loginResult);

					Display.getDefault().asyncExec(new Runnable() {
						public void run()
						{
							enableAll(true);
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
				enableAll(true);
		}
	}

	private void enableAll(boolean enable)
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
				showErrorMessage(Messages.getString("login.LoginDialog.errorauthenticationFailed")); //$NON-NLS-1$
			}
			else if (loginResult.isWasCommunicationErr()) {
				Throwable error = loginResult.getException();
				while (error.getLocalizedMessage() == null && error.getCause() != null) {
					error = ExceptionUtils.getCause(error);
				}
				showErrorMessage(String.format(Messages.getString("login.LoginDialog.errorcommunicatinError"), error.getLocalizedMessage())); //$NON-NLS-1$
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
				showErrorMessage(message);
				
			}
			// show a message to the user
		}
	}
	
	protected void offlinePressed(SelectionEvent event) 
	{
		loginResult.setSuccess(false);
		loginResult.setWorkOffline(true);
		close();
	}

	protected void detailsPressed(SelectionEvent event) 
	{
		showDetails(detailsAreaGridData.heightHint == 0);
	}
	
	protected void showDetails(boolean visible) 
	{
		Point windowSize = getShell().getSize();
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if(visible) {
			detailsAreaGridData.heightHint = SWT.DEFAULT;
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
		} else {
			detailsAreaGridData.heightHint = 0;
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		}
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
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
