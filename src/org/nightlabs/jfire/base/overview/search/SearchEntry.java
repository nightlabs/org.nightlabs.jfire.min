package org.nightlabs.jfire.base.overview.search;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.jfire.base.overview.AbstractEntry;
import org.nightlabs.jfire.base.overview.Entry;
import org.nightlabs.jfire.base.overview.EntryFactory;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class SearchEntry
extends AbstractEntry
implements Entry 
{	
	public SearchEntry(EntryFactory entryFactory) {
		super(entryFactory);
	}
	
	private Composite searchComposite;
	public Composite getSearchComposite() {
		return searchComposite;
	}
	
	private Composite resultComposite;
	public Composite getResultComposite() {
		return resultComposite;
	}
	
	private SashForm sashform = null;
	public Composite createEntryComposite(Composite parent) 
	{		 		 
		sashform = new SashForm(parent, SWT.VERTICAL);		
		sashform.setLayout(new FillLayout());
		searchComposite = createSearchComposite(sashform);
		resultComposite = createResultComposite(sashform);
		configureSash(sashform);
		
		if (parent.getLayout() instanceof GridLayout)
			sashform.setLayoutData(new GridData(GridData.FILL_BOTH));
		
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
		
		if (!(obj instanceof Entry))
				return false;
		
		Entry controller = (Entry) obj;
		if (controller == null)
			return false;
		
		if (controller.getID() == null)
			return false;
		
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
	
	protected void configureSash(SashForm sashform) {
		sashform.setWeights(new int[] {2,4});		
	}
	
	public Composite createCategoryEntryComposite(Composite parent) {
		throw new UnsupportedOperationException("SearchEntry does not support custom Composites for the Category.");
	}
	
}
