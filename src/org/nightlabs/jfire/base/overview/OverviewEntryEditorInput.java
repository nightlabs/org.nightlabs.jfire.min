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
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj == null)
			return false;
		
		if (!(obj instanceof OverviewEntryEditorInput))
			return false;
	
		OverviewEntryEditorInput input = (OverviewEntryEditorInput) obj;
		if (entryViewController.equals(input.getEntryViewController()))
			return true;
		
		return false;
	}
		
}
