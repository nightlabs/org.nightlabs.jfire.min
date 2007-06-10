/**
 * 
 */
package org.nightlabs.jfire.base.login;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.action.AbstractContributionItem;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.login.action.LoginAction;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LoginStateStatusLineContribution 
extends AbstractContributionItem
implements LoginStateListener
{
	private XComposite wrapper;
	private Label image;
	private Label text;
	
	public LoginStateStatusLineContribution(String name, boolean fillToolBar, boolean fillCoolBar, boolean fillMenuBar, boolean fillComposite) {
		super(LoginStateStatusLineContribution.class.getName(), name, fillToolBar, fillCoolBar, fillMenuBar, fillComposite);
		init();
	}

	public LoginStateStatusLineContribution(String name) {
		super(LoginStateStatusLineContribution.class.getName(), name);
		init();
	}
	
	private void init() {
		try {
			Login.getLogin(false).addLoginStateListener(this);
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	private String earlyLoginText;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.action.AbstractContributionItem#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE);
		StatusLineLayoutData layoutData = new StatusLineLayoutData();
		layoutData.widthHint = 200;
		wrapper.setLayoutData(layoutData);
		wrapper.getGridLayout().numColumns = 2;
		wrapper.getGridLayout().makeColumnsEqualWidth = false;
		image = new Label(wrapper, SWT.ICON);
		image.setImage(SharedImages.getSharedImage(JFireBasePlugin.getDefault(), LoginAction.class, "Login")); //$NON-NLS-1$
		image.setLayoutData(new GridData());
		text = new Label(wrapper, SWT.NONE);		
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (earlyLoginText != null)
			text.setText(earlyLoginText);
		wrapper.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				try {
					Login.getLogin(false).removeLoginStateListener(LoginStateStatusLineContribution.this);
				} catch (LoginException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return wrapper;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.login.LoginStateListener#loginStateChanged(int, org.eclipse.jface.action.IAction)
	 */
	public void loginStateChanged(final int loginState, final IAction action) 
	{
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Login login = null;
				try {
					login = Login.getLogin(false);
				} catch (LoginException e) {
					throw new RuntimeException(e);
				}
				switch (loginState) {
					case Login.LOGINSTATE_LOGGED_IN: text.setText(String.format(Messages.getString("login.LoginStateStatusLineContribution.loggedInStatus"), login.getUserID(), login.getWorkstationID(), login.getOrganisationID())); break; //$NON-NLS-1$
					case Login.LOGINSTATE_LOGGED_OUT: text.setText(Messages.getString("login.LoginStateStatusLineContribution.loggedOutStatus")); break; //$NON-NLS-1$
					case Login.LOGINSTATE_OFFLINE: text.setText(Messages.getString("login.LoginStateStatusLineContribution.offlineStatus")); break; //$NON-NLS-1$
				}
			}
		});
	}	
}
