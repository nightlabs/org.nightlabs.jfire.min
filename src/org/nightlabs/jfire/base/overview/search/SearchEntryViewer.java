package org.nightlabs.jfire.base.overview.search;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.base.form.NightlabsFormsToolkit;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.jfire.base.overview.AbstractEntryViewer;
import org.nightlabs.jfire.base.overview.Entry;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class SearchEntryViewer
extends AbstractEntryViewer
{	
	public SearchEntryViewer(Entry entry) {
		super(entry);
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
	public Composite createComposite(Composite parent) 
	{		 		 
		sashform = new SashForm(parent, SWT.VERTICAL);		
		sashform.setLayout(new FillLayout());
		searchComposite = createSearchComposite(sashform);
		createToolBar(searchComposite);
		resultComposite = createResultComposite(sashform);
		configureSash(sashform);
		
		if (parent.getLayout() instanceof GridLayout)
			sashform.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Form Look & Feel
		if (searchComposite instanceof XComposite) {
			((XComposite)searchComposite).setToolkit(new NightlabsFormsToolkit(Display.getDefault()));
			((XComposite)searchComposite).adaptToToolkit();
		}
				
		// Context Menu
		menuManager = new MenuManager();		
		Menu contextMenu = menuManager.createContextMenu(parent); 
		searchComposite.setMenu(contextMenu);
		resultComposite.setMenu(contextMenu);
				
		return sashform;
	}

	public Composite getComposite() {
		if (sashform == null)
			throw new IllegalStateException("createComposite() was not called before getComposite()!"); //$NON-NLS-1$
		return sashform;
	}
		
	private ToolBarManager toolBarManager = null;
	public ToolBarManager getToolBarManager() {
		return toolBarManager;
	}
	
	protected void createToolBar(final Composite searchComposite) 
	{
		Label separator = new Label(searchComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		Composite wrapper = new XComposite(searchComposite, SWT.NONE, 
				LayoutMode.TOTAL_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label spacer = new Label(wrapper, SWT.NONE);
		spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBar toolBar = new ToolBar(wrapper, SWT.NONE);
		toolBarManager = new ToolBarManager(toolBar);		
	}
		
	public abstract Composite createSearchComposite(Composite parent);	
	public abstract Composite createResultComposite(Composite parent);
	public abstract void applySearch();
	
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
//		sashform.setWeights(new int[] {2,4});
		sashform.setWeights(new int[] {5,8});
	}
	
	public Composite createCategoryEntryComposite(Composite parent) {
		throw new UnsupportedOperationException("SearchEntryViewer does not support custom Composites for the Category."); //$NON-NLS-1$
	}
	
}
