package org.nightlabs.jfire.update.base;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class InconsistentUpdateProceduresException extends IllegalStateException
{
	private static final long serialVersionUID = 1L;

	public enum Reason {
		fromVersionMismatch,
		toVersionMismatch,
		overlap,
		hole
	}

	private Reason reason;

//	public InconsistentUpdateProceduresException() { }

	public InconsistentUpdateProceduresException(Reason reason, String s) {
		super(s);
		this.reason = reason;
	}

//	public InconsistentUpdateProceduresException(Throwable cause) {
//		super(cause);
//	}
//
//	public InconsistentUpdateProceduresException(String message, Throwable cause) {
//		super(message, cause);
//	}

	public Reason getReason() {
		return reason;
	}
}
