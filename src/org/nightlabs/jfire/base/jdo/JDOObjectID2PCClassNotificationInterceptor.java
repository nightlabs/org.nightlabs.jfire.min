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

package org.nightlabs.jfire.base.jdo;

import java.util.Iterator;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.notification.Interceptor;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.SubjectCarrier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JDOObjectID2PCClassNotificationInterceptor
implements Interceptor
{
	public NotificationEvent intercept(NotificationEvent event)
	{
		for (Iterator itSubjectCarriers = event.getSubjectCarriers().iterator(); itSubjectCarriers.hasNext(); ) {
			SubjectCarrier subjectCarrier = (SubjectCarrier) itSubjectCarriers.next();
			Object subject = subjectCarrier.getSubject();

 			if (subject instanceof ObjectID) {
 				Class jdoObjectClass = JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(subject);
 				subjectCarrier.getSubjectClasses().add(jdoObjectClass);
 			} // if (subject instanceof ObjectID) {
 			else if (subject instanceof DirtyObjectID) {
 				DirtyObjectID dirtyObjectID = (DirtyObjectID) subject;

 				// We remove the DirtyObjectID class, because it is not needed and thus, this removal should make
 				// the notification a little bit faster (only 2 classes have to be searched instead of 3). If it
 				// ever becomes needed, we can safely delete the following line (and thus react on DirtyObjectIDs directly).
 				subjectCarrier.getSubjectClasses().remove(DirtyObjectID.class);

 				Object jdoObjectID = dirtyObjectID.getObjectID();
 				subjectCarrier.getSubjectClasses().add(jdoObjectID.getClass());

 				Class jdoObjectClass = JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(jdoObjectID);
 				subjectCarrier.getSubjectClasses().add(jdoObjectClass);
 			}
		} // for (Iterator itSubjectCarriers = event.getSubjectCarriers().iterator(); itSubjectCarriers.hasNext(); ) {

		return null;
	}
}
