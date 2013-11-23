package org.nightlabs.jfire.organisation;

public class NoPendingRegistrationExistingException
extends IllegalStateException
{
	private static final long serialVersionUID = 1L;

	public NoPendingRegistrationExistingException() {
	}

	public NoPendingRegistrationExistingException(String s) {
		super(s);
	}

	public NoPendingRegistrationExistingException(Throwable cause) {
		super(cause);
	}

	public NoPendingRegistrationExistingException(String message,
			Throwable cause) {
		super(message, cause);
	}
}
