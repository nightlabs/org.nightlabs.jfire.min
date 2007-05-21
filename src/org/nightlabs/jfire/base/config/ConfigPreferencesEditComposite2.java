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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.job.Job;
import org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassMap;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.dao.ConfigModuleDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferencesEditComposite2 
extends XComposite
implements ConfigPreferenceChangedListener, IStoreChangedConfigModule
{

	private List<IConfigModuleChangedListener> cfModChangedListeners = new LinkedList<IConfigModuleChangedListener>();
	
	protected ConfigPreferencesTreeComposite treeComposite;
	protected ConfigPreferencesComposite preferencesComposite;
	protected ConfigModule currentConfigModule;
	protected ConfigID currentConfigID;
//	protected Set<AbstractConfigModulePreferencePage> involvedPages = new HashSet<AbstractConfigModulePreferencePage>();
	protected AbstractConfigModulePreferencePage currentPage;

	protected Map<String, ConfigModule> involvedConfigModules = new HashMap<String, ConfigModule>();

	/**
	 * (ConfigModule.class.getSimpleName.toString(), AbstractConfigModulePreferencePage)
	 */
	protected Map<String, AbstractConfigModulePreferencePage> 
			involvedPages = new HashMap<String, AbstractConfigModulePreferencePage>(); 

	protected Set<ConfigModule> dirtyConfigModules = new HashSet<ConfigModule>();
	
	private boolean editingConfigGroup = false;

	/** 
	 * Set of ConfigModules that have been updated on the server side and for which the update 
	 * is not yet reflected by the gui.  
	 */
	private Set<ConfigModule> changedModules;

	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 */
	public ConfigPreferencesEditComposite2(Composite parent, int style, boolean setLayoutData) {
		super(parent, style, LayoutMode.TIGHT_WRAPPER);
		this.getGridLayout().numColumns = 2;
		treeComposite = new ConfigPreferencesTreeComposite(this, SWT.BORDER, false, null);
		GridData treeGD = new GridData(GridData.FILL_BOTH);
		treeComposite.setLayoutData(treeGD);
		treeComposite.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener(){ 
			public void selectionChanged(SelectionChangedEvent event) {
				ConfigPreferenceNode selection = treeComposite.getFirstSelectedElement();
				if (selection == null) {
					currentPage = null;
					return;					
				}
				currentPage = selection.getPreferencePage();
				updateCurrentConfigModule();
				updatePreferencesComposite();
			}			
		});
		preferencesComposite = new ConfigPreferencesComposite(this, SWT.NONE, true);
		
	}

	private void updatePreferencesGUI() {
		AbstractConfigModulePreferencePage selectedPage = getCurrentPage();
		if (selectedPage == null) {
			preferencesComposite.setNoEditGUI();
			return;
		}

		if (!involvedPages.values().contains(selectedPage)) 
		{
			selectedPage.createContents(preferencesComposite.getWrapper(), currentConfigID);
			selectedPage.addConfigPreferenceChangedListener(this);
			involvedPages.put(currentConfigModule.getClass().getSimpleName().toString(), selectedPage);
		}

		preferencesComposite.getStackLayout().topControl = selectedPage.getControl();		
		preferencesComposite.getWrapper().layout();
	}

	private void updateCurrentConfigModule() {
		if (currentConfigModule != null && currentPage != null) {
			if (currentConfigModule.getClass().equals(currentPage.getConfigModuleClass())) {				
				currentPage.updateCurrentConfigModule();
			}
		}
	}
	
	public void updatePreferencesComposite() {
		Job job = new Job("Updating ConfigModule") {
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				fetchCurrentConfigModule(monitor);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						updatePreferencesGUI();
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
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
		editingConfigGroup = ConfigGroup.class.equals(
				JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(currentConfigID) 
				);
	}

	protected AbstractConfigModulePreferencePage getCurrentPage() {
		return currentPage;
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
	protected void fetchCurrentConfigModule(ProgressMonitor monitor) {
		AbstractConfigModulePreferencePage selectedPage = getCurrentPage();
		if (selectedPage == null)
			return;

		if (currentConfigID == null)
			throw new IllegalStateException("Can not fetch ConfigModule, currentConfigID is null");
		
		String cfModKey = getCfModKey(selectedPage);
		
		currentConfigModule = involvedConfigModules.get(cfModKey);
		if (currentConfigModule != null)
			return;
		
		try {
			currentConfigModule = ConfigModuleDAO.sharedInstance().getConfigModule(
				currentConfigID, 
				selectedPage.getConfigModuleClass(), 
				selectedPage.getConfigModuleCfModID(), 
				selectedPage.getConfigModuleFetchGroups(),
				selectedPage.getConfigModuleMaxFetchDepth(),
				monitor
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
			dirtyConfigModules.add(cfMod);
		notifyChangedListeners(cfMod);
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

	public Set<AbstractConfigModulePreferencePage> getInvolvedPages() {
		return new HashSet<AbstractConfigModulePreferencePage>(involvedPages.values());
	}

	/**
	 * Stored the changed ConfigModule to ask the user later about whether to reload these modules or not.  
	 * @see org.nightlabs.jfire.base.config.IStoreChangedConfigModule#addChangedConfigModule(org.nightlabs.jfire.config.ConfigModule)
	 */
	public void addChangedConfigModule(ConfigModule module) {
		if (changedModules == null)
			changedModules = new HashSet<ConfigModule>();
		
		changedModules.add(module);
	}
}
