package org.nightlabs.jfire.security;

/**
 * Determines how to proceed if a {@link SecuredObject} has no <code>securingAuthorityID</code> assigned.
 * If {@link SecuredObject#getSecuringAuthorityID()} returns <code>null</code>, there are the following possibilities:
 * <ul>
 * <li>{@link #organisation}: Use the organisation's {@link Authority} (see {@link Authority#AUTHORITY_ID_ORGANISATION}) and check access rights within it.</li>
 * <li>{@link #allow}: Simply grant access. If an EJB method has already been tagged with the appropriate role-identifier, there is no need to check again.
 *		Therefore it is recommended, to use this option in order to reduce unnecessary work and thus increase performance.</li>
 * </ul>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public enum ResolveSecuringAuthorityStrategy
{
	/**
	 * Resolve the organisation's {@link Authority}.
	 */
	organisation,

	/**
	 * Resolve a dummy {@link Authority} which allows everything to everybody.
	 * <p>
	 * The methods {@link Authority#containsRoleRef(org.nightlabs.jfire.security.id.UserID, org.nightlabs.jfire.security.id.RoleID)} and
	 * {@link Authority#containsRoleRef(org.nightlabs.jfire.base.JFireBasePrincipal, org.nightlabs.jfire.security.id.RoleID)} return
	 * <code>true</code>, no matter what the parameters are.
	 * </p>
	 */
	allow
}
