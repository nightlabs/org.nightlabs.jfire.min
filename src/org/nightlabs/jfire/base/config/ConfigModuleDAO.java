/**
 * 
 */
package org.nightlabs.jfire.base.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;

/**
 * The Access Object for {@link ConfigModule}s.
 * @version $Revision$ - $Date$ 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]de
 */
public class ConfigModuleDAO extends JDOObjectDAO<ConfigModuleID, ConfigModule> {

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
			String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) 
			throws Exception {
		if (configModuleIDs == null)
			return null;
		monitor.beginTask("Retrieving ConfigModules", configModuleIDs.size());
		Collection<ConfigModule> result;
		try {
			ConfigManager configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			result = new HashSet<ConfigModule>(configModuleIDs.size());
			// ConfigManager is not able to retrieve a collection of ConfigModules by means of their IDs. 
			for (ConfigModuleID moduleID : configModuleIDs)
				result.add(getConfigModuleFromDataStore(configManager, moduleID, fetchGroups, maxFetchDepth, monitor));
			// use the given monitor since the helper method is private and cannot be called from outside			
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("ConfigModule download failed!\n", e);
		}
		
		monitor.done();
		return result;
	}

	/**
	 * Helper method to retrieve a ConfigModule used by {@link #retrieveJDOObjects(Set, String[], int, IProgressMonitor)}
	 * 
	 * @return the ConfigModule corresponding to the given ConfigModuleID.
	 * @throws Exception if ConfigModule class of the corresponding ConfigModule to the given 
	 * 										ConfigModuleID is unknown.
	 * 
	 * FIXME: Delete this helper Method as soon as the Bean is able to fetch a bunch of ConfigModules at once! 
	 */
	private ConfigModule getConfigModuleFromDataStore(ConfigManager cm, ConfigModuleID moduleID, String[] fetchGroups, 
			int maxFetchDepth, IProgressMonitor monitor) throws Exception {
		String className = ConfigModule.getClassNameOutOfCfModKey(moduleID.cfModKey);
		Class cfModClass = null;
		try {
			cfModClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could resolve ConfigModule class "+className, e);
		}
		String cfModID = ConfigModule.getCfModIDOutOfCfModKey(moduleID.cfModKey);		
		ConfigID configID = ConfigID.create(moduleID.organisationID, moduleID.configKey, moduleID.configType);
		monitor.worked(1);
		return cm.getConfigModule(configID, cfModClass, cfModID, fetchGroups, maxFetchDepth);
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
			int maxFetchDepth, IProgressMonitor monitor) {
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
	public ConfigModule getConfigModule(ConfigModuleID moduleID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) {
		return getJDOObject(null, moduleID, fetchGroups, maxFetchDepth, monitor);
	}
	
	/**
	 * Get the ConfigModule of the given class and cfModID for the Config defined
	 * by the given configID.
	 */
	public ConfigModule getConfigModule(
			ConfigID config, Class cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth, 
			IProgressMonitor monitor) 
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
			String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) 
	{
		return getConfigModule((ConfigID)JDOHelper.getObjectId(config), cfModClass, cfModID, 
														fetchGroups, maxFetchDepth, monitor);
	}

}
