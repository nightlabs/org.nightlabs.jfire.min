package org.nightlabs.jdo.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class DefaultQueryProvider<R, Q extends AbstractSearchQuery<? extends R>>
	implements QueryProvider<R, Q>
{
	private QueryMap<R, Q> queryMap;
	private Map<Class<? extends Q>, PropertyChangeSupport> listeners;
	private PropertyChangeListener queryChangeDelegator = new PropertyChangeListener()
	{
		@SuppressWarnings("unchecked")
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			if (evt instanceof QueryEvent)
			{
				final QueryEvent event = (QueryEvent) evt;
				// cast is safe, since we only allow such kinds of queries to be added / retrieved!
				notifyListeners((Class<? extends Q>) event.getChangedQuery().getClass(), event);
			}
			else
			{
				// the query we attached this listener to, must have been at least of type Q
				final QueryEvent event = new QueryEvent((AbstractSearchQuery<?>) evt.getSource(), 
					evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()
					);
				
				notifyListeners((Class<? extends Q>) event.getChangedQuery().getClass(), event);
			}
		}
	};
	
	public DefaultQueryProvider()
	{
		queryMap = new QueryMap<R, Q>();
		listeners = new HashMap<Class<? extends Q>, PropertyChangeSupport>();
	}
	
	@Override
	public QueryCollection<R, Q> getManagedQueries()
	{
		return new QueryCollection<R, Q>(queryMap);
	}
	
	@Override
	public <ReqQuery extends Q> ReqQuery getQueryOfType(Class<ReqQuery> queryClass)
	{
		return getQueryOfType(queryClass, true);
	}
	
	public <ReqQuery extends Q> ReqQuery getQueryOfType(Class<ReqQuery> queryClass, boolean createIfNotExisting)
	{
		ReqQuery query = queryMap.getQueryOfType(queryClass, createIfNotExisting);
		if (query != null)
		{
			query.addQueryChangeListener(queryChangeDelegator);
		}
		
		return query; 
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadQueries(QueryCollection<R, Q> newQueries)
	{
		final QueryMap<R, Q> oldQueries = queryMap;
		queryMap = new QueryMap<R, Q>();
		
		// add new Queries
		if (newQueries != null)
		{
			// TODO: maybe we should check whether the given QueryCollection contains multiple queries of the same kind => they will override each other!
			queryMap.load(newQueries);
		}
		queryMap.setFromInclude(newQueries.getFromInclude());
		queryMap.setToExclude(newQueries.getToExclude());
		
		// notify all listeners of the old query types (with the new query if there is one)
		for (Q oldQuery : oldQueries)
		{
			// remove change propagation listener
			oldQuery.removeQueryChangeListener(queryChangeDelegator);
			
			// notify change listeners of that type
			PropertyChangeSupport queryTypeListeners = listeners.get(oldQuery.getClass());
			if (queryTypeListeners != null && queryTypeListeners.hasListeners(null))
			{
				final Q newQuery = queryMap.getQueryOfType((Class<? extends Q>)oldQuery.getClass(), false);
				final QueryEvent event = new QueryEvent(newQuery, AbstractSearchQuery.PROPERTY_WHOLE_QUERY,
					oldQuery, newQuery);
				
				queryTypeListeners.firePropertyChange(event);
			}
		}
		
		Set<Class<? extends Q>> registeredListenerTypes = oldQueries.getManagedQueryTypes();
		// notify all listeners of new query types that haven't been handled yet 
		for (Q query : queryMap)
		{
			// add propagation listener
			query.addQueryChangeListener(queryChangeDelegator);
			
			// if this type of query was already handled -> skip it
			if (registeredListenerTypes.contains(query.getClass()))
				continue;
			
			PropertyChangeSupport queryTypeListeners = listeners.get(query.getClass());
			if (queryTypeListeners != null && queryTypeListeners.hasListeners(null))
			{
				final QueryEvent event = new QueryEvent(query, AbstractSearchQuery.PROPERTY_WHOLE_QUERY,
					null, query);
				
				queryTypeListeners.firePropertyChange(event);
			}
		}
	}
	
	/**
	 * Internal method used to notify the listeners of a given query type.
	 * @param <T> the type of Query for which all registered listeners shall be notified.
	 * @param queryType the runtime type information of <code>T</code>.
	 */
	@SuppressWarnings("unchecked")
	protected void notifyListeners(Class<? extends Q> queryType, QueryEvent event)
	{
		PropertyChangeSupport queryListeners = listeners.get(queryType);
		if (queryListeners == null)
		{
			return;
		}

		queryListeners.firePropertyChange(event);
	}

	@Override
	public void addModifyListener(Class<? extends Q> targetQueryKind,
		PropertyChangeListener listener)
	{
		PropertyChangeSupport queryListeners = listeners.get(targetQueryKind);
		if (queryListeners == null)
		{
			queryListeners = new PropertyChangeSupport(this);
			
			listeners.put(targetQueryKind, queryListeners);
		}
		
		if (! Arrays.asList(queryListeners.getPropertyChangeListeners()).contains(listener))
		{
			queryListeners.addPropertyChangeListener(listener);
		}
	}
	
	@Override
	public void removeModifyListener(PropertyChangeListener listener)
	{
		for (Class<? extends Q> queryType : listeners.keySet())
		{
			removeModifyListener(queryType, listener);
		}
	}

	@Override
	public void removeModifyListener(Class<? extends Q> targetQueryKind,
		PropertyChangeListener listener)
	{
		PropertyChangeSupport queryListeners = listeners.get(targetQueryKind);
		if (queryListeners != null)
		{
			queryListeners.removePropertyChangeListener(listener);
		}
	}

}
