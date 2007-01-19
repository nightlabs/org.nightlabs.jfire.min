package org.nightlabs.jfire.base.prop.structedit.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.nightlabs.jfire.base.prop.structedit.StructureChangedListener;
import org.nightlabs.jfire.base.prop.structedit.StructEditor;
import org.nightlabs.jfire.base.prop.structedit.StructEditorView;

public class StoreStructure implements IViewActionDelegate {
	private StructEditorView structEditView;
	private IAction action;
	
	public void init(IViewPart view) {
		structEditView = (StructEditorView) view;
		StructEditor editor = structEditView.getStructEditor();
		editor.addStructureChangedListener(new StructureChangedListener() {
			public void structureChanged() {
				if (action != null)
					action.setEnabled(true);
			}
		});
	}

	public void run(IAction action) {
		structEditView.getStructEditor().storeStructure();
		action.setEnabled(false);
	}

	public void selectionChanged(IAction action, ISelection selection) {		
		if (this.action == null) {			
			this.action = action;
			action.setEnabled(false);
		}
	}	
}
