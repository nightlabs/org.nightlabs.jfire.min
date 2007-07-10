package org.nightlabs.jfire.servermanager.db;

import org.nightlabs.annotation.Implement;


public abstract class AbstractDatabaseAdapter
implements DatabaseAdapter
{
	private boolean closed = false;

	@Implement
	public boolean isClosed()
	{
		return closed;
	}

	@Implement
	public void close()
	throws DatabaseException
	{
		this.closed = true;
	}

	protected void assertOpen()
	throws DatabaseAdapterClosedException
	{
		if (isClosed())
			throw new DatabaseAdapterClosedException("This DatabaseAdapter is already closed: " + this);
	}

}
