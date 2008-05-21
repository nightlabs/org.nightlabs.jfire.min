/**
 * 
 */
package org.nightlabs.jfire.language;

import java.util.Locale;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 * @author marco schulze - marco at nightlabs dot de
 */
public class UserLocaleWrapper {

	private String completeUserID;
	private Locale locale;
	private long timeStamp;
	
	public UserLocaleWrapper(String completeUserID, Locale locale, long timeStamp) {
		this.completeUserID = completeUserID;
		this.locale = locale;
		this.timeStamp = timeStamp;
	}

	/**
	 * Return the locale.
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Return the timeStamp.
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Return the completeUserID.
	 * @return the completeUserID
	 */
	public String getCompleteUserID() {
		return completeUserID;
	}

}
