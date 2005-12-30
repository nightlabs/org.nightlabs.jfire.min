/*
 * Created 	on Dec 9, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.nightlabs.base.composite.XComposite;


/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonManagementPreferencePage 
extends 
	PreferencePage 
implements
	IWorkbenchPreferencePage
{
	
	private XComposite wrapperComposite;
	
	public PersonManagementPreferencePage() {
		super();
		System.out.println("Constuctor of PersonManagementPreferencePage was called");
	}
	
  protected Control createContents(Composite parent) {
  	createWrapperComposite(parent);
  	Label label = new Label(wrapperComposite,SWT.PUSH);
  	label.setText("Test label for person management pref page.");
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
  

}
