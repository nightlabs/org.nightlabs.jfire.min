/*
 * Created on Oct 7, 2005
 */
package org.nightlabs.ipanema.base.jdo.notification;

import java.util.Set;

import org.nightlabs.notification.SubjectCarrier;

public class ChangeSubjectCarrier extends SubjectCarrier
{
	private Set sourceSessionIDs;
	private long createTimestamp = System.currentTimeMillis();

	public ChangeSubjectCarrier(Set sourceSessionIDs, Object subject)
	{
		super(subject);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set sourceSessionIDs, Object subject, boolean inheritanceIgnored)
	{
		super(subject, inheritanceIgnored);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set sourceSessionIDs, Object subject, boolean inheritanceIgnored,
			boolean interfacesIgnored)
	{
		super(subject, inheritanceIgnored, interfacesIgnored);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set sourceSessionIDs, Class subjectClass)
	{
		super(subjectClass);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set sourceSessionIDs, Class subjectClass, boolean inheritanceIgnored)
	{
		super(subjectClass, inheritanceIgnored);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set sourceSessionIDs, Class subjectClass, boolean inheritanceIgnored,
			boolean interfacesIgnored)
	{
		super(subjectClass, inheritanceIgnored, interfacesIgnored);
		setSessionIDs(sourceSessionIDs);
	}

	public ChangeSubjectCarrier(Set sourceSessionIDs, Object subject, Class subjectClass)
	{
		super(subject, subjectClass);
		setSessionIDs(sourceSessionIDs);
	}

	private void setSessionIDs(Set sourceSessionIDs)
	{
		if (sourceSessionIDs == null)
			throw new IllegalArgumentException("sourceSessionIDs must not be null!");

		this.sourceSessionIDs = sourceSessionIDs;
	}

	public Set getSourceSessionIDs()
	{
		return sourceSessionIDs;
	}

	public long getCreateTimestamp()
	{
		return createTimestamp;
	}
}
