/*
 * Created on Aug 19, 2005
 */
package org.nightlabs.ipanema.servermanager.config;

import java.util.Set;

public class RootOrganisationCf extends OrganisationCf
{
	private ServerCf server;

	public RootOrganisationCf()
	{
	}

	public RootOrganisationCf(String _organisationID, String _organisationName, ServerCf server)
	{
		super(_organisationID, _organisationName);
		setServer(server);
	}

	public ServerCf getServer()
	{
		return server;
	}
	public void setServer(ServerCf server)
	{
		this.server = server;
	}

	public void setServerAdmins(Set _serverAdmins)
	{
		super.setServerAdmins(null);
	}
	public Set getServerAdmins()
	{
		return null;
	}
	public void addServerAdmin(String userID)
	{
	}
}
