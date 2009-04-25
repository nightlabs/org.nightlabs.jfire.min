package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class AuthorityTypeDAO extends BaseJDOObjectDAO<AuthorityTypeID, AuthorityType>
{
	private static AuthorityTypeDAO sharedInstance;
	public static AuthorityTypeDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new AuthorityTypeDAO();
		return sharedInstance;
	}

	@Override
	protected Collection<AuthorityType> retrieveJDOObjects(
			Set<AuthorityTypeID> authorityTypeIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		JFireSecurityManagerRemote sm = securityManager;
		if (sm == null)
			sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return sm.getAuthorityTypes(authorityTypeIDs, fetchGroups, maxFetchDepth);
	}

	private JFireSecurityManagerRemote securityManager;

	public synchronized List<AuthorityType> getAuthorityTypes(
			String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			securityManager = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<AuthorityTypeID> authorityTypeIDs = securityManager.getAuthorityTypeIDs();
			return getJDOObjects(null, authorityTypeIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			securityManager = null;
		}
	}

	public List<AuthorityType> getAuthorityTypes(
			Set<AuthorityTypeID> authorityTypeIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, authorityTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public AuthorityType getAuthorityType(
			AuthorityTypeID authorityTypeID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, authorityTypeID, fetchGroups, maxFetchDepth, monitor);
	}
}
