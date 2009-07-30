package org.nightlabs.jfire.web.admin.serverinit;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.servermanager.config.RootOrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;

/**
 * A custom bean is needed as the RootOrganisationCf has an inner member ServerCf
 * which cannot be handled in a generic way.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RootOrganisationBean
{
	private String organisationID;
	private String organisationName;
	private String serverID;
	private String serverName;
	private String j2eeServerType;
	private Map<String, String> protocol2initialContextURL = new HashMap<String, String>();
//	private String initialContextURL_jnp;
//	private String initialContextURL_https;

	/**
	 * Get the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * Set the organisationID.
	 * @param organisationID the organisationID to set
	 */
	public void setOrganisationID(String organisationID)
	{
		this.organisationID = organisationID;
	}
	/**
	 * Get the organisationName.
	 * @return the organisationName
	 */
	public String getOrganisationName()
	{
		return organisationName;
	}
	/**
	 * Set the organisationName.
	 * @param organisationName the organisationName to set
	 */
	public void setOrganisationName(String organisationName)
	{
		this.organisationName = organisationName;
	}
	/**
	 * Get the serverID.
	 * @return the serverID
	 */
	public String getServerID()
	{
		return serverID;
	}
	/**
	 * Set the serverID.
	 * @param serverID the serverID to set
	 */
	public void setServerID(String serverID)
	{
		this.serverID = serverID;
	}
	/**
	 * Get the serverName.
	 * @return the serverName
	 */
	public String getServerName()
	{
		return serverName;
	}
	/**
	 * Set the serverName.
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}
	/**
	 * Get the j2eeServerType.
	 * @return the j2eeServerType
	 */
	public String getJ2eeServerType()
	{
		return j2eeServerType;
	}
	/**
	 * Set the j2eeServerType.
	 * @param serverType the j2eeServerType to set
	 */
	public void setJ2eeServerType(String serverType)
	{
		j2eeServerType = serverType;
	}
	public Map<String, String> getProtocol2initialContextURL() {
		return protocol2initialContextURL;
	}
	public void setProtocol2initialContextURL(Map<String, String> protocol2initialContextURL) {
		this.protocol2initialContextURL = protocol2initialContextURL;
	}

	public void copyFromCf(RootOrganisationCf rootOrganisationCf)
	{
		if(rootOrganisationCf == null)
			rootOrganisationCf = new RootOrganisationCf();
		setOrganisationID(rootOrganisationCf.getOrganisationID());
		setOrganisationName(rootOrganisationCf.getOrganisationName());
		ServerCf server = rootOrganisationCf.getServer();
		if(server == null)
			server = new ServerCf();
		setServerID(server.getServerID());
		setServerName(server.getServerName());
		setJ2eeServerType(server.getJ2eeServerType());
		protocol2initialContextURL = new HashMap<String, String>(server.getProtocol2initialContextURL());
	}

	public void copyToCf(RootOrganisationCf rootOrganisationCf)
	{
		rootOrganisationCf.setOrganisationID(getOrganisationID());
		rootOrganisationCf.setOrganisationName(getOrganisationName());
		ServerCf server = rootOrganisationCf.getServer();
		if(server == null)
			server = new ServerCf();
		server.setServerID(getServerID());
		server.setServerName(getServerName());
		server.setJ2eeServerType(getJ2eeServerType());
		server.setProtocol2initialContextURL(new HashMap<String, String>(protocol2initialContextURL));
		rootOrganisationCf.setServer(server);
	}
}
