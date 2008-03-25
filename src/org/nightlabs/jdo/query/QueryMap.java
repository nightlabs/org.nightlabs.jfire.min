package org.nightlabs.jdo.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This implementation of QueryCollection maintains a Map of self-created queries, in which only one
 * instance of a query type may exist. You cannot add or remove elements directly, unless you call
 * either {@link #load(QueryCollection)} or {@link #clear()}. <br>
 * The correct way to retrieve the single query instance of one type is by calling
 * {@link #getQueryOfType(Class)}.
 *  
 * @param <R> the type of the Query result.
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryMap<R, Q extends AbstractSearchQuery<? extends R>>
	extends QueryCollection<R, Q>
{
	/**
	 * A map maintaining one JDOQuery object of each type.
	 */
	private Map<Class<? extends Q>, Q> managedQueries;

	/**
	 * Serial version number.
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void checkCreateCollection()
	{
		if (managedQueries != null)
			return;

		managedQueries = new HashMap<Class<? extends Q>, Q>();
	}

	@Override
	protected Collection<Q> getManagedQueries()
	{
		checkCreateCollection();
		return managedQueries.values();
	}
	
	/**
	 * Returns the types of all queries managed by this map.
	 * @return the types of all queries managed by this map.
	 */
	public Set<Class<? extends Q>> getManagedQueryTypes()
	{
		return managedQueries.keySet();
	}
	
	/**
	 * Replaces the existing query of the given type by the given instance, if there was a query
	 * the given type existing.
	 * <p>Note:
	 * 	No checks are done whether the given QueryCollection contains queries of the same type, which
	 * 	will result in one of them overriding the other!
	 * </p>
	 *
	 * @param queries the {@link QueryCollection} containing queries that shall be used to replace
	 * 	existing ones.
	 */
	@SuppressWarnings("unchecked")
	public void load(QueryCollection<R, Q> queries)
	{
		if (queries == null || queries.isEmpty())
			return;

		for (Q query : queries)
		{
			// safe cast since queries may only contain AbstractSearchQueries of type ? extends Q
			managedQueries.put((Class<? extends Q>) query.getClass(), query);
		}
	}

	/**
	 * Convenient method for {@link #getQueryOfType(Class, boolean)} with
	 * <code>createIfNotExisting = true</code>.
	 * 
	 * @param <T> the type of JDOQuery you want to have.
	 * @param queryClass the class object of <code>T</code>.
	 * @return the managed instance of <code>T</code>.
	 */
	public <T extends Q> T getQueryOfType(Class<T> queryClass)
	{
		return getQueryOfType(queryClass, true);
	}
	
	/**
	 * Returns the already managed query of type <code>T</code> or instantiates a new one with the
	 * given class object and returns it.
	 * 
	 * @param <T> the type of JDOQuery you want to have.
	 * @param queryClass the class object of <code>T</code>.
	 * @param createIfNotExisting if this flag is <code>true</code> and no currently existing instance
	 * 	of the desired <code>queryClass</code> was found then a new instance is created.
	 * @return the managed instance of <code>T</code> or <code>null</code> depending on the
	 * 	<code>createIfNotExisting</code> flag.
	 */
	public <T extends Q> T getQueryOfType(Class<T> queryClass, boolean createIfNotExisting)
	{
		assert queryClass != null;
		checkCreateCollection();
		
		if (managedQueries.get(queryClass) != null)
		{
			return queryClass.cast(managedQueries.get(queryClass));
		}

		// if no new instance shall be created and none was found return null
		if (! createIfNotExisting)
		{
			return null;			
		}
		
		// make use of the rule that every JDOQuery subclass must have a default constructor.
		T newQuery;
		try
		{
			newQuery = queryClass.newInstance();
		} catch (InstantiationException e)
		{
			throw new RuntimeException("The given JDOQuery class: " + queryClass.getName()
				+ " cannot be instantiated!", e);
		} catch (IllegalAccessException e)
		{
			throw new RuntimeException("The constructor of the given JDOQuery class: "
				+ queryClass.getName()
				+ "is not public, hence violates the rule of the JDOQuery superclass!", e);
		}

		managedQueries.put(queryClass, newQuery);
		return newQuery;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(Q newQuery)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Q> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Clears the collection of managed queries.
	 * <p>
	 * Warning:<br/> Be sure to know what you are doing, since others may still rely on the queries
	 * maintained by this map!
	 * </p>
	 */
	@Override
	public void clear()
	{
		managedQueries.clear();
	}
}
