/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.base.notification.NotificationAdapterJob;
import org.nightlabs.base.progress.XProgressMonitor;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.dao.ConfigModuleDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.Utils;

/**
 * An abstract PreferencePage for ConfigModules.
 * Basicly it takes care of retrieving and storing
 * the config module for you and provides callbacks
 * to present the module to the user.
 * <p>
 * See 
 * <ul>
 * <li>{@link #getConfigModuleClass()}</li>
 * <li>{@link #createPreferencePage(Composite)}</li>
 * <li>{@link #updatePreferencePage(ConfigModule)}</li>
 * <li>{@link #updateConfigModule(ConfigModule)}</li>
 * </ul>
 * for methods you need to implement.
 * 
 * <p>
 * Also take a look at
 * {@link #getConfigModuleFetchGroups()}
 * in order to pass appropriate fetch groups to detach
 * your config module.
 * 
 * <p>
 * Note that by default the {@link ConfigModule}s are cloned
 * by {@link Utils#cloneSerializable(Object)} before passed
 * to implementors so they might be changed directly without
 * taking care of restorage when the user decides not to
 * store the configuration.
 * See {@link #doCloneConfigModule()} in order to change this
 * behaviour.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class AbstractConfigModulePreferencePage<C extends ConfigModule> 
extends PreferencePage 
implements IWorkbenchPreferencePage
{

	// TODO: are you sure you want FetchPlan.ALL?, Yes, as default, individual pages, can overwrite.
	protected static String[] CONFIG_MODULE_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT, FetchPlan.ALL}; 

	private XComposite wrapper;
	private XComposite header;
	private Button checkBoxAllowOverwrite;


	protected ConfigModuleChangeListener changeListener = new ConfigModuleChangeListener();
	protected ConfigID currentConfigID;
	protected C currentConfigModule;
	protected boolean currentConfigIsGroupMember = false;
	protected boolean configChanged = false;
	
	/**
	 * 
	 */
	public AbstractConfigModulePreferencePage() {
		super();
	}

	/**
	 * @param title
	 */
	public AbstractConfigModulePreferencePage(String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public AbstractConfigModulePreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Checks if the user is allowed to changed configuration 
	 * for groups or other users.
	 * 
	 * @return Weather the user is allowed to change other configurations
	 */
	protected boolean isUserConfigSelectionAllowed() {
		return true;
	}

	/**
	 * Returns the current config module.
	 * 
	 * @return The current config module.
	 */
	protected C getCurrentConfigModule() {
		return currentConfigModule;
	}

	/**
	 * Sets the current {@link ConfigModule}.
	 * Note that according to {@link #doCloneConfigModule()}
	 * the module might be cloned before it is used.
	 * 
	 * @param configModule The {@link ConfigModule} to set.
	 */
	protected void setCurrentConfigModule(C configModule) {
		setCurrentConfigModule(configModule, doCloneConfigModule());
	}
	
	/**
	 * Clones the given ConfigModule according to <code>doCloneConfigModule</code>.
	 *   
	 * @param configModule new ConfigModule to display
	 * @param doCloneConfigModule whether or not the new ConfigModule shall be cloned before displaying
	 */
	void setCurrentConfigModule(C configModule, boolean doCloneConfigModule) {
		if (doCloneConfigModule)
			this.currentConfigModule = Utils.cloneSerializable(configModule);
		else
			this.currentConfigModule = configModule;
	}
	
	private void checkAndSetIsGroupMember(C module) {
		ConfigID groupID = ConfigSetupRegistry.sharedInstance().getGroupForConfig(
				ConfigID.create(module.getOrganisationID(), 
						module.getConfigKey(), 
						module.getConfigType()
						));
		currentConfigIsGroupMember = groupID != null;
	}

	/**
	 * only needed for MessageBox result on Gui Thread
	 */
	private boolean doReloadConfigModule = false;
	protected class ConfigModuleChangeListener extends NotificationAdapterJob {

		public void notify(NotificationEvent notificationEvent) {
			Set<DirtyObjectID> dirtyObjectIDs = notificationEvent.getSubjects();
			ConfigModuleID currentModuleID = (ConfigModuleID) JDOHelper.getObjectId(currentConfigModule);
//		 there aren't many open ConfigModulePages showing the same kind of ConfigModule, this loop is 
// 			therefore not as time consuming as one might think. But if the set of DirtyObjectIDs
//			would be capable of efficiently checking whether a given ConfigID is contained inside itself,
//			then this check would be faster.
			boolean moduleIsUpdated = false;
			for (DirtyObjectID dirtyID : dirtyObjectIDs) { 
				if (! dirtyID.getObjectID().equals( currentModuleID ))
					continue;
				
				moduleIsUpdated = true;
				break;
			}
			
			if (! moduleIsUpdated)
				return;
			
			// check if this module has been saved lately and is therefore already up to date.
			if (recentlySaved) {
				recentlySaved = false;
				return;
			}
			
			ConfigModuleDAO moduleDAO = ConfigModuleDAO.sharedInstance();
			C updatedModule = (C) moduleDAO.getConfigModule(currentModuleID, getConfigModuleFetchGroups(), 
					getConfigModuleMaxFetchDepth(), getProgressMonitorWrapper());
			
			checkAndSetIsGroupMember(updatedModule);

			// Check if cache sent the same version of the GroupModule after the ChildModule got changed.
			// This might happen, since the cache removes all objects depending on a changed one.
			// Child ConfigModule changes -> GroupModule will be removed from Cache as well.
//			if (JDOHelper.getVersion(currentConfigModule) == JDOHelper.getVersion(updatedModule))
			// --> not applicable, since change in ChildConfigModule results in new Version of GroupConfigModule
//			if (currentConfigModule.isGroupConfigModule() && currentConfigModule.isContentEqual(updatedModule))
			// --> not applicable either, since isContentEqual needs to rely on equals of the the members 
			//     of every ConfigModule and equals of JDOObjects is agreed to be true iff the corresponding
			//     JDOObjectIDs are equal.
			// FIXME: Hence, we need a new way of checking whether the content of two given ConfigModules is equal!
			// Until then we reload the page iff the currentModule hasn't changed, else we ask the user if the page shall be reloaded.
			if (currentConfigModule.isGroupConfigModule()) {
				if (! configChanged) {
					setCurrentConfigModule(updatedModule);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							updateConfigHeader();
							updatePreferencePage(currentConfigModule);								
						}
					});
					return;
					
				} else {
					doReloadConfigModule = false;
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							doReloadConfigModule = MessageDialog.openConfirm(RCPUtil.getActiveWorkbenchShell(), "ConfigModule changed!",
							"Do you want the config module to be reloaded?");
						}
					});
					
					if (!doReloadConfigModule)
						return;
					
					setCurrentConfigModule(updatedModule);
					
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							updateConfigHeader();
							updatePreferencePage(currentConfigModule);								
						}
					});
					return;
				}
			}				
			// end of workaround
			
			// if oldModule was not editable or new module isn't than simply update the page
			if (! canEdit(currentConfigModule) || ! canEdit(updatedModule)) {
				setCurrentConfigModule(updatedModule);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						updateConfigHeader();
						updatePreferencePage(currentConfigModule);								
					}
				});
			} else { 
				// if module is affected by a groupmodule but may overwrite groupmodule's settings -> notify User
				doReloadConfigModule = false;
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						doReloadConfigModule = MessageDialog.openConfirm(RCPUtil.getActiveWorkbenchShell(), "ConfigModule changed!",
						"Do you want the config module to be reloaded?");
					}
				});

				if (!doReloadConfigModule)
					return;
				
				setCurrentConfigModule(updatedModule);
				
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						updateConfigHeader();
						updatePreferencePage(currentConfigModule);								
					}
				});
			}
		} // notify(NotificationEvent notificationEvent)
		
	} // ConfigModuleChangeListener

	/**
	 * Determines whether config modules are
	 * cloned by {@link Utils#cloneSerializable(Object)}
	 * after retrieval. Doing so allows pages to change
	 * the module directly, without changing the instance
	 * that might be in the Cache and used in other places
	 * throughout the application?
	 * The default implementation returns <code>true</code>.
	 * 
	 * @return Whether the config module should be cloned.
	 */
	protected boolean doCloneConfigModule() {
		return true;
	}

	/**
	 * Whether the current config has changed since it was last set.
	 *  
	 * @return Whether the current config has changed since it was last set.
	 */
	public boolean isConfigChanged() {
		return configChanged;
	}

	/**
	 * Use this to mark the config as changed/not changed in your implementation.
	 * <p>
	 * Configurations will only be stored to the server if {@link #isConfigChanged()}
	 * returns true when the user decides to store.
	 * 
	 * @param configChanged The changed flag for the Config.
	 */
	protected void setConfigChanged(boolean configChanged) {
		if (!canEdit(currentConfigModule))
			return;
		
		this.configChanged = configChanged;
		if (configChanged)
			notifyConfigChangedListeners();
	} 

	/**
	 * Default value is true can only be changed by {@link #createContents(Composite, boolean, boolean, boolean)}
	 * This is needed because the PreferencePage is used in the Eclipse Preferences Dialog as well as in
	 * the custom {@link ConfigPreferencesEditComposite2}. 
	 */
	private boolean refreshConfigModule = true;
	
	/**
	 * Default value is false can only be changed by {@link #createContents(Composite, boolean, boolean, boolean)}
	 */
	private boolean doCreateConfigGroupHeader = false;
	/**
	 * Default value is false can only be changed by {@link #createContents(Composite, boolean, boolean, boolean)}
	 */
	private boolean doSetControl = false;

	/**
	 * This might be called from outside in order to create the page, 
	 * when the PreferencePage is not used within the Eclipse Preferences Dialog.
	 * 
	 * @param parent The parent to add the page to.
	 * @param refreshConfigModule Whether to refresh and display the config module 
	 * @param doCreateConfigGroupHeader Whether to create the header showing controls for ConfigModules of {@link ConfigGroup}s
	 * @param doSetControl Whether to call super.setControl() wich is only needed, when inside the Preferences Dialog.
	 * @return The pages Top control.
	 */
	public Control createContents(Composite parent, boolean doCreateConfigGroupHeader, 
			boolean doSetControl, C configModule) {
		
		currentConfigModule = configModule;
		checkAndSetIsGroupMember(currentConfigModule);
		this.doCreateConfigGroupHeader = doCreateConfigGroupHeader;
		this.doSetControl = doSetControl;
		refreshConfigModule = configModule == null;
		try {
			return createContents(parent);
		}
		finally {
			doCreateConfigGroupHeader = false;
			doSetControl = false;
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		XComposite x = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);		
		header = new XComposite(x, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);
		header.getGridData().grabExcessVerticalSpace = false;
		wrapper = new XComposite(x, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		JDOLifecycleManager.sharedInstance().addNotificationListener(getConfigModuleClass(), changeListener);

		if (doCreateConfigGroupHeader)
			createConfigGroupHeader(header);
		
		createPreferencePage(wrapper);

		if (refreshConfigModule) {
			if (getCurrentConfigModule() == null)
				setCurrentConfigModule(retrieveConfigModule());
			
			updatePreferencesGUI(currentConfigModule);
		}

		
		if (doSetControl)
			setControl(x);
		return x;
	}

	protected C retrieveConfigModule() {
		return (C) ConfigModuleDAO.sharedInstance().getConfigModule(
				currentConfigID, 
				getConfigModuleClass(),
				getConfigModuleCfModID(),
				getConfigModuleFetchGroups(),
				getConfigModuleMaxFetchDepth(), 
				new NullProgressMonitor() // TODO: Wrap a job around this ...
				);
	}
	
	/**
	 * Creates the header of any ConfigPreferncePage. If additional components are 
	 * to be shown, then this method should be overwritten.
	 * @param parent the wrapper composite to pack all components into.
	 */
	protected void updateConfigHeader() {
		boolean headerCreated = false;
		if (! canEdit(getCurrentConfigModule())) {
			clearComposite(header);
			new Label(header, 0).setText(Messages.getString("AbstractConfigModulePreferencePage.GroupDisallowsOverwrite")); //$NON-NLS-1$
			headerCreated = true;
		} else if (currentConfigIsGroupMember) {
			clearComposite(header);
			Button resetToGroupDefaults = new Button(header, 0);
			resetToGroupDefaults.setText(Messages.getString("AbstractConfigModulePreferencePage.ResetToGroupConfig_ButtonText")); //$NON-NLS-1$
			resetToGroupDefaults.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent arg0) {}
				public void widgetSelected(SelectionEvent arg0) {
					boolean doIt = MessageDialog.openConfirm(RCPUtil.getActiveWorkbenchShell(), Messages.getString("AbstractConfigModulePreferencePage.ResetToGroupConfig_ConfirmationDialogHeading"),  //$NON-NLS-1$
							Messages.getString("AbstractConfigModulePreferencePage.ResetToGroupConfig_ConfirmationDialogMessage")); //$NON-NLS-1$
					if (! doIt)
						return;
					
					ConfigManager cm;
					try {
					 cm = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
					 ConfigModuleID moduleID = (ConfigModuleID) JDOHelper.getObjectId(currentConfigModule);
					 setCurrentConfigModule( (C) cm.applyGroupInheritence(
							 				moduleID, 
							 				true, 
							 				getConfigModuleFetchGroups(), 
							 				getConfigModuleMaxFetchDepth())
							 );
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					updatePreferencePage(currentConfigModule);
				}			
			});
			headerCreated = true;
		}
		else {
			header.dispose();
		}
		if (headerCreated) {
			(new Label(header, SWT.SEPARATOR | SWT.HORIZONTAL)).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			header.layout(true, true);
		}
	}
	
	/**
	 * Clears all contents of a given Composite.
	 * @param comp the composite to clear.
	 */
	private void clearComposite(Composite comp) {
		Control[] children = comp.getChildren();
		
		if (comp.getChildren().length == 0)
			return;
		
		for (Control child : children)
			child.dispose();
	}

	/**
	 * Create the header showing controls for {@link ConfigModule}s of {@link ConfigGroup}s.
	 * <p>
	 * Default implementation adds a checkbox for controlling overwriting for the complete module.
	 * 
	 * @param parent The parent to add the header to.
	 */
	protected void createConfigGroupHeader(Composite parent) {
		checkBoxAllowOverwrite = new Button(parent, SWT.CHECK);
		checkBoxAllowOverwrite.setText(Messages.getString("AbstractConfigModulePreferencePage.WhetherGroupAllowsConfigOverwrite")); //$NON-NLS-1$
		
		checkBoxAllowOverwrite.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				setConfigChanged(true);
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		checkBoxAllowOverwrite.setLayoutData(gd);
	}


	/**
	 * Called to ask the reciever to create its UI representation.
	 * The parent will be a Composite with a GridLayout.
	 * 
	 * @param parent The Composite to wich the Controls should be edited.
	 */
	protected abstract void createPreferencePage(Composite parent);

	/**
	 * Will be called when the UI has to be updated with values of 
	 * a new ConfigModule.
	 * 
	 * @param configModule The currently edited ConfigModule
	 */
	protected abstract void updatePreferencePage(C configModule);

	/**
	 * Will be called to determine whether the given ConfigModule is allowed
	 * to be edited. The default implementation will return false only when
	 * the group of the given module's Config 
	 * is part of set allowOverwrite to
	 * false for the given configModule.
	 * If false is returned {@link #setEditable(boolean)} will be called to 
	 * disable/hide the GUI elements.
	 * This is intended to be overridden for different behaviour.
	 * 
	 * @param configModule
	 * @return Whether the configModule is allowed to be edited.
	 */
	protected boolean canEdit(C configModule) {
		return configModule.isGroupConfigModule() || configModule.isGroupAllowsOverride() || !currentConfigIsGroupMember;
	}

	/**
	 * Should change the GUI to either an editable
	 * or an read-only version of the view of the current ConfigModule.
	 * The default implementation recursively disables/enables 
	 * all Buttons of this preference-page. 
	 * This is intended to be extended for different behaviour on canEdit() == false.
	 */
	protected void setEditable(boolean editable) {
		setEditable(wrapper, editable);
	}


	/**
	 * Private helper, recursively sets enabled of
	 * all Buttons in the preference-page to the given
	 * value of enabled
	 */
	private void setEditable(Control control, boolean editable) {
		if (control instanceof Button)
			control.setEnabled(editable);
		if (control instanceof Combo) 
			control.setEnabled(editable);
		
		if (control instanceof Composite) {
			Control[] children = ((Composite)control).getChildren();
			for (int i = 0; i < children.length; i++) {
				setEditable(children[i], editable);
			}
		}
	}

	/**
	 * Sets the given config module and updates the gui to display it.
	 *  
	 * @param configModule The {@link ConfigModule} to set and display.
	 */
	public void updatePreferencesGUI(C configModule) {
		updatePreferencePage(configModule);
		setEditable(canEdit(configModule));
		if (! currentConfigModule.isGroupConfigModule())
			updateConfigHeader();
		
		if (checkBoxAllowOverwrite != null)
			checkBoxAllowOverwrite.setSelection(configModule.isGroupMembersMayOverride());		
	}
	
	
	/**
	 * Updates the config module. 
	 * 
	 * @param configModule the ConfigModule to be updated.
	 */
	public void updateCurrentCfMod() {
		if (checkBoxAllowOverwrite != null) {
			boolean allowOverwrite = checkBoxAllowOverwrite.getSelection();
			currentConfigModule.setAllowUserOverride(allowOverwrite);			
		}
				
		updateConfigModule(currentConfigModule);
	}
	
	/**
	 * Here you should update the config module with the data from specific UI.
	 * 
	 * @param configModule The config module to be updated.
	 */
	protected abstract void updateConfigModule(C configModule);

	/**
	 * Returns the ConfigModule class this PreferencePage does edit.
	 * 
	 * @return The ConfigModule class this PreferencePage does edit.
	 */
	public abstract Class<C> getConfigModuleClass();

	/**
	 * Should return the cfModID of the ConfigModule this preference page
	 * does edit. This method is intended to be overridden. The default 
	 * implementation returns null.
	 * 
	 * @return null
	 */
	public String getConfigModuleCfModID() {
		return null;
	}

	/**
	 * Returns fetch-groups containing FetchPlan.ALL. Intented to be overridden
	 * by subclasses to detach ConfigModules with custom fetchgroups.
	 *  
	 * @return fetch-groups containing FetchPlan.ALL
	 */
	public String[] getConfigModuleFetchGroups() {
		return CONFIG_MODULE_FETCH_GROUPS;
	}

	/**
	 * 
	 * @return
	 */
	public int getConfigModuleMaxFetchDepth() {
		return NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT;
	}

	/**
	 * Default implementation does nothing. Subclasses (AbstractUser..., AbstractWorkstation...),
	 * set a ConfigID for this PreferencePage. 
	 * This method is called by the PreferencePage-Framework of Eclipse but
	 * not by the Config-Framework of JFire. 
	 *  
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	public boolean okToLeave() {
		return super.okToLeave();
	}

	private boolean recentlySaved = false;
	/**
	 * Calls implementors to {@link #updateConfigModule(ConfigModule)} and
	 * stores the updatedConfig module to the server.
	 * 
	 */
	public void storeConfigModule() {
		ConfigManager configManager = null;
		try {
			configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		if (isConfigChanged()) {
			if (Thread.currentThread() != Display.getDefault().getThread()) {
				Display.getDefault().syncExec( new Runnable() {
					public void run() {
						updateCurrentCfMod();				
					}
				});				
			} else {
				updateCurrentCfMod(); 
			} // if (Thread.currentThread() != Display.getDefault().getThread())
		}
		else 
			return;

		try {
			currentConfigModule = (C) configManager.storeConfigModule(
					getCurrentConfigModule(), true, getConfigModuleFetchGroups(), getConfigModuleMaxFetchDepth());
			recentlySaved = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * When called all widgets created in {@link #createPreferencePage(Composite)}
	 * should be discarded (nulled).
	 * TODO: Remove this method. Why? Still needed, e.g. {@link ReportLayoutConfigPreferencePage} (marius)
	 */
	protected abstract void discardPreferencePageWidgets();

	public void discardWidgets() {
		wrapper = null;
		checkBoxAllowOverwrite = null;
		setControl(null);
		discardPreferencePageWidgets();
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		storeConfigModule();
		return true;
	}

	protected void updateApplyButton() {
		super.updateApplyButton();
	}


	private List<ConfigPreferenceChangedListener> configChangedListeners = 
							new ArrayList<ConfigPreferenceChangedListener>();

	/**
	 * Call this when you modified the entity object.
	 *
	 */
	public void notifyConfigChangedListeners()
	{
		Iterator<ConfigPreferenceChangedListener> i = configChangedListeners.iterator();
		while(i.hasNext())
			i.next().configPreferenceChanged(this);
	}

	/**
	 * Listen for modifications of the entity object
	 * @param listener your listener
	 */
	public void addConfigPreferenceChangedListener(ConfigPreferenceChangedListener listener)
	{
		if(!configChangedListeners.contains(listener))
			configChangedListeners.add(listener);
	}

	/**
	 * Remove a listener
	 * @param listener the listener
	 */
	public void removeDataChangedListener(ConfigPreferenceChangedListener listener)
	{
		if(configChangedListeners.contains(listener))
			configChangedListeners.remove(listener);
	}

	public void dispose() {
		configChangedListeners.clear();
		JDOLifecycleManager.sharedInstance().removeNotificationListener(getConfigModuleClass(), changeListener);
		changeListener = null;
		super.dispose();
	}

	public void setCurrentConfigID(ConfigID currentConfigID, boolean retrieveNewConfigModule) {
		this.currentConfigID = currentConfigID;
		if (retrieveNewConfigModule) 
			setCurrentConfigModule(retrieveConfigModule());
	}
}
