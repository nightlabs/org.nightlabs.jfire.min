package org.nightlabs.jfire.base.overview;

import org.eclipse.ui.internal.part.NullEditorInput;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewEntryEditorInput 
//implements IEditorInput 
extends NullEditorInput
{

	public OverviewEntryEditorInput(EntryViewController entryViewController) {
		this.entryViewController = entryViewController;
	}

	private EntryViewController entryViewController;
	public EntryViewController getEntryViewController() {
		return entryViewController;
	}
	
}
