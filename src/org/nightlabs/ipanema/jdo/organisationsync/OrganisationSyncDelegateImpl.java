/*
 * Created on Sep 16, 2005
 */
package org.nightlabs.ipanema.jdo.organisationsync;

import java.util.Collection;

import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.base.Lookup;
import org.nightlabs.ipanema.jdo.JDOManager;
import org.nightlabs.ipanema.jdo.JDOManagerUtil;

/**
 * This class must be defined in this project in order to be able to access the beans.
 * That's why its class name is hardcoded in
 * {@link org.nightlabs.ipanema.jdo.organisationsync.OrganisationSyncManagerFactory}.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class OrganisationSyncDelegateImpl extends OrganisationSyncDelegate
{

	public OrganisationSyncDelegateImpl() { }

	/**
	 * @see org.nightlabs.ipanema.jdo.organisationsync.OrganisationSyncDelegate#notifyDirtyObjectIDs(javax.jdo.PersistenceManager, java.lang.String, java.util.Collection)
	 */
	public void notifyDirtyObjectIDs(
			PersistenceManager persistenceManager,
			String organisationID,
			Collection dirtyObjectIDCarriers)
	throws Exception
	{
		JDOManager jdoManager = JDOManagerUtil.getHome(
				Lookup.getInitialContextProps(persistenceManager, organisationID)).create();
		jdoManager.notifyDirtyObjectIDs(dirtyObjectIDCarriers);
	}

}
