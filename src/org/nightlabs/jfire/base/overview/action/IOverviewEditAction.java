package org.nightlabs.jfire.base.overview.action;

import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.action.ISelectionAction;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface IOverviewEditAction 
extends ISelectionAction 
{
	IEditorInput getEditorInput();	
	String getEditorID();
}
