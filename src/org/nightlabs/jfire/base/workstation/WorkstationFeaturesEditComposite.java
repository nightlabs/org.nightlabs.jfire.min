package org.nightlabs.jfire.base.workstation;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.base.config.AbstractConfigModulePreferencePage;
import org.nightlabs.jfire.base.config.ConfigPreferenceChangedListener;

public class WorkstationFeaturesEditComposite extends XComposite implements
		ConfigPreferenceChangedListener {
	
	public WorkstationFeaturesEditComposite(Composite parent, int style, boolean setLayoutData)
	{
		super(parent, style, LayoutMode.TIGHT_WRAPPER,
				setLayoutData ? LayoutDataMode.GRID_DATA : LayoutDataMode.NONE);

		// TODO: Das Composite mit GUI Elementen füllen.
	}
	

	public void configPreferenceChanged(
			AbstractConfigModulePreferencePage preferencePage) {
		// TODO Auto-generated method stub

	}

}
