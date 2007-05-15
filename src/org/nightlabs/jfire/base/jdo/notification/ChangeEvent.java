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

package org.nightlabs.jfire.base.jdo.notification;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.base.jdo.login.JFireLoginProvider;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.SubjectCarrier;

public class ChangeEvent extends NotificationEvent
{

	public ChangeEvent(Object source, Object subject)
	{
		super(source, subject);
	}

	public ChangeEvent(Object source, String zone, Object subject)
	{
		super(source, zone, subject);
	}

	public ChangeEvent(Object source, Object subject, Class subjectClassToClear)
	{
		super(source, subject, subjectClassToClear);
	}

	public ChangeEvent(Object source, String zone, Object subject,
			Class subjectClassToClear)
	{
		super(source, zone, subject, subjectClassToClear);
	}

	public ChangeEvent(Object source, Object[] subjects)
	{
		super(source, subjects);
	}

	public ChangeEvent(Object source, String zone, Object[] subjects)
	{
		super(source, zone, subjects);
	}

	public ChangeEvent(Object source, Object[] subjects,
			Class[] subjectClassesToClear)
	{
		super(source, subjects, subjectClassesToClear);
	}

	public ChangeEvent(Object source, String zone, Object[] subjects,
			Class[] subjectClassesToClear)
	{
		super(source, zone, subjects, subjectClassesToClear);
	}

	public ChangeEvent(Object source, Collection subjects)
	{
		super(source, subjects);
	}

	public ChangeEvent(Object source, String zone, Collection subjects)
	{
		super(source, zone, subjects);
	}

	public ChangeEvent(Object source, Collection subjects,
			Collection subjectClassesToClear)
	{
		super(source, subjects, subjectClassesToClear);
	}

	public ChangeEvent(Object source, String zone, Collection subjects,
			Collection subjectClassesToClear)
	{
		super(source, zone, subjects, subjectClassesToClear);
	}

	public ChangeEvent(Object source, String zone, Collection subjects,
			Collection subjectClassesToClear, Collection _subjectCarriers)
	{
		super(source, zone, subjects, subjectClassesToClear, _subjectCarriers);
	}

	public ChangeEvent(Object source, String zone, SubjectCarrier subjectCarrier)
	{
		super(source, zone, subjectCarrier);
	}

	public ChangeEvent(Object source, String zone,
			SubjectCarrier[] subjectCarriers)
	{
		super(source, zone, subjectCarriers);
	}

	private Map carriersBySubject = null;

	/**
	 * Whenever an object is changed in the datastore, the server will notify all clients
	 * that are currently interested in the object's modifications. Normally, you would
	 * react in your listener by reloading the changed object in order to stay up to date.
	 * But if you modified the object yourself, then you probably already have the most current
	 * data and therefore do not want to do any work.
	 * <p>
	 * This method checks, whether the given <code>subject</code> has been changed within
	 * a certain time (default 60 sec, see {@link #HANDLING_DEACTIVATION_PERIOD_MSEC})
	 * by you yourself. If this is the case, this method
	 * returns <code>false</code> and thus, you should ignore the subject in this event (and
	 * the whole event, if there is no other subject).
	 * </p>
	 * <p>
	 * You might have several parts in your GUI which modify a certain JDO object. Therefore,
	 * it might happen, that you receive a notification about a change caused by you yourself,
	 * but from a different part of the GUI. That's why, we use the <code>myChangeTimestamp</code>:
	 * Directly before you write sth. to the server, you should save your timestamp somewhere.
	 * When you receive the event, you pass this timestamp here. If the notification happens
	 * within a certain time (60 sec), we assume that it should be ignored, otherwise, we'll
	 * react, assuming that it was another part of the local GUI.
	 * </p>
	 * <p>
	 * You should set your local variable representing <code>myChangeTimestamp</code> to '0'
	 * as soon as your listener was triggered the first time, because it is guaranteed, that one
	 * write action by you, will exactly trigger one notification event (or zero, if there's
	 * a network problem - hence, the timeout).
	 * </p>
	 *
	 * @param subject Any subject (either part of this <code>ChangeEvent</code> or not).
	 * @param lastChangeTimestamp The timestamp of the last change, which you have done to
	 *		the given subject. '0' if it has not been changed at all. Note, that it will be
	 *		reset to '0' (via {@link Date#setTime(long)}), if this event knows the subject.
	 *		If this is <code>null</code>, the method immediately returns with <code>false</code>!
	 * @return Returns whether or not the caller of this method needs to handle this
	 *		event. If <code>subject</code> is not part of this event, this method returns
	 *		<code>false</code>. If <code>subject</code> is part of this event and
	 *		the caller is not the one who changed the subject (or did it before the
	 *		{@link #HANDLING_DEACTIVATION_PERIOD_MSEC}, it returns <code>true</code>.
	 */
	public boolean needsHandling(Object subject, Date lastChangeTimestamp)
	{
		if (lastChangeTimestamp == null)
			return false;

		if (carriersBySubject == null) {
			Map cbs = new HashMap(getSubjectCarriers().size());
			for (Iterator it = getSubjectCarriers().iterator(); it.hasNext(); ) {
				SubjectCarrier subjectCarrier = (SubjectCarrier) it.next();
				Object subj = subjectCarrier.getSubject();
				if (subj != null) // there should not be nulls, but just to be sure (in principle, it's allowed)...
					cbs.put(subj, subjectCarrier);
			}
			carriersBySubject = cbs;
		}

		SubjectCarrier subjectCarrier = (SubjectCarrier) carriersBySubject.get(subject);
		if (subjectCarrier == null) // if the param subject is not known to this event, we don't need to handle this event.
			return false;

		try {
			// The finally block resets myChangeTimestamp to 0, because this event knows the given subject.
			if (subjectCarrier instanceof ChangeSubjectCarrier) {
				ChangeSubjectCarrier csc = (ChangeSubjectCarrier) subjectCarrier;
				Set sourceSessionIDs = csc.getSourceSessionIDs();
	
				if (sourceSessionIDs.isEmpty())
					return true;
	
				if (sourceSessionIDs.size() > 1)
					return true;

				// TODO shouldn't we somehow make sure the listeners don't get unsubscribed by the cache, if we return false here?!
				String sessionID = null;
				try {
					sessionID = JFireLoginProvider.sharedInstance().getSessionID();
				} catch (LoginException e) {
					throw new RuntimeException(e);
				}
				return
				  !sourceSessionIDs.contains(sessionID) ||
					csc.getCreateTimestamp() - lastChangeTimestamp.getTime() > HANDLING_DEACTIVATION_PERIOD_MSEC;
			}
			else
				return true;
		} finally {
			lastChangeTimestamp.setTime(0);
		}
	}

	protected static final long HANDLING_DEACTIVATION_PERIOD_MSEC = 60000;
}
