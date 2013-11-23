package org.nightlabs.jfire.base;

/**
 * It is - I don't know why - not possible that a bean method has the {@link java.rmi.RemoteException}
 * in it's throws clause. That's why we declare a JFireRemoteException always when the
 * bean method contacts another remote server (cascaded organisation access).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JFireRemoteException	extends JFireException
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Create a new JFireRemoteException.
	 */
	public JFireRemoteException()
	{
		super();
	}
	
	/**
	 * Create a new JFireRemoteException.
	 * @param  message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 */
	public JFireRemoteException(String message)
	{
		super(message);
	}
	
	/**
	 * Create a new JFireRemoteException.
	 * @param  message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 * @param  cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public JFireRemoteException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * Create a new JFireRemoteException.
	 * @param  cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public JFireRemoteException(Throwable cause)
	{
		super(cause);
	}
}
