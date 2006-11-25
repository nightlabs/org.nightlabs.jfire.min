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

package org.nightlabs.jfire.security;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDException;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.id.PropertyID;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserRefID;

/**
 * @author Alexander Bieber <alex@nightlabs.de>
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marco Schulze <marco@nightlabs.de>
 */

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/UserManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/UserManager"
 *	type="Stateless" 
 * 
 * @ejb.util generate = "physical"
 **/
public abstract class UserManagerBean
extends BaseSessionBeanImpl
implements SessionBean 
{
	/**
	 * LOG4J logger used by this class
	 */
  private static final Logger logger = Logger.getLogger(UserManagerBean.class);
  
  /**
   * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
   */
  public void setSessionContext(SessionContext sessionContext)
  throws EJBException, RemoteException
  {
    super.setSessionContext(sessionContext);
  }
  /**
   * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
   */
  public void unsetSessionContext() {
    super.unsetSessionContext();
  }
  
  /**
   * @ejb.create-method
   * @ejb.permission role-name="_Guest_"  
   * @!!!ejb.permission role-name="UserManager-read"
   */
  public void ejbCreate() throws CreateException
  {
  }

  /**
   * @see javax.ejb.SessionBean#ejbRemove()
   *
   * @ejb.permission unchecked="true"
   */
  public void ejbRemove() throws EJBException, RemoteException { }
  
//  /**
//   * 
//   * 
//   * @throws ModuleException
//   * 
//   * @deprecated use saveUser instead
//   * 
//   * @ejb.interface-method
//   * @ejb.permission role-name="UserManager-write"
//   * @ejb.transaction type = "Required"
//   */
//  public User saveDetachedUser(User user, String [] fetchGroups) 
//  	throws ModuleException 
//  {
//    PersistenceManager pm = this.getPersistenceManager();
//    try 
//		{
//    	User result = (User)NLJDOHelper.storeJDO(pm, user, true, fetchGroups);
//    	return result;
//    }
//    finally 
//		{
//    	pm.close();
//    }
//  }
  
  /**
   * Create a new user or change an existing one.
   * @param user The user to save
   * @param passwd The password for the user. This might be <code>null</code> for an existing user.
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   * @ejb.transaction type = "Required"
   **/
  public void saveUser(User user, String passwd)
  	throws SecurityException
  {
  	if (User.USERID_SYSTEM.equals(user.getUserID()))
  		throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USERID_SYSTEM + "\"!");
  	if (User.USERID_OTHER.equals(user.getUserID()))
  		throw new IllegalArgumentException("Cannot change properties of special user \"" + User.USERID_OTHER + "\"!");

    try
    {
      if (user.getOrganisationID() != null && !user.getOrganisationID().equals(getOrganisationID()))
        throw new IllegalArgumentException("user.organisationID must be null or equal to your organisationID!!!");

      if (user.getOrganisationID() == null)
      	user.setOrganisationID(getOrganisationID());

      PersistenceManager pm = this.getPersistenceManager();
      if (JDOHelper.isDetached(user))
      {
//        if(user.passwdChanged)
//        {
//          String password = user.getPassword();
//          if(user instanceof UserGroup)
//            throw new IllegalArgumentException("You cannot set a password for a UserGroup! userGroup.password must be null!");
//          if(user.passwdChanged)
//            user.setPassword(User.encryptPassword(user.getPassword()));
//        }
        user = (User) pm.makePersistent(user);
        if (passwd != null)
        	user.getUserLocal().setPasswordPlain(passwd);
      }
      else
      {
//        user.setPassword(User.encryptPassword(user.getPassword()));
      	UserLocal userLocal = new UserLocal(user);
      	userLocal.setPasswordPlain(passwd);
        pm.makePersistent(user);
        ConfigSetup.ensureAllPrerequisites(pm);
      }
      pm.close();
    }
    catch (ModuleException e)
    {
      throw new SecurityException(e);
    }
  }
  	
  /**
   * @see User.USERTYPE_ORGANISATION
   * @see User.USERTYPE_USER
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public UserSearchResult searchUsers (
      String userType,
      String searchStr, boolean exact, int itemsPerPage, int pageIndex, int userIncludeMask)
  throws SecurityException
  {
    try {
      PersistenceManager pm = getPersistenceManager();
      try {
        UserSearchResult result = User.searchUsers(
            pm, userType, searchStr, exact, itemsPerPage, pageIndex, userIncludeMask);
        
        result.makeTransient(userIncludeMask);
        return result;
      } finally {
        if (AuthorityManagerBean.CLOSE_PM) pm.close();
      }
    } catch (Exception x) {
      throw new SecurityException(x);
    }
  }

  /**
   * @param userType one of User.USERTYPE*
   * @return
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   */
  public Collection getUserIDsByType(String userType) 
  throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      Query query = pm.newQuery(pm.getExtent(User.class, true));
      query.declareImports("import java.lang.String");
      query.declareParameters("String userType"); // , String systemUserID, String otherUserID");
      query.setFilter("this.userType == userType"); //  && this.userID != systemUserID && this.userID != otherUserID");
      query.setOrdering("this.userID ascending");
      Collection c = (Collection)query.execute(userType); // , User.USERID_SYSTEM, User.USERID_OTHER);
      Iterator i = c.iterator();
      Collection ret = new HashSet();
      while(i.hasNext())
        ret.add(JDOHelper.getObjectId(i.next()));

      return ret;
    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @deprecated Use getUserIDsByType(...) and getUsers(...) instead
   * Calls {@link #getUsersByType(String, String[])} with only the default-fetch-group
   * 
   * @param userType one of User.USERTYPE*
   * @return
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   */
  public Collection getUsersByType(String userType) 
  throws ModuleException
  {
    return getUsersByType(userType,null);
  }
  
  /**
   * @deprecated Use getUserIDsByType(...) and getUsers(...) instead
   * @throws ModuleException
   * @see User.USERTYPE_ORGANISATION
   * @see User.USERTYPE_USER
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUsersByType(String userType, String [] fetchGroups) 
  throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);
      
      Query query = pm.newQuery(pm.getExtent(User.class, true));
      query.declareImports("import java.lang.String");
      query.declareParameters("String userType, String systemUserID, String otherUserID");
      query.setFilter("this.userType == userType && this.userID != systemUserID && this.userID != otherUserID");
      query.setOrdering("this.userID ascending");
      Collection c = (Collection)query.execute(userType, User.USERID_SYSTEM, User.USERID_OTHER);
      return pm.detachCopyAll(c);
    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getAllUsers() 
  	throws ModuleException
	{
  	return getAllUsers(null);
  }

  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getAllUsers(String [] fetchGroups) 
  	throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);

      Query query = pm.newQuery(pm.getExtent(User.class, true));
//      query.declareImports("import java.lang.String");
//      query.declareParameters("String systemUserID");
//      query.setFilter("this.userID != systemUserID");
//      query.setOrdering("this.userID ascending");
      Collection c = (Collection)query.execute(); // User.USERID_SYSTEM);
      return pm.detachCopyAll(c);
    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUserIDsInUserGroup(UserID userGroupID) 
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      UserGroup ug = (UserGroup)pm.getObjectById(userGroupID);
      Collection ret = new HashSet();
      Iterator i = ug.getUsers().iterator();
      while(i.hasNext())
        ret.add(JDOHelper.getObjectId(i.next()));

      return ret;
    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @deprecated Use getUserIDsInUserGroup(...) and getUsers(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUsersInUserGroup(String userGroupID) 
  	throws ModuleException
  {
  	return getUsersInUserGroup(userGroupID, null);
  }
  	
  /**
   * @deprecated Use getUserIDsInUserGroup(...) and getUsers(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUsersInUserGroup(String userGroupID, String [] fetchGroups) 
  	throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);

      UserGroup ug = (UserGroup)pm.getObjectById(UserID.create(getOrganisationID(), userGroupID));
      return (Collection)pm.detachCopyAll(ug.getUsers());
    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUserIDsNotInUserGroup(UserID userGroupID) 
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      Extent ext = pm.getExtent(User.class, true);

      // FIXME: JPOX generates "WHERE (1=0)" in SQL statement with this query
//      Query query = pm.newQuery( 
//          "SELECT FROM org.nightlabs.jfire.security.User " +
//          "WHERE " +
//          "  (userType == \"" + User.USERTYPE_USER + "\" || userType == \"" + User.USERTYPE_ORGANISATION + "\") &&" +
//          "  !(userGroup.users.containsValue(this)) &&" +
//          "  userGroup.organisationID == paramOrganisationID &&" +
//          "  userGroup.userID == paramUserGroupID " +
//          "  this.userID != \"" + User.USERID_SYSTEM + "\" && " +
//          "  this.userID != \"" + User.USERID_OTHER + "\" " +
//          "VARIABLES UserGroup userGroup " +
//          "PARAMETERS String paramOrganisationID, String paramUserGroupID " +
//          "import org.nightlabs.jfire.security.UserGroup; import java.lang.String");
//      Collection c = (Collection)query.execute(getOrganisationID(), userGroupID);
//      return (Collection)pm.detachCopyAll(c);

      // workaround start
      UserGroup ug = (UserGroup)pm.getObjectById(userGroupID);

      Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.User " +
          "WHERE " +
          "  (userType == \"" + User.USERTYPE_USER + "\" ||" +
          "  userType == \"" + User.USERTYPE_ORGANISATION + "\") && " +
          "  this.userID != \"" + User.USERID_SYSTEM + "\" && " +
          "  this.userID != \"" + User.USERID_OTHER + "\"");
      Collection c = (Collection)query.execute();

      Iterator i = c.iterator();
      Collection c2 = new HashSet();
      while(i.hasNext())
      {
        Object o = i.next();
        if(!ug.getUsers().contains(o))
          c2.add(JDOHelper.getObjectId(o));
      }
      return c2;
      // workaround end
    } 
    finally 
    {
      pm.close();
    }
  }  

  /**
   * @deprecated Use getUserIDsNotInUserGroup(...) and getUsers(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUsersNotInUserGroup(String userGroupID) 
  	throws ModuleException
  {
  	return getUsersNotInUserGroup(userGroupID, null);
  }  
  
  /**
   * @deprecated Use getUserIDsNotInUserGroup(...) and getUsers(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUsersNotInUserGroup(String userGroupID, String [] fetchGroups) 
  	throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);

      Extent ext = pm.getExtent(User.class, true);

      // FIXME: JPOX generates "WHERE (1=0)" in SQL statement with this query
//    	Query query = pm.newQuery( 
//    			"SELECT FROM org.nightlabs.jfire.security.User " +
//					"WHERE " +
//					"  (userType == \"" + User.USERTYPE_USER + "\" || userType == \"" + User.USERTYPE_ORGANISATION + "\") &&" +
//					"  !(userGroup.users.containsValue(this)) &&" +
//					"  userGroup.organisationID == paramOrganisationID &&" +
//					"  userGroup.userID == paramUserGroupID " +
//	      	"  this.userID != \"" + User.USERID_SYSTEM + "\" && " +
//					"  this.userID != \"" + User.USERID_OTHER + "\" " +
//					"VARIABLES UserGroup userGroup " +
//					"PARAMETERS String paramOrganisationID, String paramUserGroupID " +
//					"import org.nightlabs.jfire.security.UserGroup; import java.lang.String");
//    	Collection c = (Collection)query.execute(getOrganisationID(), userGroupID);
//    	return (Collection)pm.detachCopyAll(c);

      // workaround start
      UserGroup ug = (UserGroup)pm.getObjectById(UserID.create(getOrganisationID(), userGroupID));

      Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.User " +
      		"WHERE " +
					"  (userType == \"" + User.USERTYPE_USER + "\" ||" +
					"  userType == \"" + User.USERTYPE_ORGANISATION + "\") && " +
					"  this.userID != \"" + User.USERID_SYSTEM + "\" && " +
					"  this.userID != \"" + User.USERID_OTHER + "\"");
      Collection c = (Collection)query.execute();

      Iterator i = c.iterator();
      Collection c2 = new HashSet();
      while(i.hasNext())
      {
      	Object o = i.next();
      	if(!ug.getUsers().contains(o))
      		c2.add(o);
      }
      return pm.detachCopyAll(c2);
      // workaround end

    } 
    finally 
    {
      pm.close();
    }
  }
  
  /**
   * @deprecated Use getRoleGroupIDs(...) and getRoleGroups(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public RoleGroupListCarrier getRoleGroups(String userID, String authorityID) 
  	throws ModuleException
  {
  	return getRoleGroups(userID, authorityID, null);
  }
  
  /**
   * @deprecated Use getRoleGroupIDs(...) and getRoleGroups(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public RoleGroupListCarrier getRoleGroups(String userID, String authorityID, String [] fetchGroups) 
  	throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);
    	
      Extent ext = pm.getExtent(RoleGroup.class, false);

    	// rolegroups via userrefs
    	Query query = pm.newQuery( 
    			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
					"WHERE " +
					"  this == roleGroupRef.roleGroup &&" +
					"  roleGroupRef.userRefs.containsValue(userRef) &&" +
					"  userRef.authorityID == paramAuthorityID &&" +
					"  userRef.user == user &&" +
					"  user.organisationID == paramOrganisationID &&" +
					"  user.userID == paramUserID " +
					"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef; User user " +
					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
					"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import org.nightlabs.jfire.security.User; import java.lang.String");
    	Collection roleGroupsUser = (Collection)query.execute(getOrganisationID(), userID, authorityID);
    	
    	// rolegroups via usergroups
    	query = pm.newQuery( 
    			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
					"WHERE " +
					"  this == roleGroupRef.roleGroup &&" +
					"  roleGroupRef.userRefs.containsValue(userGroupRef) &&" +
					"  userGroupRef.user == userGroup &&" +
					"  userGroupRef.authorityID == paramAuthorityID &&" +
					"  userGroup.users.containsValue(user) &&" +
					"  user.organisationID == paramOrganisationID &&" +
					"  user.userID == paramUserID " +
					"VARIABLES RoleGroupRef roleGroupRef; UserGroupRef userGroupRef; UserGroup userGroup; User user " +
					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
					"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
    	Collection roleGroupsUserGroups = (Collection)query.execute(getOrganisationID(), userID, authorityID);

    	RoleGroupListCarrier rglc = new RoleGroupListCarrier();
    	rglc.assigned = pm.detachCopyAll(roleGroupsUser);
    	rglc.assignedByUserGroup = pm.detachCopyAll(roleGroupsUserGroups);

    	return rglc;
    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUserGroups(Object[] userGroupIDs, String[] fetchGroups, int maxFetchDepth) 
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      return NLJDOHelper.getDetachedObjectList(pm, userGroupIDs, null, fetchGroups, maxFetchDepth);
    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUsers(Object[] userIDs, String[] fetchGroups, int maxFetchDepth) 
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      return NLJDOHelper.getDetachedObjectList(pm, userIDs, null, fetchGroups, maxFetchDepth);
    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public UserGroupIDListCarrier getUserGroupIDs(String userID, String authorityID) 
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      Extent ext = pm.getExtent(UserGroup.class, false);
      
      Query query = pm.newQuery(
          "SELECT FROM org.nightlabs.jfire.security.UserGroup " +
          "WHERE " +
          "  users.containsKey(paramUserID) " +
          "PARAMETERS String paramUserID " +
          "import java.lang.String");
      Collection assignedGroups = (Collection)query.execute(userID);

      //FIXME: JPOX bug reoccured (negation generates INNER JOIN instead of LEFT OUTER JOIN)
//    Query query = pm.newQuery(
//        "SELECT FROM org.nightlabs.jfire.security.UserGroup " +
//        "WHERE " +
//        "  !(users.containsKey(paramUserID)) " +
//        "PARAMETERS String paramUserID " +
//        "import java.lang.String");
//
//    Collection excludedGroups = (Collection)query.execute(userID);
   
      // workaround start
      query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.UserGroup");
      Collection allGroups = (Collection)query.execute();
      Iterator i = allGroups.iterator();
      Collection excludedGroups = new HashSet();
      while(i.hasNext())
      {
        Object o = i.next();
        if(!assignedGroups.contains(o))
          excludedGroups.add(o);
      }
      // workaround end

      
      UserGroupIDListCarrier uglc = new UserGroupIDListCarrier();

      i = assignedGroups.iterator();
      while(i.hasNext())
        uglc.assigned.add(JDOHelper.getObjectId(i.next()));

      i = excludedGroups.iterator();
      while(i.hasNext())
        uglc.excluded.add(JDOHelper.getObjectId(i.next()));
      
      return uglc;
    } 
    finally 
    {
      pm.close();
    }
  }

  // FIXME JPOX bug workaround method. Remove this, once the jpox bug is fixed
  private static Collection getRoleGroupsForUserRef(PersistenceManager pm, String organisationID, String userID, String authorityID)
  {
  	Query query = pm.newQuery( 
        "SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
        "WHERE " +
        "  this == roleGroupRef.roleGroup &&" +
        "  roleGroupRef.userRefs.containsValue(userRef) &&" +
        "  userRef.organisationID == paramOrganisationID &&" +
        "  userRef.userID == paramUserID &&" +
        "  userRef.authorityID == paramAuthorityID " +
        "VARIABLES RoleGroupRef roleGroupRef; UserRef userRef " +
        "PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
        "import org.nightlabs.jfire.security.RoleGroupRef; " +
        "import org.nightlabs.jfire.security.UserRef; " +
        "import java.lang.String");
    return (Collection)query.execute(organisationID, userID, authorityID);
  }

  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public RoleGroupIDListCarrier getRoleGroupIDs(String userID, String authorityID) 
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
    	Query query;

      // rolegroups via userrefs
//      query = pm.newQuery( 
//          "SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//          "WHERE " +
//          "  this == roleGroupRef.roleGroup &&" +
//          "  roleGroupRef.userRefs.containsValue(userRef) &&" +
//          "  userRef.organisationID == paramOrganisationID &&" +
//          "  userRef.userID == paramUserID &&" +
//          "  userRef.authorityID == paramAuthorityID " +
//          "VARIABLES RoleGroupRef roleGroupRef; UserRef userRef " +
//          "PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//          "import org.nightlabs.jfire.security.RoleGroupRef; " +
//          "import org.nightlabs.jfire.security.UserRef; " +
//          "import java.lang.String");
//      Collection roleGroupsUser = new HashSet((Collection)query.execute(getOrganisationID(), userID, authorityID));
    	Collection roleGroupsUser = new HashSet(getRoleGroupsForUserRef(pm, getOrganisationID(), userID, authorityID));
    	// FIXME JPOX bug workarounds. Clean this up, once the jpox bug is fixed

      // rolegroups via usergroups
//      query = pm.newQuery( 
//          "SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//          "WHERE " +
//          "  this == roleGroupRef.roleGroup &&" +
//          "  roleGroupRef.userRefs.containsValue(userGroupRef) &&" +
//          "  userGroupRef.authorityID == paramAuthorityID &&" +
//          "  userGroup == userGroupRef.user &&" +
//          "  userGroup.users.containsValue(user) &&" +
//          "  user.organisationID == paramOrganisationID &&" +
//          "  user.userID == paramUserID " +
//          "VARIABLES RoleGroupRef roleGroupRef; UserGroupRef userGroupRef; UserGroup userGroup; User user " +
//          "PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//          "import org.nightlabs.jfire.security.RoleGroupRef; " +
//          "import org.nightlabs.jfire.security.UserRef; " +
//          "import org.nightlabs.jfire.security.UserGroup; " +
//          "import org.nightlabs.jfire.security.User; " +
//          "import java.lang.String");
//      Collection roleGroupsUserGroups = new HashSet((Collection)query.execute(getOrganisationID(), userID, authorityID));
    	Collection roleGroupsUserGroups = new HashSet();
    	pm.getExtent(User.class);
    	User user = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID));
    	for (Iterator iter = user.getUserGroups().iterator(); iter.hasNext();) {
				UserGroup userGroup = (UserGroup) iter.next();
				roleGroupsUserGroups.addAll(
						getRoleGroupsForUserRef(pm, userGroup.getOrganisationID(), userGroup.getUserID(), authorityID));
			}

      //FIXME: JPOX bug (INNER JOIN instead of LEFT JOIN)
      // excluded rolegroups
//    query = pm.newQuery( 
//        "SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//        "WHERE " +
//        "  this == roleGroupRef.roleGroup &&" +
//        "  !(roleGroupRef.userRefs.containsValue(userRef)) &&" +
//        "  userRef.organisationID == paramOrganisationID &&" +
//        "  userRef.userID == paramUserID &&" +
//        "  roleGroupRef.authorityID == paramAuthorityID " +
//        "VARIABLES RoleGroupRef roleGroupRef; UserRef userRef " +
//        "PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//        "import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
//
//    Collection excludedRoleGroups = (Collection)query.execute(getOrganisationID(), userID, authorityID);

      // workaround start
      query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.RoleGroup");
      Collection allRoleGroups = (Collection)query.execute();
      Iterator i = allRoleGroups.iterator();
      Collection excludedRoleGroups = new HashSet();
      while(i.hasNext())
      {
        Object o = i.next();
        if((!roleGroupsUser.contains(o)) && (!roleGroupsUserGroups.contains(o)))
          excludedRoleGroups.add(o);
      }
      // workaround end

      RoleGroupIDListCarrier rglc = new RoleGroupIDListCarrier(
      		NLJDOHelper.getObjectIDSet(excludedRoleGroups),
      		NLJDOHelper.getObjectIDSet(roleGroupsUser),
      		NLJDOHelper.getObjectIDSet(roleGroupsUserGroups));

//      i = roleGroupsUser.iterator();
//      while(i.hasNext())
//        rglc.assignedToUser.add(JDOHelper.getObjectId(i.next()));
//
//      i = roleGroupsUserGroups.iterator();
//      while(i.hasNext())
//        rglc.assignedToUserGroups.add(JDOHelper.getObjectId(i.next()));
//
//      i = excludedRoleGroups.iterator();
//      while(i.hasNext())
//        rglc.excluded.add(JDOHelper.getObjectId(i.next()));

      return rglc;
    } 
    finally 
    {
      pm.close();
    }
  }


  /**
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getRoleGroups(Object[] roleGroupIDs, String [] fetchGroups, int maxFetchDepth)
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      return NLJDOHelper.getDetachedObjectList(pm, roleGroupIDs, null, fetchGroups, maxFetchDepth);
    } 
    finally 
    {
      pm.close();
    }
  }

  
  /**
   * @deprecated Use getRoleGroupIDs(...) and getRoleGroups(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getExcludedRoleGroups(String userID, String authorityID) 
  	throws ModuleException 
  {
  	return getExcludedRoleGroups(userID, authorityID, null);
  }
  /**
   * @deprecated Use getRoleGroupIDs(...) and getRoleGroups(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getExcludedRoleGroups(String userID, String authorityID, String [] fetchGroups) 
  	throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);
    	
      Extent ext = pm.getExtent(RoleGroup.class, false);

      //FIXME: JPOX bug (INNER JOIN instead of LEFT JOIN)
//    	Query query = pm.newQuery( 
//    			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//					"WHERE " +
//					"  this == roleGroupRef.roleGroup &&" +
//					"  !(roleGroupRef.userRefs.containsValue(userRef)) &&" +
//					"  userRef.organisationID == paramOrganisationID &&" +
//					"  userRef.userID == paramUserID &&" +
//					"  roleGroupRef.authorityID == paramAuthorityID " +
//					"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef " +
//					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//					"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
//
//    	Collection res = (Collection)query.execute(getOrganisationID(), userID, authorityID);

//    	Query query = pm.newQuery( 
//    			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//					"WHERE " +
//					"  this == roleGroupRef.roleGroup &&" +
//					"  ! roleGroupRef.userRefs.containsValue(userRef) &&" +
//					"  userRef.authorityID == paramAuthorityID &&" +
//					"  userRef.user == user &&" +
//					"  user.organisationID == paramOrganisationID &&" +
//					"  user.userID == paramUserID " +
//					"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef; User user " +
//					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//					"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
//    	Collection res = (Collection)query.execute(getOrganisationID(), userID, authorityID);
//
//      return pm.detachCopyAll(res);

      // workaround start
      Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.RoleGroup");
      Collection c = (Collection)query.execute();
    	query = pm.newQuery( 
    			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
					"WHERE " +
					"  this == roleGroupRef.roleGroup &&" +
					"  roleGroupRef.userRefs.containsValue(userRef) &&" +
					"  userRef.authorityID == paramAuthorityID &&" +
					"  userRef.user == user &&" +
					"  user.organisationID == paramOrganisationID &&" +
					"  user.userID == paramUserID " +
					"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef; User user " +
					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
					"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import org.nightlabs.jfire.security.User; import java.lang.String");
    	Collection roleGroupsUser = (Collection)query.execute(getOrganisationID(), userID, authorityID);
    	query = pm.newQuery( 
    			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
					"WHERE " +
					"  this == roleGroupRef.roleGroup &&" +
					"  roleGroupRef.userRefs.containsValue(userGroupRef) &&" +
					"  userGroupRef.user == userGroup &&" +
					"  userGroupRef.authorityID == paramAuthorityID &&" +
					"  userGroup.users.containsValue(user) &&" +
					"  user.organisationID == paramOrganisationID &&" +
					"  user.userID == paramUserID " +
					"VARIABLES RoleGroupRef roleGroupRef; UserGroupRef userGroupRef; UserGroup userGroup; User user " +
					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
					"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
    	Collection roleGroupsUserGroups = (Collection)query.execute(getOrganisationID(), userID, authorityID);
      
      Iterator i = c.iterator();
      Collection c2 = new HashSet();
      while(i.hasNext())
      {
      	Object o = i.next();
      	if((!roleGroupsUser.contains(o)) && (!roleGroupsUserGroups.contains(o)))
      		c2.add(o);
      }
      return pm.detachCopyAll(c2);
      // workaround end

    } 
    finally 
    {
      pm.close();
    }
  }

  /**
   * @deprecated Use getUserGroupIDs(...) and getUserGroups(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUserGroups(String userID) 
  	throws ModuleException
  {
  	return getUserGroups(userID, null);
  }  
  
  /**
   * @deprecated Use getUserGroupIDs(...) and getUserGroups(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getUserGroups(String userID, String [] fetchGroups) 
  	throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);

//      Extent ext = pm.getExtent(UserGroup.class, false);
    	Query query = pm.newQuery(
    			"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
					"WHERE " +
					"  users.containsKey(paramUserID) " +
					"PARAMETERS String paramUserID " +
					"import java.lang.String");
      
      Collection c = (Collection)query.execute(userID);
      return pm.detachCopyAll(c);
    } 
    finally 
    {
      pm.close();
    }
  }


  /**
   * @deprecated Use getUserGroupIDs(...) and getUserGroups(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getExcludedUserGroups(String userID) 
  	throws ModuleException
  {
  	return getExcludedUserGroups(userID, null);
  }
  
  /**
   * @deprecated Use getUserGroupIDs(...) and getUserGroups(...) instead
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   **/
  public Collection getExcludedUserGroups(String userID, String [] fetchGroups) 
  	throws ModuleException
  {
  	PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);

      //FIXME: JPOX bug reoccured (negation generates INNER JOIN instead of LEFT OUTER JOIN)
//    	Query query = pm.newQuery(
//    			"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
//					"WHERE " +
//					"  !(users.containsKey(paramUserID)) " +
//					"PARAMETERS String paramUserID " +
//					"import java.lang.String");
//
//      Collection c = (Collection)query.execute(userID);


      // workaround start
      Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.UserGroup");
      Collection c = (Collection)query.execute();
    	query = pm.newQuery(
    			"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
					"WHERE " +
					"  users.containsKey(paramUserID) " +
					"PARAMETERS String paramUserID " +
					"import java.lang.String");
      
      Collection includedUsers = (Collection)query.execute(userID);
      Iterator i = c.iterator();
      Collection c2 = new HashSet();
      while(i.hasNext())
      {
      	Object o = i.next();
      	if(!includedUsers.contains(o))
      		c2.add(o);
      }
      return pm.detachCopyAll(c2);
      // workaround end

      //      return pm.detachCopyAll(c);
      
    } 
    finally 
    {
      pm.close();
    }
  }

  
  /**
   * Check if a user ID exists. Needs role "UserManager-write"; used to check ID while creating new user
   * @throws ModuleException
   * @throws ObjectIDException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public boolean userIDAlreadyRegistered(UserID userID) 
  throws ModuleException, ObjectIDException
  {
    PersistenceManager pm = getPersistenceManager();
    try
    {
      Object test = pm.getObjectById(userID, true);
      logger.debug("userIDAlreadyRegistered(\"" + userID + "\") = " + (test != null));
      return (test != null);
    }
    catch(JDOObjectNotFoundException e) 
    {
      logger.debug("userIDAlreadyRegistered(\"" + userID + "\") = false");
      return false;
    }
    finally
    {
      pm.close();
    }
  }
  
  /**
   * Returns a detached user.
   * 
   * @param userID id of the user
   * @return the detached user
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   */
  public User getUser(UserID userID, String[] fetchGroups, int maxFetchDepth)
  	throws ModuleException
  {
    PersistenceManager pm = this.getPersistenceManager();	    
    try
    {
    	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);

      //FIXME: JPOX issue with detached Collections (always attached, problem with dirty bit)
      //FIXME: JPOX generates invalid SQL when using this workaround
//      pm.getFetchPlan().addGroup(User.FETCH_GROUP_USERREFS);
      // workaround end

      pm.getExtent(User.class, true);
      Object o = pm.getObjectById(userID,true);
      User usr = (User)pm.detachCopy(o);

      // FIXME: JPOX => load-fetch-group does not work
      // this workaround makes User.person always dirty before it can be modified by the client (person is 
      // always attached when saving user!)
//      try
//			{
//      	Person person = usr.getPerson();
//      	if(person != null)
//      	{
//      		pm.getFetchPlan().setGroups(new String[] {FetchPlan.ALL});
//      		pm.getExtent(User.class, true);
//      		Object p = pm.getObjectById(PersonID.create(getOrganisationID(), person.getPersonID()),true);
//      		usr.setPerson((Person)pm.detachCopy(p));
//      	}
//			}
//      catch(JDODetachedFieldAccessException e2)
//			{
//      	// do nothing
//      }
      // workaround end
      
      return usr;
    }
    catch(JDOObjectNotFoundException e) 
    {
      throw new ModuleException(e);
    }
    finally
    {
      pm.close();
    }
  }
  
  /**
   * @param userID The ID of the user to be returned.
   * @param includeMask What linked objects shall be included in the transient result. See User.INCLUDE* for details.
   * @return Returns the User with the given userID. If this user does not exist,
   *			a UserNotFoundException is thrown.
   *
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-read"
   * @deprecated
   **/
  public User getUser(String userID, int includeMask)
  throws SecurityException
  {
    try {
      PersistenceManager pm = getPersistenceManager();
      try {
        pm.getExtent(User.class, true);
        try {
          User user = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
          //					user = (User)pm.detachCopy(user);
          user.makeTransient(includeMask);
//          user.setPassword(null);
          return user;
        } catch (JDOObjectNotFoundException x) {
          throw new UserNotFoundException("User \""+userID+"\" not found at organisation \""+getOrganisationID()+"\"!");
        }
      } finally {
        if (AuthorityManagerBean.CLOSE_PM) pm.close();
      }
    } catch (SecurityException x) {
      throw x;
    } catch (Exception x) {
      throw new SecurityException(x);
    }
  }


  /**
   * Assign rolegroups to a user (should be renamed)
   * @param userID id of the user
   * @param authorityID id of the authority for which to get the rolegroups
   * @param roleGroupIDs Collection of rolegroup IDs
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void addUserToRoleGroups(String userID, String authorityID, Collection roleGroupIDs) 
  	throws ModuleException
  {
  	Iterator i = roleGroupIDs.iterator();
  	while(i.hasNext())
  	{
  		Object o = i.next();
  		if(o instanceof String)
  			addUserToRoleGroup(userID, authorityID, (String)o);
  	}
  }

  /**
   * Assign a rolegroup to a user (should be renamed)
   * @param userID id of the user
   * @param authorityID id of the authority the user gets the rolegroup for
   * @param roleGroupID ID of the rolegroup
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void addUserToRoleGroup(String userID, String authorityID, String roleGroupID) 
  	throws ModuleException
  {
  	if (User.USERID_SYSTEM.equals(userID))
  		throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USERID_SYSTEM + "\"!");

  	PersistenceManager pm = getPersistenceManager();
  	try 
		{
  		pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
  		pm.getExtent(Authority.class, true);
 			Authority auth = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
  		pm.getExtent(User.class, true);
 			User usr = (User)pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
  		pm.getExtent(RoleGroup.class, true);
  		RoleGroup rg = (RoleGroup)pm.getObjectById(RoleGroupID.create(roleGroupID), true);
  		
  		RoleGroupRef rgf = auth.createRoleGroupRef(rg);
  		UserRef ur = auth.createUserRef(usr);
  		ur.addRoleGroupRef(rgf);
		} 
    catch(JDOObjectNotFoundException e) 
    {
      throw new ModuleException(e);
    }
  	finally 
		{
  		pm.close();
		}
  }

  /**
   * Add users to a usergroup
   * @param userGoupID id of the user group
   * @param userIDs Collection of user IDs
   * @throws SecurityException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void addUsersToUserGroup(String userGroupID, Collection userIDs) 
  	throws SecurityException
  {
  	Iterator i = userIDs.iterator();
  	while(i.hasNext())
  	{
  		Object o = i.next();
  		if(o instanceof String)
  			addUserToUserGroup((String)o, userGroupID);
  	}
  }
  
  
  /**
   * Add a user to usergroups
   * @param userID id of the user
   * @param userGroupIDs Collection of usergroup IDs
   * @throws SecurityException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void addUserToUserGroups(String userID, Collection userGroupIDs) 
  	throws SecurityException
  {
  	Iterator i = userGroupIDs.iterator();
  	while(i.hasNext())
  	{
  		Object o = i.next();
  		if(o instanceof String)
  			addUserToUserGroup(userID, (String)o);
  	}
  }
  
  
  /**
   * Add a user to a usergroup
   * @param userID id of the user
   * @param userGroupID id of the usergroup
   * @throws SecurityException
   *
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void addUserToUserGroup(String userID, String userGroupID)
  throws SecurityException
  {
  	if (User.USERID_SYSTEM.equals(userID))
  		throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USERID_SYSTEM + "\"!");

    try {
      PersistenceManager pm = getPersistenceManager();
      try {
      	pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
        pm.getExtent(User.class, true);
        pm.getExtent(UserGroup.class, true);
        
        User user;
        try {
          user = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
        } catch (JDOObjectNotFoundException x) {
          throw new UserNotFoundException("User \""+userID+"\" not found at organisation \""+getOrganisationID()+"\"!");
        }
        
        UserGroup userGroup;
        try {
          User tmpUser = (User) pm.getObjectById(UserID.create(getOrganisationID(), userGroupID), true);
          
          if (!(tmpUser instanceof UserGroup))
            throw new ClassCastException("userGroupID \""+userGroupID+"\" does not represent an object of type UserGroup, but of "+tmpUser.getClass().getName());
          
          userGroup = (UserGroup) tmpUser;
        } catch (JDOObjectNotFoundException x) {
          throw new UserNotFoundException("UserGroup \""+userGroupID+"\" not found at organisation \""+getOrganisationID()+"\"!");
        }
        
        userGroup.addUser(user);
        
      } finally {
        if (AuthorityManagerBean.CLOSE_PM) pm.close();
      }
    } catch (SecurityException x) {
      throw x;
    } catch (Exception x) {
      throw new SecurityException(x);
    }
  }

  /**
   * Revoke rolegroups from a user (should be renamed)
   * @param userID id of the user
   * @param authorityID id of the authority for which the rolegroups are revoked
   * @param roleGroupIDs Collection of role group IDs
   * @throws ModuleException
   * 
	 * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void removeUserFromRoleGroups(String userID, String authorityID, Collection roleGroupIDs) 
  	throws ModuleException
  {
  	Iterator i = roleGroupIDs.iterator();
  	while(i.hasNext())
  	{
  		Object o = i.next();
  		if(o instanceof String)
  			removeUserFromRoleGroup(userID, authorityID, (String)o);
  	}
  }

  /**
   * Revoke rolegroup from a user (should be renamed)
   * @param userID the id of the user
   * @param authorityID the id of the authority for which the rolegroup is revoked
   * @param roleGroupID the rolegroup id
   * @throws ModuleException
   *
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void removeUserFromRoleGroup(String userID, String authorityID, String roleGroupID)
  	throws ModuleException
  {
  	PersistenceManager pm = getPersistenceManager();
  	try 
		{
  		pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
 			UserRef uref = (UserRef)pm.getObjectById(UserRefID.create(authorityID, getOrganisationID(), userID), true);
 			uref.removeRoleGroupRef(roleGroupID);
		} 
    catch(JDOObjectNotFoundException e) 
    {
      throw new ModuleException(e);
    }
  	finally 
		{
  		pm.close();
		}
  }
  

  /**
   * Remove users from usergroup
   * @param userGroupID the usergroup ID
   * @param userIDs Collection of user IDs
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void removeUsersFromUserGroup(String userGroupID, Collection userIDs) 
  	throws ModuleException
  {
  	Iterator i = userIDs.iterator();
  	while(i.hasNext())
  	{
  		Object o = i.next();
  		if(o instanceof String)
  			removeUserFromUserGroup((String)o, userGroupID);
  	}
  }

  /**
   * Remove user from usergroups
   * @param userID the user ID
   * @param userGroupIDs Collection of usergroup IDs
   * @throws ModuleException
   * 
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  public void removeUserFromUserGroups(String userID, Collection userGroupIDs) 
  	throws ModuleException
  {
  	Iterator i = userGroupIDs.iterator();
  	while(i.hasNext())
  	{
  		Object o = i.next();
  		if(o instanceof String)
  			removeUserFromUserGroup(userID, (String)o);
  	}
  }

  
  /**
   * Remove user from a usergroup
   * @param userID the user ID
   * @param userGroupID the usergroup ID
   * @throws ModuleException
   *
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  
  public void removeUserFromUserGroup(String userID, String userGroupID)
  throws ModuleException
  {
  	PersistenceManager pm = getPersistenceManager();
  	try 
		{
  		pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
//  		pm.getExtent(User.class, true);
//  		User user = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
  		pm.getExtent(UserGroup.class, true);
  		UserGroup userGroup = (UserGroup) pm.getObjectById(UserID.create(getOrganisationID(), userGroupID), true);
  		
  		
  		userGroup.removeUser(userID);
		}
  	catch(JDOObjectNotFoundException e) 
		{
  		throw new ModuleException(e);
		}
  	finally 
		{
  		pm.close();
		}
  }

  
  /**
   * Assign a person to a user
   * @param userID the user ID
   * @param personID the person ID
   * @throws ModuleException
   *
   * @ejb.interface-method
   * @ejb.permission role-name="UserManager-write"
   **/
  
  public void assignPersonToUser(String userID, long personID)
  	throws ModuleException
  {
  	PersistenceManager pm = getPersistenceManager();
  	try 
		{
  		pm.getExtent(User.class, true);
  		User usr = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
  		
  		pm.getExtent(Person.class, true);
  		Person ps = (Person) pm.getObjectById(PropertyID.create(getOrganisationID(), personID), true);
  		
  		usr.setPerson(ps);
		}
  	catch(JDOObjectNotFoundException e) 
		{
  		throw new ModuleException(e);
		}
  	finally 
		{
  		pm.close();
		}
  }

  
  /**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void whoami()
		throws ModuleException
	{
		logger.info("******** WHOAMI: "+getPrincipalString());
	}

  //	/**
  //	 * Normally, there is a timeout of 30 mins until a change at the user rights
  //	 * gets active. To flush the cache immediately, call this method.
  //	 * <br/><br/>
  //	 * <b>Warning! Calling this method might cause trouble to currently logged in
  //	 * users!</b> Thus, you should avoid flushing manually.
  //	 * <br/><br/>
  //	 * Because all users of the current server - and not only the current organisation
  //	 * is affected by this, the privilege "_ServerAdmin_" is required for this method.
  //	 *
  //	 * @throws SecurityException
  //	 *
  //	 * @ejb.interface-method
  //	 * @ejb.permission role-name="_ServerAdmin_"
  //	 * @ejb.transaction type = "Required"
  //	 */
  //	public void flushAuthenticationCache()
  //		throws SecurityException
  //	{
  //		try {
  //			JFireServerManager ism = getJFireServerManager();
  //			try {
  //				ism.j2ee_flushAuthenticationCache();
  //			} finally {
  //				ism.close();
  //			}
  //		} catch (Exception x) {
  //			throw new SecurityException(x);
  //		}
  //	}
  
}
