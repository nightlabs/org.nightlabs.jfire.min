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
	
//	private JFireServerManagerFactoryImpl jfireServerManagerFactoryImpl;
	private JFireServerConfigModule jfireServerConfigModule;

	public JFireServerConfigModule getJFireServerConfigModule()
	{
		return jfireServerConfigModule;
	}

	public void setJFireServerConfigModule(
			JFireServerConfigModule jfireServerConfigModule)
	{
		this.jfireServerConfigModule = jfireServerConfigModule;
	}

//	public JFireServerManagerFactoryImpl getJFireServerManagerFactoryImpl()
//	{
//		return jfireServerManagerFactoryImpl;
//	}
//
//	public void setJFireServerManagerFactoryImpl(
//			JFireServerManagerFactoryImpl jfireServerManagerFactoryImpl)
//	{
//		this.jfireServerManagerFactoryImpl = jfireServerManagerFactoryImpl;
//	}

	private boolean rebootRequired = false;

	/**
	 * This method indicates whether the j2ee server needs to be rebooted.
	 *
	 * @return Returns true after {@link #doConfigureServer()} has been called, if it modified
	 *		the server configuration in a way that requires it to be rebooted.
	 */
	public boolean isRebootRequired()
	{
		return rebootRequired;
	}
	
	protected void setRebootRequired(boolean rebootRequired)
	{
		Logger.getLogger(ServerConfigurator.class).info("setRebootRequired: rebootRequired=" + rebootRequired);

		this.rebootRequired = rebootRequired;
	}

	/**
	 * Configure the server using the {@link ServerConfigurator} defined in the
	 * given server config module.
	 * This method will return <code>true</code> if the server needs to be restarted.
	 * @param jfireServerConfigModule The server config module
	 * @return <code>true</code> if the server needs to be restarted, 
	 * 		<code>false</code> otherwise.
	 * @throws ServerConfigurationException In case of an error during configuration.
	 */
	public static boolean configureServer(JFireServerConfigModule jfireServerConfigModule) throws ServerConfigurationException
	{
		//	 instantiating and calling ServerConfigurator
		String serverConfiguratorClassName = jfireServerConfigModule.getJ2ee().getServerConfigurator();

		if (logger.isDebugEnabled())
			logger.debug("Instantiating ServerConfigurator: " + serverConfiguratorClassName);

		Class serverConfiguratorClass;
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
			//serverConfigurator.setJFireServerManagerFactoryImpl(this);
			serverConfigurator.setJFireServerConfigModule(jfireServerConfigModule);
		} catch (Throwable x) {
			throw new ServerConfigurationException("Instantiating ServerConfigurator from class " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
		}

		logger.info("Configuring server with ServerConfigurator " + serverConfiguratorClassName);

//		try {
			serverConfigurator.doConfigureServer();
//		} catch (Throwable x) {
//			throw new ServerConfigurationException("Calling ServerConfigurator.configureServer() with instance of " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
//		}
		
		return serverConfigurator.isRebootRequired();
	}
	
	/**
	 * This method is called as well explicitely by a GUI operation (via the web-frontend for
	 * server initialization), as implicitely on every server start.
	 * <p>
	 * Configuration changes might require the server to be rebooted. If you changed the
	 * configuration in a way that renders the server non-workable or require a reboot for
	 * another reason, you should call {@link #setRebootRequired(boolean)}.
	 * </p>
	 *
	 * @throws ServerConfigurationException In case of an error during configuration.
	 */
	protected abstract void doConfigureServer() throws ServerConfigurationException;
}
