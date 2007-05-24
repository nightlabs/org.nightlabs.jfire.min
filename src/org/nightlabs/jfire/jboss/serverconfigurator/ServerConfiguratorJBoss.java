package org.nightlabs.jfire.jboss.serverconfigurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.jboss.authentication.JFireServerLocalLoginModule;
import org.nightlabs.jfire.jboss.authentication.JFireServerLoginModule;
import org.nightlabs.jfire.jboss.cascadedauthentication.CascadedAuthenticationClientInterceptor;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.util.Utils;

/**
 * This implementation of {@link ServerConfigurator} performs the following tasks in order
 * to configure your JBoss server:
 * <ul>
 * <li>Add the security domains <code>jfireLocal</code> and <code>jfire</code> to the <code>login-config.xml</code></li>
 * <li>Add the <code>CascadedAuthenticationClientInterceptor</code> to the <code>standard-jboss.xml</code></li>
 * <li>Add the <code>CascadedAuthenticationClientInterceptor.properties</code> file, if it does not yet exist (in JBoss' bin directory)</li>
 * </ul>
 * Note, that it has been tested only with the JBoss version 4.0.4.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerConfiguratorJBoss
		extends ServerConfigurator
{
	private static final Logger logger = Logger.getLogger(ServerConfiguratorJBoss.class);
	protected static final boolean rebootOnDeployDirChanges = false;

	protected static void waitForServer()
	{
		if(System.getProperty("jboss.home.dir") != null) {
			// we are running in jboss!
			logger.debug("Waiting for server...");
			try { Thread.sleep(15000); } catch (InterruptedException ignore) { }
		}
	}
	
	protected static File backup(File f) throws IOException
	{
		if(!f.exists() || !f.canRead())
			throw new FileNotFoundException("Invalid file to backup: "+f);
		File backupFile = new File(f.getAbsolutePath()+".bak");
		Utils.copyFile(f, backupFile);
		return backupFile;
	}

	protected static File moveToBackup(File f) throws IOException
	{
		if(!f.exists())
			throw new FileNotFoundException("Invalid file to backup: "+f);
		File backupFile = new File(f.getAbsolutePath()+".bak");
		if(!f.renameTo(backupFile))
			throw new IOException("Renaming file "+f.getAbsolutePath()+" to "+f.getName()+" failed");
		return backupFile;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.serverconfigurator.ServerConfigurator#doConfigureServer()
	 */
	@Override
	protected void doConfigureServer()
			throws ServerConfigurationException
	{
		try {
			// jbossDeployDir is ${jboss}/server/default/deploy - not ${jboss}/server/default/deploy/JFire.last
			File jbossDeployDir = new File(getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getParentFile().getAbsoluteFile();
			File jbossConfDir = new File(jbossDeployDir.getParentFile(), "conf");
			File jbossBinDir = new File(jbossDeployDir.getParentFile().getParentFile().getParentFile(), "bin");
	
			configureLoginConfigXml(jbossConfDir);
			configureStandardJBossXml(jbossConfDir);
			configureJBossServiceXml(jbossConfDir);
			configureCascadedAuthenticationClientInterceptorProperties(jbossBinDir);
			configureRunSh(jbossConfDir);
			configureRunBat(jbossConfDir);
			removeUnneededFiles(jbossDeployDir);
			
		} catch(Exception e) {
			throw new ServerConfigurationException("Server configuration failed in server configurator "+getClass().getName(), e);
		}
	}

	/**
	 * create ${jboss.bin}/CascadedAuthenticationClientInterceptor.properties if not yet existent
	 * jboss' bin is *NOT ALWAYS* our current working directory
	 * 
	 * @param jbossBinDir The JBoss bin dir
	 * @throws FileNotFoundException If the file eas not found
	 * @throws IOException In case of an io error
	 */
	private void configureCascadedAuthenticationClientInterceptorProperties(File jbossBinDir) throws FileNotFoundException, IOException
	{
		File destFile = new File(jbossBinDir, "CascadedAuthenticationClientInterceptor.properties");
		if (!destFile.exists()) {
			logger.info("File " + destFile.getAbsolutePath() + " does not exist. Will create it with enable=yes.");
			Properties props = new Properties();
			props.put("enable", "yes");
			FileOutputStream out = new FileOutputStream(destFile);
			try {
				props.store(out, "Automatically created by " + this.getClass().getName());
			} finally {		
				out.close();
			}
			CascadedAuthenticationClientInterceptor.reloadProperties(); // reboot should not be necessary anymore after this extension
		}
	}

	/**
	 * We deactivate the JAAS cache, because we have our own cache that is
	 * proactively managed and reflects changes immediately.
	 * Additionally, we extend the transaction timeout to 15 min (default is 5 min).
	 * 
	 * @param jbossConfDir The JBoss config dir
	 * @throws FileNotFoundException If the file eas not found
	 * @throws IOException In case of an io error
	 */
	private void configureJBossServiceXml(File jbossConfDir) throws FileNotFoundException, IOException
	{
		File destFile = new File(jbossConfDir, "jboss-service.xml");
		String text = Utils.readTextFile(destFile);
		String modificationMarker = "!!!ModifiedByJFire!!!";
		if (text.indexOf(modificationMarker) < 0) {
			logger.info("File " + destFile.getAbsolutePath() + " was not yet updated. Will increase transaction timeout and reduce JAAS cache timeout to 5 min - we cannot deactivate the JAAS cache completely or reduce the timeout further, because that causes JPOX problems (though I don't understand why).");
			setRebootRequired(true);

			Pattern pattern = Pattern.compile(
					"(<mbean[^>]*?org\\.jboss.security\\.plugins\\.JaasSecurityManagerService(?:\\n|.)*?<attribute +?name *?= *?\"DefaultCacheTimeout\")>[0-9]*<((?:\\n|.)*?</mbean>)"
					);
			text = pattern.matcher(text).replaceAll(
					"<!-- " + modificationMarker + "\n " +
					ServerConfiguratorJBoss.class.getName() + " has reduced the JAAS cache timeout to 5 min.\n" +
					" JFire has its own cache, which is updated immediately. We cannot completely deactivate the JAAS cache, however,\n" +
					" because that causes JPOX bugs (why?!).\n Marco :-)\n-->\n" +
					"   $1>300<$2"
					);

// IMHO 60 is the default - Marco.
//			pattern = Pattern.compile(
//					"(<mbean[^>]*?org\\.jboss\\.security\\.plugins\\.JaasSecurityManagerService(?:\\n|.)*?<attribute +?name *?= *?\"DefaultCacheResolution\")>[0-9]*<((?:\\n|.)*?</mbean>)"
//					);
//			text = pattern.matcher(text).replaceAll("$1>60<$2");

			pattern = Pattern.compile(
					"(<mbean[^>]*?org\\.jboss\\.tm\\.TransactionManagerService(?:\\n|.)*?<attribute +?name *?= *?\"TransactionTimeout\")>[0-9]*<((?:\\n|.)*?</mbean>)"
					);
			text = pattern.matcher(text).replaceAll(
					"<!-- " + modificationMarker + "\n " +
					ServerConfiguratorJBoss.class.getName() + " has increased the transaction timeout to 15 min.\n-->\n" +
					"   $1>900<$2"
					);

			
			pattern = Pattern.compile("(<property\\s+name\\s*=\\s*\"suffixes\">)([^<]*)(</property>)");
			text = pattern.matcher(text).replaceAll(
					"<!-- " + modificationMarker + "\n         " +
					ServerConfiguratorJBoss.class.getName() + " has added -clrepository.xml\n         -->\n" +
					"         $1$2,-clrepository.xml$3");
			
			
			Utils.writeTextFile(destFile, text);
		}
	}

	/**
	 * *** work necessary for NightLabsCascadedAuthenticationJBoss ***
	 * check/modify ${jboss.conf}/standardjboss.xml and REBOOT if changes occured
	 * 
	 * @param jbossConfDir The JBoss config dir
	 * @throws FileNotFoundException If the file eas not found
	 * @throws IOException In case of an io error
	 */
	private void configureStandardJBossXml(File jbossConfDir) throws FileNotFoundException, IOException
	{
		File destFile = new File(jbossConfDir, "standardjboss.xml");
		String text = Utils.readTextFile(destFile);
		if (text.indexOf(CascadedAuthenticationClientInterceptor.class.getName()) < 0) {
			logger.info("File " + destFile.getAbsolutePath() + " does not contain an interceptor registration for "+CascadedAuthenticationClientInterceptor.class.getName()+". Will add it.");
			setRebootRequired(true); // this is a must, because the conf directory doesn't support redeployment
			String replacementText = "$1\n            <interceptor>"+CascadedAuthenticationClientInterceptor.class.getName()+"</interceptor>";

			Pattern pattern = Pattern.compile("(<client-interceptors>[^<]*?<home>)");
			text = pattern.matcher(text).replaceAll(replacementText);			

			pattern = Pattern.compile("(<client-interceptors>[^<]*?<home>(.|\\n)*?</home>[^<]*?<bean>)");
			text = pattern.matcher(text).replaceAll(replacementText);

			Utils.writeTextFile(destFile, text);
		}
	}

	/**
	 * *** work necessary for using JFire Authentication & Authorization ***
	 * add our JFire security domains to ${jboss.conf]/login-config.xml if necessary
	 * 
	 * @param jbossConfDir The JBoss config dir
	 * @throws FileNotFoundException If the file eas not found
	 * @throws IOException In case of an io error
	 */
	private void configureLoginConfigXml(File jbossConfDir) throws FileNotFoundException, IOException
	{
		File destFile = new File(jbossConfDir, "login-config.xml");
		String text;
		text = Utils.readTextFile(destFile);
		if (text.indexOf("jfireLocal") < 0)
		{
			setRebootRequired(true);
			logger.info("File " + destFile.getAbsolutePath() + " does not contain the security domain \"jfireLocal\". Will add both, \"jfireLocal\" and \"jfire\".");
			String replacementText =
					"    <application-policy name = \"jfireLocal\">\n" +
					"        <authentication>\n" +
					"            <login-module code = \""+JFireServerLocalLoginModule.class.getName()+"\" flag = \"required\"/>\n" +
					"            <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\">\n" +
					"                <module-option name=\"restore-login-identity\">true</module-option>\n" +
					"            </login-module>\n" +
					"\n" +
					"<!--                    <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\"/>\n" +
					"-->\n" +
					"        </authentication>\n" +
					"    </application-policy>\n" +
					"\n" +
					"    <application-policy name = \"jfire\">\n" +
					"        <authentication>\n" +
					"            <login-module code = \""+JFireServerLoginModule.class.getName()+"\" flag = \"required\"/>\n" +
					"\n" +
					"<!--                    <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\">\n" +
					"                <module-option name=\"multi-threaded\">true</module-option>\n" +
					"            </login-module>\n" +
					"-->\n" +
					"\n" +
					"<!--                    <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\">\n" +
					"                <module-option name=\"multi-threaded\">true</module-option>\n" +
					"                <module-option name=\"restore-login-identity\">true</module-option>\n" +
					"                <module-option name=\"password-stacking\">true</module-option>\n" +
					"            </login-module>\n" +
					"-->\n" +
					"        </authentication>\n" +
					"    </application-policy>\n" +
					"\n" +
					"</policy>\n";

			text = text.replaceAll("</policy>", replacementText);

			Utils.writeTextFile(destFile, text);
		}
	}
	
	private void configureRunSh(File jbossConfDir) throws FileNotFoundException, IOException
	{
		Properties serverConfiguratorSettings = getJFireServerConfigModule().getJ2ee().getServerConfiguratorSettings();
		if(serverConfiguratorSettings == null)
			return;
		String rmiHost = serverConfiguratorSettings.getProperty("java.rmi.server.hostname");
		if(rmiHost == null)
			return;
		
		File destFile = new File(jbossConfDir, "run.sh");
		String text;
		text = Utils.readTextFile(destFile);
		String originalText = "JAVA_OPTS=\"$JAVA_OPTS -Dprogram.name=$PROGNAME\"";
		if (text.indexOf(originalText) >= 0)
		{
			setRebootRequired(true);
			logger.info("File " + destFile.getAbsolutePath() + " does not contain the java.rmi.server.hostname setting. Adding it...");
			String replacementText = 
					originalText + "\n\n" +
					"# Setting RMI host for JNDI to "+rmiHost+" (auto added by "+getClass().getName()+")\n"+
					"JAVA_OPTS=\"$JAVA_OPTS -Djava.rmi.server.hostname="+rmiHost+"\"";

			text = text.replaceAll(Pattern.quote(originalText), replacementText);

			Utils.writeTextFile(destFile, text);
		}
	}

	private void configureRunBat(File jbossConfDir) throws FileNotFoundException, IOException
	{
		Properties serverConfiguratorSettings = getJFireServerConfigModule().getJ2ee().getServerConfiguratorSettings();
		if(serverConfiguratorSettings == null)
			return;
		String rmiHost = serverConfiguratorSettings.getProperty("java.rmi.server.hostname");
		if(rmiHost == null)
			return;
		
		File destFile = new File(jbossConfDir, "run.bat");
		String text;
		text = Utils.readTextFile(destFile);
		String originalText = "set JAVA_OPTS=%JAVA_OPTS% -Dprogram.name=%PROGNAME%";
		if (text.indexOf(originalText) >= 0)
		{
			setRebootRequired(true);
			logger.info("File " + destFile.getAbsolutePath() + " does not contain the java.rmi.server.hostname setting. Adding it...");
			String replacementText = 
					originalText + "\r\n\r\n" +
					"rem Setting RMI host for JNDI to "+rmiHost+" (auto added by "+getClass().getName()+")\r\n"+
					"set JAVA_OPTS=%JAVA_OPTS% -Djava.rmi.server.hostname="+rmiHost+"\r\n";

			text = text.replaceAll(Pattern.quote(originalText), replacementText);

			Utils.writeTextFile(destFile, text);
		}
	}
	
	private void removeUnneededFiles(File jbossDeployDir) throws IOException
	{
		File[] filesToRemove = {
				new File(jbossDeployDir, "uuid-key-generator.sar"),
				new File(new File(jbossDeployDir, "jms"), "jbossmq-destinations-service.xml"),
		};
		
		for (File f : filesToRemove) {
			if(f.exists()) {
				File backup = moveToBackup(f);
				logger.info("Moved "+f.getAbsolutePath()+" to "+backup.getAbsolutePath()+" in order to deactivate it");
				setRebootRequired(true);
			}
		}
	}
}
