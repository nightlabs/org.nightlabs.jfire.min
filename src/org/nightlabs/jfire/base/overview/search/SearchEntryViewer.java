package org.nightlabs.jfire.base.overview.search;

import java.util.Collection;
import java.util.Set;

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
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.form.NightlabsFormsToolkit;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.toolkit.IToolkit;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.overview.AbstractEntryViewer;
import org.nightlabs.jfire.base.overview.Entry;
import org.nightlabs.jfire.base.overview.EntryViewer;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Base class for creating {@link EntryViewer}s which are responsible for searching
 *  
 * Subclasses must implement 4 Methods:
 * 
 * {@link #createResultComposite(Composite)} which creates a Composite
 * which displayes the result of the search 
 * {@link #createSearchComposite(Composite)} which creates a Composite
 * which displayes the search criteria
 * {@link #displaySearchResult(Object)} must display the search result
 * passed to this method
 * {@link #getDefaultQuickSearchEntryFactory()} must return a
 * {@link QuickSearchEntry} which will be used by default
 *  
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
		searchCriteriaSection.setText(Messages.getString("org.nightlabs.jfire.base.overview.search.SearchEntryViewer.searchCriteriaSection.text")); //$NON-NLS-1$
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
		searchEntryType = getDefaultQuickSearchEntryFactory().createQuickSearchEntry();
		
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
	
	private Spinner limit;
	/**
	 * creates the top toolbar including the search text, the search item where
	 * all the quickSearchEntries will be displayed, the limit spinner and 
	 * an aditional toolbar where custom actions can be added
	 * 
	 * @param searchComposite the parent Composite where the toolbar will be located in 
	 */
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
		searchLabel.setText(Messages.getString("org.nightlabs.jfire.base.overview.search.SearchEntryViewer.searchLabel.text")); //$NON-NLS-1$
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
		new Label(rangeWrapper, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.overview.search.SearchEntryViewer.limitLabel.text")); //$NON-NLS-1$
		limit = new Spinner(rangeWrapper, SWT.BORDER);
		limit.setMinimum(0);
		limit.setMaximum(Integer.MAX_VALUE);
		limit.setSelection(00);
		ToolItem rangeItem = new ToolItem(searchTextToolBar, SWT.SEPARATOR);
		rangeItem.setControl(rangeWrapper);
		rangeItem.setWidth(350);
		
		ToolBar toolBar = new ToolBar(toolBarWrapper, SWT.NONE);
		toolBarManager = new ToolBarManager(toolBar);	
	}
		
	private Text searchText = null;
	
	/**
	 * Implement this method for displaying the search criteria 
	 * 
	 * @param parent the parent {@link Composite}
	 * @return a Composite which displays the search criteria
	 */
	public abstract Composite createSearchComposite(Composite parent);
	
	/**
	 * Implement this method for displaying the result of a search
	 * 
	 * @param parent the parent {@link Composite}
	 * @return a Composite which displayes the result of a search
	 */
	public abstract Composite createResultComposite(Composite parent);
	
	private Composite searchComposite;
	public Composite getSearchComposite() {
		return searchComposite;
	}	
	private Composite resultComposite;
	public Composite getResultComposite() {
		return resultComposite;
	}	
	
	/**
	 * performs a search with the current criteria
	 * 
	 * This is done by calling {@link QuickSearchEntry#search(ProgressMonitor)} of the
	 * current selected {@link QuickSearchEntry}
	 * 
	 * Furthermore the selected result ranges are set
	 * and after the search is done {@link #displaySearchResult(Object)} is called
	 */
	public void search() 
	{
		new Job(Messages.getString("org.nightlabs.jfire.base.overview.search.JDOQuerySearchEntryViewer.job.name")){			 //$NON-NLS-1$
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
	
	private QuickSearchEntry searchEntryType;
	
	/**
	 * will be called after the search of the current {@link QuickSearchEntry} 
	 * {@link #searchEntryType} is done and the result should be displayed 
	 * in the Composite returned by {@link #createResultComposite(Composite)}
	 * 
	 * @param result the search result to display
	 */
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
		if (getQuickSearchEntryFactories() != null && !getQuickSearchEntryFactories().isEmpty()) { 
			Collection<QuickSearchEntryFactory> quickSearchEntryFactories = getQuickSearchEntryFactories();
			final Menu menu = new Menu(RCPUtil.getActiveWorkbenchShell(), SWT.POP_UP);
			for (final QuickSearchEntryFactory quickSearchEntryFactory : quickSearchEntryFactories) {
				final MenuItem menuItem = new MenuItem(menu, SWT.CHECK);			
				menuItem.setText(quickSearchEntryFactory.getName());
				menuItem.setImage(quickSearchEntryFactory.getImage());
				final QuickSearchEntry quickSearchEntry = quickSearchEntryFactory.createQuickSearchEntry(); 
				menuItem.setData(quickSearchEntry);
				menuItem.addSelectionListener(new SelectionListener(){			
					public void widgetSelected(SelectionEvent e) {
						searchEntryType = quickSearchEntry;
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
	
//	/** 
//	 * Subclasses can return here their implementations of {@link QuickSearchEntryFactory}
//	 * which can be used for searching
//	 *  
//	 * @return a List of {@link QuickSearchEntryFactory}s will can be used for quick searching
//	 */
//	protected List<QuickSearchEntryFactory> getQuickSearchEntryFactories() {
//		return Collections.EMPTY_LIST;
//	}
	/** 
	 * Subclasses can return here their implementations of {@link QuickSearchEntryFactory}
	 * which can be used for searching
	 *  
	 * @return a List of {@link QuickSearchEntryFactory}s will can be used for quick searching
	 */
	protected Collection<QuickSearchEntryFactory> getQuickSearchEntryFactories() 
	{
		Set<QuickSearchEntryFactory> factories = 
			QuickSearchEntryRegistry.sharedInstance().getFactories(this.getClass().getName());
		return factories;
	}
	
	/**
	 * Subclasses must implement this method to return at least one 
	 * {@link QuickSearchEntryFactory} which will be used for searching by default 
	 * 
	 * @return the {@link QuickSearchEntryFactory} which is used by this implementation by default 
	 */
	protected abstract QuickSearchEntryFactory getDefaultQuickSearchEntryFactory();
	
//	/**
//	 * Subclasses my override this method to return the default search entry factory
//	 * by default the {@link QuickSearchEntryFactory} returned by 
//	 * {@link #getAdvancedQuickSearchEntryFactory()} is used 
//	 */
//	protected QuickSearchEntryFactory getDefaultSearchEntryFactory() {
//		return getAdvancedQuickSearchEntryFactory();
//	}
		
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
