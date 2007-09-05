package org.nightlabs.jfire.base.overview.search;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.base.form.NightlabsFormsToolkit;
import org.nightlabs.base.job.Job;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.base.toolkit.IToolkit;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jfire.base.overview.AbstractEntryViewer;
import org.nightlabs.jfire.base.overview.Entry;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class SearchEntryViewer
extends AbstractEntryViewer
{	
	public static final Logger logger = Logger.getLogger(SearchEntryViewer.class);
	
	public SearchEntryViewer(Entry entry) {
		super(entry);
	}
		
	private XComposite searchWrapper = null;
	private SashForm sashform = null;
	private ScrolledComposite scrollComp = null;
	private ToolItem searchItem = null;
	private ToolBar searchTextToolBar = null;
	private Section searchCriteriaSection = null;
	public Composite createComposite(Composite parent) 
	{		 		 
		sashform = new SashForm(parent, SWT.VERTICAL);		
		
		searchWrapper = new XComposite(sashform, SWT.NONE, LayoutMode.TOP_BOTTOM_WRAPPER);
		searchWrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createToolBar(searchWrapper);
		IToolkit toolkit = new NightlabsFormsToolkit(Display.getDefault());
		searchCriteriaSection = toolkit.createSection(searchWrapper, Section.TITLE_BAR | Section.TWISTIE);
		searchCriteriaSection.setLayout(new GridLayout());
		searchCriteriaSection.setText("Advanced");
		searchCriteriaSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));	
		searchCriteriaSection.addExpansionListener(expansionListener);
		configureSection(searchCriteriaSection);
		
		scrollComp = new ScrolledComposite(searchCriteriaSection, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		scrollComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		scrollComp.setLayout(new GridLayout());
		searchComposite = createSearchComposite(scrollComp);
		searchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		scrollComp.setContent(searchComposite);		
		searchCriteriaSection.setClient(scrollComp);
		scrollComp.setMinSize(searchComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));		
		
		resultComposite = createResultComposite(sashform);
		
		if (parent.getLayout() instanceof GridLayout)
			sashform.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Form Look & Feel
		searchWrapper.setToolkit(toolkit);
		searchWrapper.adaptToToolkit();
				
		// Context Menu
		menuManager = new MenuManager();		
		Menu contextMenu = menuManager.createContextMenu(parent); 
		resultComposite.setMenu(contextMenu);
				
//		configureSash(sashform);
		sashform.setWeights(new int[] {1, 10});		
		
		configureQuickSearchEntries(searchItem);
		searchEntryType = getDefaultSearchEntryType();
		
		return sashform;
	}

//	protected abstract QuickSearchEntryType getDefaultSearchEntryType();
	/**
	 * Inheritans my override this method to return the default search entry type
	 * by default {@link QuickSearchEntryType} returned by {@link #getAdvancedQuickSearchEntryType()}
	 * is used 
	 */
	protected QuickSearchEntryType getDefaultSearchEntryType() {
		return getAdvancedQuickSearchEntryType();
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
	
	private Spinner limit;
	protected void createToolBar(final Composite searchComposite) 
	{		
		Composite toolBarWrapper = new XComposite(searchComposite, SWT.NONE, 
				LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		
		searchTextToolBar = new ToolBar(toolBarWrapper, SWT.NONE);
		searchTextToolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		XComposite wrapper = new XComposite(searchTextToolBar, SWT.NONE, 
				LayoutMode.TOP_BOTTOM_WRAPPER, LayoutDataMode.NONE, 2);
		GridLayout wrapperLayout = XComposite.getLayout(LayoutMode.TOP_BOTTOM_WRAPPER, 
				wrapper.getGridLayout(), 2);
		wrapperLayout.marginTop = 3;
		wrapper.setLayout(wrapperLayout);		
		Label searchLabel = new Label(wrapper, SWT.NONE);
		searchLabel.setText("Search");
		searchText = new Text(wrapper, SWT.BORDER);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		searchText.addSelectionListener(searchTextListener);
		ToolItem textItem = new ToolItem(searchTextToolBar, SWT.SEPARATOR);
		textItem.setControl(wrapper);
		textItem.setWidth(300);
		searchItem = new ToolItem(searchTextToolBar, SWT.DROP_DOWN);
		searchItem.setImage(SharedImages.SEARCH_16x16.createImage());			
		searchItem.addSelectionListener(searchItemListener);
		
		Composite rangeWrapper = new XComposite(searchTextToolBar, SWT.NONE, 
				LayoutMode.TOP_BOTTOM_WRAPPER, LayoutDataMode.NONE, 2);
		new Label(rangeWrapper, SWT.NONE).setText("Limit number of results (0 = unlimited): ");
		limit = new Spinner(rangeWrapper, SWT.BORDER);
		limit.setMinimum(0);
		limit.setMaximum(Integer.MAX_VALUE);
		limit.setSelection(00);
//		refreshButton = new Button(rangeWrapper, SWT.NONE);
////		refreshButton.setImage(SharedImages.getSharedImage(TradePlugin.getDefault(), ProductTransferFilterSection.class, "refreshButton"));
//		refreshButton.setText("Refresh");
//		refreshButton.setToolTipText("Apply the filter modifications and refresh the list of ProductTransfers.");		
		ToolItem rangeItem = new ToolItem(searchTextToolBar, SWT.SEPARATOR);
		rangeItem.setControl(rangeWrapper);
		rangeItem.setWidth(350);
		
		ToolBar toolBar = new ToolBar(toolBarWrapper, SWT.NONE);
		toolBarManager = new ToolBarManager(toolBar);	
	}
		
	protected Text searchText = null;
	
	public abstract Composite createSearchComposite(Composite parent);	
	public abstract Composite createResultComposite(Composite parent);
	
	private Composite searchComposite;
	public Composite getSearchComposite() {
		return searchComposite;
	}	
	private Composite resultComposite;
	public Composite getResultComposite() {
		return resultComposite;
	}	
	
//	public abstract void search();
	public void search() 
	{
		new Job(Messages.getString("org.nightlabs.jfire.base.overview.search.BaseSearchEntryViewer.job.name")){			 //$NON-NLS-1$
			@Override
			protected IStatus run(final ProgressMonitor monitor) 
			{
				if (searchEntryType != null) {
					Display.getDefault().syncExec(new Runnable(){
						public void run() {
							searchEntryType.setSearchText(searchText.getText());
							if (limit.getSelection() > 0)
								searchEntryType.setResultRange(0, limit.getSelection());
							else
								searchEntryType.setResultRange(0, Long.MAX_VALUE);
						}
					});
					final Object result = searchEntryType.search(monitor);
					Display.getDefault().syncExec(new Runnable(){
						public void run() {
							displaySearchResult(result);	
						}
					});					
				}
				return Status.OK_STATUS;
			}			
		}.schedule();
	}			
	
	private QuickSearchEntryType searchEntryType;
	public abstract void displaySearchResult(Object result);
	
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
//		sashform.setWeights(new int[] {1, 10});
		int searchHeight = searchWrapper.getSize().y;
		int resultHeight = resultComposite.getSize().y;
		sashform.setWeights(new int[] {searchHeight, resultHeight});
	}
	
	public Composite createCategoryEntryComposite(Composite parent) {
		throw new UnsupportedOperationException("SearchEntryViewer does not support custom Composites for the Category."); //$NON-NLS-1$
	}

	private SelectionListener searchItemListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
			if (e.detail != SWT.ARROW)
				search();
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};
	
	private SelectionListener searchTextListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			search();
		}
	};
	
	private int initalSearchHeight = -1; 
	private IExpansionListener expansionListener = new IExpansionListener(){
		public void expansionStateChanging(ExpansionEvent e) {
		}
		public void expansionStateChanged(ExpansionEvent e) {
			doExpand();
		}
	};
		
	protected void doExpand() 
	{
		if (initalSearchHeight == -1) {
			initalSearchHeight = searchWrapper.getSize().y;
		}			
		int completeHeight = sashform.getSize().y;
		int searchHeight = 1;
		int resultHeight = 10;
//		if (e.getState()) {
		if (searchCriteriaSection.isExpanded()) {
			searchHeight = initalSearchHeight + searchComposite.getSize().y;
		} else {
			searchHeight = initalSearchHeight;
		}
		resultHeight = completeHeight - searchHeight;			
		sashform.setWeights(new int[] {searchHeight, resultHeight});
		sashform.layout(true, true);					
	}
	
	protected void configureQuickSearchEntries(final ToolItem searchItem) 
	{
		if (getQuickSearchEntryTypes() != null && !getQuickSearchEntryTypes().isEmpty()) { 
			List<QuickSearchEntryType> quickSearchEntryTypes = getQuickSearchEntryTypes();
			final Menu menu = new Menu(RCPUtil.getActiveWorkbenchShell(), SWT.POP_UP);
			for (final QuickSearchEntryType quickSearchEntryType : quickSearchEntryTypes) {
				final MenuItem menuItem = new MenuItem(menu, SWT.CHECK);			
//				menuItem.setText(quickSearchEntryType.getName().getText());
				menuItem.setText(quickSearchEntryType.getName());
				menuItem.setImage(quickSearchEntryType.getImage());
				menuItem.setData(quickSearchEntryType);
				menuItem.addSelectionListener(new SelectionListener(){			
					public void widgetSelected(SelectionEvent e) {
						searchEntryType = quickSearchEntryType;
						for (int i=0; i<menu.getItems().length; i++) {
							MenuItem mi = menu.getItem(i);
							mi.setSelection(false);
						}
						menuItem.setSelection(true);
						search();	
					}			
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}			
				});				
			}
			searchItem.addListener(SWT.Selection, new Listener(){	
	  		public void handleEvent(Event event) {
	  			if (event.detail == SWT.ARROW) {
	  				Rectangle rect = searchItem.getBounds();
	  				Point p = new Point(rect.x, rect.y + rect.height);
	  				p = searchItem.getParent().toDisplay(p);
	  				menu.setLocation(p.x, p.y);
	  				menu.setVisible(true);
	  			}
	  		}
			});
		}		 
	}
	
	protected List<QuickSearchEntryType> getQuickSearchEntryTypes() {
		return Collections.EMPTY_LIST;
	}
	
	protected abstract QuickSearchEntryType getAdvancedQuickSearchEntryType();
	
	protected void configureSection(final Section section) 
	{
		Button activeButton = new Button(section, SWT.CHECK);
		activeButton.setText(Messages.getString("org.nightlabs.jfire.base.overview.search.AbstractQueryFilterComposite.activeButton.text")); //$NON-NLS-1$
		activeButton.setSelection(false);
		activeButton.addSelectionListener(new SelectionListener(){	
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				section.setExpanded(b.getSelection());
				doExpand();
				RCPUtil.setControlEnabledRecursive(searchTextToolBar, !b.getSelection());
//				searchTextToolBar.setEnabled(!b.getSelection());
			}	
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}	
		});				
		section.setTextClient(activeButton);
	}	
	
}
