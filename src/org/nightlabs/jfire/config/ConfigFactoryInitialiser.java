/**
 * 
 */
package org.nightlabs.jfire.config;

import org.nightlabs.jfire.config.xml.XMLConfigFactory;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.serverinit.ServerInitialiserDelegate;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ConfigFactoryInitialiser extends ServerInitialiserDelegate {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.serverinit.ServerInitialiserDelegate#initialise()
	 */
	@Override
	public void initialise() throws InitException {
		// The stuff for the XML config has nothing to do with the JFire config system. Instead, it makes
		// the XML-based config from NightLabsBase available in the server. This should only be used for
		// the DateFormatter and the NumberFormatter! Do not use it for anything else!!! Marco.		
		System.setProperty(org.nightlabs.config.Config.PROPERTY_KEY_CONFIG_FACTORY, XMLConfigFactory.class.getName());
	}

}
