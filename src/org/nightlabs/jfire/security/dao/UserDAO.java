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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 ******************************************************************************/
package org.nightlabs.jfire.security.dao;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.JFireSecurityManager;
import org.nightlabs.jfire.security.JFireSecurityManagerUtil;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * Get user JDO objects using the JFire client cache.
 * 
 * @version $Revision: 6500 $ - $Date: 2007-05-12 19:18:24 +0200 (Sa, 12 Mai 2007) $
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class UserDAO extends BaseJDOObjectDAO<UserID, User>
{
	/**
	 * The shared instance
	 */
	private static UserDAO sharedInstance = null;

	/**
	 * Get the lazily created shared instance.
	 * @return The shared instance
	 */
	public static UserDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new UserDAO();
		return sharedInstance;
	}

	/**
	 * Default constructor.
	 */
	public UserDAO()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Collection, java.util.Set, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	@Implement
	protected Collection<User> retrieveJDOObjects(Set<UserID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Fetching "+objectIDs.size()+" user information", 1);
		Collection<User> users;
		try {
			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			users = um.getUsers(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Failed downloading User information!", e);
		}
		
		monitor.done();
		return users;
	}

	/**
	 * Get a single user.
	 * @param userID The ID of the user to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested user object
	 */
	public synchronized User getUser(UserID userID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading user "+userID.userID, 1);
		User user = getJDOObject(null, userID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return user;
	}

	/**
	 * Store a user and its person on the server.
	 * 
	 * @param user the user to save.
	 * @param newPassword the password for the user. This might be <code>null</code>. If a new user is created without password,
	 *		it cannot login, since the presence of a password is forced by the login-module.
	 *		Note, that this parameter is ignored, if the given <code>user</code> has a {@link UserLocal} assigned.
	 *		In this case, the property {@link UserLocal#getNewPassword()} is used instead. In other words, this field
	 *		is meant to be used to create a new <code>User</code> with an initial password.
	 * @param get Whether to return the newly saved user.
	 * @param fetchGroups The fetch-groups to detach the returned User with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 * @param monitor The progress monitor to use. This method will
	 * 		make a progress of 5 work units per user.
	 */
	public synchronized User storeUser(User user, String newPassword, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if(user == null)
			throw new NullPointerException("User to save must not be null");

		user = Util.cloneSerializable(user); // we clone the user since its properties are changed below (e.g. the person is deflated)

		monitor.beginTask("Storing user: "+user.getName(), 3); // 4);
		try {
			Properties initialContextProperties = SecurityReflector.getInitialContextProperties();
			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(initialContextProperties).create();
			monitor.worked(1);
			
			Person person = user.getPerson();
			if (person != null) {
				user.getPerson().deflate();
				long activePersonID = person.getPropertySetID();
				if (activePersonID == PropertySet.TEMPORARY_PROP_ID) {
					person.assignID(IDGenerator.nextID(PropertySet.class));
				}
			}
			User result = um.storeUser(user, newPassword, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
			return result;
		} catch(Exception e) {
			monitor.done();
			throw new RuntimeException("User upload failed", e);
		}
	}

	public synchronized List<User> getUsers(String organisationID, String[] userTypes, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getUsers(organisationID, CollectionUtil.array2HashSet(userTypes), fetchgroups, maxFetchDepth, monitor);
	}

	/**
	 * Get users by type.
	 * @param type One of User.USERTYPE*
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The users of the given type.
	 */
	public synchronized List<User> getUsers(String organisationID, Set<String> userTypes, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<UserID> ids = um.getUserIDs(organisationID, userTypes);
			return getJDOObjects(null, ids, fetchgroups, maxFetchDepth, monitor);
		} catch(Exception e) {
			throw new RuntimeException("User download failed", e);
		}
	}

	public synchronized List<User> getUsers(Set<UserID> userIDs, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, userIDs, fetchgroups, maxFetchDepth, monitor);
	}

//	/**
//	 * Get all users.
//	 * @param fetchGroups Wich fetch groups to use
//	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
//	 * @param monitor The progress monitor for this action. For every downloaded
//	 * 					object, <code>monitor.worked(1)</code> will be called.
//	 * @return The users.
//	 */
//	public synchronized Collection<User> getUsers(String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
//	{
//		return getUsers(null, null, fetchgroups, maxFetchDepth, monitor);
//	}
//
//	/**
//	 * Get a single user group.
//	 * @param userGroupID The ID of the user group to get
//	 * @param fetchGroups Wich fetch groups to use
//	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
//	 * @param monitor The progress monitor for this action. For every downloaded
//	 * 					object, <code>monitor.worked(1)</code> will be called.
//	 * @return The requested user group object
//	 */
//	public synchronized UserGroup getUserGroup(UserID userGroupID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
//	{
//		return (UserGroup)getUser(userGroupID, fetchGroups, maxFetchDepth, monitor);
//	}

//	/**
//	 * Add a user to a user group
//	 * @param user The user
//	 * @param userGroup The user group in wich to add the user
//	 */
//	public void addUserToUserGroup(User user, UserGroup userGroup, ProgressMonitor monitor)
//	{
//		assert user != null : "User to add to user group must not be null";
//		assert userGroup != null : "User group to add user for must not be null";
//		addUserToUserGroup((UserID)JDOHelper.getObjectId(user), (UserID)JDOHelper.getObjectId(userGroup), monitor);
//	}
//
//	public synchronized void addUserToUserGroup(UserID userID, UserID userGroupID, ProgressMonitor monitor)
//	{
//		monitor.beginTask("Adding user: "+userID.userID+" to group: "+userGroupID.userID, 1);
//		try {
//			Properties initialContextProperties = SecurityReflector.getInitialContextProperties();
//			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(initialContextProperties).create();
//			um.addUserToUserGroup(userID, userGroupID);
//			monitor.worked(1);
//			monitor.done();
//		} catch(Exception e) {
//			monitor.done();
//			throw new RuntimeException("Adding user to user group failed", e);
//		}
//	}
//
//	/**
//	 * Remove a user from a user group
//	 * @param user The user
//	 * @param userGroup The user group from which to remove the user
//	 */
//	public void removeUserFromUserGroup(User user, UserGroup userGroup, ProgressMonitor monitor)
//	{
//		assert user != null : "User to remove from user group must not be null";
//		assert userGroup != null : "User group to remove user from must not be null";
//		removeUserFromUserGroup((UserID)JDOHelper.getObjectId(user), (UserID)JDOHelper.getObjectId(userGroup), monitor);
//	}
//	public synchronized void removeUserFromUserGroup(UserID userID, UserID userGroupID, ProgressMonitor monitor)
//	{
//		monitor.beginTask("Removing user: "+userID.userID+" from group: "+userGroupID.userID, 1);
//		try {
//			Properties initialContextProperties = SecurityReflector.getInitialContextProperties();
//			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(initialContextProperties).create();
//			um.removeUserFromUserGroup(userID, userGroupID);
//			monitor.worked(1);
//			monitor.done();
//		} catch(Exception e) {
//			monitor.done();
//			throw new RuntimeException("Adding user to user group failed", e);
//		}
//	}

//	public void addRoleGroupToUser(User user, Authority authority, RoleGroup roleGroup, ProgressMonitor monitor)
//	{
//		addRoleGroupToUser(
//				(UserID)JDOHelper.getObjectId(user),
//				(AuthorityID)JDOHelper.getObjectId(authority),
//				(RoleGroupID)JDOHelper.getObjectId(roleGroup), monitor);
//	}
//
//	public void removeRoleGroupFromUser(User user, Authority authority, RoleGroup roleGroup, ProgressMonitor monitor)
//	{
//		removeRoleGroupFromUser(
//				(UserID)JDOHelper.getObjectId(user),
//				(AuthorityID)JDOHelper.getObjectId(authority),
//				(RoleGroupID)JDOHelper.getObjectId(roleGroup), monitor);
//	}

//	public synchronized void addRoleGroupToUser(UserID userID, AuthorityID authorityID, RoleGroupID roleGroupID, ProgressMonitor monitor)
//	{
//		monitor.beginTask("Adding a user to a rolegroup", 1);
//		try {
//			Properties initialContextProperties = SecurityReflector.getInitialContextProperties();
//			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(initialContextProperties).create();
//			um.addRoleGroupToUser(userID, authorityID, roleGroupID);
//		} catch(RuntimeException e) {
//			throw e;
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			monitor.worked(1);
//			monitor.done();
//		}
//	}
//
//	public synchronized void removeRoleGroupFromUser(UserID userID, AuthorityID authorityID, RoleGroupID roleGroupID, ProgressMonitor monitor)
//	{
//		monitor.beginTask("Removing a user from a rolegroup", 1);
//		try {
//			String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
//			if (!organisationID.equals(userID.organisationID))
//				throw new IllegalArgumentException("Cannot manage foreign user! user.organisationID=\""+userID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
//			if (!organisationID.equals(authorityID.organisationID))
//				throw new IllegalArgumentException("Cannot manage foreign authority! authority.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
//
//			Properties initialContextProperties = SecurityReflector.getInitialContextProperties();
//			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(initialContextProperties).create();
//			um.removeRoleGroupFromUser(userID, authorityID, roleGroupID);
//		} catch(RuntimeException e) {
//			throw e;
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			monitor.worked(1);
//			monitor.done();
//		}
//	}

//	public synchronized Collection<User> getUsersInUserGroup(UserID userGroupID, 
//			String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
//	{
//		monitor.beginTask("Get users in user group", 1);
//		try {
//			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
//			Collection<UserID> ids = um.getUserIDsInUserGroup(userGroupID);
//			return getJDOObjects(null, ids, fetchgroups, maxFetchDepth, monitor);			
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			monitor.worked(1);
//			monitor.done();
//		}
//	}

//	public synchronized Collection<User> getUsersNotInUserGroup(UserID userGroupID, 
//			String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
//	{
//		monitor.beginTask("Get users not in user group", 1);
//		try {
//			JFireSecurityManager um = JFireSecurityManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
//			Collection<UserID> ids = um.getUserIDsNotInUserGroup(userGroupID);
//			return getJDOObjects(null, ids, fetchgroups, maxFetchDepth, monitor);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			monitor.worked(1);
//			monitor.done();
//		}
//	}

//	public void addUsersToAuthority(Set<UserID> userIDs, AuthorityID authorityID, ProgressMonitor monitor)
//	{
//		monitor.beginTask("Add users to authority", 1);
//		try {
//			JFireSecurityManager sm = JFireSecurityManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
//			sm.addUsersToAuthority(userIDs, authorityID);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			monitor.worked(1);
//			monitor.done();
//		}
//	}
//
//	public void removeUsersFromAuthority(Set<UserID> userIDs, AuthorityID authorityID, ProgressMonitor monitor)
//	{
//		monitor.beginTask("Remove users from authority", 1);
//		try {
//			JFireSecurityManager sm = JFireSecurityManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
//			sm.removeUsersFromAuthority(userIDs, authorityID);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			monitor.worked(1);
//			monitor.done();
//		}
//	}
}
