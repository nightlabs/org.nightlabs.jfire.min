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

package org.nightlabs.jfire.jdo.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheSessionContainer
implements Serializable
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CacheSessionContainer.class);

	private CacheManagerFactory cacheManagerFactory;

	public CacheSessionContainer(CacheManagerFactory cacheManagerFactory)
	{
		this.cacheManagerFactory = cacheManagerFactory;

		if (logger.isDebugEnabled())
			logger.debug("Created new instance of CacheSessionContainer (createDT="+createDT+")");
	}

	private long createDT = System.currentTimeMillis();
	/**
	 * @return Returns the createDT, which is the timestamp of the creation of this
	 *		instance.
	 */
	public long getCreateDT()
	{
		return createDT;
	}

	/**
	 * key: String cacheSessionID<br/>
	 * value: CacheSession cacheSession
	 */
	private Map cacheSessions = new HashMap();

	/**
	 * This method is called by
	 * {@link CacheSession#setCacheSessionContainer(CacheSessionContainer)}.
	 * <p>
	 * A <tt>CacheSession</tt> is always in exactly none or one <tt>CacheSessionContainer</tt>.
	 *
	 * @param cacheSession The CacheSession which is added to this CacheSessionContainer.
	 */
	protected void addCacheSession(CacheSession cacheSession)
	{
		if (closed)
			throw new IllegalStateException("This CacheSessionContainer (createDT="+createDT+") is right now closing all cache sessions or has done this already! Why the hell are you trying to add a CacheSession??? This should not be the active container!");

		if (cacheSession.getCacheSessionContainer() != this)
			throw new IllegalArgumentException("cacheSession.getCacheSessionContainer() != this!!! cacheSession.cacheSessionID=\""+cacheSession.getCacheSessionID()+"\"");

		if (logger.isDebugEnabled())
			logger.debug("Adding CacheSession (cacheSessionID=\""+cacheSession.getCacheSessionID()+"\") to CacheSessionContainer (createDT="+createDT+")");

		synchronized (cacheSessions) {
			cacheSessions.put(cacheSession.getCacheSessionID(), cacheSession);
		}
	}

	/**
	 * This method is called by
	 * {@link CacheSession#setCacheSessionContainer(CacheSessionContainer)}.
	 * <p>
	 * A <tt>CacheSession</tt> is always in exactly none or one <tt>CacheSessionContainer</tt>.
	 *
	 * @param cacheSession The CacheSession which is removed from this CacheSessionContainer.
	 */
	protected void removeCacheSession(CacheSession cacheSession)
	{
		if (closed)
			return;

		if (logger.isDebugEnabled())
			logger.debug("Removing CacheSession (cacheSessionID=\""+cacheSession.getCacheSessionID()+"\") from CacheSessionContainer (createDT="+createDT+")");

		synchronized (cacheSessions) {
			cacheSessions.remove(cacheSession.getCacheSessionID());
		}
	}

	boolean closed = false;

	/**
	 * This method closes all <tt>CacheSession</tt>s. Note, that this container
	 * cannot be used anymore afterwards.
	 */
	protected void close()
	{
		closed = true;

		synchronized (cacheSessions) {
			logger.info("CacheSessionContainer (createDT="+createDT+") closes " + cacheSessions.size() + " CacheSessions now.");

			for (Iterator it = cacheSessions.keySet().iterator(); it.hasNext(); ) {
				String cacheSessionID = (String) it.next();
				cacheManagerFactory.closeCacheSession(cacheSessionID);
			}
		}
	}

}
