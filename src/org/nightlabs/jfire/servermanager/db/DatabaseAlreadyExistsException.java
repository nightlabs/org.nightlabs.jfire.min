package org.nightlabs.jfire.servermanager.db;

/**
 * This exception is thrown by
 * {@link DatabaseAdapter#createDatabase(org.nightlabs.jfire.servermanager.config.JFireServerConfigModule, String)}
 * if the database cannot be created, because it already exists.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DatabaseAlreadyExistsException
		extends DatabaseException
{
	private static final long serialVersionUID = 1L;

//	private String dbServerURL;
//	private String databaseName;
	private String databaseURL;

	/**
	 * @param databaseName The name of the database that should have been created, but does already exist.
	 */
	public DatabaseAlreadyExistsException(String databaseURL)
	{
		this(databaseURL, null);
	}

	public DatabaseAlreadyExistsException(String databaseURL, Throwable cause)
	{
		super("Database \"" + databaseURL + "\" cannot be created, because it already exists!", cause);
		this.databaseURL = databaseURL;
	}

	public String getDatabaseURL()
	{
		return databaseURL;
	}
}
