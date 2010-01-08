package org.nightlabs.jfire.config;

import org.nightlabs.jfire.config.xml.JFireDateFormatterFactory;
import org.nightlabs.jfire.config.xml.JFireNumberFormatterFactory;
import org.nightlabs.jfire.config.xml.XMLConfigFactory;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.serverinit.ServerInitialiserDelegate;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.l10n.NumberFormatter;

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

		// Since the Date/Number formatting is closely related to the config, we initialise it here.
		System.setProperty(DateFormatter.PROPERTY_KEY_DATE_FORMATTER_FACTORY, JFireDateFormatterFactory.class.getName());
		System.setProperty(NumberFormatter.PROPERTY_KEY_NUMBER_FORMATTER_FACTORY, JFireNumberFormatterFactory.class.getName());
	}

}
