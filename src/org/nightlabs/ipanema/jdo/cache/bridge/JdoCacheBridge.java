/*
 * Created on Jul 30, 2005
 */
package org.nightlabs.ipanema.jdo.cache.bridge;

import javax.jdo.PersistenceManagerFactory;

import org.nightlabs.ipanema.jdo.cache.CacheManagerFactory;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class JdoCacheBridge
{
	private CacheManagerFactory cacheManagerFactory;
	private PersistenceManagerFactory persistenceManagerFactory;

	public JdoCacheBridge()
	{
	}

	/**
	 * @return Returns the cacheManagerFactory.
	 */
	public CacheManagerFactory getCacheManagerFactory()
	{
		return cacheManagerFactory;
	}
	/**
	 * @param cacheManagerFactory The cacheManagerFactory to set.
	 */
	public void setCacheManagerFactory(CacheManagerFactory cacheManagerFactory)
	{
		this.cacheManagerFactory = cacheManagerFactory;
	}

	/**
	 * @return Returns the persistenceManagerFactory.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory()
	{
		return persistenceManagerFactory;
	}
	/**
	 * @param persistenceManagerFactory The persistenceManagerFactory to set.
	 */
	public void setPersistenceManagerFactory(
			PersistenceManagerFactory persistenceManagerFactory)
	{
		this.persistenceManagerFactory = persistenceManagerFactory;
	}

	/**
	 * This method is called after the following methods have been executed:
	 * <ul>
	 *		<li>{@link #setCacheManagerFactory(CacheManagerFactory)}</li>
	 *		<li>{@link #setPersistenceManagerFactory(PersistenceManagerFactory)}</li>
	 * </ul>
	 *
	 * Overwrite it and do your initialization (e.g. register your listeners).
	 */
	public abstract void init();

	/**
	 * In this method, you must unregister all your listeners and make sure, your
	 * bridge is unusable after this method is called.
	 */
	public abstract void close();
}
