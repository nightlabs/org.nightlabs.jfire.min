package org.nightlabs.jfire.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserID;

/**
 * Exception indicating that an action cannot be performed because certain access rights
 * (i.e. {@link Role}s) are not assigned to the acting user within the scope of a certain
 * {@link Authority}.
 * <p>
 * This exception can be thrown
 * in the client (e.g. in deferred exceptions - like the access to the method {@link User#getUserLocal()})
 * as well as in the server.
 * </p>
 * <p>
 * In the JFire RCP client, there is a special exception-handler understanding this exception:
 * <code>org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionHandler</code>
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class MissingRoleException extends SecurityException
{
	private static final long serialVersionUID = 1L;

	public static final String[] FETCH_GROUPS_AUTHORITY = new String[] {
		FetchPlan.DEFAULT,
		Authority.FETCH_GROUP_NAME,
		Authority.FETCH_GROUP_DESCRIPTION,
		Authority.FETCH_GROUP_AUTHORITY_TYPE
	};

	public static final String[] FETCH_GROUPS_ROLE = new String[] {
		FetchPlan.DEFAULT,
		Role.FETCH_GROUP_ROLE_GROUPS,
		RoleGroup.FETCH_GROUP_NAME,
		RoleGroup.FETCH_GROUP_DESCRIPTION
	};

	private UserID userID;
	private AuthorityID authorityID;
	private Authority authority;
	private Set<RoleID> requiredRoleIDs;
	private Set<Role> requiredRoles;

	private static String requiredRoleIDSetToString(Set<RoleID> requiredRoleIDs)
	{
		StringBuilder sb = new StringBuilder();
		for (RoleID roleID : requiredRoleIDs) {
			if (sb.length() != 0)
				sb.append(", ");

			sb.append('"');
			sb.append(roleID.roleID);
			sb.append('"');
		}
		return sb.toString();
	}

	/**
	 * Construct an instance with one single required role.
	 * This is a convenience constructor delegating to
	 * {@link #MissingRoleException(javax.jdo.PersistenceManager, UserID, AuthorityID, RoleID)}.
	 *
	 * @param userID the identifier of the currently logged-in user.
	 * @param authorityID the identifier of the {@link Authority} in which the role is required but missing.
	 * @param requiredRoleID the identifier of the required (but missing) role.
	 */
	public MissingRoleException(UserID userID, AuthorityID authorityID, RoleID requiredRoleID) {
		this((javax.jdo.PersistenceManager) null, userID, authorityID, requiredRoleID);
	}

	/**
	 * Construct an instance with one single required role.
	 * This is a convenience constructor delegating to
	 * {@link #MissingRoleException(javax.jdo.PersistenceManager, UserID, AuthorityID, Set)}.
	 *
	 * @param pm the gate to the datastore or <code>null</code> (e.g. when throwing this exception in the client).
	 * @param userID the identifier of the currently logged-in user.
	 * @param authorityID the identifier of the {@link Authority} in which the role is required but missing.
	 * @param requiredRoleID the identifier of the required (but missing) role.
	 */
	public MissingRoleException(javax.jdo.PersistenceManager pm, UserID userID, AuthorityID authorityID, RoleID requiredRoleID) {
		this(pm, userID, authorityID, Collections.singleton(requiredRoleID));
	}
	
	/**
	 * Construct an instance one single required role and an additional message text
	 * which is appended after the generated message.
	 * This is a convenience constructor delegating to
	 * {@link #MissingRoleException(javax.jdo.PersistenceManager, UserID, AuthorityID, RoleID, String)}.
	 *
	 * @param userID the identifier of the currently logged-in user.
	 * @param authorityID the identifier of the {@link Authority} in which the role is required but missing.
	 * @param requiredRoleID the identifier of the required (but missing) role.
	 * @param additionalMessage And additional message or <code>null</code>. If this is not <code>null</code>, the text is directly
	 *		(without space!) appended to the auto-generated exception-message-text.
	 */
	public MissingRoleException(UserID userID, AuthorityID authorityID, RoleID requiredRoleID, String additionalMessage) {
		this((javax.jdo.PersistenceManager) null, userID, authorityID, requiredRoleID, additionalMessage);
	}

	/**
	 * Construct an instance one single required role and an additional message text
	 * which is appended after the generated message.
	 * This is a convenience constructor delegating to
	 * {@link #MissingRoleException(javax.jdo.PersistenceManager, UserID, AuthorityID, Set, String)}.
	 *
	 * @param pm the gate to the datastore or <code>null</code> (e.g. when throwing this exception in the client).
	 * @param userID the identifier of the currently logged-in user.
	 * @param authorityID the identifier of the {@link Authority} in which the role is required but missing.
	 * @param requiredRoleID the identifier of the required (but missing) role.
	 * @param additionalMessage And additional message or <code>null</code>. If this is not <code>null</code>, the text is directly
	 *		(without space!) appended to the auto-generated exception-message-text.
	 */
	public MissingRoleException(javax.jdo.PersistenceManager pm, UserID userID, AuthorityID authorityID, RoleID requiredRoleID, String additionalMessage) {
		this(pm, userID, authorityID, Collections.singleton(requiredRoleID), additionalMessage);
	}

	/**
	 * Construct an instance with multiple required roles.
	 * This is a convenience constructor delegating to
	 * {@link #MissingRoleException(javax.jdo.PersistenceManager, UserID, AuthorityID, Set)}.
	 * 
	 * @param userID the identifier of the currently logged-in user.
	 * @param authorityID the identifier of the {@link Authority} in which the roles are required but all of them missing.
	 * @param requiredRoleIDs the identifiers of the required roles. The user would need at least one of these roles to be allowed
	 *		to perform the action (and thus prevent this exception from being thrown).
	 */
	public MissingRoleException(UserID userID, AuthorityID authorityID, Set<RoleID> requiredRoleIDs) {
		this((javax.jdo.PersistenceManager) null, userID, authorityID, requiredRoleIDs);
	}

	/**
	 * Construct an instance with multiple required roles.
	 * This is a convenience constructor delegating to
	 * {@link #MissingRoleException(javax.jdo.PersistenceManager, UserID, AuthorityID, Set, String)}.
	 *
	 * @param pm the gate to the datastore or <code>null</code> (e.g. when throwing this exception in the client).
	 * @param userID the identifier of the currently logged-in user.
	 * @param authorityID the identifier of the {@link Authority} in which the roles are required but all of them missing.
	 * @param requiredRoleIDs the identifiers of the required roles. The user would need at least one of these roles to be allowed
	 *		to perform the action (and thus prevent this exception from being thrown).
	 */
	public MissingRoleException(javax.jdo.PersistenceManager pm, UserID userID, AuthorityID authorityID, Set<RoleID> requiredRoleIDs) {
		this(pm, userID, authorityID, requiredRoleIDs, null);
	}

	/**
	 * Construct an instance with multiple required roles and an additional message text
	 * which is appended after the generated message.
	 * Convenience constructor delegating to 
	 * {@link #MissingRoleException(javax.jdo.PersistenceManager, UserID, AuthorityID, Set, String)}
	 * with <code>pm == null</code>.
	 *
	 * @param userID the identifier of the currently logged-in user.
	 * @param authorityID the identifier of the {@link Authority} in which the roles are required but all of them missing.
	 * @param requiredRoleIDs the identifiers of the required roles. The user would need at least one of these roles to be allowed
	 *		to perform the action (and thus prevent this exception from being thrown).
	 * @param additionalMessage And additional message or <code>null</code>. If this is not <code>null</code>, the text is directly
	 *		(without space!) appended to the auto-generated exception-message-text.
	 */
	public MissingRoleException(UserID userID, AuthorityID authorityID, Set<RoleID> requiredRoleIDs, String additionalMessage)
	{
		this((javax.jdo.PersistenceManager) null, userID, authorityID, requiredRoleIDs, additionalMessage);
	}

	/**
	 * Construct an instance with multiple required roles and an additional message text
	 * which is appended after the generated message.
	 *
	 * @param pm the gate to the datastore or <code>null</code> (e.g. when throwing this exception in the client).
	 * @param userID the identifier of the currently logged-in user.
	 * @param authorityID the identifier of the {@link Authority} in which the roles are required but all of them missing.
	 * @param requiredRoleIDs the identifiers of the required roles. The user would need at least one of these roles to be allowed
	 *		to perform the action (and thus prevent this exception from being thrown).
	 * @param additionalMessage And additional message or <code>null</code>. If this is not <code>null</code>, the text is directly
	 *		(without space!) appended to the auto-generated exception-message-text.
	 */
	public MissingRoleException(javax.jdo.PersistenceManager pm, UserID userID, AuthorityID authorityID, Set<RoleID> requiredRoleIDs, String additionalMessage) {
		super(
				"The user \"" + userID.userID
				+ User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + userID.organisationID
				+ "\" does not have any of the required roles [" + requiredRoleIDSetToString(requiredRoleIDs)
				+ "] within the authority \"" + authorityID.authorityID + "\"!"
				+ (additionalMessage == null ? "" : additionalMessage)
		);

		this.userID = userID;
		this.authorityID = authorityID;
		this.requiredRoleIDs = Collections.unmodifiableSet(new HashSet<RoleID>(requiredRoleIDs));

		if (pm != null) {
			FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
			try {
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
	
				pm.getFetchPlan().setGroups(MissingRoleException.FETCH_GROUPS_AUTHORITY);
				Authority authority = (Authority) pm.getObjectById(this.authorityID);
				this.authority = pm.detachCopy(authority);
	
				pm.getFetchPlan().setGroups(MissingRoleException.FETCH_GROUPS_ROLE);
	
				Set<Role> r = NLJDOHelper.getObjectSet(pm, this.requiredRoleIDs, Role.class);
				requiredRoles = new HashSet<Role>(pm.detachCopyAll(r));

				populateRoleSetWithSpecialRoles(this.requiredRoleIDs, requiredRoles);

				requiredRoles = Collections.unmodifiableSet(requiredRoles);
			} finally {
				NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
			}
		}
	}

	protected static final void populateRoleSetWithSpecialRoles(Set<RoleID> roleIDs, Set<Role> roles)
	{
		// Handle special roles that are not persisted.
		// => role _ServerAdmin_
		if (roleIDs.contains(RoleConstants.serverAdmin)) {
			Role role = new Role(RoleConstants.serverAdmin.roleID);
			RoleGroup roleGroup = new RoleGroup(RoleConstants.serverAdmin.roleID);
			roleGroup.addRole(role);
			roleGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Server administrator");
			roleGroup.getName().setText(Locale.GERMAN.getLanguage(), "Server-Administrator");
			roleGroup.getDescription().setText(Locale.ENGLISH.getLanguage(), "Allows to perform server administration tasks.");
			roleGroup.getDescription().setText(Locale.GERMAN.getLanguage(), "Erlaubt die Durchführung von Server-Administrations-Aufgaben.");
			roles.add(role);
		}

		// => role _System_
		if (roleIDs.contains(RoleConstants.system)) {
			Role role = new Role(RoleConstants.system.roleID);
			RoleGroup roleGroup = new RoleGroup(RoleConstants.system.roleID);
			roleGroup.addRole(role);
			roleGroup.getName().setText(Locale.ENGLISH.getLanguage(), "System");
			roleGroup.getDescription().setText(Locale.ENGLISH.getLanguage(), "Only the system itself has this right. No user can get the access right \"System\" assigned.");
			roleGroup.getDescription().setText(Locale.GERMAN.getLanguage(), "Nur das System selbst hat dieses Recht. Das Recht \"System\" kann keinem Benutzer zugewiesen werden.");
			roles.add(role);
		}

		// => role _Guest_
		if (roleIDs.contains(RoleConstants.guest)) {
			Role role = new Role(RoleConstants.guest.roleID);
			RoleGroup roleGroup = new RoleGroup(RoleConstants.guest.roleID);
			roleGroup.addRole(role);
			roleGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Guest");
			roleGroup.getName().setText(Locale.GERMAN.getLanguage(), "Gast");
			roleGroup.getDescription().setText(Locale.ENGLISH.getLanguage(), "Every authenticated user has this right.");
			roleGroup.getDescription().setText(Locale.GERMAN.getLanguage(), "Jeder authentifizierte Benutzer hat dieses Recht.");
			roles.add(role);
		}
	}

	/**
	 * Get the identifier of the user who was currently logged-in when the exception was thrown.
	 * @return the acting user's identifier.
	 */
	public UserID getUserID() {
		return userID;
	}

	/**
	 * Get the object-id of the authority within in which the roles are required but all of them missing.
	 * @return the object-id of the authority in which roles are not assigned to the acting user.
	 */
	public AuthorityID getAuthorityID() {
		return authorityID;
	}

	/**
	 * Get the authority within in which the roles are required but all of them missing.
	 * If this exception has been created without a <code>PersistenceManager</code> being present,
	 * this property is <code>null</code>.
	 *
	 * @return the authority or <code>null</code>.
	 */
	public Authority getAuthority() {
		return authority;
	}

	/**
	 * Get the object-ids of the required roles that allow the user to execute the denied method. At least one of these required roles
	 * must be present for the user to be granted the right to do what he just tried in vain.
	 *
	 * @return the object-ids of the required roles.
	 * @see #getRequiredRoles()
	 */
	public Set<RoleID> getRequiredRoleIDs() {
		return requiredRoleIDs;
	}

	/**
	 * Get the requiredRoles that allow the user to execute the denied method. At least one of these requiredRoles must be present
	 * for the user to be granted the right to do what he just tried in vain.
	 * <p>
	 * This methdod returns <code>null</code>, if this exception has been created without a <code>PersistenceManager</code>
	 * (e.g. on the client-side).
	 * </p>
	 *
	 * @return the required roles or <code>null</code>.
	 * @see #getRequiredRoleIDs()
	 */
	public Set<Role> getRequiredRoles() {
		return requiredRoles;
	}
}
