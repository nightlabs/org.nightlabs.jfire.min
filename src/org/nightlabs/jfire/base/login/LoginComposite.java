/**
 * 
 */
package org.nightlabs.jfire.base.login;

import java.util.LinkedList;

import javax.security.auth.login.LoginContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.IMessageContainer;
import org.nightlabs.base.composite.XComboComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.login.splash.LoginSplashHandler;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * LoginComposite displayes all necessary information for Login
 * Is used in The {@link LoginDialog} as well as in the {@link LoginSplashHandler}
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class LoginComposite 
extends Composite
{
	public static final Logger logger = Logger.getLogger(LoginComposite.class);
	
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * Mode which controls how the LoginComposite should be created
	 */
	public enum Mode 
	{
		SHOW_ONLY_LOGIN_AREA,
		SHOW_ONLY_DETAIL_AREA,
		SHOW_LOGIN_AND_DETAIL_AREA
	}
	
	/**
	 * Creates a LoginComposite for login to an JFire Application
	 * 
	 * @param parent the parent Composite
	 * @param style teh SWT style falg
	 * @param loginResult the loginResult
	 * @param loginModule the LoginConfigMoudle
	 * @param loginContext the loginContext
	 * @param messageContainer the IMEssageContainer for displaying messages
	 * @param mode the mode in which the loginComposite should be created
	 */
	public LoginComposite(Composite parent, int style, Login.AsyncLoginResult loginResult, 
			LoginConfigModule loginModule, JFireLoginContext loginContext, 
			IMessageContainer messageContainer, Mode mode) 
	{
		super(parent, style);
		try {
			persistentLoginModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}		
		this.loginResult = loginResult;
		this.runtimeLoginModule = loginModule;
		this.loginContext = loginContext;
		this.messageContainer = messageContainer;
		this.mode = mode;
		
		setBackgroundMode(backgroundMode);
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createContent(this);
//		layout(true, true);
		pack(true);
	}
	
	/**
	 * Used to set the details area visible or invisible 
	 * by setting heightHint to 0 or SWT.DEFAULT.
	 */
	private GridData detailsAreaGridData = null;
	/**
	 * Used to set the main area visible or invisible 
	 * by setting heightHint to 0 or SWT.DEFAULT.
	 */	
	private GridData mainAreaGridData = null;	
	private Mode mode = null;
	private IMessageContainer messageContainer = null;
	private LoginConfigModule persistentLoginModule = null;
	private LoginConfigModule runtimeLoginModule = null;
	private Login.AsyncLoginResult loginResult = null;	
	private JFireLoginContext loginContext = null;	
	private XComboComposite<LoginConfiguration> recentLoginConfigs;	
	private Text textUserID = null;
	private Text textPassword = null;
	private Text textOrganisationID = null;
	private Text textServerURL = null;
	private Button checkBoxSaveSettings = null;
	private Text textInitialContextFactory = null;
	private Text textWorkstationID = null;
	private Text textIdentityName = null;
	private int backgroundMode = SWT.INHERIT_DEFAULT;
		
	private boolean manuallyUpdating = false;
	private ModifyListener loginDataModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (!manuallyUpdating) {
				if (recentLoginConfigs != null)
					recentLoginConfigs.setSelection(-1);
//				deleteButton.setEnabled(false);
			}
		}
	};
		
	protected void createContent(Composite parent) 
	{
		createMainArea(parent);
		createDetailsArea(parent);

		switch (mode) 
		{
			case SHOW_ONLY_LOGIN_AREA:
				detailsAreaGridData.heightHint = 0;
				mainAreaGridData.heightHint = SWT.DEFAULT;
				break;
			case SHOW_ONLY_DETAIL_AREA:
				detailsAreaGridData.heightHint = SWT.DEFAULT;
				mainAreaGridData.heightHint = 0;
				break;
			case SHOW_LOGIN_AND_DETAIL_AREA:
				mainAreaGridData.heightHint = SWT.DEFAULT;
				detailsAreaGridData.heightHint = SWT.DEFAULT;
				break;
		}
				
		initializeWidgetValues();
		setSmartFocus();
//		showDetails(isInitiallyShowDetails());		
	}	
	
	protected Control createMainArea(Composite parent)
	{
		Composite mainArea = new Composite(parent, SWT.NONE);
		mainAreaGridData = new GridData(GridData.FILL_HORIZONTAL);
		mainArea.setLayoutData(mainAreaGridData);
		mainArea.setBackgroundMode(backgroundMode);
		
		GridLayout gridLayoutStatic = new GridLayout();
		mainArea.setLayout(gridLayoutStatic);
		gridLayoutStatic.numColumns = 2;

		LabelProvider loginConfigLabelProv = new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LoginConfiguration) {
					LoginConfiguration loginConfig = (LoginConfiguration) element;
					if (runtimeLoginModule != null && loginConfig == runtimeLoginModule.getLatestLoginConfiguration()) {
						return Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.currentIdentityMarker"); //$NON-NLS-1$
					}
					return loginConfig.toString();
				} else
					return ""; //$NON-NLS-1$
			}
		};
		
		Label labelRecentLoginConfigs = new Label(mainArea, SWT.NONE);
		labelRecentLoginConfigs.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.recentLoginsComboLabel")); //$NON-NLS-1$
		recentLoginConfigs = new XComboComposite<LoginConfiguration>(mainArea, SWT.READ_ONLY);
		recentLoginConfigs.setLabelProvider(loginConfigLabelProv);		
		recentLoginConfigs.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateUIWithLoginConfiguration(recentLoginConfigs.getSelectedElement());
			}
		});
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		new Label(mainArea, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(gd);
		
		Label labelUserID = new Label(mainArea, SWT.NONE);
		textUserID = new Text(mainArea, SWT.BORDER);
		Label labelPassword = new Label(mainArea, SWT.NONE);
		textPassword = new Text(mainArea, SWT.BORDER);
				
		labelUserID.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labeluser")); //$NON-NLS-1$
		textUserID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelPassword.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labelpass")); //$NON-NLS-1$
		textPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textPassword.setEchoChar('*');
		textUserID.addModifyListener(loginDataModifyListener);
		
		return mainArea;
	}	
	
	private void updateUIWithLoginConfiguration(LoginConfiguration loginConfiguration) 
	{
		manuallyUpdating = true;
		textUserID.setText(loginConfiguration.getUserID());
		textPassword.setText(""); //$NON-NLS-1$			
		textOrganisationID.setText(loginConfiguration.getOrganisationID());
		textServerURL.setText(loginConfiguration.getServerURL());
		textInitialContextFactory.setText(loginConfiguration.getInitialContextFactory());
		textWorkstationID.setText(loginConfiguration.getWorkstationID());					
		manuallyUpdating = false;
	}
	
	protected Control createDetailsArea(Composite parent)
	{
		Composite detailsArea = new Composite(parent, SWT.NONE);
		detailsArea.setBackgroundMode(backgroundMode);
		
		detailsAreaGridData = new GridData(GridData.FILL_HORIZONTAL);
		detailsArea.setLayoutData(detailsAreaGridData);
				
		if (mode == Mode.SHOW_ONLY_LOGIN_AREA)
			detailsAreaGridData.heightHint = 0;
		
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
		
		labelOrganisationID.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labelorganisation")); //$NON-NLS-1$
		textOrganisationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
		labelWorkstationID.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labelworkstation")); //$NON-NLS-1$
		textWorkstationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelServerURL.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labelserverURL")); //$NON-NLS-1$
		textServerURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		labelInitialContextFactory.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labelinitialContext")); //$NON-NLS-1$
		textInitialContextFactory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		GridData spanHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		spanHorizontalGD.horizontalSpan = 2;
		new Label(detailsArea, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(spanHorizontalGD);
		
		checkBoxSaveSettings = new Button(detailsArea, SWT.CHECK);
		checkBoxSaveSettings.setSelection(false);
		checkBoxSaveSettings.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labelsaveSettings")); //$NON-NLS-1$
		checkBoxSaveSettings.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				textIdentityName.setEnabled(checkBoxSaveSettings.getSelection());
				checkIdentityName();
			}
		});
		
//		XComposite wrapper = new XComposite(detailsArea, SWT.NONE, 
//				LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		Composite wrapper = new Composite(detailsArea, SWT.BORDER);
		GridLayout layout = new GridLayout(2, false);
		layout = XComposite.getLayout(LayoutMode.TIGHT_WRAPPER, layout);
		wrapper.setLayout(layout);
		wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		wrapper.setBackgroundMode(backgroundMode);

		new Label(wrapper, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.identityNameTextLabel")); //$NON-NLS-1$
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
						
		return detailsArea;
	}

	private void checkIdentityName() {
		String name = getTextIdentityName().getText();
		if (getCheckBoxSaveSettings().getSelection() && (name == null || "".equals(name))) { //$NON-NLS-1$
//			getButton(IDialogConstants.OK_ID).setEnabled(false);
			setErrorMessage(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.validNameMissingErrorMessage")); //$NON-NLS-1$
		} else {
//			getButton(IDialogConstants.OK_ID).setEnabled(true);
			if (getCheckBoxSaveSettings().getSelection() && runtimeLoginModule.hasConfigWithName(name))
				setWarningMessage(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.loginConfigurationAlreadyExists")); //$NON-NLS-1$
			else {
				setWarningMessage(null);
				setErrorMessage(null);
			}
		}
	}	
	
	/**
	 * @return the textUserID
	 */
	private Text getTextUserID() {
		return textUserID;
	}

	/**
	 * @return the textPassword
	 */
	private Text getTextPassword() {
		return textPassword;
	}

	/**
	 * @return the textOrganisationID
	 */
	private Text getTextOrganisationID() {
		return textOrganisationID;
	}

	/**
	 * @return the textServerURL
	 */
	private Text getTextServerURL() {
		return textServerURL;
	}

	/**
	 * @return the checkBoxSaveSettings
	 */
	private Button getCheckBoxSaveSettings() {
		return checkBoxSaveSettings;
	}

	/**
	 * @return the textWorkstationID
	 */
	private Text getTextWorkstationID() {
		return textWorkstationID;
	}

	/**
	 * returns the {@link Text} which displayes the user id
	 * @return the textIdentityName
	 */
	public Text getTextIdentityName() {
		return textIdentityName;
	}	
	
	/**
	 * 
	 * @return the textInitialContextFactory
	 */
	private Text getTextInitialContextFactory() {
		return textInitialContextFactory;
	}

	/**
	 * return the {@link XComboComposite} which displayes the
	 * recent {@link LoginConfiguration}s
	 *  
	 * @return the recentLoginConfigs
	 */
	public XComboComposite<LoginConfiguration> getRecentLoginConfigs() {
		return recentLoginConfigs;
	}
	
	/**
	 * Tries to perform a Login with the given values displayed in the LoginComposite
	 * and returns if it was successful or not
	 * 
	 * @param async determines if the login should be performed asynchronously or not
	 * @param monitor the optional IPOrgressMonitor for displaying the progress, may be null
	 * @param loginStateListener the optional {@link LoginStateListener} to get notified about the login result
	 * @return true if the login was a success or false if not
	 */
	public boolean checkLogin(boolean async, final IProgressMonitor monitor, 
			final LoginStateListener loginStateListener)
	{
		boolean hadError = true;
		setInfoMessage(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.tryingLogin")); //$NON-NLS-1$
		enableDialogUI(false);
		try {
			// use entries and log in
			monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.login.LoginComposite.loginTask.name"), 4); //$NON-NLS-1$
			storeUserInput();
			monitor.worked(1);
			final boolean saveSettings = checkBoxSaveSettings.getSelection();

			if (async) {
				Job job = new Job(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.authentication")) { //$NON-NLS-1$
					@Override
					protected IStatus run(IProgressMonitor arg0)
					{
						doCheckLogin(saveSettings, monitor, loginStateListener);
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				hadError = false;
				return false;
			} else {
				hadError = false;
				return doCheckLogin(saveSettings, monitor, loginStateListener);
			}
		} finally {
			if (hadError)
				enableDialogUI(true);
			monitor.done();
		}		
	}	
	
	private boolean doCheckLogin(boolean saveSettings, IProgressMonitor monitor, LoginStateListener loginStateListener) 
	{
		Login.AsyncLoginResult testResult = Login.testLogin(loginContext);
		monitor.worked(1);
		testResult.copyValuesTo(loginResult);
		monitor.worked(1);
		
		try {
			if (testResult.isSuccess()) {
				runtimeLoginModule.makeLatestFirst();
				
				if (saveSettings)
					runtimeLoginModule.saveLatestConfiguration();
				
				if (loginStateListener != null)
					loginStateListener.loginStateChanged(Login.LOGINSTATE_LOGGED_IN, null);
			} else {
				if (loginStateListener != null)
					loginStateListener.loginStateChanged(Login.LOGINSTATE_LOGGED_OUT, null);
			}

			BeanUtils.copyProperties(persistentLoginModule, runtimeLoginModule);
			persistentLoginModule.setChanged();
		} catch (Exception e) {
			logger.error(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errorSaveConfig"), e); //$NON-NLS-1$
		}
		
		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				enableDialogUI(true);
				updateUIAfterLogin();
			}
		});
		monitor.worked(1);
		
		return testResult.isSuccess();
	}
	
	/**
	 * Enable or disable all dialog UI elements.
	 * @param enable <code>true</code> To enable all elements -
	 * 		<code>false</code> otherwise.
	 */
	private void enableDialogUI(boolean enable)
	{
		if (!isDisposed())
			getShell().setEnabled(enable);
	}	
	
	/**
	 * stores the values in the {@link LoginContext} as well as in the
	 * {@link LoginConfigModule} as lastest Login Configuration 
	 */
	public void storeUserInput()
	{		
		loginContext.setCredentials(
				textUserID.getText(),
				textOrganisationID.getText(),
				textPassword.getText()
		);
		
		runtimeLoginModule.setLatestLoginConfiguration(
				textUserID.getText(), 
				textWorkstationID.getText(), textOrganisationID.getText(),
				textServerURL.getText(), textInitialContextFactory.getText(), 
				null, textIdentityName.getText());
	}
	
	private void updateUIAfterLogin()
	{
		// verify login done 
		if ((!loginResult.isWasAuthenticationErr()) && (loginResult.isSuccess())) {
			// is now done by LoginStateListener in doCheckLogin
//			close();
		} 
		else {
			// login failed
			if (loginResult.isWasAuthenticationErr()) {
				setErrorMessage(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errorauthenticationFailed")); //$NON-NLS-1$
			}
			else if (loginResult.isWasCommunicationErr()) {
				Throwable error = loginResult.getException();
				while (error.getLocalizedMessage() == null && error.getCause() != null) {
					error = ExceptionUtils.getCause(error);
				}
				setErrorMessage(String.format(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errorcommunicatinError"), error.getLocalizedMessage())); //$NON-NLS-1$
			}
			else {
				String message = loginResult.getMessage();
				if (loginResult.getException() != null) {
					message += String.format(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errorAppend"), loginResult.getException().getClass().getName(), loginResult.getException().getLocalizedMessage()); //$NON-NLS-1$
					Throwable cause = loginResult.getException();
					while ( cause != null ) {
						message += String.format(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errorAppend"), cause.getClass().getName(), cause.getLocalizedMessage()); //$NON-NLS-1$
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
	 * returns the loginResult 
	 * @return the loginResult
	 */
	public Login.AsyncLoginResult getLoginResult() {
		return loginResult;
	}
	
	private void initializeWidgetValues()	{
		LinkedList<LoginConfiguration> loginConfigurations = new LinkedList<LoginConfiguration>(runtimeLoginModule.getSavedLoginConfigurations());		
		LoginConfiguration latestLoginConfiguration = runtimeLoginModule.getLatestLoginConfiguration();
		
		if (latestLoginConfiguration != null)
			loginConfigurations.addFirst(latestLoginConfiguration);
		
		if (getRecentLoginConfigs() != null)
			getRecentLoginConfigs().setInput(loginConfigurations);
		
		if (latestLoginConfiguration != null) {
			if (getRecentLoginConfigs() != null)
				getRecentLoginConfigs().setSelection(latestLoginConfiguration);
			updateUIWithLoginConfiguration(latestLoginConfiguration);
		} else {
			LoginConfiguration loginConfiguration = new LoginConfiguration();
			loginConfiguration.init();
			updateUIWithLoginConfiguration(loginConfiguration);
		}
	}
		
	private void setSmartFocus()
	{
			getTextPassword().setFocus();
			
			if(EMPTY_STRING.equals(getTextUserID().getText()))
				getTextUserID().setFocus();
		
			else if(EMPTY_STRING.equals(getTextWorkstationID().getText())) {
				showDetails(true);
				getTextWorkstationID().setFocus();
			}
			else if(EMPTY_STRING.equals(getTextOrganisationID().getText())) {
				showDetails(true);
				getTextOrganisationID().setFocus();
			}
			else if(EMPTY_STRING.equals(getTextServerURL().getText())) {
				showDetails(true);
				getTextServerURL().setFocus();
			}
			else if(EMPTY_STRING.equals(getTextInitialContextFactory().getText())) {
				showDetails(true);
				getTextInitialContextFactory().setFocus();
			}			
	}	
	
	/**
	 * Show or hide the details area.
	 * @param visible <code>true</code> if the details should be shown
	 * 		<code>false</code> otherwise.
	 */
	private void showDetails(boolean visible) 
	{
		if (visible)
			setMode(Mode.SHOW_LOGIN_AND_DETAIL_AREA);
		else
			setMode(Mode.SHOW_ONLY_LOGIN_AREA);
	}	
	
	/**
	 * checks if the user input is valid and if not displays an 
	 * errorMessage in the given {@link IMessageContainer}
	 * 
	 * @return true if user input is ok, false if not
	 */
	public boolean checkUserInput()
	{
		// check entries
		String errorMessage = null;
		if (getTextUserID().getText().equals(LoginComposite.EMPTY_STRING))
			errorMessage = Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errormissingUserID"); //$NON-NLS-1$
		else if (getTextWorkstationID().getText().equals(LoginComposite.EMPTY_STRING))
			errorMessage = Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errormissingWorkstationID"); //$NON-NLS-1$
		else if (getTextOrganisationID().getText().equals(LoginComposite.EMPTY_STRING))
			errorMessage = Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errormissingOrganisationID"); //$NON-NLS-1$
		else if (getTextInitialContextFactory().getText().equals(LoginComposite.EMPTY_STRING))
			errorMessage = Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errormissingContextFactory"); //$NON-NLS-1$
		else if (getTextServerURL().getText().equals(LoginComposite.EMPTY_STRING))
			errorMessage = Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.errormissingServerURL"); //$NON-NLS-1$
		if(errorMessage != null) {
			setErrorMessage(errorMessage);
			setSmartFocus();
			return false;
		}
		return true;
	}	
	
	private void setErrorMessage(String message) {
		messageContainer.setMessage(message, IMessageProvider.ERROR);
	}
		
	private void setWarningMessage(String message) {
		messageContainer.setMessage(message, IMessageProvider.WARNING);
	}
	
	private void setInfoMessage(String message) {
		messageContainer.setMessage(message, IMessageProvider.INFORMATION);
	}
	
	/**
	 * sets the mode in which the LoginComposite should be visible
	 * the values {@link Mode#SHOW_LOGIN_AND_DETAIL_AREA}, {@link Mode#SHOW_ONLY_DETAIL_AREA} and
	 * {@link Mode#SHOW_ONLY_LOGIN_AREA} are legal
	 * 
	 * @param mode the mode to display
	 */
	public void setMode(Mode mode) 
	{
		this.mode = mode;
		switch (mode) 
		{
			case SHOW_ONLY_LOGIN_AREA:
				detailsAreaGridData.heightHint = 0;
				mainAreaGridData.heightHint = SWT.DEFAULT;
				break;
			case SHOW_ONLY_DETAIL_AREA:
				detailsAreaGridData.heightHint = SWT.DEFAULT;
				mainAreaGridData.heightHint = 0;
				break;
			case SHOW_LOGIN_AND_DETAIL_AREA:
				mainAreaGridData.heightHint = SWT.DEFAULT;
				detailsAreaGridData.heightHint = SWT.DEFAULT;
				break;
		}
		layout(true, true);
	}
	
	/**
	 * adds a SelectionListener to listen for selection on any text inside the loginComposite
	 *  
	 * @param selectionListener
	 */
	public void addSelectionListener(SelectionListener selectionListener) {
		textIdentityName.addSelectionListener(selectionListener);
		textInitialContextFactory.addSelectionListener(selectionListener);
		textOrganisationID.addSelectionListener(selectionListener);
		textPassword.addSelectionListener(selectionListener);
		textServerURL.addSelectionListener(selectionListener);
		textUserID.addSelectionListener(selectionListener);
		textWorkstationID.addSelectionListener(selectionListener);
	}
	
	/**
	 * removes a previously added selectionListener
	 * 
	 * @param selectionListener
	 */
	public void removeSelectionListener(SelectionListener selectionListener) {
		textIdentityName.removeSelectionListener(selectionListener);
		textInitialContextFactory.removeSelectionListener(selectionListener);
		textOrganisationID.removeSelectionListener(selectionListener);
		textPassword.removeSelectionListener(selectionListener);
		textServerURL.removeSelectionListener(selectionListener);
		textUserID.removeSelectionListener(selectionListener);
		textWorkstationID.removeSelectionListener(selectionListener);		
	}
}
