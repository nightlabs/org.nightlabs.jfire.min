/**
 * 
 */
package org.nightlabs.jfire.base.prop.structedit.action;

import org.nightlabs.base.action.SelectionAction;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.jfire.base.prop.structedit.StructBlockNode;
import org.nightlabs.jfire.base.prop.structedit.StructEditor;
import org.nightlabs.jfire.base.prop.structedit.StructFieldNode;
import org.nightlabs.jfire.base.prop.structedit.TreeNode;

/**
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 */
public class AddStructFieldAction extends SelectionAction {

	private StructEditor editor;
	
	public AddStructFieldAction(StructEditor editor) {
		super("Add a StructField", SharedImages.ADD_16x16); // TODO: needs an own icon!
		this.editor = editor;
		setToolTipText("Add a new StructField to the currently selected StructBlock");
	}
	
	@Override
	public void run() {
		TreeNode selected = editor.getStructTree().getSelectedNode();
		if (selected == null)
			return;
		
		if (selected instanceof StructBlockNode) {
			final StructBlockNode blockNode = (StructBlockNode) selected;
			editor.addStructField(blockNode.getBlock());
			
		} else if (selected instanceof StructFieldNode) {
			final StructFieldNode fieldNode = (StructFieldNode) selected;
			editor.addStructField(fieldNode.getParentBlock().getBlock());
		}

	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.action.IUpdateActionOrContributionItem#calculateEnabled()
	 */
	public boolean calculateEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.action.IUpdateActionOrContributionItem#calculateVisible()
	 */
	public boolean calculateVisible() {
		return true;
	}

}
