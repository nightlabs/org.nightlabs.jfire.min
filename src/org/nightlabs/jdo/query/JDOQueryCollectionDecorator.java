package org.nightlabs.jdo.query;

import java.util.Collection;

import javax.jdo.PersistenceManager;

/**
 * A Decorator for a QueryCollection that contains JDO-capable queries. This decorator sets the 
 * given PersitenceManager for all JDO-capable queries.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class JDOQueryCollectionDecorator<R, Q extends AbstractSearchQuery<? extends R>>
	extends QueryCollection<R, Q>
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	private QueryCollection<R, ? extends Q> wrappedCollection;
	private transient PersistenceManager pm;

	public JDOQueryCollectionDecorator(QueryCollection<R, ? extends Q> wrappedCollection)
	{
		super();
		assert wrappedCollection != null;
		this.wrappedCollection = wrappedCollection;
	}
	
	@Override
	public Collection<R> executeQueries()
	{
		for (Q query : wrappedCollection)
		{
			if (query instanceof AbstractJDOQuery)
			{
				final AbstractJDOQuery<? extends R> jdoQuery = (AbstractJDOQuery<? extends R>) query;
				jdoQuery.setPersistenceManager(getPersistenceManager());
			}
		}
		return wrappedCollection.executeQueries();
	}

	/**
	 * @return the PersitenceManager used for the JDO-capable queries in this collection.
	 */
	public PersistenceManager getPersistenceManager()
	{
		return pm;
	}

	/**
	 * @param pm the PersitenceManager used for the JDO-capable queries in this collection.
	 */
	public void setPersistenceManager(PersistenceManager pm)
	{
		assert pm != null;
		this.pm = pm;
	}
}
