/*
 * Created 	on Sep 2, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.login.view;

import javax.security.auth.login.LoginException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.base.login.Login;

public class NeedLoginComposite extends XComposite {

	public NeedLoginComposite(Composite parent, int style) {
		super(parent, style);		
		Label label = new Label(this, SWT.WRAP);
		label.setText("You need to login to use this view!");
		label.setLayoutData(new GridData());
		
		Button loginButton = new Button(this, SWT.PUSH);
		loginButton.setText("Login");
		loginButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					Login.getLogin();
				} catch (LoginException e1) {
					// nevermind if failed or working offline ...
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	
}
