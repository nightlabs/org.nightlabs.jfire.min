package org.nightlabs.jfire.base.overview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.nightlabs.jfire.base.overview.search.ApplySearchAction;
import org.nightlabs.jfire.base.overview.search.SearchEntryViewer;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewEntryEditorActionBarContributor  
extends EditorActionBarContributor
{
	public OverviewEntryEditorActionBarContributor() {
		super();
	}
	
	@Override
	public void contributeToCoolBar(ICoolBarManager coolBarManager) 
	{
		if (getApplySearchAction() != null)
			coolBarManager.add(getApplySearchAction());
	}

	@Override
	public void contributeToMenu(IMenuManager menuManager) 
	{
		if (getApplySearchAction() != null)
			menuManager.add(getApplySearchAction());
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) 
	{
		if (getApplySearchAction() != null)
			toolBarManager.add(getApplySearchAction());
	}

	private OverviewEntryEditor editor;
	protected OverviewEntryEditor getEditor() {
		return editor;
	}
	
  public void setActiveEditor(IEditorPart targetEditor) 
  {
  	if (targetEditor instanceof OverviewEntryEditor) {
  		editor = (OverviewEntryEditor) targetEditor;
  		
  		if (!alreadyContributed) {
  			contributeToToolBar(getActionBars().getToolBarManager());
  			alreadyContributed = true;
  		}
  	}
  }
  
  protected boolean alreadyContributed = false;
  
  protected EntryViewer getEntryViewer() 
  {
  	if (editor != null)
  		return editor.getEntryViewer();
  	return null;
  }
  
  private ApplySearchAction applySearchAction;
  public Action getApplySearchAction() {
  	if (applySearchAction == null && getEntryViewer() instanceof SearchEntryViewer) {
  		applySearchAction = new ApplySearchAction((SearchEntryViewer) getEntryViewer());
  	}
  	return applySearchAction;
  }
}
