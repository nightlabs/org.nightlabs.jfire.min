package org.nightlabs.jfire.server;

import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;

@Remote
public interface ServerManagerRemote
{
	String ping(String message);

	/**
	 * @return Whether or not this server is new and needs setup.
	 */
	boolean isNewServerNeedingSetup();

	/**
	 * @return Returns a clone of the main config module.
	 */
	JFireServerConfigModule getJFireServerConfigModule();

	/**
	 * @throws ConfigException If the configuration is obviously wrong - not all errors are detected, however!
	 */
	void setJFireServerConfigModule(JFireServerConfigModule cfMod)
			throws ConfigException;

	/**
	 * Configures the server using the currently configured {@link ServerConfigurator} and
	 * shuts it down if necessary.
	 *
	 * @param delayMSec In case shutdown is necessary, how long to delay it (this method will return immediately).
	 *		This is necessary for having a few secs left to return the client a new web page.
	 * @return <code>true</code>, if the server configuration was changed in a way that requires reboot.
	 * @throws ServerConfigurationException If configuring the server failed
	 */
	boolean configureServerAndShutdownIfNecessary(long delayMSec)
			throws ServerConfigurationException;

//	/**
//	 * @throws XMLReadException If loading the module information failed
//	 */
//	List<ModuleDef> getModules(ModuleType moduleType) throws XMLReadException;
//
//	void flushModuleCache();

	List<J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> getJ2eeRemoteServers();

	/**
	 * Get the server's current time. This can - of course - not be used for synchronizing a client,
	 * because it is unknown how long the response travels to the client (latency), but sufficient
	 * for a rough guess whether the server's and client's time are too far off.
	 */
	Date getServerTime();

}