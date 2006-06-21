/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.servermanager.config;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;
import org.nightlabs.jfire.server.LocalServer;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapterHSQL;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapterMySQL;
import org.nightlabs.util.Utils;

/**
 * @author marco
 */
public class JFireServerConfigModule extends ConfigModule
{
	public static Logger LOGGER = Logger.getLogger(JFireServerConfigModule.class);

	public static class J2ee implements Serializable
	{
		private String j2eeDeployBaseDirectory;

		/**
		 * @return Returns the j2eeDeployBaseDirectory.
		 */
		public String getJ2eeDeployBaseDirectory() {
			return j2eeDeployBaseDirectory;
		}
		/**
		 * @param deployBaseDirectory The j2eeDeployBaseDirectory to set.
		 */
		public void setJ2eeDeployBaseDirectory(String deployBaseDirectory) {
			j2eeDeployBaseDirectory = deployBaseDirectory;
		}

		public void init()
		{
			if (j2eeDeployBaseDirectory == null)
				j2eeDeployBaseDirectory = "../server/default/deploy/JFire.last/";
		}
	}

	public static class Database implements Serializable
	{
		public static final String DATABASE_NAME_VAR = "{databaseName}";

		public static String DEFAULTS_DEFAULT_KEY = "MySQL";
		public static Map<String, Database> DEFAULTS = new HashMap<String, Database>();
		static {
			try {
				Database db;

				// Default values for Mckoi
				db = new Database();
				db._init();
				db.setDatabaseDriverName("com.mckoi.JDBCDriver");
				db.setDatabaseURL("jdbc:mckoi:local:/" + new File(Utils.getTempDir(), "jfire-mckoi").getAbsolutePath() + File.separatorChar + DATABASE_NAME_VAR + File.separatorChar + "db.conf");
//				db.setDatabaseUserName("jfire");
//				db.setDatabasePassword("jfire_password");
				db.setDatabaseAdapter("org.nightlabs.jfire.databaseadaptermckoi.DatabaseAdapterMckoi");
				db.setDatasourceMetadataTypeMapping("mySQL"); // TODO - i have no idea???!! 
				DEFAULTS.put("Mckoi", db);

				// Default values for HSQLDB (file)
				db = new Database();
				db._init();
				db.setDatabaseDriverName("org.hsqldb.jdbcDriver");
				db.setDatabaseURL("jdbc:hsqldb:file:" + new File(Utils.getTempDir(), "jfire-hsqldb").getAbsolutePath() + File.separatorChar + DATABASE_NAME_VAR);
				db.setDatabaseUserName("sa");
				db.setDatabasePassword("");
				db.setDatabaseAdapter(DatabaseAdapterHSQL.class.getName());
				db.setDatasourceMetadataTypeMapping("mySQL"); // TODO - what value??!
				DEFAULTS.put("HSQL (file)", db);

				// Default values for HSQLDB (memory)
				db = new Database();
				db._init();
				db.setDatabaseDriverName("org.hsqldb.jdbcDriver");
				db.setDatabaseURL("jdbc:hsqldb:mem:" + new File(Utils.getTempDir(), "jfire-hsqldb").getAbsolutePath() + File.separatorChar + DATABASE_NAME_VAR);
				db.setDatabaseUserName("sa");
				db.setDatabasePassword("");
				db.setDatabaseAdapter(DatabaseAdapterHSQL.class.getName());
				db.setDatasourceMetadataTypeMapping("mySQL"); // TODO - what value??!
				DEFAULTS.put("HSQL (memory)", db);

				// Default values for MySQL
				db = new Database();
				db._init();
				db.setDatabaseDriverName("com.mysql.jdbc.Driver");
				db.setDatabaseURL("jdbc:mysql://localhost/" + DATABASE_NAME_VAR);
//				db.setDatabaseUserName("jfire");
//				db.setDatabasePassword("jfire_password");
				db.setDatabaseAdapter(DatabaseAdapterMySQL.class.getName());
				db.setDatasourceMetadataTypeMapping("mySQL");
				DEFAULTS.put("MySQL", db);

			} catch (Throwable t) {
				LOGGER.error("Creating database default values failed!", t);
			}
		}

		private String databaseDriverName;
		private String databaseURL;
		private String databasePrefix;
		private String databaseSuffix;
		private String databaseUserName;
		private String databasePassword;

		private String databaseAdapter;
		private String datasourceMetadataTypeMapping;

		/**
		 * @return Returns the databaseDriverName.
		 */
		public String getDatabaseDriverName() {
			return databaseDriverName;
		}
		/**
		 * @param databaseDriverName The databaseDriverName to set.
		 */
		public void setDatabaseDriverName(String _databaseDriverName) {
			this.databaseDriverName = _databaseDriverName;
		}

		public String getDatabaseURL()
		{
			return databaseURL;
		}
		/**
		 * Returns the databaseURL after the {databaseName} has been replaced by the given
		 * value. In order to connect to the database server without any database, use
		 * either <code>null</code> or an empty string as <code>databaseName</code>.
		 *
		 * @param databaseName The name that should be replaced into the database URL template.
		 *
		 * @return Returns a final JDBC connection string for connecting to one database (but without user name and password)
		 */
		public String getDatabaseURL(String databaseName)
		{
			if (databaseName == null)
				databaseName = "";

			return databaseURL.replace(DATABASE_NAME_VAR, databaseName);
		}
		public void setDatabaseURL(String databaseURL)
		{
			if (databaseURL == null)
				throw new IllegalArgumentException("databaseURL must not be null!");

			if (databaseURL.indexOf(DATABASE_NAME_VAR) < 0)
				throw new IllegalArgumentException("databaseURL must contain \"" + DATABASE_NAME_VAR + "\"!");

			this.databaseURL = databaseURL;
		}
		/**
		 * @return Returns the databaseURLPrefix.
		 */
		public String getDatabasePrefix() {
			return databasePrefix;
		}
		/**
		 * @param databaseURLPrefix The databaseURLPrefix to set.
		 */
		public void setDatabasePrefix(String _databasePrefix) {
			this.databasePrefix = _databasePrefix;
		}
		/**
		 * @return Returns the databaseURLSuffix.
		 */
		public String getDatabaseSuffix() {
			return databaseSuffix;
		}
		/**
		 * @param databaseURLSuffix The databaseURLSuffix to set.
		 */
		public void setDatabaseSuffix(String _databaseSuffix) {
			this.databaseSuffix = _databaseSuffix;
		}
		/**
		 * @return Returns the databaseUserName.
		 */
		public String getDatabaseUserName() {
			return databaseUserName;
		}
		/**
		 * @param databaseUserName The databaseUserName to set.
		 */
		public void setDatabaseUserName(String _databaseUserName) {
			this.databaseUserName = _databaseUserName;
		}
		/**
		 * @return Returns the databasePassword.
		 */
		public String getDatabasePassword() {
			return databasePassword;
		}
		/**
		 * @param databasePassword The databasePassword to set.
		 */
		public void setDatabasePassword(String _databasePassword) {
			this.databasePassword = _databasePassword;
		}

		public String getDatasourceMetadataTypeMapping()
		{
			return datasourceMetadataTypeMapping;
		}
		public void setDatasourceMetadataTypeMapping(
				String datasourceMetadataTypeMapping)
		{
			this.datasourceMetadataTypeMapping = datasourceMetadataTypeMapping;
		}

		/**
		 * @return Returns the databaseAdapter.
		 */
		public String getDatabaseAdapter() {
			return databaseAdapter;
		}
		/**
		 * @param databaseAdapter The databaseAdapter to set.
		 */
		public void setDatabaseAdapter(String databaseCreator) {
			this.databaseAdapter = databaseCreator;
		}

		protected void _init()
		{
			if (databaseDriverName == null)
				setDatabaseDriverName("com.mysql.jdbc.Driver");

			if (databaseURL == null)
				setDatabaseURL("jdbc:mysql://localhost/" + DATABASE_NAME_VAR);

			if (datasourceMetadataTypeMapping == null)
				setDatasourceMetadataTypeMapping("mySQL");
				
			if (databasePrefix == null)	
				setDatabasePrefix("JFire_");

			if (databaseSuffix == null)	
				setDatabaseSuffix("");

			if (databaseUserName == null)
				setDatabaseUserName("jfire");

			if (databasePassword == null)
				setDatabasePassword("jfire_password");

			if (databaseAdapter == null)
				setDatabaseAdapter(DatabaseAdapterMySQL.class.getName());
		}

		public void init()
		{
			_init();

			LOGGER.info("databaseDriverName = "+databaseDriverName);
			LOGGER.info("databaseURL = "+databaseURL);
			LOGGER.info("databasePrefix = "+databasePrefix);
			LOGGER.info("databaseSuffix = "+databaseSuffix);
			LOGGER.info("databaseUserName = "+databaseUserName);
			LOGGER.info("databasePassword = "+databasePassword);
			LOGGER.info("databaseAdapter = "+databaseAdapter);
			LOGGER.info("datasourceMetadataTypeMapping = "+datasourceMetadataTypeMapping);
		}

		public void loadDefaults(String defaultKey)
		{
			Database db = DEFAULTS.get(defaultKey);
			if (db == null)
				throw new IllegalArgumentException("No defaults known with defaultKey=" + defaultKey);

			setDatabaseDriverName(db.getDatabaseDriverName());
			setDatabaseURL(db.getDatabaseURL());
			setDatabasePrefix(db.getDatabasePrefix());
			setDatabaseSuffix(db.getDatabaseSuffix());
			setDatabaseUserName(db.getDatabaseUserName());
			setDatabasePassword(db.getDatabasePassword());
			setDatabaseAdapter(db.getDatabaseAdapter());
		}
	}

	public static class JDO implements Serializable
	{
		public static final String ORGANISATION_ID_VAR = "{organisationID}";

		private String jdoConfigDirectory;

		private String datasourceConfigFile;
		private String datasourceTemplateDSXMLFile;

		private String jdoConfigFile;
		private String jdoTemplateDSXMLFile;

		/**
		 * @return Returns the jdoConfigDirectory.
		 */
		public String getJdoConfigDirectory() {
			return jdoConfigDirectory;
		}
		public String getJdoConfigDirectory(String organisationID)
		{
			if (organisationID == null || "".equals(organisationID))
				throw new IllegalArgumentException("organisationID must not be null or empty string!");

			return jdoConfigDirectory.replace(ORGANISATION_ID_VAR, organisationID);
		}
		/**
		 * @param jdoConfigDirectory The jdoConfigDirectory to set.
		 */
		public void setJdoConfigDirectory(String _jdoConfigDirectory) {
			if (_jdoConfigDirectory == null)
				throw new IllegalArgumentException("jdoConfigDirectory must not be null!");

			if (_jdoConfigDirectory.indexOf(ORGANISATION_ID_VAR) < 0)
				throw new IllegalArgumentException("jdoConfigDirectory must contain \"" + ORGANISATION_ID_VAR + "\"!");

			this.jdoConfigDirectory = _jdoConfigDirectory;
		}

		public String getDatasourceConfigFile(String organisationID)
		{
			if (organisationID == null || "".equals(organisationID))
				throw new IllegalArgumentException("organisationID must not be null or empty string!");

			return datasourceConfigFile.replace(ORGANISATION_ID_VAR, organisationID);
		}

		public String getDatasourceConfigFile()
		{
			return datasourceConfigFile;
		}

		public void setDatasourceConfigFile(String datasourceConfigFile)
		{
			if (datasourceConfigFile == null)
				throw new IllegalArgumentException("datasourceConfigFile must not be null!");

			if (datasourceConfigFile.indexOf(ORGANISATION_ID_VAR) < 0)
				throw new IllegalArgumentException("datasourceConfigFile must contain \"" + ORGANISATION_ID_VAR + "\"!");

			this.datasourceConfigFile = datasourceConfigFile;
		}

		public String getJdoConfigFile(String organisationID)
		{
			if (organisationID == null || "".equals(organisationID))
				throw new IllegalArgumentException("organisationID must not be null or empty string!");

			return jdoConfigFile.replace(ORGANISATION_ID_VAR, organisationID);
		}
		public String getJdoConfigFile()
		{
			return jdoConfigFile;
		}

		public void setJdoConfigFile(String jdoConfigFile)
		{
			if (jdoConfigFile == null)
				throw new IllegalArgumentException("jdoConfigFile must not be null!");

			if (jdoConfigFile.indexOf(ORGANISATION_ID_VAR) < 0)
				throw new IllegalArgumentException("jdoConfigFile must contain \"" + ORGANISATION_ID_VAR + "\"!");

			this.jdoConfigFile = jdoConfigFile;
		}

		public String getDatasourceTemplateDSXMLFile()
		{
			return datasourceTemplateDSXMLFile;
		}
		public void setDatasourceTemplateDSXMLFile(
				String datasourceTemplateDSXMLFile)
		{
			this.datasourceTemplateDSXMLFile = datasourceTemplateDSXMLFile;
		}

		/**
		 * @return Returns the jdoTemplateDSXMLFile.
		 */
		public String getJdoTemplateDSXMLFile() {
			return jdoTemplateDSXMLFile;
		}
		/**
		 * @param jdoTemplateDSXMLFile The jdoTemplateDSXMLFile to set.
		 */
		public void setJdoTemplateDSXMLFile(String jdoTemplateDSXMLFile) {
			this.jdoTemplateDSXMLFile = jdoTemplateDSXMLFile;
		}
		
		public void init()
		{
			if (jdoConfigDirectory == null)
				setJdoConfigDirectory("../server/default/deploy/JFire_JDO_" + ORGANISATION_ID_VAR + ".last/");

			if (datasourceConfigFile == null)
				datasourceConfigFile = "db-" + ORGANISATION_ID_VAR + "-ds.xml";

			if (jdoConfigFile == null)
				jdoConfigFile = "jdo-" + ORGANISATION_ID_VAR + "-ds.xml";

			if (datasourceTemplateDSXMLFile == null)
				datasourceTemplateDSXMLFile = "../server/default/deploy/JFire.last/JFireBase.ear/db-all-ds.template.xml";

			if (jdoTemplateDSXMLFile == null)
				jdoTemplateDSXMLFile = "../server/default/deploy/JFire.last/JFireBase.ear/jdo-jpox-ds.template.xml";

			LOGGER.info("jdoConfigDirectory = "+jdoConfigDirectory);
			LOGGER.info("datasourceConfigFile = "+datasourceConfigFile);
			LOGGER.info("datasourceTemplateDSXMLFile = "+datasourceTemplateDSXMLFile);
			LOGGER.info("jdoConfigFile = "+jdoConfigFile);
			LOGGER.info("jdoTemplateDSXMLFile = "+jdoTemplateDSXMLFile);
		}
	}

	private RootOrganisationCf rootOrganisation = null;
	private ServerCf localServer = null;
	private J2ee j2ee;
	private Database database;
	private JDO jdo;

	public RootOrganisationCf getRootOrganisation()
	{
		return rootOrganisation;
	}
	public void setRootOrganisation(RootOrganisationCf rootOrganisation)
	{
		this.rootOrganisation = rootOrganisation;
	}

	/**
	 * @return Returns the localServer.
	 */
	public ServerCf getLocalServer() {
		return localServer;
	}
	/**
	 * @param localServer The localServer to set.
	 */
	public void setLocalServer(ServerCf _localServer) {
		if (_localServer == null)
			throw new NullPointerException("localServer must not be null!");
		this.localServer = _localServer;
	}
	public LocalServer createJDOLocalServer()
	throws ModuleException
	{
		if (localServer == null)
			throw new NullPointerException("localServer is null! Check basic server configuration!");

		Server server = new Server(localServer.getServerID());
		try {
			BeanUtils.copyProperties(server, localServer);
		} catch (IllegalAccessException e) {
			throw new ModuleException(e);
		} catch (InvocationTargetException e) {
			throw new ModuleException(e);
		}
		LocalServer localServer = new LocalServer(server);

		return localServer;
	}

	/**
	 * @return Returns the j2ee.
	 */
	public J2ee getJ2ee() {
		return j2ee;
	}
	/**
	 * @param j2ee The j2ee to set.
	 */
	public void setJ2ee(J2ee j2ee) {
		this.j2ee = j2ee;
	}

	/**
	 * @return Returns the database.
	 */
	public Database getDatabase() {
		return database;
	}
	/**
	 * @param database The database to set.
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}
	/**
	 * @return Returns the jdo.
	 */
	public JDO getJdo() {
		return jdo;
	}
	/**
	 * @param jdo The jdo to set.
	 */
	public void setJdo(JDO jdo) {
		this.jdo = jdo;
	}


	// *********************************************
	// *** Methods from ConfigModule             ***
	// *********************************************	
	/**
	 * @see org.nightlabs.config.Initializable#init()
	 */
	public void init() throws InitException {
		if (rootOrganisation == null) {
			ServerCf server = new ServerCf("jfire.nightlabs.org");
			server.init();
			server.setServerID("jfire.nightlabs.org");
			server.setServerName("JFire Devil Server");
			rootOrganisation = new RootOrganisationCf("jfire.nightlabs.org", "JFire Devil Root Organisation", server);
		} // if (rootOrganisation == null) {

		if (j2ee == null)
			j2ee = new J2ee();

		if (database == null)
			database = new Database();

		if (jdo == null)
			jdo = new JDO();

		j2ee.init();
		database.init();
		jdo.init();

		if (localServer != null)
			localServer.init();
	}

}
