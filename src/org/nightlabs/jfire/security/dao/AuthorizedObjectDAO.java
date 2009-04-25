package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class AuthorizedObjectDAO extends BaseJDOObjectDAO<AuthorizedObjectID, AuthorizedObject>
{
	private static AuthorizedObjectDAO sharedInstance;
	public static AuthorizedObjectDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new AuthorizedObjectDAO();
		return sharedInstance;
	}

	private JFireSecurityManagerRemote jfireSecurityManager;

	@Override
	protected Collection<AuthorizedObject> retrieveJDOObjects(
			Set<AuthorizedObjectID> authorizedObjectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("", 1);
		try {
			JFireSecurityManagerRemote m = jfireSecurityManager;
			if (m == null)
				m = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());

			return m.getAuthorizedObjects(authorizedObjectIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized List<AuthorizedObject> getAuthorizedObjects(
			String[] fetchGroups,
			int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading authorized objects", 100);
		try {
			jfireSecurityManager = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(10);
			Set<AuthorizedObjectID> authorizedObjectIDs = jfireSecurityManager.getAuthorizedObjectIDs();
			monitor.worked(30);
			return getJDOObjects(null, authorizedObjectIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 60));
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			jfireSecurityManager = null;
			monitor.done();
		}
	}

	public List<AuthorizedObject> getAuthorizedObjects(
			Set<AuthorizedObjectID> authorizedObjectIDs,
			String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObjects(null, authorizedObjectIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public AuthorizedObject getAuthorizedObject(
			AuthorizedObjectID authorizedObjectID,
			String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObject(null, authorizedObjectID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Set which {@link RoleGroup}s are assigned to a certain {@link AuthorizedObject} within the scope of a certain {@link Authority}.
	 * <p>
	 * The assignment of {@link RoleGroup}s to {@link AuthorizedObject}s is managed by {@link RoleGroupRef} and {@link AuthorizedObjectRef} instances
	 * which live within an {@link Authority}. This method removes the {@link AuthorizedObjectRef} (and with it all assignments), if
	 * the given <code>roleGroupIDs</code> argument is <code>null</code>. If the <code>roleGroupIDs</code> argument is not <code>null</code>,
	 * a {@link AuthorizedObjectRef} instance is created - even if the <code>roleGroupIDs</code> is an empty set.
	 * </p>
	 *
	 * @param userID the user-id. Must not be <code>null</code>.
	 * @param authorityID the authority-id. Must not be <code>null</code>.
	 * @param roleGroupIDs the role-group-ids that should be assigned to the specified user within the scope of the specified
	 *		authority. If this is <code>null</code>, the {@link AuthorizedObjectRef} of the specified user will be removed from the {@link Authority}.
	 *		If this is not <code>null</code>, a <code>AuthorizedObjectRef</code> is created (if not yet existing).
	 * @param monitor the progress monitor for feedback.
	 */
	public synchronized void setGrantedRoleGroups(AuthorizedObjectID authorizedObjectID, AuthorityID authorityID, Set<RoleGroupID> roleGroupIDs, ProgressMonitor monitor)
	{
		monitor.beginTask("Setting granted role groups within an authority.", 1);
		try {
			JFireSecurityManagerRemote um = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());
			um.setGrantedRoleGroups(authorizedObjectID, authorityID, roleGroupIDs);
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
}
