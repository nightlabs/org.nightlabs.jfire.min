/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.prop;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ui.search.EarlySearchFilterProvider;
import org.nightlabs.jdo.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerHome;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.dao.PropertySetDAO;
import org.nightlabs.jfire.prop.id.PropertyID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.ObjectCarrier;
import org.nightlabs.util.Util;

/**
 * A Composite that can be used as basis for searches for {@link PropertySet}s
 * matching a set of criteria or for objects that have a {@link PropertySet} as member.
 * <p>
 * The Composite constructs an upper area with a {@link TabFolder} for a static (simple)
 * and a dynamic (complex) search. Clients are responsible to create the static and dynamic
 * filter providers {@link #createStaticSearchFilterProvider(SearchResultFetcher)}, {@link #createDynamicSearchFilterProvider(SearchResultFetcher)}.  
 * </p>
 * <p>
 * Below the search filter TabFolder the Composite will create a so-called
 * Button-bar, a simple Composite that can be filled by clients with 
 * Widgets to trigger search or other custom actions ({@link #getButtonBar()}).
 * </p>
 * <p>
 * Below the Button-bar the Composite will construct a table
 * to view the search results. Here also the client is responsible for creating
 * the table {@link #createResultTable(Composite)}.
 * </p>
 * <p>
 * Clients can control whether the search is performed as ID search an ID search with the collected
 * criteria and afterwards to found objects are resolved via the cache, this is the default.
 * The other alternative is to retrieve the objects directly with the search. This has to be
 * used when the objects searched are not instances of {@link PropertySet}.
 * </p>
 *  
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @param <PropertySetType> The type of {@link PropertySet} the search should be performed for.
 * 		This can be {@link PropertySet} itself or a subclass as well as as objects that are
 * 		in an other way linked to a property set. 
 */
public abstract class PropertySetSearchComposite<PropertySetType> extends XComposite {
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PropertySetSearchComposite.class);
	
	public static final String[] FETCH_GROUPS_FULL_DATA = new String[] {FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_FULL_DATA};
	
	private XComposite wrapper;
	private TabFolder filterProviderFolder;
	private XComposite buttonBar;
	private PropertySetTable<PropertySetType> resultTable;
	private String earlySearchText;
	private XComposite resultLabelWrapper;
	private Label resultLabel;
	private boolean doIDSearchAndUsePropertySetCache = true;
	
	/**
	 * Create a new {@link PropertySetSearchComposite}.
	 * 
	 * @param parent The parent to use.
	 * @param style The style to use.
	 * @param earlySearchText The initial search text (will be used to search right after )
	 * @param doIDSearchAndUsePropertySetCache Whether to do an ID search with the collected
	 * 		criteria and afterwards resolve the found objects via the cache.
	 */
	public PropertySetSearchComposite(
			Composite parent, int style, String earlySearchText,
			boolean doIDSearchAndUsePropertySetCache
	) {
		super(parent, style);
		this.earlySearchText = earlySearchText;
		this.doIDSearchAndUsePropertySetCache = doIDSearchAndUsePropertySetCache;
		init(this);
	}
	
	/**
	 * Create a new {@link PropertySetSearchComposite}. With {@link #doIDSearchAndUsePropertySetCache}
	 * set to <code>true</code>.
	 * 
	 * @param parent The parent to use.
	 * @param style The style to use.
	 * @param earlySearchText The initial search text (will be used to search right after )
	 */
	public PropertySetSearchComposite(
			Composite parent, int style, String quickSearchText
	) {
		this(parent, style, quickSearchText, true);
	}
	
	/**
	 * Interface for filter provider tabs
	 */
	private static interface FilterProviderTab {
		SearchFilterProvider getFilterProvider();
	}
	
	/**
	 * Static filter provider tab.
	 */	
	private class StaticProviderTabItem implements FilterProviderTab {
		private TabItem tabItem;
		private SearchFilterProvider filterProvider;
		private Composite providerComposite;
		
		public StaticProviderTabItem(
				TabFolder parent, 
				SearchResultFetcher resultFetcher
			) 
		{
			tabItem = new TabItem(parent, SWT.NONE);
			filterProvider = createStaticSearchFilterProvider(resultFetcher);			
			providerComposite = filterProvider.createComposite(parent);			
			providerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			tabItem.setControl(providerComposite);
			tabItem.setData(this);
		}		
		
		public TabItem getTabItem() {
			return tabItem;
		}
		public SearchFilterProvider getFilterProvider() {
			return filterProvider;
		}
		public void setQuickSearchText(String quickSearchText) {
			if (filterProvider instanceof EarlySearchFilterProvider) {
				((EarlySearchFilterProvider) filterProvider).setEarlySearchText(quickSearchText);
			}
		}
	}
	
	private StaticProviderTabItem staticTab;

	/**
	 * Dynamic filter provider tab.
	 */	
	private class DynamicProviderTabItem implements FilterProviderTab {
		private TabItem tabItem;
		private SearchFilterProvider filterProvider;
		private Composite providerComposite;
		
		public DynamicProviderTabItem(TabFolder parent, SearchResultFetcher resultFetcher) {
			tabItem = new TabItem(parent, SWT.NONE);
			filterProvider = createDynamicSearchFilterProvider(resultFetcher);
			providerComposite = filterProvider.createComposite(parent);
			providerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			tabItem.setControl(providerComposite);
			tabItem.setData(this);
		}		
		public TabItem getTabItem() {
			return tabItem;
		}
		public SearchFilterProvider getFilterProvider() {
			return filterProvider;
		}
	}
	
	private DynamicProviderTabItem dynamicTab;

	/**
	 * The {@link SearchResultFetcher} for this Composite
	 * that will search using the {@link PropertyManager}.
	 */
	private SearchResultFetcher resultFetcher = new SearchResultFetcher() {
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void searchTriggered(final SearchFilterProvider filterProvider) {
			logger.debug("Search triggered, getting PersonManager"); //$NON-NLS-1$
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					resultTable.setInput(new String[] {Messages.getString("org.nightlabs.jfire.base.prop.PropertySetSearchComposite.statusMessage.searching")}); //$NON-NLS-1$
				}
			});	
			PropertyManagerHome home = null;
			PropertyManager propertyManager = null;
			try {
				home = PropertyManagerUtil.getHome(Login.getLogin().getInitialContextProperties());
				propertyManager = home.create();
			} catch (Exception e) {
				logger.error("Error creating PersonManagerUtil.",e); //$NON-NLS-1$
				throw new RuntimeException(e);
			}
			logger.debug("Have PersonManager searching"); //$NON-NLS-1$
			final ObjectCarrier<PropSearchFilter> oc = new ObjectCarrier<PropSearchFilter>();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					oc.setObject((PropSearchFilter) filterProvider.getSearchFilter());
				}
			});
			PropSearchFilter searchFilter = oc.getObject();

			try {
				long start = System.currentTimeMillis();
				Collection<?> input;
				if (doIDSearchAndUsePropertySetCache) {
					Set<PropertyID> propIDs = new HashSet<PropertyID>(propertyManager.searchPropertySetIDs(searchFilter));
					logger.debug("ID search for "+propIDs.size()+" entries took " + Util.getTimeDiffString(start)); //$NON-NLS-1$ //$NON-NLS-2$
					start = System.currentTimeMillis();
					input = PropertySetDAO.sharedInstance().getPropertySets(
							propIDs, 
							getFetchGroups(), 
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
							new NullProgressMonitor()
					);
					if(input == null)
						input = Collections.emptyList();
					start = System.currentTimeMillis();
					logger.debug("Getting "+input.size()+" from cache took " + Util.getTimeDiffString(start)); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					input = propertyManager.searchPropertySets(searchFilter, getFetchGroups(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
					if(input == null)
						input = Collections.emptyList();
					logger.debug("Object search for " + input.size() + " entries took " + Util.getTimeDiffString(start)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				start = System.currentTimeMillis();
				final Collection<?> finalInput = input;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						resultTable.setInput(finalInput);
						if (finalInput != null && finalInput.size() == 1) {
							resultTable.setSelection(new StructuredSelection(finalInput.iterator().next()));
						}
					}
				});				
			} catch (Exception e) {
				logger.error("Error searching person.",e); //$NON-NLS-1$
				throw new RuntimeException(e);
			}

		}
	};
	
	/**
	 * Performs a search with the current filter provider.
	 * <p>
	 * The search will be done in a Job.
	 * </p>
	 */
	public void performSearch() {
		final SearchFilterProvider filterProvider = ((FilterProviderTab) filterProviderFolder.getSelection()[0].getData()).getFilterProvider();		
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.prop.PropertySetSearchComposite.searchJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				resultFetcher.searchTriggered(filterProvider);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
//	public SearchFilterProvider getSearchFilterProvider() {
//		return ((FilterProviderTab) filterProviderFolder.getSelection()[0]).getFilterProvider();
//	}
	
	/**
	 * Creates the wrapping top-level {@link Composite}.
	 * This might be overridden by clients.
	 */
	protected Composite createWrapper(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		return wrapper;
	}
	
	/**
	 * Returns the wrapping top-level {@link Composite}.
	 * @return The wrapping top-level {@link Composite}.
	 */
	protected Composite getWrapper() {
		return wrapper;
	}

	/**
	 * Creates the Composites children.
	 * @param parent The parent to use
	 * @return The wrapping top-level composite.
	 */
	protected Control init(Composite parent) {
		createWrapper(parent);
		filterProviderFolder = new TabFolder(getWrapper(), SWT.NONE);
		filterProviderFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		staticTab = new StaticProviderTabItem(filterProviderFolder, resultFetcher);
		staticTab.getTabItem().setText(Messages.getString("org.nightlabs.jfire.base.prop.PropertySetSearchComposite.staticTab.text")); //$NON-NLS-1$
		staticTab.setQuickSearchText(earlySearchText);
		
		dynamicTab = new DynamicProviderTabItem(filterProviderFolder, resultFetcher);
		dynamicTab.getTabItem().setText(Messages.getString("org.nightlabs.jfire.base.prop.PropertySetSearchComposite.dynamicTab.text")); //$NON-NLS-1$
		
		buttonBar = new XComposite(getWrapper(), SWT.NONE, XComposite.LayoutMode.LEFT_RIGHT_WRAPPER, XComposite.LayoutDataMode.GRID_DATA_HORIZONTAL);
		
		
		resultLabelWrapper = new XComposite(getWrapper(), SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		resultLabelWrapper.getGridData().verticalIndent = 5;
		resultLabelWrapper.getGridData().horizontalIndent = 5;
		resultLabelWrapper.getGridData().verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		resultLabelWrapper.getGridData().grabExcessVerticalSpace = false;
		resultLabel = new Label(resultLabelWrapper, SWT.NONE | SWT.WRAP);		
		resultLabel.setLayoutData(new GridData());
		resultLabel.setText(Messages.getString("org.nightlabs.jfire.base.prop.PropertySetSearchComposite.resultLabel.text")); //$NON-NLS-1$
		
		resultTable = createResultTable(parent);
		resultTable.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
//				selectedLegalEntity = resultTable.getSelectedLegalEntity();
			}
		});
		GridData tgd = new GridData(GridData.FILL_BOTH);
		tgd.heightHint = 300;
		resultTable.setLayoutData(tgd);
		
		if (earlySearchText != null && !"".equals(earlySearchText)) { //$NON-NLS-1$
			resultFetcher.searchTriggered(staticTab.getFilterProvider());
		}
		return getWrapper();
	}

	/**
	 * Creates a Button in the given Composite that will trigger {@link #performSearch()}
	 * when selected. 
	 * <p>
	 * This method is intended to be used by clients and is not called internally.
	 * </p>
	 * @param parent The parent to add the button to.
	 * @return The newly created Button.
	 */
	public Button createSearchButton(Composite parent) {
		Button searchButton = new Button(parent, SWT.PUSH);
		searchButton.setText(Messages.getString("org.nightlabs.jfire.base.prop.PropertySetSearchComposite.searchButton.text")); //$NON-NLS-1$
		searchButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performSearch();
			}
		});
		return searchButton;
	}
	
	/**
	 * Get the result Table created with {@link #createResultTable(Composite)}. 
	 * @return The result Table created with {@link #createResultTable(Composite)}.
	 */
	public PropertySetTable<PropertySetType> getResultTable() {
		return resultTable;
	}
	
	/**
	 * Get the Button-bar created for this Composite.
	 * @return The Button-bar created for this Composite.
	 */
	public Composite getButtonBar() {
		return buttonBar;
	}
	
	/**
	 * @return Whether to do an ID search with the collected
	 * 		criteria and afterwards resolve the found objects via the cache.
	 */
	public boolean isDoIDSearchAndUsePropertySetCache() {
		return doIDSearchAndUsePropertySetCache;
	}
	
	/**
	 * Define whether to do an ID search with the collected
	 * criteria and afterwards resolve the found objects via the cache.
	 * <p>
	 * This will affect the next search.
	 * </p>
	 * @param doIDSearchAndUsePropertySetCache The doIDSearchAndUsePropertySetCache to search.
	 */
	public void setDoIDSearchAndUsePropertySetCache(
			boolean doIDSearchAndUsePropertySetCache) {
		this.doIDSearchAndUsePropertySetCache = doIDSearchAndUsePropertySetCache;
	}

	/**
	 * Returns the fetch-groups to detach the found results with.
	 * <p>
	 * Clients may override this, the default implementation
	 * returns fetch-groups to get full {@link PropertySet} data.
	 * </p>
	 * 
	 * @return The fetch-groups to detach the found results with.
	 */
	protected String[] getFetchGroups() {
		return FETCH_GROUPS_FULL_DATA;
	}
	
	/**
	 * Create the table to display the found results in.
	 * This should be a {@link PropertySetTable} parameterized
	 * with the same type as this class.
	 *  
	 * @param parent The parent to add the table to.
	 * @return The newly created table.
	 */
	protected abstract PropertySetTable<PropertySetType> createResultTable(Composite parent);
	
	/**
	 * Create the static (simple) {@link SearchFilterProvider} that should be used in the first Tab.
	 * 
	 * @param resultFetcher The {@link SearchResultFetcher} for the new provider.
	 * @return The newly created {@link SearchFilterProvider}.
	 */
	protected abstract SearchFilterProvider createStaticSearchFilterProvider(SearchResultFetcher resultFetcher);
	
	/**
	 * Create the dynamic (complex) {@link SearchFilterProvider} that should be used in the second Tab.
	 * 
	 * @param resultFetcher The {@link SearchResultFetcher} for the new provider.
	 * @return The newly created {@link SearchFilterProvider}.
	 */
	protected abstract SearchFilterProvider createDynamicSearchFilterProvider(SearchResultFetcher resultFetcher);
	
	
	
	
}
