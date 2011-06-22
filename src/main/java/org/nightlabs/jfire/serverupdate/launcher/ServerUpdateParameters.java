/**
 * 
 */
package org.nightlabs.jfire.serverupdate.launcher;

import java.io.File;

import org.kohsuke.args4j.Option;
import org.nightlabs.jfire.serverupdate.launcher.config.ServerUpdateConfig;

/**
 * Parameter-Bean for the ServerUpdate executable. 
 * 
 * @author abieber
 */
public class ServerUpdateParameters {

	@Option(
			name="-config",
			required=false,
			usage="The config-file for the update. The config file defines the classpath for the udpate and the path to search for database-descriptors. This parameter is optional it will default to \"update_server.xml\".")
	private File configFile;
	
	@Option(
			name="-dryRun", 
			required=false, 
			usage="Define this parameter if you want the update tov produce an SQL script instead of executing the SQL directly. As default the SQL script will be printed to standart-out. If you want to store the script to a file use the -dryRunFile parameter.")
	private boolean dryRun;
	
	@Option(
			name="-dryRunFile",
			required=false,
			usage="Defines the file where the SQL script of a dry run should be stored to")
	private File dryRunFile;
	
	@Option(
			name="-tryRun", 
			required=false, 
			usage="use this parameter if you want the update to try but not apply all changes, i.e. rollback everything after exection. This will show you whether the changes could be applied using your database-server.")
	private boolean tryRun;
	
	@Option(
			name="-help",
			required=false,
			usage="Show help")
	private boolean showHelp;
	
	/** Not read as parameter, but set from outside */
	private ServerUpdateConfig config;
	
	public ServerUpdateParameters() {
	}

	/**
	 * @return The config-file. Defaults to "update_server.xml"
	 */
	public File getConfigFile() {
		if (configFile == null) {
			configFile = new File("update_server.xml");
		}
		return configFile;
	}
	
	/**
	 * @return Whether a dry-run should be performed.
	 */
	public boolean isDryRun() {
		return dryRun;
	}
	
	/**
	 * @return The dump-file for a dry-run.
	 */
	public File getDryRunFile() {
		return dryRunFile;
	}
	
	/**
	 * @return Whether a try-run should be performed.
	 */
	public boolean isTryRun() {
		return tryRun;
	}
	
	/**
	 * @return Whether to show the help text.
	 */
	public boolean isShowHelp() {
		return showHelp;
	}
	
	/**
	 * @return The {@link ServerUpdateConfig} read from {@link #getConfigFile()}
	 */
	public ServerUpdateConfig getConfig() {
		if (config == null) {
			try {
				config = new ServerUpdateConfig(getConfigFile());
			} catch (Exception e) {
				throw new IllegalStateException("Could not read ServerUpdateConfig from " + getConfigFile(), e);
			}
		}
		return config;
	}
	
	public void logValues(String logLevel) {
		Log.log(logLevel, "====================================================================");
		Log.log(logLevel, "                ServerUpdate parameters                             ");
		Log.log(logLevel, "*** configFile: %s", getConfigFile());
		Log.log(logLevel, "*** dryRun: %s", isDryRun());
		Log.log(logLevel, "*** dryRunFile: %s", getDryRunFile());
		Log.log(logLevel, "*** tryRun: %s", isTryRun());
		Log.log(logLevel, "*** help: %s", isShowHelp());
		Log.log(logLevel, "====================================================================");
	}
}
