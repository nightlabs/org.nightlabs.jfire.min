/**
 * 
 */
package org.nightlabs.jfire.base.prop.structedit.action;

import org.nightlabs.base.action.SelectionAction;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.jfire.base.prop.structedit.StructEditor;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;

/**
 * Simple Action to remove a {@link Struct} element; a {@link StructBlock} or a {@link StructField}.
 * 
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 */
public class RemoveStructElementAction extends SelectionAction {

	private StructEditor editor;
	
	public RemoveStructElementAction(StructEditor editor) {
		super("Delete the selected Block/Field", SharedImages.DELETE_16x16); // TODO: needs an own icon!
		this.editor = editor;
		setToolTipText("Removes the currently selected Field/Block");
	}
	
	@Override
	public void run() {
		if (editor.getStructTree().getSelectedNode() == null)
			return;
		
		editor.removeSelectedItem();
	}

	public boolean calculateEnabled() {
		return true;
	}

	public boolean calculateVisible() {
		return true;
	}
	
}
