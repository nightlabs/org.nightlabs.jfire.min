package org.nightlabs.jfire.jboss.serverconfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.db.DatabaseAlreadyExistsException;
import org.nightlabs.util.IOUtil;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.CachedXPathAPI;

/**
 * This implementation of {@link ServerConfigurator} will modify your JBossMQ
 * and Timer configuration as described in
 * https://www.jfire.org/modules/phpwiki/index.php/Switch%20JBossMQ%20from%20HSQL%20to%20MySQL
 * and it creates the database <code>${databasePrefix}JBossMQ${databaseSuffix}</code> (prefix + suffix
 * are taken from the {@link JFireServerConfigModule})
 * in your MySQL server (if it does not yet exist).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerConfiguratorJBossMySQL
extends ServerConfiguratorJBoss
{
	private static final Logger logger = Logger.getLogger(ServerConfiguratorJBossMySQL.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBoss#doConfigureServer()
	 */
	@Override
	protected void doConfigureServer() throws ServerConfigurationException
	{
		String needle = "mysql";
		if (getJFireServerConfigModule().getDatabase().getDatabaseDriverName_noTx().indexOf(needle) < 0)
			throw new ServerConfigurationException("Database driver seems not to be MySQL! Mismatch of ServerConfigurator and database configuration!");
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
	
			configureMySqlDsXml(jbossDeployDir, databaseName, databaseURL);
			
			boolean deletedDeploymentDescriptor = false;
			deletedDeploymentDescriptor |= configureHsqldbJdbc2ServiceXml(jbossDeployJmsDir);
			deletedDeploymentDescriptor |= configureHsqldbJdbcStateServiceXml(jbossDeployJmsDir);
			deletedDeploymentDescriptor |= configureHsqldbDsXml(jbossDeployDir);
			
			if (deletedDeploymentDescriptor)
				waitForServer();
	
			configureJfireJBossmqMysqlJdbcStateServiceXml(jbossDeployJmsDir);
			configureJmsMysqlJdbc2Service(jbossDeployJmsDir);
			configureEjbDeployerXml(jbossDeployDir);
			configureLoginConfigXmlMySQL(jbossConfDir);
			
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

		File mysqlDSFile = new File(jbossDeployDir, "mysql-ds.xml");
		File tmpMysqlDSFile = new File(jbossDeployDir, "mysql-ds.xml.bak");

		if (!mysqlDSFile.renameTo(tmpMysqlDSFile))
			logger.error("Renaming mysql-ds deployment descriptor temporarily from " + mysqlDSFile.getAbsolutePath() + " to " + tmpMysqlDSFile.getAbsolutePath() + " failed!!!");

		// give jboss some time to undeploy
		waitForServer();

		// and redeploy

		if (!tmpMysqlDSFile.renameTo(mysqlDSFile))
			logger.error("Renaming mysql-ds deployment descriptor back from temporary name " + tmpMysqlDSFile.getAbsolutePath() + " to " + mysqlDSFile.getAbsolutePath() + " failed!!!");

		if (!tmpJmsDir.renameTo(jbossDeployJmsDir))
			logger.error("Moving JMS deploy directory back from temporary location " + tmpJmsDir.getAbsolutePath() + " to " + jbossDeployJmsDir.getAbsolutePath() + " failed!!!");
	}

	private void configureLoginConfigXmlMySQL(File jbossConfDir) throws FileNotFoundException, IOException, UnsupportedEncodingException
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

	private void configureEjbDeployerXml(File jbossDeployDir)
	throws IOException, UnsupportedEncodingException, SAXException, DOMException, TransformerException
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

//			String replacementText = "<!-- "
//					+ modifiedMarker
//					+ " Do not change this line!!! The modification has been done by "
//					+ ServerConfiguratorJBossMySQL.class.getName()
//					+ ". -->\n"
//					+ "  <!-- A persistence policy that persistes timers to a database\n"
//					+ "  <mbean code=\"org.jboss.ejb.txtimer.DatabasePersistencePolicy\" name=\"jboss.ejb:service=EJBTimerService,persistencePolicy=database\">\n"
//					+ "    <!- DataSource JNDI name ->\n"
//					+ "    <depends optional-attribute-name=\"DataSource\"JFire_JBossMQ>jboss.jca:service=DataSourceBinding,name=JFireJBossMQDS</depends>\n"
//					+ "    <!- The plugin that handles database persistence ->\n"
//					+ "    <attribute name=\"DatabasePersistencePlugin\">org.jboss.ejb.txtimer.GeneralPurposeDatabasePersistencePlugin</attribute>\n"
//					+ "  </mbean>\n"
//					+ "  -->\n"
//					+ "  <!-- For JFire, there is no need to persist the timer -->\n"
//					+ "  <mbean code=\"org.jboss.ejb.txtimer.NoopPersistencePolicy\" name=\"jboss.ejb:service=EJBTimerService,persistencePolicy=noop\"/>\n"
//					+ "\n";
//			Pattern pattern = Pattern
//					.compile("<mbean[^<]*?EJBTimerService,persistencePolicy=database(.|\\n)*?</mbean>");
//			text = pattern.matcher(text).replaceAll(replacementText);
//
//			IOUtil.writeTextFile(destFile, text);

			DOMParser parser = new DOMParser();
			InputStream in = new FileInputStream(destFile);
			try {
				parser.parse(new InputSource(in));
			} finally {
				in.close();
			}
			Document document = parser.getDocument();
			CachedXPathAPI xpa = new CachedXPathAPI();

			Node n;
			for (NodeIterator ni1 = xpa.selectNodeIterator(document, "//server/mbean[@code=\"org.jboss.ejb.txtimer.DatabasePersistencePolicy\"]"); (n = ni1.nextNode()) != null; ) {
				if (!NLDOMUtil.getAttributeValue(n, "name").contains("service=EJBTimerService"))
					continue;

				Comment comment = document.createComment(
						"\n" +
						modifiedMarker + " Do not change this line!!! The modification has been done by " +
						this.getClass().getName() + "\n\n  " +
						NLDOMUtil.getElementAsString((Element) n, IOUtil.CHARSET_NAME_UTF_8).replaceAll("--", "- -") +
						"\n\n"
				);
				Element mbeanElement = document.createElement("mbean");
				mbeanElement.setAttribute("code", "org.jboss.ejb.txtimer.NoopPersistencePolicy");
				mbeanElement.setAttribute("name", "jboss.ejb:service=EJBTimerService,persistencePolicy=noop");
				n.getParentNode().insertBefore(comment, n);
				n.getParentNode().insertBefore(mbeanElement, n);
				n.getParentNode().removeChild(n);
			}

			FileOutputStream out = new FileOutputStream(destFile);
			try {
				NLDOMUtil.writeDocument(
						document,
						out,
						"UTF-8"
				);
			} finally {
				out.close();
			}
		}
	}

	private void configureJmsMysqlJdbc2Service(File jbossDeployJmsDir) throws IOException
	{
		// ${jboss.deploy}/jms/mysql-jdbc2-service.xml
		File destFile = new File(jbossDeployJmsDir, "mysql-jdbc2-service.xml");
		if (!destFile.exists()) {
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			IOUtil.copyResource(ServerConfiguratorJBossMySQL.class,
					"mysql-jdbc2-service.xml.jfire", destFile);
		}
	}

	private void configureJfireJBossmqMysqlJdbcStateServiceXml(File jbossDeployJmsDir) throws IOException
	{
		// deploy more files
		// ${jboss.deploy}/jms/jfire-jbossmq-mysql-jdbc-state-service.xml
		File destFile = new File(jbossDeployJmsDir, "jfire-jbossmq-mysql-jdbc-state-service.xml");
		if (!destFile.exists()) {
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			IOUtil.copyResource(ServerConfiguratorJBossMySQL.class,
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

	private void configureMySqlDsXml(File jbossDeployDir, String databaseName, String databaseURL) throws FileNotFoundException, IOException, UnsupportedEncodingException
	{
		// *** work necessary for switching JBossMQ from HSQL to MySQL ***
		// check the following files and deploy/replace them if necessary
		// ${jboss.deploy}/mysql-ds.xml
		File destFile = new File(jbossDeployDir, "mysql-ds.xml");
		if (!destFile.exists()) {
			logger.info("File " + destFile.getAbsolutePath() + " does not exist. Will create it from template.");
			if (rebootOnDeployDirChanges)
				setRebootRequired(true);

			InputStream in = ServerConfiguratorJBossMySQL.class.getResourceAsStream("mysql-ds.xml.jfire");
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

			new File(jbossDeployDir, "mysql-ds.xml").delete();
			new File(jbossDeployJmsDir, "mysql-jdbc2-service.xml").delete();
			new File(jbossDeployJmsDir, "jfire-jbossmq-mysql-jdbc-state-service.xml").delete();
		} catch (IOException e) {
			throw new ServerConfigurationException(e);
		}

		super.undoConfigureServer();
	}

}
