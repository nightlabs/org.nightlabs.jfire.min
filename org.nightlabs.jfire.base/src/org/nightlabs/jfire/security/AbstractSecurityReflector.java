package org.nightlabs.jfire.security;

import java.io.Serializable;

import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class AbstractSecurityReflector implements ISecurityReflector, Serializable {

	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.ISecurityReflector#authorityContainsRoleRef(org.nightlabs.jfire.security.id.AuthorityID, org.nightlabs.jfire.security.id.RoleID)
	 */
	@Override
	public boolean authorityContainsRoleRef(AuthorityID authorityID, RoleID roleID) throws NoUserException
	{
		if (roleID == null)
			throw new IllegalArgumentException("roleID must not be null!");

		UserDescriptor userDescriptor = getUserDescriptor();

		if (authorityID == null)
			authorityID = AuthorityID.create(userDescriptor.getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION);
		else {
			if (!authorityID.organisationID.equals(userDescriptor.getOrganisationID()))
				throw new IllegalArgumentException("authorityID.organisationID != user.organisationID");
		}

		return getRoleIDs(authorityID).contains(roleID);
	}
}
