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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.nightlabs.base.dialog.ExpandableAreaDialog;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.JFireBasePlugin;

/**
 * @author Alexander Bieber
 */
public class LoginDialog extends ExpandableAreaDialog {
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(LoginDialog.class);
	
	private LoginConfigModule persistentLoginModule = null;
	private LoginConfigModule runtimeLoginModule = null;
	/**
	 * @param parent
	 */
	public LoginDialog(Shell parent) {
		super(
			parent, 
			JFireBasePlugin.getResourceString("login.label.login"), 
			JFireBasePlugin.getResourceString("login.label.details")
		);
		try {
			persistentLoginModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
//		setMaxWidth(350);
	}
	
	public LoginDialog(Shell parent, Login.AsyncLoginResult loginResult, LoginConfigModule loginModule) {
		this(parent);
		loginResult.reset();
		setLoginResult(loginResult);
		setLoginModule(loginModule);
	}

	
	protected Composite createStaticArea(Composite parent) 
	{
		return new LoginDialogStaticArea(parent,SWT.NONE, runtimeLoginModule);		
	}

	protected Composite createExpandableArea(Composite 
	 parent) 
	{
		return new LoginDialogExpandableArea(parent,SWT.NONE,runtimeLoginModule);		
	}
	
	
	public LoginDialogStaticArea getLoginStaticArea(){
		return (LoginDialogStaticArea)super.getStaticArea();
	}
	
	public LoginDialogExpandableArea getLoginDetailArea(){
		return (LoginDialogExpandableArea)super.getExpandableArea();
	}
	
	private Login.AsyncLoginResult loginResult = null;	
	public void setLoginResult(Login.AsyncLoginResult loginResult) {
		this.loginResult = loginResult;
	}	
	public Login.AsyncLoginResult getLoginResult() {
		return loginResult;
	}
	
	private JFireLoginContext loginContext = null;
	public void setLoginContext(JFireLoginContext loginContext){
		this.loginContext = loginContext;
	}	
	public JFireLoginContext getLoginContext() {
		return loginContext;
	}
	
	public void setLoginModule(LoginConfigModule loginConfigModule) {
		this.runtimeLoginModule = loginConfigModule;
	}
	public int open(){
//		this.getShell().addControlListener()
		return super.open();
	}
		
	public void setActualValues(){
		loginContext.setCredentials(
				getLoginStaticArea().getTextUserID().getText(),
				getLoginDetailArea().getTextOrganisationID().getText(),
				getLoginStaticArea().getTextPassword().getText()
		);
		
		runtimeLoginModule.setUserID(getLoginStaticArea().getTextUserID().getText());
		runtimeLoginModule.setOrganisationID(getLoginDetailArea().getTextOrganisationID().getText());
		runtimeLoginModule.setInitialContextFactory(getLoginDetailArea().getTextInitialContextFactory().getText());
		runtimeLoginModule.setServerURL(getLoginDetailArea().getTextServerURL().getText());
//		runtimeLoginModule.setSecurityProtocol(getLoginDetailArea().getTextSecurityProtocol().getText());
	}

	
	
	
	protected static final int BUTTON_LOGIN = 100;
	protected static final int BUTTON_OFFLINE = 200;
	protected static final int BUTTON_QUIT = 300;
	
	private Button loginButton;
	private Button offlineButton;
	private Button quitButton;
	
	
	private void createLoginButton(Composite parent) {
		loginButton = new Button(parent, SWT.PUSH);
//		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
//		loginButton.setLayoutData(gridData);
		loginButton.setText(JFireBasePlugin.getResourceString("login.label.button.login"));
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
	
	private void createOfflineButton(Composite parent) {
		offlineButton = new Button(parent, SWT.PUSH);
//		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
//		offlineButton.setLayoutData(gridData);
		offlineButton.setText(JFireBasePlugin.getResourceString("login.label.button.offline"));
		offlineButton.setData(new Integer(BUTTON_OFFLINE));
		offlineButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				offlinePressed(event);
			}
		});
	}
	
	private void createQuitButton(Composite parent) {
		quitButton = new Button(parent, SWT.PUSH);
//		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
//		quitButton.setLayoutData(gridData);
		quitButton.setText(JFireBasePlugin.getResourceString("login.label.button.quit"));
		quitButton.setData(new Integer(BUTTON_QUIT));
		quitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				quitPressed(event);
			}
		});
	}
	
	

//	protected Control createButtonBar(Composite parent) {
//		Composite composite = new Composite(parent, SWT.NONE);
//		// create a layout with spacing and margins appropriate for the font
//		// size.
//		RowLayout layout = new RowLayout();
//		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
//		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
//		composite.setLayout(layout);
//		
//		// Add the buttons to the button bar.
//		createButtonsForButtonBar(composite);
//		return composite;
//	}
	
	
	protected void createButtonsForButtonBar(Composite parent) {
		GridLayout parentLayout = (GridLayout) parent.getLayout();
		parentLayout.numColumns = 2;
		parentLayout.verticalSpacing = 0;
		parentLayout.marginHeight = 0;
//		parentLayout.marginWidth = 0;
		Composite left = new Composite(parent,SWT.NONE);
		GridData leftData = new GridData(GridData.FILL_HORIZONTAL);
		leftData.grabExcessHorizontalSpace  = true;
		left.setLayoutData(leftData);
	  Composite right = new Composite(parent,SWT.NONE);
		GridData rightData = new GridData(GridData.FILL_HORIZONTAL);
		right.setLayoutData(rightData);
		RowLayout layout = new RowLayout();
		right.setLayout(layout);
		createLoginButton(right);
		createOfflineButton(right);
//		createQuitButton(right);
	}
	
	protected void loginPressed(SelectionEvent event) {
		// check entries
		if (getLoginStaticArea().getTextUserID().getText().equals("")) {
			getLoginStaticArea().setErrMessage(JFireBasePlugin.getResourceString("login.error.missingUserID"));
			return;
		}
		
		if (getLoginDetailArea().getTextOrganisationID().getText().equals("")) {
			getLoginStaticArea().setErrMessage(JFireBasePlugin.getResourceString("login.error.missingOrganisationID"));
			return;
		}
		if (getLoginDetailArea().getTextInitialContextFactory().getText().equals("")) {
			getLoginStaticArea().setErrMessage(JFireBasePlugin.getResourceString("login.error.missingContextFactory"));
			return;
		}
		if (getLoginDetailArea().getTextServerURL().getText().equals("")) {
			getLoginStaticArea().setErrMessage(JFireBasePlugin.getResourceString("login.error.missingServerURL"));
			return;
		}
//		if (getLoginDetailArea().getTextSecurityProtocol().getText().equals("")) {
//			getLoginStaticArea().setErrMessage(JFireBasePlugin.getResourceString("login.error.missingSecurityProtocol"));
//			return;
//		}

		// use entries and log in
		setActualValues();		
		if (getLoginDetailArea().getCheckBoxSaveSettings().getSelection()) {
			try {
				BeanUtils.copyProperties(persistentLoginModule,runtimeLoginModule);
				persistentLoginModule.setChanged();
//				persistentLoginModule._getConfig().saveConfFile();
			} catch (Exception e) {
				logger.error("Saving config failed!", e);
			}
		}
		
		Login.AsyncLoginResult testResult = Login.testLogin(loginContext);
		testResult.copyValuesTo(loginResult);
		
//		Login login = null;
//		try {
//			login = Login.getLogin(false);
//		} catch (LoginException e) {
//			LOGGER.error("Obtaining shared instance of Login failed!", e);
//		}
//		
//		if ( login != null)
//			login.copyPropertiesFrom(loginContext);
//		else
//			throw new IllegalStateException("Shared instance of Login must not be null");
//		
//		boolean wasAuthenticationErr = false;
//		boolean wasCommunicationErr = false;
//		boolean wasSocketTimeout = false;
//		loginResult.setSuccess(true);
//		loginResult.setMessage(null);
//		loginResult.setException(null);
//		login.flushInitialContextProperties();
//		
//		// verify login
//		JFireRCLBackend jfireCLBackend = null;
////		LanguageManager languageManager = null;
//		if (jfireCLBackend == null) {
//			try {
//				jfireCLBackend = JFireRCLBackendUtil.getHome(
//						login.getInitialContextProperties()).create();
////				languageManager = LanguageManagerUtil.getHome(
////						login.getInitialContextProperties()).create();
//			} catch (RemoteException remoteException) {
//				Throwable cause = remoteException.getCause();
//				if (cause != null && cause.getCause() instanceof EJBException) {
//					EJBException ejbE = (EJBException)cause.getCause();
//					if (ejbE != null) {
//						if (ejbE.getCausedByException() instanceof SecurityException) {
//							// SecurityException authentication failure
//							wasAuthenticationErr = true;
//						}
//					}
//				}
//				else {
//					if (ExceptionUtils.indexOfThrowable(cause, SecurityException.class) >= 0)
//						wasAuthenticationErr = true;
//				}
//			} catch (LoginException x) {
//				LOGGER.warn("Login failed with a very weird LoginException!", x);
//				// something went very wrong as we are in the login procedure
//				IllegalStateException ill = new IllegalStateException("Caught LoginException although getLogin(FALSE) was executed. "+x.getMessage());
//				ill.initCause(x);
//				throw ill;
//			} catch (Exception x) {
//				if (x instanceof CommunicationException) {
//					wasCommunicationErr = true;
//				}
//				if (x instanceof SocketTimeoutException) {
//					wasSocketTimeout = true;
//				}
//				// cant create local bean stub
//				LOGGER.warn("Login failed!", x);
//				LoginException loginE = new LoginException(x.getMessage());
//				loginE.initCause(x);
//				loginResult.setSuccess(false);
//				loginResult.setMessage(JFireBasePlugin.getResourceString("login.error.unhadledExceptionMessage"));
//				loginResult.setException(loginE);
//			}
//		}
//		System.out.println(Locale.getDefault().getDisplayLanguage());
//		try {
//			languageManager.createLanguage(Locale.getDefault().getLanguage(),Locale.getDefault().getDisplayLanguage());
//		} catch (Exception e) {
//			// setting language failed, but login ok
//			LOGGER.error("Login OK, but creating lanugage failed.");
//		}		
		// verify login done 
		
		if ((!loginResult.isWasAuthenticationErr()) && (loginResult.isSuccess()))
			close();
		else {
			// login failed
			if (loginResult.isWasAuthenticationErr()) {
				getLoginStaticArea().setErrMessage(JFireBasePlugin.getResourceString("login.error.authenticationFailed"),3);
			}
			else if (loginResult.isWasCommunicationErr()) {
				Throwable error = loginResult.getException();
				while (error.getLocalizedMessage() == null && error.getCause() != null) {
					error = ExceptionUtils.getCause(error);
				}
				getLoginStaticArea().setErrMessage(JFireBasePlugin.getResourceString("login.error.communicatinError") + " " + error.getLocalizedMessage(),3);				
			}
			else {
				int lineNo = 2;
				String message = loginResult.getMessage();
				if (loginResult.getException() != null) {
					message += "\n"+loginResult.getException().getClass().getName()+": "+loginResult.getException().getLocalizedMessage();
					Throwable cause = loginResult.getException();
					while ( cause != null ) {
						message += "\n"+cause.getClass().getName()+": "+cause.getLocalizedMessage();
						lineNo++;
						cause = cause.getCause();
					}
					loginResult.getException().printStackTrace();
				}
				getLoginStaticArea().setErrMessage(message,lineNo);
				
			}
			// show a message to the user
		}
	}
	
	protected void offlinePressed(SelectionEvent event) {
		loginResult.setSuccess(false);
		loginResult.setWorkOffline(true);
//		loginResult.setMessage(JFireBasePlugin.getResourceString("login.error.offlineDecision"));
//		loginResult.setException(new WorkOfflineException(JFireBasePlugin.getResourceString("login.error.offlineDecision")));
		close();
	}

	protected void quitPressed(SelectionEvent event) {
		loginResult.setSuccess(false);
		close();
//		Workbench.getInstance().close();
		System.exit(0);
	}
	
	
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
//		shell.setImage(new Image(null,"/home/alex/Java/eclipse-SDK-3.0R3/workspace/org.nightlabs.jfire.base.app/icons/Login_loggedin.gif"));
	}
	
	
	protected static LoginDialog sharedInstance = null;
	
	public static void registerSharedInstance(LoginDialog dialog) {
		sharedInstance = dialog;
	}
	
	public static void deregisterSharedInstance() {
		sharedInstance = null;
	}

	public static LoginDialog getSharedInstace() {
		return sharedInstance;
	}
}
