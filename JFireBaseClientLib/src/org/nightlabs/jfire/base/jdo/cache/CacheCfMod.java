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

package org.nightlabs.jfire.base.jdo.cache;

import org.apache.log4j.Logger;
import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheCfMod extends ConfigModule
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CacheCfMod.class);

	private String documentation;

	private long threadErrorWaitMSec = 0;
	private long localListenerReactionTimeMSec = 0;
	private long waitForChangesTimeoutMSec = 0;
	private long cacheManagerThreadIntervalMSec = 0;
//	private long resyncRemoteListenersIntervalMSec = 0;
	private int carrierContainerCount = 0;
	private long carrierContainerActivityMSec = 0;

	public static final String REFERENCE_TYPE_HARD = "hard";
	public static final String REFERENCE_TYPE_SOFT = "soft";
	private String referenceType = null;

	private int oldGraphDependencyContainerCount = 0;
	private long oldGraphDependencyContainerActivityMSec = 0;

	private Boolean exactFetchGroupsOnly;

	public CacheCfMod()
	{
	}

	/**
	 * @see org.nightlabs.config.ConfigModule#init()
	 */
	@Override
	public void init() throws InitException
	{
		documentation = "This is the documentation for the settings in this ConfigModule.\n" +
				"\n" +
				"* threadErrorWaitMSec: This specifies, how long the threads of the Cache\n" +
				"    (CacheManagerThread and NotificationThread) shall wait before trying\n" +
				"    again, after an error occured (e.g. because of lost connection to the\n" +
				"    server). Default is 60000 (1 min).\n" +
				"\n" +
				"* localListenerReactionTimeMSec:  This is the time in millisec, in which the\n" +
				"    local class based change listeners can download a changed object and add\n" +
				"    it to the cache again, before the remote listener will be unregistered.\n" +
				"    Default is 300000 (5 min).\n" +
				"\n" +
				"* waitForChangesTimeoutMSec: How long shall JDOManager.waitForChanges(String, long)\n" +
				"    wait before returning null if no changes occur. Note, that there's a\n" +
				"    server-side min and max. If this is exceeded, the timeout will be silently\n" +
				"    changed.\n" +
				"    Note, that this must be shorter than any bean-rmi-timeout.\n" +
				"\n" +
				"* cacheManagerThreadIntervalMSec: How long will the CacheManagerThread sleep between\n" +
				"    being active. Default is 3000 (3 sec). This means, in periods of 3 sec, new\n" +
				"    remote-listeners will be added to the server or old ones removed and all 3 sec\n" +
				"    it will be checked, whether the CarrierContainers need to be rolled (and\n" +
				"    therefore old ones be dropped). Since the same thread rolls the\n" +
				"    oldGraphDependencyContainer s, this setting affects them, too.\n" +
//				"\n" +
//				"* resyncRemoteListenersIntervalMSec: In which intervals shall the Cache sync all\n" +
//				"    the listeners (means drop all remote ones and resubscribe the ones in its local\n" +
//				"    CacheManagerThread.currentlySubscribedObjectIDs. Default is 3600000 (60 min).\n" +
				"\n" +
				"* carrierContainerCount: Carriers (means the cached objects in their wrappers)\n" +
				"    expire after a certain time. To avoid iterating all Carriers and check their\n" +
				"    age, they will be put into the active CarrierContainer when they are created.\n" +
				"    This allows to simply throw away the complete oldest container once it expired.\n" +
				"    The expiry age of a Carrier is therefore carrierContainerCount multiplied with\n" +
				"    carrierContainerActivityMSec. Default is 24. Minimum is 2 and maximum 300.\n" +
				"\n" +
				"* carrierContainerActivityMSec: How long shall the active CarrierContainer be\n" +
				"    active, before it will be replaced by a new one (and rolled through the\n" +
				"    LinkedList. Default is 300000 (5 min). Minimum is 1 min and maximum 30 min.\n" +
				"\n" +
				"* referenceType: What kind of references will the cache use to hold its cached\n" +
				"    objects. This can be either "+REFERENCE_TYPE_SOFT+" or "+REFERENCE_TYPE_HARD+". "+REFERENCE_TYPE_SOFT+" means, the garbage collector \n" +
				"    can release objects when memory is getting short. But, you should recalibrate\n" +
				"    carrierContainerCount and carrierContainerActivityMSec, if the garbage\n" +
				"    collector often releases objects. Default is "+REFERENCE_TYPE_HARD+" which means, objects are\n" +
				"    only released by the cache (and cannot be released by the garbage collector).\n" +
				"\n" +
				"* oldGraphDependencyContainerCount: When an object is put into the Cache, its complete\n" +
				"    object graph is indexed in order to notify a change of the container object\n" +
				"    when its content changed. When one of the objects in the graph is modified,\n" +
				"    the container is removed from the cache. If now another object in the same graph\n" +
				"    (or the same object again) is changed, the Cache needs to notify the listeners\n" +
				"    registered on the container about the modification again. Therefore, even after\n" +
				"    the deletion of the container, the dependency information still needs to be present. This means\n" +
				"    even after deletion, it must still be kept for a while. Hence, we store a deleted\n" +
				"    dependency record in a container which expires after a while. Just like the main\n" +
				"    object carriers, we have a list of these containers, where each container is active\n" +
				"    for a while (see oldGraphDependencyContainerActivityMSec) and then pushed back within\n" +
				"    its queue. oldGraphDependencyContainerCount controls the length of this queue (i.e.\n" +
				"    how many containers are kept within the list).\n" +
				"    see https://www.jfire.org/modules/bugs/view.php?id=84\n" +
				"\n" +
				"* oldGraphDependencyContainerActivityMSec: Together with oldGraphDependencyContainerCount\n" +
				"    this controls how long a deleted dependency record is kept. This setting defines the\n" +
				"    life time of the first container. Therefore, a dependency record lives at least\n" +
				"    (oldGraphDependencyContainerCount - 1) * oldGraphDependencyContainerActivityMSec millisec\n" +
				"    and at most oldGraphDependencyContainerCount * oldGraphDependencyContainerActivityMSec ms.\n" +
				"\n" +
				"* exactFetchGroupsOnly: Normally (with the default value \"false\"), the cache will search for\n" +
				"    alternative entries having more than the desired fetch-groups set. However, this behaviour\n" +
				"    can cause Heisenbugs (the accessed fields are there solely because other code has already\n" +
				"    fetched the object before with more fetch-groups). During development, you should set this\n" +
				"    option to \"true\" in order to prevent the Heisenbugs and reliably cause exceptions when\n" +
				"    fetch-groups are missing in your code.\n";

		if (threadErrorWaitMSec <= 0)
			setThreadErrorWaitMSec(60 * 1000);

		if (localListenerReactionTimeMSec <= 0)
			setLocalListenerReactionTimeMSec(5 * 60 * 1000);

		if (waitForChangesTimeoutMSec <= 0)
			setWaitForChangesTimeoutMSec(15 * 60 * 1000);

		if (cacheManagerThreadIntervalMSec < 100)
			setCacheManagerThreadIntervalMSec(3 * 1000);

//		if (resyncRemoteListenersIntervalMSec <= 0)
//			setResyncRemoteListenersIntervalMSec(60 * 60 * 1000);

		if (carrierContainerCount < 2 || carrierContainerCount > 300)
			setCarrierContainerCount(24);

		if (carrierContainerActivityMSec < 60 * 1000 || 30 * 60 * 1000 < carrierContainerActivityMSec)
			setCarrierContainerActivityMSec(5 * 60 * 1000);

		if (oldGraphDependencyContainerActivityMSec < 30 * 1000 || 10 * 60 * 1000 < oldGraphDependencyContainerActivityMSec)
			setOldGraphDependencyContainerActivityMSec(60 * 1000);

		if (oldGraphDependencyContainerCount < 3 || 10 < oldGraphDependencyContainerCount)
			setOldGraphDependencyContainerCount(5);

		if (!REFERENCE_TYPE_SOFT.equals(referenceType) &&
				!REFERENCE_TYPE_HARD.equals(referenceType))
			setReferenceType(REFERENCE_TYPE_HARD);

		if (exactFetchGroupsOnly == null)
			setExactFetchGroupsOnly(Boolean.FALSE);

		if (logger.isDebugEnabled()) {
			logger.debug("The Cache settings are:");
			logger.debug("    threadErrorWaitMSec=" + threadErrorWaitMSec);
			logger.debug("    localListenerReactionTimeMSec=" + localListenerReactionTimeMSec);
			logger.debug("    waitForChangesTimeoutMSec=" + waitForChangesTimeoutMSec);
			logger.debug("    cacheManagerThreadIntervalMSec=" + cacheManagerThreadIntervalMSec);
//			logger.debug("    resyncRemoteListenersIntervalMSec=" + resyncRemoteListenersIntervalMSec);
			logger.debug("    carrierContainerCount=" + carrierContainerCount);
			logger.debug("    carrierContainerActivityMSec=" + carrierContainerActivityMSec);
			logger.debug("    referenceType=" + referenceType);
			logger.debug("    exactFetchGroupsOnly=" + exactFetchGroupsOnly);
		}
	}

	/**
	 * @return Returns the cacheManagerThreadIntervalMSec.
	 */
	public long getCacheManagerThreadIntervalMSec()
	{
		return cacheManagerThreadIntervalMSec;
	}
	/**
	 * @param cacheManagerThreadIntervalMSec The cacheManagerThreadIntervalMSec to set.
	 */
	public void setCacheManagerThreadIntervalMSec(
			long cacheManagerThreadIntervalMSec)
	{
		this.cacheManagerThreadIntervalMSec = cacheManagerThreadIntervalMSec;
		setChanged();
	}
	/**
	 * @return Returns the carrierContainerActivityMSec.
	 */
	public long getCarrierContainerActivityMSec()
	{
		return carrierContainerActivityMSec;
	}
	/**
	 * @param carrierContainerActivityMSec The carrierContainerActivityMSec to set.
	 */
	public void setCarrierContainerActivityMSec(long carrierContainerActivityMSec)
	{
		this.carrierContainerActivityMSec = carrierContainerActivityMSec;
		setChanged();
	}
	/**
	 * @return Returns the carrierContainerCount.
	 */
	public int getCarrierContainerCount()
	{
		return carrierContainerCount;
	}
	/**
	 * @param carrierContainerCount The carrierContainerCount to set.
	 */
	public void setCarrierContainerCount(int carrierContainerCount)
	{
		this.carrierContainerCount = carrierContainerCount;
		setChanged();
	}
	/**
	 * @return Returns the documentation.
	 */
	public String getDocumentation()
	{
		return documentation;
	}
	/**
	 * @param documentation The documentation to set.
	 */
	public void setDocumentation(String documentation)
	{
		this.documentation = documentation;
//		setChanged();
	}
	/**
	 * @return Returns the localListenerReactionTimeMSec.
	 */
	public long getLocalListenerReactionTimeMSec()
	{
		return localListenerReactionTimeMSec;
	}
	/**
	 * @param localListenerReactionTimeMSec The localListenerReactionTimeMSec to set.
	 */
	public void setLocalListenerReactionTimeMSec(
			long localListenerReactionTimeMSec)
	{
		this.localListenerReactionTimeMSec = localListenerReactionTimeMSec;
		setChanged();
	}
//	/**
//	 * @return Returns the resyncRemoteListenersIntervalMSec.
//	 */
//	public long getResyncRemoteListenersIntervalMSec()
//	{
//		return resyncRemoteListenersIntervalMSec;
//	}
//	/**
//	 * @param resyncRemoteListenersIntervalMSec The resyncRemoteListenersIntervalMSec to set.
//	 */
//	public void setResyncRemoteListenersIntervalMSec(
//			long resyncRemoteListenersIntervalMSec)
//	{
//		this.resyncRemoteListenersIntervalMSec = resyncRemoteListenersIntervalMSec;
//		setChanged();
//	}
	/**
	 * @return Returns the threadErrorWaitMSec.
	 */
	public long getThreadErrorWaitMSec()
	{
		return threadErrorWaitMSec;
	}
	/**
	 * @param threadErrorWaitMSec The threadErrorWaitMSec to set.
	 */
	public void setThreadErrorWaitMSec(long threadErrorWaitMSec)
	{
		this.threadErrorWaitMSec = threadErrorWaitMSec;
		setChanged();
	}
	/**
	 * @return Returns the waitForChangesTimeoutMSec.
	 */
	public long getWaitForChangesTimeoutMSec()
	{
		return waitForChangesTimeoutMSec;
	}
	/**
	 * @param waitForChangesTimeoutMSec The waitForChangesTimeoutMSec to set.
	 */
	public void setWaitForChangesTimeoutMSec(long waitForChangesTimeoutMSec)
	{
		this.waitForChangesTimeoutMSec = waitForChangesTimeoutMSec;
		setChanged();
	}

	public String getReferenceType()
	{
		return referenceType;
	}
	public void setReferenceType(String referenceType)
	{
		this.referenceType = referenceType;
		setChanged();
	}

	public long getOldGraphDependencyContainerActivityMSec()
	{
		return oldGraphDependencyContainerActivityMSec;
	}

	public void setOldGraphDependencyContainerActivityMSec(
			long oldSyntheticDirtyObjectIDContainerActivityMSec)
	{
		this.oldGraphDependencyContainerActivityMSec = oldSyntheticDirtyObjectIDContainerActivityMSec;
		setChanged();
	}

	public int getOldGraphDependencyContainerCount()
	{
		return oldGraphDependencyContainerCount;
	}
	public void setOldGraphDependencyContainerCount(
			int oldSyntheticDirtyObjectIDContainerCount)
	{
		this.oldGraphDependencyContainerCount = oldSyntheticDirtyObjectIDContainerCount;
		setChanged();
	}

	/**
	 * Normally (with the default value <code>false</code>), the cache will search for
	 * alternative entries having more than the desired fetch-groups set. However,
	 * this behaviour can cause Heisenbugs (the accessed fields are there solely
	 * because other code has already fetched the object before with more fetch-groups).
	 * During development, you should set this option to <code>true</code> in order to
	 * prevent the Heisenbugs and reliably cause exceptions when fetch-groups are missing
	 * in the code.
	 * <p>
	 * Note, that the "at-least" matching behaviour only exists for the <code>scope</code>
	 * <code>null</code> (see {@link Cache#get(String, Object, String[], int)}).
	 * </p>
	 *
	 * @return whether to force matching of exact fetch-groups rather than looking for an
	 *		object with AT LEAST the desired fetch-groups.
	 */
	public Boolean getExactFetchGroupsOnly()
	{
		return exactFetchGroupsOnly;
	}
	public void setExactFetchGroupsOnly(Boolean exactFetchGroupsOnly)
	{
		this.exactFetchGroupsOnly = exactFetchGroupsOnly;
		setChanged();
	}
}
