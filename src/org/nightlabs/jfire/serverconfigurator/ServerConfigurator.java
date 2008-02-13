package org.nightlabs.jfire.serverconfigurator;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;

/**
 * You can configure one implementation of <code>ServerConfigurator</code> for your JFire server
 * (see {@link JFireServerConfigModule#getJ2ee()}),
 * which is triggered on every server start. The ServerConfigurator should ensure that your
 * server is configured in the appropriate way.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class ServerConfigurator
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ServerConfigurator.class);
	
	/**
	 * The config to use for configuration
	 */
	private JFireServerConfigModule jfireServerConfigModule;
	
	/**
	 * Wether a reboot is required after doing the changes.
	 */
	private boolean rebootRequired = false;

	/**
	 * Get the jfireServerConfigModule.
	 * @return the jfireServerConfigModule
	 */
	public JFireServerConfigModule getJFireServerConfigModule()
	{
		return jfireServerConfigModule;
	}

	/**
	 * Set the jfireServerConfigModule.
	 * @param jfireServerConfigModule the jfireServerConfigModule to set
	 */
	public void setJFireServerConfigModule(JFireServerConfigModule jfireServerConfigModule)
	{
		this.jfireServerConfigModule = jfireServerConfigModule;
	}
	
	/**
	 * This method indicates whether the j2ee server needs to be rebooted.
	 * @return Returns true after {@link #doConfigureServer()} has been called,
	 * 		if it modified the server configuration in a way that requires it to
	 * 		be rebooted.
	 */
	public boolean isRebootRequired()
	{
		return rebootRequired;
	}
	
	/**
	 * Set the reboot required value. If set to <code>true</code>, the
	 * server will be stopped after all server configurations have happened.
	 * @param rebootRequired <code>true</code> if a reboot is required -
	 * 		<code>false</code> otherwise
	 */
	protected void setRebootRequired(boolean rebootRequired)
	{
		// NEVER REMOVE THIS LINE:
		logger.info("setRebootRequired: rebootRequired=" + rebootRequired);
		this.rebootRequired = rebootRequired;
	}

	/**
	 * Configure the server using the {@link ServerConfigurator} defined in the
	 * given server config module.
	 * <p>
	 * This method will return <code>true</code> if the server needs to be restarted.
	 * 
	 * @param jfireServerConfigModule The server config module
	 * @return <code>true</code> if the server needs to be restarted -
	 * 		<code>false</code> otherwise.
	 * @throws ServerConfigurationException In case of an error during configuration.
	 */
	public static boolean configureServer(JFireServerConfigModule jfireServerConfigModule) throws ServerConfigurationException
	{
		//	 instantiating and calling ServerConfigurator
		String serverConfiguratorClassName = jfireServerConfigModule.getJ2ee().getServerConfigurator();

		if (logger.isDebugEnabled())
			logger.debug("Instantiating ServerConfigurator: " + serverConfiguratorClassName);

		Class<?> serverConfiguratorClass;
		try {
			serverConfiguratorClass = Class.forName(serverConfiguratorClassName);
		} catch (Throwable x) {
			throw new ServerConfigurationException("Loading ServerConfigurator class " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
		}

		if (!ServerConfigurator.class.isAssignableFrom(serverConfiguratorClass))
			throw new IllegalStateException("ServerConfigurator " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) does not extend class " + ServerConfigurator.class);

		ServerConfigurator serverConfigurator;
		try {
			serverConfigurator = (ServerConfigurator) serverConfiguratorClass.newInstance();
			serverConfigurator.setJFireServerConfigModule(jfireServerConfigModule);
		} catch (Throwable x) {
			throw new ServerConfigurationException("Instantiating ServerConfigurator from class " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
		}

		logger.info("Loading database driver classes.");
		try {
			Class.forName(jfireServerConfigModule.getDatabase().getDatabaseDriverName_noTx());
		} catch (Exception x) {
			logger.warn("Could not load database driver configured for noTx!", x);
		}
		try {
			Class.forName(jfireServerConfigModule.getDatabase().getDatabaseDriverName_localTx());
		} catch (Exception x) {
			logger.warn("Could not load database driver configured for localTx!", x);
		}
		try {
			Class.forName(jfireServerConfigModule.getDatabase().getDatabaseDriverName_xa());
		} catch (Exception x) {
			logger.warn("Could not load database driver configured for xa!", x);
		}

		logger.info("Configuring server with ServerConfigurator " + serverConfiguratorClassName);
		serverConfigurator.doConfigureServer();
		
		return serverConfigurator.isRebootRequired();
	}
	
	/**
	 * This method is called as well explicitely by a GUI operation (via the web-frontend for
	 * server initialization or the JFire installer), as implicitely on every server start.
	 * <p>
	 * Configuration changes might require the server to be rebooted. If you changed the
	 * configuration in a way that renders the server non-workable or require a reboot for
	 * another reason, you should call {@link #setRebootRequired(boolean)}.
	 *
	 * @throws ServerConfigurationException In case of an error during configuration.
	 */
	protected abstract void doConfigureServer() throws ServerConfigurationException;
}
