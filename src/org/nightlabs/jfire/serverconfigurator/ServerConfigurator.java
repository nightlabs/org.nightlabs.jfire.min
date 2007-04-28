package org.nightlabs.jfire.serverconfigurator;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;

/**
 * You can configure one implementation of <code>ServerConfigurator</code> for your JFire server
 * (see {@link JFireServerConfigModule#getJ2ee()}),
 * which is triggered on every server start. The ServerConfigurator should ensure that your
 * server is configured in the appropriate way.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class ServerConfigurator
{
	private JFireServerManagerFactoryImpl jfireServerManagerFactoryImpl;
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

	public JFireServerManagerFactoryImpl getJFireServerManagerFactoryImpl()
	{
		return jfireServerManagerFactoryImpl;
	}

	public void setJFireServerManagerFactoryImpl(
			JFireServerManagerFactoryImpl jfireServerManagerFactoryImpl)
	{
		this.jfireServerManagerFactoryImpl = jfireServerManagerFactoryImpl;
	}

	private boolean rebootRequired = false;

	/**
	 * This method indicates whether the j2ee server needs to be rebooted.
	 *
	 * @return Returns true after {@link #configureServer()} has been called, if it modified
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
	 * This method is called as well explicitely by a GUI operation (via the web-frontend for
	 * server initialization), as implicitely on every server start.
	 * <p>
	 * Configuration changes might require the server to be rebooted. If you changed the
	 * configuration in a way that renders the server non-workable or require a reboot for
	 * another reason, you should call {@link #setRebootRequired(boolean)}.
	 * </p>
	 *
	 * @throws Exception
	 */
	public abstract void configureServer() throws Exception;
}
