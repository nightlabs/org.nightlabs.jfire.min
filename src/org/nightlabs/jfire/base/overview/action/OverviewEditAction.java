package org.nightlabs.jfire.base.overview.action;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.nightlabs.base.action.SelectionAction;
import org.nightlabs.base.util.RCPUtil;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class OverviewEditAction 
extends SelectionAction 
{
	public OverviewEditAction() {
	}

	/**
	 * @param text
	 */
	public OverviewEditAction(String text) {
		super(text);
	}

	/**
	 * @param text
	 * @param image
	 */
	public OverviewEditAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * @param text
	 * @param style
	 */
	public OverviewEditAction(String text, int style) {
		super(text, style);
	}

	public abstract IEditorInput getEditorInput();
	
	public abstract String getEditorID();

	@Override
	public void run() {
		try {
			RCPUtil.openEditor(getEditorInput(), getEditorID());
		} catch (PartInitException e) {
			throw new RuntimeException(e);
		}
	}
		
}
