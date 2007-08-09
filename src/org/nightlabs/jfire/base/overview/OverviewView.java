package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class OverviewView 
extends ViewPart 
{
	public OverviewView() {
		super();
	}

	private OverviewShelf overviewShelf;
	@Override
	public void createPartControl(Composite parent) {
		overviewShelf = new OverviewShelf(parent, SWT.NONE) {
			@Override
			protected OverviewRegistry getOverviewRegistry() {
				return OverviewView.this.getOverviewRegistry();
			}
		};
	}

	@Override
	public void setFocus() {
		overviewShelf.setFocus();
	}

	protected abstract OverviewRegistry getOverviewRegistry();
}
