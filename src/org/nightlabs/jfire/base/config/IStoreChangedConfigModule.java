package org.nightlabs.jfire.base.config;

import org.nightlabs.jfire.config.ConfigModule;

//public interface IAskUserAboutChangedConfigModule {
public interface IStoreChangedConfigModule {
	/**
	 * Stores the changed ConfigModule somehow for later use.
	 * @param module the changed ConfigModule.
	 */
	public void addChangedConfigModule(ConfigModule module);
}
