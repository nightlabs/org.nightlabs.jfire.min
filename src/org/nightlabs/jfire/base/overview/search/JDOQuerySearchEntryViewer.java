package org.nightlabs.jfire.base.overview.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.base.overview.Entry;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Base Implementation of a {@link SearchEntryViewer} which is designed
 * to work with an implementation of {@link AbstractQueryFilterComposite} as
 * Composite returned by {@link #createSearchComposite(org.eclipse.swt.widgets.Composite)}
 * and an implementation of {@link AbstractTableComposite} as Composite returned
 * by {@link #createResultComposite(org.eclipse.swt.widgets.Composite)}
 *  
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class JDOQuerySearchEntryViewer 
extends SearchEntryViewer 
{
	public JDOQuerySearchEntryViewer(Entry entry) {
		super(entry);
	}

	/**
	 * takes the given result and calls setInput(Object input)
	 * with it 
	 */
	@Override
	public void displaySearchResult(Object result) 
	{
		if (getListComposite() != null) {
			getListComposite().getTableViewer().setInput(result);
		}				
	}

	@Override
	public QuickSearchEntryFactory getDefaultQuickSearchEntryFactory() {
		return new AdvancedQuickSearchEntryFactory();
	}

	@Override
	public Composite createResultComposite(Composite parent) {
		return createListComposite(parent);
	}

	@Override
	public Composite createSearchComposite(Composite parent) {
		return createFilterComposite(parent);
	}

	/**
	 * creates an {@link AbstractQueryFilterComposite} which is used as search composite
	 * this Method is called by {@link SearchEntryViewer#createSearchComposite(Composite)
	 * 
	 * @param parent the parent Composite
	 * @return the {@link AbstractQueryFilterComposite} which is used as search composite
	 */
	public abstract AbstractQueryFilterComposite createFilterComposite(Composite parent); 
	
	/**
	 * creates an {@link AbstractTableComposite} which is used as result composite
	 * this Method is called by {@link SearchEntryViewer#createResultComposite(Composite)}
	 * 
	 * @param parent the parent Composite
	 * @return the {@link AbstractTableComposite} which is used as result composite
	 */
	public abstract AbstractTableComposite createListComposite(Composite parent); 
	
	/**
	 * returns the AbstractTableComposite created by {@link #createListComposite(Composite)}
	 * @return the AbstractTableComposite created by {@link #createListComposite(Composite)}
	 */
	public AbstractTableComposite getListComposite() {
		return (AbstractTableComposite) getResultComposite();
	}

	/**
	 * returns the {@link AbstractQueryFilterComposite} created by {@link #createFilterComposite(Composite)}
	 * @return the {@link AbstractQueryFilterComposite} created by {@link #createFilterComposite(Composite)}  
	 */
	public AbstractQueryFilterComposite getFilterComposite() {
		return (AbstractQueryFilterComposite) getSearchComposite();
	}
	
	/**
	 * can be overridden by inheritans to optimize their search results
	 * by default this method does nothing
	 * 
	 * @param result the search result to optimize
	 */
	protected void optimizeSearchResults(Object result) {
		
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * return the result of the given queries
	 * 
	 * @param queries a collection of {@link JDOQuery}s
	 * @param monitor the {@link IProgressMonitor} to display the progress
	 * @return the result of the queries 
	 */
	protected abstract Object getQueryResult(Collection<JDOQuery> queries, ProgressMonitor monitor);

	private class AdvancedQuickSearchEntryFactory
	extends AbstractQuickSearchEntryFactory
	{
		public String getName() {
			return Messages.getString("org.nightlabs.jfire.base.overview.search.JDOQuerySearchEntryViewer.advancedEntry.name"); //$NON-NLS-1$
		}

		public QuickSearchEntry createQuickSearchEntry() {
			return new AdvancedQuickSearchEntryType(this);
		}
	}
	
	/**
	 * Implementation of an {@link AbstractQuickSearchEntry} which 
	 * takes the queries returned from the {@link AbstractQueryFilterComposite}
	 * which should be returned by {@link SearchEntryViewer#createSearchComposite(org.eclipse.swt.widgets.Composite)}
	 * and use these for searching
	 *  
	 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
	 */
	private class AdvancedQuickSearchEntryType
	extends AbstractQuickSearchEntry 
	{
		public AdvancedQuickSearchEntryType(QuickSearchEntryFactory factory) {
			super(factory);
		}
		
		public Object search(ProgressMonitor monitor) 
		{
			Display.getDefault().syncExec(new Runnable(){
				public void run() {
					getListComposite().getTableViewer().setInput(new String[] {Messages.getString("org.nightlabs.jfire.base.overview.search.JDOQuerySearchEntryViewer.applySearch.listComposite_loading")}); //$NON-NLS-1$					
				}
			});
			
			final List<JDOQuery> queries = new ArrayList<JDOQuery>(2);
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					queries.addAll(getFilterComposite().getJDOQueries());					
				}
			});
			
			for (JDOQuery query : queries) {
				query.setFromInclude(getMinIncludeRange());
				query.setToExclude(getMaxExcludeRange());				
			}
			
			final Object result = getQueryResult(queries, monitor);
			optimizeSearchResults(result);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					getListComposite().getTableViewer().setInput(result);							
				}					
			});
			
			return result;
		}
	}
		
}
