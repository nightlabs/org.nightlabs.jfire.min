package org.nightlabs.jfire.security;

public class NoUserException
		extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NoUserException()
	{
	}

	public NoUserException(String message)
	{
		super(message);
	}

	public NoUserException(Throwable cause)
	{
		super(cause);
	}

	public NoUserException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
