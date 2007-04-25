package org.nightlabs.jfire.base.overview;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.EditorPart;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewEntryEditor 
extends EditorPart 
{
	private static final Logger logger = Logger.getLogger(OverviewEntryEditor.class);
	
	public OverviewEntryEditor() {
		super();
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
	throws PartInitException 
	{
		setSite(site);
		setInput(input);
		if (input instanceof OverviewEntryEditorInput) {
			OverviewEntryEditorInput entryInput = (OverviewEntryEditorInput) input;
			entryViewController = entryInput.getEntryViewController();
		}
		getSite().getPage().addPartListener(partListener);		
	}
	
	private EntryViewController entryViewController;
	public EntryViewController getEntryViewController() {
		return entryViewController;
	}
	
	private Composite composite;
	@Override
	public void createPartControl(Composite parent) 
	{
		if (entryViewController != null) {
			composite = entryViewController.createComposite(parent);
//			updateContextMenu();
			if (entryViewController.getSelectionProvider() != null)
				getSite().setSelectionProvider(entryViewController.getSelectionProvider());
		}
	}

	protected void updateContextMenu() 
	{
		EditorActionBarContributor actionBarContributor = 
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null) {
			MenuManager menuManager = entryViewController.getMenuManager();
			if (menuManager != null) {
				actionBarContributor.contributeToMenu(menuManager);
				menuManager.updateAll(true);				
//				logger.info("updateContextMenu, Number of entries = "+menuManager.getItems().length);
			}				
		}		
	}
	
	protected void removeContextMenu() 
	{
		EditorActionBarContributor actionBarContributor = 
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null) {
			MenuManager menuManager = entryViewController.getMenuManager();
			if (menuManager != null) {
				menuManager.removeAll();
				menuManager.updateAll(true);				
				logger.info("updateContextMenu, Number of entries = "+menuManager.getItems().length);
			}							
		}
	}
	
	@Override
	public void setFocus() {
		if (composite != null && !composite.isDisposed())		
			composite.setFocus();
	}
		
	private IPartListener partListener = new IPartListener(){	
		public void partOpened(IWorkbenchPart part) {			
		}	
		public void partDeactivated(IWorkbenchPart part) {		
			removeContextMenu();
		}	
		public void partClosed(IWorkbenchPart part) {
		}	
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partActivated(IWorkbenchPart part) {
			updateContextMenu();
		}	
	};
	
}
