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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.jdo.notification.AbsoluteFilterID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheManager
{
	private CacheManagerFactory cacheManagerFactory;
	private JFirePrincipal principal;

	/**
	 * You can only use some of the methods after having created an instance with
	 * this constructor before having called {@link #initPrincipal(JFirePrincipal)}.
	 * Most methods require that you have created the CacheManager with
	 * {@link #CacheManager(CacheManagerFactory, JFirePrincipal)}
	 *
	 * @param cacheManagerFactory
	 */
	protected CacheManager(CacheManagerFactory cacheManagerFactory)
	{
		this(cacheManagerFactory, (JFirePrincipal)null);
	}

	protected CacheManager(CacheManagerFactory cacheManagerFactory, JFirePrincipal principal)
	{
		this.cacheManagerFactory = cacheManagerFactory;
		this.principal = principal;
	}

	public void initPrincipal(JFirePrincipal principal)
	{
		if (this.principal != null)
			throw new IllegalStateException("principal is already initialized!");

		this.principal = principal;
	}

	protected void assertPrincipalExisting()
	{
		if (principal == null)
			throw new IllegalStateException("The principal is unknown! Either create the CacheManager with a principal or use the method initPrincipal(...)!");
	}

	/**
	 * Use this method in order to subscribe your current <tt>CacheSession</tt>
	 * on changes of the JDO object specified by <tt>objectID</tt>.
	 * <p>
	 * This method implicitely opens a <tt>CacheSession</tt>.
	 *
	 * @param objectID A JDO object ID referencing a JDO object in which
	 *		you are interested
	 *		(see {@link javax.jdo.JDOHelper#getObjectId(java.lang.Object)}).
	 */
	public void addChangeListener(Object objectID)
	{
		assertPrincipalExisting();
		cacheManagerFactory.addChangeListener(
				principal.getUserID(),
				new ChangeListenerDescriptor(principal.getSessionID(), objectID));
	}

	/**
	 * @param objectIDs Instances of JDO object IDs.
	 *
	 * @see #addChangeListener(Object)
	 */
	public void addChangeListeners(Collection objectIDs)
	{
		assertPrincipalExisting();
		for (Iterator it = objectIDs.iterator(); it.hasNext(); )
			cacheManagerFactory.addChangeListener(
					principal.getUserID(),
					new ChangeListenerDescriptor(principal.getSessionID(), it.next()));
	}

	public void resubscribeAllListeners(Set<Object> subscribedObjectIDs,
			Collection<IJDOLifecycleListenerFilter> filters)
	{
		assertPrincipalExisting();
		cacheManagerFactory.resubscribeAllListeners(
				principal.getSessionID(), principal.getUserID(), subscribedObjectIDs, filters);
	}

	public void addLifecycleListenerFilters(Collection<IJDOLifecycleListenerFilter> filters)
	{
		assertPrincipalExisting();
		cacheManagerFactory.addLifecycleListenerFilters(principal.getUserID(), filters);
	}

	public void removeLifecycleListenerFilters(Collection<Long> filterIDs)
	{
		assertPrincipalExisting();

		Set<AbsoluteFilterID> absoluteFilterIDs = new HashSet<AbsoluteFilterID>(filterIDs.size());
		for (Long filterID : filterIDs)
			absoluteFilterIDs.add(new AbsoluteFilterID(principal.getSessionID(), filterID.longValue()));

		cacheManagerFactory.removeLifecycleListenerFilters(absoluteFilterIDs);
	}

	/**
	 * Removes a listener that has been previously added by
	 * {@link #addChangeListener(Object)}. Note, that one single call of this method
	 * always removes the listener, even if <tt>addChangeListener(...)</tt>
	 * has been called multiple times.
	 *
	 * @param objectID A JDO object ID referencing a JDO object
	 *		(see {@link javax.jdo.JDOHelper#getObjectId(java.lang.Object)}).
	 */
	public void removeChangeListener(Object objectID)
	{
		assertPrincipalExisting();
		cacheManagerFactory.removeChangeListener(principal.getSessionID(), objectID);
	}

	/**
	 * @param objectIDs Instances of JDO object IDs.
	 *
	 * @see #removeChangeListener(Object)
	 */
	public void removeChangeListeners(Collection objectIDs)
	{
		assertPrincipalExisting();
		for (Iterator it = objectIDs.iterator(); it.hasNext(); )
			cacheManagerFactory.removeChangeListener(principal.getSessionID(), it.next());
	}

	/**
	 * This method closes the current <tt>CacheSession</tt> and removes all
	 * listeners of this session. A subsequent call to {@link #addChangeListener(Object)}
	 * or {@link #waitForChanges()} automatically reopens a new empty session
	 * (for the same <tt>cacheSessionID</tt>).
	 * <p>
	 * If a <tt>CacheSession</tt> has not been used for a configurable time (default
	 * is 30 minutes) - e.g. because the client is not online anymore -,
	 * it will be closed automatically. Each call to {@link #waitForChanges()}
	 * or {@link #addChangeListener(Object)} will keep the session alive.
	 */
	public void closeCacheSession()
	{
		assertPrincipalExisting();
		cacheManagerFactory.closeCacheSession(principal.getSessionID());
	}

	/**
	 * This method causes <b>all</b> <tt>CacheSession</tt>s to be notified which
	 * have a listener on one or more of the given <tt>objectIDs</tt>.
	 * The notification works asynchronously, hence this method quickly returns
	 * and if it is called multiple times within a short time, the
	 * objectIDs will be collected and sent together to the client.
	 * @param objectIDs The IDs of the JDO objects that have been changed
	 *		(see {@link javax.jdo.JDOHelper#getObjectId(java.lang.Object)}).
	 */
	public void addDirtyObjectIDs(String sessionID, Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> dirtyObjectIDs)
	{
		cacheManagerFactory.addDirtyObjectIDs(sessionID, dirtyObjectIDs);
	}

	/**
	 * This method blocks until either the timeout occurs or
	 * changes for the current <tt>CacheSession</tt> have
	 * occured.
	 *
	 * @param waitTimeout The timeout in milliseconds after which this method
	 *		will return <tt>null</tt> if no changes occur before.
	 *
	 * @return Returns either <tt>null</tt> if nothing changed or a {@link NotificationBundle}
	 *		of object ids. Hence {@link NotificationBundle#isEmpty()} will never return <code>true</code>
	 *		(<code>null</code> would have been returned instead of a <code>NotificationBundle</code>).
	 */
	public NotificationBundle waitForChanges(long waitTimeout)
	{
		assertPrincipalExisting();
		return cacheManagerFactory.waitForChanges(principal.getSessionID(), principal.getUserID(), waitTimeout);
	}
}
