package org.nightlabs.jfire.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.nightlabs.config.ConfigModuleNotFoundException;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.jfire.config.id.ConfigSetupID;

@Remote
public interface ConfigManagerRemote 
{
	/**
	 * Stores the given ConfigModule. If it is a ConfigModule belonging to a
	 * ConfigGroup, the corresponding ConfigModules of all members of this group
	 * will inherit from the given ConfigModule according to its
	 * inheritence settings.
	 */
	ConfigModule storeConfigModule(ConfigModule configModule, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns all ConfigGroups with the given configType. If the parameter
	 * is null all ConfigGroups will be returned.
	 *
	 * @param configType If set, all ConfigGroups with the given configType
	 * will be returned. If null, all ConfigGroups will be returned
	 * @param fetchGroups The fetch-groups to be used to detach the found groups
	 */
	Collection<ConfigGroup> getConfigGroups(String configType,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get all ConfigGroups.
	 *
	 * @see #getConfigGroups(String, String[])
	 */
	Collection<ConfigGroup> getConfigGroups(String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Returns all Configs corresponding to the given Set of ConfigIDs.
	 *
	 * @param configIDs The Set of ConfigIDs corresponding to the desired Configs.
	 * @param fetchGroups The fetch-groups to be used to detach the found Configs
	 */
	Collection<Config> getConfigs(Set<ConfigID> configIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns all Configs with the given configType. If the parameter
	 * is null all Configs will be returned.
	 * {@link Config#getConfigsByType(PersistenceManager, String, String)}
	 *
	 * @param configType If set, all Configs with the given configType
	 * will be returned. If null, all Configs will be returned
	 * @param fetchGroups The fetch-groups to be used to detach the found Configs
	 */
	Set<Config> getConfigs(String configType, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Returns all ConfigIDs corresponding to Configs of the given <code>configType</code>.
	 * {@link Config#getConfigIDsByConfigType(PersistenceManager, String, String)}
	 */
	Set<ConfigID> getConfigIDsByConfigType(String configType,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get all Configs.
	 */
	Collection<Config> getConfigs(String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get a certain Config.
	 */
	Config getConfig(ConfigID configID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Searches the ConfigModule of the given Config, cfModClass and cfModID.
	 * If not found it will be auto-created.
	 *
	 * @param userConfigID The UserConfigID the returned ConfigModule should belong to
	 * @param cfModClass The ConfigModule's class
	 * @param cfModID The ConfigModules cfModID (suffix)
	 * @param fetchGroups The fetch-groups to be used to detach the ConfigModule
	 * @return The ConfigModule of the given userConfig, cfModClass and cfModID
	 */
	ConfigModule getConfigModule(ConfigID configID,
			Class<? extends ConfigModule> cfModClass, String cfModID,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Searches the ConfigModule for the given keyObject and if there is none, one is created.
	 *
	 * @param keyObjectID The ObjectID of the Object the Config holding the ConfigModule is assigned to.
	 * @param cfModClass The classname of the ConfigModule desired
	 * @param cfModID The cfModID of the ConfigModule desired
	 * @param fetchGroups The fetch-groups to detach the returned ConfigModule with
	 * @return
	 */
	ConfigModule createConfigModule(ObjectID keyObjectID,
			Class<? extends ConfigModule> cfModClass, String cfModID,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Searches the ConfigModule for the given keyObject.
	 *
	 * @param keyObjectID The ObjectID of the Object the Config holding the ConfigModule is assigned to.
	 * @param cfModClass The classname of the ConfigModule desired
	 * @param cfModID The cfModID of the ConfigModule desired
	 * @param throwExceptionIfNotFound If <code>true</code> and the ConfigModule does not exist, a {@link ConfigModuleNotFoundException} will be thrown. If it's <code>false</code>, <code>null</code> will be returned instead of an exception.
	 * @param fetchGroups The fetch-groups to detach the returned ConfigModule with
	 * @return Returns <code>null</code> (if allowed) or an instance of a class extending ConfigModule.
	 */
	ConfigModule getConfigModule(ConfigID keyObjectID,
			Class<? extends ConfigModule> cfModClass, String cfModID,
			boolean throwExceptionIfNotFound, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Returns a Collection of {@link ConfigModule}s, corresponding to the set of {@link ConfigModuleID}s.
	 * If a corresponding ConfigModule doesn't exists it and its surrounding {@link Config} are created.
	 *
	 * @param moduleIDs the set of {@link ConfigModuleID}s for which to gather the corresponding {@link ConfigModule}s
	 * @param fetchGroups the FetchGroups for the detached {@link ConfigModule}s
	 * @param maxFetchDepth the maximum fetch depth for the detached ConfigModules.
	 * @return a Collection of {@link ConfigModule}s, corresponding to the set of {@link ConfigModuleID}s.
	 */
	Collection<ConfigModule> getConfigModules(Set<ConfigModuleID> moduleIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the ConfigModule of the ConfigGroup of the Config corresponding to the given ConfigID
	 * and with the given Class and moduleID.
	 * <p>
	 * The caller can choose if either an exception is thrown when the queried ConfigModule does not exist
	 * or if it rather should be auto-created (autoCreate parameter).
	 * </p>
	 *
	 * @param childID the {@link ConfigID} of the child's {@link Config}.
	 * @param configModuleClass the Class of the ConfigModule to return.
	 * @param moduleID the module ID in the case there is more than one instance of that ConfigModule.
	 * @param autoCreate Whether the {@link ConfigModule} should be auto-created if not yet existing,
	 *                   otherwise a {@link ConfigModuleNotFoundException} will be thrown.
	 * @param fetchGroups the fetchGroups with which to detach the ConfigModule.
	 * @param maxFetchDepth the maximum fetch depth while detaching.
	 * @return the ConfigModule of the ConfigGroup of the Config corresponding to the given ConfigID
	 * and with the given Class and moduleID.
	 */
	ConfigModule getGroupConfigModule(ConfigID childID,
			Class<? extends ConfigModule> configModuleClass, String moduleID,
			boolean autoCreate, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Add a new ConfigGroup with ge given configKey, the given groupType as
	 * configType and the given groupName.
	 *
	 * @param get If true the new ConfigGroup will be detached and returned. If
	 * false null is returned.
	 * @param fetchGroups The fetch-groups to be used to detach the returned group
	 */
	ConfigGroup addConfigGroup(String configKey, String groupType,
			String groupName, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Returns the ConfigSetup with the given configSetupID.
	 * <p>
	 * The {@link ConfigSetup} will hereby be prepared by
	 * its {@link ConfigSetup#getCompleteConfigSetup(PersistenceManager, String, String[], String[])}.
	 * </p>
	 *
	 * @param configSetupID The id of the {@link ConfigSetup} to return.
	 * @param groupsFetchGropus Fetch-groups to detach the {@link ConfigGroup}s with, that are part of the setup.
	 * 		If this is <code>null</code>, {@link FetchPlan#DEFAULT} will be used.
	 * @param configsFetchGroups Fetch-groups to detach the {@link Config}s with, that are part of the setup.
	 * 		If this is <code>null</code>, {@link FetchPlan#DEFAULT} will be used.
	 */
	ConfigSetup getConfigSetup(ConfigSetupID configSetupID,
			String[] groupsFetchGropus, int groupsMaxFetchDepth,
			String[] configsFetchGroups, int configsMaxFetchDepth);

	/**
	 * Returns a list of ConfigSetups for the given {@link ConfigSetupID}s.
	 *
	 * @param setupIDs The set of {@link ConfigSetupID}s for which to retrieve the corresponding ConfigSetups.
	 * @param fetchGroups The fetch groups with which to detach the object.
	 * @param maxFetchDepth The maximal fetch depth.
	 * @return a list of ConfigSetups for the given {@link ConfigSetupID}s.
	 */
	List<ConfigSetup> getConfigSetups(Set<ConfigSetupID> setupIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the {@link ConfigSetupID}s of all ConfigSetups.
	 *
	 * @param fetchGroups the fetch groups, with which the ConfigSetups shall be detached.
	 * @param maxFetchDepth the maximum fetch depth to use for detaching.
	 * @return the {@link ConfigSetupID}s of all ConfigSetups.
	 */
	Collection<ConfigSetupID> getAllConfigSetupIDs();

	/**
	 * Stores the given ConfigSetup if belonging to the current organisation and
	 * throws and exception if not.
	 */
	void storeConfigSetup(Collection<Config> setup);

	/**
	 * Stores the given {@link Config} and returns a detached copy if the caller requests it.
	 * @param config The {@link Config} to be stored.
	 * @param get A boolean indicating whether a detached copy of the stored {@link Config} should be returned.
	 * @param fetchGroups The fetch groups to be used
	 * @param maxFetchDepth The maximal fetch depth to be used
	 * @return A detached copy of the stored {@link Config} if <code>get == true</code> and <code>null</code> otherwise.
	 */
	Config storeConfig(Config config, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Applies the inheritence of a groupModule to the given ConfigModule if the Config corresponding to the
	 * given ModuleID is in a ConfigGroup. Finally, returns a detached copy of the ConfigModule corresponding
	 * to the given ConfigModuleID with the given <code>fetchGroups</code> and the given <code>maxFetchDepth</code>.
	 * @param IDOfModuleToInherit ID of the ConfigModule, which shall inherit.
	 * @param get Whether or not a detached Copy of the corresponding ConfigModule shall be returned.
	 * @param fetchGroups FetchGroups of detached copy to return.
	 * @param maxFetchDepth FetchDepth of detached copy to return.
	 * @return a detached Copy of the ConfigModule of the given ConfigModuleID after applying group inheritence.
	 */
	ConfigModule applyGroupInheritence(ConfigModuleID IDOfModuleToInherit,
			boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Initializes the JFireBase Config-System.
	 */
	void initialise();

	String ping(String message);
}