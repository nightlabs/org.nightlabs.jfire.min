package org.nightlabs.jfire.base.jdo.notification;

import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;

public interface JDOLifecycleListener
{
//	void setJDOLifecycleListenerFilter(JDOLifecycleListenerFilter filter);

	/**
	 * @return Returns the filter defining in which events this listener is interested.
	 */
	IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter();

	/**
	 * This method is triggered on the client side, whenever a JDO object has been
	 * newly created / modified / deleted and the filter matched on the server-side.
	 *
	 * @param event The event containing detailed information about what happened.
	 */
	void notifyLifecycleEvent(JDOLifecycleEvent event);
}
