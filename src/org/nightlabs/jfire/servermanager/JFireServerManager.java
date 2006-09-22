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

import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.classloader.CLRegistrar;
import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.security.registry.SecurityRegistrar;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;

import org.nightlabs.ModuleException;

/**
 * @author marco
 */
public interface JFireServerManager
{

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
	 * This method creates a real organisation on this server. It cannot be converted into
	 * a representative afterwards!
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
	 * @return
	 */
	public boolean isOrganisationCfsEmpty();

	public List getOrganisationCfs(boolean sorted);

	public List getModules(ModuleType moduleType)
		throws ModuleException;

	public void flushModuleCache();

	public void addServerAdmin(String organisationID, String userID)
	throws ModuleException;

	public boolean removeServerAdmin(String organisationID, String userID)
	throws ModuleException;
	
//	public Config getConfig();

	public void close();
	
	public RoleImportSet roleImport_prepare(String organisationID);

	public void roleImport_commit(RoleImportSet roleImportSet)
		throws ModuleException;
	
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

	public SecurityRegistrar getSecurityRegistrar()
		throws ModuleException;

	public CLRegistrar getCLRegistrar()
		throws ModuleException;

	public void jfireSecurity_flushCache(String organisationID, String userID);
	public void jfireSecurity_flushCache(String userID);
	public void jfireSecurity_flushCache();

	public String jfireSecurity_createTempUserPassword(String organisationID, String userID);
	
	public JFirePrincipal login(String _organisationID, String _userID, String _sessionID, String _password)
		throws LoginException;

	public void createDeploymentDescriptor(
			File deploymentDescriptorFile, File templateFile, Map<String, String> additionalVariables, DeployOverwriteBehaviour deployOverwriteBehaviour)
	throws IOException;

	public void createDeploymentJar(File deploymentJar, Collection<DeploymentJarItem> deploymentJarItems, DeployOverwriteBehaviour deployOverwriteBehaviour)
	throws IOException;
}
