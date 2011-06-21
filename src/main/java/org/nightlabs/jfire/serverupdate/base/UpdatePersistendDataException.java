package org.nightlabs.jfire.serverupdate.base;

public class UpdatePersistendDataException 
extends Exception
{
	private static final long serialVersionUID = 1L;
	public UpdatePersistendDataException() {}
	public UpdatePersistendDataException(String gripe)
	{
		super(gripe);
	}

}
