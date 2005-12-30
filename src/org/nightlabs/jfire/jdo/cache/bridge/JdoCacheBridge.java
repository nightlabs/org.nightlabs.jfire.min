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

package org.nightlabs.jfire.jdo.cache.bridge;

import javax.jdo.PersistenceManagerFactory;

import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;


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
