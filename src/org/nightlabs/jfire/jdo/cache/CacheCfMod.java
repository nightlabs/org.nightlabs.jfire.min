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

import org.apache.log4j.Logger;
import org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridgeJPOX;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheCfMod extends ConfigModule
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CacheCfMod.class);

	private String documentation;

	private long notificationIntervalMSec = 0;
	private long delayNotificationMSec = -1;
//	private long cacheSessionContainerCheckIntervalMSec = 0;
	private long cacheSessionContainerActivityMSec = 0;
	private int cacheSessionContainerCount = 0;
	private long waitForChangesTimeoutMin = 0;
	private long waitForChangesTimeoutMax = 0;

	private int freshDirtyObjectIDContainerCount = 0;
	private long freshDirtyObjectIDContainerActivityMSec = 0;

	private String jdoCacheBridgeClassName = null;

	public CacheCfMod()
	{
	}

	/**
	 * @see org.nightlabs.config.ConfigModule#init()
	 */
	public void init() throws InitException
	{
		documentation = "This is the documentation for the settings in this ConfigModule.\n" +
				"\n" +
				"  delayNotificationMSec: How long in milliseconds to wait before forwarding a\n" +
				"    notification to the interested listeners. The delay is realized in the\n" +
				"    method CacheSession#fetchDirtyObjectIDs(). Default is 500.\n" +
				"\n" +
				"* notificationIntervalMSec: The length of the interval in millisec, in which the\n" +
				"    NotificationThread will check changed objects and trigger events. Default\n" +
				"    is 3000 (3 sec). Minimum is 100.\n" +
				"\n" +
//				"* cacheSessionContainerCheckIntervalMSec: In which intervals shall be checked\n" +
//				"    whether the CacheSessionContainers need to be rolled. Default is 120000\n" +
//				"    (2 min). Minimum is 10 sec.\n" +
//				"\n" +
				"* cacheSessionContainerCount: CacheSessions expire after the client didn't use\n" +
				"    them for a longer time. To avoid the need to iterate all CacheSessions in\n" +
				"    order to find out which ones expired, they're grouped in CacheSessionContainers.\n" +
				"    There is always one active CacheSessionContainer into which a CacheSession\n" +
				"    is moved, when it is used. This allows to periodically roll the containers\n" +
				"    and simply throw away a whole expired container. This setting controls, how\n" +
				"    many containers will be used. So, the maximum age of an unused CacheSession\n" +
				"    is cacheSessionContainerCount * cacheSessionContainerActivityMSec. Default\n" +
				"    is 12. Minimum is 2 and maximum 300.\n" +
				"\n" +
				"* cacheSessionContainerActivityMSec: How long shall the active CacheSessionContainer\n" +
				"    be active, before it will be replaced by a new one (and rolled through the\n" +
				"    LinkedList. Default is 300000 (5 min). Minimum is 1 min and maximum 30 min.\n" +
				"\n" +
				"* freshDirtyObjectIDContainerCount: While one client is loading an object, another\n" +
				"    client might simultaneously modify this object. In this case, the listener from\n" +
				"    the first client will be established too late (after the change happened). This\n" +
				"    would cause the first client to miss this modification completely. To avoid this,\n" +
				"    modifications are considered to be fresh for a certain time. Whenever a client\n" +
				"    registers a new listener, the system checks whether the target-object has been\n" +
				"    modified by ANOTHER session during this freshness-period. If so, the new listener\n" +
				"    will be triggered. Like for cache sessions, we use a rolling mechanism to enhance\n" +
				"    performance. This setting controls how many buckets there are. Hence, the time,\n" +
				"    in which a change is considered to be fresh, is:\n" +
				"    freshDirtyObjectIDContainerCount * freshDirtyObjectIDContainerActivityMSec\n" +
				"    Default is 9. Minimum is 2 and maximum is 100.\n" +
				"\n" +
				"* freshDirtyObjectIDContainerActivityMSec: How long is the active bucket active,\n" +
				"    before being replaced by a new one. Default is 20000 millisec. Minimum is 1 sec\n" +
				"    and maximum is 10 min.\n" +
				"\n" +
				"* waitForChangesTimeoutMin: This is the lower limit of what the client can pass\n" +
				"    to CacheManager.waitForChanges(long waitTimeout). If the client requested\n" +
				"    a lower value, it will be changed to this limit. Default is 30000 (30 sec).\n" +
				"    Minumum is 1 sec and maximum 5 min.\n" +
				"\n" +
				"* waitForChangesTimeoutMax: Similar to waitForChangesTimeoutMin, the maximum\n" +
				"    wait timeout can be set, here. Default is 3600000 (1 h). Minimum is\n" +
				"    waitForChangesTimeoutMin and maximum is 3h.\n" +
				"\n" +
				"* jdoCacheBridgeClassName: Depending on the JDO implemention, you're using, you\n" +
				"    need to use a specialized JDO-cache-bridge. This bridge makes sure, the\n" +
				"    CacheManagerFactory (the core of the cache) is notified whenever an object\n" +
				"    is changed in datastore.\n" +
				"    Default: org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridgeJPOX\n";

		if (delayNotificationMSec < 0)
			setDelayNotificationMSec(500);

		if (notificationIntervalMSec < 100)
			setNotificationIntervalMSec(3 * 1000);

//		if (cacheSessionContainerCheckIntervalMSec < 10)
//			setCacheSessionContainerCheckIntervalMSec(2 * 60 * 1000);

		if (cacheSessionContainerActivityMSec < 60 * 1000 || 30 * 60 * 1000 < cacheSessionContainerActivityMSec)
			setCacheSessionContainerActivityMSec(5 * 60 * 1000);

		if (cacheSessionContainerCount < 2 || 300 < cacheSessionContainerCount)				
			setCacheSessionContainerCount(12);

		if (freshDirtyObjectIDContainerActivityMSec < 1000 || 10 * 60 * 1000 < freshDirtyObjectIDContainerActivityMSec)
			setFreshDirtyObjectIDContainerActivityMSec(20000);

		if (freshDirtyObjectIDContainerCount < 2 || 100 < freshDirtyObjectIDContainerCount)
			setFreshDirtyObjectIDContainerCount(9);

		if (waitForChangesTimeoutMin < 1000 || 5 * 60 * 1000 < waitForChangesTimeoutMin)
			setWaitForChangesTimeoutMin(30 * 1000);

		if (waitForChangesTimeoutMax < waitForChangesTimeoutMin || 3 * 60 * 60 * 1000 < waitForChangesTimeoutMax)
			setWaitForChangesTimeoutMax(60 * 60 * 1000);

		if (jdoCacheBridgeClassName == null || "".equals(jdoCacheBridgeClassName))
			setJdoCacheBridgeClassName(JdoCacheBridgeJPOX.class.getName());

		// 2 * 60000 < cacheSessionLifeTime < 300 * 30*60000
		long cacheSessionLifeTime = cacheSessionContainerCount * cacheSessionContainerActivityMSec;
		if (cacheSessionLifeTime < waitForChangesTimeoutMax) {
			logger.warn("cacheSessionLifeTime (" + cacheSessionLifeTime +" msec = cacheSessionContainerCount (" + cacheSessionContainerCount + ") * cacheSessionContainerActivityMSec (" + cacheSessionContainerActivityMSec + ")) < waitForChangesTimeoutMax (" + waitForChangesTimeoutMax + ")! Adjusting waitForChangesTimeoutMax!");
			setWaitForChangesTimeoutMax(cacheSessionLifeTime - 60000);
			// now 120000 - 60000 < waitForChangesTimeoutMax
		}

		if (waitForChangesTimeoutMax < waitForChangesTimeoutMin) {
			logger.warn("waitForChangesTimeoutMax (" + waitForChangesTimeoutMax + ") < waitForChangesTimeoutMin(" + waitForChangesTimeoutMin + ")! Adjusting waitForChangesTimeoutMin!");
			setWaitForChangesTimeoutMin(waitForChangesTimeoutMax);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("The Cache settings are:");
			logger.debug("      notificationIntervalMSec=" + notificationIntervalMSec);
//			LOGGER.debug("      cacheSessionContainerCheckIntervalMSec=" + cacheSessionContainerCheckIntervalMSec);
			logger.debug("      cacheSessionContainerActivityMSec=" + cacheSessionContainerActivityMSec);
			logger.debug("      cacheSessionContainerCount=" + cacheSessionContainerCount);
			logger.debug("      freshDirtyObjectIDContainerActivityMSec=" + freshDirtyObjectIDContainerActivityMSec);
			logger.debug("      freshDirtyObjectIDContainerCount=" + freshDirtyObjectIDContainerCount);
			logger.debug("      waitForChangesTimeoutMin=" + waitForChangesTimeoutMin);
			logger.debug("      waitForChangesTimeoutMax=" + waitForChangesTimeoutMax);
			logger.debug("      jdoCacheBridgeClassName=" + jdoCacheBridgeClassName);
		}
	}

	public long getDelayNotificationMSec()
	{
		return delayNotificationMSec;
	}
	public void setDelayNotificationMSec(long delayNotificationMSec)
	{
		this.delayNotificationMSec = delayNotificationMSec;
		setChanged();
	}

	/**
	 * @return Returns the cacheSessionContainerActivityMSec.
	 */
	public long getCacheSessionContainerActivityMSec()
	{
		return cacheSessionContainerActivityMSec;
	}
	/**
	 * @param cacheSessionContainerActivityMSec The cacheSessionContainerActivityMSec to set.
	 */
	public void setCacheSessionContainerActivityMSec(
			long cacheSessionContainerActivityMSec)
	{
		this.cacheSessionContainerActivityMSec = cacheSessionContainerActivityMSec;
		setChanged();
	}
//	/**
//	 * @return Returns the cacheSessionContainerCheckIntervalMSec.
//	 */
//	public long getCacheSessionContainerCheckIntervalMSec()
//	{
//		return cacheSessionContainerCheckIntervalMSec;
//	}
//	/**
//	 * @param cacheSessionContainerCheckIntervalMSec The cacheSessionContainerCheckIntervalMSec to set.
//	 */
//	public void setCacheSessionContainerCheckIntervalMSec(
//			long cacheSessionContainerCheckIntervalMSec)
//	{
//		this.cacheSessionContainerCheckIntervalMSec = cacheSessionContainerCheckIntervalMSec;
//		setChanged();
//	}
	/**
	 * @return Returns the cacheSessionContainerCount.
	 */
	public int getCacheSessionContainerCount()
	{
		return cacheSessionContainerCount;
	}
	/**
	 * @param cacheSessionContainerCount The cacheSessionContainerCount to set.
	 */
	public void setCacheSessionContainerCount(int cacheSessionContainerCount)
	{
		this.cacheSessionContainerCount = cacheSessionContainerCount;
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
	}
	/**
	 * @return Returns the notificationIntervalMSec.
	 */
	public long getNotificationIntervalMSec()
	{
		return notificationIntervalMSec;
	}
	/**
	 * @param notificationIntervalMSec The notificationIntervalMSec to set.
	 */
	public void setNotificationIntervalMSec(long notificationIntervalMSec)
	{
		this.notificationIntervalMSec = notificationIntervalMSec;
		setChanged();
	}
	/**
	 * @return Returns the waitForChangesTimeoutMax.
	 */
	public long getWaitForChangesTimeoutMax()
	{
		return waitForChangesTimeoutMax;
	}
	/**
	 * @param waitForChangesTimeoutMax The waitForChangesTimeoutMax to set.
	 */
	public void setWaitForChangesTimeoutMax(long waitForChangesTimeoutMax)
	{
		this.waitForChangesTimeoutMax = waitForChangesTimeoutMax;
		setChanged();
	}
	/**
	 * @return Returns the waitForChangesTimeoutMin.
	 */
	public long getWaitForChangesTimeoutMin()
	{
		return waitForChangesTimeoutMin;
	}
	/**
	 * @param waitForChangesTimeoutMin The waitForChangesTimeoutMin to set.
	 */
	public void setWaitForChangesTimeoutMin(long waitForChangesTimeoutMin)
	{
		this.waitForChangesTimeoutMin = waitForChangesTimeoutMin;
		setChanged();
	}
	/**
	 * @return Returns the jdoCacheBridgeClassName.
	 */
	public String getJdoCacheBridgeClassName()
	{
		return jdoCacheBridgeClassName;
	}
	/**
	 * @param jdoCacheBridgeClassName The jdoCacheBridgeClassName to set.
	 */
	public void setJdoCacheBridgeClassName(String jdoCacheBridgeClassName)
	{
		this.jdoCacheBridgeClassName = jdoCacheBridgeClassName;
		setChanged();
	}

	public int getFreshDirtyObjectIDContainerCount()
	{
		return freshDirtyObjectIDContainerCount;
	}
	public void setFreshDirtyObjectIDContainerCount(
			int freshDirtyObjectIDContainerCount)
	{
		this.freshDirtyObjectIDContainerCount = freshDirtyObjectIDContainerCount;
		setChanged();
	}

	public long getFreshDirtyObjectIDContainerActivityMSec()
	{
		return freshDirtyObjectIDContainerActivityMSec;
	}
	public void setFreshDirtyObjectIDContainerActivityMSec(
			long freshDirtyObjectIDContainerActivityMSec)
	{
		this.freshDirtyObjectIDContainerActivityMSec = freshDirtyObjectIDContainerActivityMSec;
		setChanged();
	}
}
