package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapterDerby;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapterMySQL;

/**
 * The server core database configuration. It is part of the {@link JFireServerConfigModule}.
 *
 * @author Marco Schulze
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DatabaseCf extends JFireServerConfigPart implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class.
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DatabaseCf.class);

	public static final String DATABASE_NAME_VAR = "${databaseName}";

	public static String DEFAULTS_DEFAULT_KEY = "Derby";

	private static Map<String, DatabaseCf> _defaults = null;

	public static Map<String, DatabaseCf> defaults() {
		if (_defaults == null)
			_defaults = createDefaults();

		return _defaults;
	}

	private String databaseDriverName_noTx;
	private String databaseDriverName_localTx;
	private String databaseDriverName_xa;

	private String databaseURL;
	private String databasePrefix;
	private String databaseSuffix;
	private String databaseUserName;
	private String databasePassword;

	private String databaseAdapter;
	private String datasourceMetadataTypeMapping;

	private String datasourceConfigFile;
	private String datasourceTemplateDSXMLFile;
	
	private static Map<String, DatabaseCf> createDefaults()
	{
		Map<String, DatabaseCf> defaults = new HashMap<String, DatabaseCf>();
		try {

			// Default values for Derby
			defaults.put("Derby", createDerbyDefaults());

			// Default values for Mckoi
			//defaults.put("Mckoi", createMckoiDefaults());

			// Default values for MySQL
			defaults.put("MySQL", createMySQLDefaults());

		} catch (Throwable t) {
			logger.error("Creating database default values failed!", t);
		}
		
		return defaults;
	}
	// TODO remove the metadatatypemapping... the template file is configurable, hence we don't need the type mapping here.

	/**
	 * Create default values for the MySQL DB type.
	 * @return The database defaults
	 */
	private static DatabaseCf createMySQLDefaults()
	{
		DatabaseCf db = new DatabaseCf();
		db._init();
		db.setDatabaseDriverName_noTx("com.mysql.jdbc.Driver");
		db.setDatabaseDriverName_localTx("com.mysql.jdbc.Driver");
		db.setDatabaseDriverName_xa("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		db.setDatabaseURL("jdbc:mysql://localhost/" + DATABASE_NAME_VAR);
//				db.setDatabaseUserName("jfire");
//				db.setDatabasePassword("jfire_password");
		db.setDatabaseAdapter(DatabaseAdapterMySQL.class.getName());
		db.setDatasourceMetadataTypeMapping("mySQL");
//				db.setDatasourceConfigFile("db-mysql-" + ORGANISATION_ID_VAR + "-ds.xml");
		db.setDatasourceTemplateDSXMLFile("../server/default/deploy/JFire.last/JFireBase.ear/db-mysql-ds.template.xml");
		return db;
	}

	/**
	 * Create default values for the Derby DB type.
	 * @return The database defaults
	 */
	private static DatabaseCf createDerbyDefaults()
	{
		DatabaseCf db = new DatabaseCf();
		db._init();
		db.setDatabaseDriverName_noTx("org.apache.derby.jdbc.EmbeddedDriver");
		db.setDatabaseDriverName_localTx("org.apache.derby.jdbc.EmbeddedDriver");
		db.setDatabaseDriverName_xa("org.apache.derby.jdbc.EmbeddedXADataSource");
		db.setDatabasePrefix("../server/default/data/derby/" + db.getDatabasePrefix());
		db.setDatabaseURL("jdbc:derby:" + DATABASE_NAME_VAR);
		db.setDatabaseAdapter(DatabaseAdapterDerby.class.getName());
		db.setDatasourceMetadataTypeMapping("Derby");
//				db.setDatasourceConfigFile("db-derby-" + ORGANISATION_ID_VAR + "-ds.xml");
		db.setDatasourceTemplateDSXMLFile("../server/default/deploy/JFire.last/JFireBase.ear/db-derby-ds.template.xml");
		return db;
	}

//	/**
//	 * Create default values for the Mckoi DB type.
//	 * @return The database defaults
//	 * @deprecated Mckoi caused problem with JPOX and we use now Derby instead.
//	 */
//	@SuppressWarnings("unused")
//	private static DatabaseCf createMckoiDefaults()
//	{
//		DatabaseCf db = new DatabaseCf();
//		db._init();
//		db.setDatabaseDriverName("com.mckoi.JDBCDriver");
//		db.setDatabaseURL("jdbc:mckoi:local:/" + new File(IOUtil.getTempDir(), "jfire-mckoi").getAbsolutePath() + File.separatorChar + DATABASE_NAME_VAR + File.separatorChar + "db.conf");
////		db.setDatabaseUserName("jfire");
////		db.setDatabasePassword("jfire_password");
//		db.setDatabaseAdapter("org.nightlabs.jfire.databaseadaptermckoi.DatabaseAdapterMckoi");
//		db.setDatasourceMetadataTypeMapping("mySQL"); // TODO - i have no idea???!!
//		return db;
//	}
	
	protected void _init()
	{
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

		if (databaseDriverName_noTx == null && databaseDriverName_localTx == null && databaseDriverName_xa == null) {
			setDatabaseDriverName_noTx("com.mysql.jdbc.Driver");
			setDatabaseDriverName_localTx("com.mysql.jdbc.Driver");
			setDatabaseDriverName_xa("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		}
		else if (databaseDriverName_noTx == null || databaseDriverName_localTx == null || databaseDriverName_xa == null) {
			String d = databaseDriverName_noTx;
			if (d == null)
				d = databaseDriverName_localTx;
			if (d == null)
				d = databaseDriverName_xa;

			if (databaseDriverName_noTx == null)
				setDatabaseDriverName_noTx(d);

			if (databaseDriverName_localTx == null)
				setDatabaseDriverName_localTx(d);

			if (databaseDriverName_xa == null)
				setDatabaseDriverName_xa(d);
		}

		if (datasourceConfigFile == null)
			setDatasourceConfigFile("db-" + JFireServerConfigModule.ORGANISATION_ID_VAR + "-ds.xml");

		if (datasourceTemplateDSXMLFile == null)
			setDatasourceTemplateDSXMLFile("../server/default/deploy/JFire.last/JFireBase.ear/db-default-ds.template.xml");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.servermanager.config.JFireServerConfigPart#init()
	 */
	@Override
	public void init()
	{
		_init();

		logger.info("databaseDriverName_noTx = "+databaseDriverName_noTx);
		logger.info("databaseDriverName_localTx = "+databaseDriverName_localTx);
		logger.info("databaseDriverName_xa = "+databaseDriverName_xa);
		logger.info("databaseURL = "+databaseURL);
		logger.info("databasePrefix = "+databasePrefix);
		logger.info("databaseSuffix = "+databaseSuffix);
		logger.info("databaseUserName = "+databaseUserName);
//		logger.info("databasePassword = "+databasePassword); // better not to log it (or shall we log as TRACE?): https://www.jfire.org/modules/bugs/view.php?id=824
		logger.info("databaseAdapter = "+databaseAdapter);
		logger.info("datasourceMetadataTypeMapping = "+datasourceMetadataTypeMapping);
	}

	public void loadDefaults(String defaultKey)
	{
		DatabaseCf db = defaults().get(defaultKey);
		if (db == null)
			throw new IllegalArgumentException("No defaults known with defaultKey=" + defaultKey);

//		setDatabaseDriverName(db.getDatabaseDriverName());
		setDatabaseDriverName_noTx(db.getDatabaseDriverName_noTx());
		setDatabaseDriverName_localTx(db.getDatabaseDriverName_localTx());
		setDatabaseDriverName_xa(db.getDatabaseDriverName_xa());
		setDatabaseURL(db.getDatabaseURL());
		setDatabasePrefix(db.getDatabasePrefix());
		setDatabaseSuffix(db.getDatabaseSuffix());
		setDatabaseUserName(db.getDatabaseUserName());
		setDatabasePassword(db.getDatabasePassword());
		setDatabaseAdapter(db.getDatabaseAdapter());
		setDatasourceMetadataTypeMapping(db.getDatasourceMetadataTypeMapping());
		setDatasourceTemplateDSXMLFile(db.getDatasourceTemplateDSXMLFile());
		setDatasourceConfigFile(db.getDatasourceConfigFile());
	}

	public DatabaseAdapter instantiateDatabaseAdapter() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String databaseAdapterClassName = getDatabaseAdapter();
		Class<?> dbAdapterClass = Class.forName(databaseAdapterClassName);
		if (!DatabaseAdapter.class.isAssignableFrom(dbAdapterClass))
			throw new ClassCastException("DatabaseCreatorClass \"" + databaseAdapterClassName + "\" does not implement interface \""+DatabaseAdapter.class.getName()+"\"!");

		return (DatabaseAdapter) dbAdapterClass.newInstance();
	}

	public String getDatasourceConfigFile(String organisationID)
	{
		if (organisationID == null || "".equals(organisationID))
			throw new IllegalArgumentException("organisationID must not be null or empty string!");

		return datasourceConfigFile.replace(JFireServerConfigModule.ORGANISATION_ID_VAR, organisationID);
	}
	
	/**
	 * Get the databaseAdapter.
	 * @return the databaseAdapter
	 */
	public String getDatabaseAdapter()
	{
		return databaseAdapter;
	}

	/**
	 * Set the databaseAdapter.
	 * @param databaseAdapter the databaseAdapter to set
	 */
	public void setDatabaseAdapter(String databaseAdapter)
	{
		this.databaseAdapter = databaseAdapter;
		setChanged();
	}

//	/**
//	 * @return Returns the databaseDriverName.
//	 * @deprecated Use {@link #getDatabaseDriverName_noTx()}, {@link #getDatabaseDriverName_localTx()} or {@link #getDatabaseDriverName_xa()}
//	 */
//	public String getDatabaseDriverName()
//	{
//		return databaseDriverName;
//	}
//
//	/**
//	 * @param databaseDriverName The databaseDriverName to set.
//	 * @deprecated Use {@link #setDatabaseDriverName_noTx(String)}. {@link #setDatabaseDriverName_localTx(String)} or {@link #setDatabaseDriverName_xa(String)}
//	 */
//	public void setDatabaseDriverName(String _databaseDriverName)
//	{
//		this.databaseDriverName = _databaseDriverName;
//		setChanged();
//	}
	
	/**
	 * Get the databaseDriverName_localTx.
	 * @return the databaseDriverName_localTx
	 */
	public String getDatabaseDriverName_localTx()
	{
		return databaseDriverName_localTx;
	}

	/**
	 * Set the databaseDriverName_localTx.
	 * @param databaseDriverName_localTx the databaseDriverName_localTx to set
	 */
	public void setDatabaseDriverName_localTx(String databaseDriverName_localTx)
	{
		this.databaseDriverName_localTx = databaseDriverName_localTx;
		setChanged();
	}

	/**
	 * Get the databaseDriverName_noTx.
	 * @return the databaseDriverName_noTx
	 */
	public String getDatabaseDriverName_noTx()
	{
		return databaseDriverName_noTx;
	}

	/**
	 * Set the databaseDriverName_noTx.
	 * @param databaseDriverName_noTx the databaseDriverName_noTx to set
	 */
	public void setDatabaseDriverName_noTx(String databaseDriverName_noTx)
	{
		this.databaseDriverName_noTx = databaseDriverName_noTx;
		setChanged();
	}

	/**
	 * Get the databaseDriverName_xa.
	 * @return the databaseDriverName_xa
	 */
	public String getDatabaseDriverName_xa()
	{
		return databaseDriverName_xa;
	}

	/**
	 * Set the databaseDriverName_xa.
	 * @param databaseDriverName_xa the databaseDriverName_xa to set
	 */
	public void setDatabaseDriverName_xa(String databaseDriverName_xa)
	{
		this.databaseDriverName_xa = databaseDriverName_xa;
		setChanged();
	}

	/**
	 * Get the databasePassword.
	 * @return the databasePassword
	 */
	public String getDatabasePassword()
	{
		return databasePassword;
	}

	/**
	 * Set the databasePassword.
	 * @param databasePassword the databasePassword to set
	 */
	public void setDatabasePassword(String databasePassword)
	{
		this.databasePassword = databasePassword;
		setChanged();
	}

	/**
	 * Get the databasePrefix.
	 * @return the databasePrefix
	 */
	public String getDatabasePrefix()
	{
		return databasePrefix;
	}

	/**
	 * Set the databasePrefix.
	 * @param databasePrefix the databasePrefix to set
	 */
	public void setDatabasePrefix(String databasePrefix)
	{
		this.databasePrefix = databasePrefix;
		setChanged();
	}

	/**
	 * Get the databaseSuffix.
	 * @return the databaseSuffix
	 */
	public String getDatabaseSuffix()
	{
		return databaseSuffix;
	}

	/**
	 * Set the databaseSuffix.
	 * @param databaseSuffix the databaseSuffix to set
	 */
	public void setDatabaseSuffix(String databaseSuffix)
	{
		this.databaseSuffix = databaseSuffix;
		setChanged();
	}

	/**
	 * Get the databaseUserName.
	 * @return the databaseUserName
	 */
	public String getDatabaseUserName()
	{
		return databaseUserName;
	}

	/**
	 * Set the databaseUserName.
	 * @param databaseUserName the databaseUserName to set
	 */
	public void setDatabaseUserName(String databaseUserName)
	{
		this.databaseUserName = databaseUserName;
		setChanged();
	}

	/**
	 * Get the datasourceMetadataTypeMapping.
	 * @return the datasourceMetadataTypeMapping
	 */
	public String getDatasourceMetadataTypeMapping()
	{
		return datasourceMetadataTypeMapping;
	}

	/**
	 * Set the datasourceMetadataTypeMapping.
	 * @param datasourceMetadataTypeMapping the datasourceMetadataTypeMapping to set
	 */
	public void setDatasourceMetadataTypeMapping(
			String datasourceMetadataTypeMapping)
	{
		this.datasourceMetadataTypeMapping = datasourceMetadataTypeMapping;
		setChanged();
	}

	/**
	 * Get the datasourceTemplateDSXMLFile.
	 * @return the datasourceTemplateDSXMLFile
	 */
	public String getDatasourceTemplateDSXMLFile()
	{
		return datasourceTemplateDSXMLFile;
	}

	/**
	 * Set the datasourceTemplateDSXMLFile.
	 * @param datasourceTemplateDSXMLFile the datasourceTemplateDSXMLFile to set
	 */
	public void setDatasourceTemplateDSXMLFile(String datasourceTemplateDSXMLFile)
	{
		this.datasourceTemplateDSXMLFile = datasourceTemplateDSXMLFile;
		setChanged();
	}

	/**
	 * Get the databaseURL.
	 * @return the databaseURL
	 */
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
		setChanged();
	}
	
	/**
	 * Get the datasourceConfigFile.
	 * @return the datasourceConfigFile
	 */
	public String getDatasourceConfigFile()
	{
		return datasourceConfigFile;
	}
	
	public void setDatasourceConfigFile(String datasourceConfigFile)
	{
		if (datasourceConfigFile == null)
			throw new IllegalArgumentException("datasourceConfigFile must not be null!");

		if (datasourceConfigFile.indexOf(JFireServerConfigModule.ORGANISATION_ID_VAR) < 0)
			throw new IllegalArgumentException("datasourceConfigFile must contain \"" + JFireServerConfigModule.ORGANISATION_ID_VAR + "\"!");

		this.datasourceConfigFile = datasourceConfigFile;
		setChanged();
	}
}