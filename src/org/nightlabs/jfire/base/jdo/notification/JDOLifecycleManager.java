package org.nightlabs.jfire.base.jdo.notification;

import org.nightlabs.base.notification.NotificationManager;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.jdo.cache.DirtyObjectID;
import org.nightlabs.notification.NotificationEvent;

/**
 * Use the shared instance of this manager to get notified
 * about changes on JDO object or similar objects where
 * you can't register a change listener directly.
 * <p>
 * For many objects which are obtained from the server, there exist
 * multiple instances in the client. Often, these instances even are
 * replaced with every change (because a new copy is loaded from the server).
 * Hence, you can't use a property change support within these objects or
 * similar mechanisms.
 * <p>
 * To handle JDO object changes with this manager works as follows:
 * <ul>
 * 	<li>
 * 		Register a listener on the object-id-class of the JDO object that interests
 * 		you.
 * 	</li>
 * 	<li>
 *  	The JFire-Cache automatically triggers change notifications whenever objects change in
 *		the server (you don't need to take care about that). You only need to put all objects
 *		which you retrieve into the {@link Cache} for this feature to work.
 *	</li>
 *	<li>
 *		In your listener, you check, whether you currently display the object which is
 *		referenced by the given ID. If so, reload it from the server.
 *	</li>
 *	<li>
 *		Note that {@link NotificationEvent#getSubjects()} will always return instances of {@link DirtyObjectID}. Hence,
 *		you can check via {@link DirtyObjectID#getLifecycleType()} what exactly happened: The jdo object could have
 *		become dirty (changed on the server / attached) or deleted from the datastore.
 *	</li>
 * </ul>
 * When registering a listener, you can define, how the listener should be called
 * (<tt>notificationMode</tt>): This means on which thread and whether to wait or not.
 * In most cases, it makes sense to use a worker thread, because then you don't need to
 * manually handle the reload-process asnychronously (and you shouldn't do it on the GUI
 * thread, because it's expensive).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JDOLifecycleManager
		extends NotificationManager
{
	private static JDOLifecycleManager _sharedInstance = null;

	public static JDOLifecycleManager sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new JDOLifecycleManager();

		return _sharedInstance;
	}

	protected JDOLifecycleManager()
	{
	}
}
