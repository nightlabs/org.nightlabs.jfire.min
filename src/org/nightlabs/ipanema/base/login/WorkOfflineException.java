/*
 * Created 	on Nov 22, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.login;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class WorkOfflineException extends Exception {

	/**
	 * 
	 */
	public WorkOfflineException() {
		super();
	}

	/**
	 * @param message
	 */
	public WorkOfflineException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public WorkOfflineException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WorkOfflineException(String message, Throwable cause) {
		super(message, cause);
	}

}
