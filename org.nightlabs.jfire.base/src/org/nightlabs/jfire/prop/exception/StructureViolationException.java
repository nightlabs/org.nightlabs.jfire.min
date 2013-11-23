package org.nightlabs.jfire.prop.exception;


/**
 * @author Unknown
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class StructureViolationException extends PropertyException
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new StructureViolationException.
	 */
	public StructureViolationException()
	{
		super();
	}

	/**
	 * Create a new StructureViolationException.
	 * @param  message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 */
	public StructureViolationException(String message)
	{
		super(message);
	}

	/**
	 * Create a new StructureViolationException.
	 * @param  message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 * @param  cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public StructureViolationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Create a new StructureViolationException.
	 * @param  cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public StructureViolationException(Throwable cause)
	{
		super(cause);
	}
}
