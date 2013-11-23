package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class UserSecurityGroupDAO extends BaseJDOObjectDAO<UserSecurityGroupID, UserSecurityGroup>
{
	private static UserSecurityGroupDAO sharedInstance;
	public static UserSecurityGroupDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new UserSecurityGroupDAO();
		return sharedInstance;
	}

	private JFireSecurityManagerRemote jfireSecurityManager;

	@Override
	protected Collection<UserSecurityGroup> retrieveJDOObjects(
			Set<UserSecurityGroupID> userSecurityGroupIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	throws Exception
	{
		monitor.beginTask("Retrieving user security groups", 1);
		try {
			JFireSecurityManagerRemote m = jfireSecurityManager;
			if (m == null)
				m = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);

			return m.getUserSecurityGroups(userSecurityGroupIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public List<UserSecurityGroup> getUserSecurityGroups(
			Collection<UserSecurityGroupID> userSecurityGroupIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObjects(null, userSecurityGroupIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public UserSecurityGroup getUserSecurityGroup(
			UserSecurityGroupID userSecurityGroupID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObject(null, userSecurityGroupID, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized List<UserSecurityGroup> getUserSecurityGroups(
			String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		monitor.beginTask("Get user security groups", 100);
		try {
			jfireSecurityManager = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);
			monitor.worked(10);
			Set<UserSecurityGroupID> userSecurityGroupIDs = jfireSecurityManager.getUserSecurityGroupIDs();
			monitor.worked(30);
			return getJDOObjects(null, userSecurityGroupIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 60));
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			jfireSecurityManager = null;
			monitor.done();
		}
	}

	public void setMembersOfUserSecurityGroup(UserSecurityGroupID userSecurityGroupID, Set<? extends AuthorizedObjectID> memberAuthorizedObjectIDs, ProgressMonitor monitor)
	{
		monitor.beginTask("Setting membership", 100);
		try {
			JFireSecurityManagerRemote m = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);
			m.setMembersOfUserSecurityGroup(userSecurityGroupID, memberAuthorizedObjectIDs);
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			jfireSecurityManager = null;
			monitor.worked(100);
			monitor.done();
		}
	}

	public void setUserSecurityGroupsOfMember(Set<UserSecurityGroupID> userSecurityGroupIDs, AuthorizedObjectID memberAuthorizedObjectID, ProgressMonitor monitor)
	{
		monitor.beginTask("Setting membership", 100);
		try {
			JFireSecurityManagerRemote m = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);
			m.setUserSecurityGroupsOfMember(userSecurityGroupIDs, memberAuthorizedObjectID);
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			jfireSecurityManager = null;
			monitor.worked(100);
			monitor.done();
		}
	}

	public UserSecurityGroup storeUserSecurityGroup(
			UserSecurityGroup userSecurityGroup,
			boolean get,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Storing user security group", 100);
		try {
			JFireSecurityManagerRemote m = getEjbProvider().getRemoteBean(JFireSecurityManagerRemote.class);
			UserSecurityGroup result = m.storeUserSecurityGroup(userSecurityGroup, get, fetchGroups, maxFetchDepth);

			if (result != null)
				getCache().put(null, result, fetchGroups, maxFetchDepth);

			return result;
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			jfireSecurityManager = null;
			monitor.worked(100);
			monitor.done();
		}
	}
}
