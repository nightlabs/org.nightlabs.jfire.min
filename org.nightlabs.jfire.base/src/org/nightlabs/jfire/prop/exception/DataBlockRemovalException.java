package org.nightlabs.jfire.prop.exception;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DataBlockRemovalException extends PropertyException {
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new {@link DataBlockRemovalException}.
	 */
	public DataBlockRemovalException() {
		super();
	}

	/**
	 * Create a new {@link DataBlockRemovalException}.
	 * @param message The detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 * @param cause The cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public DataBlockRemovalException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new {@link DataBlockRemovalException}.
	 * @param message The detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 */
	public DataBlockRemovalException(String message) {
		super(message);
	}

	/**
	 * Create a new {@link DataBlockRemovalException}.
	 * @param cause The cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public DataBlockRemovalException(Throwable cause) {
		super(cause);
	}
}
