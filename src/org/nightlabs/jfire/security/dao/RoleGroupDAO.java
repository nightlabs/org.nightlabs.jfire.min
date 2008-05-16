/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 ******************************************************************************/
package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupIDSetCarrier;
import org.nightlabs.jfire.security.RoleGroupSetCarrier;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link RoleGroup}s.
 * 
 * @version $Revision: 4710 $ - $Date: 2006-10-11 01:49:29 +0200 (Mi, 11 Okt 2006) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RoleGroupDAO extends BaseJDOObjectDAO<RoleGroupID, RoleGroup>
{
	/**
	 * The shared instance
	 */
	private static RoleGroupDAO sharedInstance = null;

	/**
	 * Get the lazily created shared instance.
	 * @return The shared instance
	 */
	public static RoleGroupDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new RoleGroupDAO();
		return sharedInstance;
	}

	/**
	 * The temporary used UserManager.
	 */
	private UserManager userManager;

	@Override
	protected Collection<RoleGroup> retrieveJDOObjects(
			Set<RoleGroupID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	) throws Exception
	{
		UserManager um = userManager;
		if (um == null)
			um = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

		return um.getRoleGroups(objectIDs, fetchGroups, maxFetchDepth);
	}

	/**
	 * Get all role groups for the given user.
	 * @param userID The user
	 * @param authorityID The authority
	 * @param fetchgroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return A carrier object containing assigned, unassigned and assigned-by-usergroup
	 * 					role groups.
	 */
	public synchronized RoleGroupSetCarrier getUserRoleGroupSetCarrier(UserID userID, AuthorityID authorityID, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			userManager = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			RoleGroupIDSetCarrier ids = userManager.getRoleGroupIDSetCarrier(userID, authorityID);

			monitor.worked(1);
			RoleGroupSetCarrier x = new RoleGroupSetCarrier();
			x.assigned = getJDOObjects(null, ids.assignedToUser, fetchgroups, maxFetchDepth, monitor);
			System.err.println("HAVE "+x.assigned.size()+" ASSIGNED ROLE GROUPS");
			monitor.worked(1);

			x.assignedByUserGroup = getJDOObjects(null, ids.assignedToUserGroups, fetchgroups, maxFetchDepth, monitor);
			System.err.println("HAVE "+x.assignedByUserGroup.size()+" ASSIGNED ROLE GROUPS BY USER GROUP");
			monitor.worked(1);
			x.excluded = getJDOObjects(null, ids.excluded, fetchgroups, maxFetchDepth, monitor);
			System.err.println("HAVE "+x.excluded.size()+" EXCLUDED ROLE GROUPS");
			monitor.worked(1);
			return x;
		} catch(Exception e) {
			throw new RuntimeException("Role group download failed", e);
		} finally {
			userManager = null;
		}
	}

	/**
	 * Returns a collection of all role groups for the given user groups.
	 * 
	 * @param userGroupIDs A collection of the user group IDs whose {@link RoleGroup}s are to be returned.
	 * @param authorityID The authority
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor TODO
	 * @param fetchgroups The fetch groups to use
	 * @return a collection of all role groups for the given user groups.
	 */
	public synchronized Collection<RoleGroup> getRoleGroupsForUserGroups(Collection<UserID> userGroupIDs, AuthorityID authorityID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			userManager = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<RoleGroup> roleGroups = new HashSet<RoleGroup>();

			for (UserID userGroupID : userGroupIDs) {
				RoleGroupIDSetCarrier groupIDs = userManager.getRoleGroupIDSetCarrier(userGroupID, authorityID);
				roleGroups.addAll(getJDOObjects(null, groupIDs.assignedToUser, fetchGroups, maxFetchDepth, new NullProgressMonitor()));
			}
			return roleGroups;
		} catch(Exception e) {
			throw new RuntimeException("Role group download failed", e);
		} finally {
			userManager = null;
		}
	}

	public List<RoleGroup> getRoleGroups(Collection<RoleGroupID> roleGroupIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, roleGroupIDs, fetchGroups, maxFetchDepth, monitor);
	}

	@SuppressWarnings("unchecked")
	public Map<User, RoleGroupSetCarrier> getRoleGroupSetCarriers(
			AuthorityID authorityID,
			String[] fetchGroupsUser, int maxFetchDepthUser,
			String[] fetchGroupsRoleGroup, int maxFetchDepthRoleGroup,
			ProgressMonitor monitor)
	{
		monitor.beginTask("Loading users and role groups", 100);

		Map<RoleGroupID, RoleGroup> roleGroupID2roleGroup;
		Map<UserID, RoleGroupIDSetCarrier> userID2roleGroupIDSetCarrier;
		Map<User, RoleGroupSetCarrier> user2roleGroupSetCarrier;
		synchronized (this) {
			try {
				userManager = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
				userID2roleGroupIDSetCarrier = userManager.getRoleGroupIDSetCarriers(authorityID);

				Set<RoleGroupID> roleGroupIDs = new HashSet<RoleGroupID>();
				for (Map.Entry<UserID, RoleGroupIDSetCarrier> me : userID2roleGroupIDSetCarrier.entrySet()) {
					roleGroupIDs.addAll(me.getValue().assignedToUser);
					roleGroupIDs.addAll(me.getValue().assignedToUserGroups);
					roleGroupIDs.addAll(me.getValue().excluded);
				}

				roleGroupID2roleGroup = new HashMap<RoleGroupID, RoleGroup>(roleGroupIDs.size());

				monitor.worked(30);

				Collection<RoleGroup> roleGroups = getJDOObjects(
						null,
						roleGroupIDs,
						fetchGroupsRoleGroup,
						maxFetchDepthRoleGroup,
						new SubProgressMonitor(monitor, 30));

				for (RoleGroup roleGroup : roleGroups) {
					RoleGroupID roleGroupID = (RoleGroupID) JDOHelper.getObjectId(roleGroup);
					assert roleGroupID != null : "JDOHelper.getObjectId(roleGroup) != null";
					roleGroupID2roleGroup.put(roleGroupID, roleGroup);
				}

			} catch(Exception e) {
				throw new RuntimeException(e);
			} finally {
				userManager = null;
			}
		}

		Collection<User> users = UserDAO.sharedInstance().getUsers(
				userID2roleGroupIDSetCarrier.keySet(),
				fetchGroupsUser,
				maxFetchDepthUser,
				new SubProgressMonitor(monitor, 30));

		Map<UserID, User> userID2user = new HashMap<UserID, User>(userID2roleGroupIDSetCarrier.keySet().size());
		for (User user : users) {
			UserID userID = (UserID) JDOHelper.getObjectId(user);
			assert userID != null : "JDOHelper.getObjectId(user) != null";
			userID2user.put(userID, user);
		}

		user2roleGroupSetCarrier = new HashMap<User, RoleGroupSetCarrier>(userID2roleGroupIDSetCarrier.size());
		for (Map.Entry<UserID, RoleGroupIDSetCarrier> me : userID2roleGroupIDSetCarrier.entrySet()) {
			User user = userID2user.get(me.getKey());
			assert user != null : "userID2user.get(userID) != null :: userID=" + me.getKey();

			RoleGroupSetCarrier roleGroupSetCarrier = new RoleGroupSetCarrier();
			roleGroupSetCarrier.assigned = getRoleGroups(roleGroupID2roleGroup, me.getValue().assignedToUser);
			roleGroupSetCarrier.assignedByUserGroup = getRoleGroups(roleGroupID2roleGroup, me.getValue().assignedToUserGroups);
			roleGroupSetCarrier.excluded = getRoleGroups(roleGroupID2roleGroup, me.getValue().excluded);
			
			user2roleGroupSetCarrier.put(user, roleGroupSetCarrier);
		}

		monitor.worked(10);
		monitor.done();
		return user2roleGroupSetCarrier;
	}

	private static Set<RoleGroup> getRoleGroups(Map<RoleGroupID, RoleGroup> roleGroupID2roleGroup, Set<RoleGroupID> roleGroupIDs)
	{
		Set<RoleGroup> res = new HashSet<RoleGroup>(roleGroupIDs.size());
		for (RoleGroupID roleGroupID : roleGroupIDs) {
			RoleGroup roleGroup = roleGroupID2roleGroup.get(roleGroupID);
			assert roleGroup != null : "roleGroupID2roleGroup.get(roleGroupID) != null :: roleGroupID=" + roleGroupID;
			res.add(roleGroup);
		}
		return res;
	}
}
