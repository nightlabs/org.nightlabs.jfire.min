/**
 * 
 */
package org.nightlabs.jfire.base.prop.structedit.action;

import org.nightlabs.base.action.SelectionAction;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.jfire.base.prop.structedit.StructEditor;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;

/**
 * Adds a {@link StructBlock} to the {@link Struct} edited in the {@link StructEditor}.
 * 
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 */
public class AddStructBlockAction extends SelectionAction {
	
	private StructEditor editor;
	
	public AddStructBlockAction(StructEditor editor) {
		super(
				Messages.getString("org.nightlabs.jfire.base.prop.structedit.action.AddStructBlockAction.text"), //$NON-NLS-1$
				SharedImages.ADD_16x16); // TODO: needs an own icon!
		this.editor = editor;
		setToolTipText(Messages.getString("org.nightlabs.jfire.base.prop.structedit.action.AddStructBlockAction.toolTipText")); //$NON-NLS-1$
	}
	
	@Override
	public void run() {
		editor.addStructBlock();
	}

	public boolean calculateEnabled() {
		return true;
	}

	public boolean calculateVisible() {
		return true;
	}
}
