package org.nightlabs.jfire.security;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.AuthorizedObjectRefID;
import org.nightlabs.jfire.security.id.PendingUserID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.search.UserQuery;
import org.nightlabs.jfire.timer.id.TaskID;

@Remote
public interface JFireSecurityManagerRemote
{
	String ping(String message);

	void initialise() throws Exception;

	void checkConsistency(TaskID taskID) throws Exception;

	/**
	 * Create a new user-security-group or change an existing one.
	 *
	 * @param userSecurityGroup the group to save.
	 * @param get Whether to return the newly saved user.
	 * @param fetchGroups The fetch-groups to detach the returned User with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 * @ejb.transaction type="Required"
	 */
	UserSecurityGroup storeUserSecurityGroup(
			UserSecurityGroup userSecurityGroup, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Create a new pending user or change an existing one.
	 *
	 * @param user the pending user to save.
	 * @param get Whether to return the newly saved pending user.
	 * @param fetchGroups The fetch-groups to detach the returned PendingUser with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 */
	PendingUser storePendingUser(PendingUser user, boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Create a new pending user or change an existing one.
	 *
	 * @param user the pending user to save.
	 * @param get Whether to return the newly saved user.
	 * @param fetchGroups The fetch-groups to detach the returned User with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 */
	User storePendingUserAsUser(PendingUser user, boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Create a new user or change an existing one.
	 *
	 * @param user the user to save.
	 * @param newPassword the password for the user. This might be <code>null</code>.
	 *		If a new user is created without password,
	 *		it cannot login, since the presence of a password is forced by the login-module.
	 *		Note, that this parameter is ignored, if the given <code>user</code> has a {@link UserLocal} assigned or if
	 *		it is an instance of {@link UserGroup}.
	 *		In this case, the property {@link UserLocal#getNewPassword()} is used instead. In other words, this field
	 *		is meant to be used to create a new <code>User</code> with an initial password.
	 * @param get Whether to return the newly saved user.
	 * @param fetchGroups The fetch-groups to detach the returned User with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 */
	User storeUser(User user, String newPassword, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get {@link Role} instances for the given {@link RoleID}s. This method is meant to be used when
	 * access to an EJB has been denied and the {@link RoleGroup}s that would allow the action to be performed
	 * are shown in an error dialog.
	 * <p>
	 * This method can be called by everyone (who is logged in), because it does not reveal any confidential data
	 * and is meant to be used in exactly those cases where access is denied.
	 * </p>
	 */
	Set<Role> getRolesForRequiredRoleIDs(Set<RoleID> roleIDs);

	/**
	 * @param userType one of User.USERTYPE* or <code>null</code> to get all
	 * @param organisationID an organisationID in order to filter for it or <code>null</code> to get all.
	 * @return the unique IDs of those users that match the given criteria.
	 */
	Set<UserID> getUserIDs(String organisationID, Set<String> userTypes);

	/**
	 * Returns a Collection of {@link User}s corresponding to the given set of {@link UserID}s.
	 * <p>
	 * This method can be called by every logged-in user, because (1) it is not possible to retrieve
	 * access right information (i.e the {@link UserLocal} instance) this way if the
	 * {@link RoleConstants#accessRightManagement} role is not present, (2) it is necessary to
	 * know the user-id before hand (and querying is only allowed with
	 * the {@link RoleConstants#queryUsers} role) and (3) it is necessary and possible to
	 * obtain {@link User} instances indirectly anyway (e.g. because it's the contact person
	 * attached to an invoice).
	 * </p>
	 *
	 * @param userIDs the {@link UserID}s for which to retrieve the {@link User}s
	 * @param fetchGroups the FetchGroups for the detached Users
	 * @param maxFetchDepth the maximum fetch depth of the detached Users.
	 * @return a Collection of {@link User}s corresponding to the given set of {@link UserID}s.
	 */
	List<User> getUsers(Collection<UserID> userIDs, String[] fetchGroups,
			int maxFetchDepth);

	Set<UserSecurityGroupID> getUserSecurityGroupIDs();

	/**
	 * Returns a Collection of {@link User}s corresponding to the given set of {@link UserID}s.
	 */
	List<UserSecurityGroup> getUserSecurityGroups(
			Collection<UserSecurityGroupID> userSecurityGroupIDs,
			String[] fetchGroups, int maxFetchDepth);

	List<RoleGroupIDSetCarrier> getRoleGroupIDSetCarriers(
			Collection<? extends AuthorizedObjectID> authorizedObjectIDs,
			AuthorityID authorityID);

	/**
	 * Get the {@link RoleGroupIDSetCarrier} for a single {@link User} within the scope of a single {@link Authority}.
	 * Since this method is used by every user to query its own security configuration, it is allowed to be executed by everyone,
	 * but only if the given <code>userID</code> matches the currently logged-in user. Additionally, every user is allowed to query
	 * this information for user-groups in which he is a member.
	 *
	 * @param userID the identifier of the user to query data for.
	 * @param authorityID the identifier of the {@link Authority} defining the scope in which to query access rights.
	 */
	RoleGroupIDSetCarrier getRoleGroupIDSetCarrier(
			AuthorizedObjectID authorizedObjectID, AuthorityID authorityID);

	/**
	 * @param authorityID identifier of the {@link Authority} for which to query the access rights configuration
	 **/
	List<RoleGroupIDSetCarrier> getRoleGroupIDSetCarriers(
			AuthorityID authorityID);

	Collection<RoleGroup> getRoleGroups(Collection<RoleGroupID> roleGroupIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the {@link AuthorityID}s for a given {@link AuthorityType} specified by its object-id.
	 * Since the {@link AuthorityID}s do not contain any security-relevant data, this method can be
	 * executed by everyone.
	 */
	Set<AuthorityID> getAuthorityIDs(String organisationID,
			AuthorityTypeID authorityTypeID);

	List<Authority> getAuthorities(Collection<AuthorityID> authorityIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the {@link AuthorityTypeID}s existing in the organisation. Since the {@link AuthorityType}s are objects defined by the
	 * programmers, they are not secret and thus everyone is allowed to execute this method.
	 */
	Set<AuthorityTypeID> getAuthorityTypeIDs();

	/**
	 * Get the {@link AuthorityType}s specified by the given IDs. Since the {@link AuthorityType}s are objects defined by the
	 * programmers, they are not secret and thus everyone is allowed to execute this method.
	 */
	List<AuthorityType> getAuthorityTypes(
			Collection<AuthorityTypeID> authorityTypeIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Check if a user ID exists. This method is used to check the ID while creating a new user.
	 * @deprecated Use {@link #isUserIDAlreadyRegistered(UserID)} instead
	 */
	@Deprecated
	boolean userIDAlreadyRegistered(UserID userID);

	/**
	 * Check if a user ID exists. This method is used to check the ID while creating a new user.
	 */
	boolean isUserIDAlreadyRegistered(UserID userID);

	/**
	 * Check if a pending user ID exists. This method is used to check the ID while creating a new user.
	 */
	boolean isPendingUserIDAlreadyRegistered(PendingUserID userID);

	/**
	 * Check if the given ids are currently available for a new user to create.
	 * This also checks for existence of pending users.
	 * @param organisationID The user's organisation id
	 * @param userID The user id
	 * @return <code>true</code> if the user id is available to create either a user
	 * 		or a pending user.
	 */
	boolean isUserIDAvailable(String organisationID, String userID);

	Set<AuthorizedObjectID> getAuthorizedObjectIDs();

	List<AuthorizedObject> getAuthorizedObjects(
			Collection<AuthorizedObjectID> authorizedObjectIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Set which {@link RoleGroup}s are assigned to a certain {@link User} within the scope of a certain {@link Authority}.
	 * <p>
	 * The assignment of {@link RoleGroup}s to {@link User}s is managed by {@link RoleGroupRef} and {@link AuthorizedObjectRef} instances
	 * which live within an {@link Authority}. This method removes the {@link AuthorizedObjectRef} (and with it all assignments), if
	 * the given <code>roleGroupIDs</code> argument is <code>null</code>. If the <code>roleGroupIDs</code> argument is not <code>null</code>,
	 * a {@link AuthorizedObjectRef} instance is created - even if the <code>roleGroupIDs</code> is an empty set.
	 * </p>
	 *
	 * @param userID the user-id. Must not be <code>null</code>.
	 * @param authorityID the authority-id. Must not be <code>null</code>.
	 * @param roleGroupIDs the role-group-ids that should be assigned to the specified user within the scope of the specified
	 *		authority. If this is <code>null</code>, the {@link AuthorizedObjectRef} of the specified user will be removed from the {@link Authority}.
	 *		If this is not <code>null</code>, a <code>AuthorizedObjectRef</code> is created (if not yet existing).
	 */
	void setGrantedRoleGroups(AuthorizedObjectID authorizedObjectID,
			AuthorityID authorityID, Set<RoleGroupID> roleGroupIDs);

	/**
	 * @see #setMembersOfUserSecurityGroup(UserSecurityGroupID, Set)
	 */
	void setUserSecurityGroupsOfMember(
			Set<UserSecurityGroupID> userSecurityGroupIDs,
			AuthorizedObjectID memberAuthorizedObjectID);

	/**
	 * @see #setUserSecurityGroupsOfMember(Set, AuthorizedObjectID)
	 */
	void setMembersOfUserSecurityGroup(UserSecurityGroupID userSecurityGroupID,
			Set<? extends AuthorizedObjectID> memberAuthorizedObjectIDs);

	void whoami();

	Set<UserID> getUserIDs(QueryCollection<? extends UserQuery> userQueries);

	/**
	 * Sets the password of the user that is calling this method to the given password.
	 */
	void setUserPassword(String password);

	Authority storeAuthority(Authority authority, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @param securedObjectID the object-id of an object implementing {@link SecuredObject}.
	 * @param authorityID the object-id of the {@link Authority} that shall be assigned to the object specified by <code>securedObjectID</code>.
	 * @param inherited set whether the field is inherited or not.
	 */
	void assignSecuringAuthority(Object securedObjectID,
			AuthorityID authorityID, boolean inherited);

	/**
	 * Get a {@link Set} containing the object-ids of all {@link Role}s that are granted to the current
	 * user in specified authority. Since this is a self-information, every user is allowed to execute this method.
	 */
	Set<RoleID> getRoleIDs(AuthorityID authorityID);

	/**
	 * Get all {@link RoleGroupID}s known in the current organisation. Since the {@link RoleGroup}s are objects defined by the
	 * programmers, they are not secret and thus everyone is allowed to execute this method.
	 */
	Set<RoleGroupID> getRoleGroupIDs();

	Collection<Authority> getAuthoritiesSelfInformation(
			Set<AuthorityID> authorityIDs,
			Set<AuthorizedObjectRefID> authorizedObjectRefIDs);

	void importAuthoritiesOnCrossOrganisationRegistration(Context context)
			throws Exception;
}