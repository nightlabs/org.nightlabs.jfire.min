package org.nightlabs.jfire.base.organisation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.OrganisationManager;
import org.nightlabs.jfire.organisation.OrganisationManagerUtil;
import org.nightlabs.jfire.organisation.id.OrganisationID;

public class OrganisationDAO
		extends JDOObjectDAO<OrganisationID, Organisation>
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
			IProgressMonitor monitor)
			throws Exception
	{
		OrganisationManager organisationManager = this.organisationManager;
		if (organisationManager == null)
			organisationManager = OrganisationManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();

		return organisationManager.getOrganisations(organisationIDs, fetchGroups, maxFetchDepth);
	}

	private OrganisationManager organisationManager = null;

	public synchronized List<Organisation> getOrganisations(String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		int restWorked = 2;
		monitor.beginTask("Loading Organisations", 2);
		try {
			try {
				organisationManager = OrganisationManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
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

	public List<Organisation> getOrganisations(Collection<OrganisationID> organisationIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
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
