package org.nightlabs.jfire.servermanager.db;

/**
 * This exception is called by
 * {@link DatabaseAdapter#createDatabase(org.nightlabs.jfire.servermanager.config.JFireServerConfigModule, String)}
 * if the database cannot be created, because it already exists.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DatabaseAlreadyExistsException
		extends DatabaseException
{
	private static final long serialVersionUID = 1L;

	private String dbServerURL;
	private String databaseName;

	/**
	 * @param databaseName The name of the database that should have been created, but does already exist.
	 */
	public DatabaseAlreadyExistsException(String dbServerURL, String databaseName)
	{
		this(dbServerURL, databaseName, null);
	}

	public DatabaseAlreadyExistsException(String dbServerURL, String databaseName, Throwable cause)
	{
		super("Database \"" + databaseName + "\" cannot be created, because it already exists!", cause);
		this.databaseName = databaseName;
	}

	public String getDbServerURL()
	{
		return dbServerURL;
	}

	public String getDatabaseName()
	{
		return databaseName;
	}
}
