package org.nightlabs.jfire.organisation.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * DAO object to retrieve and store {@link Organisation}s.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author unascribed
 */
public class OrganisationDAO extends BaseJDOObjectDAO<OrganisationID, Organisation> implements IJDOObjectDAO<Organisation>
{
	private static OrganisationDAO sharedInstance = null;
	/**
	 * @return The shared instance of {@link OrganisationDAO}.
	 */
	public static OrganisationDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (OrganisationDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new OrganisationDAO();
			}
		}
		return sharedInstance;
	}

	protected OrganisationDAO() { }

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<Organisation> retrieveJDOObjects(
			Set<OrganisationID> organisationIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		OrganisationManagerRemote organisationManager = this.organisationManager;
		if (organisationManager == null)
			organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return organisationManager.getOrganisations(organisationIDs, fetchGroups, maxFetchDepth);
	}

	/**
	 * This member is used in for subsequent calls, for example to first query IDs and then the objects.
	 */
	private OrganisationManagerRemote organisationManager = null;

	/**
	 * Get the list of all {@link Organisation}s known to (in cooperation with) the current {@link Organisation}.
	 *
	 * @param fetchGroups The fetch-groups to detach the found {@link Organisation}s with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 * @param monitor The monitor to report progress to.
	 * @return The list of all {@link Organisation}s known to (in cooperation with) the current {@link Organisation}.
	 */
	public synchronized List<Organisation> getOrganisations(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		int restWorked = 2;
		monitor.beginTask("Loading Organisations", 2);
		try {
			try {
				organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, SecurityReflector.getInitialContextProperties());
				try {
					Set<OrganisationID> organisationIDs = organisationManager.getOrganisationIDs();
					monitor.worked(1); --restWorked;
					return getJDOObjects(null, organisationIDs, fetchGroups, maxFetchDepth, monitor);
				} finally {
					organisationManager = null;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} finally {
			monitor.worked(restWorked);
		}
	}

	/**
	 * Get the detached {@link Organisation} objects for the given {@link OrganisationID}s from the server/cache.
	 *
	 * @param organisationIDs The {@link OrganisationID}s to fetch.
	 * @param fetchGroups The fetch-groups to detach the {@link Organisation}s with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 * @param monitor The monitor to report progress to.
	 * @return The detached {@link Organisation} objects for the given {@link OrganisationID}s from the server/cache.
	 */
	public List<Organisation> getOrganisations(Collection<OrganisationID> organisationIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (organisationIDs == null)
			return getOrganisations(fetchGroups, maxFetchDepth, monitor);

		monitor.beginTask("Loading Organisations", 1);
		try {
			return getJDOObjects(null, organisationIDs, fetchGroups, maxFetchDepth, monitor);
		} finally {
			monitor.worked(1);
		}
	}

	/**
	 * Get the detached {@link Organisation} the given {@link OrganisationID} from the server/cache.
	 * @param organisationID The {@link OrganisationID} of the {@link Organisation} to fetch.
	 * @param fetchGroups The fetch-groups to detach the {@link Organisation} with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 * @param monitor The monitor to report progress to.
	 * @return The detached {@link Organisation} the given {@link OrganisationID} from the server/cache.
	 */
	public Organisation getOrganisation(OrganisationID organisationID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (organisationID == null)
			throw new IllegalArgumentException("Parameter organisationID must not be null.");

		monitor.beginTask("Loading Organisation", 1);
		try {
			return getJDOObject(null, organisationID, fetchGroups, maxFetchDepth, monitor);
		} finally {
			monitor.worked(1);
		}
	}

	/**
	 * Stores the given {@link Organisation} if it is the local organisation of the calling user.
	 * Optionally returns a detached copy of the new version.
	 *
	 * @param organisation The {@link Organisation} to store.
	 * @param get Whether to return a detached copy of the new verison of the given {@link Organisation}.
	 * @param fetchGroups The fetch-groups to detach the {@link Organisation} with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 * @param monitor The monitor to report progress to.
	 * @return A detached copy of the new version of the given {@link Organisation}, or <code>null</code> if get is <code>false</code>.
	 */
	public Organisation storeLocalOrganisation(Organisation organisation, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			OrganisationManagerRemote organisationManager = this.organisationManager;
			if (organisationManager == null)
				organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, SecurityReflector.getInitialContextProperties());

			return organisationManager.storeLocalOrganisation(organisation, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Note, that this implementation will only work for the local organisation of the calling user.
	 * </p>
	 * @see org.nightlabs.jfire.base.jdo.IJDOObjectDAO#storeJDOObject(java.lang.Object, boolean, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public Organisation storeJDOObject(Organisation jdoObject, boolean get,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return storeLocalOrganisation(jdoObject, get, fetchGroups, maxFetchDepth, monitor);
	}

}
