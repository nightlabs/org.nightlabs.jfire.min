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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassMap;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferencesEditComposite2 
extends XComposite
implements ConfigPreferenceChangedListener
{

	private List<IConfigModuleChangedListener> cfModChangedListeners = new LinkedList<IConfigModuleChangedListener>();
	
	protected ConfigPreferencesTreeComposite treeComposite;
	protected ConfigPreferencesComposite preferencesComposite;
	protected ConfigModule currentConfigModule;
	protected ConfigID currentConfigID;
	protected Set involvedPages = new HashSet();
	protected AbstractConfigModulePreferencePage currentPage;

	protected Map<String, ConfigModule> involvedConfigModules = new HashMap<String, ConfigModule>();

	protected Set<ConfigModule> dirtyConfigModules = new HashSet<ConfigModule>();
	
	private boolean editingConfigGroup = false;

	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 */
	public ConfigPreferencesEditComposite2(Composite parent, int style, boolean setLayoutData) {
		super(parent, style);
		this.getGridLayout().numColumns = 2;
		treeComposite = new ConfigPreferencesTreeComposite(this, SWT.BORDER, false, null);
		GridData treeGD = new GridData(GridData.FILL_BOTH);
//		treeGD.minimumWidth = 150;
//		treeGD.grabExcessHorizontalSpace = true;
		treeComposite.setLayoutData(treeGD);
//		treeComposite.getGridData().grabExcessHorizontalSpace = false;
		treeComposite.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				updateCurrentConfigModule();
				currentPage = getSelectedPrefencePage();
				updatePreferencesComposite();
			}			
		});
//		TightWrapperComposite wrapper = new TightWrapperComposite(this, SWT.NONE, true);
//		DialogMessageArea messageArea = new DialogMessageArea();
//		messageArea.createContents(wrapper);
//		messageArea.showTitle("Test", null);
		preferencesComposite = new ConfigPreferencesComposite(this, SWT.NONE, true);
	}

	private void updatePreferencesGUI() {
		AbstractConfigModulePreferencePage selectedPage = getSelectedPrefencePage();
		if (selectedPage == null) {
			preferencesComposite.setNoEditGUI();
			return;
		}
		selectedPage.setCurrentConfigModule(getCurrentConfigModule(), false);
		if (!involvedPages.contains(selectedPage)) 
		{			
			selectedPage.createContents(preferencesComposite.getWrapper(), true, editingConfigGroup, true);
			selectedPage.addConfigPreferenceChangedListener(this);
			involvedPages.add(selectedPage);
		}
		else {
			selectedPage.updatePreferencesGUI(getCurrentConfigModule());
		}
//		selectedPage.setCurrentConfigID(currentConfigID, true);		

		preferencesComposite.getStackLayout().topControl = selectedPage.getControl();		
		preferencesComposite.getWrapper().layout();
	}

	private void updateCurrentConfigModule() {
		if (currentConfigModule != null && currentPage != null) {
			if (currentConfigModule.getClass().equals(currentPage.getConfigModuleClass())) {				
				currentPage.updateCurrentCfMod();
			}
		}
	}
	
	public void updatePreferencesComposite() {
		fetchCurrentConfigModule();
		if (Thread.currentThread() == Display.getDefault().getThread())
			updatePreferencesGUI();
		else 
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updatePreferencesGUI();
				}
			});
	}

	protected ConfigModule getCurrentConfigModule() {
		return currentConfigModule;
	}

	public ConfigID getCurrentConfigID() {
		return currentConfigID;
	}

	public void setCurrentConfigID(ConfigID currentConfigID) {
		this.currentConfigID = currentConfigID;
		treeComposite.setConfigID(currentConfigID);
		editingConfigGroup = ConfigGroup.class.equals(JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(currentConfigID));

	}

//	public void setConfigGroup(ConfigID configGroupID) {		
//	setCurrentConfigID(configGroupID);
//	}

	protected AbstractConfigModulePreferencePage getSelectedPrefencePage() {
		ConfigPreferenceNode selectedNode = treeComposite.getSelectedPreferenceNode();
		if (selectedNode == null)
			return null;		
		return selectedNode.getPreferencePage();
	}

	private String getCfModKey(AbstractConfigModulePreferencePage page) {
		String cfModKey = page.getConfigModuleClass().getName();
		if (page.getConfigModuleCfModID() != null)
			cfModKey = cfModKey + "_" + page.getConfigModuleCfModID();
		return cfModKey;
	}
	
	/**
	 * Sets currentConfigModule to the ConfigModule assigned to the
	 * Config or ConfigGroup with the id of the given userConfigID.
	 * 
	 * @param userConfigID
	 */
	protected void fetchCurrentConfigModule() {
		AbstractConfigModulePreferencePage selectedPage = getSelectedPrefencePage();
		if (selectedPage == null)
			return;
		ConfigManager configManager = null;
		try {
			configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		if (currentConfigID == null)
			throw new IllegalStateException("Can not fetch ConfigModule, currentConfigID is null");
		
		String cfModKey = getCfModKey(selectedPage);
		
		currentConfigModule = involvedConfigModules.get(cfModKey);
		if (currentConfigModule != null)
			return;
		
		try {
			// TODO: Use a DAO here and Utils.cloneSerializable()			
			currentConfigModule = configManager.getConfigModule(
					(ConfigID)currentConfigID,
					selectedPage.getConfigModuleClass(), 
					selectedPage.getConfigModuleCfModID(), 
					selectedPage.getConfigModuleFetchGroups(),
					selectedPage.getConfigModuleMaxFetchDepth()
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		involvedConfigModules.put(cfModKey, currentConfigModule);
	}

	public void configPreferenceChanged(AbstractConfigModulePreferencePage preferencePage) {
		String cfModKey = getCfModKey(preferencePage);
		ConfigModule cfMod = involvedConfigModules.get(cfModKey);
		if (cfMod != null)
			notifyChangedListeners(cfMod);
		dirtyConfigModules.add(cfMod);
	}

	public void addConfigModuleChangedListener(IConfigModuleChangedListener listener) {
		cfModChangedListeners.add(listener);
	}
	
	public void removeConfigModuleChangedListener(IConfigModuleChangedListener listener) {
		cfModChangedListeners.remove(listener);
	}
	
	protected void notifyChangedListeners(ConfigModule configModule) {
		for (IConfigModuleChangedListener listener : cfModChangedListeners) {
			listener.configModuleChanged(configModule);
		}
	}
	
	public void clear() {
		involvedConfigModules.clear();
		dirtyConfigModules.clear();
		currentConfigModule = null;
	}
	
	public Set<ConfigModule> getDirtyConfigModules() {
		updateCurrentConfigModule();
		return new HashSet<ConfigModule>(dirtyConfigModules);
	}
}
