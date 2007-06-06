package org.nightlabs.jfire.base.config;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.core.runtime.ListenerList;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.dao.ConfigModuleDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.Utils;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ConfigModuleController 
implements IConfigModuleController 
{
	private static final Logger logger = Logger.getLogger(ConfigModuleController.class);
	
	public ConfigModuleController(AbstractConfigModulePreferencePage preferencePage) {
		this.preferencePage = preferencePage;
	}

	private ConfigID configID;
	public ConfigID getConfigID() {
		return configID;
	}
	public void setConfigID(ConfigID configID, boolean useNotAsPreferencePage) {
		this.configID = configID;
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
	public boolean checkIfIsGroupMember(ConfigModule module) 
	{
		ConfigID groupID = ConfigSetupRegistry.sharedInstance().getGroupForConfig(
				ConfigID.create(module.getOrganisationID(), 
						module.getConfigKey(), 
						module.getConfigType()
						));
		return groupID != null;
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
	@SuppressWarnings("unchecked")
	public ConfigModule retrieveConfigModule(ProgressMonitor monitor) 
	{
		if (getConfigID() == null)
			throw new RuntimeException("The configID of the Config for which the ConfigModule should be fetched is not set!");
		
		return Utils.cloneSerializable((ConfigModule) ConfigModuleDAO.sharedInstance().getConfigModule(
				getConfigID(), 
				getPreferencePage().getConfigModuleClass(),
				getPreferencePage().getConfigModuleCfModID(),
				getPreferencePage().getConfigModuleFetchGroups().toArray(new String[] {}),
				getPreferencePage().getConfigModuleMaxFetchDepth(), 
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
			throw new RuntimeException("The ConfigModule configModule passed to updateGuiWith(configModule) must not be null!");
		
		try {
			ConfigID newConfigID = (ConfigID) JDOHelper.getObjectId(configModule.getConfig()); 
			if (newConfigID != null && ! newConfigID.equals(getConfigID()))
				throw new IllegalStateException("The given ConfigModule does not belong to the Config this page is editing!");
		} catch (JDODetachedFieldAccessException e) {
			if (logger.isEnabledFor(Priority.WARN))
				logger.warn("The given ConfigModule has no Config detached with it! Module = "+configModule);			
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
