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

import java.util.Set;

import org.nightlabs.notification.SubjectCarrier;

public class ChangeSubjectCarrier extends SubjectCarrier
{
	private Set<?> sourceSessionIDs;
	private long createTimestamp = System.currentTimeMillis();

	public ChangeSubjectCarrier(Set<?> sourceSessionIDs, Object subject)
	{
		super(subject);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set<?> sourceSessionIDs, Object subject, boolean inheritanceIgnored)
	{
		super(subject, inheritanceIgnored);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set<?> sourceSessionIDs, Object subject, boolean inheritanceIgnored,
			boolean interfacesIgnored)
	{
		super(subject, inheritanceIgnored, interfacesIgnored);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set<?> sourceSessionIDs, Class<?> subjectClass)
	{
		super(subjectClass);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set<?> sourceSessionIDs, Class<?> subjectClass, boolean inheritanceIgnored)
	{
		super(subjectClass, inheritanceIgnored);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set<?> sourceSessionIDs, Class<?> subjectClass, boolean inheritanceIgnored,
			boolean interfacesIgnored)
	{
		super(subjectClass, inheritanceIgnored, interfacesIgnored);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set<?> sourceSessionIDs, Object subject, Class<?> subjectClass)
	{
		super(subject, subjectClass);
		setSessionIDs(sourceSessionIDs);
	}

	private void setSessionIDs(Set<?> sourceSessionIDs)
	{
		if (sourceSessionIDs == null)
			throw new IllegalArgumentException("sourceSessionIDs must not be null!");

		this.sourceSessionIDs = sourceSessionIDs;
	}

	public Set<?> getSourceSessionIDs()
	{
		return sourceSessionIDs;
	}

	public long getCreateTimestamp()
	{
		return createTimestamp;
	}
}
