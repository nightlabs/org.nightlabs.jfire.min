package org.nightlabs.jfire.init;

public class InitException extends Exception {
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
