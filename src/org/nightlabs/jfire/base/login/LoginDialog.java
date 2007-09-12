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
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.composite.IMessageContainer;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.login.LoginComposite.Mode;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * @author Alexander Bieber
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Daniel Mazurek <!-- daniel[dot]nightlabs[dot]de -->
 */
public class LoginDialog 
extends TitleAreaDialog
implements IMessageContainer
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(LoginDialog.class);
		
	protected static LoginDialog sharedInstance = null;
	
	protected static final int DETAILS_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int DELETE_BUTTON_ID = IDialogConstants.CLIENT_ID+2;
	
	private LoginComposite loginComposite = null;	
	private Button deleteButton = null;	
	private LoginConfigModule persistentLoginModule = null;
	private LoginConfigModule runtimeLoginModule = null;
	private Login.AsyncLoginResult loginResult = null;	
	private JFireLoginContext loginContext = null;	
	private boolean detailsVisible = false;
	
	/**
	 * The parent shell. This is needed because {@link #getParentShell()} does not always
	 * return the shell given in the constructor and we need it to know whether there is
	 * already at least a main window.
	 */
	private Shell parentShell;
		
	public LoginDialog(Shell parent, Login.AsyncLoginResult loginResult, LoginConfigModule loginModule, JFireLoginContext loginContext)
	{
		super(parent);
		this.parentShell = parent;
		setShellStyle(getShellStyle()|SWT.RESIZE);
		try {
			persistentLoginModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}		
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
		shell.setText(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labellogin")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) 
	{
		loginComposite = new LoginComposite(parent, SWT.NONE, loginResult, 
				runtimeLoginModule, loginContext, this, Mode.SHOW_ONLY_LOGIN_AREA);
		
		setTitle(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.titleAreaTitle")); //$NON-NLS-1$
		// TODO: information icon only because of redraw bug:
		setInfoMessage(Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.titleAreaMessage")); //$NON-NLS-1$
		
		return dialogArea;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) 
	{
		deleteButton = createButton(parent, DELETE_BUTTON_ID, Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.deleteButtonLabel"), false); //$NON-NLS-1$
		deleteButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				LoginConfiguration toBeDeleted = loginComposite.getRecentLoginConfigs().getSelectedElement();
				if (toBeDeleted != null) {
					runtimeLoginModule.deleteSavedLoginConfiguration(toBeDeleted);
					try {
						BeanUtils.copyProperties(persistentLoginModule, runtimeLoginModule);
					} catch (Exception e1) {
						e1.printStackTrace();
						throw new RuntimeException(e1);
					}
					persistentLoginModule.setChanged();
					loginComposite.getRecentLoginConfigs().removeAllSelected();
					loginComposite.getRecentLoginConfigs().setSelection(-1);
					loginComposite.getTextIdentityName().setText(""); //$NON-NLS-1$
				}
			}
		});
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labelbutton.login"), true); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("org.nightlabs.jfire.base.login.LoginDialog.labelbutton.offline"), false); //$NON-NLS-1$
		createButton(parent, DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);		
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
		if(!loginComposite.checkUserInput())
			return;
		// execute login asynchronously only if parent shell is not null - i.e. when at least the workbench window is existent.
		System.out.println("parent shell: "+parentShell);
		boolean async = parentShell != null;
		if (loginComposite.checkLogin(async, new NullProgressMonitor(), new LoginStateListener() {
			public void loginStateChanged(int loginState, IAction action) {
				if (loginState == Login.LOGINSTATE_LOGGED_IN) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							close();
						}
					});
				}
			}
		})) {
			super.okPressed();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() 
	{
		loginResult.setSuccess(false);
		loginResult.setWorkOffline(true);
		super.cancelPressed();
	}

	/**
	 * Called when the "Details..." button was pressed.
	 */
	protected void detailsPressed() 
	{
		detailsVisible =! detailsVisible;
		Button detailsButton = getButton(DETAILS_ID);
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (detailsVisible) {
//			loginComposite.getDetailsAreaGridData().heightHint = SWT.DEFAULT;
			loginComposite.setMode(Mode.SHOW_LOGIN_AND_DETAIL_AREA);
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
		} else {
//			loginComposite.getDetailsAreaGridData().heightHint = 0;
			loginComposite.setMode(Mode.SHOW_ONLY_LOGIN_AREA);
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		}		
		Point windowSize = getShell().getSize();
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if(getShell().isVisible())
			getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
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
				
	public static void registerSharedInstance(LoginDialog dialog) {
		sharedInstance = dialog;
	}
	
	public static void deregisterSharedInstance() {
		sharedInstance = null;
	}

	public static LoginDialog getSharedInstace() {
		return sharedInstance;
	}
	
	private void updateUIWithLoginConfiguration(LoginConfiguration loginConfiguration) {
		if (runtimeLoginModule.getLatestLoginConfiguration() != loginConfiguration) {
			loginComposite.getTextIdentityName().setText(loginConfiguration.getName());
			deleteButton.setEnabled(true);
		}	else {
			loginComposite.getTextIdentityName().setText(""); //$NON-NLS-1$
			deleteButton.setEnabled(false);
		}		
	}	
}
