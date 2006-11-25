package org.nightlabs.jfire.base.prop.structedit.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.nightlabs.jfire.base.prop.structedit.StructBlockNode;
import org.nightlabs.jfire.base.prop.structedit.StructEditorView;
import org.nightlabs.jfire.base.prop.structedit.StructFieldNode;
import org.nightlabs.jfire.base.prop.structedit.TreeNode;
import org.nightlabs.jfire.prop.StructBlock;

public class AddStructFieldAction implements IViewActionDelegate {
	private StructEditorView structEditView;

	public void init(IViewPart view) {
		structEditView = (StructEditorView) view;

	}

	public void run(IAction action) {
		if (structEditView.getStructEditor().getSelection().isEmpty())
			return;
		
		structEditView.getStructEditor().addStructField(
				getSelectedStructBlockNode().getBlock()
			);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection.isEmpty())
			action.setEnabled(false);
		else
			action.setEnabled(true);
	}

	public StructBlockNode getSelectedStructBlockNode() {
		TreeNode current = (TreeNode) ((IStructuredSelection) structEditView.getStructEditor().getSelection()).getFirstElement();
		if (current instanceof StructBlockNode)
			return (StructBlockNode) current;
		else if (current instanceof StructFieldNode)
			return ((StructFieldNode) current).getParentBlock();
		return null;
		
	}
	
}
