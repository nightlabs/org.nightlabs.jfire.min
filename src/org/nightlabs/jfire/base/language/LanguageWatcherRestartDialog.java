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

package org.nightlabs.jfire.base.language;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.nightlabs.jfire.base.JFireBasePlugin;

/**
 * Shows a question to the user wheather he wants
 * to restart with a different language.
 * If OK is pressed then the new language will be set 
 * and the workbench will be restarted.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class LanguageWatcherRestartDialog extends Dialog {

	/**
	 * @param parentShell
	 */
	public LanguageWatcherRestartDialog(Shell parentShell) {
		super(parentShell);
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent,SWT.NONE);
		
		return comp;
	}
	
	protected void okPressed() {
		super.okPressed();
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(JFireBasePlugin.getResourceString("language.watcher.restartdialog.title"));
	}
}
