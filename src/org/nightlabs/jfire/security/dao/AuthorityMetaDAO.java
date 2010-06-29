package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.AuthorityMeta;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityMetaID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class AuthorityMetaDAO extends BaseJDOObjectDAO<AuthorityMetaID, AuthorityMeta>
{
	private static AuthorityMetaDAO sharedInstance;
	public static AuthorityMetaDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new AuthorityMetaDAO();
		return sharedInstance;
	}

	private AuthorityMetaDAO() {}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<? extends AuthorityMeta> retrieveJDOObjects(
			Set<AuthorityMetaID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		JFireSecurityManagerRemote jsm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,
				SecurityReflector.getInitialContextProperties());
		return jsm.getAuthorityMetas(objectIDs, fetchGroups, maxFetchDepth);
	}

	public Collection<? extends AuthorityMeta> getAuthorityMetas(
			Set<AuthorityMetaID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			monitor.beginTask("Load Authority Meta Data", 100);
			Collection<? extends AuthorityMeta> authorityMetas = getJDOObjects(null, objectIDs, fetchGroups, maxFetchDepth, monitor);
			monitor.worked(100);
			return authorityMetas;
		} finally {
			monitor.done();
		}
	}

	public AuthorityMeta getAuthorityMeta(AuthorityMetaID authorityMetaID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			monitor.beginTask("Load Authority Meta Data", 100);
			AuthorityMeta authorityMeta = getJDOObject(null, authorityMetaID, fetchGroups, maxFetchDepth, monitor);
			monitor.worked(100);
			return authorityMeta;
		} finally {
			monitor.done();
		}
	}

	public AuthorityMeta storeAuthorityMeta(AuthorityMeta authorityMeta, boolean get, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			monitor.beginTask("Save Authority Meta Data", 100);
			JFireSecurityManagerRemote jsm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,
					SecurityReflector.getInitialContextProperties());
			authorityMeta = jsm.storeAuthorityMeta(authorityMeta, get, fetchGroups, maxFetchDepth);
			monitor.worked(100);
			return authorityMeta;
		} finally {
			monitor.done();
		}
	}

	public AuthorityMeta getAuthorityMetaForAuthority(AuthorityID authorityID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			monitor.beginTask("Load Authority Meta Data", 100);
			JFireSecurityManagerRemote jsm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,
					SecurityReflector.getInitialContextProperties());
			AuthorityMeta authorityMeta = jsm.getAuthorityMeta(authorityID, fetchGroups, maxFetchDepth);
			monitor.worked(100);
			return authorityMeta;
		} finally {
			monitor.done();
		}
	}
}
