/*
 * Created on Apr 15, 2005
 */
package org.nightlabs.ipanema.base.jdo.notification;

import java.util.Collection;

import org.nightlabs.base.notification.NotificationManager;
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
 *  	Whenever you manipulate a JDO object, notify the <tt>ChangeManager</tt>
 *  	with the object-id-instance.
 *		<strong>IMPORTANT CHANGE: You do not need to fire change events anymore!
 *		The JFire-Cache
 *		automatically triggers change notifications whenever objects change in
 *		the server.</strong>
 *	</li>
 *	<li>
 *		In your listener, you check, whether you currently display the object which is
 *		referenced by the given ID. If so, reload it from the server.
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
public class ChangeManager extends NotificationManager
{
	private static ChangeManager _sharedInstance = null;

	public static ChangeManager sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new ChangeManager();

		return _sharedInstance;
	}

	protected ChangeManager()
	{
	}

	public void notify(NotificationEvent event)
	{
		if (!(event instanceof ChangeEvent))
			throw new IllegalArgumentException("event must be an instance of ChangeEvent, but is " + (event == null ? null : event.getClass()));

		super.notify(event);
	}

	protected NotificationEvent createNotificationEvent(Object source, String zone, Collection subjects, Collection subjectClassesToClear, Collection _subjectCarriers)
	{
		return new ChangeEvent(source, zone, subjects,
				subjectClassesToClear, _subjectCarriers);
	}
}
