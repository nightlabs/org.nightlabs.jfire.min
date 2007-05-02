package org.nightlabs.jfire.base.overview.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.JDOQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class BaseSearchEntryViewController 
extends SearchEntryViewController 
{

	public BaseSearchEntryViewController() {
		super();
	}

	@Override
	public void applySearch() 
	{
		if (getFilterComposite() != null && getListComposite() != null) 
		{
			getListComposite().getTableViewer().setInput(new String[] {"Loading!"});
			new Job("Loading"){			
				@Override
				protected IStatus run(IProgressMonitor monitor) 
				{	
					final List<JDOQuery> queries = new ArrayList<JDOQuery>(2);
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							queries.addAll(getFilterComposite().getJDOQueries());
						}
					});
					
					final Object result = getQueryResult(queries, monitor);
					optimizeSearchResults(result);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							getListComposite().getTableViewer().setInput(result);							
						}					
					});
					return Status.OK_STATUS;
				}			
			}.schedule();
		}			
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
	
	/**
	 * return the result of the given queries
	 * 
	 * @param queries a collection of {@link JDOQuery}s
	 * @param monitor the {@link IProgressMonitor} to display the progress
	 * @return the result of the queries 
	 */
	protected abstract Object getQueryResult(Collection<JDOQuery> queries, IProgressMonitor monitor);	
}
