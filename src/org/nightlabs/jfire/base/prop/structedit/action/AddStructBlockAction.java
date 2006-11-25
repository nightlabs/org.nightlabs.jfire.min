package org.nightlabs.jfire.base.prop.structedit.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.nightlabs.jfire.base.prop.structedit.StructEditorView;

public class AddStructBlockAction implements IViewActionDelegate {
	private StructEditorView structEditView;

	public void init(IViewPart view) {
		structEditView = (StructEditorView) view;
	}

	public void run(IAction action) {
		structEditView.getStructEditComposite().addStructBlock();		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (structEditView.getStructEditor() == null || !structEditView.getStructEditor().hasStructureLoaded())
			action.setEnabled(false);
		else
			action.setEnabled(true);
	}
}
