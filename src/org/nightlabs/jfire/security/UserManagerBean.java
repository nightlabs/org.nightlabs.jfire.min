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
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserRefID;
import org.nightlabs.jfire.security.search.UserQuery;
import org.nightlabs.jfire.servermanager.JFireServerManager;

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
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 **/
public abstract class UserManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(UserManagerBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	@Override
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

	/**
	 * Create a new user or change an existing one.
	 * @param user The user to save
	 * @param passwd The password for the user. This might be <code>null</code> for an existing user.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-write"
	 * @ejb.transaction type="Required"
	 **/
	public void saveUser(User user, String passwd)
	throws SecurityException
	{
		if (User.USERID_SYSTEM.equals(user.getUserID()))
			throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USERID_SYSTEM + "\"!");
		if (User.USERID_OTHER.equals(user.getUserID()))
			throw new IllegalArgumentException("Cannot change properties of special user \"" + User.USERID_OTHER + "\"!");

//		try
//		{
		if (user.getOrganisationID() != null && !user.getOrganisationID().equals(getOrganisationID()))
			throw new IllegalArgumentException("user.organisationID must be null or equal to your organisationID!!!");

		if (user.getOrganisationID() == null)
			user.setOrganisationID(getOrganisationID());

		PersistenceManager pm = this.getPersistenceManager();
		try {
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
				user = pm.makePersistent(user);
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
		} finally {
			pm.close();
		}
//		}
//		catch (ModuleException e) {
//		throw new SecurityException(e);
//		}
	}

	/**
	 * Create a new user or change an existing one.
	 *
	 * @param user The user to save
	 * @param passwd The password for the user. This might be <code>null</code> for an existing user.
	 * @param get Whether to return the newly saved user.
	 * @param fetchGroups The fetch-groups to detach the returned User with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-write"
	 * @ejb.transaction type="Required"
	 **/
	public User storeUser(User user, String passwd, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws SecurityException
	{
		if (User.USERID_SYSTEM.equals(user.getUserID()))
			throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USERID_SYSTEM + "\"!");
		if (User.USERID_OTHER.equals(user.getUserID()))
			throw new IllegalArgumentException("Cannot change properties of special user \"" + User.USERID_OTHER + "\"!");

		if (user.getOrganisationID() != null && !user.getOrganisationID().equals(getOrganisationID()))
			throw new IllegalArgumentException("user.organisationID must be null or equal to your organisationID!!!");

		if (user.getOrganisationID() == null)
			user.setOrganisationID(getOrganisationID());

		String newPassword = user.getUserLocal().getNewPassword();

		PersistenceManager pm = this.getPersistenceManager();
		try {
			user = pm.makePersistent(user);

			if (user.getUserLocal() == null)
				new UserLocal(user); // self-registering

//			if (passwd != null && !passwd.equals(UserLocal.UNCHANGED_PASSWORD))
//				user.getUserLocal().setPasswordPlain(passwd);

			if (newPassword != null)
				user.getUserLocal().setPasswordPlain(newPassword);

			ConfigSetup.ensureAllPrerequisites(pm);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(user);
		} finally {
			pm.close();
		}
	}

	/**
	 * @deprecated should not be used anymore
	 * @see User.USERTYPE_ORGANISATION
	 * @see User.USERTYPE_USER
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-read"
	 **/
	@Deprecated
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

//				result.makeTransient(userIncludeMask);
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
	public Collection<UserID> getUserIDsByType(String userType)
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
			Collection<User> c = (Collection<User>)query.execute(userType); // , User.USERID_SYSTEM, User.USERID_OTHER);
			Iterator<User> i = c.iterator();
			Collection<UserID> ret = new HashSet<UserID>();
			while(i.hasNext())
				ret.add((UserID) JDOHelper.getObjectId(i.next()));

			return ret;
		}
		finally {
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
	@Deprecated
	public Collection<User> getUsersByType(String userType)
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
	@Deprecated
	public Collection<User> getUsersByType(String userType, String [] fetchGroups)
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
			Collection<User> c = (Collection<User>)query.execute(userType, User.USERID_SYSTEM, User.USERID_OTHER);
			return pm.detachCopyAll(c);
		}
		finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-read"
	 **/
	public Collection<User> getAllUsers()
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
	public Collection<User> getAllUsers(String [] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query query = pm.newQuery(pm.getExtent(User.class, true));
//			query.declareImports("import java.lang.String");
//			query.declareParameters("String systemUserID");
//			query.setFilter("this.userID != systemUserID");
//			query.setOrdering("this.userID ascending");
			Collection<User> c = (Collection<User>)query.execute(); // User.USERID_SYSTEM);
			return pm.detachCopyAll(c);
		}
		finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-read"
	 **/
	public Collection<UserID> getUserIDsInUserGroup(UserID userGroupID)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			UserGroup ug = (UserGroup)pm.getObjectById(userGroupID);
			Collection<UserID> ret = new HashSet<UserID>();
			Iterator<User> i = ug.getUsers().iterator();
			while(i.hasNext())
				ret.add((UserID) JDOHelper.getObjectId(i.next()));

			return ret;
		}
		finally {
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
	@Deprecated
	public Collection<User> getUsersInUserGroup(String userGroupID)
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
	@Deprecated
	public Collection<User> getUsersInUserGroup(String userGroupID, String [] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			UserGroup ug = (UserGroup)pm.getObjectById(UserID.create(getOrganisationID(), userGroupID));
			return pm.detachCopyAll(ug.getUsers());
		}
		finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-read"
	 **/
	public Collection<UserID> getUserIDsNotInUserGroup(UserID userGroupID)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
//			Extent ext = pm.getExtent(User.class, true);

			// FIXME: JPOX generates "WHERE (1=0)" in SQL statement with this query
//			Query query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.User " +
//			"WHERE " +
//			"  (userType == \"" + User.USERTYPE_USER + "\" || userType == \"" + User.USERTYPE_ORGANISATION + "\") &&" +
//			"  !(userGroup.users.containsValue(this)) &&" +
//			"  userGroup.organisationID == paramOrganisationID &&" +
//			"  userGroup.userID == paramUserGroupID " +
//			"  this.userID != \"" + User.USERID_SYSTEM + "\" && " +
//			"  this.userID != \"" + User.USERID_OTHER + "\" " +
//			"VARIABLES UserGroup userGroup " +
//			"PARAMETERS String paramOrganisationID, String paramUserGroupID " +
//			"import org.nightlabs.jfire.security.UserGroup; import java.lang.String");
//			Collection c = (Collection)query.execute(getOrganisationID(), userGroupID);
//			return (Collection)pm.detachCopyAll(c);

			// workaround start
			UserGroup ug = (UserGroup)pm.getObjectById(userGroupID);

			Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.User " +
					"WHERE " +
					"  (userType == \"" + User.USERTYPE_USER + "\" ||" +
					"  userType == \"" + User.USERTYPE_ORGANISATION + "\") && " +
					"  this.userID != \"" + User.USERID_SYSTEM + "\" && " +
					"  this.userID != \"" + User.USERID_OTHER + "\"");
			Collection<User> c = (Collection<User>)query.execute();

			Iterator<User> i = c.iterator();
			Collection<UserID> c2 = new HashSet<UserID>();
			while(i.hasNext())
			{
				Object o = i.next();
				if(!ug.getUsers().contains(o))
					c2.add((UserID) JDOHelper.getObjectId(o));
			}
			return c2;
			// workaround end
		}
		finally {
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
	@Deprecated
	public Collection<User> getUsersNotInUserGroup(String userGroupID)
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
	@Deprecated
	public Collection<User> getUsersNotInUserGroup(String userGroupID, String [] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

//			Extent ext = pm.getExtent(User.class, true);

			// FIXME: JPOX generates "WHERE (1=0)" in SQL statement with this query
//			Query query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.User " +
//			"WHERE " +
//			"  (userType == \"" + User.USERTYPE_USER + "\" || userType == \"" + User.USERTYPE_ORGANISATION + "\") &&" +
//			"  !(userGroup.users.containsValue(this)) &&" +
//			"  userGroup.organisationID == paramOrganisationID &&" +
//			"  userGroup.userID == paramUserGroupID " +
//			"  this.userID != \"" + User.USERID_SYSTEM + "\" && " +
//			"  this.userID != \"" + User.USERID_OTHER + "\" " +
//			"VARIABLES UserGroup userGroup " +
//			"PARAMETERS String paramOrganisationID, String paramUserGroupID " +
//			"import org.nightlabs.jfire.security.UserGroup; import java.lang.String");
//			Collection c = (Collection)query.execute(getOrganisationID(), userGroupID);
//			return (Collection)pm.detachCopyAll(c);

			// workaround start
			UserGroup ug = (UserGroup)pm.getObjectById(UserID.create(getOrganisationID(), userGroupID));

			Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.User " +
					"WHERE " +
					"  (userType == \"" + User.USERTYPE_USER + "\" ||" +
					"  userType == \"" + User.USERTYPE_ORGANISATION + "\") && " +
					"  this.userID != \"" + User.USERID_SYSTEM + "\" && " +
					"  this.userID != \"" + User.USERID_OTHER + "\"");
			Collection<User> c = (Collection<User>)query.execute();

			Iterator<User> i = c.iterator();
			Collection<User> c2 = new HashSet<User>();
			while(i.hasNext())
			{
				User user = i.next();
				if(!ug.getUsers().contains(user))
					c2.add(user);
			}
			return pm.detachCopyAll(c2);
			// workaround end

		}
		finally {
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
	@Deprecated
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
	@Deprecated
	public RoleGroupListCarrier getRoleGroups(String userID, String authorityID, String [] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

//			Extent ext = pm.getExtent(RoleGroup.class, false);

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
			Collection<RoleGroup> roleGroupsUser = (Collection<RoleGroup>)query.execute(getOrganisationID(), userID, authorityID);

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
			Collection<RoleGroup> roleGroupsUserGroups = (Collection<RoleGroup>)query.execute(getOrganisationID(), userID, authorityID);

			RoleGroupListCarrier rglc = new RoleGroupListCarrier();
			rglc.assigned = pm.detachCopyAll(roleGroupsUser);
			rglc.assignedByUserGroup = pm.detachCopyAll(roleGroupsUserGroups);

			return rglc;
		}
		finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-read"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 **/
	public Collection<UserGroup> getUserGroups(Set<UserID> userGroupIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, userGroupIDs, null, fetchGroups, maxFetchDepth);
		}
		finally {
			pm.close();
		}
	}

	/**
	 * Returns a Collection of {@link User}s corresponding to the given set of {@link UserID}s.
	 * @param userIDs the {@link UserID}s for which to retrieve the {@link User}s
	 * @param fetchGroups the FetchGroups for the detached Users
	 * @param maxFetchDepth the maximum fetch depth of the detached Users.
	 * @return a Collection of {@link User}s corresponding to the given set of {@link UserID}s.
	 * @throws ModuleException a wrapper for many kinds of Exceptions.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<User> getUsers(Set<UserID> userIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, userIDs, null, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-read"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 **/
	public UserGroupIDListCarrier getUserGroupIDs(String userID, String authorityID)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
//			Extent ext = pm.getExtent(UserGroup.class, false);

			Query query = pm.newQuery(
					"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
					"WHERE " +
					"  users.containsKey(paramUserID) " +
					"PARAMETERS String paramUserID " +
			"import java.lang.String");
			Collection<UserGroup> assignedGroups = (Collection<UserGroup>)query.execute(userID);

			//FIXME: JPOX bug reoccured (negation generates INNER JOIN instead of LEFT OUTER JOIN)
//			Query query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
//			"WHERE " +
//			"  !(users.containsKey(paramUserID)) " +
//			"PARAMETERS String paramUserID " +
//			"import java.lang.String");

//			Collection excludedGroups = (Collection)query.execute(userID);

			// workaround start
			query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.UserGroup");
			Collection<UserGroup> allGroups = (Collection<UserGroup>)query.execute();
			Iterator<UserGroup> i = allGroups.iterator();
			Collection<UserGroup> excludedGroups = new HashSet<UserGroup>();
			while(i.hasNext())
			{
				UserGroup userGroup = i.next();
				if(!assignedGroups.contains(userGroup))
					excludedGroups.add(userGroup);
			}
			// workaround end


			UserGroupIDListCarrier uglc = new UserGroupIDListCarrier();

			i = assignedGroups.iterator();
			while(i.hasNext())
				uglc.assigned.add((UserID) JDOHelper.getObjectId(i.next()));

			i = excludedGroups.iterator();
			while(i.hasNext())
				uglc.excluded.add((UserID) JDOHelper.getObjectId(i.next()));

			return uglc;
		}
		finally {
			pm.close();
		}
	}

	// FIXME JPOX bug workaround method. Remove this, once the jpox bug is fixed
	private static Collection<RoleGroup> getRoleGroupsForUserRef(PersistenceManager pm, String organisationID, String userID, String authorityID)
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
		return (Collection<RoleGroup>)query.execute(organisationID, userID, authorityID);
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-read"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 **/
	public RoleGroupIDListCarrier getRoleGroupIDs(String userID, String authorityID)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			Query query;

			// rolegroups via userrefs
//			query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//			"WHERE " +
//			"  this == roleGroupRef.roleGroup &&" +
//			"  roleGroupRef.userRefs.containsValue(userRef) &&" +
//			"  userRef.organisationID == paramOrganisationID &&" +
//			"  userRef.userID == paramUserID &&" +
//			"  userRef.authorityID == paramAuthorityID " +
//			"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef " +
//			"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; " +
//			"import org.nightlabs.jfire.security.UserRef; " +
//			"import java.lang.String");
//			Collection roleGroupsUser = new HashSet((Collection)query.execute(getOrganisationID(), userID, authorityID));
			Collection<RoleGroup> roleGroupsUser = new HashSet<RoleGroup>(getRoleGroupsForUserRef(pm, getOrganisationID(), userID, authorityID));
			// FIXME JPOX bug workarounds. Clean this up, once the jpox bug is fixed

			// rolegroups via usergroups
//			query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//			"WHERE " +
//			"  this == roleGroupRef.roleGroup &&" +
//			"  roleGroupRef.userRefs.containsValue(userGroupRef) &&" +
//			"  userGroupRef.authorityID == paramAuthorityID &&" +
//			"  userGroup == userGroupRef.user &&" +
//			"  userGroup.users.containsValue(user) &&" +
//			"  user.organisationID == paramOrganisationID &&" +
//			"  user.userID == paramUserID " +
//			"VARIABLES RoleGroupRef roleGroupRef; UserGroupRef userGroupRef; UserGroup userGroup; User user " +
//			"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; " +
//			"import org.nightlabs.jfire.security.UserRef; " +
//			"import org.nightlabs.jfire.security.UserGroup; " +
//			"import org.nightlabs.jfire.security.User; " +
//			"import java.lang.String");
//			Collection roleGroupsUserGroups = new HashSet((Collection)query.execute(getOrganisationID(), userID, authorityID));
			Collection<RoleGroup> roleGroupsUserGroups = new HashSet<RoleGroup>();
			pm.getExtent(User.class);
			User user = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID));
			for (Iterator<UserGroup> iter = user.getUserGroups().iterator(); iter.hasNext();)
			{
//				UserGroup o = iter.next();
//				if (o instanceof UserGroup) {
//					UserGroup userGroup = (UserGroup)o;

				UserGroup userGroup = iter.next();
					roleGroupsUserGroups.addAll(
							getRoleGroupsForUserRef(pm, userGroup.getOrganisationID(), userGroup.getUserID(), authorityID));

//				}
//				else {
//					logger.error("entry in user.getUserGroups() is not instanceof UserGroup but is a "+o.getClass());
//				}
			}

			//FIXME: JPOX bug (INNER JOIN instead of LEFT JOIN)
			// excluded rolegroups
//			query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//			"WHERE " +
//			"  this == roleGroupRef.roleGroup &&" +
//			"  !(roleGroupRef.userRefs.containsValue(userRef)) &&" +
//			"  userRef.organisationID == paramOrganisationID &&" +
//			"  userRef.userID == paramUserID &&" +
//			"  roleGroupRef.authorityID == paramAuthorityID " +
//			"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef " +
//			"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");

//			Collection excludedRoleGroups = (Collection)query.execute(getOrganisationID(), userID, authorityID);

			// workaround start
			query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.RoleGroup");
			Collection<RoleGroup> allRoleGroups = (Collection<RoleGroup>)query.execute();
			Iterator<RoleGroup> i = allRoleGroups.iterator();
			Collection<RoleGroup> excludedRoleGroups = new HashSet<RoleGroup>();
			while(i.hasNext())
			{
				RoleGroup o = i.next();
				if((!roleGroupsUser.contains(o)) && (!roleGroupsUserGroups.contains(o)))
					excludedRoleGroups.add(o);
			}
			// workaround end

			Set<RoleGroupID> excludedRoleGroupsIDs = NLJDOHelper.getObjectIDSet(excludedRoleGroups);
			Set<RoleGroupID> roleGroupsUserIDs = NLJDOHelper.getObjectIDSet(roleGroupsUser);
			Set<RoleGroupID> roleGrouopsUserGroupsIDs = NLJDOHelper.getObjectIDSet(roleGroupsUserGroups);
			RoleGroupIDListCarrier rglc = new RoleGroupIDListCarrier(excludedRoleGroupsIDs,	roleGroupsUserIDs, roleGrouopsUserGroupsIDs);

//			i = roleGroupsUser.iterator();
//			while(i.hasNext())
//			rglc.assignedToUser.add(JDOHelper.getObjectId(i.next()));

//			i = roleGroupsUserGroups.iterator();
//			while(i.hasNext())
//			rglc.assignedToUserGroups.add(JDOHelper.getObjectId(i.next()));

//			i = excludedRoleGroups.iterator();
//			while(i.hasNext())
//			rglc.excluded.add(JDOHelper.getObjectId(i.next()));

			return rglc;
		}
		finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-read"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<RoleGroup> getRoleGroups(Set<RoleGroupID> roleGroupIDs, String [] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, roleGroupIDs, null, fetchGroups, maxFetchDepth);
		} finally {
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
	@Deprecated
	public Collection<RoleGroup> getExcludedRoleGroups(String userID, String authorityID)
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
	@Deprecated
	public Collection<RoleGroup> getExcludedRoleGroups(String userID, String authorityID, String [] fetchGroups)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

//			Extent ext = pm.getExtent(RoleGroup.class, false);

			//FIXME: JPOX bug (INNER JOIN instead of LEFT JOIN)
//			Query query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//			"WHERE " +
//			"  this == roleGroupRef.roleGroup &&" +
//			"  !(roleGroupRef.userRefs.containsValue(userRef)) &&" +
//			"  userRef.organisationID == paramOrganisationID &&" +
//			"  userRef.userID == paramUserID &&" +
//			"  roleGroupRef.authorityID == paramAuthorityID " +
//			"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef " +
//			"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");

//			Collection res = (Collection)query.execute(getOrganisationID(), userID, authorityID);

//			Query query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//			"WHERE " +
//			"  this == roleGroupRef.roleGroup &&" +
//			"  ! roleGroupRef.userRefs.containsValue(userRef) &&" +
//			"  userRef.authorityID == paramAuthorityID &&" +
//			"  userRef.user == user &&" +
//			"  user.organisationID == paramOrganisationID &&" +
//			"  user.userID == paramUserID " +
//			"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef; User user " +
//			"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
//			Collection res = (Collection)query.execute(getOrganisationID(), userID, authorityID);

//			return pm.detachCopyAll(res);

			// workaround start
			Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.RoleGroup");
			Collection<RoleGroup> c = (Collection<RoleGroup>)query.execute();
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
			Collection<RoleGroup> roleGroupsUser = (Collection<RoleGroup>)query.execute(getOrganisationID(), userID, authorityID);
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
			Collection<RoleGroup> roleGroupsUserGroups = (Collection<RoleGroup>)query.execute(getOrganisationID(), userID, authorityID);

			Iterator<RoleGroup> i = c.iterator();
			Collection<RoleGroup> c2 = new HashSet<RoleGroup>();
			while(i.hasNext())
			{
				RoleGroup o = i.next();
				if((!roleGroupsUser.contains(o)) && (!roleGroupsUserGroups.contains(o)))
					c2.add(o);
			}
			return pm.detachCopyAll(c2);
			// workaround end

		}
		finally {
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
	@Deprecated
	public Collection<UserGroup> getUserGroups(String userID)
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
	@Deprecated
	public Collection<UserGroup> getUserGroups(String userID, String [] fetchGroups)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

//			Extent ext = pm.getExtent(UserGroup.class, false);
			Query query = pm.newQuery(
					"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
					"WHERE " +
					"  users.containsKey(paramUserID) " +
					"PARAMETERS String paramUserID " +
			"import java.lang.String");

			Collection<UserGroup> c = (Collection<UserGroup>)query.execute(userID);
			return pm.detachCopyAll(c);
		}
		finally {
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
	@Deprecated
	public Collection<UserGroup> getExcludedUserGroups(String userID)
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
	@Deprecated
	public Collection<UserGroup> getExcludedUserGroups(String userID, String [] fetchGroups)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			//FIXME: JPOX bug reoccured (negation generates INNER JOIN instead of LEFT OUTER JOIN)
//			Query query = pm.newQuery(
//			"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
//			"WHERE " +
//			"  !(users.containsKey(paramUserID)) " +
//			"PARAMETERS String paramUserID " +
//			"import java.lang.String");

//			Collection c = (Collection)query.execute(userID);


			// workaround start
			Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.UserGroup");
			Collection<UserGroup> c = (Collection<UserGroup>)query.execute();
			query = pm.newQuery(
					"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
					"WHERE " +
					"  users.containsKey(paramUserID) " +
					"PARAMETERS String paramUserID " +
			"import java.lang.String");

			Collection<UserGroup> includedUsers = (Collection<UserGroup>)query.execute(userID);
			Iterator<UserGroup> i = c.iterator();
			Collection<UserGroup> c2 = new HashSet<UserGroup>();
			while(i.hasNext())
			{
				UserGroup o = i.next();
				if(!includedUsers.contains(o))
					c2.add(o);
			}
			return pm.detachCopyAll(c2);
			// workaround end

			//      return pm.detachCopyAll(c);

		}
		finally {
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
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			Object test = pm.getObjectById(userID, true);
			logger.debug("userIDAlreadyRegistered(\"" + userID + "\") = " + (test != null));
			return (test != null);
		}
		catch(JDOObjectNotFoundException e) {
			logger.debug("userIDAlreadyRegistered(\"" + userID + "\") = false");
			return false;
		}
		finally {
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
	{
		PersistenceManager pm = this.getPersistenceManager();
		try
		{
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			//FIXME: JPOX issue with detached Collections (always attached, problem with dirty bit)
			//FIXME: JPOX generates invalid SQL when using this workaround
//			pm.getFetchPlan().addGroup(User.FETCH_GROUP_USERREFS);
			// workaround end

			pm.getExtent(User.class, true);
			Object o = pm.getObjectById(userID,true);
			User usr = (User)pm.detachCopy(o);

			// FIXME: JPOX => load-fetch-group does not work
			// this workaround makes User.person always dirty before it can be modified by the client (person is
			// always attached when saving user!)
//			try
//			{
//			Person person = usr.getPerson();
//			if(person != null)
//			{
//			pm.getFetchPlan().setGroups(new String[] {FetchPlan.ALL});
//			pm.getExtent(User.class, true);
//			Object p = pm.getObjectById(PersonID.create(getOrganisationID(), person.getPersonID()),true);
//			usr.setPerson((Person)pm.detachCopy(p));
//			}
//			}
//			catch(JDODetachedFieldAccessException e2)
//			{
//			// do nothing
//			}
			// workaround end

			return usr;
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @param userID The ID of the user to be returned.
//	 * @param includeMask What linked objects shall be included in the transient result. See User.INCLUDE* for details.
//	 * @return Returns the User with the given userID. If this user does not exist,
//	 *			a UserNotFoundException is thrown.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="UserManager-read"
//	 * @deprecated
//	 **/
//	public User getUser(String userID, int includeMask)
//	throws SecurityException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				pm.getExtent(User.class, true);
//				try {
//					User user = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
//					//					user = (User)pm.detachCopy(user);
////					user.makeTransient(includeMask);
////					user.setPassword(null);
//					return user;
//				} catch (JDOObjectNotFoundException x) {
//					throw new UserNotFoundException("User \""+userID+"\" not found at organisation \""+getOrganisationID()+"\"!");
//				}
//			} finally {
//				if (AuthorityManagerBean.CLOSE_PM) pm.close();
//			}
//		} catch (SecurityException x) {
//			throw x;
//		} catch (Exception x) {
//			throw new SecurityException(x);
//		}
//	}


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
	public void addUserToRoleGroups(String userID, String authorityID, Collection<String> roleGroupIDs)
	{
		Iterator<String> i = roleGroupIDs.iterator();
		while(i.hasNext())
		{
			String o = i.next();
//			if(o instanceof String)
			addUserToRoleGroup(userID, authorityID, o);
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
	{
		if (User.USERID_SYSTEM.equals(userID))
			throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USERID_SYSTEM + "\"!");

//		logger.info("********* addUserToRoleGroup");

		PersistenceManager pm = getPersistenceManager();
		try {
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
			JFireServerManager jfsm = getJFireServerManager();
			try {
				jfsm.jfireSecurity_flushCache(userID);
			} finally {
				jfsm.close();
			}
		} finally {
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
	public void addUsersToUserGroup(String userGroupID, Collection<String> userIDs)
	throws SecurityException
	{
		Iterator<String> i = userIDs.iterator();
		while(i.hasNext())
		{
			String o = i.next();
//			if(o instanceof String)
			addUserToUserGroup(o, userGroupID);
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
	public void addUserToUserGroups(String userID, Collection<String> userGroupIDs)
	throws SecurityException
	{
		Iterator<String> i = userGroupIDs.iterator();
		while(i.hasNext())
		{
			String o = i.next();
//			if(o instanceof String)
			addUserToUserGroup(userID, o);
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


				JFireServerManager jfsm = getJFireServerManager();
				try {
					jfsm.jfireSecurity_flushCache(userID);
				} finally {
					jfsm.close();
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
	 * Revoke rolegroups from a user (should be renamed)
	 * @param userID id of the user
	 * @param authorityID id of the authority for which the rolegroups are revoked
	 * @param roleGroupIDs Collection of role group IDs
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="UserManager-write"
	 **/
	public void removeUserFromRoleGroups(String userID, String authorityID, Collection<String> roleGroupIDs)
	{
		Iterator<String> i = roleGroupIDs.iterator();
		while(i.hasNext())
		{
			String o = i.next();
//			if(o instanceof String)
			removeUserFromRoleGroup(userID, authorityID, o);
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
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			UserRef uref;
			try {
				uref = (UserRef)pm.getObjectById(UserRefID.create(authorityID, getOrganisationID(), userID), true);
			} catch (JDOObjectNotFoundException ignore) {
				return;
			}

			uref.removeRoleGroupRef(roleGroupID);

			JFireServerManager jfsm = getJFireServerManager();
			try {
				jfsm.jfireSecurity_flushCache(userID);
			} finally {
				jfsm.close();
			}
		} finally {
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
	public void removeUsersFromUserGroup(String userGroupID, Collection<String> userIDs)
	{
		Iterator<String> i = userIDs.iterator();
		while(i.hasNext())
		{
			String o = i.next();
//			if(o instanceof String)
			removeUserFromUserGroup(o, userGroupID);
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
	public void removeUserFromUserGroups(String userID, Collection<String> userGroupIDs)
	{
		Iterator<String> i = userGroupIDs.iterator();
		while(i.hasNext())
		{
			String o = i.next();
//			if(o instanceof String)
			removeUserFromUserGroup(userID, o);
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
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
//			pm.getExtent(User.class, true);
//			User user = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
			pm.getExtent(UserGroup.class, true);
			UserGroup userGroup = (UserGroup) pm.getObjectById(UserID.create(getOrganisationID(), userGroupID), true);
			userGroup.removeUser(userID);

			JFireServerManager jfsm = getJFireServerManager();
			try {
				jfsm.jfireSecurity_flushCache(userID);
			} finally {
				jfsm.close();
			}
		}
		finally {
			pm.close();
		}
	}


//	/**
//	 * Assign a person to a user
//	 * @param userID the user ID
//	 * @param personID the person ID
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="UserManager-write"
//	 **/
//	public void assignPersonToUser(String userID, long personID)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try
//		{
//			pm.getExtent(User.class, true);
//			User usr = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
//
//			pm.getExtent(Person.class, true);
//			Person ps = (Person) pm.getObjectById(PropertySetID.create(getOrganisationID(), personID), true);
//
//			usr.setPerson(ps);
//		}
//		finally {
//			pm.close();
//		}
//	}


	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void whoami()
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
	//	 * @ejb.transaction type="Required"
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

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<UserID> getUserIDs(QueryCollection<User, ? extends UserQuery> userQueries) {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<User, UserQuery> decoratedCollection;
			if (userQueries instanceof JDOQueryCollectionDecorator)
			{
				decoratedCollection = (JDOQueryCollectionDecorator<User, UserQuery>) userQueries;
			}
			else
			{
				decoratedCollection = new JDOQueryCollectionDecorator<User, UserQuery>(userQueries);
			}

			decoratedCollection.setPersistenceManager(pm);
			Collection<User> users = decoratedCollection.executeQueries();

			return NLJDOHelper.getObjectIDSet(users);
		} finally {
			pm.close();
		}
	}

	/**
	 * Sets the password of the user that is calling this method to the given password.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public void setUserPassword(String password) {
		String userID = SecurityReflector.getUserDescriptor().getUserID();
		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();

		PersistenceManager pm = getPersistenceManager();
		try {
			User user = (User) pm.getObjectById(UserID.create(organisationID, userID));
			user.getUserLocal().setPasswordPlain(password);
		} finally {
			pm.close();
		}
	}

}
