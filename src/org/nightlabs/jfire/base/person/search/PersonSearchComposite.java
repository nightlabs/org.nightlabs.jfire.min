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

package org.nightlabs.jfire.base.person.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.prop.PropertySetTable;
import org.nightlabs.jfire.base.prop.search.PropertySetSearchFilterItemListMutator;
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
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonSearchComposite<PersonLinkType> extends XComposite {
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PersonSearchComposite.class);
	
	public static final String[] FETCH_GROUPS_FULL_DATA = new String[] {FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_FULL_DATA};
	
	private XComposite wrapper;
	private TabFolder filterProviderFolder;
	private XComposite buttonBar;
	private PropertySetTable<PersonLinkType> resultTable;
	private String quickSearchText;
	private XComposite resultLabelWrapper;
	private Label resultLabel;
	private boolean doIDSearchAndUsePropertySetCache = true;
	
	/**
	 * @param title The pages title
	 * @param quickSearchText The initial quick search text
	 */
	public PersonSearchComposite(
			Composite parent, int style, String quickSearchText,
			boolean doIDSearchAndUsePropertySetCache
	) {
		super(parent, style);
		this.quickSearchText = quickSearchText;
		this.doIDSearchAndUsePropertySetCache = doIDSearchAndUsePropertySetCache;
		init(this);
	}
	
	/**
	 * @param title The pages title
	 * @param quickSearchText The initial quick search text
	 */
	public PersonSearchComposite(
			Composite parent, int style, String quickSearchText
	) {
		this(parent, style, quickSearchText, true);
	}
	
//	protected void createNewCreation

	/*
	 * ********* interface for filter provider tabs ************
	 */
	private static interface FilterProviderTab {
		public SearchFilterProvider getFilterProvider();
	}
	
	/*
	 * ********* static and dynamic filter provider tabs ************
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
			if (filterProvider instanceof StaticPersonSearchFilterProvider) {
				((StaticPersonSearchFilterProvider) filterProvider).setQuickSearchText(StaticPersonSearchFilterProvider.parseNameNeedle(quickSearchText));
			}
		}
	}
	
	private StaticProviderTabItem staticTab;

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

	/*
	 * ********* result fetcher searches for LegalEntities ************
	 */	
	private SearchResultFetcher resultFetcher = new SearchResultFetcher() {
		public void searchTriggered(final SearchFilterProvider filterProvider) {
			logger.debug("Search triggered, getting PersonManager"); //$NON-NLS-1$
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					resultTable.setInput(new String[] {"Searching ..."});
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
					start = System.currentTimeMillis();
					logger.debug("Getting "+input.size()+" from cache took " + Util.getTimeDiffString(start)); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					input = propertyManager.searchPropertySets(searchFilter, getFetchGroups(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
					logger.debug("Object search for " + input.size() + " entries took " + Util.getTimeDiffString(start)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				start = System.currentTimeMillis();
				final Collection<?> finalInput = input;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						resultTable.setInput(finalInput);
					}
				});				
			} catch (Exception e) {
				logger.error("Error searching person.",e); //$NON-NLS-1$
				throw new RuntimeException(e);
			}

		}
	};
	
	public void performSearch() {
		final SearchFilterProvider filterProvider = ((FilterProviderTab) filterProviderFolder.getSelection()[0].getData()).getFilterProvider();		
		Job job = new Job("Searching LegalEntities") {
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
	
	protected Composite createWrapper(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		return wrapper;
	}
	
	protected Composite getWrapper() {
		return wrapper;
	}

	protected Control init(Composite parent) {
		createWrapper(parent);
		filterProviderFolder = new TabFolder(getWrapper(), SWT.NONE);
		filterProviderFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		staticTab = new StaticProviderTabItem(filterProviderFolder, resultFetcher);
		staticTab.getTabItem().setText("Normal search");
		staticTab.setQuickSearchText(quickSearchText);
		
		dynamicTab = new DynamicProviderTabItem(filterProviderFolder, resultFetcher);
		dynamicTab.getTabItem().setText("Advanced search");
		
		buttonBar = new XComposite(getWrapper(), SWT.NONE, XComposite.LayoutMode.LEFT_RIGHT_WRAPPER, XComposite.LayoutDataMode.GRID_DATA_HORIZONTAL);
		
		
		resultLabelWrapper = new XComposite(getWrapper(), SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		resultLabelWrapper.getGridData().verticalIndent = 5;
		resultLabelWrapper.getGridData().horizontalIndent = 5;
		resultLabelWrapper.getGridData().verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		resultLabelWrapper.getGridData().grabExcessVerticalSpace = false;
		resultLabel = new Label(resultLabelWrapper, SWT.NONE | SWT.WRAP);		
		resultLabel.setLayoutData(new GridData());
		resultLabel.setText("Search results");
		
		resultTable = createResultTable(parent);
		resultTable.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
//				selectedLegalEntity = resultTable.getSelectedLegalEntity();
			}
		});
		GridData tgd = new GridData(GridData.FILL_BOTH);
		tgd.heightHint = 300;
		resultTable.setLayoutData(tgd);
		
		if (quickSearchText != null && !"".equals(quickSearchText)) { //$NON-NLS-1$
			resultFetcher.searchTriggered(staticTab.getFilterProvider());
		}
		return getWrapper();
	}

	public Button createSearchButton(Composite parent) {
		Button searchButton = new Button(parent, SWT.PUSH);
		searchButton.setText("&Search");
		searchButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performSearch();
			}
		});
		return searchButton;
	}
	
	/* ************************************************************************
	 * Configuration methods, TODO: Document
	 * ************************************************************************/
	
	protected PropertySetTable<PersonLinkType> createResultTable(Composite parent) {
		return (PropertySetTable<PersonLinkType>) new PersonResultTable(parent, SWT.NONE);
	}
	
	protected SearchFilterProvider createStaticSearchFilterProvider(SearchResultFetcher resultFetcher) {
		return new StaticPersonSearchFilterProvider(resultFetcher, false);
	}
	
	protected SearchFilterProvider createDynamicSearchFilterProvider(SearchResultFetcher resultFetcher) {
		return new DynamicPersonSearchFilterProvider(new PropertySetSearchFilterItemListMutator(), resultFetcher);
	}
	
	public PropertySetTable<PersonLinkType> getResultTable() {
		return resultTable;
	}

	public Composite getButtonBar() {
		return buttonBar;
	}
	
	protected String[] getFetchGroups() {
		return FETCH_GROUPS_FULL_DATA;
	}
	
	
}
