package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.nightlabs.base.notification.IDirtyStateManager;
import org.nightlabs.base.part.ControllablePart;
import org.nightlabs.base.part.PartVisibilityListener;
import org.nightlabs.base.part.PartVisibilityTracker;
import org.nightlabs.base.selection.SelectionProviderProxy;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.part.LSDPartController;

public class StructEditorView
extends ViewPart
implements PartVisibilityListener, ControllablePart, IDirtyStateManager {

	private StructEditorComposite structEditComposite;
	private StructEditor structEditor;
	private SelectionProviderProxy selectionProviderProxy;

	public StructEditorView() {
		// Register the view at the view-controller
		LSDPartController.sharedInstance().registerPart(this);
		
		structEditor = new StructEditor(this);		
	}

	@Override
	public void createPartControl(Composite parent) {
		// Delegate this to the view-controller, to let him decide what to display
		LSDPartController.sharedInstance().createPartControl(this, parent);

		// Add this view as visibility listener (optional)
		PartVisibilityTracker.sharedInstance().addVisibilityListener(this, this);
		
		selectionProviderProxy = new SelectionProviderProxy();
		getViewSite().setSelectionProvider(selectionProviderProxy);
		
		// ugly hack to update view actions
		selectionProviderProxy.clearSelection();
	}

	@Override
	public void setFocus() {
	}

	public StructEditor getStructEditor() {
		return structEditor;
	}

	public void partHidden(IWorkbenchPartReference partRef) {
		if (structEditComposite != null)
			selectionProviderProxy.removeRealSelectionProvider(structEditor.getStructTree().getTreeViewer());
	}

	public void partVisible(IWorkbenchPartReference partRef) {
		if (structEditComposite != null)
			selectionProviderProxy.addRealSelectionProvider(structEditor.getStructTree().getTreeViewer());
	}

	public boolean canDisplayPart() {
		return Login.isLoggedIn();
	}

	public void createPartContents(Composite parent) {
		structEditComposite = structEditor.createComposite(parent, parent.getStyle());
//		structEditor.setCurrentStructLocalID(structLocalID)
		getViewSite().setSelectionProvider(structEditor.getStructTree().getTreeViewer());
	}
	
	public void triggerSiteSelection() {
		selectionProviderProxy.clearSelection();
	}

	public boolean isDirty() {
		return false;
	}

	public void markDirty() {
		triggerSiteSelection();
	}

	public void markUndirty() {
	}
	
}
