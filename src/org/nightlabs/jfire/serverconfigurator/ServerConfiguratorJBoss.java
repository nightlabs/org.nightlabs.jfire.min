package org.nightlabs.jfire.serverconfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nightlabs.authentication.jboss.CascadedAuthenticationClientInterceptor;
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
 */
public class ServerConfiguratorJBoss
		extends ServerConfigurator
{
	private static final Logger logger = Logger.getLogger(ServerConfiguratorJBoss.class);
	protected static final boolean rebootOnDeployDirChanges = false;

	protected static void writeTextFile(File file, String text)
	throws IOException
	{
		FileOutputStream out = null;
		Writer w = null;
		try {
			out = new FileOutputStream(file);
			w = new OutputStreamWriter(out, Utils.CHARSET_NAME_UTF_8);
			w.write(text);
		} finally {
			if (w != null) w.close();
			if (out != null) out.close();
		}
	}

	@Override
	public void configureServer()
			throws Exception
	{
		// jbossDeployDir is ${jboss}/server/default/deploy - not ${jboss}/server/default/deploy/JFire.last
		File jbossDeployDir = new File(getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getParentFile().getAbsoluteFile();
		File jbossConfDir = new File(jbossDeployDir.getParentFile(), "conf");

		File destFile;
		String text;

		// *** work necessary for using JFire Authentication & Authorization ***
		// add our JFire security domains to ${jboss.conf]/login-config.xml if necessary
		destFile = new File(jbossConfDir, "login-config.xml");
		text = Utils.readTextFile(destFile);
		if (text.indexOf("jfireLocal") < 0)
		{
			setRebootRequired(true);
			logger.info("File " + destFile.getAbsolutePath() + " does not contain the security domain \"jfireLocal\". Will add both, \"jfireLocal\" and \"jfire\".");
			String replacementText =
					"    <application-policy name = \"jfireLocal\">\n" +
					"        <authentication>\n" +
					"            <login-module code = \"org.nightlabs.jfire.base.JFireServerLocalLoginModule\" flag = \"required\"/>\n" +
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
					"            <login-module code = \"org.nightlabs.jfire.base.JFireServerLoginModule\" flag = \"required\"/>\n" +
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

			writeTextFile(destFile, text);
		}


		// *** work necessary for NightLabsCascadedAuthenticationJBoss ***
		// check/modify ${jboss.conf}/standardjboss.xml and REBOOT if changes occured
		destFile = new File(jbossConfDir, "standardjboss.xml");
		text = Utils.readTextFile(destFile);
		if (text.indexOf(CascadedAuthenticationClientInterceptor.class.getName()) < 0) {
			logger.info("File " + destFile.getAbsolutePath() + " does not contain an interceptor registration for org.nightlabs.authentication.jboss.CascadedAuthenticationClientInterceptor. Will add it.");
			setRebootRequired(true); // this is a must, because the conf directory doesn't support redeployment
			String replacementText = "$1\n            <interceptor>org.nightlabs.authentication.jboss.CascadedAuthenticationClientInterceptor</interceptor>";

			Pattern pattern = Pattern.compile("(<client-interceptors>[^<]*?<home>)");
			text = pattern.matcher(text).replaceAll(replacementText);			

			pattern = Pattern.compile("(<client-interceptors>[^<]*?<home>(.|\\n)*?</home>[^<]*?<bean>)");
			text = pattern.matcher(text).replaceAll(replacementText);

			writeTextFile(destFile, text);
		}

		// We deactivate the JAAS cache, because we have our own cache that is
		// proactively managed and reflects changes immediately.
		destFile = new File(jbossConfDir, "jboss-service.xml");
		text = Utils.readTextFile(destFile);
		String modificationMarker = "!!!ModifiedByJFire!!!";
		if (text.indexOf(modificationMarker) < 0) {
			logger.info("File " + destFile.getAbsolutePath() + " was not yet updated. Will reduce JAAS cache timeout to 5 min - we cannot deactivate it completely or reduce it further, because that causes JPOX problems (though I don't understand why).");
			setRebootRequired(true);

			Pattern pattern = Pattern.compile(
					"(<mbean[^>]*?org.jboss.security.plugins.JaasSecurityManagerService(?:\\n|.)*?<attribute +?name *?= *?\"DefaultCacheTimeout\")>[0-9]*<((?:\\n|.)*?</mbean>)"
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
//					"(<mbean[^>]*?org.jboss.security.plugins.JaasSecurityManagerService(?:\\n|.)*?<attribute +?name *?= *?\"DefaultCacheResolution\")>[0-9]*<((?:\\n|.)*?</mbean>)"
//					);
//			text = pattern.matcher(text).replaceAll("$1>60<$2");

			writeTextFile(destFile, text);
		}

		// create ${jboss.bin}/CascadedAuthenticationClientInterceptor.properties if not yet existent
		// jboss' bin is our current working directory
		destFile = new File("CascadedAuthenticationClientInterceptor.properties");
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

}
