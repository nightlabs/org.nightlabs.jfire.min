/*
 * Created on Sep 15, 2005
 */
package org.nightlabs.ipanema.jdo.organisationsync;

import org.nightlabs.ModuleException;

public class DirtyObjectIDBufferException extends ModuleException
{

	public DirtyObjectIDBufferException()
	{
		super();
	}

	public DirtyObjectIDBufferException(String message)
	{
		super(message);
	}

	public DirtyObjectIDBufferException(String message,
			Throwable cause)
	{
		super(message, cause);
	}

	public DirtyObjectIDBufferException(Throwable cause)
	{
		super(cause);
	}

}
