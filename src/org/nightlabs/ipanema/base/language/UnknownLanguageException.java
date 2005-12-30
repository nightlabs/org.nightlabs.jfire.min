/*
 * Created on Jan 6, 2005
 */
package org.nightlabs.ipanema.base.language;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class UnknownLanguageException extends RuntimeException
{

	public UnknownLanguageException()
	{
	}

	/**
	 * @param message
	 */
	public UnknownLanguageException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownLanguageException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public UnknownLanguageException(Throwable cause)
	{
		super(cause);
	}

}
