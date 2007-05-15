package org.nightlabs.jfire.base.overview;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.nightlabs.base.action.registry.editor.XEditorActionBarContributor;
import org.nightlabs.jfire.base.login.part.LSDEditorPart;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewEntryEditor 
//extends EditorPart 
extends LSDEditorPart
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
			entry = entryInput.getEntryViewController();
		}
		getSite().getPage().addPartListener(partListener);			
	}
	
	private Entry entry;
	public Entry getEntryViewController() {
		return entry;
	}
	
	private Composite composite;
	public void createPartContents(Composite parent) 
	{
		if (entry != null) {
			composite = entry.createComposite(parent);
			if (entry.getSelectionProvider() != null)
				getSite().setSelectionProvider(entry.getSelectionProvider());
			
			updateContextMenu();
		}		
	}
		
	@Override
	public void setFocus() {
		if (composite != null && !composite.isDisposed())		
			composite.setFocus();
	}
	
	protected void updateContextMenu() 
	{
		EditorActionBarContributor actionBarContributor = 
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null && entry != null) {
			MenuManager menuManager = entry.getMenuManager();
			if (menuManager != null) {
				if (actionBarContributor instanceof XEditorActionBarContributor) {
					XEditorActionBarContributor xEditorActionBarContributor = (XEditorActionBarContributor) actionBarContributor;
					xEditorActionBarContributor.getActionRegistry().contributeToContextMenu(menuManager);
					if (logger.isDebugEnabled())
						logger.debug("updateContextMenu, Number of entries = "+menuManager.getItems().length+", actionBarContributor = "+actionBarContributor);
				} else {
					actionBarContributor.contributeToMenu(menuManager);
				}
			}				
		}		
	}
	
	protected void removeContextMenu() 
	{
		EditorActionBarContributor actionBarContributor = 
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null && entry != null) {
			MenuManager menuManager = entry.getMenuManager();
			if (menuManager != null) {
				menuManager.removeAll();
				menuManager.updateAll(true);
				if (logger.isDebugEnabled())
					logger.debug("removeContextMenu, Number of entries = "+menuManager.getItems().length+", actionBarContributor = "+actionBarContributor);
			}							
		}
	}
			
	private IPartListener partListener = new IPartListener(){	
		public void partOpened(IWorkbenchPart part) {			
		}	
		public void partDeactivated(IWorkbenchPart part) {		
			removeContextMenu();
		}	
		public void partClosed(IWorkbenchPart part) {
			editorDisposed();
		}	
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partActivated(IWorkbenchPart part) {
			updateContextMenu();
		}	
	};
	
//	private DisposeListener disposeListener = new DisposeListener(){	
//		public void widgetDisposed(DisposeEvent e) {
//			getSite().getPage().removePartListener(partListener);
//		}	
//	};
	
	protected void editorDisposed() {
		getSite().getPage().removePartListener(partListener);
		if (entry != null)
			getSite().setSelectionProvider(entry.getSelectionProvider());		
	}
}
