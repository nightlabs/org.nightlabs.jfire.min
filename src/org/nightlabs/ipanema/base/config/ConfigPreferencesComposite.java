/*
 * Created 	on May 31, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.nightlabs.base.composite.XComposite;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferencesComposite extends XComposite {

	private XComposite wrapper;
	private XComposite noEditGUIComposite;
	
	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 */
	public ConfigPreferencesComposite(Composite parent, int style,
			boolean setLayoutData) {
		super(parent, style, 
				XComposite.LAYOUT_MODE_TIGHT_WRAPPER, 
				setLayoutData ? XComposite.LAYOUT_DATA_MODE_GRID_DATA : XComposite.LAYOUT_DATA_MODE_NONE
			);
		createWrapper();
		createNoEditGUI();
		getStackLayout().topControl = noEditGUIComposite;
	}
	
	protected void createWrapper() {
		wrapper = new XComposite(this, SWT.NONE, XComposite.LAYOUT_MODE_TIGHT_WRAPPER);
		wrapper.setLayout(new StackLayout());
	}
	
	protected void createNoEditGUI() {
		noEditGUIComposite = new XComposite(wrapper, SWT.NONE, XComposite.LAYOUT_MODE_TIGHT_WRAPPER);
		Label label = new Label(noEditGUIComposite, SWT.WRAP);
		label.setLayoutData(new GridData());
		label.setText("This ConfigModule has no PreferencePage registered to edit it!");
	}
	
	public void clear(){
		wrapper.dispose();
		wrapper = null;
		createWrapper();
	}
	
	public Composite getWrapper() {
		return wrapper;
	}
	
	public StackLayout getStackLayout() {
		return (StackLayout)wrapper.getLayout();
	}
	
	public void setNoEditGUI() {
		getStackLayout().topControl = noEditGUIComposite;
		wrapper.layout();
	}
	
}
