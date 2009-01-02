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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.JFireSecurityManager;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupIDSetCarrier;
import org.nightlabs.jfire.security.RoleGroupSetCarrier;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
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
	 * The temporary used JFireSecurityManager.
	 */
	private JFireSecurityManager securityManager;

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<RoleGroup> retrieveJDOObjects(
			Set<RoleGroupID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	) throws Exception
	{
		JFireSecurityManager um = securityManager;
		if (um == null)
			um = JFireEjbUtil.getBean(JFireSecurityManager.class, SecurityReflector.getInitialContextProperties());

		return um.getRoleGroups(objectIDs, fetchGroups, maxFetchDepth);
	}

	/**
	 * Get all role groups for the given user.
	 * @param authorizedObjectID The user
	 * @param authorityID The authority
	 * @param fetchgroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return A carrier object containing assigned, unassigned and assigned-by-usergroup
	 * 					role groups.
	 */
	public RoleGroupSetCarrier getRoleGroupSetCarrier(
			AuthorizedObjectID authorizedObjectID,
			AuthorityID authorityID,
			String[] fetchGroupsAuthorizedObject, int maxFetchDepthAuthorizedObject,
			String[] fetchGroupsAuthority, int maxFetchDepthAuthority,
			String[] fetchGroupsRoleGroup, int maxFetchDepthRoleGroup,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading role groups", 100);
		try {
			RoleGroupSetCarrier roleGroupSetCarrier = new RoleGroupSetCarrier();
			RoleGroupIDSetCarrier roleGroupIDSetCarrier;

			synchronized (this) { // synchronize because of usage of the field securityManager
				try {
					securityManager = JFireEjbUtil.getBean(JFireSecurityManager.class, SecurityReflector.getInitialContextProperties());
					roleGroupIDSetCarrier = securityManager.getRoleGroupIDSetCarrier(authorizedObjectID, authorityID);
					monitor.worked(20);

					roleGroupSetCarrier.setAllInAuthority(
							new HashSet<RoleGroup>(
									getJDOObjects(
											null,
											roleGroupIDSetCarrier.getAllInAuthority(),
											fetchGroupsRoleGroup,
											maxFetchDepthRoleGroup,
											new SubProgressMonitor(monitor, 30)
									)
							)
					);

					roleGroupSetCarrier.setAssignedToUser(
							new HashSet<RoleGroup>(
									getJDOObjects(
											null,
											roleGroupIDSetCarrier.getAssignedToUser(),
											fetchGroupsRoleGroup,
											maxFetchDepthRoleGroup,
											new SubProgressMonitor(monitor, 10)
									)
							)
					);

					roleGroupSetCarrier.setAssignedToUserGroups(
							new HashSet<RoleGroup>(
									getJDOObjects(
											null,
											roleGroupIDSetCarrier.getAssignedToUserGroups(),
											fetchGroupsRoleGroup,
											maxFetchDepthRoleGroup,
											new SubProgressMonitor(monitor, 10)
									)
							)
					);

					roleGroupSetCarrier.setAssignedToOtherUser(
							new HashSet<RoleGroup>(
									getJDOObjects(
											null,
											roleGroupIDSetCarrier.getAssignedToOtherUser(),
											fetchGroupsRoleGroup,
											maxFetchDepthRoleGroup,
											new SubProgressMonitor(monitor, 10)
									)
							)
					);

				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					securityManager = null;
				}
			}

			roleGroupSetCarrier.setInAuthority(roleGroupIDSetCarrier.isInAuthority());
			roleGroupSetCarrier.setControlledByOtherUser(roleGroupIDSetCarrier.isControlledByOtherUser());

			roleGroupSetCarrier.setAuthority(
					AuthorityDAO.sharedInstance().getAuthority(
							roleGroupIDSetCarrier.getAuthorityID(),
							fetchGroupsAuthority,
							maxFetchDepthAuthority,
							new SubProgressMonitor(monitor, 10)
					)
			);

			roleGroupSetCarrier.setAuthorizedObject(
					AuthorizedObjectDAO.sharedInstance().getAuthorizedObject(
							roleGroupIDSetCarrier.getAuthorizedObjectID(),
							fetchGroupsAuthorizedObject,
							maxFetchDepthAuthorizedObject,
							new SubProgressMonitor(monitor, 10)
					)
			);

			return roleGroupSetCarrier;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns a collection of all role groups for the given user security groups.
	 * 
	 * @param userSecurityGroupIDs A collection of the user group IDs whose {@link RoleGroup}s are to be returned.
	 * @param authorityID The authority
	 * @param fetchgroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor the monitor to give progress feedback
	 * @return a collection of all role groups for the given user groups.
	 */
	@SuppressWarnings("unchecked")
	public synchronized Set<RoleGroup> getRoleGroupsForUserSecurityGroups(
			Collection<UserSecurityGroupID> userSecurityGroupIDs,
			AuthorityID authorityID,
			String[] fetchGroups,
			int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		try {
			int totalTicks = 1000;
			int ticksLeft = totalTicks;
			monitor.beginTask("Get role groups", totalTicks);
			securityManager = JFireEjbUtil.getBean(JFireSecurityManager.class, SecurityReflector.getInitialContextProperties());
			Set<RoleGroup> roleGroups = new HashSet<RoleGroup>();

			Collection<RoleGroupIDSetCarrier> roleGroupIDSetCarriers = securityManager.getRoleGroupIDSetCarriers(userSecurityGroupIDs, authorityID);
			monitor.worked(500);
			ticksLeft -= 500;

			int ticksPerUserGroup = userSecurityGroupIDs.isEmpty() ? ticksLeft : ticksLeft / userSecurityGroupIDs.size();
			for (RoleGroupIDSetCarrier roleGroupIDSetCarrier : roleGroupIDSetCarriers) {
				roleGroups.addAll(getJDOObjects(
						null,
						roleGroupIDSetCarrier.getAssignedToUser(),
						fetchGroups,
						maxFetchDepth,
						new SubProgressMonitor(monitor, ticksPerUserGroup)));
				ticksLeft -= ticksPerUserGroup;
			}

			if (ticksLeft > 0)
				monitor.worked(ticksLeft);

			monitor.done();
			return roleGroups;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			securityManager = null;
		}
	}

	public List<RoleGroup> getRoleGroups(Collection<RoleGroupID> roleGroupIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, roleGroupIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public List<RoleGroupSetCarrier> getRoleGroupSetCarriers(
			AuthorityID authorityID,
			String[] fetchGroupsAuthorizedObject, int maxFetchDepthAuthorizedObject,
			String[] fetchGroupsAuthority, int maxFetchDepthAuthority,
			String[] fetchGroupsRoleGroup, int maxFetchDepthRoleGroup,
			ProgressMonitor monitor)
	{
		return getRoleGroupSetCarriers(
				null,
				authorityID,
				fetchGroupsAuthorizedObject,
				maxFetchDepthAuthorizedObject,
				fetchGroupsAuthority,
				maxFetchDepthAuthority,
				fetchGroupsRoleGroup,
				maxFetchDepthRoleGroup,
				monitor
		);
	}

	@SuppressWarnings("unchecked")
	public List<RoleGroupSetCarrier> getRoleGroupSetCarriers(
			Set<AuthorizedObjectID> _authorizedObjectIDs,
			AuthorityID authorityID,
			String[] fetchGroupsAuthorizedObject, int maxFetchDepthAuthorizedObject,
			String[] fetchGroupsAuthority, int maxFetchDepthAuthority,
			String[] fetchGroupsRoleGroup, int maxFetchDepthRoleGroup,
			ProgressMonitor monitor)
	{
		monitor.beginTask("Loading users and role groups", 100);
		try {

			Map<RoleGroupID, RoleGroup> roleGroupID2roleGroup;
			List<RoleGroupIDSetCarrier> roleGroupIDSetCarriers;
			List<RoleGroupSetCarrier> roleGroupSetCarriers;
			synchronized (this) {
				try {
					securityManager = JFireEjbUtil.getBean(JFireSecurityManager.class, SecurityReflector.getInitialContextProperties());
					if (_authorizedObjectIDs != null)
						roleGroupIDSetCarriers = securityManager.getRoleGroupIDSetCarriers(_authorizedObjectIDs, authorityID);
					else
						roleGroupIDSetCarriers = securityManager.getRoleGroupIDSetCarriers(authorityID);

					if (roleGroupIDSetCarriers.isEmpty()) {
						monitor.worked(100);
						return new ArrayList<RoleGroupSetCarrier>(0);
					}

					Set<RoleGroupID> roleGroupIDs = roleGroupIDSetCarriers.iterator().next().getAllInAuthority();
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
					securityManager = null;
				}
			}

			Set<AuthorizedObjectID> authorizedObjectIDs = new HashSet<AuthorizedObjectID>(roleGroupIDSetCarriers.size());
			for (RoleGroupIDSetCarrier roleGroupIDSetCarrier : roleGroupIDSetCarriers)
				authorizedObjectIDs.add(roleGroupIDSetCarrier.getAuthorizedObjectID());

			Collection<AuthorizedObject> authorizedObjects = AuthorizedObjectDAO.sharedInstance().getAuthorizedObjects(
					authorizedObjectIDs,
					fetchGroupsAuthorizedObject,
					maxFetchDepthAuthorizedObject,
					new SubProgressMonitor(monitor, 30));

			Map<AuthorizedObjectID, AuthorizedObject> authorizedObjectID2authorizedObject = new HashMap<AuthorizedObjectID, AuthorizedObject>(authorizedObjectIDs.size());
			for (AuthorizedObject authorizedObject : authorizedObjects) {
				AuthorizedObjectID authorizedObjectID = (AuthorizedObjectID) JDOHelper.getObjectId(authorizedObject);
				assert authorizedObjectID != null : "JDOHelper.getObjectId(authorizedObject) != null";
				authorizedObjectID2authorizedObject.put(authorizedObjectID, authorizedObject);
			}

			Authority authority = AuthorityDAO.sharedInstance().getAuthority(
					authorityID,
					fetchGroupsAuthority,
					maxFetchDepthAuthority,
					new SubProgressMonitor(monitor, 10)
			);

			roleGroupSetCarriers = new ArrayList<RoleGroupSetCarrier>(roleGroupIDSetCarriers.size()); 
			for (RoleGroupIDSetCarrier roleGroupIDSetCarrier : roleGroupIDSetCarriers) {
				if (!roleGroupIDSetCarrier.getAuthorityID().equals(authorityID))
					throw new IllegalStateException("roleGroupIDSetCarrier.authorityID != authorityID");

				AuthorizedObject authorizedObject = authorizedObjectID2authorizedObject.get(roleGroupIDSetCarrier.getAuthorizedObjectID());
				assert authorizedObject != null : "authorizedObjectID2authorizedObject.get(authorizedObjectID) != null :: authorizedObjectID=" + roleGroupIDSetCarrier.getAuthorizedObjectID();

				RoleGroupSetCarrier roleGroupSetCarrier = new RoleGroupSetCarrier();
				roleGroupSetCarrier.setAuthorizedObject(authorizedObject);
				roleGroupSetCarrier.setAuthority(authority);

				roleGroupSetCarrier.setAllInAuthority(new HashSet<RoleGroup>(roleGroupID2roleGroup.values()));

				roleGroupSetCarrier.setAssignedToUser(
						getRoleGroups(roleGroupID2roleGroup, roleGroupIDSetCarrier.getAssignedToUser())
				);

				roleGroupSetCarrier.setAssignedToUserGroups(
						getRoleGroups(roleGroupID2roleGroup, roleGroupIDSetCarrier.getAssignedToUserGroups())
				);

				roleGroupSetCarrier.setAssignedToOtherUser(
						getRoleGroups(roleGroupID2roleGroup, roleGroupIDSetCarrier.getAssignedToOtherUser())
				);

				roleGroupSetCarrier.setInAuthority(roleGroupIDSetCarrier.isInAuthority());
				roleGroupSetCarrier.setControlledByOtherUser(roleGroupIDSetCarrier.isControlledByOtherUser());

				roleGroupSetCarriers.add(roleGroupSetCarrier);
			}

			monitor.worked(10);
			return roleGroupSetCarriers;
		} finally {
			monitor.done();
		}
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
