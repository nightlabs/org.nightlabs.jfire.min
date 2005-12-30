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

package org.nightlabs.jfire.base.login.action;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.LoginStateListener;

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
