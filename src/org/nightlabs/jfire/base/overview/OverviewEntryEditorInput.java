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

	public OverviewEntryEditorInput(Entry entry) {
		this.entry = entry;
	}

	private Entry entry;
	public Entry getEntry() {
		return entry;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj == null)
			return false;
		
		if (!(obj instanceof OverviewEntryEditorInput))
			return false;
	
		OverviewEntryEditorInput input = (OverviewEntryEditorInput) obj;
		if (entry.getEntryFactory().getID().equals(input.getEntry().getEntryFactory().getID()))
			return true;
		
		return false;
	}
		
}
