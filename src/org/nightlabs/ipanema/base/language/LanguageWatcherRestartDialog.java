/*
 * Created 	on Jan 5, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.language;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.nightlabs.ipanema.base.JFireBasePlugin;

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
