package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.progress.ProgressMonitor;

public class AuthorityDAO extends BaseJDOObjectDAO<AuthorityID, Authority>
{
	private static AuthorityDAO sharedInstance;
	public static AuthorityDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new AuthorityDAO();
		return sharedInstance;
	}

	@Override
	protected Collection<Authority> retrieveJDOObjects(
			Set<AuthorityID> authorityIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		JFireSecurityManagerRemote um = userManager;
		if (um == null)
			um = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);

		return um.getAuthorities(authorityIDs, fetchGroups, maxFetchDepth);
	}

	private JFireSecurityManagerRemote userManager;

	public synchronized List<Authority> getAuthorities(
			String organisationID, AuthorityTypeID authorityTypeID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			userManager = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);
			Set<AuthorityID> authorityIDs = userManager.getAuthorityIDs(organisationID, authorityTypeID);

			return getJDOObjects(null, authorityIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			userManager = null;
		}
	}

	public List<Authority> getAuthorities(
			Set<AuthorityID> authorityIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, authorityIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public Authority getAuthority(
			AuthorityID authorityID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, authorityID, fetchGroups, maxFetchDepth, monitor);
	}

	public Authority storeAuthority(Authority authority, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Storing authority", 1);
		try {
			JFireSecurityManagerRemote sm = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);
			authority = sm.storeAuthority(authority, get, fetchGroups, maxFetchDepth);

			if (authority != null)
				getCache().put(null, authority, fetchGroups, maxFetchDepth);

			return authority;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized void assignSecuringAuthority(Object securedObjectID, AuthorityID authorityID, boolean inherited, ProgressMonitor monitor)
	{
		monitor.beginTask("Assigning authority to secured object", 1);
		try {
			JFireSecurityManagerRemote sm = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);
			sm.assignSecuringAuthority(securedObjectID, authorityID, inherited);
			getCache().removeByObjectID(securedObjectID, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public void setGrantedRoleGroups(AuthorizedObjectID authorizedObjectID, AuthorityID authorityID, Set<RoleGroupID> roleGroupIDs, ProgressMonitor monitor)
	{
		monitor.beginTask("Setting granted role groups", 1);
		try {
			JFireSecurityManagerRemote sm = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);
			sm.setGrantedRoleGroups(authorizedObjectID, authorityID, roleGroupIDs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
}
