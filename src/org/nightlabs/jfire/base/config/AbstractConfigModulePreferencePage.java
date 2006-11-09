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

import javax.jdo.FetchPlan;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.id.ConfigID;
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
public abstract class AbstractConfigModulePreferencePage 
extends PreferencePage 
implements IWorkbenchPreferencePage
{

	protected static String[] CONFIG_MODULE_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT, FetchPlan.ALL}; // TODO are you sure you want FetchPlan.ALL?, Yes, as default, individual pages, can overwrite. 

	private XComposite wrapper;
	private Button checkBoxAllowOverwrite;


	protected ConfigID currentConfigID;
	protected ConfigModule currentConfigModule;
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
	protected ConfigModule getCurrentConfigModule() {
		return currentConfigModule;
	}

	/**
	 * Sets the current {@link ConfigModule}.
	 * Note that according to {@link #doCloneConfigModule()}
	 * the module might be cloned before it is used.
	 * 
	 * @param configModule The {@link ConfigModule} to set.
	 */
	protected void setCurrentConfigModule(ConfigModule configModule) {
		if (doCloneConfigModule())
			this.currentConfigModule = Utils.cloneSerializable(configModule);
		else
			this.currentConfigModule = configModule;
	}

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
		this.configChanged = configChanged;
		if (configChanged)
			notifyConfigChangedListeners();
	} 

	/**
	 * Default value is true can only be changed by {@link #createContents(Composite, boolean, boolean, boolean)}
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
	public Control createContents(Composite parent, boolean refreshConfigModule, boolean doCreateConfigGroupHeader, boolean doSetControl) {
		this.refreshConfigModule = refreshConfigModule;
		this.doCreateConfigGroupHeader = doCreateConfigGroupHeader;
		this.doSetControl = doSetControl;
		try {
			return createContents(parent);
		}
		finally {
			refreshConfigModule = true;
			doCreateConfigGroupHeader = false;
			doSetControl = false;
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);

		if (doCreateConfigGroupHeader)
			createConfigGroupHeader(wrapper);

		createPreferencePage(wrapper);

		if (refreshConfigModule) {
			if (getCurrentConfigModule() == null)
				setCurrentConfigModule(retrieveConfigModule());
			updatePreferencesGUI(getCurrentConfigModule());
		}
		if (doSetControl)
			setControl(wrapper);
		return wrapper;
	}

	protected ConfigModule retrieveConfigModule() {
		return ConfigModuleProvider.sharedInstance().getConfigModule(
				currentConfigID,
				getConfigModuleClass(),
				getConfigModuleCfModID(),
				getConfigModuleFetchGroups(),
				getConfigModuleMaxFetchDepth()
		);
	}

	/**
	 * Create the header showing controls for {@link ConfigModule}s of {@link ConfigGroup}s.
	 * <p>
	 * Default implementation adds a checkbox for controlling overwriting for the complete module.
	 * 
	 * @param parent The parent to add the header to.
	 */
	protected void createConfigGroupHeader(Composite parent) {
		checkBoxAllowOverwrite = new Button(wrapper, SWT.CHECK);
		checkBoxAllowOverwrite.setText("Allow this configuration to be overwritten.");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		checkBoxAllowOverwrite.setLayoutData(gd);
		(new Label(wrapper, SWT.SEPARATOR | SWT.HORIZONTAL)).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
	protected abstract void updatePreferencePage(ConfigModule configModule);

	/**
	 * Will be called to determine whether the given ConfigModule is allowed
	 * to be edited. The default implementation will return false only when
	 * the group the given module's Config is part of set allowOverwrite to
	 * false for the given configModule.
	 * If false is returned {@link #makeUneditable()} will be called to 
	 * disable/hide the GUI elements.
	 * This is intended to be overridden for different behaviour.
	 * 
	 * @param configModule
	 * @return Wheter the configModule is allowed to be edited.
	 */
	protected boolean canEdit(ConfigModule configModule) {
		return configModule.isGroupAllowOverwrite();
	}

	/**
	 * Should change the GUI to either an editable
	 * or an read-only version of the view of the current ConfigModule.
	 * The default implementation recursively disables/enables 
	 * all Buttons of this preference-page. 
	 * Is intended to be extended for different behaviour on canEdit() == false.
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
	public void updatePreferencesGUI(ConfigModule configModule) {
		updatePreferencePage(configModule);
		setEditable(canEdit(configModule));
		if (doCreateConfigGroupHeader)
			checkBoxAllowOverwrite.setSelection(configModule.isAllowOverride());		
	}

	/**
	 * Will be called to update the ConfigModule from the UI.
	 * 
	 * @param configModule
	 */
	public abstract void updateConfigModule(ConfigModule configModule);

	/**
	 * Returns the ConfigModule class this PreferencePage does edit.
	 * 
	 * @return The ConfigModule class this PreferencePage does edit.
	 */
	public abstract Class getConfigModuleClass();

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
		if (isConfigChanged())
			updateConfigModule(getCurrentConfigModule());
		boolean allowOverwrite = false;
		if (checkBoxAllowOverwrite != null)
			allowOverwrite = checkBoxAllowOverwrite.getSelection();
		getCurrentConfigModule().setAllowUserOverride(allowOverwrite);

		try {
			currentConfigModule = configManager.storeConfigModule(
					getCurrentConfigModule(), true, getConfigModuleFetchGroups(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * When called all widgets created in {@link #createPreferencePage(Composite)}
	 * should be discarded (nulled).
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


	private List configChangedListeners = new ArrayList();

	/**
	 * Call this when you modified the entity object.
	 *
	 */
	public void notifyConfigChangedListeners()
	{
		Iterator i = configChangedListeners.iterator();
		while(i.hasNext())
			((ConfigPreferenceChangedListener)i.next()).configPreferenceChanged(this);
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
		super.dispose();
	}

	public void setCurrentConfigID(ConfigID currentConfigID, boolean retrieveNewConfigModule) {
		this.currentConfigID = currentConfigID;
		if (retrieveNewConfigModule) 
			setCurrentConfigModule(retrieveConfigModule());
	}
}
