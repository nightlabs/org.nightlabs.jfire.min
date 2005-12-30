/**
 * Created Aug 23, 2005, 5:23:51 PM by nick
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
import org.nightlabs.ipanema.security.UserGroupIDListCarrier;
import org.nightlabs.ipanema.security.UserGroupListCarrier;
import org.nightlabs.ipanema.security.UserManager;
import org.nightlabs.ipanema.security.UserManagerUtil;

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
