package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The server core J2EE configuration
 * @author Marco Schulze
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class J2eeCf extends JFireServerConfigPart implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private String j2eeDeployBaseDirectory;
	private String serverConfigurator;
	private Properties serverConfiguratorSettings;
	private List<String> availableServerConfigurators;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.servermanager.config.JFireServerConfigPart#init()
	 */
	@Override
	public void init()
	{
		if (j2eeDeployBaseDirectory == null)
			setJ2eeDeployBaseDirectory("../server/default/deploy/JFire.last/");

		if (serverConfigurator == null)
			setServerConfigurator("org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBoss");

		if (availableServerConfigurators == null) {
			availableServerConfigurators = new ArrayList<String>();
			availableServerConfigurators.add("org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBoss");
			availableServerConfigurators.add("org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBossMySQL");
		}
		
		if(serverConfiguratorSettings == null)
			serverConfiguratorSettings = new Properties();
	}
	
	/**
	 * Get the availableServerConfigurators.
	 * @return the availableServerConfigurators
	 */
	public List<String> getAvailableServerConfigurators()
	{
		return availableServerConfigurators;
	}
	
	/**
	 * Set the availableServerConfigurators.
	 * @param availableServerConfigurators the availableServerConfigurators to set
	 */
	public void setAvailableServerConfigurators(
			List<String> availableServerConfigurators)
	{
		this.availableServerConfigurators = availableServerConfigurators;
		setChanged();
	}
	
	/**
	 * Get the j2eeDeployBaseDirectory.
	 * @return the j2eeDeployBaseDirectory
	 */
	public String getJ2eeDeployBaseDirectory()
	{
		return j2eeDeployBaseDirectory;
	}
	
	/**
	 * Set the j2eeDeployBaseDirectory.
	 * @param deployBaseDirectory the j2eeDeployBaseDirectory to set
	 */
	public void setJ2eeDeployBaseDirectory(String deployBaseDirectory)
	{
		j2eeDeployBaseDirectory = deployBaseDirectory;
		setChanged();
	}
	
	/**
	 * Get the serverConfigurator.
	 * @return the serverConfigurator
	 */
	public String getServerConfigurator()
	{
		return serverConfigurator;
	}
	
	/**
	 * Set the serverConfigurator.
	 * @param serverConfigurator the serverConfigurator to set
	 */
	public void setServerConfigurator(String serverConfigurator)
	{
		this.serverConfigurator = serverConfigurator;
		setChanged();
	}
	
	/**
	 * Get the serverConfiguratorSettings.
	 * @return the serverConfiguratorSettings
	 */
	public Properties getServerConfiguratorSettings()
	{
		return serverConfiguratorSettings;
	}
	
	/**
	 * Set the serverConfiguratorSettings.
	 * @param serverConfiguratorSettings the serverConfiguratorSettings to set
	 */
	public void setServerConfiguratorSettings(Properties serverConfiguratorSettings)
	{
		this.serverConfiguratorSettings = serverConfiguratorSettings;
		setChanged();
	}
}