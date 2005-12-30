/*
 * Created 	on Sep 1, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.login.view;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.nightlabs.base.view.ControllableView;
import org.nightlabs.base.view.ViewController;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.LoginStateListener;

/**
 * ViewController that will update the registered views whenever the
 * LoginState changes. It will dispose the contents of all controlled views
 * when the user logs out.
 * See {@link org.nightlabs.base.view.ViewController} and
 * {@link org.nightlabs.base.view.ControllableView} for detailed explanation on
 * how a ViewController works, here is a exaple on how to use the LSDViewController.
 * 
 * In the constructor of your view you want to make LoginStateDependent register
 * it to the sharedInstance of LSDViewController:
 * <pre>
 * 	public MyView() {
 * 		LSDViewController.sharedInstance().registerView(this);
 * 	}
 * </pre>
 * 
 * Delegate the createPartControl() method of your View to the sharedInstance:
 * <pre>
 *  public void createPartControl(Composite parent)
 *  {
 *  	LSDViewController.sharedInstance().createViewControl(this, parent);
 *  }
 * </pre>
 * And create the real View contents in {@link org.nightlabs.base.view.ControllableView#createViewContents(Composite)}.
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class LSDViewController extends ViewController implements LoginStateListener {

	protected Composite createNewConditionUnsatisfiedComposite(Composite parent) {
		return new NeedLoginComposite(parent, SWT.BORDER);
	}

	/**
	 * @see org.nightlabs.base.view.ViewController#registerView(org.nightlabs.base.view.ControllableView)
	 */
	public void registerView(ControllableView view) {		
		super.registerView(view);
		try {
			Login.getLogin();
		} catch (LoginException e) {
			// ignore LoginExceptions here
		}
	}
	
	public void loginStateChanged(int loginState, IAction action) {
		if (loginState != Login.LOGINSTATE_LOGGED_IN)
			disposeViewsContents();
		updateViews();
	}	
	
	private static LSDViewController sharedInstance;
	
	public static LSDViewController sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new LSDViewController();
			Login login = null;
			try {
				login = Login.getLogin(false);
			} catch (LoginException e) {
				throw new IllegalStateException("This should never happen as Login.getLogin(false) was called.");
			}
			login.addLoginStateListener(sharedInstance);
		}
		return sharedInstance;
	}
}
