/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 ******************************************************************************/
package org.nightlabs.jfire.config.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * An access object for configs.
 * 
 * @version $Revision: 6439 $ - $Date: 2007-05-09 14:13:14 +0200 (Mi, 09 Mai 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ConfigDAO extends BaseJDOObjectDAO<ConfigID, Config>
{
	/**
	 * The shared instance.
	 */
	private static ConfigDAO sharedInstance = null;

	/**
	 * Get the lazily created shared instance.
	 * @return The shared instance
	 */
	public static ConfigDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new ConfigDAO();
		return sharedInstance;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Collection, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	@Implement
	protected Collection<Config> retrieveJDOObjects(
			Set<ConfigID> configIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor) throws Exception
	{
		Collection<Config> configs;
		monitor.beginTask("Fetching Configs", 1);
		try {
			ConfigManager cm = ConfigManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			// ConfigManager does not provide a way to get multiple Configs...
			configs = cm.getConfigs(configIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("User information download failed!\n", e);
		}
		
		monitor.worked(1);
		monitor.done();
		return configs;
	}
	
	/**
	 * Get a config object.
	 * @param configID The config id to get
	 * @param fetchgroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return the requested config object
	 */
  public synchronized Config getConfig(ConfigID configID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
  {
  	return getJDOObject(null, configID, fetchGroups, maxFetchDepth, monitor);
	}
  
	/**
	 * Get a Collection of config objects.
	 * @param configIDs The set of ConfigIDs corresponding to the desired configs.
	 * @param fetchgroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return a list with requested config objects
	 */
  public synchronized Collection<Config> getConfigs (Set<ConfigID> configIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
  {
		return getJDOObjects(null, configIDs, fetchGroups, maxFetchDepth, monitor);
	}
  
	/**
	 * Get a Collection of all configs of a certain type.
	 * @param configType The type of configs to fetch
	 * @param fetchgroups The fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return a collection of all config of type <code>configType</code>
	 */
  public synchronized Collection<Config> getConfigs(String configType,
  			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
  	// get all ConfigIDs of the corresponding Configs
  	Collection<ConfigID> configIDs;
  	try {
  		ConfigManager cm = ConfigManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
  		configIDs = cm.getConfigIDsByConfigType(configType, fetchGroups, maxFetchDepth);
  	} catch (Exception e) {
  		throw new RuntimeException("Error while downloading ConfigIDs!\n" ,e);
  	}
  	
  	// ask the cache if there are already wanted Configs present
  	return getJDOObjects(null, configIDs, fetchGroups, maxFetchDepth, monitor);
	}
  
  /**
   * Stores the given {@link Config}.
   * @param config The {@link Config} to be stored.
	 * @param get A boolean indicating whether a detached copy of the stored {@link Config} should be returned.
	 * @param fetchGroups The fetch groups to be used
	 * @param maxFetchDepth The maximal fetch depth to be used
	 * @return A detached copy of the stored {@link Config} if <code>get == true</code> and <code>null</code> otherwise.
   */
  public synchronized Config storeConfig(Config config, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
  	monitor.beginTask("Saving config...", 10);
  	try {
  		ConfigManager cm = ConfigManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
  		return cm.storeConfig(config, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException("Error while saving the config.");
		} finally {
  		monitor.done();
  	}
	}
}
