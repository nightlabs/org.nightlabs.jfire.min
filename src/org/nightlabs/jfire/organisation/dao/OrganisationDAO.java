package org.nightlabs.jfire.organisation.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.login.JFireLoginProvider;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.OrganisationManager;
import org.nightlabs.jfire.organisation.OrganisationManagerUtil;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.progress.ProgressMonitor;

public class OrganisationDAO extends BaseJDOObjectDAO<OrganisationID, Organisation>
{
	private static OrganisationDAO sharedInstance = null;

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

	@Implement
	protected Collection<Organisation> retrieveJDOObjects(
			Set<OrganisationID> organisationIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		OrganisationManager organisationManager = this.organisationManager;
		if (organisationManager == null)
			organisationManager = OrganisationManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();

		return organisationManager.getOrganisations(organisationIDs, fetchGroups, maxFetchDepth);
	}

	private OrganisationManager organisationManager = null;

	public synchronized List<Organisation> getOrganisations(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		int restWorked = 2;
		monitor.beginTask("Loading Organisations", 2);
		try {
			try {
				organisationManager = OrganisationManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();
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
}
