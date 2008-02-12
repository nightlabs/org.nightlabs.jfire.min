package org.nightlabs.jfire.jboss.serverconfigurator.internal;

import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.nightlabs.config.Config;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;

/**
 * Executes the {@link ServerConfigurator} configured in the JFire server configuration
 * (i.e. the module {@link JFireServerConfigModule}). Therefore, it first creates a {@link Config}
 * for the configuration in ${jboss}/server/default/JFire.last/JFireBase.ear/config and then
 * asks it for the instance of {@link JFireServerConfigModule}.
 * <p>
 * An instance of this class is created by the {@link Launcher} after it has loaded this class with its new
 * class-loader. Thus, this instance can access all JFire classes (and all other classes deployed in the JBoss).
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class InternalLauncher
{
	public void run() throws Exception
	{
		System.out.println("InternalLauncher.run: getClass().getClassLoader(): " + getClass().getClassLoader());

		File serverDefaultFolder = new File(new File(new File("..").getAbsoluteFile(), "server"), "default");
		File serverDeployFolder = new File(serverDefaultFolder, "deploy");
		File jfireLastFolder = new File(serverDeployFolder, "JFire.last");

		if (!jfireLastFolder.exists()) {
			System.err.println("The folder JFire.last cannot be found! Either it does not exist or you started the Launcher in the wrong directory! The Launcher must be started from the JBoss server's bin directory. This path does not exist: " + jfireLastFolder.getAbsolutePath());
			throw new IllegalStateException("Directory does not exist: " + jfireLastFolder.getAbsolutePath());
		}

		File jbossConfigDir = new File(serverDefaultFolder, "conf");
		File jbossLogDir = new File(serverDefaultFolder, "log");
		File jfireConfigDir = new File(new File(jfireLastFolder, "JFireBase.ear"), "config");
		File jfireConfigFile = new File(jfireConfigDir, "Config.xml");

		System.setProperty("jboss.server.log.dir", jbossLogDir.getAbsolutePath());

		// configure log4j
		File log4jConfigFile = new File(jbossConfigDir, "jboss-log4j.xml");
		if (!log4jConfigFile.exists())
			log4jConfigFile = new File(jbossConfigDir, "log4j.xml"); // older jboss used this file

		if (!log4jConfigFile.exists()) { // still not existing => warning!
			System.err.println("log4j configuration file not existing!");
			log4jConfigFile = null;
		}

		if (log4jConfigFile != null) {
			try {
				DOMConfigurator.configure(log4jConfigFile.toURI().toURL());
			} catch (Exception x) {
				System.err.println("Configuring log4j failed!");
				x.printStackTrace();
			}
		}

		Config config = Config.createSharedInstance(jfireConfigFile);
		JFireServerConfigModule jfscm = config.createConfigModule(JFireServerConfigModule.class);
		ServerConfigurator.configureServer(jfscm);
	}
}
