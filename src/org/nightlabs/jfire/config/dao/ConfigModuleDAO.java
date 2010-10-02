/**
 *
 */
package org.nightlabs.jfire.config.dao;

import java.util.Collection;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManagerRemote;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * The Access Object for {@link ConfigModule}s.
 * @version $Revision$ - $Date$
 * @author Marius Heinzmann - marius[at]nightlabs[dot]de
 */
public class ConfigModuleDAO extends BaseJDOObjectDAO<ConfigModuleID, ConfigModule> {

	/**
	 * The shared instance of this DAO.
	 */
	private static ConfigModuleDAO sharedInstance = null;

	/**
	 * Accessor for the Singleton of this Class.
	 * @return the only instance available;
	 */
	public static ConfigModuleDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new ConfigModuleDAO();

		return sharedInstance;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Collection<ConfigModule> retrieveJDOObjects(
			Set<ConfigModuleID> configModuleIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	) throws Exception
	{
		if (configModuleIDs == null)
			return null;
		monitor.beginTask("Retrieving ConfigModules", 1);
		Collection<ConfigModule> result;
		try {
			ConfigManagerRemote configManager = getEjbProvider().getRemoteBean(ConfigManagerRemote.class);
			result = configManager.getConfigModules(configModuleIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("ConfigModule download failed!", e);
		}

		monitor.done();
		return result;
	}

	/**
	 * Returns a Set of all ConfigModules corresponding to the given Set of ConfigModuleIDs.
	 * @param ids the set of ConfigModuleIDs for which the corresponding modules shall be returned
	 * @param fetchGroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The monitor which reflects the progress of the retrieval.
	 * @return a Set of all ConfigModules corresponding to the given Set of ConfigModuleIDs.
	 */
	public Collection<ConfigModule> getConfigModules(Set<ConfigModuleID> ids, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, ids, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Return the ConfigModule corresponding to the ConfigModuleID.
	 * @param moduleID the ConfigModuleID of the ConfigModule to return.
	 * @param fetchGroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The monitor which reflects the progress of the retrieval.
	 * @return the ConfigModule corresponding to the ConfigModuleID.
	 */
	public ConfigModule getConfigModule(ConfigModuleID moduleID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObject(null, moduleID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get the ConfigModule of the given class and cfModID for the Config defined
	 * by the given configID.
	 */
	public <T extends ConfigModule> T getConfigModule(
			ConfigID config, Class<T> cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		return cfModClass.cast(getJDOObject(
				null,
				ConfigModuleID.create(
						config.organisationID,
						config.configKey,
						config.configType,
						ConfigModule.getCfModKey(cfModClass, cfModID)
				),
				fetchGroups,
				maxFetchDepth,
				monitor
			)
		);
	}

	/**
	 * Get the ConfigModule of the given class and cfModID for the given Config.
	 */
	public ConfigModule getConfigModule(Config config, Class<? extends ConfigModule> cfModClass, String cfModID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getConfigModule((ConfigID)JDOHelper.getObjectId(config), cfModClass, cfModID,
														fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Returns the {@link ConfigModule} of the {@link ConfigGroup} of the {@link Config} corresponding to the given {@link ConfigID}
	 * and with the given Class and moduleID if the given {@link Config} is member of a {@link ConfigGroup}.
	 * Note, that the {@link ConfigModule} for the ConfigGroup will be auto-created if it doey not extist
	 * yet.
	 * <p>
	 * If the given {@link Config} is not member of a {@link ConfigGroup} <code>null</code> is returned.
	 * </p>
	 *
	 * @param childID the {@link ConfigID} of the child's {@link Config}.
	 * @param configModuleClass the Class of the ConfigModule to return.
	 * @param moduleID the module ID in the case there is more than one instance of that ConfigModule.
	 * @param fetchGroups the fetchGroups with which to detach the ConfigModule.
	 * @param maxFetchDepth the maximum fetch depth while detaching.
	 * @param monitor the ProgressMonitor to use for showing the progress of the operation.
	 * @return the ConfigModule of the ConfigGroup of the {@link Config} corresponding to the given {@link ConfigID}
	 *         and with the given Class and moduleID, or <code>null</code> if the given {@link Config} is not member
	 *         of a {@link ConfigGroup}.
	 */
	public ConfigModule getGroupsCorrespondingModule(ConfigID childID, Class<? extends ConfigModule> configModuleClass,
			String moduleID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Getting Groups ConfigModule...", 1);
		try {
			ConfigManagerRemote cm = getEjbProvider().getRemoteBean(ConfigManagerRemote.class);
			ConfigModule searchedModule = cm.getGroupConfigModule(childID, configModuleClass, moduleID, true,
					fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();

			return searchedModule;
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("ConfigModule download failed: ", e);
		}
	}

	public ConfigModule storeConfigModule(ConfigModule configModule, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		monitor.beginTask("Storing ConfigModule...", 1);
		try {
			ConfigManagerRemote cm = getEjbProvider().getRemoteBean(ConfigManagerRemote.class);
			ConfigModule storedModule = cm.storeConfigModule(configModule, get,
					fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();

			return storedModule;
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("ConfigModule store failed: ", e);
		}
	}
}
