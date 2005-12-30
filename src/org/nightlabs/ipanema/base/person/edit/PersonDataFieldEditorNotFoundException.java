/*
 * Created 	on Nov 26, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonDataFieldEditorNotFoundException extends Exception {

	/**
	 * 
	 */
	public PersonDataFieldEditorNotFoundException() {
		super();
	}

	/**
	 * @param message
	 */
	public PersonDataFieldEditorNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PersonDataFieldEditorNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PersonDataFieldEditorNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
