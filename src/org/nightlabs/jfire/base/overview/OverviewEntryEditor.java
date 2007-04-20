package org.nightlabs.jfire.base.overview;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
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
			EditorActionBarContributor actionBarContributor = 
				(EditorActionBarContributor) getEditorSite().getActionBarContributor();
			if (actionBarContributor != null) {
				MenuManager menuManager = new MenuManager();
				actionBarContributor.contributeToMenu(menuManager);
				Menu contextMenu = menuManager.createContextMenu(composite);
				composite.setMenu(contextMenu);
			}
		}
	}

	@Override
	public void setFocus() {
		if (composite != null && !composite.isDisposed())		
			composite.setFocus();
	}
		
}
