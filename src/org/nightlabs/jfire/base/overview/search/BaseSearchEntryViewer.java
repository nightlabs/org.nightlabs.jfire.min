package org.nightlabs.jfire.base.overview.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.base.overview.Entry;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class BaseSearchEntryViewer 
extends SearchEntryViewer 
{
	public BaseSearchEntryViewer(Entry entry) {
		super(entry);
	}

//	@Override
//	public void search() 
//	{
//		if (getFilterComposite() != null && getListComposite() != null) 
//		{
//			getListComposite().getTableViewer().setInput(new String[] {Messages.getString("org.nightlabs.jfire.base.overview.search.BaseSearchEntryViewer.applySearch.listComposite_loading")}); //$NON-NLS-1$
//			new Job(Messages.getString("org.nightlabs.jfire.base.overview.search.BaseSearchEntryViewer.job.name")){			 //$NON-NLS-1$
//				@Override
//				protected IStatus run(ProgressMonitor monitor) 
//				{	
//					final List<JDOQuery> queries = new ArrayList<JDOQuery>(2);
//					Display.getDefault().syncExec(new Runnable() {
//						public void run() {
//							queries.addAll(getFilterComposite().getJDOQueries());
//						}
//					});
//					
//					final Object result = getQueryResult(queries, monitor);
//					optimizeSearchResults(result);
//					Display.getDefault().asyncExec(new Runnable() {
//						public void run() {
//							getListComposite().getTableViewer().setInput(result);							
//						}					
//					});
//					return Status.OK_STATUS;
//				}			
//			}.schedule();
//		}			
//	}
	
	@Override
	public void displaySearchResult(Object result) 
	{
		if (getListComposite() != null) {
			getListComposite().getTableViewer().setInput(result);
		}				
	}

	@Override
	public QuickSearchEntryType getAdvancedQuickSearchEntryType() {
		return new AdvancedQuickSearchEntryType();
	}

	public AbstractQueryFilterComposite getFilterComposite() 
	{
		if (getSearchComposite() instanceof AbstractQueryFilterComposite)
			return (AbstractQueryFilterComposite) getSearchComposite();
		
		return null;
	}
	
	public AbstractTableComposite getListComposite() 
	{
		if (getResultComposite() instanceof AbstractTableComposite)
			return (AbstractTableComposite) getResultComposite();
		
		return null;
	}
	
	// default implementation does nothing
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

	private class AdvancedQuickSearchEntryType
	extends AbstractQuickSearchEntry 
	{
		public Object search(ProgressMonitor monitor) 
		{
			Display.getDefault().syncExec(new Runnable(){
				public void run() {
					getListComposite().getTableViewer().setInput(new String[] {Messages.getString("org.nightlabs.jfire.base.overview.search.BaseSearchEntryViewer.applySearch.listComposite_loading")}); //$NON-NLS-1$					
				}
			});
			
			final List<JDOQuery> queries = new ArrayList<JDOQuery>(2);
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					queries.addAll(getFilterComposite().getJDOQueries());					
				}
			});
			
			for (JDOQuery query : queries) {
				query.setFromInclude(minInclude);
				query.setToExclude(maxExclude);				
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

		public String getName() {
			return Messages.getString("org.nightlabs.jfire.base.overview.search.BaseSearchEntryViewer.advancedEntry.name"); //$NON-NLS-1$
		}
	}
		
}
