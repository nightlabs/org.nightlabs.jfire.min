package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.ComboComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.StructLocalID;

public class PropertySelectionComposite extends XComposite {

	private ComboComposite<StructLocalID> structLocalIDs;
	private ComboComposite<PropertySet> availableProperties;
	private PropertyManager propertyManager;
	
	public PropertySelectionComposite(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new GridLayout(2, true));
		
		structLocalIDs = new ComboComposite<StructLocalID>(this, SWT.NONE);
		availableProperties = new ComboComposite<PropertySet>(this, SWT.NONE);
		
		propertyManager = getPropertyManager();
		
		try {
			structLocalIDs.setInput(propertyManager.getAvailableStructLocalIDs());
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		structLocalIDs.getCombo().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				updateAvailableProperties();
			}
		});
	}
	
	private void updateAvailableProperties() {
		StructLocalID structLocalID = structLocalIDs.getSelectedElement();
	}
	
	private PropertyManager getPropertyManager() {
		if (propertyManager == null) {
			try {
				propertyManager = PropertyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return propertyManager;
	}
}
