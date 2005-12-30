/*
 * Created on Sep 16, 2005
 */
package org.nightlabs.ipanema.jdo.organisationsync;

import java.util.Collection;

import javax.jdo.PersistenceManager;

public abstract class OrganisationSyncDelegate
{
	private OrganisationSyncManagerFactory organisationSyncManagerFactory;

	public OrganisationSyncDelegate() { }

	/**
	 * This method is called once upon creation.
	 *
	 * @param factory
	 */
	public void init(OrganisationSyncManagerFactory factory)
	{
		this.organisationSyncManagerFactory = factory;
	}

	public OrganisationSyncManagerFactory getOrganisationSyncManagerFactory()
	{
		return organisationSyncManagerFactory;
	}

	/**
	 * This method is called by
	 * {@link org.nightlabs.ipanema.jdo.organisationsync.OrganisationSyncManagerFactory#processDirtyObjects()}
	 * in order to notify the remote organisation.
	 *
	 * @param organisationID The organisationID that needs to be notified.
	 * @param dirtyObjectIDCarriers Instances of {@link DirtyObjectIDCarrier}.
	 * @throws Exception TODO
	 */
	public abstract void notifyDirtyObjectIDs(PersistenceManager persistenceManager, String organisationID, Collection dirtyObjectIDCarriers) throws Exception;
}
