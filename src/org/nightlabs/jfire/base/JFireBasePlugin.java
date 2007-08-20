/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class JFireBasePlugin
	extends AbstractUIPlugin
{
	public static final String PLUGIN_ID = "org.nightlabs.jfire.base"; //$NON-NLS-1$
	public static final String ZONE_ADMIN = JFireBasePlugin.class.getName() + "#ZONE_ADMIN"; //$NON-NLS-1$
	
	
	//The shared instance.
	private static JFireBasePlugin plugin;
//	//Resource bundle.
//	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor registeres this plugin
	 * as LoginStateListener.
	 * 
	 */
	public JFireBasePlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// We cannot use org.nightlabs.jfire.idgenerator.IDGenerator.PROPERTY_KEY_ID_GENERATOR_CLASS
		// or IDGeneratorClient,  because this would cause the server side class to be loaded -
		// and probably we're OFFLINE and can't do that!
		System.setProperty("org.nightlabs.jfire.idgenerator.idGeneratorClass", "org.nightlabs.jfire.base.idgenerator.IDGeneratorClient"); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("org.nightlabs.jfire.security.SecurityReflector", "org.nightlabs.jfire.base.security.SecurityReflectorClient"); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager", "org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManagerRCP"); //$NON-NLS-1$ //$NON-NLS-2$

//		try {
//			resourceBundle = Platform.getResourceBundle(getBundle());
//		} catch (MissingResourceException x) {
//			resourceBundle = null;
//		}
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

//	/**
//	 * Returns the string from the plugin's resource bundle,
//	 * or 'key' if not found.
//	 */
//	public static String getResourceString(String key) {
//		ResourceBundle bundle = JFireBasePlugin.getDefault().getResourceBundle();
//		try {
//			return (bundle != null) ? bundle.getString(key) : key;
//		} catch (MissingResourceException e) {
//			return key;
//		}
//	}

//	/**
//	 * Returns the plugin's resource bundle,
//	 */
//	public ResourceBundle getResourceBundle() {
//		return resourceBundle;
//	}

	public static ConfigManager getConfigManager() {
		try {
			return ConfigManagerUtil.getHome(
					Login.getLogin().getInitialContextProperties()
			).create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) 
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
