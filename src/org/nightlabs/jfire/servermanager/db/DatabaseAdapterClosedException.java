package org.nightlabs.jfire.servermanager.db;

/**
 * This exception is thrown by all {@link DatabaseAdapter} methods (except <code>close()</code>) after {@link DatabaseAdapter#close()} had been called.
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class DatabaseAdapterClosedException
extends DatabaseException
{
	private static final long serialVersionUID = 1L;

	public DatabaseAdapterClosedException()
	{
	}

	public DatabaseAdapterClosedException(String message)
	{
		super(message);
	}

	public DatabaseAdapterClosedException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DatabaseAdapterClosedException(Throwable cause)
	{
		super(cause);
	}
}
