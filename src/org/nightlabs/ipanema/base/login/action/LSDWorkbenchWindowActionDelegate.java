/*
 * Created 	on Nov 23, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.login.action;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.base.login.LoginStateListener;

/**
 * Provides login-state-dependency for WorkbenchWindowActions wich are
 * actions contributed into the workbench window menu or tool bar.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class LSDWorkbenchWindowActionDelegate implements
		IWorkbenchWindowActionDelegate, LoginStateListener {

	/**
	 * Default implementation of dispose removes this instance
	 * as LoginStateListener, so make sure to always call super.dispose(). 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		try {
			Login.getLogin(false).removeLoginStateListener(this);
		} catch (LoginException e) {
			throw new RuntimeException("Login.getLogin(false) should never throw this exception!", e);
		}
	}
	
	IWorkbenchWindow window;
	/**
	 * Returns the IWorkbenchWindow passed in {@link #init(IWorkbenchWindow)}
	 * @return
	 */
	protected IWorkbenchWindow getWindow() {
		return window;
	}
	
	/**
	 * Default implementation of init remembers the
	 * passed IWorkbenchWindow and makes it accessible 
	 * through {@link #getWindow()}
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
		this.window = window;
		ISelectionProvider oldProvider = window.getActivePage().getActivePart().getSite().getSelectionProvider();
		ISelectionProvider test = new ISelectionProvider(){
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}
			public ISelection getSelection() {
				return new StructuredSelection();
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
			}
		};
		try {
			window.getActivePage().getActivePart().getSite().setSelectionProvider(test);	
			test.setSelection(null);
		} finally {
			window.getActivePage().getActivePart().getSite().setSelectionProvider(oldProvider);
		}
//		window.getWorkbench().getActiveWorkbenchWindow().
	}

	/**
	 * Has to be implemented.
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public abstract void run(IAction action);
	
	/**
	 * Subclasses may override this but have to make sure
	 * super.selectionChanged(action,selection) is called to 
	 * further provide login-state-dependency
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		Login.sharedInstance().addLoginStateListener(this,action);
	}
	
	/**
	 * Default implementation of loginStateChanged does nothing.
	 * @see LoginStateListener#loginStateChanged(int, IAction)
	 */
	public void loginStateChanged(int loginState, IAction action) {		
	}

}
