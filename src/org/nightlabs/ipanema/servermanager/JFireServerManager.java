/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 07.06.2004
 */
package org.nightlabs.ipanema.servermanager;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.nightlabs.ipanema.base.JFirePrincipal;
import org.nightlabs.ipanema.classloader.CLRegistrar;
import org.nightlabs.ipanema.module.ModuleType;
import org.nightlabs.ipanema.security.registry.SecurityRegistrar;
import org.nightlabs.ipanema.servermanager.config.JFireServerConfigModule;
import org.nightlabs.ipanema.servermanager.config.OrganisationCf;

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
	 * @throws ModuleException
	 */
	public JFireServerConfigModule getJFireServerConfigModule()
		throws ModuleException;

	public void setJFireServerConfigModule(JFireServerConfigModule cfMod)
		throws ModuleException;

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

	public OrganisationCf getOrganisationConfig(String organisationID)
	throws ModuleException;

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
	 * @see ipanemaSecurity_flushCache(String organisationID, String userID);
	 */
	public void j2ee_flushAuthenticationCache()
		throws ModuleException;

	public SecurityRegistrar getSecurityRegistrar()
		throws ModuleException;

	public CLRegistrar getCLRegistrar()
		throws ModuleException;

	public void ipanemaSecurity_flushCache(String organisationID, String userID);
	public void ipanemaSecurity_flushCache(String userID);
	public void ipanemaSecurity_flushCache();

	public String ipanemaSecurity_createTempUserPassword(String organisationID, String userID);
	
	public JFirePrincipal login(String _organisationID, String _userID, String _sessionID, String _password)
		throws LoginException;
}
