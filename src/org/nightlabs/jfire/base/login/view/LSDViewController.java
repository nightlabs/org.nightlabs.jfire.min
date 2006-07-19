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
			// TODO: Really ignore LoginExceptions here?
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
