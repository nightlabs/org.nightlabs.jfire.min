/**
 * 
 */
package org.nightlabs.jfire.base.config;

import org.nightlabs.jfire.config.ConfigModule;

/**
 * @author alex
 *
 */
public interface IConfigModuleChangedListener {
	
	void configModuleChanged(ConfigModule configModule);
}
