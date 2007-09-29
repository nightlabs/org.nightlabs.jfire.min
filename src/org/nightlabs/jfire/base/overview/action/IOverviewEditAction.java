package org.nightlabs.jfire.base.overview.action;

import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.action.ISelectionAction;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface IOverviewEditAction 
extends ISelectionAction 
{
	/**
	 * returns the {@link IEditorInput}
	 * @return the input of the editor
	 */
	IEditorInput getEditorInput();	
	
	/**
	 * return the id of the editor to edit
	 * @return the id of the editor to edit
	 */
	String getEditorID();
}
