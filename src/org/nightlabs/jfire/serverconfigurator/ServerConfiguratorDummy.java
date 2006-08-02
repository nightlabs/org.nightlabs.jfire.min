package org.nightlabs.jfire.serverconfigurator;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;

/**
 * This implementation of {@link ServerConfigurator} does nothing.
 * It is the default setting configured in {@link JFireServerConfigModule}.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ServerConfiguratorDummy
		extends ServerConfigurator
{

	public void configureServer() throws Exception
	{
		// This implementation of ServerConfigurator does not do anything.
		Logger.getLogger(this.getClass()).info("This implementation of ServerConfigurator is a noop! It does not change your configuration!");
	}

}
