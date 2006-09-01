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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.person.PersonStructProvider;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonManager;
import org.nightlabs.jfire.person.PersonManagerUtil;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
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
	 * The temporary used UserManager.
	 */
  private UserManager um;

	/**
	 * The temporary used UserManager.
	 */
  private PersonManager pm;
  
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
	protected Collection<User> retrieveJDOObjects(Collection<UserID> objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) throws Exception
	{
		Assert.isNotNull(um);
		Collection<User> users = um.getUsers(objectIDs.toArray(), fetchGroups, maxFetchDepth);
		monitor.worked(objectIDs.size());
		return users;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObject(java.lang.Object, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected User retrieveJDOObject(UserID objectID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) throws Exception
	{
		Assert.isNotNull(um);
		User user = um.getUser(objectID, fetchGroups, maxFetchDepth);
		monitor.worked(1);
		return user;
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
  	try {
  		monitor.setTaskName("Loading user "+userID.userID);
  		um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
  		User user = getJDOObject(null, userID, fetchGroups, maxFetchDepth, monitor);
  		monitor.worked(1);
  		return user;
  	} catch(Exception e) {
  		throw new RuntimeException("User download failed", e);
  	} finally {
  		um = null;
  	}
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
		try {
			Hashtable initialContextProperties = Login.getLogin().getInitialContextProperties();
			um = UserManagerUtil.getHome(initialContextProperties).create();
			pm = PersonManagerUtil.getHome(initialContextProperties).create();
    	monitor.worked(1);
			Person person = user.getPerson();
			PersonStructProvider.getPersonStructure().implodePerson(person);
			long activePersonID = person.getPersonID();
			if (activePersonID == Person.TEMPORARY_PERSON_ID) {
				person.assignPersonID(pm.createPersonID());
				// FIXME: how to do this?
//	    	person.setAutoGenerateDisplayName(true);
//	    	person.setPersonDisplayName(null, person.)
	    	monitor.worked(1);
				pm.storePerson(person, false, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				um.assignPersonToUser(user.getUserID(), person.getPersonID());
	    	monitor.worked(1);
			}
			else {
				// FIXME: how to do this?
//	    	person.setAutoGenerateDisplayName(true);
				pm.storePerson(person, false, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
	    	monitor.worked(2);
			}
			// FIXME: how to do this?
			// set person to call User.setNameAuto()
//    	user.setPerson(person);
    	um.saveUser(user, null);
    	monitor.worked(1);
			PersonStructProvider.getPersonStructure().explodePerson(person);    
	  	monitor.worked(1);
  	} catch(Exception e) {
  		throw new RuntimeException("User upload failed", e);
  	} finally {
  		um = null;
  		pm = null;
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
  public synchronized Collection<User> getUsersByType(String type, String[] fetchgroups, int maxFetchDepth, IProgressMonitor monitor) 
	{
  	try {
		  um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		  Collection<UserID> ids = um.getUserIDsByType(type);
		  return getJDOObjects(null, ids, fetchgroups, maxFetchDepth, monitor);
  	} catch(Exception e) {
  		throw new RuntimeException("User download failed", e);
  	} finally {
  		um = null;
  	}
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
		try {
			Hashtable initialContextProperties = Login.getLogin().getInitialContextProperties();
			um = UserManagerUtil.getHome(initialContextProperties).create();
			um.addUserToUserGroup(user.getUserID(), userGroup.getUserID());
  	} catch(Exception e) {
  		throw new RuntimeException("Adding user to user group failed", e);
  	} finally {
  		um = null;
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
		try {
			Hashtable initialContextProperties = Login.getLogin().getInitialContextProperties();
			um = UserManagerUtil.getHome(initialContextProperties).create();
			um.removeUserFromUserGroup(user.getUserID(), userGroup.getUserID());
  	} catch(Exception e) {
  		throw new RuntimeException("Adding user to user group failed", e);
  	} finally {
  		um = null;
  	}
  }
}
