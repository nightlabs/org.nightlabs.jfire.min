package org.nightlabs.jfire.config.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManagerRemote;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * DAO for ConfigSetups. It is capable of serving {@link ConfigSetup} as a normal DAO
 * would (detached with their jdo-data). Additionally it is also capable of serving
 * {@link ConfigSetup}s that are prepared on the server side with their
 * {@link ConfigSetup#getCompleteConfigSetup(javax.jdo.PersistenceManager, String[], int, String[], int)} method.
 *
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
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

	/**
	 * Cache scope for the complete {@link ConfigSetup}s.
	 */
	public static final String SCOPE_COMPLETE_SETUPS = ConfigSetup.class.getName() + "#complete";
	/**
	 * Key for putting the result of the server-query for all {@link ConfigSetupID}s into the Cache.
	 */
	public static final String KEY_ALL_SETUP_IDS = ConfigSetup.class.getName() + "#allSetupIDs";

	public static final String[] FETCH_GROUPS_COMPLETE_GROUPS = new String[] {
		FetchPlan.DEFAULT
	};

	public static final String[] FETCH_GROUPS_COMPLETE_CONFIGS = new String[] {
		FetchPlan.DEFAULT, Config.FETCH_GROUP_CONFIG_GROUP
	};

	/**
	 * Private class used to pass the parameters
	 * for complete setups.
	 */
	private static class CompleteSetupParameters {
		String[] groupsFetchGroups;
		int groupsMaxFetchDepth;
		String[] configsFetchGroups;
		int configsMaxFetchDepth;
		CompleteSetupParameters(
				String[] groupsFetchGroups, int groupsMaxFetchDepth, String[] configsFetchGroups, int configsMaxFetchDepth)
		{
			this.groupsFetchGroups = groupsFetchGroups;
			this.groupsMaxFetchDepth = groupsMaxFetchDepth;
			this.configsFetchGroups = configsFetchGroups;
			this.configsMaxFetchDepth = configsMaxFetchDepth;
		}
	}

	/**
	 * Private class used to cache the results of getAllSetupIDs.
	 *
	 */
	private static class AllSetupIDsResult {
		Collection<ConfigSetupID> allSetupIDs;
		// DummySetup is used in order for the result to get removed
		// from the Cache when the ConfigSetup changes.
		ConfigSetup dummySetup;
		AllSetupIDsResult(Collection<ConfigSetupID> allSetupIDs, ConfigSetup dummySetup) {
			this.allSetupIDs = allSetupIDs;
			this.dummySetup = dummySetup;
		}
	}

	/**
	 * This is a switch for {@link #retrieveJDOObjects(Set, String[], int, ProgressMonitor)} that
	 * will fetch complete {@link ConfigSetup}s if this is not <code>null</code>.
	 * Complete means with {@link Config}s and {@link ConfigGroup}s included.
	 * <p>
	 * If this is <code>null</code>, the {@link ConfigSetup} objects will simply be fetched, detached
	 * with the given fetch-groups.
	 * </p>
	 */
	private CompleteSetupParameters completeSetupParameters = null;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<ConfigSetup> retrieveJDOObjects(Set<ConfigSetupID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		Collection<ConfigSetup> configSetups;
		ConfigManagerRemote cm = getEjbProvider().getRemoteBean(ConfigManagerRemote.class);
		if (completeSetupParameters != null) {
			monitor.beginTask("Fetching ConfigSetups", objectIDs.size());
			configSetups = new ArrayList<ConfigSetup>(objectIDs.size());
			for (ConfigSetupID configSetupID : objectIDs) {
				ConfigSetup cs = cm.getConfigSetup(
						configSetupID,
						completeSetupParameters.groupsFetchGroups, completeSetupParameters.groupsMaxFetchDepth,
						completeSetupParameters.configsFetchGroups, completeSetupParameters.configsMaxFetchDepth);
				configSetups.add(cs);
				monitor.worked(1);
			}
		} else {
			monitor.beginTask("Fetching ConfigSetups", 1);
			try {
				configSetups = cm.getConfigSetups(objectIDs, fetchGroups, maxFetchDepth);
			} catch (Exception e) {
				monitor.setCanceled(true);
				throw new RuntimeException("ConfigSetup download failed!\n", e);
			}
			monitor.worked(1);
		}
		monitor.done();
		return configSetups;
	}

	/**
	 * Returns all ConfigSetups simply detached with the given fetch-groups.
	 *
	 * @param fetchGroups the fetchgroups to use for detaching.
	 * @param maxFetchDepth the maximum fetch depth.
	 * @param monitor the monitor to show the progress with.
	 * @return all ConfigSetups.
	 */
	public Collection<ConfigSetup> getAllConfigSetups(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		Collection<ConfigSetupID> allIDs;
		try {
			ConfigManagerRemote cm = getEjbProvider().getRemoteBean(ConfigManagerRemote.class);
			allIDs = getAllConfigSetupIDs(cm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		getCache().put(SCOPE_COMPLETE_SETUPS, KEY_ALL_SETUP_IDS, allIDs, new String[] {}, -1);
		return getJDOObjects(null, allIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Returns the ConfigSetup corresponding to the given ConfigSetupID
	 * simply detached with the given fetch-groups.
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
	 * Returns the ConfigSetups corresponding to the given set of ConfigSetupIDs
	 * simply detached with the given fetch-groups.
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

	/**
	 * Returns the {@link ConfigSetup}s corresponding to the given configSetupIDs
	 * and prepared with their {@link ConfigSetup#getCompleteConfigSetup(javax.jdo.PersistenceManager, String[], int, String[], int)} method.
	 *
	 * @param configSetupIDs The {@link ConfigSetupID}s corresponding to the wanted ConfigSetups.
	 * @param groupsFetchGroups The fetch-groups to detach the {@link ConfigGroup}s of each {@link ConfigSetup} with.
	 * @param groupsMaxFetchDepth The maximum fetch-depth for detaching the {@link ConfigGroup}s of each {@link ConfigGroup}.
	 * @param configsFetchGroups The fetch-groups to detach the {@link Config}s of each {@link ConfigSetup} with.
	 * @param configsMaxFetchDepth The maximum fetch-depth for detaching the {@link Config}s of each {@link ConfigGroup}.
	 * @param monitor The monitor to report progress.
	 * @return The {@link ConfigSetup}s corresponding to the given ids.
	 */
	public Collection<ConfigSetup> getCompleteConfigSetups(Set<ConfigSetupID> configSetupIDs, String[] groupsFetchGroups, int groupsMaxFetchDepth, String[] configsFetchGroups, int configsMaxFetchDepth,
			ProgressMonitor monitor) {

		completeSetupParameters = new CompleteSetupParameters(groupsFetchGroups, groupsMaxFetchDepth, configsFetchGroups, configsMaxFetchDepth);
		try {
			return getJDOObjects(SCOPE_COMPLETE_SETUPS, configSetupIDs, new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
		} finally {
			completeSetupParameters = null;
		}
	}

	/**
	 * Get all {@link ConfigSetupID}s of all known {@link ConfigSetup}s (cached).
	 * @param cm The {@link ConfigManagerRemote} to use.
	 * @return All {@link ConfigSetupID}s of all known {@link ConfigSetup}s.
	 */
	private Collection<ConfigSetupID> getAllConfigSetupIDs(ConfigManagerRemote cm)  {
		AllSetupIDsResult result = (AllSetupIDsResult) getCache().get(SCOPE_COMPLETE_SETUPS, KEY_ALL_SETUP_IDS, new String[] {}, -1);
		if (result == null) {
			return cm.getAllConfigSetupIDs();
		}
		return result.allSetupIDs;
	}

	/**
	 * Returns all {@link ConfigSetup}s prepared with their {@link ConfigSetup#getCompleteConfigSetup(javax.jdo.PersistenceManager, String[], int, String[], int)} method.
	 *
	 * @param configSetupIDs The {@link ConfigSetupID}s corresponding to the wanted ConfigSetups.
	 * @param groupsFetchGroups The fetch-groups to detach the {@link ConfigGroup}s of each {@link ConfigSetup} with.
	 * @param groupsMaxFetchDepth The maximum fetch-depth for detaching the {@link ConfigGroup}s of each {@link ConfigGroup}.
	 * @param configsFetchGroups The fetch-groups to detach the {@link Config}s of each {@link ConfigSetup} with.
	 * @param configsMaxFetchDepth The maximum fetch-depth for detaching the {@link Config}s of each {@link ConfigGroup}.
	 * @param monitor The monitor to report progress.
	 * @return All {@link ConfigSetup}s.
	 */
	public Collection<ConfigSetup> getCompleteConfigSetups(
			String[] groupsFetchGroups, int groupsMaxFetchDepth, String[] configsFetchGroups, int configsMaxFetchDepth,
			ProgressMonitor monitor)
	{
		Collection<ConfigSetupID> allIDs;
		try {
			ConfigManagerRemote cm = getEjbProvider().getRemoteBean(ConfigManagerRemote.class);
			allIDs = getAllConfigSetupIDs(cm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Collection<ConfigSetup> setups = getCompleteConfigSetups(new HashSet<ConfigSetupID>(allIDs), groupsFetchGroups, groupsMaxFetchDepth, configsFetchGroups, configsMaxFetchDepth, monitor);
		AllSetupIDsResult allSetupDsResult = new AllSetupIDsResult(allIDs, setups.iterator().next());
		getCache().put(SCOPE_COMPLETE_SETUPS, KEY_ALL_SETUP_IDS, allSetupDsResult, new String[] {}, -1);
		return setups;
	}

	/**
	 * Returns all {@link ConfigSetup}s
	 * prepared with their {@link ConfigSetup#getCompleteConfigSetup(javax.jdo.PersistenceManager, String[], int, String[], int)} method.
	 * This method uses default values for the fetch-groups and fetch-depth. (See {@link #FETCH_GROUPS_COMPLETE_GROUPS}
	 * {@link #FETCH_GROUPS_COMPLETE_CONFIGS}).
	 *
	 * @param monitor The monitor to report progress.
	 * @return All {@link ConfigSetup}s.
	 */
	public Collection<ConfigSetup> getCompleteConfigSetups(ProgressMonitor monitor) {
		return getCompleteConfigSetups(
				FETCH_GROUPS_COMPLETE_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				FETCH_GROUPS_COMPLETE_CONFIGS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor);
	}

	/**
	 * Returns the {@link ConfigSetup}s of the given type, if existent, <code>null</code> otherwise.
	 * The setup will be prepared with its {@link ConfigSetup#getCompleteConfigSetup(javax.jdo.PersistenceManager, String[], int, String[], int)} method.
	 *
	 * @param configSetupType The type of the {@link ConfigSetup} to fetch.
	 * @param monitor The monitor to report progress.
	 * @return The ConfigSetup of the given configSetupType or <code>null</code>.
	 */
	public ConfigSetup getCompleteConfigSetup(String configSetupType, ProgressMonitor monitor) {
		ConfigSetupID configSetupID = ConfigSetupID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), configSetupType);
		Collection<ConfigSetup> configSetups = getCompleteConfigSetups(
				Collections.singleton(configSetupID),
				FETCH_GROUPS_COMPLETE_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				FETCH_GROUPS_COMPLETE_CONFIGS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor);
		if (configSetups.size() != 1)
			throw new IllegalStateException("GetCompleteConfigSetups did not return expected number of ConfigSetups (1), it returned " + configSetups.size());
		return configSetups.iterator().next();
	}


	/**
	 * Returns the {@link ConfigSetup} that holds ConfigGroups of the same type as
	 * the given ConfigGroup. Be sure to pass the ConfigID of a ConfigGroup
	 * here.
	 * The setup will be prepared with its {@link ConfigSetup#getCompleteConfigSetup(javax.jdo.PersistenceManager, String[], int, String[], int)} method.
	 *
	 * @param configGroupID The ConfigID of a ConfigGroup.
	 * @param
	 * @return ConfigType with configGroupType like the given ID's configType.
	 */
	public ConfigSetup getConfigSetupForGroup(ConfigID configGroupID, ProgressMonitor monitor) {
		String configType = configGroupID.configType;
		if (configType == null || "".equals(configType)) //$NON-NLS-1$
			throw new IllegalArgumentException("Parameter configType must not be null or empty!"); //$NON-NLS-1$
		Collection<ConfigSetup> configSetups = getCompleteConfigSetups(monitor);

		for (ConfigSetup configSetup : configSetups) {
			if (configSetup.getConfigGroupType().equals(configType)) {
				return configSetup;
			}
		}
		return null;
	}

	/**
	 * Checks whether a ConfigSetup is registered that links Configs to Objects
	 * of the given linkClassName.
	 *
	 * @param linkClassName The classname of the linked objects.
	 * @param monitor The monitor to report progress.
	 * @return Whether there is a ConfigSetup registered that links objects of the
	 * given class.
	 */
	public boolean containsRegistrationForLinkClass(String linkClassName, ProgressMonitor monitor) {
		return getConfigSetupForConfigType(linkClassName, monitor) != null;
	}

	/**
	 * Returns the {@link ConfigSetup} that either has {@link Config}s or {@link ConfigGroup}s with
	 * the configType of the given ConfigID.
	 * The setup will be prepared with its {@link ConfigSetup#getCompleteConfigSetup(javax.jdo.PersistenceManager, String[], int, String[], int)} method.
	 *
	 * @param configID The ConfigID which type should be part of the returned ConfigSetup
	 * @param monitor The monitor to report progress.
	 * @return The ConfigSetup that either has {@link Config}s or {@link ConfigGroup}s with
	 * 		the configType of the given ConfigID.
	 */
	public ConfigSetup getConfigSetupForConfigType(ConfigID configID, ProgressMonitor monitor) {
		return getConfigSetupForConfigType(configID.configType, monitor);
	}

	/**
	 * Returns the ConfigSetup, which links to the Object whose classname == <code>configType</code>.
	 * The setup will be prepared with its {@link ConfigSetup#getCompleteConfigSetup(javax.jdo.PersistenceManager, String[], int, String[], int)} method.
	 *
	 * @param configType the configType(full classname) of the Object to check for Linkage of ConfigSetups.
	 * @param monitor The monitor to report progress.
	 * @return the ConfigSetup, which links to the Object with classname == <code>configType</code>.
	 */
	public ConfigSetup getConfigSetupForConfigType(String configType, ProgressMonitor monitor) {
		if (configType == null || "".equals(configType)) //$NON-NLS-1$
			throw new IllegalArgumentException("Parameter configType must not be null or empty!"); //$NON-NLS-1$
		Collection<ConfigSetup> configSetups = getCompleteConfigSetups(monitor);

		for (ConfigSetup configSetup : configSetups) {
			if (configSetup.getConfigType().equals(configType)) {
				return configSetup;
			}
		}
		for (ConfigSetup configSetup : configSetups) {
			if (configSetup.getConfigGroupType().equals(configType)) {
				return configSetup;
			}
		}
		return null;
	}

	/**
	 * Checks whether the given ConfigID is the id-object of a ConfigGroup.
	 * @param configID The {@link ConfigID} to check.
	 * @param monitor The monitor to report progress.
	 */
	public boolean isConfigGroup(ConfigID configID, ProgressMonitor monitor) {
		return getConfigSetupForGroup(configID, monitor) != null;
	}

	/**
	 * Checks whether a ConfigSetup is registered that has a Config linked to
	 * the given linkObject.
	 *
	 * @param linkObject The object to check linkage for.
	 * @param monitor The monitor to report progress.
	 * @return Whether there is a ConfigSetup registered that links the given
	 * linkObject.
	 */
	public boolean containsRegistrationForLinkObject(Object linkObject, ProgressMonitor monitor) {
		return containsRegistrationForLinkClass(linkObject.getClass(), monitor);
	}

	/**
	 * Checks whether a ConfigSetup is registered that links {@link Config} to Objects
	 * of the given Class.
	 *
	 * @param linkClass The Class of the linked objects.
	 * @param monitor The monitor to report progress.
	 * @return Whether there is a ConfigSetup registered that links objects of the
	 * given linkClass.
	 */
	public boolean containsRegistrationForLinkClass(Class<?> linkClass, ProgressMonitor monitor) {
		return containsRegistrationForLinkClass(linkClass.getName(), monitor);
	}
}
