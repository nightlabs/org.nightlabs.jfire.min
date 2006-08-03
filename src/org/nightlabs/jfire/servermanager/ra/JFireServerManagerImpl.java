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

package org.nightlabs.jfire.servermanager.ra;

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
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.classloader.CLRegistrar;
import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.registry.SecurityRegistrar;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;

import org.nightlabs.ModuleException;
import org.nightlabs.config.Config;

/**
 * @author marco
 */
public class JFireServerManagerImpl
	implements Connection, JFireServerManager
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireServerManagerImpl.class);
	
	private ManagedConnectionFactoryImpl managedConnectionFactoryImpl;
	private ManagedConnectionImpl managedConnectionImpl;
	private JFireServerManagerFactoryImpl jfireServerManagerFactoryImpl;
	
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
	protected void setJFireServerManagerFactory(final JFireServerManagerFactoryImpl _jfireServerManagerFactoryImpl)
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": setJFireManagerFactory(...)");
		this.jfireServerManagerFactoryImpl = _jfireServerManagerFactoryImpl;
	}

	/**
	 * @see javax.resource.cci.Connection#close()
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#close()
	 */
	public void close()
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": close()");
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
//	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#createOrganisation(java.lang.String, java.lang.String)
//	 */
//	public void createOrganisation(String organisationID, String organisationCaption) throws ModuleException {
//		assertOpen();
//		jfireServerManagerFactoryImpl.createOrganisation(
//				organisationID, organisationCaption,
//				null, null, false); // isServerAdmin is ignored
//	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#createOrganisation(String, String, String, String, boolean)
	 */
	public void createOrganisation(String organisationID, String organisationCaption, String userID, String password, boolean isServerAdmin) throws ModuleException {
		assertOpen();
		jfireServerManagerFactoryImpl.createOrganisation(
				organisationID, organisationCaption,
				userID, password, isServerAdmin);
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#getOrganisationConfig(java.lang.String)
	 */
	public OrganisationCf getOrganisationConfig(String organisationID) throws ModuleException {
		assertOpen();
		return jfireServerManagerFactoryImpl.getOrganisationConfig(organisationID);
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#getConfig()
	 */
	public Config getConfig() {
		assertOpen();
		return jfireServerManagerFactoryImpl.getConfig();
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#addServerAdmin(java.lang.String, java.lang.String)
	 */
	public void addServerAdmin(String organisationID, String userID)
			throws ModuleException
	{
		jfireServerManagerFactoryImpl.addServerAdmin(organisationID, userID);
	}
	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#removeServerAdmin(java.lang.String, java.lang.String)
	 */
	public boolean removeServerAdmin(String organisationID, String userID)
			throws ModuleException
	{
		return jfireServerManagerFactoryImpl.removeServerAdmin(organisationID, userID);
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#isOrganisationCfsEmpty()
	 */
	public boolean isOrganisationCfsEmpty() {
		assertOpen();
		return jfireServerManagerFactoryImpl.isOrganisationCfsEmpty();
	}

	public List getOrganisationCfs(boolean sorted)
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.getOrganisationCfs(sorted);
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#isNewServerNeedingSetup()
	 */
	public boolean isNewServerNeedingSetup() {
		assertOpen();
		return jfireServerManagerFactoryImpl.isNewServerNeedingSetup();
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#getJFireServerConfigModule()
	 */
	public JFireServerConfigModule getJFireServerConfigModule()
		throws ModuleException
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.getJFireServerConfigModule();
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#setJFireServerConfigModule(org.nightlabs.jfire.servermanager.config.JFireServerConfigModule)
	 */
	public void setJFireServerConfigModule(JFireServerConfigModule cfMod)
		throws ModuleException 
	{
		assertOpen();
		jfireServerManagerFactoryImpl.setJFireServerConfigModule(cfMod);
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#j2ee_flushAuthenticationCache()
	 */
	public void j2ee_flushAuthenticationCache() throws ModuleException
	{
		assertOpen();
		jfireServerManagerFactoryImpl.j2ee_flushAuthenticationCache();
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#roleImport_prepare(java.lang.String)
	 */
	public RoleImportSet roleImport_prepare(String organisationID) {
		assertOpen();
		return jfireServerManagerFactoryImpl.roleImport_prepare(organisationID);
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#roleImport_commit(org.nightlabs.jfire.servermanager.RoleImportSet)
	 */
	public void roleImport_commit(RoleImportSet roleImportSet)
		throws ModuleException
	{
		assertOpen();
		jfireServerManagerFactoryImpl.roleImport_commit(roleImportSet, null);
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#getJFireSecurityManager(java.lang.String organisationID)
	 */
	public SecurityRegistrar getSecurityRegistrar()
		throws ModuleException
	{
		assertOpen();
		assertAuthenticated();
		return jfireServerManagerFactoryImpl.getSecurityRegistrar(principal);
	}
	
	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#getCLRegistrar()
	 */
	public CLRegistrar getCLRegistrar() throws ModuleException
	{
		assertOpen();
		assertAuthenticated();
		return jfireServerManagerFactoryImpl.getCLRegistrar(principal);
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#getModules(org.nightlabs.jfire.module.ModuleType, boolean)
	 */
	public List getModules(ModuleType moduleType)
		throws ModuleException
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.getModules(moduleType);
	}
	
	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#flushModuleCache()
	 */
	public void flushModuleCache() {
		assertOpen();
		jfireServerManagerFactoryImpl.flushModuleCache();
	}

	// *** authentication stuff ***
	private JFirePrincipal principal = null;
	
	public boolean isAuthenticated()
	{
		return principal != null;
	}

	public void jfireSecurity_flushCache(String organisationID, String userID)
	{
		assertOpen();
		jfireServerManagerFactoryImpl.jfireSecurity_flushCache(organisationID, userID);
	}

	public void jfireSecurity_flushCache()
	{
		assertOpen();
		jfireServerManagerFactoryImpl.jfireSecurity_flushCache();
	}

	public void jfireSecurity_flushCache(String userID)
	{
		assertOpen();
		assertAuthenticated();
		jfireServerManagerFactoryImpl.jfireSecurity_flushCache(this.principal.getOrganisationID(), userID);
	}

	protected void setJFirePrincipal(JFirePrincipal jfirePrincipal)
	{
		if (jfirePrincipal == null)
			throw new NullPointerException("JFirePrincipal must not be null!");
		this.principal = jfirePrincipal;
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
//				TransactionManager tx = jfireServerManagerFactoryImpl.getJ2EEVendorAdapter().getTransactionManager(new InitialContext());
//				boolean handleTx = tx.getStatus() == Status.STATUS_NO_TRANSACTION;
//				if (handleTx)
//					tx.begin();
//				try {

					boolean authenticated = jfireServerManagerFactoryImpl.jfireSecurity_checkTempUserPassword(organisationID, userID, password);

					// get persistence manager
					PersistenceManager pm = lookup.getPersistenceManager();
					try
					{
						if (User.USERID_SYSTEM.equals(userID)) {
							if (!authenticated) {
								logger.info("Login failed because system user of organisation \""+organisationID+"\" either has no temporary password assigned or the given password does not match. This user cannot have a real password and before login, a temporary password must be created.");
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
									logger.info("Login failed because user \""+userID+"\" not known in organisation \""+organisationID+"\".", x);
									throw new LoginException("Invalid username or password!");
								}

								if(!userLocal.checkPassword(password))
								{
									logger.info("Login failed because password for user \""+userID + '@' + organisationID +"\" is incorrect.");
									throw new LoginException("Invalid username or password!");
								}

								authenticated = true;
							} // if (!authenticated) { // temporary password NOT matched
						} // if (!User.USERID_SYSTEM.equals(userID)) {

						RoleSet roleSet = jfireServerManagerFactoryImpl.jfireSecurity_getRoleSet(organisationID, userID);

						// login succeeded, create principal
						this.principal = new JFirePrincipal(userID, organisationID, sessionID, userIsOrganisation, lookup, roleSet);
						if(logger.isDebugEnabled())
							logger.debug("Created JFirePrincipal \""+principal+"\".");
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
			logger.fatal("Login failed!", e);
			throw new LoginException(e.getMessage());
		}

		return this.principal;
	}

	public String jfireSecurity_createTempUserPassword(String organisationID, String userID)
	{
		return jfireServerManagerFactoryImpl.jfireSecurity_createTempUserPassword(organisationID, userID); 
	}

	public boolean configureServerAndShutdownIfNecessary(long delayMSec) throws ModuleException
	{
		return jfireServerManagerFactoryImpl.configureServerAndShutdownIfNecessary(delayMSec);
	}
}
