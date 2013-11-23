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

import org.apache.log4j.Logger;
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
	private static final Logger logger = Logger.getLogger(JDOObjectID2PCClassNotificationInterceptor.class);

	private JDOManagerProvider jdoManagerProvider;
	
	public JDOObjectID2PCClassNotificationInterceptor(JDOManagerProvider jdoManagerProvider)
	{
		this.jdoManagerProvider = jdoManagerProvider;
	}
	
	public NotificationEvent intercept(NotificationEvent event)
	{
		for (Iterator<SubjectCarrier> itSubjectCarriers = event.getSubjectCarriers().iterator(); itSubjectCarriers.hasNext(); ) {
			SubjectCarrier subjectCarrier = itSubjectCarriers.next();
			Object subject = subjectCarrier.getSubject();

 			if (subject instanceof ObjectID) {
 				Class<?> jdoObjectClass = jdoManagerProvider.getObjectID2PCClassMap().getPersistenceCapableClass(subject);
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

 				if (!(jdoObjectID instanceof ObjectID)) {
 					// After I searched quite a while for an exception (a jdoObjectID was an instance of java.lang.String and
 					// no jdo object could be loaded by PersistenceManager.getObjectById(...), of course)
 					// I'd like to mention here, that Cache$NotificationThread.run() creates "synthetic" DirtyObjectIDs
 					// (stored in the Map indirectlyAffectedDirtyObjectIDs). As everything can be put into the cache,
 					// it might well be possible that an object has a key assigned, which is no jdo object id and for which
 					// no jdo object exists (e.g. a Collection or Map cached by an artificial key). That's why, we must not
 					// try to get the PersistenceCapableClass for object ids which are not explicitely tagged by org.nightlabs.jdo.ObjectID.
 					if (logger.isDebugEnabled())
 						logger.debug("jdoObjectID does not implement " + ObjectID.class.getName() + "! It is an instance of " + (jdoObjectID == null ? null : jdoObjectID.getClass().getName()) + ": " + jdoObjectID);
 				}
 				else {
	 				Class<?> jdoObjectClass = jdoManagerProvider.getObjectID2PCClassMap().getPersistenceCapableClass(jdoObjectID);
	 				subjectCarrier.getSubjectClasses().add(jdoObjectClass);
 				}
 			}
		} // for (Iterator itSubjectCarriers = event.getSubjectCarriers().iterator(); itSubjectCarriers.hasNext(); ) {

		return null;
	}
}
