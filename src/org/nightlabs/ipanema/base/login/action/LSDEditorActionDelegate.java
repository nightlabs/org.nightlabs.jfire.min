/*
 * Created 	on Nov 23, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.login.action;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.base.login.LoginStateListener;

/**
 * Provides login-state-dependency for WorkbenchWindowActions wich are
 * actions contributed into an editor-activated menu or tool bar.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class LSDEditorActionDelegate implements IEditorActionDelegate, LoginStateListener {

	private IEditorPart activeEditor;
	/**
	 * Returns the IEditorPart passed in {@link #setActiveEditor(IAction, IEditorPart)}
	 * @return
	 */
	protected IEditorPart getActiveEditor() {
		return activeEditor;
	}
	
	/**
	 * Default implementation remembers the passed
	 * IEditorPart and makes it Accessible through {@link}
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.activeEditor = targetEditor;
	}

	/**
	 * Has to be implemented
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
		try {
			Login.getLogin(false).addLoginStateListener(this,action);
		} catch (LoginException e) {
			throw new RuntimeException("Login.getLogin(false) should never throw this exception!", e);
		}
	}
	
	/**
	 * Default implementation of loginStateChanged does nothing.
	 * @see LoginStateListener#loginStateChanged(int, IAction)
	 */
	public void loginStateChanged(int loginState, IAction action) {		
	}

}
