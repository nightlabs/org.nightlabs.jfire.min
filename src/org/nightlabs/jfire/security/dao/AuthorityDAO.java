package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.security.id.AuthorityID;
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

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Authority> retrieveJDOObjects(
			Set<AuthorityID> authorityIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		UserManager um = userManager;
		if (um == null)
			um = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

		return um.getAuthorities(authorityIDs, fetchGroups, maxFetchDepth);
	}

	private UserManager userManager;

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
}
