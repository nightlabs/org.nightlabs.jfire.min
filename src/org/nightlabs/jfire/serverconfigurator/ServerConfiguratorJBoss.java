package org.nightlabs.jfire.serverconfigurator;

import java.io.File;
import java.io.FileOutputStream;
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

			// write the file
			FileOutputStream out = new FileOutputStream(destFile);
			Writer w = new OutputStreamWriter(out, Utils.CHARSET_NAME_UTF_8);
			w.write(text);
			w.close();
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

			// write the file
			FileOutputStream out = new FileOutputStream(destFile);
			Writer w = new OutputStreamWriter(out, Utils.CHARSET_NAME_UTF_8);
			w.write(text);
			w.close();
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
