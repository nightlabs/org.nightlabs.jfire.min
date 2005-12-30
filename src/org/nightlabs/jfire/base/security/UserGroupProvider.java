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
import org.nightlabs.jfire.security.UserGroupIDListCarrier;
import org.nightlabs.jfire.security.UserGroupListCarrier;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class UserGroupProvider extends JDOObjectProvider
{
  private static UserGroupProvider _sharedInstance = null;
  private UserManager um;
  
  public UserGroupProvider() 
    throws RemoteException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
  }
  
  public static UserGroupProvider sharedInstance() 
    throws RemoteException, LoginException, CreateException, NamingException
  {
    if (_sharedInstance == null) 
      _sharedInstance = new UserGroupProvider();

    return _sharedInstance;
  }

  public UserGroupListCarrier getUserGroups(String userID, String authorityID, String[] fetchgroups) 
    throws RemoteException, ModuleException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    UserGroupIDListCarrier idc = um.getUserGroupIDs(userID, authorityID);
  
    UserGroupListCarrier ugc = new UserGroupListCarrier();
    ugc.assigned.addAll(super.getJDOObjects(null, idc.assigned.toArray(), fetchgroups));
    ugc.excluded.addAll(super.getJDOObjects(null, idc.excluded.toArray(), fetchgroups));
    
    return ugc;
  }

  
  protected Object retrieveJDOObject( String scope, Object objectID, String[] fetchGroups)
  throws Exception
  {
    Collection ret = retrieveJDOObjects(null, new Object[] {objectID}, fetchGroups);
    if(ret.iterator().hasNext())
      return ret.iterator().next();
    else
      return null;
  }
  
  protected Collection retrieveJDOObjects(String scope, Object[] objectIDs, String[] fetchGroups)
  throws Exception
  {
    return um.getUserGroups(objectIDs, fetchGroups);
  }

}
