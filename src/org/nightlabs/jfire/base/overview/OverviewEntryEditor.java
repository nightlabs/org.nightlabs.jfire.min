package org.nightlabs.jfire.base.overview;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
 * Editor displaying the {@link EntryViewer} of an {@link Entry}.
 * It therefore requires an {@link OverviewEntryEditorInput} as input.
 * Each editor instance will create its own {@link EntryViewer} instance.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class OverviewEntryEditor 
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
			entryViewer = entryInput.getEntry().createEntryViewer();
		}
		getSite().getPage().addPartListener(partListener);			
	}
	
	private EntryViewer entryViewer;
	public EntryViewer getEntryViewer() {
		return entryViewer;
	}
	
	private Composite composite;
	public void createPartContents(Composite parent) 
	{
		if (entryViewer != null) {
			composite = entryViewer.createComposite(parent);
			if (entryViewer.getSelectionProvider() != null) {
				getSite().setSelectionProvider(entryViewer.getSelectionProvider());
				entryViewer.getSelectionProvider().addSelectionChangedListener(selectionChangedListener);
			}
			
			if (getEditorSite().getActionBarContributor() != null && 
					getEditorSite().getActionBarContributor() instanceof XEditorActionBarContributor) 
			{
				XEditorActionBarContributor editorActionBarContributor = 
					(XEditorActionBarContributor) getEditorSite().getActionBarContributor();
				addSelectionChangedListener(editorActionBarContributor);
			}
			
			updateContextMenu();
			updateToolbar();
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
		if (actionBarContributor != null && entryViewer != null) {
			MenuManager menuManager = entryViewer.getMenuManager();
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
		if (actionBarContributor != null && entryViewer != null) {
			MenuManager menuManager = entryViewer.getMenuManager();
			if (menuManager != null) {
				menuManager.removeAll();
				menuManager.updateAll(true);
				if (logger.isDebugEnabled())
					logger.debug("removeContextMenu, Number of entries = "+menuManager.getItems().length+", actionBarContributor = "+actionBarContributor);
			}							
		}
	}

	protected void updateToolbar() 
	{
		EditorActionBarContributor actionBarContributor = 
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null && entryViewer != null) {
			ToolBarManager toolbarManager = entryViewer.getToolBarManager();
			if (toolbarManager != null) {
				if (actionBarContributor instanceof XEditorActionBarContributor) {
					XEditorActionBarContributor xEditorActionBarContributor = (XEditorActionBarContributor) actionBarContributor;
					xEditorActionBarContributor.getActionRegistry().contributeToToolBar(toolbarManager);
					if (logger.isDebugEnabled())
						logger.debug("updateToolbar, Number of entries = "+toolbarManager.getItems().length+", actionBarContributor = "+actionBarContributor);
				} else {
					actionBarContributor.contributeToToolBar(toolbarManager);
				}
			}				
		}		
	}
	
	protected void removeToolbar() 
	{
		EditorActionBarContributor actionBarContributor = 
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null && entryViewer != null) {
			ToolBarManager toolbarManager = entryViewer.getToolBarManager();
			if (toolbarManager != null) {
				toolbarManager.removeAll();
				toolbarManager.update(true);
				if (logger.isDebugEnabled())
					logger.debug("removeToolbar, Number of entries = "+toolbarManager.getItems().length+", actionBarContributor = "+actionBarContributor);
			}							
		}
	}	
	
	private IPartListener partListener = new IPartListener(){	
		public void partOpened(IWorkbenchPart part) {			
		}	
		public void partDeactivated(IWorkbenchPart part) {		
			removeContextMenu();
			removeToolbar();
		}	
		public void partClosed(IWorkbenchPart part) {
			editorDisposed();
		}	
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partActivated(IWorkbenchPart part) {
			updateContextMenu();
			updateToolbar();
		}	
	};
		
	protected void editorDisposed() {
		getSite().getPage().removePartListener(partListener);
		if (entryViewer != null)
			getSite().setSelectionProvider(entryViewer.getSelectionProvider());		
	}
	
	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener(){
		public void selectionChanged(SelectionChangedEvent event) {
			logger.debug("selection changed "+event.getSelection());
			fireSelectionChanged(event);
		}
	};
	
	private ListenerList listeners = new ListenerList();
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}	
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}
	protected void fireSelectionChanged(SelectionChangedEvent event) {
		for (int i=0; i<listeners.size(); i++) {
			ISelectionChangedListener listener = (ISelectionChangedListener) listeners.getListeners()[i];
			listener.selectionChanged(event);
		}
	}
}
