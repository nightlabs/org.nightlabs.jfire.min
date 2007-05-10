package org.nightlabs.jfire.init;

public class InitException extends Exception {
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public InitException(Exception cause) {
		super(cause);
	}

	public InitException(String message, Exception cause) {
		super(message, cause);
	}

	public InitException(String message) {
		super(message);
	}
}
