package org.nightlabs.jfire.base;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class JFireBasePlugin
	extends AbstractUIPlugin
{
	
	public static final String ZONE_ADMIN = JFireBasePlugin.class.getName() + "#ZONE_ADMIN";
	
	
	//The shared instance.
	private static JFireBasePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor registeres this plugin
	 * as LoginStateListener.
	 * 
	 */
	public JFireBasePlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.nightlabs.jfire.base.plugin");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
//		Login.addLoginStateListener(this);
//		LOGGER.debug("Registered JFireBasePlugin as LoginStateListener");
//		LanguageWatcher.registerAsLoginStateListener();
//		LOGGER.debug("Registered LanguageWatcher as LoginStateListener");
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static JFireBasePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = JFireBasePlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

}
