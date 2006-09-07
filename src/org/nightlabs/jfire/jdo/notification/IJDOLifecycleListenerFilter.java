package org.nightlabs.jfire.jdo.notification;

import java.io.Serializable;
import java.util.Collection;

import org.nightlabs.jfire.jdo.cache.DirtyObjectID;

/**
 * An implementation of this interface filters JDO lifecycle events on the server side
 * in order to reduce the traffic to the client.
 * <p>
 * It's recommended to extend {@link JDOLifecycleListenerFilter} instead of implementing
 * this interface directly.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IJDOLifecycleListenerFilter
extends Serializable
{
	/**
	 * This method is called automatically by the client-side <code>JDOLifecycleManager</code>
	 * when this filter is registered.
	 *
	 * @param filterID A unique ID.
	 */
	void setFilterID(AbsoluteFilterID filterID);
	/**
	 * @return The id that has previously been assigned by {@link #setFilterID(long)}. Returns <code>null</code>, before
	 *		{@link #setFilterID(long)} was called.
	 */
	AbsoluteFilterID getFilterID();

	/**
	 * @return The classes of JDO objects about which the client-sided listener wants to get notified.
	 *		This must not be <code>null</code> and it must not be empty. It is urgently recommended, not
	 *		to use <code>Object</code> here, but be as specific as possible.
	 * @see #includeSubclasses()
	 */
	Class[] getCandidateClasses();

	/**
	 * If this method returns <code>false</code>, this listener will only be triggered
	 * when the jdo object's class exactly matches one of the given candidate classes.
	 * If this method returns <code>true</code>, this listener will be triggered for
	 * subclasses of the given candidate classes, as well.
	 *
	 * @return Whether or not to include subclasses of the given candidate classes.
	 */
	boolean includeSubclasses();

	/**
	 * @return The lifecycle stages in which the listener is interested. This must not be <code>null</code> and it must not be empty, too.
	 */
	DirtyObjectID.LifecycleStage[] getLifecycleStages();

	/**
	 * This method checks which ones of the given {@link DirtyObjectID}s require
	 * to be forwarded to the client-sided <code>JDOLifecycleListener</code>.
	 * Note that {@link JDOLifecycleRemoteEvent#getDirtyObjectIDs()} contains
	 * only those instances that match already the other criteria of this filter
	 * (e.g. the candidate classes and the lifecycle types).
	 *
	 * @param event The event containing information about what happened.
	 * @return Those {@link DirtyObjectID}s that shall be forwarded to the client.
	 *		This can be empty or even <code>null</code> (they're understood as equivalent).
	 */
	Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event);
}
