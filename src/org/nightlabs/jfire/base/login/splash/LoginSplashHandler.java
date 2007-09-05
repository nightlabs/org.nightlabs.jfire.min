/**
 * 
 */
package org.nightlabs.jfire.base.login.splash;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;
import org.nightlabs.base.composite.IMessageContainer;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.login.JFireLoginContext;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.LoginComposite;
import org.nightlabs.jfire.base.login.LoginConfigModule;
import org.nightlabs.jfire.base.login.LoginComposite.Mode;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * This Implementation of AbstractSplashHandler shows a login inside
 * the splash screen.
 * 
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class LoginSplashHandler 
extends AbstractSplashHandler
implements IMessageContainer
//,BundleListener
{
	private static Logger logger = Logger.getLogger(LoginSplashHandler.class);

	private LoginComposite loginComposite;
	private boolean authenticationPending = true;

	private boolean isAuthenticationPending() {
		return authenticationPending;
	}
	private void setAuthenticationPending(boolean authenticationPending) {
		this.authenticationPending = authenticationPending;
	}
	
	private StackLayout stackLayout;
	private Composite loginComp;
	private Button detailsButton;
	private int backgroundMode = SWT.INHERIT_FORCE;
	private Composite progressWrapper;
	private SplashProgressMonitor progressMonitor = null;

	private static LoginSplashHandler _sharedInstance;
	public static LoginSplashHandler sharedInstance() {
		return _sharedInstance;
	}
	
	public LoginSplashHandler() {
		super();
		_sharedInstance = this;
	}

	public static boolean canShowSplashLogin() {
		return _sharedInstance != null && _sharedInstance.isCanShowSplashLogin();
	}
				
	@Override
	public void init(Shell splash) 
	{
		super.init(splash); 
		getSplash().addDisposeListener(disposeListener);
		stackLayout = new StackLayout();		
		getSplash().setLayout(stackLayout);
		getSplash().setBackgroundMode(backgroundMode);
		canShowSplashLogin = true;
		
		progressWrapper = new Composite(getSplash(), SWT.NONE);
		progressWrapper.setLayout(new GridLayout());
		Composite spacer = new Composite(progressWrapper, SWT.NONE);
		spacer.setLayoutData(new GridData(GridData.FILL_BOTH));
		progressMonitor = new SplashProgressMonitor(new ProgressMonitorPart(progressWrapper, null));
		progressMonitor.getProgressMonitorPart().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
//		JFireBasePlugin.getDefault().getBundle().getBundleContext().addBundleListener(this);
		
		showProgressMonitor();
		try {
			Login.getLogin();
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}

	/**
	 * is called when an login should be displayed inside the splash
	 * 
	 * @param loginContext
	 * @param loginConfigModule
	 * @param loginResult
	 * @throws LoginException
	 */
	public void handleSplashLogin(JFireLoginContext loginContext, 
			LoginConfigModule loginConfigModule, final Login.AsyncLoginResult loginResult) 
	throws LoginException 
	{
		loginComp = createLoginComposite(getSplash(), loginResult, 
				loginConfigModule, loginContext, Mode.SHOW_ONLY_LOGIN_AREA);
		loginComp.setBackgroundMode(backgroundMode);
		stackLayout.topControl = loginComp; 		
		getSplash().layout(true, true);
		doEventLoop();
	}
			
	private Composite createLoginComposite(Composite parent, Login.AsyncLoginResult loginResult, 
			LoginConfigModule loginModule, JFireLoginContext loginContext, Mode mode) 
	{
		Composite wrapper = new Composite(parent, SWT.NONE);
		wrapper.setBackgroundMode(backgroundMode);
		wrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
		wrapper.setLayout(new GridLayout());		
		
		messageLabel = new Label(wrapper, SWT.WRAP);
		messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		loginComposite = new LoginComposite(wrapper, SWT.NONE, loginResult, 
				loginModule, loginContext, this, mode);		
		loginComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		loginComposite.setBackgroundMode(backgroundMode);
		loginComposite.addSelectionListener(textSelectionListener);
		
		Composite spacerComp = new Composite(wrapper, SWT.NONE);
		spacerComp.setBackgroundMode(backgroundMode);
		spacerComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite buttonComp = new Composite(wrapper, SWT.NONE);
		buttonComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonComp.setLayout(new GridLayout(4, false));		
		Label spacer = new Label(buttonComp, SWT.NONE);
		spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		Button okButton = new Button(buttonComp, SWT.NONE);
		okButton.setText("OK");
		okButton.addSelectionListener(okListener);
		Button cancelButton = new Button(buttonComp, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(cancelListener);
		detailsButton = new Button(buttonComp, SWT.NONE);
		detailsButton.addSelectionListener(detailsListener);
		if (mode == Mode.SHOW_ONLY_LOGIN_AREA)
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		else if (mode == Mode.SHOW_ONLY_DETAIL_AREA)
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
		
		return wrapper;
	}
	
	private SelectionListener textSelectionListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			okPressed();
		}
	};
			
	
	private void doEventLoop() 
	{
		if (logger.isDebugEnabled())
			logger.debug("doEventLoop: begin");

		Shell splash = getSplash();
		while (isAuthenticationPending()) {
			try {
				if (!splash.getDisplay().readAndDispatch()) {
					splash.getDisplay().sleep();
				}
			} catch (Throwable t) {
				logger.error("Caught exception in event loop!", t);
				setMessage(t.getMessage(), IMessageProvider.ERROR);
			}
		}

		if (logger.isDebugEnabled())
			logger.debug("doEventLoop: end");
	}
	
	private void okPressed() {
		boolean successfulAuthentication = loginComposite.checkLogin(
				false, getBundleProgressMonitor(), null
		);
		if (successfulAuthentication) {
			showProgressMonitor();
			setAuthenticationPending(false);
		} else {
			showLoginComposite();
		}
	}
	
	private SelectionListener okListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
			okPressed();
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};
	
	private SelectionListener cancelListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
			loginComposite.getLoginResult().setSuccess(false);
			loginComposite.getLoginResult().setWorkOffline(true);
			setAuthenticationPending(false);
			showProgressMonitor();
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};	
	
	private boolean detailsPressed = false;
	private SelectionListener detailsListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
			if (!detailsPressed) {
				loginComposite.setMode(Mode.SHOW_ONLY_DETAIL_AREA);
				detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);				
			} else {
				loginComposite.setMode(Mode.SHOW_ONLY_LOGIN_AREA);
				detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
			}
			detailsPressed = !detailsPressed;			
			stackLayout.topControl = loginComp;
			getSplash().layout(true, true);
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};
	
	private boolean canShowSplashLogin = false;
	public boolean isCanShowSplashLogin() {
		return canShowSplashLogin;
	}
	
	private DisposeListener disposeListener = new DisposeListener(){
		public void widgetDisposed(DisposeEvent e) {
			canShowSplashLogin = false;
		}
	};
	
	private Label messageLabel = null;
	public void setMessage(String newMessage, int newType) 
	{
		if (messageLabel != null && !messageLabel.isDisposed())
			messageLabel.setText(newMessage == null ? "" : newMessage);
	}
	
	@Override
	public IProgressMonitor getBundleProgressMonitor() {
		return progressMonitor;
	}
	
	private void showProgressMonitor() {
		stackLayout.topControl = progressWrapper;		
		getSplash().layout(true, true);
		getSplash().redraw();
	}
	
	private void showLoginComposite() {
		stackLayout.topControl = loginComp;
		getSplash().layout(true, true);
		getSplash().redraw();
	}

//	public void bundleChanged(BundleEvent event) 
//	{		
//			System.err.println(event.getBundle().getSymbolicName()+" "+bundleEventTypeToString(event.getType()));
//	}
//	
//	private static final String EVENT_TYPE_INSTALLED = "installed";
//	private static final String EVENT_TYPE_RESOLVED = "resolved";
//	private static final String EVENT_TYPE_STARTED = "started";
//	private static final String EVENT_TYPE_STOPPED = "stopped";
//	private static final String EVENT_TYPE_UNINSTALLED = "uninstalled";
//	private static final String EVENT_TYPE_UNRESOLVED = "unresolved";
//	private static final String EVENT_TYPE_UPDATED = "updated";
//	private static final String EVENT_TYPE_UNKNOWN = "unknown type";
//
//	public static String bundleEventTypeToString(int eventType) {
//		switch (eventType) {
//			case BundleEvent.INSTALLED: return EVENT_TYPE_INSTALLED;
//			case BundleEvent.RESOLVED: return EVENT_TYPE_RESOLVED;
//			case BundleEvent.STARTED: return EVENT_TYPE_STARTED;
//			case BundleEvent.STOPPED: return EVENT_TYPE_STOPPED;
//			case BundleEvent.UNINSTALLED: return EVENT_TYPE_UNINSTALLED;
//			case BundleEvent.UNRESOLVED: return EVENT_TYPE_UNRESOLVED;
//			case BundleEvent.UPDATED: return EVENT_TYPE_UPDATED;		 
//		}
//		return EVENT_TYPE_UNKNOWN;
//	}	
}
