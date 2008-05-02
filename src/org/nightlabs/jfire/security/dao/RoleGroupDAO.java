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
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupIDListCarrier;
import org.nightlabs.jfire.security.RoleGroupListCarrier;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

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
  private UserManager um;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Collection, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Collection<RoleGroup> retrieveJDOObjects(Set<RoleGroupID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
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
  public synchronized RoleGroupListCarrier getUserRoleGroups(UserID userID, String authorityID, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
  {
  	try {
		  um = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		  RoleGroupIDListCarrier ids = um.getRoleGroupIDs(userID.userID, authorityID);
		  
		  monitor.worked(1);
		  RoleGroupListCarrier x = new RoleGroupListCarrier();
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
  		um = null;
  	}
  }
  
  /**
   * Returns a collection of all role groups for the given user groups.
   * 
   * @param userGroupIDs A collection of the user group IDs whose {@link RoleGroup}s are to be returned.
   * @param authorityID The authority
	 * @param fetchgroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
   * @return a collection of all role groups for the given user groups.
   */
  public synchronized Collection<RoleGroup> getRoleGroupsForUserGroups(Collection<UserID> userGroupIDs, String authorityID, String[] fetchGroups, int maxFetchDepth) {
  	try {
		  um = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		  Collection<RoleGroup> roleGroups = new HashSet<RoleGroup>();
		  
		  for (UserID userGroupID : userGroupIDs) {
		  	RoleGroupIDListCarrier groupIDs = um.getRoleGroupIDs(userGroupID.userID, authorityID);
		  	roleGroups.addAll(getJDOObjects(null, groupIDs.assignedToUser, fetchGroups, maxFetchDepth, new NullProgressMonitor()));
		  }
		  return roleGroups;
  	} catch(Exception e) {
  		throw new RuntimeException("Role group download failed", e);
  	} finally {
  		um = null;
  	}
  }
}
