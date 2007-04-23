package org.nightlabs.jfire.base.overview;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.table.AbstractTableComposite;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class SearchEntryViewController
implements EntryViewController 
{	
	public SearchEntryViewController() {
		super();
	}
	
	private Composite searchComposite;
	public Composite getSearchComposite() {
		return searchComposite;
	}
	
	private Composite resultComposite;
	public Composite getResultComposite() {
		return resultComposite;
	}
	
	public Composite createComposite(Composite parent) 
	{		 		
		SashForm sashform = new SashForm(parent, SWT.VERTICAL);
		sashform.setLayout(new FillLayout());
		searchComposite = createSearchComposite(sashform);
		resultComposite = createResultComposite(sashform);
		sashform.setWeights(new int[] {1,3});
		
		// Form Look & Feel
		if (searchComposite instanceof XComposite) {
			((XComposite)searchComposite).setToolkit(new FormToolkit(Display.getDefault()));
			((XComposite)searchComposite).adaptToToolkit();
		}
				
		// Context Menu
		menuManager = new MenuManager();		
		Menu contextMenu = menuManager.createContextMenu(parent); 
		searchComposite.setMenu(contextMenu);
		resultComposite.setMenu(contextMenu);
		
		return sashform;
	}
	
	public abstract Composite createSearchComposite(Composite parent);	
	public abstract Composite createResultComposite(Composite parent);
	public abstract void applySearch();
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj == null)
			return false;
		
		if (!(obj instanceof EntryViewController))
				return false;
		
		EntryViewController controller = (EntryViewController) obj;
		if (controller.getID().equals(getID()))
				return true;
		
		return false;
	}

	private MenuManager menuManager;
	public MenuManager getMenuManager() {
		return menuManager;
	}
	
	public ISelectionProvider getSelectionProvider() {
		if (resultComposite instanceof AbstractTableComposite) {
			AbstractTableComposite tableComposite = (AbstractTableComposite) resultComposite;
			return tableComposite.getTableViewer();
		}
		return null;
	}		
	
	
}
