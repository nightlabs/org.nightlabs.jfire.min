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
package org.nightlabs.jfire.base.config;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * An access object for configs.
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ConfigDAO extends JDOObjectDAO<ConfigID, Config>
{
	/**
	 * The temporary used config manager.
	 */
	ConfigManager cm;
	
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
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObject(java.lang.Object, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Config retrieveJDOObject(ConfigID configID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) throws Exception
	{
		Assert.isNotNull(cm);
		Config config = cm.getConfig(configID, fetchGroups, maxFetchDepth);
		monitor.worked(1);
		return config;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Collection, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Collection<Config> retrieveJDOObjects(
			Collection<ConfigID> configIDs, String[] fetchGroups, int maxFetchDepth,
			IProgressMonitor monitor) throws Exception
	{
		// ConfigManager does not provide a way to get multiple Configs...
		Collection<Config> configs = new ArrayList<Config>(configIDs.size());
		for (ConfigID configID : configIDs)
			configs.add(retrieveJDOObject(configID, fetchGroups, maxFetchDepth, monitor));
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
  public synchronized Config getConfig(ConfigID configID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
  {
  	try {
  		cm = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
  		Config config = getJDOObject(null, configID, fetchGroups, maxFetchDepth, monitor);
  		monitor.worked(1);
  		return config;
  	} catch(Exception e) {
  		throw new RuntimeException("Config download failed", e);
  	} finally {
  		cm = null;
  	}
	}
}
