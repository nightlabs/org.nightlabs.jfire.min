/*
 * Created on Mar 23, 2004
 */
package org.nightlabs.ipanema.base;

import java.security.Principal;

import org.nightlabs.ipanema.base.JFireBasePrincipal;
import org.nightlabs.ipanema.security.RoleSet;


/**
 * @author nick@nightlabs.de
 */
public class JFirePrincipal
	extends JFireBasePrincipal
	implements Principal
{
	protected Lookup lookup;

	public JFirePrincipal(String _userID, String _organisationID, String _sessionID, boolean _userIsOrganisation, Lookup _lookup, RoleSet _roleSet)
	{
		super(_userID, _organisationID, _sessionID, _userIsOrganisation, _roleSet);

		if (_lookup == null)
			throw new NullPointerException("lookup must not be null!");
		this.lookup = _lookup;
		this.lookup.setJFirePrincipal(this);
	}

	public Lookup getLookup()
	{
		return lookup;
	}

}
