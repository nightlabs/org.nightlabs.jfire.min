package org.nightlabs.jfire.servermanager.createorganisation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.nightlabs.jdo.ObjectIDUtil;

public class CreateOrganisationProgress
implements Serializable
{
	private static final long serialVersionUID = 2L;

	private CreateOrganisationProgressID createOrganisationProgressID = new CreateOrganisationProgressID(ObjectIDUtil.makeValidIDString(null));
	private String organisationID;

	private int stepsTotal = Integer.MAX_VALUE;

	private List<CreateOrganisationStatus> createOrganisationStatusList = new ArrayList<CreateOrganisationStatus>();
	private List<CreateOrganisationStatus> createOrganisationStatusList_readOnly = null;

	private Date begin = new Date();
	private Date done = null;

	public CreateOrganisationProgress(String organisationID)
	{
		if (organisationID == null)
			throw new IllegalArgumentException("organisationID must not be null!");

		this.organisationID = organisationID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public CreateOrganisationProgressID getCreateOrganisationProgressID()
	{
		return createOrganisationProgressID;
	}

	/**
	 * The total number of steps to be performed. This number might change between
	 * subsequent calls, if the JFireServerManagerFactory
	 *
	 * @return
	 */
	public synchronized int getStepsTotal()
	{
		return stepsTotal;
	}

	public synchronized void setStepsTotal(int stepsTotal)
	{
		if (done != null)
			throw new IllegalStateException("done() was already called!");

		if (stepsTotal < 0)
			throw new IllegalArgumentException("stepsTotal < 0");

		this.stepsTotal = stepsTotal;
	}

	public synchronized void incStepsTotal(int stepsTotal)
	{
		if (done != null)
			throw new IllegalStateException("done() was already called!");

		int newStepsTotal = this.stepsTotal + stepsTotal;

		if (newStepsTotal < 0)
			throw new IllegalArgumentException("newStepsTotal < 0");

		this.stepsTotal = newStepsTotal;
	}

	/**
	 * Get a read-only copy of all the status objects stored to this progress.
	 *
	 * @return a read-only copy of {@link CreateOrganisationStatus} objects-{@link List}.
	 */
	public synchronized List<CreateOrganisationStatus> getCreateOrganisationStatusList()
	{
		if (createOrganisationStatusList_readOnly == null)
			createOrganisationStatusList_readOnly = Collections.unmodifiableList(new ArrayList<CreateOrganisationStatus>(createOrganisationStatusList));

		return createOrganisationStatusList_readOnly;
	}

	public synchronized void addCreateOrganisationStatus(CreateOrganisationStatus createOrganisationStatus)
	{
		if (done != null)
			throw new IllegalStateException("done() was already called! Cannot add!");

		createOrganisationStatusList.add(createOrganisationStatus);
		createOrganisationStatusList_readOnly = null;
	}

	/**
	 * @return <code>null</code>, if there is no status; the last one otherwise.
	 */
	public synchronized CreateOrganisationStatus getLastCreateOrganisationStatus()
	{
		if (createOrganisationStatusList.isEmpty())
			return null;

		return createOrganisationStatusList.get(createOrganisationStatusList.size() - 1);
	}

	public Date getBegin()
	{
		return begin;
	}

	public synchronized boolean isDone()
	{
		return done != null;
	}

	public synchronized Date getDone()
	{
		return done;
	}

	public synchronized void done()
	{
		if (done != null)
			throw new IllegalStateException("done() was already called before!");

		done = new Date();
	}
}
