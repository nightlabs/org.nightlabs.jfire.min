/**
 * 
 */
package org.nightlabs.jfire.language;

import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.serverinit.ServerInitialiserDelegate;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class JFireLocaleInitialiser extends ServerInitialiserDelegate {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.serverinit.ServerInitialiserDelegate#initialise()
	 */
	@Override
	public void initialise() 
	throws InitException 
	{
		// sets the system property so that when NLLocale.getDefault() is called it will
		// return the Locale of the current user
		System.setProperty(JFireLocale.SYSTEM_PROPERTY_KEY_NL_LOCALE_CLASS, JFireLocale.class.getName());
	}

}
