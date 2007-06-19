/**
 * 
 */
package org.nightlabs.jfire.config.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Small DAO for ConfigSetups. 
 * 
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 */
public class ConfigSetupDAO extends BaseJDOObjectDAO<ConfigSetupID, ConfigSetup> {
	
	protected ConfigSetupDAO() {
		super();
	};

	private static ConfigSetupDAO sharedInstance = null;

	public static ConfigSetupDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ConfigSetupDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ConfigSetupDAO();
			}
		}
		return sharedInstance;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<ConfigSetup> retrieveJDOObjects(Set<ConfigSetupID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		Collection<ConfigSetup> configSetups;
		monitor.beginTask("Fetching Configs", 1);
		try {
			ConfigManager cm = ConfigManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			configSetups = cm.getConfigSetups(objectIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException("User information download failed!\n", e);
		}
		
		monitor.worked(1);
		monitor.done();
		return configSetups;
	}
	
	/**
	 * Returns all ConfigSetups.
	 * 
	 * @param fetchGroups the fetchgroups to use for detaching.
	 * @param maxFetchDepth the maximum fetch depth.
	 * @param monitor the monitor to show the progress with.
	 * @return all ConfigSetups.
	 */
	public Collection<ConfigSetup> getAllConfigSetups(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		Collection<ConfigSetupID> allIDs;
		try {
			ConfigManager cm = ConfigManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			allIDs = cm.getAllConfigSetupIDs(fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return getJDOObjects(null, allIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Returns the ConfigSetup corresponding to the given ConfigSetupID.
	 * 
	 * @param setupID the {@link ConfigSetupID} corresponding to the wanted ConfigSetup.
	 * @param fetchGroups the fetchgroups to use for detaching.
	 * @param maxFetchDepth the maximum fetch depth.
	 * @param monitor the monitor to show the progress with.
	 * @return the ConfigSetup corresponding to the given ConfigSetupID.
	 */
	public ConfigSetup getConfigSetup(ConfigSetupID setupID, String[] fetchGroups, int maxFetchDepth, 
			ProgressMonitor monitor) 
	{
		return getJDOObject(null, setupID, fetchGroups, maxFetchDepth, monitor);
	}
	
	/**
	 * Returns the ConfigSetups corresponding to the given set of ConfigSetupIDs.
	 * 
	 * @param setupIDs the {@link ConfigSetupID}s corresponding to the wanted ConfigSetups.
	 * @param fetchGroups the fetchgroups to use for detaching.
	 * @param maxFetchDepth the maximum fetch depth.
	 * @param monitor the monitor to show the progress with.
	 * @return the ConfigSetup corresponding to the given ConfigSetupID.
	 */
	public Collection<ConfigSetup> getConfigSetups(Set<ConfigSetupID> setupIDs, String[] fetchGroups, 
			int maxFetchDepth, ProgressMonitor monitor) 
	{
		return getJDOObjects(null, setupIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
