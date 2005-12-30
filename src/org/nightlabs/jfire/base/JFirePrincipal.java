/*
 * Created on Mar 23, 2004
 */
package org.nightlabs.jfire.base;

import java.security.Principal;

import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.security.RoleSet;


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
