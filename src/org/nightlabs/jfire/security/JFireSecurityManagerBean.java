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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserRefID;
import org.nightlabs.jfire.security.search.UserQuery;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.Util;

/**
 * @author Alexander Bieber <alex@nightlabs.de>
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marco Schulze <marco@nightlabs.de>
 *
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/JFireSecurityManager"
 *		jndi-name="jfire/ejb/JFireBaseBean/JFireSecurityManager"
 *		type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class JFireSecurityManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireSecurityManagerBean.class);

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
	 * @!!!ejb.permission role-name="JFireSecurityManager-read"
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
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
	 * @ejb.permission role-name="JFireSecurityManager-read"
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
				pm.close();
			}
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @param userType one of User.USERTYPE* or <code>null</code> to get all
	 * @param organisationID an organisationID in order to filter for it or <code>null</code> to get all. 
	 * @return the unique IDs of those users that match the given criteria.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 */
	@SuppressWarnings("unchecked")
	public Set<UserID> getUserIDs(String organisationID, Set<String> userTypes)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Set<UserID> result = null;

			Query query = pm.newQuery(pm.getExtent(User.class, true));
			query.setResult("JDOHelper.getObjectId(this)");

			StringBuffer filter = new StringBuffer();

			if (userTypes != null && !userTypes.isEmpty())
				filter.append("this.userType == :userType");

			if (organisationID != null) {
				if (filter.length() > 0)
					filter.append(" && ");

				filter.append("this.organisationID == :organisationID");
			}

			query.setFilter(filter.toString());

			HashMap<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", organisationID);

			if (userTypes == null || userTypes.isEmpty())
				result = new HashSet<UserID>((Collection<? extends UserID>) query.executeWithMap(params));
			else {
				for (String userType : userTypes) {
					params.put("userType", userType);
					Collection<? extends UserID> c = (Collection<? extends UserID>) query.executeWithMap(params);
					if (result == null)
						result = new HashSet<UserID>(c);
					else
						result.addAll(c);
				}
			}

			return result;
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @deprecated Use getUserIDsByType(...) and getUsers(...) instead
//	 * Calls {@link #getUsersByType(String, String[])} with only the default-fetch-group
//	 *
//	 * @param userType one of User.USERTYPE*
//	 * @return
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 */
//	@Deprecated
//	public Collection<User> getUsersByType(String userType)
//	throws ModuleException
//	{
//		return getUsersByType(userType,null);
//	}
//
//	/**
//	 * @deprecated Use getUserIDsByType(...) and getUsers(...) instead
//	 * @throws ModuleException
//	 * @see User.USERTYPE_ORGANISATION
//	 * @see User.USERTYPE_USER
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public Collection<User> getUsersByType(String userType, String [] fetchGroups)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try
//		{
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			Query query = pm.newQuery(pm.getExtent(User.class, true));
//			query.declareImports("import java.lang.String");
//			query.declareParameters("String userType, String systemUserID, String otherUserID");
//			query.setFilter("this.userType == userType && this.userID != systemUserID && this.userID != otherUserID");
//			query.setOrdering("this.userID ascending");
//			Collection<User> c = (Collection<User>)query.execute(userType, User.USERID_SYSTEM, User.USERID_OTHER);
//			return pm.detachCopyAll(c);
//		}
//		finally {
//			pm.close();
//		}
//	}

//	/**
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	public Collection<User> getAllUsers()
//	throws ModuleException
//	{
//		return getAllUsers(null);
//	}

//	/**
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	public Collection<User> getAllUsers(String [] fetchGroups)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try
//		{
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			Query query = pm.newQuery(pm.getExtent(User.class, true));
////			query.declareImports("import java.lang.String");
////			query.declareParameters("String systemUserID");
////			query.setFilter("this.userID != systemUserID");
////			query.setOrdering("this.userID ascending");
//			Collection<User> c = (Collection<User>)query.execute(); // User.USERID_SYSTEM);
//			return pm.detachCopyAll(c);
//		}
//		finally {
//			pm.close();
//		}
//	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
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
	 * @ejb.permission role-name="JFireSecurityManager-read"
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
	 * @ejb.permission role-name="JFireSecurityManager-read"
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
	 * @ejb.permission role-name="JFireSecurityManager-read"
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
	 * @ejb.permission role-name="JFireSecurityManager-read"
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
	 * @ejb.permission role-name="JFireSecurityManager-read"
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

//	/**
//	 * @deprecated Use getRoleGroupIDs(...) and getRoleGroups(...) instead
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public RoleGroupSetCarrier getRoleGroups(String userID, String authorityID)
//	throws ModuleException
//	{
//		return getRoleGroups(userID, authorityID, null);
//	}
//
//	/**
//	 * @deprecated Use getRoleGroupIDs(...) and getRoleGroups(...) instead
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public RoleGroupSetCarrier getRoleGroups(String userID, String authorityID, String [] fetchGroups)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try
//		{
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
////			Extent ext = pm.getExtent(RoleGroup.class, false);
//
//			// rolegroups via userrefs
//			Query query = pm.newQuery(
//					"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//					"WHERE " +
//					"  this == roleGroupRef.roleGroup &&" +
//					"  roleGroupRef.userRefs.containsValue(userRef) &&" +
//					"  userRef.authorityID == paramAuthorityID &&" +
//					"  userRef.user == user &&" +
//					"  user.organisationID == paramOrganisationID &&" +
//					"  user.userID == paramUserID " +
//					"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef; User user " +
//					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import org.nightlabs.jfire.security.User; import java.lang.String");
//			Collection<RoleGroup> roleGroupsUser = (Collection<RoleGroup>)query.execute(getOrganisationID(), userID, authorityID);
//
//			// rolegroups via usergroups
//			query = pm.newQuery(
//					"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//					"WHERE " +
//					"  this == roleGroupRef.roleGroup &&" +
//					"  roleGroupRef.userRefs.containsValue(userGroupRef) &&" +
//					"  userGroupRef.user == userGroup &&" +
//					"  userGroupRef.authorityID == paramAuthorityID &&" +
//					"  userGroup.users.containsValue(user) &&" +
//					"  user.organisationID == paramOrganisationID &&" +
//					"  user.userID == paramUserID " +
//					"VARIABLES RoleGroupRef roleGroupRef; UserGroupRef userGroupRef; UserGroup userGroup; User user " +
//					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
//			Collection<RoleGroup> roleGroupsUserGroups = (Collection<RoleGroup>)query.execute(getOrganisationID(), userID, authorityID);
//
//			RoleGroupSetCarrier rglc = new RoleGroupSetCarrier();
//			rglc.assigned = pm.detachCopyAll(roleGroupsUser);
//			rglc.assignedByUserGroup = pm.detachCopyAll(roleGroupsUserGroups);
//
//			return rglc;
//		}
//		finally {
//			pm.close();
//		}
//	}

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 **/
//	public Collection<UserGroup> getUserGroups(Collection<UserID> userGroupIDs, String[] fetchGroups, int maxFetchDepth)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			return NLJDOHelper.getDetachedObjectList(pm, userGroupIDs, null, fetchGroups, maxFetchDepth);
//		}
//		finally {
//			pm.close();
//		}
//	}

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
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<User> getUsers(Collection<UserID> userIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, userIDs, User.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
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

	@SuppressWarnings("unchecked")
	private static Collection<RoleGroup> getRoleGroupsForUserRef(PersistenceManager pm, User user, Authority authority)
	{
		if (!Util.equals(authority.getOrganisationID(), user.getOrganisationID()))
			throw new IllegalArgumentException("user.organisationID != authority.organisationID");

		String _organisationID = user.getOrganisationID();
		String _userID = user.getUserID();
		String _authorityID = authority.getAuthorityID();

		Query query = pm.newQuery(
				"SELECT FROM org.nightlabs.jfire.security.RoleGroup \n" +
				"WHERE \n" +
				"  this == roleGroupRef.roleGroup && \n" +
				"  roleGroupRef.userRefs.containsValue(userRef) && \n" +
				"  userRef.organisationID == paramOrganisationID && \n" +
				"  userRef.userID == paramUserID && \n" +
				"  userRef.authorityID == paramAuthorityID \n" +
				"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef \n" +
				"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID \n" +
				"import org.nightlabs.jfire.security.RoleGroupRef; \n" +
				"import org.nightlabs.jfire.security.UserRef; \n" +
				"import java.lang.String");
		return (Collection<RoleGroup>)query.execute(_organisationID, _userID, _authorityID);
	}

	/**
	 * Get all those {@link UserGroupRef}s within the given <code>authority</code> that
	 * belong to a {@link UserGroup} where the given <code>user</code> is a member.
	 *
	 * @param pm the gate to our datastore.
	 * @param user the {@link User} for which to query the membership of its {@link UserGroup}s.
	 * @param authority the {@link Authority} in which we query.
	 */
	@SuppressWarnings("unchecked")
	private static Collection<UserGroupRef> getUserGroupRefsForUserWithinAuthority(PersistenceManager pm, User user, Authority authority)
	{
		if (!Util.equals(authority.getOrganisationID(), user.getOrganisationID()))
			throw new IllegalArgumentException("user.organisationID != authority.organisationID");

		Query q = pm.newQuery(UserGroupRef.class);
		q.setFilter(":authority.userRefs.containsValue(this) && :user.userGroups.containsValue(this.user)");
		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("user", user);
		params.put("authority", authority);
		return (Collection<UserGroupRef>) q.executeWithMap(params);
	}

	private static RoleGroupIDSetCarrier getRoleGroupIDSetCarrier(PersistenceManager pm, User user, Authority authority)
	{
		String organisationID = user.getOrganisationID();
		if (!organisationID.equals(authority.getOrganisationID()))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authority.organisationID=\""+authority.getOrganisationID()+"\" does not match user.organisationID=\""+user.getOrganisationID()+"\"!");

		UserID userID = (UserID)JDOHelper.getObjectId(user);
		if (userID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(user) returned null!");

		boolean userIsInAuthority = authority.getUserRef(userID) != null;
		boolean controlledByOtherUser = !userIsInAuthority;
		if (controlledByOtherUser) {
			// find out if there is a UserGroup in which the user is member and which is within this authority
			controlledByOtherUser = getUserGroupRefsForUserWithinAuthority(pm, user, authority).isEmpty();
		}

		Collection<RoleGroup> roleGroupsUser = getRoleGroupsForUserRef(pm, user, authority);
		Collection<RoleGroup> roleGroupsUserGroups = new HashSet<RoleGroup>();
		for (UserGroup userGroup : user.getUserGroups()) {
			roleGroupsUserGroups.addAll(getRoleGroupsForUserRef(pm, userGroup, authority));
		}

		Collection<RoleGroup> allRoleGroups = authority.getAuthorityType().getRoleGroups();
		Set<RoleGroupID> allRoleGroupIDs = NLJDOHelper.getObjectIDSet(allRoleGroups);
		Set<RoleGroupID> roleGroupIDsUser = NLJDOHelper.getObjectIDSet(roleGroupsUser);
		Set<RoleGroupID> roleGroupIDsUserGroups = NLJDOHelper.getObjectIDSet(roleGroupsUserGroups);
		Set<RoleGroupID> roleGroupIDsOtherUser;

		if (controlledByOtherUser) {
			User otherUser = (User) pm.getObjectById(UserID.create(organisationID, User.USERID_OTHER));
			Collection<RoleGroup> roleGroupsOtherUser = getRoleGroupsForUserRef(pm, otherUser, authority);
			roleGroupIDsOtherUser = NLJDOHelper.getObjectIDSet(roleGroupsOtherUser);
		}
		else
			roleGroupIDsOtherUser = new HashSet<RoleGroupID>(0);

		return new RoleGroupIDSetCarrier(
				userID,
				(AuthorityID)JDOHelper.getObjectId(authority),
				allRoleGroupIDs,
				roleGroupIDsUser,
				roleGroupIDsUserGroups,
				roleGroupIDsOtherUser,
				userIsInAuthority,
				controlledByOtherUser);
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!role-assigned(Marco, 2008-05-05): _Guest_ is ok. We check inside by code, whether the user can read this data or not.
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 **/
	public RoleGroupIDSetCarrier getRoleGroupIDSetCarrier(UserID userID, AuthorityID authorityID)
	{
		String organisationID = getOrganisationID();
		if (!organisationID.equals(userID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! userID.organisationID=\""+userID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
		if (!organisationID.equals(authorityID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

		PersistenceManager pm = getPersistenceManager();
		try
		{
			User user = (User) pm.getObjectById(userID);
			Authority authority = (Authority) pm.getObjectById(authorityID);

			boolean allowed = authority.resolveSecuringAuthority().containsRoleRef(getPrincipal(), RoleConstants.securityManager_getRoleGroupIDSetCarrier);
			// Not allowed means that the authority which controls the access rights for the given authority
			// (which might be the global authority) does not grant the user the necessary right. In this
			// case, we check if the user is asking about himself, which is allowed.
			if (!allowed) {
				if (getUserID().equals(userID.userID)) // the user asks about himself - this is allowed
					allowed = true;
			}

			if (!allowed) {
				if (user instanceof UserGroup) { // since the user does not ask about himself, we check if he asks about a group in which he's a member
					UserGroup userGroup = (UserGroup) user;
					if (userGroup.getUser(userID.userID) != null)
						allowed = true;
				}
			}

			if (!allowed)
				throw new SecurityException("The current user \""+ getPrincipalString() +"\" misses the access right " + RoleConstants.securityManager_getRoleGroupIDSetCarrier + " and does not ask data about himself.");

			return getRoleGroupIDSetCarrier(pm, user, authority);
		}
		finally {
			pm.close();
		}
	}

	/**
	 * @param authorityID identifier of the {@link Authority} for which to query the access rights configuration
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!role-assigned(Marco, 2008-05-05): _Guest_ is ok. We check inside by code, whether the user can read this data or not.
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 **/
	@SuppressWarnings("unchecked")
	public List<RoleGroupIDSetCarrier> getRoleGroupIDSetCarriers(AuthorityID authorityID)
	{
		String organisationID = getOrganisationID();
		if (!organisationID.equals(authorityID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

		PersistenceManager pm = getPersistenceManager();
		try {
			Authority authority = (Authority) pm.getObjectById(authorityID);

			boolean allowed = authority.resolveSecuringAuthority().containsRoleRef(getPrincipal(), RoleConstants.securityManager_getRoleGroupIDSetCarrier);
			if (!allowed)
				throw new SecurityException("The current user \""+ getPrincipalString() +"\" misses the access right " + RoleConstants.securityManager_getRoleGroupIDSetCarrier + " and does not ask data about himself.");

			Query q = pm.newQuery(User.class);
			q.setFilter("this.organisationID = :organisationID");
			Collection<User> users = (Collection<User>) q.execute(organisationID);
			List<RoleGroupIDSetCarrier> result = new ArrayList<RoleGroupIDSetCarrier>(users.size());
			for (User user : users) {
				RoleGroupIDSetCarrier roleGroupIDSetCarrier = getRoleGroupIDSetCarrier(pm, user, authority);
				result.add(roleGroupIDSetCarrier);
			}

			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!role-assigned(Marco, 2008-05-05): _Guest_ is ok. Which RoleGroups exist and what their properties are is no secret.
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<RoleGroup> getRoleGroups(Collection<RoleGroupID> roleGroupIDs, String [] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, roleGroupIDs, RoleGroup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<AuthorityID> getAuthorityIDs(AuthorityTypeID authorityTypeID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Authority.class);
			q.setResult("JDOHelper.getObjectId(this)");
			AuthorityType authorityType = null; // TODO remove this workaround line!
			if (authorityTypeID != null) {
//				q.setFilter("JDOHelper.getObjectId(this.authorityType) == :authorityTypeID");
				// TODO DataNucleus WORKAROUND
				authorityType = (AuthorityType) pm.getObjectById(authorityTypeID);
				q.setFilter("this.authorityType == :authorityType");
			}

//			return new HashSet<AuthorityID>((Collection<? extends AuthorityID>) q.execute(authorityTypeID));
			return new HashSet<AuthorityID>((Collection<? extends AuthorityID>) q.execute(authorityType));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public List<Authority> getAuthorities(Collection<AuthorityID> authorityIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, authorityIDs, Authority.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public List<AuthorityType> getAuthorityTypes(Collection<AuthorityTypeID> authorityTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, authorityTypeIDs, AuthorityType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}


//	/**
//	 * @deprecated Use getRoleGroupIDs(...) and getRoleGroups(...) instead
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public Collection<RoleGroup> getExcludedRoleGroups(String userID, String authorityID)
//	{
//		return getExcludedRoleGroups(userID, authorityID, null);
//	}
//	/**
//	 * @deprecated Use getRoleGroupIDs(...) and getRoleGroups(...) instead
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public Collection<RoleGroup> getExcludedRoleGroups(String userID, String authorityID, String [] fetchGroups)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try
//		{
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
////			Extent ext = pm.getExtent(RoleGroup.class, false);
//
//			//FIXME: JPOX bug (INNER JOIN instead of LEFT JOIN)
////			Query query = pm.newQuery(
////			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
////			"WHERE " +
////			"  this == roleGroupRef.roleGroup &&" +
////			"  !(roleGroupRef.userRefs.containsValue(userRef)) &&" +
////			"  userRef.organisationID == paramOrganisationID &&" +
////			"  userRef.userID == paramUserID &&" +
////			"  roleGroupRef.authorityID == paramAuthorityID " +
////			"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef " +
////			"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
////			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
//
////			Collection res = (Collection)query.execute(getOrganisationID(), userID, authorityID);
//
////			Query query = pm.newQuery(
////			"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
////			"WHERE " +
////			"  this == roleGroupRef.roleGroup &&" +
////			"  ! roleGroupRef.userRefs.containsValue(userRef) &&" +
////			"  userRef.authorityID == paramAuthorityID &&" +
////			"  userRef.user == user &&" +
////			"  user.organisationID == paramOrganisationID &&" +
////			"  user.userID == paramUserID " +
////			"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef; User user " +
////			"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
////			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
////			Collection res = (Collection)query.execute(getOrganisationID(), userID, authorityID);
//
////			return pm.detachCopyAll(res);
//
//			// workaround start
//			Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.RoleGroup");
//			Collection<RoleGroup> c = (Collection<RoleGroup>)query.execute();
//			query = pm.newQuery(
//					"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//					"WHERE " +
//					"  this == roleGroupRef.roleGroup &&" +
//					"  roleGroupRef.userRefs.containsValue(userRef) &&" +
//					"  userRef.authorityID == paramAuthorityID &&" +
//					"  userRef.user == user &&" +
//					"  user.organisationID == paramOrganisationID &&" +
//					"  user.userID == paramUserID " +
//					"VARIABLES RoleGroupRef roleGroupRef; UserRef userRef; User user " +
//					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import org.nightlabs.jfire.security.User; import java.lang.String");
//			Collection<RoleGroup> roleGroupsUser = (Collection<RoleGroup>)query.execute(getOrganisationID(), userID, authorityID);
//			query = pm.newQuery(
//					"SELECT FROM org.nightlabs.jfire.security.RoleGroup " +
//					"WHERE " +
//					"  this == roleGroupRef.roleGroup &&" +
//					"  roleGroupRef.userRefs.containsValue(userGroupRef) &&" +
//					"  userGroupRef.user == userGroup &&" +
//					"  userGroupRef.authorityID == paramAuthorityID &&" +
//					"  userGroup.users.containsValue(user) &&" +
//					"  user.organisationID == paramOrganisationID &&" +
//					"  user.userID == paramUserID " +
//					"VARIABLES RoleGroupRef roleGroupRef; UserGroupRef userGroupRef; UserGroup userGroup; User user " +
//					"PARAMETERS String paramOrganisationID, String paramUserID, String paramAuthorityID " +
//			"import org.nightlabs.jfire.security.RoleGroupRef; import org.nightlabs.jfire.security.UserRef; import java.lang.String");
//			Collection<RoleGroup> roleGroupsUserGroups = (Collection<RoleGroup>)query.execute(getOrganisationID(), userID, authorityID);
//
//			Iterator<RoleGroup> i = c.iterator();
//			Collection<RoleGroup> c2 = new HashSet<RoleGroup>();
//			while(i.hasNext())
//			{
//				RoleGroup o = i.next();
//				if((!roleGroupsUser.contains(o)) && (!roleGroupsUserGroups.contains(o)))
//					c2.add(o);
//			}
//			return pm.detachCopyAll(c2);
//			// workaround end
//
//		}
//		finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * @deprecated Use getUserGroupIDs(...) and getUserGroups(...) instead
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public Collection<UserGroup> getUserGroups(String userID)
//	{
//		return getUserGroups(userID, null);
//	}
//
//	/**
//	 * @deprecated Use getUserGroupIDs(...) and getUserGroups(...) instead
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public Collection<UserGroup> getUserGroups(String userID, String [] fetchGroups)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try
//		{
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
////			Extent ext = pm.getExtent(UserGroup.class, false);
//			Query query = pm.newQuery(
//					"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
//					"WHERE " +
//					"  users.containsKey(paramUserID) " +
//					"PARAMETERS String paramUserID " +
//			"import java.lang.String");
//
//			Collection<UserGroup> c = (Collection<UserGroup>)query.execute(userID);
//			return pm.detachCopyAll(c);
//		}
//		finally {
//			pm.close();
//		}
//	}
//
//
//	/**
//	 * @deprecated Use getUserGroupIDs(...) and getUserGroups(...) instead
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public Collection<UserGroup> getExcludedUserGroups(String userID)
//	{
//		return getExcludedUserGroups(userID, null);
//	}
//
//	/**
//	 * @deprecated Use getUserGroupIDs(...) and getUserGroups(...) instead
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-read"
//	 **/
//	@Deprecated
//	public Collection<UserGroup> getExcludedUserGroups(String userID, String [] fetchGroups)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try
//		{
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			//FIXME: JPOX bug reoccured (negation generates INNER JOIN instead of LEFT OUTER JOIN)
////			Query query = pm.newQuery(
////			"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
////			"WHERE " +
////			"  !(users.containsKey(paramUserID)) " +
////			"PARAMETERS String paramUserID " +
////			"import java.lang.String");
//
////			Collection c = (Collection)query.execute(userID);
//
//
//			// workaround start
//			Query query = pm.newQuery("SELECT FROM org.nightlabs.jfire.security.UserGroup");
//			Collection<UserGroup> c = (Collection<UserGroup>)query.execute();
//			query = pm.newQuery(
//					"SELECT FROM org.nightlabs.jfire.security.UserGroup " +
//					"WHERE " +
//					"  users.containsKey(paramUserID) " +
//					"PARAMETERS String paramUserID " +
//			"import java.lang.String");
//
//			Collection<UserGroup> includedUsers = (Collection<UserGroup>)query.execute(userID);
//			Iterator<UserGroup> i = c.iterator();
//			Collection<UserGroup> c2 = new HashSet<UserGroup>();
//			while(i.hasNext())
//			{
//				UserGroup o = i.next();
//				if(!includedUsers.contains(o))
//					c2.add(o);
//			}
//			return pm.detachCopyAll(c2);
//			// workaround end
//
//			//      return pm.detachCopyAll(c);
//
//		}
//		finally {
//			pm.close();
//		}
//	}


	/**
	 * Check if a user ID exists. Needs role "JFireSecurityManager-write"; used to check ID while creating new user
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
	 * @ejb.permission role-name="JFireSecurityManager-read"
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
//	 * @ejb.permission role-name="JFireSecurityManager-read"
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
//				if (JFireSecurityManagerBean.CLOSE_PM) pm.close();
//			}
//		} catch (SecurityException x) {
//			throw x;
//		} catch (Exception x) {
//			throw new SecurityException(x);
//		}
//	}


//	/**
//	 * Assign rolegroups to a user (should be renamed)
//	 * @param userID id of the user
//	 * @param authorityID id of the authority for which to get the rolegroups
//	 * @param roleGroupIDs Collection of rolegroup IDs
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-write"
//	 **/
//	public void addUserToRoleGroups(UserID userID, AuthorityID authorityID, Collection<String> roleGroupIDs)
//	{
//		String organisationID = getOrganisationID();
//		if (!organisationID.equals(userID.organisationID))
//			throw new IllegalArgumentException("Cannot manage foreign access rights! userID.organisationID=\""+userID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
//		if (!organisationID.equals(authorityID.organisationID))
//			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
//		
//		Iterator<String> i = roleGroupIDs.iterator();
//		while(i.hasNext())
//		{
//			String o = i.next();
////			if(o instanceof String)
//			addUserToRoleGroup(userID, authorityID, o);
//		}
//	}

	/**
	 * Assign a {@link RoleGroup} to a {@link User} within the scope of an {@link Authority} (hence in fact a {@link RoleGroupRef} is assigned to a {@link UserRef}).
	 *
	 * @param userID id of the user
	 * @param authorityID id of the authority the user gets the rolegroup for
	 * @param roleGroupID ID of the rolegroup
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.security.JFireSecurityManager#addRoleGroupToUser"
	 * @!role-assigned(Marco, 2008-05-05)
	 **/
	public void addRoleGroupToUser(UserID userID, AuthorityID authorityID, RoleGroupID roleGroupID)
	{
		String organisationID = getOrganisationID();
		if (!organisationID.equals(userID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! userID.organisationID=\""+userID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
		if (!organisationID.equals(authorityID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

		if (User.USERID_SYSTEM.equals(userID))
			throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USERID_SYSTEM + "\"!");

		PersistenceManager pm = getPersistenceManager();
		try {
			Authority authority = (Authority)pm.getObjectById(authorityID);

			authority.getSecuringAuthority().assertContainsRoleRef(getPrincipal(), RoleConstants.securityManager_addRoleGroupToUser);

			User user = (User)pm.getObjectById(userID);
			RoleGroup roleGroup = (RoleGroup)pm.getObjectById(roleGroupID);

			RoleGroupRef rgf = authority.createRoleGroupRef(roleGroup);
			UserRef ur = authority.createUserRef(user);
			ur.addRoleGroupRef(rgf);

			JFireServerManager jfsm = getJFireServerManager();
			try {
				if (user instanceof UserGroup) {
					for (User member : ((UserGroup)user).getUsers())
						jfsm.jfireSecurity_flushCache(member.getUserID());
				}
				else
					jfsm.jfireSecurity_flushCache(userID.userID);
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
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
				pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

//	/**
//	 * Revoke rolegroups from a user (should be renamed)
//	 * @param userID id of the user
//	 * @param authorityID id of the authority for which the rolegroups are revoked
//	 * @param roleGroupIDs Collection of role group IDs
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="JFireSecurityManager-write"
//	 **/
//	public void removeUserFromRoleGroups(String userID, String authorityID, Collection<String> roleGroupIDs)
//	{
//		Iterator<String> i = roleGroupIDs.iterator();
//		while(i.hasNext())
//		{
//			String o = i.next();
////			if(o instanceof String)
//			removeUserFromRoleGroup(userID, authorityID, o);
//		}
//	}

	/**
	 * Revoke {@link RoleGroup} from a {@link User} within the scope of an {@link Authority} (hence a {@link RoleGroupRef} is in fact removed from a {@link UserRef}).
	 *
	 * @param userID the id of the user
	 * @param authorityID the id of the authority for which the rolegroup is revoked
	 * @param roleGroupID the rolegroup id
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.security.JFireSecurityManager#removeRoleGroupFromUser"
	 * @!role-assigned(Marco, 2008-05-05)
	 */
	public void removeRoleGroupFromUser(UserID userID, AuthorityID authorityID, RoleGroupID roleGroupID)
	{
		String organisationID = getOrganisationID();
		if (!organisationID.equals(userID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! userID.organisationID=\""+userID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");
		if (!organisationID.equals(authorityID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

		if (User.USERID_SYSTEM.equals(userID))
			throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USERID_SYSTEM + "\"!");

		PersistenceManager pm = getPersistenceManager();
		try
		{
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			Authority authority = (Authority) pm.getObjectById(authorityID);

			authority.getSecuringAuthority().assertContainsRoleRef(getPrincipal(), RoleConstants.securityManager_removeRoleGroupFromUser);

			User user = (User) pm.getObjectById(userID);

			UserRef uref;
			try {
				uref = (UserRef)pm.getObjectById(UserRefID.create(authorityID.authorityID, organisationID, userID.userID), true);
			} catch (JDOObjectNotFoundException ignore) {
				return;
			}

			uref.removeRoleGroupRef(roleGroupID.roleGroupID);

			JFireServerManager jfsm = getJFireServerManager();
			try {
				if (user instanceof UserGroup) {
					for (User member : ((UserGroup)user).getUsers())
						jfsm.jfireSecurity_flushCache(member.getUserID());
				}
				else
					jfsm.jfireSecurity_flushCache(userID.userID);
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
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
	 * @ejb.permission role-name="JFireSecurityManager-write"
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
//	 * @ejb.permission role-name="JFireSecurityManager-write"
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
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<UserID> getUserIDs(QueryCollection<? extends UserQuery> userQueries)
	{
		if (userQueries == null)
			return null;
		
		if (! User.class.isAssignableFrom(userQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ userQueries.getResultClassName());
		}
		
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<UserQuery> decoratedCollection;
			if (userQueries instanceof JDOQueryCollectionDecorator)
			{
				decoratedCollection = (JDOQueryCollectionDecorator<UserQuery>) userQueries;
			}
			else
			{
				decoratedCollection = new JDOQueryCollectionDecorator<UserQuery>(userQueries);
			}

			decoratedCollection.setPersistenceManager(pm);
			Collection<User> users = (Collection<User>) decoratedCollection.executeQueries();

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


	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 **/
	public Authority getAuthority(String authorityID, String [] fetchGroups)
	{
			PersistenceManager pm = getPersistenceManager();
			try {
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
				
				pm.getExtent(Authority.class, true);
				try
				{
					Object o = pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					return (Authority)pm.detachCopy(o);
				}
				catch (JDOObjectNotFoundException x)
				{
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
			} finally {
				pm.close();
			}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 **/
	public AuthoritySearchResult searchAuthorities (
			String searchStr, boolean exact, int itemsPerPage, int pageIndex, String[] fetchGroups, int maxFetchDepth)
		throws SecurityException
	{
		try
		{
			PersistenceManager pm = getPersistenceManager();
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
      if (fetchGroups != null)
      	pm.getFetchPlan().setGroups(fetchGroups);
			
      try
			{
				AuthoritySearchResult result = Authority.searchAuthorities(pm, searchStr, exact, itemsPerPage, pageIndex);
//				result.makeTransient(includeMask);

				result.detachItems(pm);
				return result;
			}
			finally
			{
				pm.close();
			}
		}
		catch (Exception x)
		{
			throw new SecurityException(x);
		}
	}

	/**
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 **/
	public List<Authority> getAllAuthorities()
	throws ModuleException
	{
	  PersistenceManager pm = getPersistenceManager();
	  try
	  {
	    Query query = pm.newQuery(pm.getExtent(Authority.class, true));
	    Collection<Authority> c = (Collection<Authority>) query.execute();
	    List<Authority> result = new ArrayList<Authority>(pm.detachCopyAll(c));
	    return result;
	  }
	  finally
	  {
	    pm.close();
	  }
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 **/
	public RoleGroupRefSearchResult searchRoleGroupRefs(
			String authorityID,
			String searchStr, boolean exact, int itemsPerPage, int pageIndex, int includeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" could not be found in organisation \""+getOrganisationID()+"\"!");
				}
				
				RoleGroupRefSearchResult result = authority.searchRoleGroupRefs(
						searchStr, exact, itemsPerPage, pageIndex);
//				result.makeTransient(includeMask);
				return result;
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 **/
	public UserRefSearchResult searchUserRefs(
			String authorityID,
			String searchStr, boolean exact, int itemsPerPage, int pageIndex, int includeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" could not be found in organisation \""+getOrganisationID()+"\"!");
				}
				
				UserRefSearchResult result = authority.searchUserRefs(
						searchStr, exact, itemsPerPage, pageIndex);
//				result.makeTransient(includeMask);
				return result;
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	// ******************************************************************
	// *** Methods for management of links between UserRefs and RoleRefs
	// ******************************************************************

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-write"
	 * @ejb.transaction type="Required"
	 */
	public void createUserRef(String authorityID, String userID)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				pm.getExtent(User.class, true);

				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
				User user;
				try {
					user = (User)pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new UserNotFoundException("User \""+userID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}

				authority.createUserRef(user);
			} finally {
				pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-write"
	 * @ejb.transaction type="Required"
	 */
	public void destroyUserRef(String authorityID, String userID)
		throws SecurityException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
					pm.getExtent(Authority.class, true);
	//				pm.getExtent(User.class, true);
	
					Authority authority;
					try {
						authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					} catch (JDOObjectNotFoundException x) {
						throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
					}
	//				User user;
	//				try {
	//					user = (User)pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
	//				} catch (JDOObjectNotFoundException x) {
	//					throw new UserNotFoundException("User \""+userID+"\" not found in organisation \""+getOrganisationID()+"\"!");
	//				}
	
					authority.destroyUserRef(userID);
					ism.jfireSecurity_flushCache();
				} finally {
					pm.close();
				}
			} finally {
				ism.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-write"
	 * @ejb.transaction type="Required"
	 */
	public void destroyRoleGroupRef(String authorityID, String roleGroupID)
		throws SecurityException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
					pm.getExtent(Authority.class, true);
	//				pm.getExtent(RoleGroup.class, true);
	
					Authority authority;
					try {
						authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					} catch (JDOObjectNotFoundException x) {
						throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
					}
	//				RoleGroup roleGroup;
	//				try {
	//					roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(roleGroupID), true);
	//				} catch (JDOObjectNotFoundException x) {
	//					throw new UserNotFoundException("RoleGroup \""+roleGroupID+"\" not found in organisation \""+getOrganisationID()+"\"!");
	//				}
	
					authority.destroyRoleGroupRef(roleGroupID);
					ism.jfireSecurity_flushCache();
				} finally {
					pm.close();
				}
			} finally {
				ism.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 * @ejb.transaction type="Required"
	 */
	public UserRef getUserRef(String authorityID, String userID, int includeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				pm.getExtent(User.class, true);

				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
				UserRef userRef = authority.getUserRef(userID);
				if (userRef == null)
					throw new UserRefNotFoundException("UserRef for User \""+userID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
				
//				userRef.makeTransient(includeMask);
				
				return userRef;
			} finally {
				pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-write"
	 * @ejb.transaction type="Required"
	 */
	public void createRoleGroupRef(String authorityID, String roleGroupID)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				pm.getExtent(RoleGroup.class, true);

				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
				RoleGroup roleGroup;
				try {
					roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(roleGroupID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new UserNotFoundException("RoleGroup \""+roleGroupID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}

				authority.createRoleGroupRef(roleGroup);
			} finally {
				pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-read"
	 * @ejb.transaction type="Required"
	 */
	public RoleGroupRef getRoleGroupRef(String authorityID, String roleGroupID, int includeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				pm.getExtent(User.class, true);

				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
				RoleGroupRef roleGroupRef = authority.getRoleGroupRef(roleGroupID);
				if (roleGroupRef == null)
					throw new RoleGroupRefNotFoundException("RoleGroupRef for RoleGroup \""+roleGroupID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
				
//				roleGroupRef.makeTransient(includeMask);
				
				return roleGroupRef;
			} finally {
				pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @param authorityID
	 * @param userID
	 * @param roleGroupID
	 * @throws SecurityException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-write"
	 * @ejb.transaction type="Required"
	 */
	public void addRoleGroupRefToUserRef(String authorityID, String userID, String roleGroupID)
		throws SecurityException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
	//				pm.getExtent(Authority.class, true);
					pm.getExtent(User.class, true);
					pm.getExtent(RoleGroup.class, true);
	
					Authority authority;
					try {
						authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					} catch (JDOObjectNotFoundException x) {
						throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
					}
					
					UserRef userRef = authority.getUserRef(userID);
					if (userRef == null)
						throw new UserRefNotFoundException("UserRef for user \""+userID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
	
					RoleGroupRef roleGroupRef = authority.getRoleGroupRef(roleGroupID);
					if (roleGroupRef == null)
						throw new RoleGroupRefNotFoundException("RoleGroupRef for roleGroup \""+roleGroupID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
	
					userRef.addRoleGroupRef(roleGroupRef);
					ism.jfireSecurity_flushCache(userID);
				} finally {
					pm.close();
				}
			} finally {
				ism.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @param String authorityID
	 * @param userID
	 * @param roleGroupID
	 * @throws SecurityException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireSecurityManager-write"
	 * @ejb.transaction type="Required"
	 */
	public void removeRoleGroupRefFromUserRef(String authorityID, String userID, String roleGroupID)
		throws SecurityException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
	//				pm.getExtent(Authority.class, true);
					pm.getExtent(User.class, true);
					pm.getExtent(RoleGroup.class, true);
	
					Authority authority;
					try {
						authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					} catch (JDOObjectNotFoundException x) {
						throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
					}
					
					UserRef userRef = authority.getUserRef(userID);
					if (userRef == null)
						throw new UserRefNotFoundException("UserRef for user \""+userID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
					
					RoleGroupRef roleGroupRef = authority.getRoleGroupRef(roleGroupID);
					if (roleGroupRef == null)
						throw new UserRefNotFoundException("RoleGroupRef for roleGroup \""+roleGroupID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
	
					userRef.removeRoleGroupRef(roleGroupRef);
					ism.jfireSecurity_flushCache(userID);
				} finally {
					pm.close();
				}
			} finally {
				ism.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

}
