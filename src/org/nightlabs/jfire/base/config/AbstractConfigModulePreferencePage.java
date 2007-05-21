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
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.composite.FadeableComposite;
import org.nightlabs.base.composite.InheritanceToggleButton;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.base.job.Job;
import org.nightlabs.base.notification.NotificationAdapterJob;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.InheritanceManager;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.dao.ConfigModuleDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;
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
	private static final Logger logger = Logger.getLogger(AbstractConfigModulePreferencePage.class);

	/**
	 * This is the default String array as returned by the base implementation of
	 * {@link #getConfigModuleFetchGroups()}. In order to force child classes to override
	 * this method, the default contains solely {@link FetchPlan#DEFAULT} - causing a
	 * {@link JDODetachedFieldAccessException} if required fields are accessed but not detached.
	 */
	private static final String[] CONFIG_MODULE_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT}; 

	/**
	 * the outmost wrapper used to grey out the page while loading.
	 */
	private FadeableComposite fadableWrapper;
	
	/**
	 * The container for the header and the body, which is shown when the ConfigModule is loaded. 
	 */
	private XComposite loadingDone;
	private XComposite body;
	private XComposite header;
	
	/**
	 * Shows a simple label telling the user that the information to display is currently being 
	 * fetched from the server.
	 */
	private XComposite loading;

	/**
	 * This <code>Button</code> is only instantiated, if we're currently editing a group's ConfigModule.
	 * Otherwise, the {@link #inheritMemberConfigModule} will be created instead. 
	 */
	private Button checkBoxAllowOverwrite;

	/**
	 * This <code>InheritanceToggleButton</code> is only instantiated, if we're currently editing
	 * a member's ConfigModule (i.e. <b>not</b> in group context).
	 *
	 * @see #checkBoxAllowOverwrite
	 */
	private InheritanceToggleButton inheritMemberConfigModule;

	/**
	 * The {@link ConfigID} corresponding to the Config, whose {@link ConfigModule} is presented by 
	 * this page.
	 */
	protected ConfigID configID;
	
	/**
	 * The {@link ConfigModule}, which is presented by this page.
	 */
	protected C currentConfigModule;
	
	/**
	 * This {@link ConfigModule} is used as a backup. If the Config is in a group and it may inherit
	 * settings from a group module, then the user may edit the configuration of the {@link #currentConfigModule}
	 * and later decide to inherit again.
	 */
//	private C backupConfigModule;

	/**
	 * Whether the <code>currentConfigModule</code> is in a ConfigGroup and therefore underlies the 
	 * inheritence constrains.
	 */
	protected boolean currentConfigIsGroupMember = false;
	
	/**
	 * Whether the <code>currentConfigModule</code> can be edited by the user. This can be set by 
	 * {@link #canEdit(ConfigModule)}.
	 */
	protected boolean currentConfigModuleIsEditable = false;
	
	/**
	 * Whether the <code>currentConfigModule</code> was modified by the user. It is only saved iff
	 * <code>configChanged == true</code>. 
	 */
	private boolean configChanged = false;
	
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
	 * Checks if the user is allowed to change configuration 
	 * for groups or other users.
	 * 
	 * @return Weather the user is allowed to change other configurations
	 */
	protected boolean isUserConfigSelectionAllowed() {
		return true;
	}

	/**
	 * Returns the config module currently displayed.
	 * 
	 * @return The config module currently displayed.
	 */
	public C getConfigModule() {
		return currentConfigModule;
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
	public void updateGuiWith(C configModule) {
		try {
			if (JDOHelper.getObjectId(configModule.getConfig()) != configID)
				throw new RuntimeException("The given ConfigModule does not belong to the Config set for this ConfigPreferencePage!");			
		} catch (Exception e) {} // if config is not in FetchGroups -> believe given configModule belongs to this config

		currentConfigModule = configModule;
		currentConfigIsGroupMember = checkIfIsGroupMember(configModule);
		currentConfigModuleIsEditable = canEdit(configModule);
		configChanged = false;
		
		updateConfigHeader();
		updatePreferencePage(configModule);
		
		setEditable(currentConfigModuleIsEditable);
	}
	
	/**
	 * Checks and sets the {@link #currentConfigIsGroupMember} flag.
	 */
	private boolean checkIfIsGroupMember(ConfigModule module) {
		ConfigID groupID = ConfigSetupRegistry.sharedInstance().getGroupForConfig(
				ConfigID.create(module.getOrganisationID(), 
						module.getConfigKey(), 
						module.getConfigType()
						));
		return groupID != null;
	}

	/**
	 * Implicit Listener for the cache. This is needed in order to get notified when the 
	 * {@link #currentConfigModule} changed in the database and to reflect this change in the GUI.
	 */
	private NotificationListener changeListener = new NotificationAdapterJob() {

		public void notify(NotificationEvent notificationEvent) {
			Set<DirtyObjectID> dirtyObjectIDs = notificationEvent.getSubjects();
			ConfigModuleID currentModuleID = (ConfigModuleID) JDOHelper.getObjectId(currentConfigModule);
//		 there aren't many open ConfigModulePages showing the same kind of ConfigModule, this loop is 
// 			therefore not as time consuming as one might think. But if the set of DirtyObjectIDs
//			would be capable of efficiently checking whether a given ConfigID is contained inside itself,
//			then this check would be a lot faster.
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
			// This is a workaround for the recently saved module being notified right after saving.
			// TODO this should be no prob' anymore once the Cache uses Versioning - I will soon implement this - too busy right now. Marco.
			if (recentlySaved) {
				recentlySaved = false;
				return;
			}
			
//			ConfigModuleDAO moduleDAO = ConfigModuleDAO.sharedInstance();
//			final C updatedModule = (C) Utils.cloneSerializable(moduleDAO.getConfigModule(currentModuleID, getConfigModuleFetchGroups(), 
//					getConfigModuleMaxFetchDepth(), getProgressMonitorWrapper()));
			final C updatedModule = retrieveConfigModule(getProgressMonitorWrapper());
			

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
			
			/**
			 * FIXME: Do i get notified when the membership to a ConfigGroup ends?
			 * 	if so -> change updateGUIwith(module) accordingly.
			 */
			boolean updatedModuleIsGroupMember = checkIfIsGroupMember(updatedModule);
			final boolean updatedModuleIsEditable = canEdit(updatedModule);
			
			if (updatedModuleIsGroupMember) {
				if (! updatedModuleIsEditable || ! currentConfigModuleIsEditable) { 
					// simply update the view
					Display.getDefault().asyncExec( new Runnable() {
						public void run() {
							if (fadableWrapper.isDisposed())
								return;

							updateGuiWith(updatedModule);
							setEditable(updatedModuleIsEditable);
						}
					});
				} // (! updatedModuleIsEditable || ! currentConfigModuleIsEditable)
				else {
					if (inheritMemberConfigModule.getSelection()) {
						// we are in a group and the memberConfigModule wants to inherit settings
						// -> synchronious update
						Display.getDefault().asyncExec( new Runnable() {
							public void run() {
								if (fadableWrapper.isDisposed())
									return;

								updateGuiWith(updatedModule);
								setEditable(true);
							}
						});							
					} // (inheritMemberConfigModule.getSelection())
					else {
						if (! configChanged) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if (fadableWrapper.isDisposed())
										return;

									updateGuiWith(updatedModule);
								}
							});
						} else {
							// the memberConfigModule does not want to inherit settings -> inform user
							Display.getDefault().asyncExec( new Runnable() {
								public void run() {
									if (fadableWrapper.isDisposed())
										return;

									ChangedConfigModulePagesDialog.addChangedConfigModule(AbstractConfigModulePreferencePage.this, updatedModule);
								}
							});
						}
					} // (! inheritMemberConfigModule.getSelection())
				} // (updatedModuleIsEditable && currentConfigModuleIsEditable)
			} // (updatedModuleIsGroupMember)
//			if (! currentConfigIsGroupMember) {
//				// FIXME: How to show the user that the Config is now a group member?
//			}
//			else {
////				if (! currentConfigModule.getConfig().equals(updatedModule.getConfig()))
//				// FIXME: How to show Group changed?
//			}
		else { // (! updatedModuleIsGroupMember)
			if (! currentConfigModuleIsEditable) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						updateGuiWith(updatedModule);
						setEditable(true);
					}
				});
			}	else {
				if (! configChanged) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (fadableWrapper.isDisposed())
								return;

							updateGuiWith(updatedModule);
							setEditable(true);
						}
					});
				} else {
					Display.getDefault().asyncExec( new Runnable() {
						public void run() {
							if (fadableWrapper.isDisposed())
								return;

							ChangedConfigModulePagesDialog.addChangedConfigModule(AbstractConfigModulePreferencePage.this, updatedModule);
						}
					});
				}
			} // (! currentModuleIsEditable)
		} // (! updatedModuleIsGroupMember)
		
		} // notify(NotificationEvent notificationEvent)		
	}; // ConfigModuleChangeListener

//	/**
//	 * Determines whether config modules are
//	 * cloned by {@link Utils#cloneSerializable(Object)}
//	 * after retrieval. Doing so allows pages to change
//	 * the module directly, without changing the instance
//	 * that might be in the Cache and used in other places
//	 * throughout the application?
//	 * The default implementation returns <code>true</code>.
//	 * 
//	 * @return Whether the config module should be cloned.
//	 */
//	// brauchen wir das? -> no
//	protected boolean doCloneConfigModule() {
//		return true;
//	}

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
		if (! currentConfigModuleIsEditable)
			return;
		
		this.configChanged = configChanged;
		if (inheritMemberConfigModule != null && inheritMemberConfigModule.getSelection())
			inheritMemberConfigModule.setSelection(false);
		
		if (configChanged)
			notifyConfigChangedListeners();
	} 

	/**
	 * doSetControl Whether to call super.setControl() wich is only needed, when inside the Preferences Dialog.
	 */
	private boolean doSetControl = false;

	/**
	 * This might be called from outside in order to create the page, 
	 * when the PreferencePage is not used within the Eclipse Preferences Dialog.
	 * 
	 * @param parent The parent to add the page to.
	 * @param refreshConfigModule Whether to refresh and display the config module 
	 * @param doCreateConfigGroupHeader Whether to create the header showing controls for ConfigModules of {@link ConfigGroup}s
	 * @return The pages Top control.
	 */
	public Control createContents(Composite parent, ConfigID configID) 
	{
		this.doSetControl = true;
		initPage(configID);
		return createContents(parent);
	}

	@Implement
	public Control createContents(Composite parent) {
		fadableWrapper = new FadeableComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		
		// create temporary label for loading time
		StackLayout layout = new StackLayout();
		fadableWrapper.setLayout(layout);
		loading = new XComposite(fadableWrapper, SWT.NONE);
		new Label(loading, SWT.NONE).setText("Loading...");
		layout.topControl = loading;
		fadableWrapper.setFaded(true);
		
		loadingDone = new XComposite(fadableWrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		header = new XComposite(loadingDone, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);
		header.getGridData().grabExcessVerticalSpace = false;
		body = new XComposite(loadingDone, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		Job fetchJob = new Job("Fetch ConfigModule Information") {
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				currentConfigModule = retrieveConfigModule(monitor);
				currentConfigIsGroupMember = checkIfIsGroupMember(currentConfigModule);
				currentConfigModuleIsEditable = canEdit(currentConfigModule);

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setUpGui();
						updateConfigHeader();
						updatePreferencePage(currentConfigModule);						
						setEditable(currentConfigModuleIsEditable);
						fadableWrapper.setFaded(false);
					}
				});
				return Status.OK_STATUS;
			}
		};
		fetchJob.setPriority(Job.SHORT);
		fetchJob.schedule();
	
		if (logger.isDebugEnabled())
			logger.debug("createContents: registering changeListener");
		
		JDOLifecycleManager.sharedInstance().addNotificationListener(getConfigModuleClass(), changeListener);
		fadableWrapper.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (logger.isDebugEnabled())
					logger.debug("widgetDisposed: UNregistering changeListener");

				configChangedListeners.clear();
				JDOLifecycleManager.sharedInstance().removeNotificationListener(getConfigModuleClass(), changeListener);
				changeListener = null;
			}
		});

		if (doSetControl)
			setControl(fadableWrapper);
		return fadableWrapper;
	}
	
	/**
	 * Initialises the main GUI elements: The header and the body of the preference page.
	 * It needs the {@link #currentConfigModule} to be set.
	 */
	private void setUpGui() {
		if (currentConfigModule.isGroupConfigModule())
			createConfigGroupHeader(header);
		else
			createConfigMemberHeader(header);

		createPreferencePage(body);
		StackLayout layout = (StackLayout) fadableWrapper.getLayout();
		layout.topControl = loadingDone;
		fadableWrapper.layout(true, true);
	}

	/**
	 * Fetches and returns the ConfigModule of the Config with {@link ConfigID} == <code>configID</code> from the 
	 * cache with ConfigModuleClass == {@link #getConfigModuleClass()}, cfModID == {@link #getConfigModuleCfModID()},
	 * the FetchGroups returned by {@link #getConfigModuleFetchGroups()} and the FetchDepth returned by 
	 * {@link #getConfigModuleMaxFetchDepth()}.
	 * @param monitor the monitor showing the progress of the operation.
	 * @return the ConfigModule of the Config with ID = <code>configID</code> and the parameter as set
	 * 	by the abstract getters (e.g. {@link #getConfigModuleClass()}).
	 */
	@SuppressWarnings("unchecked")
	protected C retrieveConfigModule(ProgressMonitor monitor) {
		if (configID == null)
			throw new RuntimeException("The configID of the Config for which the ConfigModule should be fetched is not set!");
		
		return Utils.cloneSerializable((C) ConfigModuleDAO.sharedInstance().getConfigModule(
				configID, 
				getConfigModuleClass(),
				getConfigModuleCfModID(),
				getConfigModuleFetchGroups(),
				getConfigModuleMaxFetchDepth(), 
				monitor
				));
	}

	/**
	 * Updates the {@link #header} of the preference page according to the state of 
	 * {@link #currentConfigModule}.
	 */
	public void updateConfigHeader() {
		if (currentConfigModule.isGroupConfigModule()) {
			if (checkBoxAllowOverwrite == null || checkBoxAllowOverwrite.isDisposed())
				return;
			
			checkBoxAllowOverwrite.setSelection(
					(currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).getWritableByChildren()
					& FieldMetaData.WRITABLEBYCHILDREN_YES) != 0); 
			
		} else {
			
			if (inheritMemberConfigModule == null || inheritMemberConfigModule.isDisposed())
				return;
			
			if (! currentConfigIsGroupMember) {
				inheritMemberConfigModule.setEnabled(false);
				inheritMemberConfigModule.setCaption("The config is in no group.");
				return;
			}

			inheritMemberConfigModule.setSelection(
					currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).isValueInherited());

			inheritMemberConfigModule.setEnabled(currentConfigModuleIsEditable); 

			if (! currentConfigModuleIsEditable)
				inheritMemberConfigModule.setCaption(Messages.getString("AbstractConfigModulePreferencePage.GroupDisallowsOverwrite")); //$NON-NLS-1$
			else
				inheritMemberConfigModule.setCaption("Inherit the settings of this config module from its group?");
		}
	}

	/**
	 * Create the header showing controls for {@link ConfigModule}s of {@link ConfigGroup}s.
	 * <p>
	 * Default implementation adds a checkbox for controlling overwriting for the complete module.
	 * 
	 * @param parent The parent to add the header to. It should be empty and have a GridLayout.
	 *
	 * @see #createConfigMemberHeader(Composite)
	 */
	protected void createConfigGroupHeader(Composite parent) {
		checkBoxAllowOverwrite = new Button(parent, SWT.CHECK);
		checkBoxAllowOverwrite.setSelection( 
				(currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).getWritableByChildren() 
						& FieldMetaData.WRITABLEBYCHILDREN_YES) != 0
						);
		checkBoxAllowOverwrite.setText(Messages.getString("AbstractConfigModulePreferencePage.WhetherGroupAllowsConfigOverwrite")); //$NON-NLS-1$
		
		checkBoxAllowOverwrite.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentConfigModule == null || ! currentConfigModule.isGroupConfigModule())
					return;
				
				setConfigChanged(true);
				currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).setWritableByChildren(
						checkBoxAllowOverwrite.getSelection() == true ? FieldMetaData.WRITABLEBYCHILDREN_YES
																													:	FieldMetaData.WRITABLEBYCHILDREN_NO);
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		checkBoxAllowOverwrite.setLayoutData(gd);
	}

	/**
	 * Creates the header for {@link ConfigModule}s of non-{@link ConfigGroup}s.
	 * <p>
	 * The default implementation creates an {@link InheritanceToggleButton} with a apropriate 
	 * caption. <br>
	 * @see #createConfigGroupHeader(Composite)
	 * 
	 * @param parent the Composite in which to place the header controls. It should be empty and have 
	 * 	a GridLayout.
	 */
	protected void createConfigMemberHeader(Composite parent) {
		inheritMemberConfigModule = new InheritanceToggleButton(parent, "");
		inheritMemberConfigModule.setSelection(currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).isValueInherited());
			
		inheritMemberConfigModule.setEnabled(currentConfigModuleIsEditable); 

		if (! currentConfigModuleIsEditable)
			inheritMemberConfigModule.setCaption(Messages.getString("AbstractConfigModulePreferencePage.GroupDisallowsOverwrite")); //$NON-NLS-1$
		else
			inheritMemberConfigModule.setCaption("Inherit the settings of this config module from its group?");

		inheritMemberConfigModule.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentConfigModule == null || !currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).isWritable())
					return;

				boolean selected = inheritMemberConfigModule.getSelection();
				currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).setValueInherited(selected);				
				
				if (selected) {
					fadableWrapper.setFaded(true);
					Job fetchJob = new Job("Fetch ConfigModule Information") {
						@Override
						protected IStatus run(ProgressMonitor monitor) {
							ConfigID groupID = ConfigSetupRegistry.sharedInstance().getGroupForConfig(
									ConfigID.create(currentConfigModule.getOrganisationID(), 
											currentConfigModule.getConfigKey(), 
											currentConfigModule.getConfigType()
									));
							ConfigModule groupModule = ConfigModuleDAO.sharedInstance().getConfigModule(groupID, 
									getConfigModuleClass(), getConfigModuleCfModID(), getConfigModuleFetchGroups(), 
									getConfigModuleMaxFetchDepth(), monitor);

							
							InheritanceManager inheritanceManager = new InheritanceManager();
							inheritanceManager.inheritAllFields(groupModule, currentConfigModule);
							
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									updatePreferencePage(currentConfigModule);						
									fadableWrapper.setFaded(false);
									inheritMemberConfigModule.setSelection(true); // needed, since updatePrefPage may trigger setConfigChanged(true)
								}								
							});							
							return Status.OK_STATUS;
						}
					};
					fetchJob.setPriority(Job.SHORT);
					fetchJob.schedule();
				}	else {
					setConfigChanged(true); // <-- causes the ToggleButton to be resetet.
				}
				inheritMemberConfigModule.setSelection(selected);
			}
		});
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
	 * the {@link ConfigGroup} of the given module's Config disallows the member to overwrite the 
	 * configuration.
	 * 
	 * @param configModule the {@link ConfigModule} to be checked whether it is editable or not. 
	 * @return Whether the <code>configModule</code> is allowed to be edited.
	 */
	protected boolean canEdit(ConfigModule configModule) { 
		return
				configModule.isGroupConfigModule() ||
				configModule.getFieldMetaData(ConfigModule.class.getName()).isWritable()
				|| ! checkIfIsGroupMember(configModule);
	}

	/**
	 * Should change the GUI to either an editable
	 * or an read-only version of the view of the current ConfigModule.
	 * The default implementation recursively disables/enables 
	 * all Buttons of this preference-page. 
	 * This is intended to be extended for different behaviour on canEdit() == false.
	 */
	protected void setEditable(boolean editable) {
//		fadableWrapper.setFaded(! editable);
		fadableWrapper.setEnabled(editable);
	}

	/**
	 * This method is called on the UI-thread! <p>
	 * 
	 * Updates the config module with the values from the GUI together with {@link #updateConfigModule()}.
	 * This Method first updates the {@link FieldMetaData} concerning the inheritance settings of this 
	 * {@link ConfigModule} and then calls {@link #updateConfigModule()} to complete the update.
	 */
	public void updateCurrentConfigModule() {
		if (currentConfigModule.isGroupConfigModule())
			currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).setWritableByChildren(
					checkBoxAllowOverwrite.getSelection() == true ? FieldMetaData.WRITABLEBYCHILDREN_YES
																												:	FieldMetaData.WRITABLEBYCHILDREN_NO);
		else {
			if (currentConfigIsGroupMember && currentConfigModuleIsEditable)
				currentConfigModule.getFieldMetaData(ConfigModule.class.getName()).setValueInherited(
					inheritMemberConfigModule.getSelection());
		}

		updateConfigModule();
	}
	
	/**
	 * Here you should update the config module with the data from specific UI.
	 */
	protected abstract void updateConfigModule();

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
	 * Returns fetch-groups containing FetchPlan.DEFAULT. Intented to be overridden
	 * by subclasses to detach ConfigModules with useful Fetch-Groups, which contain at least 
	 * the corresponding config and the {@link ConfigModule} fieldMetaDataMap! <br>
	 * Since only a flat copy of the corresponding config is enough for most cases 
	 * {@link #getConfigModuleMaxFetchDepth()} returns 1;
	 * 
	 * @return fetch-groups only containing {@link FetchPlan#DEFAULT}
	 */
	public String[] getConfigModuleFetchGroups() {
		return CONFIG_MODULE_FETCH_GROUPS;
	}

	/**
	 * Returns 2, since a flatly detached corresponding config is enough for most cases.
	 * If further information is necessary overwrite this method to return the correct maximal fetch 
	 * depth.
	 *  
	 * @return 2.
	 */
	public int getConfigModuleMaxFetchDepth() {
		return 2;
	}

	/**
	 * Default implementation does nothing. Subclasses (AbstractUser..., AbstractWorkstation...),
	 * set a ConfigID for this PreferencePage. 
	 * This method is called by the PreferencePage-Framework of Eclipse but
	 * not by the Config-Framework of JFire. 
	 * 
	 * If this page shall be embeded in another Context use {@link #initPage(ConfigID)}!
	 *  
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Sets the <code>configID</code>, which is later used to retrieve the correct {@link ConfigModule}.
	 * 
	 *  This is used by the {@link ConfigPreferencesEditComposite2} to set up this page. Whereas 
	 *  {@link #init(IWorkbench)} is used by the Eclipse Preference Page mechanism to initialise 
	 *  this page.
	 *    
	 * @param configID the {@link ConfigID} corresponding to the {@link Config}, whose 
	 * 	{@link ConfigModule} shall be displayed by this page.
	 */
	public void initPage(ConfigID configID) {
		this.configID = configID;
	}

	/**
	 * Calls implementors to {@link #updateConfigModule(ConfigModule)} and
	 * stores the updatedConfig module to the server.
	 * 
	 */
	public void storeConfigModule() {
		if (Thread.currentThread() == Display.getDefault().getThread())
			throw new IllegalStateException("This method must not be called on the GUI-thread! Use a job!");

		if (isConfigChanged()) {
			Display.getDefault().syncExec( new Runnable() {
				public void run() {
					updateCurrentConfigModule();
				}
			});

			storeModule();
		} // if (isConfigCachanged()) 
	}
	
	/**
	 * Is needed to omit a user interaction after the cache notifies this page that a newer {@link ConfigModule}
	 * is available, which is only there since it was just recently saved!
	 * 
	 * TODO: When Marco has implemented versioning in the cache, this can be removed.
	 */
	private boolean recentlySaved = false;

	/**
	 * Stores the <code>currentConfigModule</code> to the datastore.
	 */
	protected void storeModule() {
		ConfigManager configManager = null;
		try {
			configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			C storedConfigModule = (C) configManager.storeConfigModule(
					getConfigModule(), true, getConfigModuleFetchGroups(), getConfigModuleMaxFetchDepth());

			Cache.sharedInstance().put(null, storedConfigModule, getConfigModuleFetchGroups(), getConfigModuleMaxFetchDepth());
			currentConfigModule = Utils.cloneSerializable(storedConfigModule);
			recentlySaved = true;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

//	protected abstract void discardPreferencePageWidgets();

	// FIXME: Warum diese Methode? sie überschrieb nichts und wurde von nichts in JfireMax getriggeret?
	// 	Ebenso discardPreferencePageWidgets() ???
	
//	public void discardWidgets() {
//		body = null;
//		checkBoxAllowOverwrite = null;
//		setControl(null);
//		discardPreferencePageWidgets();
//	}

	public boolean performOk() {
		storeConfigModule();
		return true;
	}

	protected void updateApplyButton() {
		updateCurrentConfigModule();
		storeConfigModule();
	}


	/**
	 * A list of listeners that shall be triggered if this module changes.  
	 * (see {@link #notifyConfigChangedListeners()})
	 */
	private List<ConfigPreferenceChangedListener> configChangedListeners = 
							new ArrayList<ConfigPreferenceChangedListener>();

	/**
	 * Call this when you modified the entity object.
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
}
