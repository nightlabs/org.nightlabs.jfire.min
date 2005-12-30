/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.ipanema.servermanager.j2ee;

import org.jboss.security.SecurityAssociation;
import org.nightlabs.ipanema.base.JFireServerLoginModule;



/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class SecurityReflectorJBoss extends SecurityReflector
{
	/**
	 * @see org.nightlabs.ipanema.servermanager.j2ee.SecurityReflector#whoAmI()
	 */
	public UserDescriptor whoAmI()
	{
		String principalName = SecurityAssociation.getPrincipal().getName();
		String[] parts = JFireServerLoginModule.SPLIT_USERNAME_PATTERN.split(principalName);
		if (parts.length != 2 && parts.length != 3)
			throw new IllegalStateException("principal.name '"+principalName+"' does not match format 'userID@organisationID/sessionID' (where sessionID is optional)!");

		return new UserDescriptor(
				parts[1], parts[0],
				(parts.length < 3 || "".equals(parts[2])) ? (parts[1] + '_' + parts[0]) : parts[2]);
	}

}
