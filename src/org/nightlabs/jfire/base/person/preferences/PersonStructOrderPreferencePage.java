/*
 * Created 	on Dec 9, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.preferences;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.login.Login;


/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonStructOrderPreferencePage 
extends 
	PreferencePage 
implements
	IWorkbenchPreferencePage
{
	private XComposite wrapperComposite;
	private PersonStructOrderComposite structOrderComposite;
	
	public PersonStructOrderPreferencePage() {
		super();
		System.out.println("Constructor of PersonStructOrderPreferencePage called");
	}
	
  protected Control createContents(Composite parent) {
  	createWrapperComposite(parent);
  	
		Login login;
		try {
			login = Login.getLogin();
			structOrderComposite = new PersonStructOrderComposite(wrapperComposite,SWT.NONE);
		} catch (LoginException e) {
			Composite tmpComp = new Composite(wrapperComposite,SWT.BORDER);
			Label tmpLabel = new Label(tmpComp,SWT.NONE);
			tmpLabel.setText(JFireBasePlugin.getResourceString("person.preferences.structorder.labels.error.notloggedin"));
		}
  	
  	return wrapperComposite;		
  }
  
  protected void createWrapperComposite(Composite parent) {
  	if (wrapperComposite == null)
  		wrapperComposite = new XComposite(parent, SWT.NONE, XComposite.LAYOUT_MODE_TIGHT_WRAPPER); 	
  }

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		
	}
	

	protected void performApply() {
		super.performApply();
		PersonStructOrderConfigModule.sharedInstance().setStructBlockDisplayOrder(
				structOrderComposite.getStructBlockOrder()
			);		
	}
}
