package org.nightlabs.jfire.base.overview.search;

import org.eclipse.ui.IWorkbenchPart;
import org.nightlabs.base.action.WorkbenchPartAction;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.jfire.base.overview.OverviewEntryEditor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ApplySearchAction 
//extends Action 
extends WorkbenchPartAction
{
	public static final String ID = ApplySearchAction.class.getName(); 
	private SearchEntryViewer searchViewer;

	public ApplySearchAction() {
		super();
		init();
	}	
	
	public ApplySearchAction(SearchEntryViewer searchController) {
		super();
		init();
		this.searchViewer = searchController;
	}

	protected void init() {
		setId(ID);
		setText("Apply Search");
		setImageDescriptor(SharedImages.SEARCH_24x24);		
	}
	
	@Override
	public void run() 
	{
		if (searchViewer != null)
			searchViewer.applySearch();
	}

	public boolean calculateEnabled() {
		return searchViewer != null;
	}

	public boolean calculateVisible() {
		return true;
	}

	@Override
	public void setActivePart(IWorkbenchPart part) {
		super.setActivePart(part);
		if (part instanceof OverviewEntryEditor) {
			OverviewEntryEditor editor = (OverviewEntryEditor) part;
			if (editor.getEntryViewer() instanceof SearchEntryViewer) {
				this.searchViewer = (SearchEntryViewer) editor.getEntryViewer();
			}
		}
	}
			
}
