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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.QueryOption;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote;
import org.nightlabs.jfire.jdo.notification.persistent.SubscriptionUtil;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityMetaID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.AuthorizedObjectRefID;
import org.nightlabs.jfire.security.id.PendingUserID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.security.notification.AuthorityNotificationFilter;
import org.nightlabs.jfire.security.notification.AuthorityNotificationReceiver;
import org.nightlabs.jfire.security.search.UserQuery;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.util.CollectionUtil;
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class JFireSecurityManagerBean
extends BaseSessionBeanImpl
implements JFireSecurityManagerRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JFireSecurityManagerBean.class);

	private static final boolean ASSERT_CONSISTENCY_BEFORE = false;
	private static final boolean ASSERT_CONSISTENCY_AFTER = false;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise()
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
// TODO re-enable this code! Temporarily commented out to make switching to EJB3 easier, because it's only a consistency check
// and not essentially necessary for the server to be functional. Marco.
//
//			{
//				TaskID taskID = TaskID.create(
//						// Organisation.DEV_ORGANISATION_ID, // the task can be modified by the organisation and thus it's maybe more logical to use the real organisationID - not dev
//						getOrganisationID(),
//						Task.TASK_TYPE_ID_SYSTEM, "security.checkConsistency");
//				Task task;
//				try {
//					task = (Task) pm.getObjectById(taskID);
//					task.getActiveExecID();
//				} catch (JDOObjectNotFoundException x) {
//					task = new Task(
//							taskID.organisationID, taskID.taskTypeID, taskID.taskID,
//							User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
//							JFireSecurityManagerHome.JNDI_NAME,
//							"checkConsistency");
//
//					task.getName().setText(Locale.ENGLISH.getLanguage(), "Security: Consistency check");
//					task.getDescription().setText(Locale.ENGLISH.getLanguage(), "Check whether all authorities are consistent.");
//
//					task.getTimePatternSet().createTimePattern(
//							"*", // year
//							"*", // month
//							"*", // day
//							"*", // dayOfWeek
//							"03", //  hour
//							"00" // minute
//					);
//
//					task.setEnabled(true);
//					task = pm.makePersistent(task);
//				}
//			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#checkConsistency(org.nightlabs.jfire.timer.id.TaskID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void checkConsistency(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			assertConsistency(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#storeUserSecurityGroup(org.nightlabs.jfire.security.UserSecurityGroup, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.security.storeUser")
	@Override
	public UserSecurityGroup storeUserSecurityGroup(UserSecurityGroup userSecurityGroup, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		if (!userSecurityGroup.getOrganisationID().equals(getOrganisationID()))
			throw new IllegalArgumentException("userSecurityGroup.organisationID must be equal to your organisationID!!!");

		PersistenceManager pm = this.createPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				if (ASSERT_CONSISTENCY_BEFORE)
					assertConsistency(pm);

				userSecurityGroup = pm.makePersistent(userSecurityGroup);

				// ensure that the group has an AuthorizedObjectRef in the organisation-authority (everyone should have one).
				Authority.getOrganisationAuthority(pm).createAuthorizedObjectRef(userSecurityGroup);

				if (ASSERT_CONSISTENCY_AFTER)
					assertConsistency(pm);

				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}


			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(userSecurityGroup);
		} finally {
			pm.close();
		}
	}

	private void validateUserData(String userID, String organisationID)
	{
		if (User.USER_ID_SYSTEM.equals(userID))
			throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USER_ID_SYSTEM + "\"!");
		if (User.USER_ID_OTHER.equals(userID))
			throw new IllegalArgumentException("Cannot change properties of special user \"" + User.USER_ID_OTHER + "\"!");

		if (organisationID != null && !organisationID.equals(getOrganisationID()))
			throw new IllegalArgumentException("user.organisationID must be null or equal to your organisationID!!!");
	}

	protected User storeUser(PersistenceManager pm, User user, String newPassword, boolean checkForPendingUser, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		validateUserData(user.getUserID(), user.getOrganisationID());

		try {
			UserLocal userLocal = user.getUserLocal();
			if (userLocal != null)
				newPassword = userLocal.getNewPassword();
		} catch (JDODetachedFieldAccessException x) {
			// the fields are not detached, hence they can't be changed => ignore and don't set password (leave newPassword null).
		}

		if (newPassword != null && !UserLocal.isValidPassword(newPassword))
			throw new IllegalArgumentException("The new password is not a valid password!");

		if(checkForPendingUser && isIDAlreadyRegistered(PendingUserID.create(user.getOrganisationID(), user.getUserID()), pm))
			throw new UserExistsException("A pending user already exists with user id "+user.getUserID()+" for organisation "+user.getOrganisationID());

		boolean successful = false;
		SecurityChangeController.beginChanging();
		try {
			if (ASSERT_CONSISTENCY_BEFORE)
				assertConsistency(pm);

			user = pm.makePersistent(user);

			if (user.getUserLocal() == null)
				new UserLocal(user); // self-registering

			// ensure that the user has a AuthorizedObjectRef in the organisation-authority (everyone should have one).
			Authority.getOrganisationAuthority(pm).createAuthorizedObjectRef(user.getUserLocal());

			if (newPassword != null)
				user.getUserLocal().setPasswordPlain(newPassword);

			ConfigSetup.ensureAllPrerequisites(pm);

			if (ASSERT_CONSISTENCY_AFTER)
				assertConsistency(pm);

			successful = true;
		} finally {
			SecurityChangeController.endChanging(successful);
		}

		if (!get)
			return null;

		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		if (fetchGroups != null)
			pm.getFetchPlan().setGroups(fetchGroups);

		return pm.detachCopy(user);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#storeUser(org.nightlabs.jfire.security.User, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.security.storeUser")
	@Override
	public User storeUser(User user, String newPassword, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = this.createPersistenceManager();
		try {
			return storeUser(pm, user, newPassword, true, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#storePendingUser(org.nightlabs.jfire.security.PendingUser, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.security.storePendingUser")
	@Override
	public PendingUser storePendingUser(PendingUser user, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		validateUserData(user.getUserID(), user.getOrganisationID());

		String newPassword = user.getPasswordPlain();
		if (newPassword != null && !UserLocal.isValidPassword(newPassword))
			throw new IllegalArgumentException("The new password is not a valid password!");

		PersistenceManager pm = this.createPersistenceManager();
		try {

			pm.getExtent(User.class);
			pm.getExtent(PendingUser.class);
			try {
				User existingUser = (User) pm.getObjectById(UserID.create(user.getOrganisationID(), user.getUserID()));
				// TODO WORKAROUND: DataNucleus workaround - is this still needed?
				existingUser.getUserID();
				// the user does exist -> throw exception
				throw new UserExistsException("A user already exists with user id "+user.getUserID()+" for organisation "+user.getOrganisationID());
			} catch(JDOObjectNotFoundException e) {
				// the pending user does not exist -> continue.
			}

			user = pm.makePersistent(user);

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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#storePendingUserAsUser(org.nightlabs.jfire.security.PendingUser, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.security.storePendingUserAsUser")
	@Override
	public User storePendingUserAsUser(PendingUser user, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = this.createPersistenceManager();
		try {
			user = (PendingUser) pm.getObjectById(PendingUserID.create(user.getOrganisationID(), user.getUserID()));

			User newUser = new User(user.getOrganisationID(), user.getUserID());
			newUser.setDescription(user.getDescription());
			newUser.setName(user.getName());
			newUser.setPerson(user.getPerson());
			newUser = storeUser(pm, newUser, user.getPasswordPlain(), false, get, fetchGroups, maxFetchDepth);

			pm.deletePersistent(user);

			return newUser;
		} finally {
			pm.close();
		}
	}

	@Override
	public void deletePendingUser(PendingUserID pendingUserID)
	{
		PersistenceManager pm = this.createPersistenceManager();
		try {
			pm.deletePersistent(pm.getObjectById(pendingUserID));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getRolesForRequiredRoleIDs(java.util.Set)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<Role> getRolesForRequiredRoleIDs(Set<RoleID> roleIDs)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
			pm.getFetchPlan().setGroups(MissingRoleException.FETCH_GROUPS_ROLE);

			Set<Role> roles = NLJDOHelper.getObjectSet(pm, roleIDs, Role.class);
			roles = new HashSet<Role>(pm.detachCopyAll(roles));

			MissingRoleException.populateRoleSetWithSpecialRoles(roleIDs, roles);

			return roles;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getUserIDs(java.lang.String, java.util.Set)
	 */
	@RolesAllowed({"org.nightlabs.jfire.security.accessRightManagement", "org.nightlabs.jfire.security.queryUsers"})
	@SuppressWarnings("unchecked")
	@Override
	public Set<UserID> getUserIDs(String organisationID, Set<String> userTypes)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getUsers(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<User> getUsers(Collection<UserID> userIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, userIDs, User.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getPendingUserIDs(java.lang.String)
	 */
	@RolesAllowed({"org.nightlabs.jfire.security.accessRightManagement", "org.nightlabs.jfire.security.queryPendingUsers"})
	@SuppressWarnings("unchecked")
	@Override
	public Set<PendingUserID> getPendingUserIDs(String organisationID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query query = pm.newQuery(pm.getExtent(PendingUser.class, true));
			query.setResult("JDOHelper.getObjectId(this)");

			HashMap<String, Object> params = new HashMap<String, Object>(1);
			if(organisationID != null) {
				query.setFilter("this.organisationID == :organisationID");
				params.put("organisationID", organisationID);
			}

			return new HashSet<PendingUserID>((Collection<? extends PendingUserID>) query.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed({"org.nightlabs.jfire.security.accessRightManagement", "org.nightlabs.jfire.security.queryPendingUsers"})
	@Override
	public List<PendingUser> getPendingUsers(Collection<PendingUserID> pendingUserIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, pendingUserIDs, PendingUser.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getUserSecurityGroupIDs()
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@SuppressWarnings("unchecked")
	@Override
	public Set<UserSecurityGroupID> getUserSecurityGroupIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(UserSecurityGroup.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<UserSecurityGroupID>((Collection<? extends UserSecurityGroupID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getUserSecurityGroups(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public List<UserSecurityGroup> getUserSecurityGroups(Collection<UserSecurityGroupID> userSecurityGroupIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, userSecurityGroupIDs, UserSecurityGroup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	private static Collection<RoleGroup> getGrantedRoleGroups(PersistenceManager pm, AuthorizedObject authorizedObject, Authority authority)
	{
		if (!Util.equals(authority.getOrganisationID(), authorizedObject.getOrganisationID()))
			throw new IllegalArgumentException("authority.organisationID != authorizedObject.organisationID");

//		Query q = pm.newQuery(RoleGroup.class);
//		q.declareVariables(RoleGroupRef.class.getName() + " roleGroupRef; " + AuthorizedObjectRef.class.getName() + " authorizedObjectRef");
//		q.setFilter(
//				"this == roleGroupRef.roleGroup && \n" +
//				"roleGroupRef.authority == :authority && \n" +
//				"roleGroupRef.authorizedObjectRefs.containsValue(authorizedObjectRef) && \n" +
//				"authorizedObjectRef.authorizedObject == :authorizedObject && \n" +
//				"authorizedObjectRef.authority == :authority"
//		);
//		Map<String, Object> params = new HashMap<String, Object>(2);
//		params.put("authorizedObject", authorizedObject);
//		params.put("authority", authority);
//		Collection<?> c = (Collection<?>) q.executeWithMap(params);
//		Collection<? extends RoleGroup> crg = CollectionUtil.castCollection(c);
//		return new HashSet<RoleGroup>(crg);

		// TODO WORKAROUND DATANUCLEUS: The above query works but is very slow (due to the well-known short-comings of the old query-engine) - we should re-enable the above query
		// as soon as DataNucleus' new query engine is ready.
		Set<RoleGroup> result = new HashSet<RoleGroup>();
		AuthorizedObjectRef authorizedObjectRef = authority.getAuthorizedObjectRef(authorizedObject);
		if (authorizedObjectRef != null) {
			for (RoleGroupRef roleGroupRef : authorizedObjectRef.getRoleGroupRefs()) {
				result.add(roleGroupRef.getRoleGroup());
			}
		}
		return result;
	}

	/**
	 * Get all those {@link UserSecurityGroupRef}s within the given <code>authority</code> that
	 * belong to a {@link UserSecurityGroup} where the given <code>user</code> is a member.
	 *
	 * @param pm the gate to our datastore.
	 * @param member the {@link AuthorizedObject} for which to query the membership of its {@link UserSecurityGroup}s.
	 * @param authority the {@link Authority} in which we query.
	 */
	private static Collection<UserSecurityGroupRef> getUserSecurityGroupRefsForMemberWithinAuthority(PersistenceManager pm, AuthorizedObject member, Authority authority)
	{
		if (!Util.equals(authority.getOrganisationID(), member.getOrganisationID()))
			throw new IllegalArgumentException("member.organisationID != authority.organisationID");

//		Query q = pm.newQuery(UserSecurityGroupRef.class);
//		q.setFilter(":authority.authorizedObjectRefs.containsValue(this) && :member.userSecurityGroups.contains(this.authorizedObject)");
//		Map<String, Object> params = new HashMap<String, Object>(2);
//		params.put("member", member);
//		params.put("authority", authority);
//		return (Collection<UserSecurityGroupRef>) q.executeWithMap(params);

		// TODO WORKAROUND DATANUCLEUS
		// The above query fails with:
//		Caused by: org.datanucleus.store.mapped.expression.ScalarExpression$IllegalOperationException: Cannot perform operation "==" on ObjectLiteral "?" = org.nightlabs.jfire.security.UserSecurityGroup@117343f[chezfrancois.jfire.org,Administrators] and ReferenceExpression "this.authorized_object_user_security_group_organisation_id_eid,this.authorized_object_user_security_group_user_security_group_id_eid,this.authorized_object_user_local_organisation_id_eid,this.authorized_object_user_local_user_id_eid"
//		at org.datanucleus.store.mapped.expression.ScalarExpression.eq(ScalarExpression.java:563)
//		at org.datanucleus.store.mapped.expression.ObjectExpression.eq(ObjectExpression.java:321)
//		at org.datanucleus.store.mapped.expression.ObjectLiteral.eq(ObjectLiteral.java:120)
//		at org.datanucleus.store.mapped.expression.CollectionLiteral.containsMethod(CollectionLiteral.java:121)
//		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//		at java.lang.reflect.Method.invoke(Method.java:597)
//		at org.datanucleus.store.mapped.expression.ScalarExpression.callMethod(ScalarExpression.java:860)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compilePrimary(JDOQLQueryCompiler.java:852)
//		at org.datanucleus.store.rdbms.query.JavaQueryCompiler.compileUnaryExpressionNotPlusMinus(JavaQueryCompiler.java:1145)
//		at org.datanucleus.store.rdbms.query.JavaQueryCompiler.compileUnaryExpression(JavaQueryCompiler.java:1126)
//		at org.datanucleus.store.rdbms.query.JavaQueryCompiler.compileMultiplicativeExpression(JavaQueryCompiler.java:1079)
//		at org.datanucleus.store.rdbms.query.JavaQueryCompiler.compileAdditiveExpression(JavaQueryCompiler.java:1056)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileRelationalExpression(JDOQLQueryCompiler.java:690)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileEqualityExpression(JDOQLQueryCompiler.java:662)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileAndExpression(JDOQLQueryCompiler.java:650)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileExclusiveOrExpression(JDOQLQueryCompiler.java:638)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileInclusiveOrExpression(JDOQLQueryCompiler.java:626)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileConditionalAndExpression(JDOQLQueryCompiler.java:617)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileConditionalOrExpression(JDOQLQueryCompiler.java:596)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileExpression(JDOQLQueryCompiler.java:573)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compileExpressionFromString(JDOQLQueryCompiler.java:553)
//		at org.datanucleus.store.rdbms.query.JavaQueryCompiler.compileFilter(JavaQueryCompiler.java:659)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.performCompile(JDOQLQueryCompiler.java:262)
//		at org.datanucleus.store.rdbms.query.JavaQueryCompiler.executionCompile(JavaQueryCompiler.java:297)
//		at org.datanucleus.store.rdbms.query.JDOQLQueryCompiler.compile(JDOQLQueryCompiler.java:226)
//		at org.datanucleus.store.rdbms.query.JDOQLQuery.compileInternal(JDOQLQuery.java:186)
//		at org.datanucleus.store.rdbms.query.JDOQLQuery.performExecute(JDOQLQuery.java:279)
//		at org.datanucleus.store.query.Query.executeWithMap(Query.java:1334)
//		at org.datanucleus.jdo.JDOQuery.executeWithMap(JDOQuery.java:330)

		Collection<UserSecurityGroupRef> res = new HashSet<UserSecurityGroupRef>();
		for (UserSecurityGroup userSecurityGroup : member.getUserSecurityGroups()) {
			UserSecurityGroupRef ref = (UserSecurityGroupRef) authority.getAuthorizedObjectRef(userSecurityGroup);
			if (ref != null)
				res.add(ref);
		}
		return res;
	}

	private static RoleGroupIDSetCarrier getRoleGroupIDSetCarrier(PersistenceManager pm, AuthorizedObject authorizedObject, Authority authority)
	{
		String organisationID = authorizedObject.getOrganisationID();
		if (!organisationID.equals(authority.getOrganisationID()))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authority.organisationID=\""+authority.getOrganisationID()+"\" does not match authorizedObject.organisationID=\""+authorizedObject.getOrganisationID()+"\"!");

		AuthorizedObjectRef userRef = authority.getAuthorizedObjectRef(authorizedObject);
		boolean userIsInAuthority = userRef != null && userRef.isVisible();
		if (Authority.AUTHORITY_ID_ORGANISATION.equals(authority.getAuthorityID()))
			userIsInAuthority = true;

		boolean controlledByOtherUser = !userIsInAuthority;

		// the groups are never controlled by the "other" user
		// and they are currently not allowed to be included in other groups
		if (authorizedObject instanceof UserSecurityGroup)
			controlledByOtherUser = false;

		if (controlledByOtherUser) {
			// find out if there is a UserGroup in which the user is member and which is within this authority
			controlledByOtherUser = getUserSecurityGroupRefsForMemberWithinAuthority(pm, authorizedObject, authority).isEmpty();
		}

		Collection<RoleGroup> roleGroupsUser = getGrantedRoleGroups(pm, authorizedObject, authority);
		Collection<RoleGroup> roleGroupsUserGroups = new HashSet<RoleGroup>();
		for (UserSecurityGroup userGroupLocal : authorizedObject.getUserSecurityGroups()) {
			roleGroupsUserGroups.addAll(getGrantedRoleGroups(pm, userGroupLocal, authority));
		}

		Collection<RoleGroup> allRoleGroups = authority.getAuthorityType().getRoleGroups();
		Set<RoleGroupID> allRoleGroupIDs = NLJDOHelper.getObjectIDSet(allRoleGroups);
		Set<RoleGroupID> roleGroupIDsUser = NLJDOHelper.getObjectIDSet(roleGroupsUser);
		Set<RoleGroupID> roleGroupIDsUserGroups = NLJDOHelper.getObjectIDSet(roleGroupsUserGroups);
		Set<RoleGroupID> roleGroupIDsOtherUser;

		if (controlledByOtherUser) {
			User otherUser = (User) pm.getObjectById(UserID.create(organisationID, User.USER_ID_OTHER));
			Collection<RoleGroup> roleGroupsOtherUser = getGrantedRoleGroups(pm, otherUser.getUserLocal(), authority);
			roleGroupIDsOtherUser = NLJDOHelper.getObjectIDSet(roleGroupsOtherUser);
		}
		else
			roleGroupIDsOtherUser = new HashSet<RoleGroupID>(0);

		return new RoleGroupIDSetCarrier(
				(AuthorizedObjectID)JDOHelper.getObjectId(authorizedObject),
				(AuthorityID)JDOHelper.getObjectId(authority),
				allRoleGroupIDs,
				roleGroupIDsUser,
				roleGroupIDsUserGroups,
				roleGroupIDsOtherUser,
				userIsInAuthority,
				controlledByOtherUser);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getRoleGroupIDSetCarriers(java.util.Collection, org.nightlabs.jfire.security.id.AuthorityID)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<RoleGroupIDSetCarrier> getRoleGroupIDSetCarriers(Collection<? extends AuthorizedObjectID> authorizedObjectIDs, AuthorityID authorityID)
	{
		// If it's only one, we delegate to the other method since it allows retrieval of information about the user himself.
		// We don't allow these self-requests below (we assert access rights being present).
		if (authorizedObjectIDs.size() == 1)
			return Collections.singletonList(getRoleGroupIDSetCarrier(authorizedObjectIDs.iterator().next(), authorityID));

		String organisationID = getOrganisationID();
		if (!organisationID.equals(authorityID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

		PersistenceManager pm = createPersistenceManager();
		try {
			Authority authority = (Authority) pm.getObjectById(authorityID);
			Authority.resolveSecuringAuthority(
					pm,
					authority,
					ResolveSecuringAuthorityStrategy.organisation
			).assertContainsRoleRef(getPrincipal(), RoleConstants.getRoleGroupIDSetCarrier);

			List<RoleGroupIDSetCarrier> result = new ArrayList<RoleGroupIDSetCarrier>(authorizedObjectIDs.size());
			for (AuthorizedObjectID authorizedObjectID : authorizedObjectIDs) {
				AuthorizedObject authorizedObject = (AuthorizedObject) pm.getObjectById(authorizedObjectID);
				if (!organisationID.equals(authorizedObject.getOrganisationID()))
					throw new IllegalArgumentException("Cannot manage foreign access rights! authorizedObject.organisationID=\""+authorizedObject.getOrganisationID()+"\" does not match our organisationID=\""+organisationID+"\"!");

				result.add(getRoleGroupIDSetCarrier(pm, authorizedObject, authority));
			}

			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getRoleGroupIDSetCarrier(org.nightlabs.jfire.security.id.AuthorizedObjectID, org.nightlabs.jfire.security.id.AuthorityID)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public RoleGroupIDSetCarrier getRoleGroupIDSetCarrier(AuthorizedObjectID authorizedObjectID, AuthorityID authorityID)
	{
		String organisationID = getOrganisationID();
		if (!organisationID.equals(authorityID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

		PersistenceManager pm = createPersistenceManager();
		try {
			AuthorizedObject authorizedObject = (AuthorizedObject) pm.getObjectById(authorizedObjectID);
			if (!organisationID.equals(authorizedObject.getOrganisationID()))
				throw new IllegalArgumentException("Cannot manage foreign access rights! authorizedObject.organisationID=\""+authorizedObject.getOrganisationID()+"\" does not match our organisationID=\""+organisationID+"\"!");

			Authority authority = (Authority) pm.getObjectById(authorityID);

			boolean allowed = Authority.resolveSecuringAuthority(
					pm,
					authority,
					ResolveSecuringAuthorityStrategy.organisation
			).containsRoleRef(getPrincipal(), RoleConstants.getRoleGroupIDSetCarrier);

			// Not allowed means that the authority which controls the access rights for the given authority
			// (which might be the global authority) does not grant the user the necessary right. In this
			// case, we check if the user is asking about himself, which is allowed.
			if (!allowed) {
				if (authorizedObject instanceof UserLocal) {
					if (getUserID().equals(((UserLocal)authorizedObject).getUserID())) // the user asks about himself - this is allowed
						allowed = true;
				}
			}

			if (!allowed) {
				if (authorizedObject instanceof UserSecurityGroup) { // since the user does not ask about himself, we check if he asks about a group in which he's a member
					UserSecurityGroup userSecurityGroup = (UserSecurityGroup)authorizedObject;
					UserLocal userLocal = User.getUser(pm, getPrincipal()).getUserLocal();
					if (userSecurityGroup.getMembers().contains(userLocal))
						allowed = true;
				}
			}

			if (!allowed)
				throw new MissingRoleException(pm, UserID.create(getPrincipal()), authorityID, RoleConstants.getRoleGroupIDSetCarrier, " Additionally, he does not ask about himself (which is allowed even without these rights).");

			return getRoleGroupIDSetCarrier(pm, authorizedObject, authority);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getRoleGroupIDSetCarriers(org.nightlabs.jfire.security.id.AuthorityID)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public List<RoleGroupIDSetCarrier> getRoleGroupIDSetCarriers(AuthorityID authorityID)
	{
		String organisationID = getOrganisationID();
		if (!organisationID.equals(authorityID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

		PersistenceManager pm = createPersistenceManager();
		try {
			Authority authority = (Authority) pm.getObjectById(authorityID);

			Authority.resolveSecuringAuthority(
					pm,
					authority,
					ResolveSecuringAuthorityStrategy.organisation
			).assertContainsRoleRef(getPrincipal(), RoleConstants.getRoleGroupIDSetCarrier);

			Query q = pm.newQuery(UserLocal.class);
			q.setFilter("this.organisationID == :organisationID");
			Collection<UserLocal> userLocals = CollectionUtil.castCollection((Collection<?>) q.execute(organisationID));

			q = pm.newQuery(UserSecurityGroup.class);
			q.setFilter("this.organisationID == :organisationID");
			Collection<UserSecurityGroup> userSecurityGroups = CollectionUtil.castCollection((Collection<?>) q.execute(organisationID));

			List<RoleGroupIDSetCarrier> result = new ArrayList<RoleGroupIDSetCarrier>(userLocals.size() + userSecurityGroups.size());
			for (UserLocal userLocal : userLocals) {
				RoleGroupIDSetCarrier roleGroupIDSetCarrier = getRoleGroupIDSetCarrier(pm, userLocal, authority);
				result.add(roleGroupIDSetCarrier);
			}
			for (UserSecurityGroup userSecurityGroup : userSecurityGroups) {
				RoleGroupIDSetCarrier roleGroupIDSetCarrier = getRoleGroupIDSetCarrier(pm, userSecurityGroup, authority);
				result.add(roleGroupIDSetCarrier);
			}

			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getRoleGroups(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public Collection<RoleGroup> getRoleGroups(Collection<RoleGroupID> roleGroupIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, roleGroupIDs, RoleGroup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getAuthorityIDs(java.lang.String, org.nightlabs.jfire.security.id.AuthorityTypeID)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<AuthorityID> getAuthorityIDs(String organisationID, AuthorityTypeID authorityTypeID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(Authority.class);
			q.setResult("JDOHelper.getObjectId(this)");
			StringBuilder filter = new StringBuilder();

			if (organisationID != null)
				filter.append("this.organisationID == :organisationID");

			if (authorityTypeID != null) {
				if (filter.length() != 0)
					filter.append(" && ");

				filter.append("JDOHelper.getObjectId(this.authorityType) == :authorityTypeID");
			}

			if (filter.length() != 0)
				q.setFilter(filter.toString());

			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", organisationID);
			params.put("authorityTypeID", authorityTypeID);

			Collection<AuthorityID> c =  CollectionUtil.castCollection((Collection<?>) q.executeWithMap(params));
			return new HashSet<AuthorityID>(c);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getAuthorities(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public List<Authority> getAuthorities(Collection<AuthorityID> authorityIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, authorityIDs, Authority.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getAuthorityTypeIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<AuthorityTypeID> getAuthorityTypeIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(AuthorityType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<? extends AuthorityTypeID> authorityTypeIDs = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<AuthorityTypeID>(authorityTypeIDs);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getAuthorityTypes(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<AuthorityType> getAuthorityTypes(Collection<AuthorityTypeID> authorityTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, authorityTypeIDs, AuthorityType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	private boolean isIDAlreadyRegistered(Object objectID, PersistenceManager pm)
	{
		try {
			Object test = pm.getObjectById(objectID, true);

			if (logger.isDebugEnabled())
				logger.debug("idAlreadyRegistered(\"" + objectID + "\") = " + (test != null));

			return (test != null);
		} catch(JDOObjectNotFoundException e) {
			if (logger.isDebugEnabled())
				logger.debug("idAlreadyRegistered(\"" + objectID + "\") = false");

			return false;
		}
	}

	/**
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#userIDAlreadyRegistered(org.nightlabs.jfire.security.id.UserID)
	 * @deprecated use {@link #isUserIDAlreadyRegistered(UserID)} instead
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	@Deprecated
	public boolean userIDAlreadyRegistered(UserID userID)
	{
		return isUserIDAlreadyRegistered(userID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#isUserIDAlreadyRegistered(org.nightlabs.jfire.security.id.UserID)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public boolean isUserIDAlreadyRegistered(UserID userID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return isIDAlreadyRegistered(userID, pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#isPendingUserIDAlreadyRegistered(org.nightlabs.jfire.security.id.PendingUserID)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public boolean isPendingUserIDAlreadyRegistered(PendingUserID userID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return isIDAlreadyRegistered(userID, pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#isUserIDAvailable(java.lang.String, java.lang.String)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public boolean isUserIDAvailable(String organisationID, String userID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return
				!isIDAlreadyRegistered(UserID.create(organisationID, userID), pm) &&
				!isIDAlreadyRegistered(PendingUserID.create(organisationID, userID), pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getAuthorizedObjectIDs()
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public Set<AuthorizedObjectID> getAuthorizedObjectIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			// Since the class AuthorizedObject is not persistence-capable, it cannot be queried directly => we query the known 2 subclasses instead
			Class<?>[] authorizedObjectPersistenceCapables = {
					UserLocal.class,
					UserSecurityGroup.class
			};

			List<Collection<? extends AuthorizedObjectID>> queryResults = new LinkedList<Collection<? extends AuthorizedObjectID>>();

			int count = 0;
			for (Class<?> clazz : authorizedObjectPersistenceCapables) {
				Query q = pm.newQuery(clazz);
				q.setResult("JDOHelper.getObjectId(this)");
				Collection<? extends AuthorizedObjectID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
				count += c.size();
				queryResults.add(c);
			}

			Set<AuthorizedObjectID> res = new HashSet<AuthorizedObjectID>(count);
			for (Collection<? extends AuthorizedObjectID> queryResult : queryResults)
				res.addAll(queryResult);

			return res;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getAuthorizedObjects(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public List<AuthorizedObject> getAuthorizedObjects(Collection<AuthorizedObjectID> authorizedObjectIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, authorizedObjectIDs, AuthorizedObject.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

//	/**
//	* Returns a detached user.
//	*
//	* @param userID id of the user
//	* @return the detached user
//	*
//	* @ejb.interface-method
//	* @ejb.permission role-name="org.nightlabs.jfire.security.accessRightManagement"
//	*/
//	public User getUser(UserID userID, String[] fetchGroups, int maxFetchDepth)
//	{
//	PersistenceManager pm = this.getPersistenceManager();
//	try {
//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//	if (fetchGroups != null)
//	pm.getFetchPlan().setGroups(fetchGroups);

//	pm.getExtent(User.class, true);
//	Object o = pm.getObjectById(userID,true);
//	User usr = (User)pm.detachCopy(o);

//	return usr;
//	} finally {
//	pm.close();
//	}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#setGrantedRoleGroups(org.nightlabs.jfire.security.id.AuthorizedObjectID, org.nightlabs.jfire.security.id.AuthorityID, java.util.Set)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public void setGrantedRoleGroups(AuthorizedObjectID authorizedObjectID, AuthorityID authorityID, Set<RoleGroupID> roleGroupIDs)
	{
		String organisationID = getOrganisationID();
		if (!organisationID.equals(authorityID.organisationID))
			throw new IllegalArgumentException("Cannot manage foreign access rights! authorityID.organisationID=\""+authorityID.organisationID+"\" does not match our organisationID=\""+organisationID+"\"!");

		PersistenceManager pm = createPersistenceManager();
		try {
			if (ASSERT_CONSISTENCY_BEFORE)
				assertConsistency(pm);

			Authority authority = (Authority) pm.getObjectById(authorityID);

			Object o = pm.getObjectById(authorizedObjectID);
			if (!(o instanceof AuthorizedObject))
				throw new IllegalArgumentException("The object referenced by authorizedObjectID does not extend AuthorizedObject! " + authorizedObjectID);

			AuthorizedObject authorizedObject = (AuthorizedObject) o;

			if (!organisationID.equals(authorizedObject.getOrganisationID()))
				throw new IllegalArgumentException("Cannot manage foreign access rights! authorizedObject.organisationID=\"" + authorizedObject.getOrganisationID() + "\" does not match our organisationID=\""+organisationID+"\"!");

			if (authorizedObject instanceof UserLocal) {
				if (User.USER_ID_SYSTEM.equals(((UserLocal)authorizedObject).getUserID()))
					throw new IllegalArgumentException("Cannot manipulate system user \"" + User.USER_ID_SYSTEM + "\"!");
			}

			if (!getOrganisationID().equals(authorizedObject.getOrganisationID()))
				throw new IllegalArgumentException("AuthorizedObject is not managed by this organisation (" + getOrganisationID() + ")! " + authorizedObjectID);

			// authorize
			Authority.resolveSecuringAuthority(
					pm,
					authority,
					ResolveSecuringAuthorityStrategy.organisation
			).assertContainsRoleRef(
					getPrincipal(), RoleConstants.setGrantedRoleGroups
			);

			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {

				if (roleGroupIDs == null) {
					// A user should never be removed from the organisation-authority (and necessarily be added).
					if (Authority.AUTHORITY_ID_ORGANISATION.equals(authority.getAuthorityID()))
						authority.createAuthorizedObjectRef(authorizedObject);
					else
						authority.destroyAuthorizedObjectRef(authorizedObject);
				}
				else {
					AuthorizedObjectRef userRef = authority.createAuthorizedObjectRef(authorizedObject);
					Set<RoleGroupID> roleGroupIDsToAdd = new HashSet<RoleGroupID>(roleGroupIDs);

					// first remove all RoleGroupRefs from the AuthorizedObjectRef that are not in the given roleGroupIDs set
					for (RoleGroupRef roleGroupRef : new ArrayList<RoleGroupRef>(userRef.getRoleGroupRefs())) {
						RoleGroupID roleGroupID = (RoleGroupID) JDOHelper.getObjectId(roleGroupRef.getRoleGroup());
						if (roleGroupID == null)
							throw new IllegalStateException("JDOHelper.getObjectId(roleGroupRef.getRoleGroup()) returned null!");

						if (!roleGroupIDsToAdd.remove(roleGroupID))
							userRef.removeRoleGroupRef(roleGroupID);
					}

					// now add what's missing
					for (RoleGroupID roleGroupID : roleGroupIDsToAdd) {
						RoleGroup roleGroup = (RoleGroup) pm.getObjectById(roleGroupID);
						if (!authority.getAuthorityType().getRoleGroups().contains(roleGroup))
							throw new IllegalArgumentException("The roleGroupIDs argument contained the roleGroupID \"" + roleGroupID.roleGroupID + "\" which is not part of the AuthorityType of the specified authority " + authorityID + "!");

						RoleGroupRef roleGroupRef = authority.createRoleGroupRef(roleGroup);
						userRef.addRoleGroupRef(roleGroupRef);
					}
				}


				if (ASSERT_CONSISTENCY_AFTER)
					assertConsistency(pm);

				successful = true; // whether an error occurs during flushing the cache doesn't matter much
			} finally {
				SecurityChangeController.endChanging(successful);
			}

			if (Authority.AUTHORITY_ID_ORGANISATION.equals(authority.getAuthorityID())) {
				JFireServerManager jfsm = getJFireServerManager();
				try {
					if (authorizedObject instanceof UserSecurityGroup) {
						for (AuthorizedObject member : ((UserSecurityGroup)authorizedObject).getMembers()) {
							Object memberID = JDOHelper.getObjectId(member);
							if (memberID == null)
								throw new IllegalStateException("JDOHelper.getObjectId(member) returned null! " + member);

							if (memberID instanceof UserLocalID) {
								UserLocalID userLocalID = (UserLocalID) memberID;
								jfsm.jfireSecurity_flushCache(UserID.create(userLocalID));
							}
						}
					}
					else {
						if (authorizedObjectID instanceof UserLocalID) {
							UserLocalID userLocalID = (UserLocalID) authorizedObjectID;
							jfsm.jfireSecurity_flushCache(UserID.create(userLocalID));
						}
					}
				} finally {
					jfsm.close();
				}
			} // if (Authority.AUTHORITY_ID_ORGANISATION.equals(authority.getAuthorityID()))

		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#setUserSecurityGroupsOfMember(java.util.Set, org.nightlabs.jfire.security.id.AuthorizedObjectID)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.setMembersOfUserSecurityGroup")
	@Override
	public void setUserSecurityGroupsOfMember(Set<UserSecurityGroupID> userSecurityGroupIDs, AuthorizedObjectID memberAuthorizedObjectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if (ASSERT_CONSISTENCY_BEFORE)
				assertConsistency(pm);

			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {

				AuthorizedObject member = (AuthorizedObject) pm.getObjectById(memberAuthorizedObjectID);

				Set<UserSecurityGroupID> groupIDsToAdd = new HashSet<UserSecurityGroupID>(userSecurityGroupIDs);
				for (UserSecurityGroup group : new HashSet<UserSecurityGroup>(member.getUserSecurityGroups())) {
					Object groupID = JDOHelper.getObjectId(group);
					if (groupID == null)
						throw new IllegalStateException("JDOHelper.getObjectId(group) returned null!");

					if (!groupIDsToAdd.remove(groupID))
						group.removeMember(member);
				}

				for (UserSecurityGroupID groupID : groupIDsToAdd) {
					UserSecurityGroup userSecurityGroup = (UserSecurityGroup) pm.getObjectById(groupID);
					userSecurityGroup.addMember(member);
				}

				if (ASSERT_CONSISTENCY_AFTER)
					assertConsistency(pm);

				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}

			// TODO optimize flushing JavaEE authentication cache! Only flush the affected users!
			JFireServerManager jfsm = getJFireServerManager();
			try {
				jfsm.jfireSecurity_flushCache();
			} finally {
				jfsm.close();
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#setMembersOfUserSecurityGroup(org.nightlabs.jfire.security.id.UserSecurityGroupID, java.util.Set)
	 */
	@RolesAllowed("org.nightlabs.jfire.security.setMembersOfUserSecurityGroup")
	@Override
	public void setMembersOfUserSecurityGroup(UserSecurityGroupID userSecurityGroupID, Set<? extends AuthorizedObjectID> memberAuthorizedObjectIDs)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if (ASSERT_CONSISTENCY_BEFORE)
				assertConsistency(pm);

			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				pm.getExtent(UserSecurityGroup.class);
				UserSecurityGroup userSecurityGroup = (UserSecurityGroup) pm.getObjectById(userSecurityGroupID);

				Set<ObjectID> memberAuthorizedObjectIDsToAdd = new HashSet<ObjectID>(memberAuthorizedObjectIDs);
				for (AuthorizedObject member : new HashSet<AuthorizedObject>(userSecurityGroup.getMembers())) {
					Object memberID = JDOHelper.getObjectId(member);
					if (memberID == null)
						throw new IllegalStateException("JDOHelper.getObjectId(member) returned null!");

					if (!memberAuthorizedObjectIDsToAdd.remove(memberID))
						userSecurityGroup.removeMember(member);
				}

				for (ObjectID memberID : memberAuthorizedObjectIDsToAdd) {
					AuthorizedObject authorizedObject = (AuthorizedObject) pm.getObjectById(memberID);
					userSecurityGroup.addMember(authorizedObject);
				}

				if (ASSERT_CONSISTENCY_AFTER)
					assertConsistency(pm);

				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}

			// TODO optimize flushing JavaEE authentication cache! Only flush the affected users!
			JFireServerManager jfsm = getJFireServerManager();
			try {
				jfsm.jfireSecurity_flushCache();
			} finally {
				jfsm.close();
			}
		} finally {
			pm.close();
		}
	}

	private static void assertConsistency(PersistenceManager pm)
	{
		String localOrganisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

		// check if all user.groups matches group.members
		for (Iterator<UserLocal> it = pm.getExtent(UserLocal.class).iterator(); it.hasNext(); ) {
			UserLocal userLocal = it.next();
			for (UserSecurityGroup userSecurityGroup : userLocal.getUserSecurityGroups()) {
				if (!userSecurityGroup.getMembers().contains(userLocal))
					throw new IllegalStateException(userLocal.toString() + " contains " + userSecurityGroup + ", but this group doesn't list the UserLocal as a member!");
			}
		}
		// other way
		for (Iterator<UserSecurityGroup> it = pm.getExtent(UserSecurityGroup.class).iterator(); it.hasNext(); ) {
			UserSecurityGroup userSecurityGroup = it.next();
			for (AuthorizedObject member : userSecurityGroup.getMembers()) {
				if (!member.getUserSecurityGroups().contains(userSecurityGroup))
					throw new IllegalStateException(userSecurityGroup.toString() + " contains " + member + ", but this member doesn't have the group in his list of groups!");
			}
		}

		// check LOCAL authorities
		Query qAuthorities = pm.newQuery(Authority.class);
		qAuthorities.setFilter("this.organisationID == :organisationID");
		Collection<Authority> authorities = CollectionUtil.castCollection(
				(Collection<?>)qAuthorities.execute(localOrganisationID)
		);
		for (Iterator<Authority> it = authorities.iterator(); it.hasNext(); )
			_assertConsistency(pm, it.next());
	}

	private static void _assertConsistency(PersistenceManager pm, Authority authority)
	{
		for (AuthorizedObjectRef authorizedObjectRef : authority.getAuthorizedObjectRefs()) {
			int calculatedUserSecurityGroupReferenceCount = _calculateAuthorizedObjectRef_userSecurityGroupReferenceCount(pm, authority, authorizedObjectRef.getAuthorizedObject());
			if (calculatedUserSecurityGroupReferenceCount != authorizedObjectRef.getUserSecurityGroupReferenceCount())
				throw new IllegalStateException("calculatedUserSecurityGroupReferenceCount != authorizedObjectRef.getUserSecurityGroupReferenceCount()!!! authority=" + authority + " authorizedObjectRef=" + authorizedObjectRef + " calculatedUserSecurityGroupReferenceCount=" + calculatedUserSecurityGroupReferenceCount + " authorizedObjectRef.userSecurityGroupReferenceCount=" + authorizedObjectRef.getUserSecurityGroupReferenceCount());

			for (RoleRef roleRef : authorizedObjectRef.getRoleRefs()) {
				// check whether there are really this many groups and direct references
				int calculatedReferenceCount = _calculateRoleRef_referenceCount(pm, authority, authorizedObjectRef.getAuthorizedObject(), roleRef.getRole());
				if (calculatedReferenceCount != roleRef.getReferenceCount())
					throw new IllegalStateException("calculatedReferenceCount != memberRoleRef.getReferenceCount()!!! authority=" + authority + " authorizedObjectRef=" + authorizedObjectRef + " calculatedReferenceCount=" + calculatedReferenceCount + " memberRoleRef.referenceCount=" + roleRef.getReferenceCount() + " memberRoleRef" + roleRef);
			}

			if (authorizedObjectRef instanceof UserSecurityGroupRef) {
				UserSecurityGroupRef userSecurityGroupRef = (UserSecurityGroupRef)authorizedObjectRef;

				Set<Role> userSecurityGroupRoles = _getRolesFromRoleRefs(authorizedObjectRef.getRoleRefs());

				// check if all members of this group have an authorizedObjectRef
				for (AuthorizedObject member : userSecurityGroupRef.getAuthorizedObject().getMembers()) {
					AuthorizedObjectRef memberRef = authority.getAuthorizedObjectRef(member);
					if (memberRef == null)
						throw new IllegalStateException("authority.getAuthorizedObjectRef(member) returned null! authority=" + authority + " userSecurityGroupRef=" + userSecurityGroupRef + " member=" + member);

					Set<RoleRef> memberRoleRefs = new HashSet<RoleRef>(memberRef.getRoleRefs());
					Set<Role> memberRoles = _getRolesFromRoleRefs(memberRoleRefs);
					for (Role groupRole : userSecurityGroupRoles) {
						if (!memberRoles.contains(groupRole))
							throw new IllegalStateException("memberRoleSet does not contain groupRole!!! authority=" + authority + " userSecurityGroupRef=" + userSecurityGroupRef + " member=" + member + " groupRole=" + groupRole);
					}
				}
			}

			for (RoleGroupRef roleGroupRef : authorizedObjectRef.getRoleGroupRefs()) {
				if (roleGroupRef.getAuthorizedObjectRef(authorizedObjectRef.getAuthorizedObject()) == null)
					throw new IllegalStateException("roleGroupRef does not contain authorizedObjectRef!!! authority=" + authority + " roleGroupRef=" + roleGroupRef + " authorizedObjectRef=" + authorizedObjectRef);
			}
		}

		for (RoleGroupRef roleGroupRef : authority.getRoleGroupRefs()) {
			for (AuthorizedObjectRef authorizedObjectRef : roleGroupRef.getAuthorizedObjectRefs()) {
				if (authorizedObjectRef.getRoleGroupRef(roleGroupRef.getRoleGroup()) == null)
					throw new IllegalStateException("authorizedObjectRef does not contain roleGroupRef!!! authority=" + authority + " roleGroupRef=" + roleGroupRef + " authorizedObjectRef=" + authorizedObjectRef);
			}
		}
	}

	private static int _calculateAuthorizedObjectRef_userSecurityGroupReferenceCount(PersistenceManager pm, Authority authority, AuthorizedObject authorizedObject)
	{
		int count = 0;

		for (UserSecurityGroup group : authorizedObject.getUserSecurityGroups()) {
			if (authority.getAuthorizedObjectRef(group) != null)
				count++;
		}

		return count;
	}

	private static Set<Role> _getRolesFromRoleRefs(Collection<RoleRef> roleRefs)
	{
		Set<Role> roles = new HashSet<Role>(roleRefs.size());
		for (RoleRef roleRef : roleRefs)
			roles.add(roleRef.getRole());

		return roles;
	}

	private static int _calculateRoleRef_referenceCount(PersistenceManager pm, Authority authority, AuthorizedObject authorizedObject, Role role)
	{
		int referenceCount = 0;

		{
			AuthorizedObjectRef aoref = authority.getAuthorizedObjectRef(authorizedObject);
			if (aoref != null) {
				for (RoleGroupRef roleGroupRef : aoref.getRoleGroupRefs()) {
					if (roleGroupRef.getRoleGroup().getRoles().contains(role))
						referenceCount++;
				}
			}
		}

		for (UserSecurityGroup group : authorizedObject.getUserSecurityGroups()) {
			AuthorizedObjectRef aoref = authority.getAuthorizedObjectRef(group);
			if (aoref != null) {
				for (RoleGroupRef roleGroupRef : aoref.getRoleGroupRefs()) {
					if (roleGroupRef.getRoleGroup().getRoles().contains(role))
						referenceCount++;
				}
			}
		}

		return referenceCount;
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#whoami()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public void whoami()
	{
		logger.info("******** WHOAMI: "+getPrincipal());
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getUserIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed({"org.nightlabs.jfire.security.accessRightManagement", "org.nightlabs.jfire.security.queryUsers"})
	@SuppressWarnings("unchecked")
	@Override
	public Set<UserID> getUserIDs(QueryCollection<? extends UserQuery> userQueries)
	{
		if (userQueries == null)
			return null;

		if (! User.class.isAssignableFrom(userQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ userQueries.getResultClassName());
		}

		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#setUserPassword(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void setUserPassword(String password) {
		if (password == null || "".equals(password))
			throw new IllegalArgumentException("Your password must not be empty.");
		String userID = SecurityReflector.getUserDescriptor().getUserID();
		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();

		PersistenceManager pm = createPersistenceManager();
		try {
			User user = (User) pm.getObjectById(UserID.create(organisationID, userID));
			user.getUserLocal().setPasswordPlain(password);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#storeAuthority(org.nightlabs.jfire.security.Authority, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public Authority storeAuthority(Authority authority, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			if (ASSERT_CONSISTENCY_BEFORE)
				assertConsistency(pm);

			AuthorityID authorityID = (AuthorityID) JDOHelper.getObjectId(authority);
			if (authorityID == null)
				authority.assertAuthorityNotManaged();
			else
				Authority.assertAuthorityNotManaged(pm, authorityID);

			// authorize
			Authority authorityForAuthorization = null;
			try {
				if (authorityID != null)
					authorityForAuthorization = (Authority) pm.getObjectById(authorityID);
			} catch (JDOObjectNotFoundException x) {
				// ignore silently
			}
			if (authorityForAuthorization == null)
				authorityForAuthorization = authority;

			Authority.resolveSecuringAuthority(
					pm,
					authorityForAuthorization,
					ResolveSecuringAuthorityStrategy.organisation
			).assertContainsRoleRef(
					getPrincipal(), RoleConstants.storeAuthority
			);

			Authority result = NLJDOHelper.storeJDO(pm, authority, get, fetchGroups, maxFetchDepth);

			if (ASSERT_CONSISTENCY_AFTER)
				assertConsistency(pm);

			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#assignSecuringAuthority(java.lang.Object, org.nightlabs.jfire.security.id.AuthorityID, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public void assignSecuringAuthority(Object securedObjectID, AuthorityID authorityID, boolean inherited)
	{
		PersistenceManager pm = createPersistenceManager();
		try {

			Object _securedObject = pm.getObjectById(securedObjectID);
			if (!(_securedObject instanceof SecuredObject))
				throw new IllegalArgumentException("The object identified by securedObjectID \"" + securedObjectID + "\" does not implement " + SecuredObject.class + "!");

			SecuredObject securedObject = (SecuredObject) _securedObject;

			Authority authority = authorityID == null ? null : (Authority) pm.getObjectById(authorityID);

			if (securedObject.getSecuringAuthorityTypeID() == null)
				throw new IllegalStateException("securedObject.securingAuthorityType == null :: securedObjectID=" + securedObjectID);

			if (authority != null && !Util.equals(securedObject.getSecuringAuthorityTypeID(), JDOHelper.getObjectId(authority.getAuthorityType())))
				throw new IllegalArgumentException("AuthorityType mismatch: securedObject.securingAuthorityType != authority.authorityType :: securedObjectID=" + securedObjectID + " authorityID=" + authorityID);

			// Ensure the current user is allowed to assign an authority.
			// This is controlled by the securingAuthority of the securedObject's securingAuthorityType. If this does not have
			// a securingAuthority assigned, it's controlled by the global (per organisation)
			AuthorityType securingAuthorityType = (AuthorityType) pm.getObjectById(securedObject.getSecuringAuthorityTypeID());
			if (securingAuthorityType.getSecuringAuthorityID() != null) {
				Authority securingAuthority = (Authority) pm.getObjectById(securingAuthorityType.getSecuringAuthorityID());
				securingAuthority.assertContainsRoleRef(
						getPrincipal(), RoleConstants.assignAuthority
				);
			}
			else {
				Authority.getOrganisationAuthority(pm).assertContainsRoleRef(
						getPrincipal(), RoleConstants.assignAuthority
				);
			}

			if (securedObject instanceof Inheritable) {
				Inheritable inheritableSecuredObject = (Inheritable)securedObject;
				inheritableSecuredObject.getFieldMetaData(SecuredObject.FieldName.securingAuthorityID).setValueInherited(inherited);
			}

			securedObject.setSecuringAuthorityID(authorityID);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getRoleIDs(org.nightlabs.jfire.security.id.AuthorityID)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<RoleID> getRoleIDs(AuthorityID authorityID)
	{
		return SecurityReflector.getRoleIDs(authorityID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getRoleGroupIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<RoleGroupID> getRoleGroupIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(RoleGroup.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<RoleGroupID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<RoleGroupID>(c);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public AuthorityMeta getAuthorityMeta(final AuthorityID authorityID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Authority authority = (Authority) pm.getObjectById(authorityID);
			AuthorityMeta authorityMeta = AuthorityMeta.getAuthorityMetaByAuthorityID(pm, authorityID);
			if (authorityMeta == null) {
				authorityMeta = new AuthorityMeta(authority.getOrganisationID(), authority.getAuthorityID());
				authorityMeta.setSecuringAuthorityID(authorityID);
				authorityMeta.setSecuringAuthorityTypeID(authority.getSecuringAuthorityTypeID());
				return NLJDOHelper.storeJDO(pm, authorityMeta, true, fetchGroups, maxFetchDepth);
			}
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return pm.detachCopy(authorityMeta);
		}
		catch (JDOObjectNotFoundException e)
		{
			// if no authority for the given authorityID exists return null
			return null;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getAuthorityMetas(java.util.Collection, java.lang.String[], int)
	 */
	@Override
	public Collection<AuthorityMeta> getAuthorityMetas(Collection<AuthorityMetaID> authorityMetaIDs,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, authorityMetaIDs, AuthorityMeta.class, fetchGroups, maxFetchDepth, (QueryOption)null);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#storeAuthorityMeta(org.nightlabs.jfire.security.AuthorityMeta, boolean, java.lang.String[], int)
	 */
	@Override
	public AuthorityMeta storeAuthorityMeta(AuthorityMeta authorityMeta,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, authorityMeta, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#getAuthoritiesSelfInformation(java.util.Set, java.util.Set)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<Authority> getAuthoritiesSelfInformation(Set<AuthorityID> authorityIDs, Set<AuthorizedObjectRefID> authorizedObjectRefIDs)
	{
		if (!getPrincipal().userIsOrganisation())
			throw new IllegalStateException("This method can only be called by organisations!");

		PersistenceManager pm = createPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());
			UserLocal userLocal = user.getUserLocal();

			// We do NOT detach:
			//		* Authority.roleGroupRefs
			//		* ...other roleGroup[Ref]s
			//		* UserLocal.userSecurityGroups
			// And we delete a lot of information from the detached objects further down.
			pm.getFetchPlan().setGroups(
					new String[] {
							FetchPlan.DEFAULT,
							Authority.FETCH_GROUP_AUTHORITY_TYPE, // AuthorityTypes are managed programmatically, hence the client-organisation should have the same (no need to detach details)
							Authority.FETCH_GROUP_AUTHORIZED_OBJECT_REFS,
							Authority.FETCH_GROUP_DESCRIPTION,
							Authority.FETCH_GROUP_NAME,
							UserLocal.FETCH_GROUP_USER,
//							UserLocal.FETCH_GROUP_AUTHORIZED_OBJECT_REFS, // We MUST NOT detach this, because it leaks information via OTHER authorities to all UserLocals of these other authorities.
							User.FETCH_GROUP_NAME,
							User.FETCH_GROUP_PERSON, // should be the client organisation's person that was sent during cross-organisation-registration
							User.FETCH_GROUP_USER_LOCAL,
							AuthorizedObjectRef.FETCH_GROUP_AUTHORITY,
							AuthorizedObjectRef.FETCH_GROUP_AUTHORIZED_OBJECT,
							AuthorizedObjectRef.FETCH_GROUP_ROLE_REFS,
							RoleRef.FETCH_GROUP_AUTHORITY,
							RoleRef.FETCH_GROUP_AUTHORIZED_OBJECT_REF,
							RoleRef.FETCH_GROUP_ROLE,
					}
			);
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			Collection<Authority> authorities;
			if (authorityIDs != null)
				authorities = NLJDOHelper.getObjectSet(pm, authorityIDs, Authority.class);
			else
				authorities = new HashSet<Authority>();

			if (authorizedObjectRefIDs != null) {
				List<AuthorizedObjectRef> authorizedObjectRefs = NLJDOHelper.getObjectList(pm, authorizedObjectRefIDs, AuthorizedObjectRef.class);
				for (AuthorizedObjectRef authorizedObjectRef : authorizedObjectRefs) {
					if (!userLocal.equals(authorizedObjectRef.getAuthorizedObject()))
						throw new IllegalArgumentException("The AuthorizedObjectRefID references another user: authorizedObjectRef=" + authorizedObjectRef + " currentUser=" + user);

					authorities.add(authorizedObjectRef.getAuthority());
				}
			}

			User.disableDetachUserLocalAccessRightCheck(true);
			try {
				authorities = pm.detachCopyAll(authorities);
			} finally {
				User.disableDetachUserLocalAccessRightCheck(false);
			}

			// Delete all information from the authorities that is not allowed to be seen by the other organisation.
			for (Authority authority : authorities) {
				if (!getOrganisationID().equals(authority.getOrganisationID()))
					throw new IllegalStateException("Cannot query foreign organisations' Authority! thisOrganisationID=" + getOrganisationID() + " queriedAuthority=" + authority);

				authority.removeSecretDataAfterDetachmentForSingleUser(userLocal);
			}

			return authorities;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.JFireSecurityManagerRemote#importAuthoritiesOnCrossOrganisationRegistration(org.nightlabs.jfire.crossorganisationregistrationinit.Context)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void importAuthoritiesOnCrossOrganisationRegistration(Context context)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			String emitterOrganisationID = context.getOtherOrganisationID();
			Hashtable<?, ?> initialContextProperties = getInitialContextProperties(emitterOrganisationID);

			AuthorityNotificationFilter authorityNotificationFilter = new AuthorityNotificationFilter(
					emitterOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, getOrganisationID(),
					AuthorityNotificationFilter.class.getName());
			AuthorityNotificationReceiver authorityNotificationReceiver = new AuthorityNotificationReceiver(authorityNotificationFilter);
			authorityNotificationReceiver = pm.makePersistent(authorityNotificationReceiver);

			PersistentNotificationEJBRemote persistentNotificationEJB = JFireEjb3Factory.getRemoteBean(PersistentNotificationEJBRemote.class, initialContextProperties);
			persistentNotificationEJB.storeNotificationFilter(authorityNotificationFilter, false, null, 1);

			JFireSecurityManagerRemote jfireSecurityManager = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, initialContextProperties);
			Set<AuthorityID> authorityIDs = CollectionUtil.castSet(
					jfireSecurityManager.getAuthorityIDs(emitterOrganisationID, null)
			);

			authorityNotificationReceiver.replicateAuthorities(emitterOrganisationID, authorityIDs, null);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_ServerAdmin_")
	@Override
	public void grantAllRoleGroupsInAllAuthorities(UserID userID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {

			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {

				// Query all existing Authority instances and iterate them.
				// Grant all role-groups that are declared in the Authority's AuthorityType.
				User user = (User) pm.getObjectById(userID);
				UserLocal userLocal = user.getUserLocal();

				for (Iterator<Authority> it = pm.getExtent(Authority.class).iterator(); it.hasNext(); ) {
					Authority authority = it.next();
					AuthorizedObjectRef authorizedObjectRef = authority.createAuthorizedObjectRef(userLocal);

					Set<RoleGroup> roleGroups = authority.getAuthorityType().getRoleGroups();
					for (RoleGroup roleGroup : roleGroups) {
						RoleGroupRef roleGroupRef = authority.createRoleGroupRef(roleGroup);
						authorizedObjectRef.addRoleGroupRef(roleGroupRef);
					}
				}

				// Finally, we flush the security cache.
				JFireServerManager jfsm = getJFireServerManager();
				try {
					jfsm.jfireSecurity_flushCache(userID);
				} finally {
					jfsm.close();
				}

				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}

			// The JEE server (more precisely JAAS) manages its own cache that would be invalidated only after a delay
			// of some minutes (usually 5 minutes). Since this method is not called often but rather
			// an emergency tool to reverse an accidental lock-out, we flush the JAAS-cache immediately.
			try {
				InitialContext initialContext = new InitialContext();
				J2EEAdapter j2eeAdapter = (J2EEAdapter) initialContext.lookup(J2EEAdapter.JNDI_NAME);
				// TODO: this line should be removed to avoid the runtime exception caused
				// take a look at https://www.jfire.org/modules/bugs/view.php?id=1337
				j2eeAdapter.flushAuthenticationCache();
			} catch (Exception e) {
				logger.warn("grantAllRoleGroupsInAllAuthorities: Flushing the JEE-server's authentication cache failed.", e);
			}

		} finally {
			pm.close();
		}
	}
}
