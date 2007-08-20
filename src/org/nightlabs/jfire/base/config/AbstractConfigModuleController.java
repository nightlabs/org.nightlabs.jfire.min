package org.nightlabs.jfire.base.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.core.runtime.ListenerList;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.dao.ConfigModuleDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.Utils;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractConfigModuleController 
implements IConfigModuleController 
{
	private static final Logger logger = Logger.getLogger(AbstractConfigModuleController.class);
	
	public AbstractConfigModuleController(AbstractConfigModulePreferencePage preferencePage) {
		this.preferencePage = preferencePage;
	}

	private ConfigID configID;
	public ConfigID getConfigID() {
		return configID;
	}
	
	private String configModuleID;
	public String getConfigModuleID() {
		return configModuleID;
	}
	
	public void setConfigID(ConfigID configID, boolean useNotAsPreferencePage, String configModuleID) {
		this.configID = configID;
		this.configModuleID = configModuleID;
		
		if (useNotAsPreferencePage) {
			getPreferencePage().doSetControl = true;
		}
	}
	
	private ConfigModule configModule = null;
	public ConfigModule getConfigModule() {
		return configModule;
	}
	public void setConfigModule(ConfigModule configModule) {
		this.configModule = configModule;
		fireConfigModuleChanged();
	}
	
	private ListenerList listeners = new ListenerList();	
	public void addConfigModuleChangedListener(IConfigModuleChangedListener listener) {
		listeners.add(listener);
	}
	public void removeConfigModuleChangedListener(IConfigModuleChangedListener listener) {
		listeners.remove(listener);
	}
	protected void fireConfigModuleChanged() {
		for (int i=0; i<listeners.getListeners().length; i++) {
			IConfigModuleChangedListener l = (IConfigModuleChangedListener) listeners.getListeners()[i];
			l.configModuleChanged(getConfigModule());
		}
	}
	
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
	public boolean canEdit(ConfigModule configModule) { 
		return
				configModule.isGroupConfigModule() ||
				configModule.getFieldMetaData(ConfigModule.class.getName()).isWritable()
				|| ! checkIfIsGroupMember(configModule);
	}
	
	/**
	 * Checks if the configModule is member of a configGroup 
	 */
	public boolean checkIfIsGroupMember(ConfigModule module) {
		return getGroupConfigModule(module) != null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.config.IConfigModuleController#getGroupConfigModule(org.nightlabs.jfire.config.ConfigModule)
	 */
	public ConfigModule getGroupConfigModule(ConfigModule memberModule) {
		return ConfigModuleDAO.sharedInstance().getGroupsCorrespondingModule(
				configID, getConfigModuleClass(), configModuleID, 
				getConfigModuleFetchGroups().toArray(new String[0]), 
				getConfigModuleMaxFetchDepth(), new NullProgressMonitor()
				);
	}
	
	/**
	 * This is the default Set of fetch groups needed for any ConfigModule it contains: 
	 * {@link FetchPlan#DEFAULT}, {@link ConfigModule#FETCH_GROUP_FIELDMETADATAMAP},
	 * {@link ConfigModule#FETCH_GROUP_CONFIG}.
	 * 
	 * If subclasses want to extend these default fetch groups they need to overwrite 
	 * {@link #getConfigModuleFetchGroups()}. 
	 */
	private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>(); 

	/**
	 * Returns fetch-groups containing the FetchPlans, which are surely needed:
	 * {@link #CONFIG_MODULE_FETCH_GROUPS}. <br>
	 * Subclasses are intended to create a new set from this one and extend it with the fetch groups 
	 * covering the fields their class extended {@link ConfigModule} with. <p>
	 * 
	 * Note: To omit the growth of this set, as it is being used in different contexts, the returned set is 
	 * unmodifiable! 
	 * 
	 * @return an unmodifiable Set of Strings containing the default ConfigModule fetch groups ({@value #CONFIG_MODULE_FETCH_GROUPS}.
	 */
	public static Set<String> getCommonConfigModuleFetchGroups() {
		if (CONFIG_MODULE_FETCH_GROUPS.isEmpty()) {
			CONFIG_MODULE_FETCH_GROUPS.add(FetchPlan.DEFAULT);
			CONFIG_MODULE_FETCH_GROUPS.add(ConfigModule.FETCH_GROUP_FIELDMETADATAMAP);
			CONFIG_MODULE_FETCH_GROUPS.add(ConfigModule.FETCH_GROUP_CONFIG);
		}
		
		return Collections.unmodifiableSet(CONFIG_MODULE_FETCH_GROUPS);
	}
	
	/**
	 * Returns the unlimited fetch depth. If subclasses need to restrict the fetch depth, then
	 * they need overwrite this method.
	 *  
	 * @return the unlimited fetch depth.
	 */
	public int getConfigModuleMaxFetchDepth() {
		return NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT;
	}
	
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
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public ConfigModule retrieveConfigModule(ProgressMonitor monitor) 
	{
		if (getConfigID() == null)
			throw new RuntimeException("The configID of the Config for which the ConfigModule should be fetched is not set!"); //$NON-NLS-1$
		
		return Utils.cloneSerializable((ConfigModule) ConfigModuleDAO.sharedInstance().getConfigModule(
				getConfigID(), 
				getConfigModuleClass(),
				configModuleID,
				getConfigModuleFetchGroups().toArray(new String[] {}),
				getConfigModuleMaxFetchDepth(), 
				monitor
				));
	}
	
	private AbstractConfigModulePreferencePage preferencePage = null;
	public AbstractConfigModulePreferencePage getPreferencePage() {
		return preferencePage;
	}
	
	/**
	 * Sets the current version of the {@link ConfigModule} to display and updates the page 
	 * accordingly. 
	 * The given ConfigModule should be a copy of the one from the cache, otherwise changes to this 
	 * module will corrupt the state of the one in the cache! Use {@link Utils#cloneSerializable(Object)}
	 * to create a copy.
	 * 
	 * @param configModule The {@link ConfigModule} to set.
	 */
	public void updateGuiWith(ConfigModule configModule) 
	{
		if (configModule == null)
			throw new RuntimeException("The ConfigModule configModule passed to updateGuiWith(configModule) must not be null!"); //$NON-NLS-1$
		
		try {
			ConfigID newConfigID = (ConfigID) JDOHelper.getObjectId(configModule.getConfig()); 
			if (newConfigID != null && ! newConfigID.equals(getConfigID()))
				throw new IllegalStateException("The given ConfigModule does not belong to the Config this page is editing!"); //$NON-NLS-1$
		} catch (JDODetachedFieldAccessException e) {
			if (logger.isEnabledFor(Priority.WARN))
				logger.warn("The given ConfigModule has no Config detached with it! Module = "+configModule); //$NON-NLS-1$
		} // if config is not in FetchGroups -> believe given configModule belongs to this config

		setConfigModule(configModule);
		getPreferencePage().currentConfigIsGroupMember = checkIfIsGroupMember(configModule);
		getPreferencePage().currentConfigModuleIsEditable = canEdit(configModule);
		getPreferencePage().configChanged = false;
		
		getPreferencePage().updateConfigHeader();		
		getPreferencePage().updatePreferencePage();
		getPreferencePage().setEditable(getPreferencePage().currentConfigModuleIsEditable);
	}	
}
