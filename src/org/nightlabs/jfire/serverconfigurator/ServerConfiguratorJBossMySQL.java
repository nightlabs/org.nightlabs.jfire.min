package org.nightlabs.jfire.serverconfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.regex.Pattern;

import org.nightlabs.authentication.jboss.CascadedAuthenticationClientInterceptor;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.db.DatabaseAlreadyExistsException;
import org.nightlabs.util.Utils;

/**
 * This implementation of {@link ServerConfigurator} will modify your JBossMQ
 * and Timer configuration as described in
 * https://www.jfire.org/modules/phpwiki/index.php/Switch%20JBossMQ%20from%20HSQL%20to%20MySQL
 * and creates the database <code>JFire_JBossMQ</code> (as defined by {@link #DATABASE_NAME})
 * in your MySQL server (if it does not yet exist).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ServerConfiguratorJBossMySQL
extends ServerConfigurator
{
	public static String DATABASE_NAME = "JFire_JBossMQ";

	public void configureServer() throws Exception
	{
		// create the database
		String databaseURL = getJFireServerConfigModule().getDatabase().getDatabaseURL(DATABASE_NAME);
		DatabaseAdapter databaseAdapter = getJFireServerConfigModule().getDatabase().instantiateDatabaseAdapter();
		try {
			databaseAdapter.createDatabase(getJFireServerConfigModule(), databaseURL);
		} catch (DatabaseAlreadyExistsException x) {
			// the database already exists - ignore
		}

		// jbossDeployDir is ${jboss}/server/default/deploy - not ${jboss}/server/default/deploy/JFire.last
		File jbossDeployDir = new File(getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getParentFile().getAbsoluteFile();
		File jbossConfDir = new File(jbossDeployDir.getParentFile(), "conf");
		File jbossDeployJmsDir = new File(jbossDeployDir, "jms");

		File destFile;

		// *** work necessary for JBossMQ ***
		// check the following files and deploy/replace them if necessary
		// ${jboss.deploy}/mysql-ds.xml
		destFile = new File(jbossDeployDir, "mysql-ds.xml");
		if (!destFile.exists()) {
			InputStream in = ServerConfiguratorJBossMySQL.class.getResourceAsStream("resource/jboss/deploy/mysql-ds.xml.jfire");
			String text = Utils.readTextFile(in);
			in.close();

			// not very efficient mechanism, but it should work
			text = text.replaceAll("\\{databaseDriverName\\}", getJFireServerConfigModule().getDatabase().getDatabaseDriverName());
			text = text.replaceAll("\\{databaseURL\\}", databaseURL);
			text = text.replaceAll("\\{databaseUserName\\}", getJFireServerConfigModule().getDatabase().getDatabaseUserName());
			text = text.replaceAll("\\{databasePassword\\}", getJFireServerConfigModule().getDatabase().getDatabasePassword());

			FileOutputStream out = new FileOutputStream(destFile);
			Writer w = new OutputStreamWriter(out, Utils.CHARSET_NAME_UTF_8);
			w.write(text);
			w.close();
			out.close();
		}

		// ${jboss.deploy}/jms/mysql-jdbc2-service.xml
		destFile = new File(jbossDeployJmsDir, "mysql-jdbc2-service.xml");
		if (!destFile.exists()) {
			Utils.copyResource(
					ServerConfiguratorJBossMySQL.class,
					"resource/jboss/deploy/jms/mysql-jdbc2-service.xml.jfire",
					destFile);
		}

		// ${jboss.deploy}/jms/jfire-jbossmq-mysql-jdbc-state-service.xml
		destFile = new File(jbossDeployJmsDir, "jfire-jbossmq-mysql-jdbc-state-service.xml");
		if (!destFile.exists()) {
			Utils.copyResource(
					ServerConfiguratorJBossMySQL.class,
					"resource/jboss/deploy/jms/jfire-jbossmq-mysql-jdbc-state-service.xml.jfire",
					destFile);
		}

		// ${jboss.deploy}/ejb-deployer.xml
		destFile = new File(jbossDeployDir, "ejb-deployer.xml");
		String text = Utils.readTextFile(destFile);
		String modifiedMarker = "!!!ModifiedByJFire!!!";
		if (!text.matches("(.|\n)*" + modifiedMarker + "(.|\n)*")) {
			String replacementText =
					"  <!-- " + modifiedMarker + " Do not change this line!!! The modification has been done by " + ServerConfiguratorJBossMySQL.class.getName() + ". -->\n" +
					"  <!-- A persistence policy that persistes timers to a database\n" +
					"  <mbean code=\"org.jboss.ejb.txtimer.DatabasePersistencePolicy\" name=\"jboss.ejb:service=EJBTimerService,persistencePolicy=database\">\n" +
					"    <!- DataSource JNDI name ->\n" +
					"    <depends optional-attribute-name=\"DataSource\">jboss.jca:service=DataSourceBinding,name=JFireJBossMQDS</depends>\n" +
					"    <!- The plugin that handles database persistence ->\n" +
					"    <attribute name=\"DatabasePersistencePlugin\">org.jboss.ejb.txtimer.GeneralPurposeDatabasePersistencePlugin</attribute>\n" +
					"  </mbean>\n" +
					"  -->\n" +
					"  <!-- For JFire, there is no need to persist the timer -->\n" +
					"  <mbean code=\"org.jboss.ejb.txtimer.NoopPersistencePolicy\" name=\"jboss.ejb:service=EJBTimerService,persistencePolicy=noop\"/>\n" +
					"\n";
			Pattern pattern = Pattern.compile("<mbean[^<]*EJBTimerService,persistencePolicy=database(\n|.)*</mbean>");
			text = pattern.matcher(text).replaceAll(replacementText);

			// write the file
			FileOutputStream out = new FileOutputStream(destFile);
			Writer w = new OutputStreamWriter(out, Utils.CHARSET_NAME_UTF_8);
			w.write(text);
			w.close();
			out.close();
		}

		// remove the following files (we rename them into *.bak, if they exist)
		// ${jboss.deploy}/hsqldb-ds.xml
		destFile = new File(jbossDeployDir, "hsqldb-ds.xml");
		if (destFile.exists()) {
			File newFileName = new File(destFile.getParentFile(), "hsqldb-ds.xml.bak");
			destFile.renameTo(newFileName);
		}

		// ${jboss.deploy}/jms/hsqldb-jdbc2-service.xml
		destFile = new File(jbossDeployJmsDir, "hsqldb-jdbc2-service.xml");
		if (destFile.exists()) {
			File newFileName = new File(destFile.getParentFile(), "hsqldb-jdbc2-service.xml.bak");
			destFile.renameTo(newFileName);
		}

		// *** work necessary for JBossMQ and JFire in general ***
		// check/modify ${jboss.conf}/login-config.xml and REBOOT if changes occured
		destFile = new File(jbossConfDir, "login-config.xml");
		text = Utils.readTextFile(destFile);
		if (text.matches("(.|\n)*java:/DefaultDS(.|\n)*")) {
			setRebootRequired(true);
			text = text.replaceAll("java:/DefaultDS", "java:/JFireJBossMQDS");

			// write the file
			FileOutputStream out = new FileOutputStream(destFile);
			Writer w = new OutputStreamWriter(out, Utils.CHARSET_NAME_UTF_8);
			w.write(text);
			w.close();
		}

		// TODO add our JFire security domains


		// *** work necessary for NightLabsCascadedAuthenticationJBoss ***
		// check/modify ${jboss.conf}/standardjboss.xml and REBOOT if changes occured

		// create ${jboss.bin}/CascadedAuthenticationClientInterceptor.properties if not yet existent
		// jboss' bin is our current working directory
		destFile = new File("CascadedAuthenticationClientInterceptor.properties");
		if (!destFile.exists()) {
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
