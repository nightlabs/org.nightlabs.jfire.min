/*
 * Created on May 7, 2005
 */
package org.nightlabs.jfire.base.jdo;

import java.util.Iterator;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.notification.Interceptor;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.SubjectCarrier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JDOObjectID2PCClassNotificationInterceptor
implements Interceptor
{
	/**
	 * @see org.nightlabs.notification.Interceptor#intercept(org.nightlabs.notification.NotificationEvent)
	 */
	public NotificationEvent intercept(NotificationEvent event)
	{
		for (Iterator itSubjectCarriers = event.getSubjectCarriers().iterator(); itSubjectCarriers.hasNext(); ) {
			SubjectCarrier subjectCarrier = (SubjectCarrier) itSubjectCarriers.next();
			Object subject = subjectCarrier.getSubject();

 			if (subject instanceof ObjectID) {
 				Class jdoObjectClass = JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(subject);
 				subjectCarrier.getSubjectClasses().add(jdoObjectClass);
 			} // if (subject instanceof ObjectID) {
		} // for (Iterator itSubjectCarriers = event.getSubjectCarriers().iterator(); itSubjectCarriers.hasNext(); ) {

		return null;
	}

}
