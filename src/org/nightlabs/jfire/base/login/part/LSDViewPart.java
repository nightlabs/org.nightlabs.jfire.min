package org.nightlabs.jfire.base.login.part;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.nightlabs.base.part.ControllablePart;
import org.nightlabs.base.part.PartVisibilityListener;
import org.nightlabs.base.part.PartVisibilityTracker;
import org.nightlabs.jfire.base.login.Login;

/**
 * Extend this Class if you want to write an LoginStateDepenend View, instead of
 * implementing the Method createPartControl(Composite parent) you need to implement
 * the createPartContents(Composite parent) defined by the interface {@link ControllablePart}
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class LSDViewPart 
extends ViewPart 
implements PartVisibilityListener, ControllablePart 
{
	public LSDViewPart() {
		super();
		// Register the view at the view-controller
		LSDPartController.sharedInstance().registerPart(this, new FillLayout());		
	}
	
	@Override
	public void createPartControl(Composite parent) {
    // Delegate this to the view-controller, to let him decide what to display
    LSDPartController.sharedInstance().createPartControl(this, parent);
    // Add this view as visibility listener (optional)
    PartVisibilityTracker.sharedInstance().addVisibilityListener(this, this);
	}

	@Override
	public void setFocus() {

	}

	public void partHidden(IWorkbenchPartReference partRef) {
		// Inheritans can override this method if they want/need to react on hidden status 
	}

	public void partVisible(IWorkbenchPartReference partRef) {
		// Inheritans can override this method if they want/need to react on visible status
	}

	public boolean canDisplayPart() {
    // This view can be displayed only when
    return Login.isLoggedIn();
	}
}
