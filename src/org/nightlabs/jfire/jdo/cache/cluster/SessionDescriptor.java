package org.nightlabs.jfire.jdo.cache.cluster;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.jdo.cache.CacheSession;
import org.nightlabs.jfire.jdo.notification.AbsoluteFilterID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;

/**
 * Descriptor holding all information for a {@link CacheSession} that is needed for fail-over.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class SessionDescriptor
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String sessionID;
//	private long timestamp;
	private Set<Object> subscribedObjectIDs;
	private Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> filters;

	public SessionDescriptor(CacheSession cacheSession)
	{
		this.sessionID = cacheSession.getSessionID();
		this.subscribedObjectIDs = cacheSession.getSubscribedObjectIDs();
		this.filters = cacheSession.getFilters();
//		this.timestamp = System.currentTimeMillis();
	}

	public String getSessionID() {
		return sessionID;
	}

//	public long getTimestamp() {
//		return timestamp;
//	}

	public Set<Object> getSubscribedObjectIDs() {
		return subscribedObjectIDs;
	}

	public Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> getFilters() {
		return filters;
	}
}
