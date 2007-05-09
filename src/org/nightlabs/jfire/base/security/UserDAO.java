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
package org.nightlabs.jfire.base.security;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.prop.StructLocalDAO;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Property;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.util.CollectionUtil;

/**
 * Get user JDO objects using the JFire client cache.
 * 
 * @version $Revision$ - $Date$
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class UserDAO extends JDOObjectDAO<UserID, User>
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
	@Implement
	protected Collection<User> retrieveJDOObjects(Set<UserID> objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Fetching "+objectIDs.size()+" user information", 1);
		Collection<User> users;
		try {
			UserManager um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			users = um.getUsers(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
		} catch (Exception e) {
			monitor.setCanceled(true);
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
	public synchronized User getUser(UserID userID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		monitor.beginTask("Loading user "+userID.userID, 1);
		User user = getJDOObject(null, userID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor,1));
		monitor.worked(1);
		monitor.done();
		return user;
	}

	/**
	 * Store a user and its person on the server.
	 * @param user The user to save
	 * @param monitor The progress monitor to use. This method will
	 * 		make a progress of 5 work units per user.
	 */
	public synchronized void storeUser(User user, IProgressMonitor monitor)
	{
		if(user == null)
			throw new NullPointerException("User to save must not be null");
		
		monitor.beginTask("Storing user: "+user.getName(), 4);
		try {
			Hashtable initialContextProperties = Login.getLogin().getInitialContextProperties();
			UserManager um = UserManagerUtil.getHome(initialContextProperties).create();
			PropertyManager pm = PropertyManagerUtil.getHome(initialContextProperties).create();
			monitor.worked(1);
			Person person = user.getPerson();
			IStruct struct = StructLocalDAO.sharedInstance().getStructLocal(Person.class.getName());
			if (user.getPerson() != null)
				struct.implodeProperty(user.getPerson());
			long activePersonID = person.getPropertyID();
			if (activePersonID == Property.TEMPORARY_PROP_ID) {
				person.assignID(IDGenerator.nextID(Property.class));
				// FIXME: how to do this?
//				person.setAutoGenerateDisplayName(true);
//				person.setPersonDisplayName(null, person.)
				monitor.worked(1);
				pm.storeProperty(person, false, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				um.assignPersonToUser(user.getUserID(), person.getPropertyID());
				monitor.worked(1);
			}
			else {
				// FIXME: how to do this?
//				person.setAutoGenerateDisplayName(true);
//				pm.storeProperty(person, false, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//				monitor.worked(2);
			}
			// FIXME: how to do this?
			// set person to call User.setNameAuto()
//			user.setPerson(person);
			um.saveUser(user, null);
			monitor.worked(1);
			StructLocalDAO.sharedInstance().getStructLocal(Person.class.getName()).explodeProperty(person);    
			monitor.worked(1);
			monitor.done();
		} catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("User upload failed", e);
		}
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
	public synchronized List<User> getUsersByType(String type, String[] fetchgroups, int maxFetchDepth, IProgressMonitor monitor) 
	{
		try {
			UserManager um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			Collection<UserID> ids = um.getUserIDsByType(type);
			return getJDOObjects(null, ids, fetchgroups, maxFetchDepth, monitor);
		} catch(Exception e) {
			throw new RuntimeException("User download failed", e);
		}
	}

	public synchronized List<User> getUsers(Set<UserID> userIDs, String[] fetchgroups, int maxFetchDepth, IProgressMonitor monitor) 
	{
		return getJDOObjects(null, userIDs, fetchgroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all users.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The users.
	 */
	public synchronized Collection<User> getUsers(String[] fetchgroups, int maxFetchDepth, IProgressMonitor monitor) 
	{
		return getUsersByType(User.USERTYPE_USER, fetchgroups, maxFetchDepth, monitor);
	}

	/**
	 * Get a single user group.
	 * @param userGroupID The ID of the user group to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested user group object
	 */
	public synchronized UserGroup getUserGroup(UserID userGroupID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		return (UserGroup)getUser(userGroupID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Store a user group and its person on the server.
	 * @param userGroup The user group to save
	 * @param monitor The progress monitor to use. This method will
	 * 		make a progress of 5 work units per user group.
	 */
	public synchronized void storeUserGroup(UserGroup userGroup, IProgressMonitor monitor)
	{
		storeUser(userGroup, monitor);
	}

	/**
	 * Get all user groups.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The user groups.
	 */
	public synchronized Collection<UserGroup> getUserGroups(String[] fetchgroups, int maxFetchDepth, IProgressMonitor monitor) 
	{
		return CollectionUtil.castCollection(getUsersByType(User.USERTYPE_USERGROUP, fetchgroups, maxFetchDepth, monitor));
	}

	/**
	 * Add a user to a user group
	 * @param user The user
	 * @param userGroup The user group in wich to add the user
	 */
	public synchronized void addUserToUserGroup(User user, UserGroup userGroup, IProgressMonitor monitor)
	{
		Assert.isNotNull(user, "User to add to user group must not be null");
		Assert.isNotNull(userGroup, "User group to add user for must not be null");
		monitor.beginTask("Adding user: "+user.getName()+" to group: "+userGroup.getName(), 1);
		try {
			Hashtable initialContextProperties = Login.getLogin().getInitialContextProperties();
			UserManager um = UserManagerUtil.getHome(initialContextProperties).create();
			um.addUserToUserGroup(user.getUserID(), userGroup.getUserID());
			monitor.done();
		} catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("Adding user to user group failed", e);
		}
	}

	/**
	 * Remove a user from a user group
	 * @param user The user
	 * @param userGroup The user group from wich to remove the user
	 */
	public synchronized void removeUserFromUserGroup(User user, UserGroup userGroup, IProgressMonitor monitor)
	{
		Assert.isNotNull(user, "User to remove from user group must not be null");
		Assert.isNotNull(userGroup, "User group to remove user from must not be null");
		monitor.beginTask("Removing user: "+user.getName()+" from group: "+userGroup.getName(), 1);
		try {
			Hashtable initialContextProperties = Login.getLogin().getInitialContextProperties();
			UserManager um = UserManagerUtil.getHome(initialContextProperties).create();
			um.removeUserFromUserGroup(user.getUserID(), userGroup.getUserID());
			monitor.done();
		} catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("Adding user to user group failed", e);
		}
	}

	public synchronized void addUserToRoleGroup(User user, Authority authority, RoleGroup roleGroup, IProgressMonitor monitor)
	{
		addUserToRoleGroup(
				(UserID)JDOHelper.getObjectId(user),
				(AuthorityID)JDOHelper.getObjectId(authority),
				(RoleGroupID)JDOHelper.getObjectId(roleGroup), monitor);
	}

	public synchronized void removeUserFromRoleGroup(User user, Authority authority, RoleGroup roleGroup, IProgressMonitor monitor)
	{
		removeUserFromRoleGroup(
				(UserID)JDOHelper.getObjectId(user),
				(AuthorityID)JDOHelper.getObjectId(authority),
				(RoleGroupID)JDOHelper.getObjectId(roleGroup), monitor);
	}

	public synchronized void addUserToRoleGroup(UserID userID, AuthorityID authorityID, RoleGroupID roleGroupID, IProgressMonitor monitor)
	{
		monitor.beginTask("Adding a user to a rolegroup", 1);
		try {
			String organisationID = Login.getLogin().getOrganisationID();
			if (!organisationID.equals(userID.organisationID))
				throw new IllegalArgumentException("Cannot manage foreign user! user.organisationID=\""+userID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
			if (!organisationID.equals(authorityID.organisationID))
				throw new IllegalArgumentException("Cannot manage foreign authority! authority.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

			Hashtable initialContextProperties = Login.getLogin().getInitialContextProperties();
			UserManager um = UserManagerUtil.getHome(initialContextProperties).create();
			um.addUserToRoleGroup(userID.userID, authorityID.authorityID, roleGroupID.roleGroupID);
			monitor.done();
		} catch(RuntimeException e) {
			monitor.setCanceled(true);
			throw e;
		} catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("Adding user \""+userID.userID+"\" to role group \""+roleGroupID.roleGroupID+"\" within authority \""+authorityID.authorityID+"\" failed", e);
		}
	}

	public synchronized void removeUserFromRoleGroup(UserID userID, AuthorityID authorityID, RoleGroupID roleGroupID, IProgressMonitor monitor)
	{
		monitor.beginTask("Removing a user from a rolegroup", 1);
		try {
			String organisationID = Login.getLogin().getOrganisationID();
			if (!organisationID.equals(userID.organisationID))
				throw new IllegalArgumentException("Cannot manage foreign user! user.organisationID=\""+userID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
			if (!organisationID.equals(authorityID.organisationID))
				throw new IllegalArgumentException("Cannot manage foreign authority! authority.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

			Hashtable initialContextProperties = Login.getLogin().getInitialContextProperties();
			UserManager um = UserManagerUtil.getHome(initialContextProperties).create();
			um.removeUserFromRoleGroup(userID.userID, authorityID.authorityID, roleGroupID.roleGroupID);
			monitor.done();
		} catch(RuntimeException e) {
			monitor.setCanceled(true);			
			throw e;
		} catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("Adding user \""+userID.userID+"\" to role group \""+roleGroupID.roleGroupID+"\" within authority \""+authorityID.authorityID+"\" failed", e);
		}
	}
}
