/**
 * 
 */
package org.nightlabs.jfire.config.dao;

import java.util.Collection;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.login.JFireLoginProvider;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
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
	protected Collection<ConfigModule> retrieveJDOObjects(Set<ConfigModuleID> configModuleIDs, 
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) 
			throws Exception {
		if (configModuleIDs == null)
			return null;
		monitor.beginTask("Retrieving ConfigModules", 1);
		Collection<ConfigModule> result;
		try {
			ConfigManager configManager = ConfigManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();
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
	public ConfigModule getConfigModule(
			ConfigID config, Class cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth, 
			ProgressMonitor monitor) 
	{
		return getJDOObject(
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
			);
	}
	
	/**
	 * Get the ConfigModule of the given class and cfModID for the given Config.
	 */
	public ConfigModule getConfigModule(Config config, Class cfModClass, String cfModID, 
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		return getConfigModule((ConfigID)JDOHelper.getObjectId(config), cfModClass, cfModID, 
														fetchGroups, maxFetchDepth, monitor);
	}

}
