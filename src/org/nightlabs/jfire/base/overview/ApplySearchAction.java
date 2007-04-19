package org.nightlabs.jfire.base.overview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.nightlabs.base.resource.SharedImages;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ApplySearchAction 
extends Action 
{
	public static final String ID = ApplySearchAction.class.getName(); 
	private SearchEntryViewController searchController;
	
	public ApplySearchAction(SearchEntryViewController searchController) {
		super();
		setId(ID);
		setText("Apply Search");
		setImageDescriptor(SharedImages.SEARCH_24x24);
		this.searchController = searchController;
	}

	@Override
	public void run() 
	{
		if (searchController != null)
			searchController.applySearch();
	}
	
}
