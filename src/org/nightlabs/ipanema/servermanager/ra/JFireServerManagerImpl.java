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
package org.nightlabs.ipanema.servermanager.ra;

import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.security.SimplePrincipal;
import org.nightlabs.ipanema.base.JFirePrincipal;
import org.nightlabs.ipanema.base.Lookup;
import org.nightlabs.ipanema.classloader.CLRegistrar;
import org.nightlabs.ipanema.module.ModuleType;
import org.nightlabs.ipanema.security.RoleSet;
import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.security.UserLocal;
import org.nightlabs.ipanema.security.id.UserLocalID;
import org.nightlabs.ipanema.security.registry.SecurityRegistrar;
import org.nightlabs.ipanema.servermanager.JFireServerManager;
import org.nightlabs.ipanema.servermanager.RoleImportSet;
import org.nightlabs.ipanema.servermanager.config.JFireServerConfigModule;
import org.nightlabs.ipanema.servermanager.config.OrganisationCf;

import org.nightlabs.ModuleException;
import org.nightlabs.config.Config;

/**
 * @author marco
 */
public class JFireServerManagerImpl
	implements Connection, JFireServerManager
{
	public static Logger LOGGER = Logger.getLogger(JFireServerManagerImpl.class);
	
	private ManagedConnectionFactoryImpl managedConnectionFactoryImpl;
	private ManagedConnectionImpl managedConnectionImpl;
	private JFireServerManagerFactoryImpl ipanemaServerManagerFactoryImpl;
	
	private boolean closed = false;

	public JFireServerManagerImpl(
			ManagedConnectionFactoryImpl _managedConnectionFactoryImpl, 
			ManagedConnectionImpl _managedConnectionImpl)
	{
		this.managedConnectionFactoryImpl = _managedConnectionFactoryImpl;
		this.managedConnectionImpl = _managedConnectionImpl;
	}

	/**
	 * @param managedConnection The managedConnectionImpl to set.
	 */
	protected void setManagedConnection(
			ManagedConnectionImpl _managedConnectionImpl) {
		this.managedConnectionImpl = _managedConnectionImpl;
		
		// NO_TODO: get userID and organisationID
		// Because the JFireServerManager is used by the login module, we moved all authentication stuff here
		// and do it manually.
		/*
		String username = this.managedConnectionImpl.getPasswordCredential().getUserName();
		boolean userIsOrganisation = false;
		String tmpStr = username;
		if(tmpStr.startsWith(User.USERID_PREFIX_TYPE_ORGANISATION))
		{
			userIsOrganisation = true;
			tmpStr = tmpStr.substring(User.USERID_PREFIX_TYPE_ORGANISATION.length());
		}
		Pattern p = Pattern.compile("[*@*]");
		String[] txt = p.split(tmpStr);
		if(txt.length != 2)
			throw new LoginException("Invalid user string (use user@organisation)");
		if(txt[0].length() == 0 || txt[1].length() == 0)
			throw new LoginException("Invalid user string (use user@organization)");
		this.userID = userIsOrganisation?User.USERID_PREFIX_TYPE_ORGANISATION+txt[0]:txt[0];
		this.organisationID = txt[1];
		*/
	}
	protected void setJFireServerManagerFactory(final JFireServerManagerFactoryImpl _ipanemaServerManagerFactoryImpl)
	{
		LOGGER.debug(this.getClass().getName()+": setJFireManagerFactory(...)");
		this.ipanemaServerManagerFactoryImpl = _ipanemaServerManagerFactoryImpl;
	}

	/**
	 * @see javax.resource.cci.Connection#close()
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#close()
	 */
	public void close()
	{
		LOGGER.info(this.getClass().getName()+": close()");
		if (managedConnectionImpl != null) 
		{
//			managedConnectionImpl.flushDirty();
			managedConnectionImpl.notifyClosed(this);
		} // if (mc != null)
		closed = true;
		
		setManagedConnection(null);
	}

	protected void assertOpen()
	{
		if (closed)
			throw new IllegalStateException("JFireServerManager already closed!");
	}

	protected void assertAuthenticated()
	{
		if (!isAuthenticated())
			throw new IllegalStateException("You are not authenticated at this JFireServerManager!");
	}

	/**
	 * @see javax.resource.cci.Connection#createInteraction()
	 */
	public Interaction createInteraction() throws ResourceException {
		throw new ResourceException("NYI");
	}

	/**
	 * @see javax.resource.cci.Connection#getLocalTransaction()
	 */
	public LocalTransaction getLocalTransaction() throws ResourceException {
		throw new ResourceException("NYI");
	}

	/**
	 * @see javax.resource.cci.Connection#getMetaData()
	 */
	public ConnectionMetaData getMetaData() throws ResourceException {
		throw new ResourceException("NYI");
	}

	/**
	 * @see javax.resource.cci.Connection#getResultSetInfo()
	 */
	public ResultSetInfo getResultSetInfo() throws ResourceException {
		throw new ResourceException("NYI");
	}

//	/**
//	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#createOrganisation(java.lang.String, java.lang.String)
//	 */
//	public void createOrganisation(String organisationID, String organisationCaption) throws ModuleException {
//		assertOpen();
//		ipanemaServerManagerFactoryImpl.createOrganisation(
//				organisationID, organisationCaption,
//				null, null, false); // isServerAdmin is ignored
//	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#createOrganisation(String, String, String, String, boolean)
	 */
	public void createOrganisation(String organisationID, String organisationCaption, String userID, String password, boolean isServerAdmin) throws ModuleException {
		assertOpen();
		ipanemaServerManagerFactoryImpl.createOrganisation(
				organisationID, organisationCaption,
				userID, password, isServerAdmin);
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#getOrganisationConfig(java.lang.String)
	 */
	public OrganisationCf getOrganisationConfig(String organisationID) throws ModuleException {
		assertOpen();
		return ipanemaServerManagerFactoryImpl.getOrganisationConfig(organisationID);
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#getConfig()
	 */
	public Config getConfig() {
		assertOpen();
		return ipanemaServerManagerFactoryImpl.getConfig();
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#addServerAdmin(java.lang.String, java.lang.String)
	 */
	public void addServerAdmin(String organisationID, String userID)
			throws ModuleException
	{
		ipanemaServerManagerFactoryImpl.addServerAdmin(organisationID, userID);
	}
	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#removeServerAdmin(java.lang.String, java.lang.String)
	 */
	public boolean removeServerAdmin(String organisationID, String userID)
			throws ModuleException
	{
		return ipanemaServerManagerFactoryImpl.removeServerAdmin(organisationID, userID);
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#isOrganisationCfsEmpty()
	 */
	public boolean isOrganisationCfsEmpty() {
		assertOpen();
		return ipanemaServerManagerFactoryImpl.isOrganisationCfsEmpty();
	}

	public List getOrganisationCfs(boolean sorted)
	{
		assertOpen();
		return ipanemaServerManagerFactoryImpl.getOrganisationCfs(sorted);
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#isNewServerNeedingSetup()
	 */
	public boolean isNewServerNeedingSetup() {
		assertOpen();
		return ipanemaServerManagerFactoryImpl.isNewServerNeedingSetup();
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#getJFireServerConfigModule()
	 */
	public JFireServerConfigModule getJFireServerConfigModule()
		throws ModuleException
	{
		assertOpen();
		return ipanemaServerManagerFactoryImpl.getJFireServerConfigModule();
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#setJFireServerConfigModule(org.nightlabs.ipanema.servermanager.config.JFireServerConfigModule)
	 */
	public void setJFireServerConfigModule(JFireServerConfigModule cfMod)
		throws ModuleException 
	{
		assertOpen();
		ipanemaServerManagerFactoryImpl.setJFireServerConfigModule(cfMod);
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#j2ee_flushAuthenticationCache()
	 */
	public void j2ee_flushAuthenticationCache() throws ModuleException
	{
		assertOpen();
		ipanemaServerManagerFactoryImpl.j2ee_flushAuthenticationCache();
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#roleImport_prepare(java.lang.String)
	 */
	public RoleImportSet roleImport_prepare(String organisationID) {
		assertOpen();
		return ipanemaServerManagerFactoryImpl.roleImport_prepare(organisationID);
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#roleImport_commit(org.nightlabs.ipanema.servermanager.RoleImportSet)
	 */
	public void roleImport_commit(RoleImportSet roleImportSet)
		throws ModuleException
	{
		assertOpen();
		ipanemaServerManagerFactoryImpl.roleImport_commit(roleImportSet, null);
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#getJFireSecurityManager(java.lang.String organisationID)
	 */
	public SecurityRegistrar getSecurityRegistrar()
		throws ModuleException
	{
		assertOpen();
		assertAuthenticated();
		return ipanemaServerManagerFactoryImpl.getSecurityRegistrar(principal);
	}
	
	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#getCLRegistrar()
	 */
	public CLRegistrar getCLRegistrar() throws ModuleException
	{
		assertOpen();
		assertAuthenticated();
		return ipanemaServerManagerFactoryImpl.getCLRegistrar(principal);
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#getModules(org.nightlabs.ipanema.module.ModuleType, boolean)
	 */
	public List getModules(ModuleType moduleType)
		throws ModuleException
	{
		assertOpen();
		return ipanemaServerManagerFactoryImpl.getModules(moduleType);
	}
	
	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#flushModuleCache()
	 */
	public void flushModuleCache() {
		assertOpen();
		ipanemaServerManagerFactoryImpl.flushModuleCache();
	}

	// *** authentication stuff ***
	private JFirePrincipal principal = null;
	
	public boolean isAuthenticated()
	{
		return principal != null;
	}

	public void ipanemaSecurity_flushCache(String organisationID, String userID)
	{
		assertOpen();
		ipanemaServerManagerFactoryImpl.ipanemaSecurity_flushCache(organisationID, userID);
	}

	public void ipanemaSecurity_flushCache()
	{
		assertOpen();
		ipanemaServerManagerFactoryImpl.ipanemaSecurity_flushCache();
	}

	public void ipanemaSecurity_flushCache(String userID)
	{
		assertOpen();
		assertAuthenticated();
		ipanemaServerManagerFactoryImpl.ipanemaSecurity_flushCache(this.principal.getOrganisationID(), userID);
	}

	protected void setJFirePrincipal(JFirePrincipal ipanemaPrincipal)
	{
		if (ipanemaPrincipal == null)
			throw new NullPointerException("JFirePrincipal must not be null!");
		this.principal = ipanemaPrincipal;
	}

	public JFirePrincipal login(String organisationID, String userID, String sessionID, String password)
		throws LoginException
	{
		this.principal = null;

		boolean userIsOrganisation = userID.startsWith(User.USERID_PREFIX_TYPE_ORGANISATION);

		try {
			Lookup lookup = new Lookup(organisationID);
			if (this.isOrganisationCfsEmpty()) {
				RoleSet roleSet = new RoleSet();
				// add roles needed for setup
				roleSet.addMember(new SimplePrincipal("_Guest_")); // EVERYONE has this role!
				roleSet.addMember(new SimplePrincipal("_ServerAdmin_"));

				// setup mode login, create principal
				this.principal = new JFirePrincipal(
						userID, organisationID, 
						sessionID,
						userIsOrganisation, 
						lookup,
						roleSet
						);
			}
			else // authorize 
			{
//				boolean doCommit = false;
//				TransactionManager tx = ipanemaServerManagerFactoryImpl.getJ2EEVendorAdapter().getTransactionManager(new InitialContext());
//				boolean handleTx = tx.getStatus() == Status.STATUS_NO_TRANSACTION;
//				if (handleTx)
//					tx.begin();
//				try {

					boolean authenticated = ipanemaServerManagerFactoryImpl.ipanemaSecurity_checkTempUserPassword(organisationID, userID, password);

					// get persistence manager
					PersistenceManager pm = lookup.getPersistenceManager();
					try
					{
						if (User.SYSTEM_USERID.equals(userID)) {
							if (!authenticated) {
								LOGGER.info("Login failed because system user of organisation \""+organisationID+"\" either has no temporary password assigned or the given password does not match. This user cannot have a real password and before login, a temporary password must be created.");
								throw new LoginException("Invalid username or password!");
							}
						}
						else {
							if (!authenticated) { // temporary password NOT matched

								// Initialize meta data.
								pm.getExtent(UserLocal.class);
								
								UserLocal userLocal;
								try {
									userLocal = (UserLocal)pm.getObjectById(UserLocalID.create(organisationID, userID), true);
								} catch (JDOObjectNotFoundException x) {
									LOGGER.info("Login failed because user \""+userID+"\" not known in organisation \""+organisationID+"\".", x);
									throw new LoginException("Invalid username or password!");
								}

								if(!userLocal.checkPassword(password))
								{
									LOGGER.info("Login failed because password for user \""+userID + '@' + organisationID +"\" is incorrect.");
									throw new LoginException("Invalid username or password!");
								}

								authenticated = true;
							} // if (!authenticated) { // temporary password NOT matched
						} // if (!User.SYSTEM_USERID.equals(userID)) {

						RoleSet roleSet = ipanemaServerManagerFactoryImpl.ipanemaSecurity_getRoleSet(organisationID, userID);

						// login succeeded, create principal
						this.principal = new JFirePrincipal(userID, organisationID, sessionID, userIsOrganisation, lookup, roleSet);
						LOGGER.debug("Created JFirePrincipal \""+principal+"\".");
					}
					finally
					{
						// always close persistence manager
						pm.close();
					}

//					doCommit = true;
//				} finally {
//					if (handleTx) {
//						if (doCommit)
//							tx.commit();
//						else
//							tx.rollback();
//					}
//				}

			} // if (this.isOrganisationCfsEmpty()) {

		} catch (LoginException e) {
			throw e;
		} catch(Throwable e) {
			LOGGER.fatal("Login failed!", e);
			throw new LoginException(e.getMessage());
		}

		return this.principal;
	}

	/**
	 * @see org.nightlabs.ipanema.servermanager.JFireServerManager#ipanemaSecurity_createTempUserPassword(java.lang.String, java.lang.String)
	 */
	public String ipanemaSecurity_createTempUserPassword(String organisationID, String userID)
	{
		return ipanemaServerManagerFactoryImpl.ipanemaSecurity_createTempUserPassword(organisationID, userID); 
	}
}
