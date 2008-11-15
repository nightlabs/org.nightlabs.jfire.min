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

package org.nightlabs.jfire.servermanager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.nightlabs.ModuleException;
import org.nightlabs.config.ConfigException;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.classloader.CLRegistrar;
import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.createorganisation.BusyCreatingOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgressID;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;
import org.nightlabs.jfire.servermanager.xml.ModuleDef;
import org.nightlabs.jfire.shutdownafterstartup.ShutdownControlHandle;

/**
 * @author marco
 */
public interface JFireServerManager
{
	public static final String SYSTEM_PROPERTY_NON_TRANSACTIONAL_READ = JFireServerManager.class.getName() + ".nonTransactionalRead";

	/**
	 * This method checks whether we have a brand new server that needs setup.
	 *
	 * @return Whether or not the server needs initial setup.
	 */
	public boolean isNewServerNeedingSetup();

	/**
	 * @return Returns a clone of the internal config module.
	 */
	public JFireServerConfigModule getJFireServerConfigModule();

	public void setJFireServerConfigModule(JFireServerConfigModule cfMod) throws ConfigException;

//	/**
//	 * This method creates a representative organisation on this server. Note,
//	 * that it cannot be converted into a real organisation afterwards. This
//	 * representative cannot start operation before its masterOrganisation has
//	 * accepted it.
//	 *
//	 * @param organisationID The organisationID of the representative. Must be new and unique in the whole network (means world!).
//	 * @param organisationName A nice name that will be used to display the new representative organisation.
//	 * @throws ModuleException
//	 */
//	public void createOrganisation(String organisationID, String organisationName)
//		throws ModuleException;

	/**
	 * Create a new organisation. In contrast to {@link #createOrganisation(String, String, String, String, boolean)},
	 * this method will return immediately. the progress of the organisation-creation can be tracked using the result
	 * of this method and {@link #getCreateOrganisationProgress(CreateOrganisationProgressID)}
	 */
	public CreateOrganisationProgressID createOrganisationAsync(String organisationID, String organisationCaption, String userID, String password, boolean isServerAdmin)
	throws BusyCreatingOrganisationException;

	/**
	 * Get the progress information for a currently running or during this server session started and already ended
	 * organisation-creation.
	 *
	 * @param createOrganisationProgressID The result of the previously called {@link #createOrganisationAsync(String, String, String, String, boolean)}
	 * @return progress information about the creation of an organisation or <code>null</code>, if there is none for the given <code>createOrganisationProgressID</code>
	 */
	public CreateOrganisationProgress getCreateOrganisationProgress(CreateOrganisationProgressID createOrganisationProgressID);

	public void createOrganisationProgress_addCreateOrganisationStatus(
			CreateOrganisationProgressID createOrganisationProgressID, CreateOrganisationStatus createOrganisationStatus);

	/**
	 * Create a new organisation on this server.
	 * <p>
	 * This method is blocking - i.e. it will not return before the creation is complete. It is therefore
	 * recommended to use {@link #createOrganisationAsync(String, String, String, String, boolean)} when writing
	 * UI.
	 * </p>
	 *
	 * @param organisationID The ID of the new organisation. It must be unique in the whole world.
	 * @param organisationName A nice name that will be used to display the new representative organisation.
	 * @param userID The userID of the first user to create within the new organisation. It will have all necessary permissions to manage users and roles within the new organisation.
	 * @param password The password of the new user.
	 * @param isServerAdmin Whether this user should have global server administration permissions.
	 *
	 * @throws NoServerAdminException thrown if <tt>isServerAdmin == false</tt> and this is the first organisation on this server.
	 * @throws ModuleException if sth. goes wrong.
	 */
	public void createOrganisation(String organisationID, String organisationName, String userID, String password, boolean isServerAdmin)
		throws ModuleException;

	/**
	 * This method configures the server using the currently configured server configurator.
	 * @param delayMSec In case a reboot is necessary, the shutdown will be delayed by this time in milliseconds.
	 * @return Returns whether a reboot was necessary (and thus a shutdown was/will be initiated).
	 * @throws ModuleException In case sth. goes wrong.
	 */
	public boolean configureServerAndShutdownIfNecessary(long delayMSec) throws ModuleException;

	public OrganisationCf getOrganisationConfig(String organisationID) throws OrganisationNotFoundException;

	/**
	 * If a server is completely new, it does not have any organisation registered. Thus,
	 * it cannot have a server admin organisation. Therefore, the LoginModule checks with
	 * this method, wether it should switch to setup mode.
	 *
	 * @return <code>true</code> if no organisation exists on this server; <code>false</code> if there is at least one organisation.
	 */
	public boolean isOrganisationCfsEmpty();

	public List<OrganisationCf> getOrganisationCfs(boolean sorted);

	public List<ModuleDef> getModules(ModuleType moduleType)
		throws ModuleException;

	public void flushModuleCache();

	public void addServerAdmin(String organisationID, String userID)
	throws ModuleException;

	public boolean removeServerAdmin(String organisationID, String userID)
	throws ModuleException;

//	public Config getConfig();

	public void close();

	public RoleImportSet roleImport_prepare(String organisationID);

	public void roleImport_commit(RoleImportSet roleImportSet);

	/**
	 * This method flushes the authentication cache of the j2ee server.
	 * Note, that this may cause problems! You should better avoid calling this method.
	 * Instead, it is a very good idea to configure your server to deactivate
	 * caching of access rights. JFire has an own cache which should be flushed
	 * instead.
	 * <br/><br/>
	 * Probably, this method will soon become deprecated and dropped.
	 *
	 * @throws ModuleException
	 *
	 * @see jfireSecurity_flushCache(String organisationID, String userID);
	 */
	public void j2ee_flushAuthenticationCache()
		throws ModuleException;

	public CLRegistrar getCLRegistrar()
		throws ModuleException;

//	public void jfireSecurity_flushCache(String organisationID, String userID);
	public void jfireSecurity_flushCache(UserID userID);
//	public void jfireSecurity_flushCache(String userID);
	public void jfireSecurity_flushCache();

	public String jfireSecurity_createTempUserPassword(String organisationID, String userID);

	public JFirePrincipal login(LoginData loginData)
		throws LoginException;

	public void undeploy(File deployment)
	throws IOException;

	public void createDeploymentDescriptor(
			File deploymentDescriptorFile, File templateFile, Map<String, String> additionalVariables, DeployOverwriteBehaviour deployOverwriteBehaviour)
	throws IOException;

	public void createDeploymentJar(File deploymentJar, Collection<DeploymentJarItem> deploymentJarItems, DeployOverwriteBehaviour deployOverwriteBehaviour)
	throws IOException;

	/**
	 * Defer the shutdown-after-start by creating a handle for shutting down the server later.
	 * <p>
	 * If the system property
	 * <code>org.nightlabs.jfire.servermanager.JFireServerManagerFactory.shutdownAfterStartup</code>
	 * (see <a href="https://www.jfire.org/modules/phpwiki/index.php/System%20properties%20supported%20by%20the%20JFire%20Server">System properties supported by the JFire Server</a>)
	 * is set to <code>true</code>, the server normally shuts down immediately after the startup process completed
	 * (i.e. after all organisation- and server-inits have been executed).
	 * If your module wants to defer the shutdown (e.g. because certain async-invocations
	 * need to be performed before shutdown), it should declare an organisation-init (or server-init)
	 * and call this method there.
	 * This will prevent the immediate shutdown and your module must then - when it finished whatever it wanted
	 * to - call the {@link #shutdownAfterStartup_shutdown(ShutdownControlHandle)} method.
	 * </p>
	 *
	 * @return a handle for later calling {@link #shutdownAfterStartup_shutdown(ShutdownControlHandle)}.
	 */
	ShutdownControlHandle shutdownAfterStartup_createShutdownControlHandle();

	/**
	 * Shuts down the server after the last handle has signaled ready-to-shutdown. Every handle can be used exactly
	 * once in this method. Calling {@link #shutdownAfterStartup_createShutdownControlHandle()} again after
	 * this method has seen the last handle, there is no deferring effect anymore, because the shutdown is scheduled
	 * immediately when the last handle was passed to this method.
	 *
	 * @param shutdownControlHandle the handle to use for shutdown.
	 */
	void shutdownAfterStartup_shutdown(ShutdownControlHandle shutdownControlHandle);

//	boolean shutdownAfterStartup_isActive(ShutdownControlHandle shutdownControlHandle);
}
