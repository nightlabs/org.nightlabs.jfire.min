/*
 * Created 	on May 31, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.config;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.nightlabs.ModuleException;
import org.nightlabs.base.entitylist.EntityManagementOrdinaryComposite;
import org.nightlabs.jfire.base.config.AbstractConfigModulePreferencePage;
import org.nightlabs.jfire.base.config.ConfigPreferenceChangedListener;
import org.nightlabs.jfire.base.config.ConfigPreferenceNode;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferencesEditComposite 
	extends EntityManagementOrdinaryComposite
	implements ConfigPreferenceChangedListener
{

	protected ConfigPreferencesTreeComposite treeComposite;
	protected ConfigPreferencesComposite preferencesComposite;
	protected ConfigModule currentConfigModule;
	protected ConfigID currentConfigID;
	protected Set involvedPages = new HashSet();

	
	
	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 */
	public ConfigPreferencesEditComposite(Composite parent, int style, boolean setLayoutData) {
		super(parent, style, setLayoutData);
		this.getGridLayout().numColumns = 2;
		treeComposite = new ConfigPreferencesTreeComposite(this, SWT.BORDER, false, null);
		GridData treeGD = new GridData(GridData.FILL_BOTH);
//		treeGD.minimumWidth = 150;
//		treeGD.grabExcessHorizontalSpace = true;
		treeComposite.setLayoutData(treeGD);
//		treeComposite.getGridData().grabExcessHorizontalSpace = false;
		treeComposite.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				updatePreferencesComposite();
			}			
		});
//		TightWrapperComposite wrapper = new TightWrapperComposite(this, SWT.NONE, true);
//		DialogMessageArea messageArea = new DialogMessageArea();
//		messageArea.createContents(wrapper);
//		messageArea.showTitle("Test", null);
		preferencesComposite = new ConfigPreferencesComposite(this, SWT.NONE, true);
	}
	
	private void updatePreferecesGUI() {
		AbstractConfigModulePreferencePage selectedPage = getSelectedPrefencePage();
		if (selectedPage == null) {
			preferencesComposite.setNoEditGUI();
			return;
		}
		selectedPage.setCurrentConfigModule(getCurrentConfigModule());
		if (!involvedPages.contains(selectedPage)) {
		  selectedPage.createContents(preferencesComposite.getWrapper(), true, true, true);
			selectedPage.addConfigPreferenceChangedListener(this);
			involvedPages.add(selectedPage);
		}
		else {
			selectedPage.updatePreferencesGUI(getCurrentConfigModule());
		}
		// TODO: maybe have to trigger display still in selected page
		selectedPage.setCurrentConfigID(currentConfigID, true);		
		
		preferencesComposite.getStackLayout().topControl = selectedPage.getControl();		
		preferencesComposite.getWrapper().layout();
	}
	
	public void updatePreferencesComposite() {
		fetchCurrentConfigModule();
		if (Thread.currentThread() == Display.getDefault().getThread())
			updatePreferecesGUI();
		else 
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updatePreferecesGUI();
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
	}
	
//	public void setConfigGroup(ConfigID configGroupID) {		
//		setCurrentConfigID(configGroupID);
//	}
	
	protected AbstractConfigModulePreferencePage getSelectedPrefencePage() {
		ConfigPreferenceNode selectedNode = treeComposite.getSelectedPreferenceNode();
		if (selectedNode == null)
			return null;		
		return selectedNode.getPreferencePage();
	}
	
	/**
	 * Sets currentConfigModuel to the ConfigModule assigned to the
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
		try {
			currentConfigModule = configManager.getConfigModule(
					(ConfigID)currentConfigID,
					selectedPage.getConfigModuleClass(), 
					selectedPage.getConfigModuleCfModID(), 
					selectedPage.getConfigModuleFetchGroups()
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setEntity(Object entity) {
		// TODO: maybe do something in set entity
	}

	public void save() throws ModuleException, RemoteException {
		AbstractConfigModulePreferencePage selectedPage = getSelectedPrefencePage();
		if (selectedPage == null)
			return;
		selectedPage.storeConfigModule();
	}

	public void configPreferenceChanged(AbstractConfigModulePreferencePage preferencePage) {
		notifyDataChangedListeners();
	}
	
}
