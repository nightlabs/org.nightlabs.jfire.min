package org.nightlabs.jfire.workstation;

public class WorkstationNotSpecifiedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public WorkstationNotSpecifiedException() {
	}

	public WorkstationNotSpecifiedException(String message) {
		super(message);
	}

	public WorkstationNotSpecifiedException(Throwable cause) {
		super(cause);
	}

	public WorkstationNotSpecifiedException(String message, Throwable cause) {
		super(message, cause);
	}
}
