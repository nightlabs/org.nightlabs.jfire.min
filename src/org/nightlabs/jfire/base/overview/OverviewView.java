package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewView 
extends ViewPart 
{
	public static final String VIEW_ID = OverviewView.class.getName();
	
	public OverviewView() {
		super();
	}

	private OverviewShelf overviewShelf;
	@Override
	public void createPartControl(Composite parent) {
		overviewShelf = new OverviewShelf(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
		overviewShelf.setFocus();
	}

}
