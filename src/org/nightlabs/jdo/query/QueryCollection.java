package org.nightlabs.jdo.query;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.util.CollectionUtil;

/**
 * 
 * @param <R> the type of the Query result.
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryCollection<R, Q extends AbstractSearchQuery<? extends R>>
	extends AbstractCollection<Q>
	implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	/**
	 * The list of all queries combined by this one.
	 */
	private List<Q>	queryList;
	
	public QueryCollection()
	{
		super();
	}
	
	public QueryCollection(Q firstQuery)
	{
		super();
		getManagedQueries().add(firstQuery);
	}
	
	public QueryCollection(QueryCollection<R, ? extends Q> original)
	{
		super();
		getManagedQueries().addAll(original.getManagedQueries());
	}
	
	protected Collection<Q> getManagedQueries()
	{
		checkCreateCollection();
		return queryList;
	}
	
	protected void checkCreateCollection()
	{
		if (queryList != null)
			return;
		
		queryList = new LinkedList<Q>();
	}

	public Collection<R> executeQueries()
	{
		checkCreateCollection();
		Collection<? extends R> result = null;

		for (Iterator<Q> it = getManagedQueries().iterator(); it.hasNext();)
		{
			Q query = it.next();

			// clear the bounds of all queries except the last one, otherwise incorrect results would be
			// the consequence.
			if (it.hasNext())
			{
				query.setFromInclude(0);
				query.setToExclude(Long.MAX_VALUE);
			} else
			{
				query.setFromInclude(getFromInclude());
				query.setToExclude(getToExclude());
			}

			query.setCandidates(result);
			result = query.getResult();
		}

		if (result == null)
			return Collections.emptySet();
		
		return CollectionUtil.castCollection(result);
	}

	@Override
	public boolean add(Q newQuery)
	{
		checkCreateCollection();
		return getManagedQueries().add(newQuery);
	}

	@Override
	public Iterator<Q> iterator()
	{
		checkCreateCollection();
		return getManagedQueries().iterator();
	}

	@Override
	public int size()
	{
		checkCreateCollection();
		return getManagedQueries().size();
	}

	private long	fromInclude	= 0;
	private long	toExclude		= Long.MAX_VALUE;

	/**
	 * Returns the range beginning including the given value.
	 * 
	 * @return the 0-based inclusive start index.
	 */
	public long getFromInclude()
	{
		return fromInclude;
	}

	/**
	 * sets the the range beginning including the given value.
	 * 
	 * @param fromInclude 0-based inclusive start index.
	 */
	public void setFromInclude(long fromInclude)
	{
		this.fromInclude = fromInclude;
	}
	
	/**
	 * Returns the range end excluding the given value.
	 * 
	 * @return the 0-based exclusive end index, or {@link Long#MAX_VALUE} for no limit.
	 */
	public long getToExclude()
	{
		return toExclude;
	}

	/**
	 * Sets the range end excluding the given value.
	 * 
	 * @param toExclude 0-based exclusive end index, or {@link Long#MAX_VALUE} for no limit.
	 */
	public void setToExclude(long toExclude)
	{
		this.toExclude = toExclude;
	}

}
