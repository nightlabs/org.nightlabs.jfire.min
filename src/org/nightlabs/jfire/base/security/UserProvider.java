/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
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
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.security;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.security.id.UserID;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class UserProvider extends JDOObjectProvider
{
  private static UserProvider _sharedInstance = null;
  private UserManager um;
  
  public static UserProvider sharedInstance() 
    throws RemoteException, LoginException, CreateException, NamingException
  {
    if (_sharedInstance == null) 
      _sharedInstance = new UserProvider();

    return _sharedInstance;
  }

  public User getUser(UserID id, String[] fetchGroups, int maxFetchDepth)
    throws RemoteException, LoginException, CreateException, NamingException
  {
    //TODO: is there a way to check if a session bean is still valid? (e.g. after server crash)
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    return (User)super.getJDOObject(null, id, fetchGroups, maxFetchDepth);
  }
  
  public Collection getUsersByType(String type, String[] fetchgroups, int maxFetchDepth) 
    throws RemoteException, ModuleException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    Collection ids = um.getUserIDsByType(type);
    return super.getJDOObjects(null, ids.toArray(), fetchgroups, maxFetchDepth);
  }

  public Collection getUsersInUserGroup(UserID userGroupID, String[] fetchgroups, int maxFetchDepth) 
    throws RemoteException, ModuleException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    Collection ids = um.getUserIDsInUserGroup(userGroupID);
    return super.getJDOObjects(null, ids.toArray(), fetchgroups, maxFetchDepth);
  }

  public Collection getUsersNotInUserGroup(UserID userGroupID, String[] fetchgroups, int maxFetchDepth) 
    throws RemoteException, ModuleException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    Collection ids = um.getUserIDsNotInUserGroup(userGroupID);
    return super.getJDOObjects(null, ids.toArray(), fetchgroups, maxFetchDepth);
  }

  protected Object retrieveJDOObject( String scope, Object objectID, String[] fetchGroups, int maxFetchDepth)
    throws Exception
  {
    return um.getUser((UserID)objectID, fetchGroups, maxFetchDepth);
  }
  
  protected Collection retrieveJDOObjects(String scope, Object[] objectIDs, String[] fetchGroups, int maxFetchDepth)
    throws Exception
  {
    return um.getUsers(objectIDs, fetchGroups, maxFetchDepth);
  }

}
