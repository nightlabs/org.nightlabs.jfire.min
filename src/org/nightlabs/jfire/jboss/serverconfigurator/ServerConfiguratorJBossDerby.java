package org.nightlabs.jfire.jboss.serverconfigurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.db.DatabaseAlreadyExistsException;
import org.nightlabs.util.IOUtil;

/**
 * This implementation of {@link ServerConfigurator} does the same as {@link ServerConfiguratorJBossMySQL} but
 * instead of using MySQL, it uses Derby (as the name implies).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerConfiguratorJBossDerby
extends ServerConfiguratorJBoss
{
	private static final Logger logger = Logger.getLogger(ServerConfiguratorJBossDerby.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBoss#doConfigureServer()
	 */
	@Override
	protected void doConfigureServer() throws ServerConfigurationException
	{
		String needle = "derby";
		if (getJFireServerConfigModule().getDatabase().getDatabaseDriverName_noTx().indexOf(needle) < 0)
			throw new ServerConfigurationException("Database driver seems not to be Derby! Mismatch of ServerConfigurator and database configuration!");
		if (getJFireServerConfigModule().getDatabase().getDatabaseDriverName_localTx().indexOf(needle) < 0)
			throw new ServerConfigurationException("Database configuration is invalid! The driver for localTx does not match, even though noTx driver seems ok!");
		if (getJFireServerConfigModule().getDatabase().getDatabaseDriverName_xa().indexOf(needle) < 0)
			throw new ServerConfigurationException("Database configuration is invalid! The driver for XA does not match, even though noTx and localTx drivers seem ok!");

		try {
			super.doConfigureServer();

			// jbossDeployDir is ${jboss}/server/default/deploy - not ${jboss}/server/default/deploy/JFire.last
			File jbossDeployDir = new File(getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getParentFile().getAbsoluteFile();
			File jbossConfDir = new File(jbossDeployDir.getParentFile(), "conf");
			File jbossDeployJmsDir = new File(jbossDeployDir, "jms");
	
			boolean redeployJMS = false;
	
			// create the database
			String databaseName = getJFireServerConfigModule().getDatabase().getDatabasePrefix() + "JBossMQ" + getJFireServerConfigModule().getDatabase().getDatabaseSuffix();
			String databaseURL = getJFireServerConfigModule().getDatabase().getDatabaseURL(databaseName);
			DatabaseAdapter databaseAdapter = getJFireServerConfigModule().getDatabase().instantiateDatabaseAdapter();
			try {
				databaseAdapter.createDatabase(getJFireServerConfigModule(), databaseURL);
				// if the database was not existing before, we cause the JMS to be redeployed
				redeployJMS = true;
			} catch (DatabaseAlreadyExistsException x) {
				// the database already exists - ignore
			}
	
			configureDerbyDsXml(jbossDeployDir, databaseName, databaseURL);
			
			boolean deletedDeploymentDescriptor = false;
			deletedDeploymentDescriptor |= configureHsqldbJdbc2ServiceXml(jbossDeployJmsDir);
			deletedDeploymentDescriptor |= configureHsqldbJdbcStateServiceXml(jbossDeployJmsDir);
			deletedDeploymentDescriptor |= configureHsqldbDsXml(jbossDeployDir);
			
			if (deletedDeploymentDescriptor)
				waitForServer();
	
			configureJfireJBossmqDerbyJdbcStateServiceXml(jbossDeployJmsDir);
			configureJmsDerbyJdbc2Service(jbossDeployJmsDir);
			configureEjbDeployerXml(jbossDeployDir);
			configureLoginConfigXmlDerby(jbossConfDir);
			
			if (redeployJMS)
				redeployJms(jbossDeployDir, jbossDeployJmsDir);
			
		} catch(Exception e) {
			throw new ServerConfigurationException("Server configuration failed in server configurator "+getClass().getName(), e);
		}
	}

	private void redeployJms(File jbossDeployDir, File jbossDeployJmsDir)
	{
		File jbossDir = jbossDeployDir.getParentFile();
		File tmpJmsDir = new File(jbossDir, "jms.bak");
		if (!jbossDeployJmsDir.renameTo(tmpJmsDir))
			logger.error("Moving JMS deploy directory temporarily from " + jbossDeployJmsDir.getAbsolutePath() + " to " + tmpJmsDir.getAbsolutePath() + " failed!!!");

		File derbyDSFile = new File(jbossDeployDir, "derby-ds.xml");
		File tmpDerbyDSFile = new File(jbossDeployDir, "derby-ds.xml.bak");

		if (!derbyDSFile.renameTo(tmpDerbyDSFile))
			logger.error("Renaming derby-ds deployment descriptor temporarily from " + derbyDSFile.getAbsolutePath() + " to " + tmpDerbyDSFile.getAbsolutePath() + " failed!!!");

		// give jboss some time to undeploy
		waitForServer();

		// and redeploy

		if (!tmpDerbyDSFile.renameTo(derbyDSFile))
			logger.error("Renaming derby-ds deployment descriptor back from temporary name " + tmpDerbyDSFile.getAbsolutePath() + " to " + derbyDSFile.getAbsolutePath() + " failed!!!");

		if (!tmpJmsDir.renameTo(jbossDeployJmsDir))
			logger.error("Moving JMS deploy directory back from temporary location " + tmpJmsDir.getAbsolutePath() + " to " + jbossDeployJmsDir.getAbsolutePath() + " failed!!!");
	}

	private void configureLoginConfigXmlDerby(File jbossConfDir) throws FileNotFoundException, IOException, UnsupportedEncodingException
	{
		// check/modify ${jboss.conf}/login-config.xml and REBOOT if changes occured
		File destFile = new File(jbossConfDir, "login-config.xml");
		String text = IOUtil.readTextFile(destFile);
		if (text.indexOf("java:/DefaultDS") >= 0) {
			setRebootRequired(true); // this is a must, because the conf directory doesn't support redeployment
			text = text.replaceAll("java:/DefaultDS", "java:/JFireJBossMQDS");

			IOUtil.writeTextFile(destFile, text);
		}
	}

	private void configureEjbDeployerXml(File jbossDeployDir) throws FileNotFoundException, IOException, UnsupportedEncodingException
	{
		// modify the timer service - it shouldn't use persistence
		// ${jboss.deploy}/ejb-deployer.xml
		File destFile = new File(jbossDeployDir, "ejb-deployer.xml");
		String text = IOUtil.readTextFile(destFile);
		String modifiedMarker = "!!!ModifiedByJFire!!!";
		if (text.indexOf(modifiedMarker) < 0) {
			backup(destFile);
			
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			String replacementText = "<!-- "
					+ modifiedMarker
					+ " Do not change this line!!! The modification has been done by "
					+ ServerConfiguratorJBossDerby.class.getName()
					+ ". -->\n"
					+ "  <!-- A persistence policy that persistes timers to a database\n"
					+ "  <mbean code=\"org.jboss.ejb.txtimer.DatabasePersistencePolicy\" name=\"jboss.ejb:service=EJBTimerService,persistencePolicy=database\">\n"
					+ "    <!- DataSource JNDI name ->\n"
					+ "    <depends optional-attribute-name=\"DataSource\"JFire_JBossMQ>jboss.jca:service=DataSourceBinding,name=JFireJBossMQDS</depends>\n"
					+ "    <!- The plugin that handles database persistence ->\n"
					+ "    <attribute name=\"DatabasePersistencePlugin\">org.jboss.ejb.txtimer.GeneralPurposeDatabasePersistencePlugin</attribute>\n"
					+ "  </mbean>\n"
					+ "  -->\n"
					+ "  <!-- For JFire, there is no need to persist the timer -->\n"
					+ "  <mbean code=\"org.jboss.ejb.txtimer.NoopPersistencePolicy\" name=\"jboss.ejb:service=EJBTimerService,persistencePolicy=noop\"/>\n"
					+ "\n";
			Pattern pattern = Pattern
					.compile("<mbean[^<]*?EJBTimerService,persistencePolicy=database(.|\\n)*?</mbean>");
			text = pattern.matcher(text).replaceAll(replacementText);

			IOUtil.writeTextFile(destFile, text);
		}
	}

	private void configureJmsDerbyJdbc2Service(File jbossDeployJmsDir) throws IOException
	{
		// ${jboss.deploy}/jms/derby-jdbc2-service.xml
		File destFile = new File(jbossDeployJmsDir, "derby-jdbc2-service.xml");
		if (!destFile.exists()) {
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			IOUtil.copyResource(ServerConfiguratorJBossDerby.class,
					"derby-jdbc2-service.xml.jfire", destFile);
		}
	}

	private void configureJfireJBossmqDerbyJdbcStateServiceXml(File jbossDeployJmsDir) throws IOException
	{
		// deploy more files
		// ${jboss.deploy}/jms/jfire-jbossmq-derby-jdbc-state-service.xml
		File destFile = new File(jbossDeployJmsDir, "jfire-jbossmq-derby-jdbc-state-service.xml");
		if (!destFile.exists()) {
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			IOUtil.copyResource(ServerConfiguratorJBossDerby.class,
					"jfire-jbossmq-derby_mysql-jdbc-state-service.xml.jfire", destFile);
		}
	}

	private boolean configureHsqldbDsXml(File jbossDeployDir)
	throws IOException
	{
		boolean deletedDeploymentDescriptor = false;
		// ${jboss.deploy}/hsqldb-ds.xml
		File destFile = new File(jbossDeployDir, "hsqldb-ds.xml");
		if (destFile.exists()) {
			logger.info("File " + destFile.getAbsolutePath() + " exists. Will move it to backup in order to deactivate it.");
			deletedDeploymentDescriptor = true;
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

//			File newFileName = new File(destFile.getParentFile(), "hsqldb-ds.xml.bak");
//			destFile.renameTo(newFileName);
			moveToBackup(destFile);
		}
		return deletedDeploymentDescriptor;
	}

	private boolean configureHsqldbJdbcStateServiceXml(File jbossDeployJmsDir) throws IOException
	{
		boolean deletedDeploymentDescriptor = false;
		// ${jboss.deploy}/jms/hsqldb-jdbc-state-service.xml
		File destFile = new File(jbossDeployJmsDir, "hsqldb-jdbc-state-service.xml");
		if (destFile.exists()) {
			logger.info("File " + destFile.getAbsolutePath() + " exists. Will move it to backup in order to deactivate it.");
			deletedDeploymentDescriptor = true;
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			moveToBackup(destFile);
//			File newFileName = new File(destFile.getParentFile(),
//					"hsqldb-jdbc-state-service.xml.bak");
//			destFile.renameTo(newFileName);
		}
		return deletedDeploymentDescriptor;
	}

	private boolean configureHsqldbJdbc2ServiceXml(File jbossDeployJmsDir) throws IOException
	{
		boolean deletedDeploymentDescriptor = false;
		// remove the following files (we rename them into *.bak, if they exist)
		// this needs to be done before deploying the other files below
		// ${jboss.deploy}/jms/hsqldb-jdbc2-service.xml
		File destFile = new File(jbossDeployJmsDir, "hsqldb-jdbc2-service.xml");
		if (destFile.exists()) {
			logger.info("File " + destFile.getAbsolutePath() + " exists. Will move it to backup in order to deactivate it.");
			deletedDeploymentDescriptor = true;
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			moveToBackup(destFile);
//			File newFileName = new File(destFile.getParentFile(), "hsqldb-jdbc2-service.xml.bak");
//			destFile.renameTo(newFileName);
		}
		return deletedDeploymentDescriptor;
	}

	private void configureDerbyDsXml(File jbossDeployDir, String databaseName, String databaseURL) throws FileNotFoundException, IOException, UnsupportedEncodingException
	{
		// *** work necessary for switching JBossMQ from HSQL to Derby ***
		// check the following files and deploy/replace them if necessary
		// ${jboss.deploy}/derby-ds.xml
		File destFile = new File(jbossDeployDir, "derby-ds.xml");
		if (!destFile.exists()) {
			logger.info("File " + destFile.getAbsolutePath() + " does not exist. Will create it from template.");
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			InputStream in = ServerConfiguratorJBossDerby.class.getResourceAsStream("derby-ds.xml.jfire");
			String text = IOUtil.readTextFile(in);
			in.close();

			// not very efficient mechanism, but it should work
			text = text.replaceAll("\\$\\{databaseDriverName_noTx\\}", getJFireServerConfigModule().getDatabase().getDatabaseDriverName_noTx());
			text = text.replaceAll("\\$\\{databaseDriverName_localTx\\}", getJFireServerConfigModule().getDatabase().getDatabaseDriverName_localTx());
			text = text.replaceAll("\\$\\{databaseDriverName_xa\\}", getJFireServerConfigModule().getDatabase().getDatabaseDriverName_xa());
			text = text.replaceAll("\\$\\{databaseName\\}", databaseName);
			text = text.replaceAll("\\$\\{databaseURL\\}", databaseURL);
			text = text.replaceAll("\\$\\{databaseUserName\\}", getJFireServerConfigModule().getDatabase().getDatabaseUserName());
			text = text.replaceAll("\\$\\{databasePassword\\}", getJFireServerConfigModule().getDatabase().getDatabasePassword());
			text = text.replaceAll("\\$\\{datasourceMetadataTypeMapping\\}", getJFireServerConfigModule().getDatabase().getDatasourceMetadataTypeMapping());

			IOUtil.writeTextFile(destFile, text);
		}
	}

	@Override
	protected void undoConfigureServer() throws ServerConfigurationException {
		File jbossDeployDir = new File(getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getParentFile().getAbsoluteFile();
//		File jbossConfDir = new File(jbossDeployDir.getParentFile(), "conf");
		File jbossDeployJmsDir = new File(jbossDeployDir, "jms");

		File[] filesToRestore = {
				new File(jbossDeployDir, "hsqldb-ds.xml"),
//				new File(jbossConfDir, "login-config.xml"), // no need to restore this file, because it is already restored by super.undoConfigureServer()!
				new File(jbossDeployDir, "ejb-deployer.xml"),
				new File(jbossDeployJmsDir, "hsqldb-jdbc-state-service.xml"),
				new File(jbossDeployJmsDir, "hsqldb-jdbc2-service.xml"),
		};

		try {
			for (File f : filesToRestore)
				restore(f);

			new File(jbossDeployDir, "derby-ds.xml").delete();
			new File(jbossDeployJmsDir, "derby-jdbc2-service.xml").delete();
			new File(jbossDeployJmsDir, "jfire-jbossmq-derby-jdbc-state-service.xml").delete();
		} catch (IOException e) {
			throw new ServerConfigurationException(e);
		}

		super.undoConfigureServer();
	}

}
