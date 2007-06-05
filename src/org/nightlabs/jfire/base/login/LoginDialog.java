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
	
	protected static LoginDialog sharedInstance = null;
	
	private LoginConfigModule persistentLoginModule = null;
	private LoginConfigModule runtimeLoginModule = null;
	private Login.AsyncLoginResult loginResult = null;	
	private JFireLoginContext loginContext = null;
	
	protected static final int BUTTON_LOGIN = 100;
	protected static final int BUTTON_OFFLINE = 200;
	protected static final int BUTTON_DETAILS = 300;
	
	private Button loginButton;
	private Button offlineButton;
	private Button detailsButton;
	
	
	
	private Composite dialogArea;
	private Composite errorArea;
	private GridData errorAreaGridData;
	private Composite mainArea;
	private Composite detailsArea;
	private GridData detailsAreaGridData;

	private Label labelErrorIcon;
	private Label labelErrorMessage;
	
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
//		GridData gd = new GridData(GridData.FILL_BOTH);
//		gd.heightHint = 150;
//		gd.widthHint = 550;
//		dialogArea.setLayoutData(gd);
		createErrorArea(dialogArea);
		createMainArea(dialogArea);
		createDetailsArea(dialogArea);
		
		initializeWidgetValues();
		setSmartFocus();
		
		return dialogArea;
	}
	
	protected Control createErrorArea(Composite parent)
	{
		errorArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayoutError = new GridLayout();
		gridLayoutError.numColumns = 2;
		errorArea.setLayout(gridLayoutError);
		errorAreaGridData = new GridData(GridData.FILL_HORIZONTAL);
		errorAreaGridData.heightHint = 0;
		errorArea.setLayoutData(errorAreaGridData);
		
		labelErrorIcon = new Label(errorArea,SWT.PUSH);
		labelErrorIcon.setImage(Dialog.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
		
		labelErrorMessage = new Label(errorArea,SWT.PUSH);
		labelErrorMessage.setText(""); //$NON-NLS-1$
		labelErrorMessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return errorArea;
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
		if("".equals(textWorkstationID.getText())) { //$NON-NLS-1$
			showDetails(true);
			textWorkstationID.setFocus();
		}
		else if("".equals(textOrganisationID.getText())) { //$NON-NLS-1$
			showDetails(true);
			textOrganisationID.setFocus();
		}
		else if("".equals(textServerURL.getText())) { //$NON-NLS-1$
			showDetails(true);
			textServerURL.setFocus();
		}
		else if("".equals(textInitialContextFactory.getText())) { //$NON-NLS-1$
			showDetails(true);
			textInitialContextFactory.setFocus();
		}
		if("".equals(textUserID)) //$NON-NLS-1$
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
	
	private void setErrorMessage(String errorMessage)
	{
		Point windowSize = getShell().getSize();
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if(errorMessage == null || "".equals(errorMessage)) { //$NON-NLS-1$
			labelErrorMessage.setText(""); //$NON-NLS-1$
			errorAreaGridData.heightHint = 0;
		} else {
			labelErrorMessage.setText(errorMessage);
			errorAreaGridData.heightHint = SWT.DEFAULT;
			labelErrorMessage.redraw();
		}
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
	}
	
	protected void loginPressed(SelectionEvent event) 
	{
		// check entries
		String errorMessage = null;
		if (textUserID.getText().equals("")) //$NON-NLS-1$
			errorMessage = Messages.getString("login.LoginDialog.errormissingUserID"); //$NON-NLS-1$
		else if (textWorkstationID.getText().equals("")) //$NON-NLS-1$
			errorMessage = Messages.getString("login.LoginDialog.errormissingWorkstationID"); //$NON-NLS-1$
		else if (textOrganisationID.getText().equals("")) //$NON-NLS-1$
			errorMessage = Messages.getString("login.LoginDialog.errormissingOrganisationID"); //$NON-NLS-1$
		else if (textInitialContextFactory.getText().equals("")) //$NON-NLS-1$
			errorMessage = Messages.getString("login.LoginDialog.errormissingContextFactory"); //$NON-NLS-1$
		else if (textServerURL.getText().equals("")) //$NON-NLS-1$
			errorMessage = Messages.getString("login.LoginDialog.errormissingServerURL"); //$NON-NLS-1$
		if(errorMessage != null) {
			setErrorMessage(errorMessage);
			setSmartFocus();
			return;
		}

		boolean hadError = true;
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

			Job job = new Job("Authentication...") {
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

		if ((!loginResult.isWasAuthenticationErr()) && (loginResult.isSuccess()))
			close();
		else {
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
					message += String.format("\n%1$s: %2$s", loginResult.getException().getClass().getName(), loginResult.getException().getLocalizedMessage());
					//message += "\n"+loginResult.getException().getClass().getName()+": "+loginResult.getException().getLocalizedMessage();
					Throwable cause = loginResult.getException();
					while ( cause != null ) {
						message += "\n"+cause.getClass().getName()+": "+cause.getLocalizedMessage();
						cause = cause.getCause();
					}
					loginResult.getException().printStackTrace();
				}
				setErrorMessage(message);
				
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
