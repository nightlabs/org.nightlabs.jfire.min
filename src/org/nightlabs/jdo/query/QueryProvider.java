package org.nightlabs.jdo.query;

import java.beans.PropertyChangeListener;

/**
 * @param <R> the result type of the query.
 * @param <Q> the query type.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QueryProvider<R, Q extends AbstractSearchQuery<? extends R>>
{
	/**
	 * The implementing classes have to return a Query of the given type and may cache an instance
	 * to be shared among requesters.
	 * 
	 * @param queryClass the runtime class object of the query.
	 * @return a Query of the given type. Note that the returned instance may be shared among others.
	 */
	<ReqQuery extends Q> ReqQuery getQueryOfType(Class<ReqQuery> queryClass);
	
	/**
	 * Returns a copy of the Collection containing all Queries managed by me.
	 * 
	 * @return a copy of the Collection containing all Queries managed by me.
	 */
	QueryCollection<R, Q> getManagedQueries();
	
	/**
	 * Replaces all managed queries by the given ones and notifies the existing listeners accordingly.
	 * 
	 * @param newQueries the collection of queries that shall be used to replace the existing ones.
	 */
	void loadQueries(QueryCollection<R, Q> newQueries);
	
	/**
	 * Registers the given <code>listener</code> for the given <code>targetQueryKind</code> if it 
	 * isn't already registered. Whenever a query of <code>targetQueryKind</code> changes, then the
	 * <code>listener</code> is notified.
	 *  
	 * @param targetQueryKind the type of query for whose changes shall be listened. 
	 * @param listener the listener handling such changes.
	 */
	void addModifyListener(Class<? extends Q> targetQueryKind, PropertyChangeListener listener);
	
	/**
	 * Delegates to {@link #removeModifyListener(Class, PropertyChangeListener)} with the runtime class
	 * of the given <code>listener</code>.
	 * 
	 * @param listener the listener to remove.
	 */
	void removeModifyListener(PropertyChangeListener listener);
	
	/**
	 * Removes the given <code>listener</code> from the set of listeners registered for the given
	 * <code>targetQueryKind</code>. If the given listener is not found, nothing happens.
	 *
	 * @param targetQueryKind the kind of query the given listener is registered for.
	 * @param listener the listener to remove.
	 */
	void removeModifyListener(Class<? extends Q> targetQueryKind, PropertyChangeListener listener);
}
