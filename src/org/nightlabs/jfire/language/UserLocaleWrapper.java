/**
 * 
 */
package org.nightlabs.jfire.language;

import java.util.Locale;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class UserLocaleWrapper {

	private String completeUserID;
	private Locale locale;
	private long timeStamp;
	
	/**
	 * @param completeUserID
	 * @param locale
	 * @param timeStamp
	 */
	public UserLocaleWrapper(String completeUserID, Locale locale, long timeStamp) {
		super();
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
	 * Sets the locale.
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Return the timeStamp.
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets the timeStamp.
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Return the completeUserID.
	 * @return the completeUserID
	 */
	public String getCompleteUserID() {
		return completeUserID;
	}

	/**
	 * Sets the completeUserID.
	 * @param completeUserID the completeUserID to set
	 */
	public void setCompleteUserID(String completeUserID) {
		this.completeUserID = completeUserID;
	}
	
}
