package org.nightlabs.jfire.security;

import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;

import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;

/**
 * An implementation of <code>ISecurityReflector</code> provides API to find out who the current user is
 * (see {@link #getUserDescriptor()}). The purpose of this is (1) to be able to find out at every code location
 * who the current user is and (2) to have the same API for this no matter where the
 * code is executed - i.e. the JFire server, the JFire RCP client or another client.
 * <p>
 * Additionally, the <code>SecurityReflector</code> API can be used to obtain the initial context properties needed to obtain
 * an EJB (see {@link #getInitialContextProperties()}) or to query the presence of access rights
 * (see {@link #authorityContainsRoleRef(AuthorityID, RoleID)}).
 * </p>
 * <p>
 * Use {@link GlobalSecurityReflector} to access a VM-wide security reflector shared instance.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public interface ISecurityReflector {
	/**
	 * Get an instance of {@link InitialContext} with the properties that are created by {@link #getInitialContextProperties()}.
	 * This <code>InitialContext</code> can be used directly to communicate with the server as the current user. Authentication
	 * is done implicitly.
	 * <p>
	 * This method never returns <code>null</code>.
	 * </p>
	 *
	 * @return an instance of <code>InitialContext</code> ready to communicate with the server as the current user.
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	InitialContext createInitialContext() throws NoUserException;

	/**
	 * Get a {@link Properties} instance which can be used for creating an {@link InitialContext} in order
	 * to communicate with a JFire server as the current user. This might be <code>null</code>, if the current
	 * situation does not require any <code>Properties</code> to be passed when creating an EJB.
	 * <p>
	 * Since authentication is bound in JFire to an JNDI context, you can directly use the returned <code>Properties</code>
	 * for creation of an EJB (via its generated Util class) - authentication is done implicitly.
	 * </p>
	 *
	 * @return an instance of <code>Properties</code> ready to create an {@link InitialContext} or <code>null</code>, if
	 *		there is no <code>Properties</code> instance needed in the current situation (e.g. inside the server, this is usually the case).
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	Properties getInitialContextProperties() throws NoUserException;

	/**
	 * Get a descriptor telling who's the current user calling this method.
	 *
	 * @return an instance of <code>UserDescriptor</code> describing who the current user is.
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	UserDescriptor getUserDescriptor() throws NoUserException;
	
	/**
	 * Get a credential object for the current user. The return value depends on authentication type.
	 * For example, it will return {@link String} password as plain text for basic authentication. 
	 * For other authentication types the return value could be a digest of some kind or a session key
	 * or whatever else this authentication type supports. <code>null</code> also could be returned
	 * if there's no possibility to get any credential so do not forget to check the returned value.
	 * 
	 * @return credential of the current user or <code>null</code> if there's no credential or no possibility to get one
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	Object getCredential() throws NoUserException;

	/**
	 * Get a {@link Set} containing the object-ids of all {@link Role}s that are granted to the current
	 * user in specified authority.
	 *
	 * @param authorityID the identifier of the {@link Authority} within which to check for the presence of a role-reference or <code>null</code> to point to the organisation-authority (see {@link Authority#AUTHORITY_ID_ORGANISATION}).
	 * @return all permissions granted to the current user in the specified authority.
	 * @throws NoUserException if there is no user currently authenticated.
	 */
	Set<RoleID> getRoleIDs(AuthorityID authorityID) throws NoUserException;
	
	/**
	 * Find out whether the current user has a certain access right (i.e. a role) in a certain authority.
	 *
	 * @param authorityID the identifier of the {@link Authority} within which to check for the presence of a role-reference or <code>null</code> to point to the organisation-authority (see {@link Authority#AUTHORITY_ID_ORGANISATION}).
	 * @param roleID the identifier of the {@link Role} for which a reference should be searched.
	 * @return <code>true</code> if the role is present for the current user within the specified authority; <code>false</code> otherwise.
	 * @throws NoUserException if there is currently no user authenticated.
	 */
	boolean authorityContainsRoleRef(AuthorityID authorityID, RoleID roleID) throws NoUserException;
}
