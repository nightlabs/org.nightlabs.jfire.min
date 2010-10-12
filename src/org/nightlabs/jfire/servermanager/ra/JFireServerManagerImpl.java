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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.security.auth.login.LoginException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.nightlabs.config.ConfigException;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.classloader.CLRegistrar;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisationinit.OrganisationInitException;
import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.createorganisation.BusyCreatingOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgressID;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapterException;
import org.nightlabs.jfire.shutdownafterstartup.ShutdownControlHandle;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.util.IOUtil;

/**
 * @author marco schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireServerManagerImpl
	implements Connection, JFireServerManager
{
	private static final Logger logger = Logger.getLogger(JFireServerManagerImpl.class);

	private ManagedConnectionImpl managedConnectionImpl;
	private JFireServerManagerFactoryImpl jfireServerManagerFactoryImpl;

	private boolean closed = false;

	private static Boolean nonTransactionalReadBoolean = null;
	public static boolean isNonTransactionalRead()
	{
		if (nonTransactionalReadBoolean == null) {
			String nonTransactionalReadString = System.getProperty(SYSTEM_PROPERTY_NON_TRANSACTIONAL_READ);
			boolean nonTransactionalRead = Boolean.TRUE.toString().equals(nonTransactionalReadString);
			if (logger.isDebugEnabled())
				logger.debug(SYSTEM_PROPERTY_NON_TRANSACTIONAL_READ + "=" + nonTransactionalReadString);

			if (!nonTransactionalRead)
				logger.info(SYSTEM_PROPERTY_NON_TRANSACTIONAL_READ + " is false! Will use transactions even for solely reading data!");

			nonTransactionalReadBoolean = nonTransactionalRead ? Boolean.TRUE : Boolean.FALSE;
		}
		return nonTransactionalReadBoolean.booleanValue();
	}

	public JFireServerManagerImpl(ManagedConnectionImpl managedConnectionImpl)
	{
		this.managedConnectionImpl = managedConnectionImpl;
	}

	public static File getServerTempDir()
	{
		return IOUtil.getUserTempDir("jfire_server.", null);
	}

	/**
	 * @param managedConnection The managedConnectionImpl to set.
	 */
	protected void setManagedConnection(ManagedConnectionImpl _managedConnectionImpl) {
		this.managedConnectionImpl = _managedConnectionImpl;
	}

	protected void setJFireServerManagerFactory(final JFireServerManagerFactoryImpl _jfireServerManagerFactoryImpl)
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": setJFireManagerFactory(...)");
		this.jfireServerManagerFactoryImpl = _jfireServerManagerFactoryImpl;
	}

	@Override
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

	@Override
	public Interaction createInteraction() throws ResourceException {
		throw new ResourceException("NYI");
	}

	@Override
	public LocalTransaction getLocalTransaction() throws ResourceException {
		throw new ResourceException("NYI");
	}

	@Override
	public ConnectionMetaData getMetaData() throws ResourceException {
		throw new ResourceException("NYI");
	}

	@Override
	public ResultSetInfo getResultSetInfo() throws ResourceException {
		throw new ResourceException("NYI");
	}

	@Override
	public CreateOrganisationProgressID createOrganisationAsync(String organisationID, String organisationCaption, String userID, String password, boolean isServerAdmin)
	throws BusyCreatingOrganisationException
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.createOrganisationAsync(
				organisationID,
				organisationCaption, userID, password, isServerAdmin);
	}

	@Override
	public CreateOrganisationProgress getCreateOrganisationProgress(CreateOrganisationProgressID createOrganisationProgressID)
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.getCreateOrganisationProgress(createOrganisationProgressID);
	}

	@Override
	public void createOrganisationProgress_addCreateOrganisationStatus(
			CreateOrganisationProgressID createOrganisationProgressID, CreateOrganisationStatus createOrganisationStatus)
	{
		assertOpen();
		jfireServerManagerFactoryImpl.createOrganisationProgress_addCreateOrganisationStatus(
				createOrganisationProgressID, createOrganisationStatus);
	}

	@Override
	public void createOrganisation(String organisationID, String organisationCaption, String userID, String password, boolean isServerAdmin) throws OrganisationInitException, CreateOrganisationException
	{
		assertOpen();
		jfireServerManagerFactoryImpl.createOrganisation(
				new CreateOrganisationProgress(organisationID), organisationID,
				organisationCaption, userID, password, isServerAdmin);
	}

	@Override
	public OrganisationCf getOrganisationConfig(String organisationID)
	throws OrganisationNotFoundException
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.getOrganisationConfig(organisationID);
	}

//	public Config getConfig() {
//		assertOpen();
//		return jfireServerManagerFactoryImpl.getConfig();
//	}

	@Override
	public void addServerAdmin(String organisationID, String userID)
			throws OrganisationNotFoundException
	{
		jfireServerManagerFactoryImpl.addServerAdmin(organisationID, userID);
	}

	@Override
	public boolean removeServerAdmin(String organisationID, String userID) throws OrganisationNotFoundException
	{
		return jfireServerManagerFactoryImpl.removeServerAdmin(organisationID, userID);
	}

	@Override
	public boolean isOrganisationCfsEmpty() {
		assertOpen();
		return jfireServerManagerFactoryImpl.isOrganisationCfsEmpty();
	}

	@Override
	public List<OrganisationCf> getOrganisationCfs(boolean sorted)
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.getOrganisationCfs(sorted);
	}

	@Override
	public boolean isNewServerNeedingSetup() {
		assertOpen();
		return jfireServerManagerFactoryImpl.isNewServerNeedingSetup();
	}

	@Override
	public JFireServerConfigModule getJFireServerConfigModule()
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.getJFireServerConfigModule();
	}

	@Override
	public void setJFireServerConfigModule(JFireServerConfigModule cfMod)
	throws ConfigException
	{
		assertOpen();
		jfireServerManagerFactoryImpl.setJFireServerConfigModule(cfMod);
	}

	@Override
	public void j2ee_flushAuthenticationCache() throws J2EEAdapterException
	{
		assertOpen();
		jfireServerManagerFactoryImpl.j2ee_flushAuthenticationCache();
	}

	@Override
	public RoleImportSet roleImport_prepare(String organisationID)
	{
		assertOpen();
		return jfireServerManagerFactoryImpl.roleImport_prepare(organisationID);
	}

	@Override
	public void roleImport_commit(RoleImportSet roleImportSet)
	{
		assertOpen();
		jfireServerManagerFactoryImpl.roleImport_commit(roleImportSet, null);
	}

	@Override
	public CLRegistrar getCLRegistrar()
	{
		assertOpen();
		assertAuthenticated();
		return jfireServerManagerFactoryImpl.getCLRegistrar(principal);
	}

//	@Override
//	public List<ModuleDef> getModules(ModuleType moduleType) throws XMLReadException
//	{
//		assertOpen();
//		return jfireServerManagerFactoryImpl.getModules(moduleType);
//	}
//
//	@Override
//	public void flushModuleCache() {
//		assertOpen();
//		jfireServerManagerFactoryImpl.flushModuleCache();
//	}

	// *** authentication stuff ***
	private JFirePrincipal principal = null;

	public boolean isAuthenticated()
	{
		return principal != null;
	}

	@Override
	public void jfireSecurity_flushCache(UserID userID)
	{
		assertOpen();
		jfireServerManagerFactoryImpl.jfireSecurity_flushCache(userID);
	}

	@Override
	public void jfireSecurity_flushCache()
	{
		assertOpen();
		jfireServerManagerFactoryImpl.jfireSecurity_flushCache();
	}

	protected void setJFirePrincipal(JFirePrincipal jfirePrincipal)
	{
		if (jfirePrincipal == null)
			throw new NullPointerException("JFirePrincipal must not be null!");
		this.principal = jfirePrincipal;
	}

	private static final long waitAfterLoginFailureMSec = 2000;

	public static final String LOGIN_PARAM_ALLOW_EARLY_LOGIN = "allowEarlyLogin";

	@Override
	public JFirePrincipal login(LoginData loginData)
		throws LoginException
	{
		String organisationID = loginData.getOrganisationID();
		String userID = loginData.getUserID();
		String workstationID = loginData.getWorkstationID();
		String password = loginData.getPassword();
		this.principal = null;

		if (logger.isDebugEnabled()) {
			// I think we should NOT log the user's password! If this proves really necessary, then we should only do it in TRACE mode (not DEBUG). Marco.
			logger.debug("login: organisationID=\"" + organisationID + "\" userID=\"" + userID +"\""); // password=\"" + password + "\"");
		}

		if (!User.USER_ID_SYSTEM.equals(userID) && !User.USER_ID_ANONYMOUS.equals(userID)) {
			if (jfireServerManagerFactoryImpl.isShuttingDown())
				throw new LoginException("org.jfire.serverShuttingDown");

		// When the server is not yet up and running, normal users should get an exception telling them that the server is not available.
		// The following code worked already fine, but causes problems for the server-initialisation:
		// We cannot enable this without providing a solution to the ServerInits and OrganisationInits that require to log-in
		// using different users than _System_. For example, JFireDemoSetupMultiOrganisation currently fails to register the
		// organisations in each other because of the following Exception ("org.jfire.serverNotYetUpAndRunning") being thrown.
		// Marco.
		// see: https://www.jfire.org/modules/bugs/view.php?id=693
		//
		// Update (2008-11-06): Whenever organisations login to each other or other authentication is done within the server,
		// InvokeUtil is usually used and passes the parameter LOGIN_PARAM_ALLOW_EARLY_LOGIN set to true.
		// Marco.

			if (!jfireServerManagerFactoryImpl.isUpAndRunning()) {
				String allowEarlyLogin = loginData.getAdditionalParams().get(LOGIN_PARAM_ALLOW_EARLY_LOGIN);
				if (!Boolean.parseBoolean(allowEarlyLogin))
					throw new LoginException("org.jfire.serverNotYetUpAndRunning");
			}
		}

//		String userPK = userID + LoginData.USER_ORGANISATION_SEPARATOR + organisationID;

		boolean userIsOrganisation = userID.startsWith(User.USER_ID_PREFIX_TYPE_ORGANISATION);

		try {
//			Lookup lookup = new Lookup(organisationID);

			boolean isAnonymousUser = Organisation.DEV_ORGANISATION_ID.equals(organisationID) && User.USER_ID_ANONYMOUS.equals(userID);
			boolean isOrganisationUserOnRemoteServer = (
					userIsOrganisation &&
					!jfireServerManagerFactoryImpl.containsOrganisation(organisationID)
			);

			if (isAnonymousUser || isOrganisationUserOnRemoteServer) {
				// no password check required. this user has no rights at all. It basically means the same as not being logged in.
				this.principal = new JFirePrincipal(
						loginData,
						userIsOrganisation,
//						lookup,
						new RoleSet() // RoleSet.class.getName() + '[' + userPK + ']') // no roles!
						);
				return this.principal;
			}


			if (this.isOrganisationCfsEmpty()) {
				if (!UserLocal.isValidPassword(password))
					throw new LoginException("The password \"" + password + "\" is not valid!");

				Organisation.assertValidOrganisationID(organisationID);
				
				RoleSet roleSet = new RoleSet(); // RoleSet.class.getName() + '[' + userPK + ']');
				// add roles needed for setup
				roleSet.addMember(JFireServerManagerFactoryImpl.guestRolePrincipal); // EVERYONE has this role!
				roleSet.addMember(JFireServerManagerFactoryImpl.serverAdminRolePrincipal);

				if (User.USER_ID_SYSTEM.equals(userID)) {
					roleSet.addMember(JFireServerManagerFactoryImpl.systemRolePrincipal);
//					roleSet.addMember(JFireServerManagerFactoryImpl.loginWithoutWorkstationRolePrincipal);
				}

				roleSet.addMember(JFireServerManagerFactoryImpl.loginWithoutWorkstationRolePrincipal);

				// setup mode login, create principal
				this.principal = new JFirePrincipal(
						loginData,
						userIsOrganisation,
//						lookup,
						roleSet
						);

				return this.principal;
			}


			boolean handleTx = false;
			boolean doCommit = false;
			UserTransaction tx = null;
			if (!isNonTransactionalRead()) {
				InitialContext initCtx = new InitialContext();
				tx = jfireServerManagerFactoryImpl.getJ2EEVendorAdapter().getUserTransaction(initCtx);
				handleTx = tx.getStatus() == Status.STATUS_NO_TRANSACTION;
				if (handleTx)
					tx.begin();
			}
			try {
				boolean authenticated = jfireServerManagerFactoryImpl.jfireSecurity_checkTempUserPassword(
						UserID.create(organisationID, userID), password
				);

				// get persistence manager
//				PersistenceManager pm = lookup.getPersistenceManager();
				PersistenceManager pm = new Lookup(organisationID).createPersistenceManager();
				try
				{
					if (User.USER_ID_SYSTEM.equals(userID)) {
						if (!authenticated) {
							logger.info("Login failed because system user of organisation \""+organisationID+"\" either has no temporary password assigned or the given password does not match. This user cannot have a real password and before login, a temporary password must be created.");

							// Pause for a while to prevent users from trying out passwords
							try { Thread.sleep(waitAfterLoginFailureMSec); } catch (InterruptedException e) { }

							throw new LoginException("Invalid username or password!");
						}
					}
					else {
						if (!authenticated) { // temporary password NOT matched

							// Initialize meta data.
							pm.getExtent(UserLocal.class);

							UserLocal userLocal;
							try {
								userLocal = (UserLocal)pm.getObjectById(UserLocalID.create(organisationID, userID, organisationID), true);
							} catch (JDOObjectNotFoundException x) {
								logger.info("Login failed because user \""+userID+"\" is not known in organisation \""+organisationID+"\".");

								// Pause for a while to prevent users from trying out passwords
								try { Thread.sleep(waitAfterLoginFailureMSec); } catch (InterruptedException e) { }

								throw new LoginException("Invalid username or password!");
							}

							if(!userLocal.checkPassword(password))
							{
								logger.info("Login failed because password for user \""+userID + '@' + organisationID +"\" is incorrect.");

								// Pause for a while to prevent users from trying out passwords
								try { Thread.sleep(waitAfterLoginFailureMSec); } catch (InterruptedException e) { }

								throw new LoginException("Invalid username or password!");
							}

							authenticated = true;
						} // if (!authenticated) { // temporary password NOT matched
					} // if (!User.USER_ID_SYSTEM.equals(userID)) {

					RoleSet roleSet = jfireServerManagerFactoryImpl.jfireSecurity_getRoleSet(pm, organisationID, userID);

					if (workstationID != null) {
						if (workstationID.equals(Workstation.WORKSTATION_ID_FALLBACK)) {
							logger.warn("User \""+userID + '@' + organisationID +"\" specified workstationID \"" + workstationID + "\" which is illegal for login! This workstation is used only internally.");
							throw new LoginException("org.jfire.workstationIllegal");
						}

						pm.getExtent(Workstation.class);
						try {
							pm.getObjectById(WorkstationID.create(organisationID, workstationID));
						} catch (JDOObjectNotFoundException x) {
							logger.info("Login failed because workstation \"" + workstationID + "\" specified by user \""+userID + '@' + organisationID +"\" does not exist.");
							throw new LoginException("org.jfire.workstationUnknown");
						}
					}
					else {
						// TODO maybe we should better ensure that it's in the datastore? that would be more transparent.
						// Or even better implement that cross-organisation-communication has automatically not only a user, but also a worksation (same ID as user).
						if (userIsOrganisation)
							roleSet.addMember(JFireServerManagerFactoryImpl.loginWithoutWorkstationRolePrincipal);

						if (!roleSet.isMember(JFireServerManagerFactoryImpl.loginWithoutWorkstationRolePrincipal)) {
							logger.info("Login failed because workstation has not been specified by user \""+userID + '@' + organisationID +"\" but is required (role \"" + JFireServerManagerFactoryImpl.loginWithoutWorkstationRolePrincipal.getName() + "\" is not granted).");
							throw new LoginException("org.jfire.workstationRequired");
						}
					}

					// login succeeded, create principal
					this.principal = new JFirePrincipal(
							loginData,
							userIsOrganisation,
//							lookup,
							roleSet
					);
					if(logger.isDebugEnabled())
						logger.debug("Created JFirePrincipal \""+principal+"\".");
				}
				finally
				{
					// always close persistence manager
					pm.close();
				}

				doCommit = true;
			} finally {
				if (handleTx) {
					if (doCommit)
						tx.commit();
					else
						tx.rollback();
				}
			}

		} catch (LoginException e) {
			throw e;
		} catch(Throwable e) {
			logger.warn("Login failed!", e);
			throw new LoginException(e.getMessage());
		}

		return this.principal;
	}

	@Deprecated
	@Override
	public String jfireSecurity_createTempUserPassword(String organisationID, String userID)
	{
		return jfireServerManagerFactoryImpl.jfireSecurity_createTempUserPassword(organisationID, userID);
	}

	@Override
	public String jfireSecurity_createTempUserPassword(UserID userID)
	{
		return jfireServerManagerFactoryImpl.jfireSecurity_createTempUserPassword(userID);
	}

	@Override
	public boolean configureServerAndShutdownIfNecessary(long delayMSec) throws ServerConfigurationException
	{
		return jfireServerManagerFactoryImpl.configureServerAndShutdownIfNecessary(delayMSec);
	}

	@Override
	public void createDeploymentDescriptor(File deploymentDescriptorFile, File templateFile, Map<String, String> additionalVariables, DeployOverwriteBehaviour deployOverwriteBehaviour)
			throws IOException
	{
		assertOpen();
		assertAuthenticated();

		jfireServerManagerFactoryImpl.createDeploymentDescriptor(
				principal.getOrganisationID(), deploymentDescriptorFile, templateFile, additionalVariables, deployOverwriteBehaviour);
	}

	@Override
	public void createDeploymentJar(File deploymentJar, Collection<DeploymentJarItem> deploymentJarItems, DeployOverwriteBehaviour deployOverwriteBehaviour)
			throws IOException
	{
		assertOpen();
		assertAuthenticated();

		jfireServerManagerFactoryImpl.createDeploymentJar(principal.getOrganisationID(), deploymentJar, deploymentJarItems, deployOverwriteBehaviour);
	}

	@Override
	public void undeploy(File deployment)
			throws IOException
	{
		assertOpen();
		assertAuthenticated();

		jfireServerManagerFactoryImpl.undeploy(deployment);
	}

	@Override
	public ShutdownControlHandle shutdownAfterStartup_createShutdownControlHandle()
	{
		return jfireServerManagerFactoryImpl.shutdownAfterStartup_createShutdownControlHandle();
	}

	@Override
	public void shutdownAfterStartup_shutdown(ShutdownControlHandle shutdownControlHandle) {
		jfireServerManagerFactoryImpl.shutdownAfterStartup_shutdown(shutdownControlHandle);
	}

//	@Override
//	public boolean shutdownAfterStartup_isActive(ShutdownControlHandle shutdownControlHandle) {
//		return jfireServerManagerFactoryImpl.shutdownAfterStartup_isActive(shutdownControlHandle);
//	}
}
