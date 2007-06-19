package org.nightlabs.jfire.base.config;

import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.Utils;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface IConfigModuleController 
{
	/**
	 * returns the id object {@link ConfigID} of the {@link Config} where
	 * the {@link ConfigModule} belongs to, which is controlled by this IConfigModuleController
	 *    
	 * @return the configID
	 */
	public ConfigID getConfigID();
	
	/**
	 * sets the configID
	 * @param configID the configID to set
	 * @param useNotAsPreferencePage whether or not the page corresponding to this Controller is used 
	 * 				inside the PreferencePage Dialog
	 */
	public void setConfigID(ConfigID configID, boolean useNotAsPreferencePage, String configModuleID);
	
	/**
	 * returns the {@link ConfigModule}
	 * @return the configModule
	 */
	public ConfigModule getConfigModule();
	
	/**
	 * sets the configModule
	 * @param configModule the configModule to set
	 */
	public void setConfigModule(ConfigModule configModule);

	/**
	 * Returns the cfModID of the managed {@link ConfigModule}. 
	 * @return the cfModID of the managed {@link ConfigModule}.
	 */
	public String getConfigModuleID();
	
	/**
	 * adds an {@link IConfigModuleChangedListener} to listen for config changes 
	 * listener are notified when {@link #setConfigModule(ConfigModule)} is called
	 * 
	 * @param listener the listener to add
	 */
	public void addConfigModuleChangedListener(IConfigModuleChangedListener listener);
	
	/**
	 * removes an {@link IConfigModuleChangedListener}
	 * @param listener the listener to remove
	 */
	public void removeConfigModuleChangedListener(IConfigModuleChangedListener listener);
	
	/**
	 * returns the AbstractConfigModulePreferencePage 
	 * @return the AbstractConfigModulePreferencePage
	 */
	public AbstractConfigModulePreferencePage getPreferencePage();

	/**
	 * Will be called to determine whether the given ConfigModule is allowed
	 * to be edited. The default implementation will return false only when
	 * the {@link ConfigGroup} of the given module's Config disallows the member to overwrite the 
	 * configuration.
	 * This method is costly, since it calls {@link #checkIfIsGroupMember(ConfigModule)}, which needs 
	 * to fetch data from the datastore. 
	 * 
	 * @param configModule the {@link ConfigModule} to be checked whether it is editable or not. 
	 * @return Whether the <code>configModule</code> is allowed to be edited.
	 */
	public boolean canEdit(ConfigModule configModule);
	
	/**
	 * Checks if the configModule is member of a configGroup 
	 */
	public boolean checkIfIsGroupMember(ConfigModule module);
	
	/**
	 * Returns the ConfigModule of the the ConfigGroup, where the corresponding Config of the 
	 * <code>memberModule</code> is in.
	 * @param memberModule the ConfigModule for which to retrieve the GroupConfigModule.
	 * @return the ConfigModule of the the ConfigGroup, where the corresponding Config of the 
	 * <code>memberModule</code> is in.
	 */
	public ConfigModule getGroupConfigModule(ConfigModule memberModule);
	
	/**
	 * Fetches and returns the ConfigModule of the Config with {@link ConfigID} == <code>configID</code> from the 
	 * cache with ConfigModuleClass == <code>getPreferencePage().getConfigModuleClassName()</code>, 
	 * cfModID == <code>getPreferencePage().getConfigModuleCfModID()</code>,
	 * the FetchGroups returned by <code>getPreferencePage().getConfigModuleFetchGroups()</code> and the FetchDepth returned by 
	 * <code>getPreferencePage().getConfigModuleMaxFetchDepth()</code>.
	 * 
	 * @param monitor the monitor showing the progress of the operation.
	 * @return the ConfigModule of the Config with ID = <code>configID</code> and the parameter as set
	 * 	by the abstract getters (e.g. <code>getPreferencePage().getConfigModuleClassName()</code>).
	 */	
	public ConfigModule retrieveConfigModule(ProgressMonitor monitor);
	
	/**
	 * Sets the current version of the {@link ConfigModule} to display and updates the page 
	 * accordingly. 
	 * The given ConfigModule should be a copy of the one from the cache, otherwise changes to this 
	 * module will corrupt the state of the one in the cache! Use {@link Utils#cloneSerializable(Object)}
	 * to create a copy.
	 * 
	 * @param configModule The {@link ConfigModule} to set.
	 */	
	public void updateGuiWith(ConfigModule configModule);
}
