/**
 * Created Aug 23, 2005, 5:23:33 PM by nick
 */
package org.nightlabs.jfire.base.security;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.security.RoleGroupIDListCarrier;
import org.nightlabs.jfire.security.RoleGroupListCarrier;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class RoleGroupProvider extends JDOObjectProvider
{
  private static RoleGroupProvider _sharedInstance = null;
  private UserManager um;
  
  public RoleGroupProvider() 
    throws RemoteException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
  }
  
  public static RoleGroupProvider sharedInstance() 
    throws RemoteException, LoginException, CreateException, NamingException
  {
    if (_sharedInstance == null) 
      _sharedInstance = new RoleGroupProvider();

    return _sharedInstance;
  }

  public RoleGroupListCarrier getRoleGroups(String userID, String authorityID, String[] fetchgroups) 
    throws RemoteException, ModuleException, LoginException, CreateException, NamingException
  {
    um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
    RoleGroupIDListCarrier idc = um.getRoleGroupIDs(userID, authorityID);
    
    RoleGroupListCarrier rgc = new RoleGroupListCarrier();
    rgc.assigned.addAll(super.getJDOObjects(null, idc.assignedToUser.toArray(), fetchgroups));
    rgc.assignedByUserGroup.addAll(super.getJDOObjects(null, idc.assignedToUserGroups.toArray(), fetchgroups));
    rgc.excluded.addAll(super.getJDOObjects(null, idc.excluded.toArray(), fetchgroups));
    return rgc;
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
