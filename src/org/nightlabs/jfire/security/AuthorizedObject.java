package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.Collection;

/**
 * Subclasses of this abstract class can get access rights ({@link RoleGroup}s) assigned within the scope of an {@link Authority}.
 * Therefore, the {@link Authority} manages one {@link AuthorizedObjectRef} for each <code>AuthorizedObject</code> and assigns {@link RoleGroupRef}s
 * (which implicitely assign {@link RoleRef}s).
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public abstract class AuthorizedObject
implements IAuthorizedObject, Serializable
{
	private static final long serialVersionUID = 20091216L;
	
	public static final String FETCH_GROUP_NAME = "AuthorizedObject.name";
	public static final String FETCH_GROUP_DESCRIPTION = "AuthorizedObject.description";

	public abstract String getOrganisationID();

	public abstract String getName();
	public abstract void setName(String name);

	public abstract String getDescription();
	public abstract void setDescription(String description);

	public abstract Collection<? extends AuthorizedObjectRef> getAuthorizedObjectRefs();

	protected abstract void _addAuthorizedObjectRef(AuthorizedObjectRef userRef);
	protected abstract void _removeAuthorizedObjectRef(AuthorizedObjectRef userRef);
	public abstract AuthorizedObjectRef getAuthorizedObjectRef(String authorityID);
	protected abstract void _addUserSecurityGroup(UserSecurityGroup userSecurityGroup);
	protected abstract void _removeUserSecurityGroup(UserSecurityGroup userSecurityGroup);
	public abstract Collection<UserSecurityGroup> getUserSecurityGroups();

}
