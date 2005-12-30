/**
 * Created Aug 23, 2005, 5:23:19 PM by nick
 */
package org.nightlabs.ipanema.base.security;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.ModuleException;
import org.nightlabs.ipanema.base.jdo.JDOObjectProvider;
import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.security.UserManager;
import org.nightlabs.ipanema.security.UserManagerUtil;
import org.nightlabs.ipanema.security.id.UserID;

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

  public User getUser(UserID id, String[] fetchGroups)
    throws RemoteException, LoginException, CreateException, NamingException
  {
    //TODO: is there a way to check if a session bean is still valid? (e.g. after server crash)
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    return (User)super.getJDOObject(null, id, fetchGroups);
  }
  
  public Collection getUsersByType(String type, String[] fetchgroups) 
    throws RemoteException, ModuleException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    Collection ids = um.getUserIDsByType(type);
    return super.getJDOObjects(null, ids.toArray(), fetchgroups);
  }

  public Collection getUsersInUserGroup(UserID userGroupID, String[] fetchgroups) 
    throws RemoteException, ModuleException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    Collection ids = um.getUserIDsInUserGroup(userGroupID);
    return super.getJDOObjects(null, ids.toArray(), fetchgroups);
  }

  public Collection getUsersNotInUserGroup(UserID userGroupID, String[] fetchgroups) 
    throws RemoteException, ModuleException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    Collection ids = um.getUserIDsNotInUserGroup(userGroupID);
    return super.getJDOObjects(null, ids.toArray(), fetchgroups);
  }

  protected Object retrieveJDOObject( String scope, Object objectID, String[] fetchGroups)
    throws Exception
  {
    return um.getUser((UserID)objectID, fetchGroups);
  }
  
  protected Collection retrieveJDOObjects(String scope, Object[] objectIDs, String[] fetchGroups)
    throws Exception
  {
    return um.getUsers(objectIDs, fetchGroups);
  }

}
