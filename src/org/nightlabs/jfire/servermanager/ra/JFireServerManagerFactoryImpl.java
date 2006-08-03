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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.transaction.TransactionManager;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.base.JFireException;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.JFireServerLocalLoginManager;
import org.nightlabs.jfire.base.PersistenceManagerProvider;
import org.nightlabs.jfire.base.SimplePrincipal;
import org.nightlabs.jfire.classloader.CLRegistrar;
import org.nightlabs.jfire.classloader.CLRegistrarFactory;
import org.nightlabs.jfire.classloader.CLRegistryCfMod;
import org.nightlabs.jfire.datastoreinit.DatastoreInitializer;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jdo.cache.CacheCfMod;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.organisationsync.OrganisationSyncManagerFactory;
import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.RoleRef;
import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserRef;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.UserRefID;
import org.nightlabs.jfire.security.registry.SecurityRegistrar;
import org.nightlabs.jfire.security.registry.SecurityRegistrarFactoryImpl;
import org.nightlabs.jfire.server.LocalServer;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.serverinit.ServerInitializer;
import org.nightlabs.jfire.servermanager.DuplicateOrganisationException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.NoServerAdminException;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.TemplateParseException;
import org.nightlabs.jfire.servermanager.config.CreateOrganisationConfigModule;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.OrganisationConfigModule;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.j2ee.JMSConnectionFactoryLookup;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;
import org.nightlabs.jfire.servermanager.j2ee.ServerStartNotificationListener;
import org.nightlabs.jfire.servermanager.xml.EARApplicationMan;
import org.nightlabs.jfire.servermanager.xml.EJBJarMan;
import org.nightlabs.jfire.servermanager.xml.EJBRoleGroupMan;
import org.nightlabs.jfire.servermanager.xml.ModuleDef;
import org.nightlabs.jfire.servermanager.xml.RoleDef;
import org.nightlabs.jfire.servermanager.xml.RoleGroupDef;
import org.nightlabs.jfire.servermanager.xml.XMLReadException;
import org.nightlabs.util.Utils;
import org.xml.sax.SAXException;

/**
 * @author marco
 */
public class JFireServerManagerFactoryImpl
	implements
		ConnectionFactory,
		JFireServerManagerFactory,
		PersistenceManagerProvider,
		ServerStartNotificationListener
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireServerManagerFactoryImpl.class);
	
	private final ManagedConnectionFactoryImpl mcf;
	private final ConnectionManager cm;
	private Reference ref;

	private volatile boolean upAndRunning = false;

	protected J2eeServerTypeRegistryConfigModule j2eeServerTypeRegistryConfigModule;
	protected J2eeServerTypeRegistryConfigModule.J2eeLocalServer j2eeLocalServerCf;
	
	protected OrganisationConfigModule organisationConfigModule;
	protected CreateOrganisationConfigModule createOrganisationConfigModule;
	protected CacheCfMod cacheCfMod;

	private SecurityRegistrarFactoryImpl smf;
	private CLRegistrarFactory clRegistrarFactory;

	public JFireServerManagerFactoryImpl(final ManagedConnectionFactoryImpl mcf, final ConnectionManager cm)
	throws ResourceException 
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": CONSTRUCTOR");
		this.mcf = mcf;
		this.cm = cm;
		Config config = getConfig();
		boolean saveConfig =
				config.getConfigModule(OrganisationConfigModule.class, false) == null ||
				config.getConfigModule(CreateOrganisationConfigModule.class, false) == null ||
				config.getConfigModule(J2eeServerTypeRegistryConfigModule.class, false) == null ||
				config.getConfigModule(CacheCfMod.class, false) == null;

		try {
			organisationConfigModule = (OrganisationConfigModule)
					config.createConfigModule(OrganisationConfigModule.class);
		} catch (ConfigException e) {
			logger.log(Level.FATAL, "Getting/creating OrganisationConfigModule failed!", e);
			throw new ResourceException(e.getMessage());
		}
		try {
			createOrganisationConfigModule = (CreateOrganisationConfigModule)
					config.createConfigModule(CreateOrganisationConfigModule.class);
		} catch (ConfigException e) {
			logger.log(Level.FATAL, "Getting/creating CreateOrganisationConfigModule failed!", e);
			throw new ResourceException(e.getMessage());
		}
		try {
			j2eeServerTypeRegistryConfigModule = (J2eeServerTypeRegistryConfigModule)
					config.createConfigModule(J2eeServerTypeRegistryConfigModule.class);
		} catch (ConfigException e) {
			logger.log(Level.FATAL, "Getting/creating J2eeServerTypeRegistryConfigModule failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try {
			cacheCfMod = (CacheCfMod) config.createConfigModule(CacheCfMod.class);
		} catch (Exception e) {
			logger.error("Creating CacheCfMod failed!", e);
			throw new ResourceException(e.getMessage());
		}

		if (saveConfig) {
			try {
				config.save(false);
				// shall we really force all modules to be written here?
				// Probably not, after last config bugs are fixed.
				// I think I fixed the bug today ;-) Changed it to false. Marco.
			} catch (ConfigException e) {
				logger.fatal("Saving configuration failed!", e);
				throw new ResourceException(e.getMessage());
			}
		}

		// TODO make the IDGenerator configurable
		System.setProperty(IDGenerator.PROPERTY_KEY_ID_GENERATOR_CLASS, "org.nightlabs.jfire.idgenerator.IDGeneratorServer");
		// org.nightlabs.jfire.id.IDGeneratorServer comes from JFireBaseBean and thus is not known in this module


		String j2eeServerType = null;
		ServerCf localServerCf = mcf.getConfigModule().getLocalServer();
		if (localServerCf != null) {
			j2eeServerType = localServerCf.getJ2eeServerType();
		}
		if (j2eeServerType == null) {
			logger.warn("No configuration existing! Assuming that this is a 'jboss32x'. If you change the server type, you must restart!");
			j2eeServerType = Server.J2EESERVERTYPE_JBOSS32X; // TODO we assume that we're running on a jboss32x, but we should somehow allow the user to change this on the fly.
//			throw new ResourceException("JFireServerConfigModule: localServer.j2eeServerType is null! Check config!");
		}

		j2eeLocalServerCf = null;
		for (Iterator it = j2eeServerTypeRegistryConfigModule.getJ2eeLocalServers().iterator(); it.hasNext(); ) {
			J2eeServerTypeRegistryConfigModule.J2eeLocalServer jls = (J2eeServerTypeRegistryConfigModule.J2eeLocalServer)it.next();
			if (j2eeServerType.equals(jls.getJ2eeServerType())) {
				j2eeLocalServerCf = jls;
				break;
			}
		}
		if (j2eeLocalServerCf == null)
			throw new ResourceException("JFireServerConfigModule: localServer.j2eeServerType: This serverType is not registered in the J2eeServerTypeRegistryConfigModule!");


		this.smf = new SecurityRegistrarFactoryImpl(this);

		try {
			this.clRegistrarFactory = new CLRegistrarFactory(
					(CLRegistryCfMod) this.getConfig().createConfigModule(CLRegistryCfMod.class));
//			clRegistrarFactory.scan(); // TODO this should be done lazy. Only for a test here in constructor!
		} catch (Exception e) {
			logger.error("Creating CLRegistrarFactory failed!", e);
			throw new ResourceException(e.getMessage());
		}

		InitialContext initialContext = null;
		try {
			initialContext = new InitialContext();
		} catch (Exception e) {
			logger.error("Obtaining JNDI InitialContext failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try {
			try {
				initialContext.createSubcontext("java:/jfire");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			try {
				initialContext.createSubcontext("java:/jfire/system");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			try {
				initialContext.createSubcontext("jfire");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			try {
				initialContext.createSubcontext("jfire/system");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			String rootOrganisationID = getJFireServerConfigModule().getRootOrganisation().getOrganisationID();
			try
			{
				initialContext.bind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, rootOrganisationID);
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, rootOrganisationID);
			}
			
			try
			{
				initialContext.bind(JMSConnectionFactoryLookup.QUEUECF_JNDI_LINKNAME, "UIL2ConnectionFactory");
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(JMSConnectionFactoryLookup.QUEUECF_JNDI_LINKNAME, "UIL2ConnectionFactory");
			}
			
			try
			{
				initialContext.bind(JMSConnectionFactoryLookup.TOPICCF_JNDI_LINKNAME, "UIL2ConnectionFactory");
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(JMSConnectionFactoryLookup.TOPICCF_JNDI_LINKNAME, "UIL2ConnectionFactory");
			}
			
		} catch (Exception e) {
			logger.error("Binding some config settings into JNDI failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try {
			SecurityReflector userResolver = getJ2EEVendorAdapter().getUserResolver();
			if (userResolver == null)
				throw new NullPointerException("J2EEVendorAdapter "+getJ2EEVendorAdapter().getClass()+".getUserResolver() returned null!");
			try
			{
				initialContext.bind(SecurityReflector.JNDI_NAME, userResolver);
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(SecurityReflector.JNDI_NAME, userResolver);
			}
		} catch (Exception e) {
			logger.error("Creating SecurityReflector and binding it into JNDI failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try
		{
			JFireServerLocalLoginManager m = new JFireServerLocalLoginManager();
			try
			{			
				initialContext.bind(JFireServerLocalLoginManager.JNDI_NAME, m);		
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(JFireServerLocalLoginManager.JNDI_NAME, m);
			}
		}
		catch (Exception e) {
			logger.error("Creating JFireServerLocalLoginManager and binding it into JNDI failed!", e);
			throw new ResourceException(e.getMessage());
		}

		for (Iterator it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
			OrganisationCf organisation = (OrganisationCf) it.next();
			String organisationID = organisation.getOrganisationID();

			try {
				new CacheManagerFactory(initialContext, organisation, cacheCfMod); // registers itself in JNDI
			} catch (Exception e) {
				logger.error("Creating CacheManagerFactory for organisation \""+organisationID+"\" failed!", e);
				throw new ResourceException(e.getMessage());
			}
		}

		try {
			initialContext.close();
		} catch (Exception e) {
			logger.warn("Closing InitialContext failed!", e);
		}

		try {
			getJ2EEVendorAdapter().registerNotificationListenerServerStarted(this);
		} catch (Exception e) {
			logger.error("Registering NotificationListener (for notification on server start) failed!", e);
//			throw new ResourceException(e.getMessage());
		}

//// Unfortunately this does not work, because the initial context is not yet existent, when
//// this is executed.
//		Thread roleImportThread = new Thread("roleImportThread") {
//			public void run() {
//				try {
//					InitialContext ctx = new InitialContext();
//			    UserTransaction ut = (UserTransaction)ctx.lookup("java:comp/ClientUserTransaction");
//			    boolean doCommit = false;
//			    ut.begin();
//			    try {
//						for (Iterator it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
//							OrganisationCf org = (OrganisationCf)it.next();
//							try {
//								waitForPersistenceManager(org.getPersistenceManagerJNDIName()).close();
//								RoleImportSet roleImportSet = roleImport_prepare(org.getOrganisationID());
//								roleImport_commit(roleImportSet);
//							} catch (Exception x) {
//								LOGGER.warn("Role import failed for organisation \""+org.getOrganisationID()+"\"!", x);
//							}
//						}
//						doCommit = true;
//			    } finally {
//			    	if (doCommit) ut.commit();
//			    	else ut.rollback();
//			    }
//				} catch (Throwable x) {
//					LOGGER.error("roleImportThread.run had exception!", x);
//				}
//			}
//		};
//		roleImportThread.start();
	} // end constructor

	/**
	 * This method configures the server using the currently configured server configurator.
	 * @param delayMSec In case a reboot is necessary, the shutdown will be delayed by this time in milliseconds.
	 * @return Returns whether a reboot was necessary (and thus a shutdown was/will be initiated).
	 * @throws ClassNotFoundException 
	 */
	public boolean configureServerAndShutdownIfNecessary(final long delayMSec)
	throws ModuleException
	{
		try {
	//	 instantiating and calling ServerConfigurator
			String serverConfiguratorClassName = mcf.getConfigModule().getJ2ee().getServerConfigurator();
	
			if (logger.isDebugEnabled())
				logger.debug("Instantiating ServerConfigurator: " + serverConfiguratorClassName);
	
			Class serverConfiguratorClass;
			try {
				serverConfiguratorClass = Class.forName(serverConfiguratorClassName);
			} catch (Throwable x) {
				logger.error("Loading ServerConfigurator class " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
				throw x;
			}
	
			if (!ServerConfigurator.class.isAssignableFrom(serverConfiguratorClass))
				throw new IllegalStateException("ServerConfigurator " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) does not extend class " + ServerConfigurator.class);
	
			ServerConfigurator serverConfigurator;
			try {
				serverConfigurator = (ServerConfigurator) serverConfiguratorClass.newInstance();
				serverConfigurator.setJFireServerManagerFactoryImpl(this);
				serverConfigurator.setJFireServerConfigModule(mcf.getConfigModule());
			} catch (Throwable x) {
				logger.error("Instantiating ServerConfigurator from class " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
				throw x;
			}
	
			try {
				serverConfigurator.configureServer();
			} catch (Throwable x) {
				logger.error("Calling ServerConfigurator.configureServer() with instance of " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
				throw x;
			}
	
			if (serverConfigurator.isRebootRequired()) {
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				logger.warn("The Server Configurator " + serverConfigurator.getClass().getName() + " indicates that the server needs to be rebooted! Hence, I will shutdown the server NOW!");
				logger.warn("If this is an error and prevents your JFire Server from starting up correctly, you must exchange the ServerConfigurator in the config module " + JFireServerConfigModule.class.getName());
				
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				Thread thread = new Thread() {
					@Override
					public void run()
					{
						if (delayMSec > 0)
							try { Thread.sleep(delayMSec); } catch (InterruptedException ignore) { }

						try {
							getJ2EEVendorAdapter().shutdown();
							logger.warn("*** SHUTDOWN initiated ***");
						} catch (Throwable e) {
							logger.error("Shutting down server failed!", e);
						}
					}
				};
				thread.setDaemon(false);
				thread.start();
				return true;
			}
			return false;
		} catch (Throwable x) {
			throw new ModuleException(x);
		}
	}

	public void serverStarted()
	{
		logger.info("Caught SERVER STARTED event!");

		try {

			InitialContext ctx = new InitialContext();
			try {				
				if (configureServerAndShutdownIfNecessary(0))
					return;

				// DatastoreInitialization
				DatastoreInitializer datastoreInitializer = new DatastoreInitializer(this, mcf, getJ2EEVendorAdapter());

				
				for (Iterator it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
					OrganisationCf org = (OrganisationCf)it.next();
					String organisationID = org.getOrganisationID();
					
					logger.info("Importing roles and rolegroups into organisation \""+organisationID+"\"...");
					try {

						TransactionManager transactionManager = getJ2EEVendorAdapter().getTransactionManager(ctx);
						boolean doCommit = false;
						transactionManager.begin();
				    try {

				    	RoleImportSet roleImportSet = roleImport_prepare(organisationID);
							roleImport_commit(roleImportSet, null);

							doCommit = true;
				    } finally {
				    	if (doCommit)
				    		transactionManager.commit();
				    	else
				    		transactionManager.rollback();
				    }
						logger.info("Import of roles and rolegroups into organisation \""+organisationID+"\" done.");
					} catch (Exception x) {
						logger.error("Role import into organisation \""+organisationID+"\" failed!", x);
					}


					// register the cache's JDO-listeners in the PersistenceManagerFactory
					CacheManagerFactory cmf = CacheManagerFactory.getCacheManagerFactory(ctx, organisationID);
					PersistenceManagerFactory pmf = getPersistenceManagerFactory(organisationID);
					cmf.setupJdoCacheBridge(pmf);

					try {
						new OrganisationSyncManagerFactory(
								ctx, organisationID,
								getJ2EEVendorAdapter().getTransactionManager(ctx), pmf); // registers itself in JNDI
					} catch (Exception e) {
						logger.error("Creating OrganisationSyncManagerFactory for organisation \""+organisationID+"\" failed!", e);
						throw new ResourceException(e.getMessage());
					}


					logger.info("Initializing datastore of organisation \""+organisationID+"\"...");
					try {
						datastoreInitializer.initializeDatastore(
								this, mcf.getConfigModule().getLocalServer(), organisationID,
								jfireSecurity_createTempUserPassword(organisationID, User.USERID_SYSTEM));

						logger.info("Datastore initialization of organisation \""+organisationID+"\" done.");
					} catch (Exception x) {
						logger.error("Datastore initialization of organisation \""+organisationID+"\" failed!", x);
					}
				}

				// Server Initialization
				new ServerInitializer(this, mcf, getJ2EEVendorAdapter()).initializeServer(ctx);
			} finally {
				ctx.close();
			}

		} catch (Throwable x) {
			logger.fatal("Problem in serverStarted()!", x);
		}

		logger.info("*** JFireServer is up and running! ***");
		upAndRunning = true;
	}

	// **************************************
	// *** Methods from ConnectionFactory ***
	// **************************************
	/**
	 * @see javax.resource.cci.ConnectionFactory#getConnection()
	 */
	public Connection getConnection() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getConnection()");
		JFireServerManagerImpl ismi = (JFireServerManagerImpl)cm.allocateConnection(mcf, null);
		ismi.setJFireServerManagerFactory(this);
		return ismi;
	}

	/**
	 * @see javax.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
	 */
	public Connection getConnection(ConnectionSpec cs) throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getConnection(ConnectionSpec cs): cs = "+cs);
		return getConnection();
	}

	/**
	 * @see javax.resource.cci.ConnectionFactory#getRecordFactory()
	 */
	public RecordFactory getRecordFactory() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getRecordFactory()");
		return null;
	}

	/**
	 * @see javax.resource.cci.ConnectionFactory#getMetaData()
	 */
	public ResourceAdapterMetaData getMetaData() throws ResourceException {
		throw new ResourceException("NYI");
	}

	/**
	 * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
	 */
	public void setReference(Reference _ref) {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": setReference(Reference ref): ref = "+_ref);
		this.ref = _ref;
	}

	/**
	 * @see javax.naming.Referenceable#getReference()
	 */
	public Reference getReference() throws NamingException {
		return ref;
	}


	// ************************************************
	// *** Methods from JFireServerManagerFactory ***
	// ************************************************

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManagerFactory#getJFireServerManager()
	 */
	public JFireServerManager getJFireServerManager()
		throws ModuleException
	{
		try {
			return (JFireServerManager)getConnection();
		} catch (ResourceException e) {
			throw new ModuleException(e);
		}
	}
	
	public JFireServerManager getJFireServerManager(JFirePrincipal jfirePrincipal)
	throws ModuleException
	{
		try {
			JFireServerManager ism = (JFireServerManager)getConnection();
			if (jfirePrincipal != null)
				((JFireServerManagerImpl)ism).setJFirePrincipal(jfirePrincipal);
			return ism;
		} catch (ResourceException e) {
			throw new ModuleException(e);
		}
	}
	
	// ************************************************
	// *** Methods executed by JFireServerManager ***
	// ************************************************ 
	
	protected boolean isNewServerNeedingSetup()
	{
		return mcf.getConfigModule().getLocalServer() == null;
	}

	/**
	 * @return Returns a clone of the internal config module.
	 * @throws ModuleException
	 */
	protected JFireServerConfigModule getJFireServerConfigModule()
		throws ModuleException
	{
		JFireServerConfigModule cfMod = mcf.getConfigModule();
		cfMod.acquireReadLock();
		try {
			return (JFireServerConfigModule)cfMod.clone();
		} finally {
			cfMod.releaseLock();
		}
	}

	protected void setJFireServerConfigModule(JFireServerConfigModule cfMod)
		throws ModuleException
	{
		if (cfMod.getLocalServer() == null)
			throw new NullPointerException("localServer of config module must not be null!");

		if (cfMod.getDatabase() == null)
			throw new NullPointerException("database of config module must not be null!");

		if (cfMod.getJdo() == null)
			throw new NullPointerException("jdo of config module must not be null!");

		try {
			mcf.testConfiguration(cfMod);
		} catch (ConfigException e) {
			throw new ModuleException(e);
		}

		JFireServerConfigModule orgCfMod = mcf.getConfigModule();
		orgCfMod.acquireWriteLock();
		try {
			if (orgCfMod.getLocalServer() != null) {
				if (cfMod.getLocalServer().getServerID() == null)
					cfMod.getLocalServer().setServerID(orgCfMod.getLocalServer().getServerID());
				else if (!orgCfMod.getLocalServer().getServerID().equals(cfMod.getLocalServer().getServerID()))
					throw new IllegalArgumentException("Cannot change serverID after it has been set once!");
			}
			else
				if (cfMod.getLocalServer().getServerID() == null)
					throw new NullPointerException("localServer.serverID must not be null at first call to this method!");
			
			try {
				BeanUtils.copyProperties(orgCfMod, cfMod);
			} catch (Exception e) {
				throw new ModuleException(e);
			}

		} finally {
			orgCfMod.releaseLock();
		}

		try {
			getConfig().save(true); // TODO force all modules to be written???
		} catch (ConfigException e) {
			throw new ModuleException(e);
		}
	}

	protected J2EEAdapter j2eeVendorAdapter = null;
	protected synchronized J2EEAdapter getJ2EEVendorAdapter()
		throws ModuleException
	{
		if (j2eeVendorAdapter == null) {
			try {
				String j2eeVendorAdapterClassName = j2eeLocalServerCf.getJ2eeVendorAdapterClassName(); // mcf.getConfigModule().getJ2ee().getJ2eeVendorAdapterClassName();
				Class j2eeVendorAdapterClass = Class.forName(j2eeVendorAdapterClassName);
				j2eeVendorAdapter = (J2EEAdapter)j2eeVendorAdapterClass.newInstance();
			} catch (Exception e) {
				throw new ModuleException(e);
			}
		}
		return j2eeVendorAdapter;
	}

	protected synchronized void j2ee_flushAuthenticationCache()
		throws ModuleException
	{
		try {
			getJ2EEVendorAdapter().flushAuthenticationCache();
		} catch (ModuleException e) {
			throw e;
		} catch (Exception e) {
			throw new ModuleException(e);
		}
	}

	protected RoleImportSet roleImport_prepare(String organisationID)
	{
		File startDir = new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());

		EJBRoleGroupMan globalEJBRoleGroupMan = new EJBRoleGroupMan();
		Map exceptions = new HashMap(); // key: File jar; value: Throwable exception
		roleImport_prepare_collect(startDir, globalEJBRoleGroupMan, exceptions);

		return new RoleImportSet(organisationID, globalEJBRoleGroupMan, exceptions);
	}
	
	private static class FileFilterDirectories implements FilenameFilter
	{
		/**
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File dir, String name)
		{
			File f = new File(dir, name);
			return f.isDirectory();
		}
	}
	private static FileFilterDirectories fileFilterDirectories = null;

	private static class FileFilterJARs implements FilenameFilter
	{
		/**
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File dir, String name)
		{
			return name.endsWith(JAR_SUFFIX);
		}
	}
	private static String JAR_SUFFIX = ".jar";
	private static FileFilterJARs fileFilterJARs = null;
	
	private void roleImport_prepare_collect(File directory, EJBRoleGroupMan globalEJBRoleGroupMan, Map exceptions)
	{
		if (fileFilterDirectories == null)
			fileFilterDirectories = new FileFilterDirectories();
		String[] directories = directory.list(fileFilterDirectories);
		if (directories != null) {
			for (int i = 0; i < directories.length; ++i)
				roleImport_prepare_collect(new File(directory, directories[i]), globalEJBRoleGroupMan, exceptions);
		} // if (directories != null) {

		if (fileFilterJARs == null)
			fileFilterJARs = new FileFilterJARs();
		String[] jars = directory.list(fileFilterJARs);
		
		if (jars != null) {
			for (int i = 0; i < jars.length; ++i) {
				File jar = new File(directory, jars[i]);
				try {
					JarFile jf = new JarFile(jar, true);
					try {
						roleImport_prepare_readJar(globalEJBRoleGroupMan, jar, jf);
					} finally {
						jf.close();
					}
				} catch (Exception x) {
					String jarFileName;
					try {
						jarFileName = jar.getCanonicalPath();
						logger.warn("Processing Jar \""+jarFileName+"\" failed!", x);
					} catch (IOException e) {
						jarFileName = jar.getPath();
						logger.warn("Processing Jar \""+jarFileName+"\" failed!", x);
						logger.warn("Getting canonical path for \""+jarFileName+"\" failed!", e);
					}
					exceptions.put(jarFileName, x);
				}
			}
		} // if (jars != null) {
	}
	
	private void roleImport_prepare_readJar(EJBRoleGroupMan globalEJBRoleGroupMan, File jar, JarFile jf)
		throws SAXException, IOException, XMLReadException
	{
		JarEntry ejbJarXML = jf.getJarEntry("META-INF/ejb-jar.xml");
		EJBJarMan ejbJarMan;
		if (ejbJarXML == null) {
			logger.warn("Jar \""+jar.getCanonicalPath()+"\" does not contain \"META-INF/ejb-jar.xml\"!");
			ejbJarMan = new EJBJarMan(jar.getName());
		}
		else {
			logger.info("*****************************************************************");
			logger.info("Jar \""+jar.getCanonicalPath()+"\": ejb-jar.xml:");
			InputStream in = jf.getInputStream(ejbJarXML);
			try {
				ejbJarMan = new EJBJarMan(jar.getName(), in);
				for (Iterator it = ejbJarMan.getRoles().iterator(); it.hasNext(); ) {
					RoleDef roleDef = (RoleDef)it.next();
					logger.info("roleDef.roleID = "+roleDef.getRoleID());
				}
			} finally {
				in.close();
			}
			logger.info("*****************************************************************");
		}

		JarEntry roleGroupXML = jf.getJarEntry("META-INF/ejb-rolegroup.xml");
		EJBRoleGroupMan ejbRoleGroupMan;
		if (roleGroupXML == null) {
			logger.warn("Jar \""+jar.getCanonicalPath()+"\" does not contain \"META-INF/ejb-rolegroup.xml\"!");
			ejbRoleGroupMan = new EJBRoleGroupMan(ejbJarMan);
		}
		else {
			logger.info("*****************************************************************");
			logger.info("Jar \""+jar.getCanonicalPath()+"\": ejb-rolegroup.xml:");
			InputStream in = jf.getInputStream(roleGroupXML);
			try {
				ejbRoleGroupMan = new EJBRoleGroupMan(ejbJarMan, in);
				for (Iterator it = ejbRoleGroupMan.getRoleGroups().iterator(); it.hasNext(); ) {
					RoleGroupDef roleGroupDef = (RoleGroupDef)it.next();
					logger.info("roleGroupDef.roleGroupID = "+roleGroupDef.getRoleGroupID());
					for (Iterator itRoles = roleGroupDef.getAllRoles().iterator(); itRoles.hasNext(); ) {
						RoleDef roleDef = (RoleDef)itRoles.next();
						logger.info("  roleDef.roleID = "+roleDef.getRoleID());
					}
				}
			} finally {
				in.close();
			}
			logger.info("*****************************************************************");
		}
		ejbRoleGroupMan.createBackupDefaultRoleGroup();
		globalEJBRoleGroupMan.mergeRoleGroupMan(ejbRoleGroupMan);
	}

	/**
	 * @param roleImportSet
	 * @param pm can be <tt>null</tt>. If <tt>null</tt>, it will be obtained according to <tt>roleImportSet.getOrganisationID()</tt>.
	 * @throws ModuleException
	 */
	protected synchronized void roleImport_commit(RoleImportSet roleImportSet, PersistenceManager pm)
		throws ModuleException
	{
		if (roleImportSet.getOrganisationID() == null)
			throw new IllegalArgumentException("roleImportSet.organisationID is null! Use roleImport_prepare(...) to generate a roleImportSet!");
		EJBRoleGroupMan roleGroupMan = roleImportSet.getEjbRoleGroupMan();
		
		if (!roleImportSet.getJarExceptions().isEmpty())
			logger.warn("roleImportSet.jarExceptions is not empty! You should execute roleImportSet.clearJarExceptions()!", new ModuleException("roleImportSet.jarExceptions is not empty."));

		boolean localPM = pm == null;

		if (localPM)
			pm = getPersistenceManager(roleImportSet.getOrganisationID());
		try {
			if (!localPM) {
				// check whether PM datastore matches organisationID
				String datastoreOrgaID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
				if (!datastoreOrgaID.equals(roleImportSet.getOrganisationID()))
					throw new IllegalArgumentException("Parameter pm does not match organisationID of given roleImportSet!");
			}

			pm.getExtent(AuthorityType.class);
			AuthorityType authorityType = (AuthorityType) pm.getObjectById(AuthorityTypeID.create(roleImportSet.getOrganisationID(), AuthorityType.AUTHORITY_TYPE_ID_SYSTEM));

			pm.getExtent(RoleGroup.class, true);

			for (Iterator itRoleGroups = roleGroupMan.getRoleGroups().iterator(); itRoleGroups.hasNext(); ) {
				RoleGroupDef roleGroupDef = (RoleGroupDef)itRoleGroups.next();
				RoleGroup roleGroupJDO = roleGroupDef.createRoleGroup(pm);
				authorityType.addRoleGroup(roleGroupJDO);
			} // for (Iterator itRoleGroups = roleGroupMan.getRoleGroups().iterator(); itRoleGroups.hasNext(); ) {

		} finally {
			if (localPM)
				pm.close();
		}
	}

	/**
	 * This method either creates an organisation.
	 * @param organisationID The ID of the new organsitation, which must not be <code>null</code>. Example: "RioDeJaneiro.NightLabs.org"
	 * @param organisationName The "human" name of the organisation. Example: "NightLabs GmbH, Rio de Janeiro"
	 * @param userID The userID of the first user to be created. This will be the new organisation's administrator.
	 * @param password The password of the organisation's first user.
	 * @param isServerAdmin Whether the organisation's admin will have server-administrator privileges. This must be <tt>true</tt> if you create the first organisation on a server.
	 */
	protected void createOrganisation(
			String organisationID, String organisationName,
			String userID, String password, boolean isServerAdmin)
//			String masterOrganisationID
//			) 
		throws ModuleException
	{
		synchronized (this) {
			
			try {
			
				// check the parameters
				if (organisationID == null)
					throw new IllegalArgumentException("organisationID must not be null!");

				if ("".equals(organisationID))
					throw new IllegalArgumentException("organisationID must not be an empty string!");

				if (organisationID.indexOf('.') < 0)
					throw new IllegalArgumentException("organisationID is invalid! Must have domain-style form (e.g. \"de.nightlabs.fr\")!");

				if (!ObjectIDUtil.isValidIDString(organisationID))
					throw new IllegalArgumentException("organisationID is not a valid ID! Make sure it does not contain special characters. It should have a domain-style form!");

				if (organisationID.length() > 50)
					throw new IllegalArgumentException("organisationID has "+organisationID.length()+" chars and is too long! Maximum is 50 characters.");
// TODO Though the database definition currently allows 100 chars, we'll probably have to reduce it to 50 because of
// primary key constraints (max 1024 bytes with InnoDB) and the fact that MySQL uses 3 bytes per char when activating
// UTF8!

				if (organisationName == null)
					throw new IllegalArgumentException("organisationName must not be null!");

				if ("".equals(organisationName))
					throw new IllegalArgumentException("organisationName must not be an empty string!");
		
				if (userID == null)
					throw new IllegalArgumentException("userID must not be null!");

				if ("".equals(userID))
					throw new IllegalArgumentException("userID must not be an empty string!");

				if (!ObjectIDUtil.isValidIDString(userID))
					throw new IllegalArgumentException("userID is not a valid ID! Make sure it does not contain special characters!");

				if (userID.length() > 50)
					throw new IllegalArgumentException("userID has "+userID.length()+" chars and is too long! Maximum is 50 characters.");

				if (password == null)
					throw new IllegalArgumentException("userID is not null, thus password must NOT be null either to create a real (non-representative) organisation.");
	
				if ("".equals(password))
					throw new IllegalArgumentException("password must not be an empty string!");

				if (isNewServerNeedingSetup())
					throw new IllegalStateException("This server is not yet set up! Please complete the basic setup before creating organisations!");

				if (!isServerAdmin && organisationConfigModule.getOrganisations().isEmpty())
					throw new NoServerAdminException(
							"You create the first organisation, hence 'isServerAdmin' must be true! " +
							"Otherwise, you would end up locked out, without any possibility to " +
							"create another organisation or change the server-configuration.");

				// Check whether another organisation with the same name already exists.
				// Unfortunately, there is currently no possibility to check in the whole
				// network, thus we check only locally.
				if (getOrganisationCfsCloned().get(organisationID) != null)
					throw new DuplicateOrganisationException("An organisation with the name \""+organisationID+"\" already exists!");
		
				boolean creatingFirstOrganisation = isOrganisationCfsEmpty();
				
				InitialContext ctx = new InitialContext();
				TransactionManager transactionManager = getJ2EEVendorAdapter().getTransactionManager(ctx);
				File jdoConfigDir = null;
				DatabaseAdapter databaseAdapter = null;
				boolean dropDatabase = false; // will be set true, AFTER the databaseAdapter has really created the database - this prevents a database to be dropped that was already previously existing
				OrganisationCf organisationCf = null;
				boolean doCommit = false;
				transactionManager.begin();
		    try {

					JFireServerConfigModule.Database dbCf = mcf.getConfigModule().getDatabase();
					JFireServerConfigModule.JDO jdoCf = mcf.getConfigModule().getJdo();

					String organisationID_simpleChars = organisationID.replace('.', '_');

					// generate databaseName
					StringBuffer databaseNameSB = new StringBuffer();
					databaseNameSB.append(dbCf.getDatabasePrefix());
					databaseNameSB.append(organisationID_simpleChars);
					databaseNameSB.append(dbCf.getDatabaseSuffix());
					String databaseName = databaseNameSB.toString();

					// get jdbc url
					String dbURL = dbCf.getDatabaseURL(databaseName);
					String datasourceJNDIName_relative = OrganisationCf.DATASOURCE_PREFIX_RELATIVE + organisationID;
					String datasourceJNDIName_absolute = OrganisationCf.DATASOURCE_PREFIX_ABSOLUTE + organisationID;
					String jdoPersistenceManagerFactoryJNDIName_relative = OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_RELATIVE + organisationID;
					String jdoPersistenceManagerFactoryJNDIName_absolute = OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID;

					try {
						Class.forName(dbCf.getDatabaseDriverName());
					} catch (ClassNotFoundException e) {
						throw new ConfigException("Database driver class \""+dbCf.getDatabaseDriverName()+"\" could not be found!", e);
					}

					// create database
					databaseAdapter = dbCf.instantiateDatabaseAdapter();

					try {
						databaseAdapter.createDatabase(mcf.getConfigModule(), dbURL);
						dropDatabase = true;
					} catch (Exception x) {
						throw new ModuleException("Creating database with DatabaseAdapter \"" + databaseAdapter.getClass().getName() + "\" failed!", x);
					}

					jdoConfigDir = new File(jdoCf.getJdoConfigDirectory(organisationID)).getAbsoluteFile();

					Map variables = new HashMap();
					variables.put("organisationID", organisationID);
					variables.put("datasourceJNDIName_relative", datasourceJNDIName_relative);
					variables.put("datasourceJNDIName_absolute", datasourceJNDIName_absolute);
					variables.put("datasourceMetadataTypeMapping", dbCf.getDatasourceMetadataTypeMapping());
					variables.put("jdoPersistenceManagerFactoryJNDIName_relative", jdoPersistenceManagerFactoryJNDIName_relative);
					variables.put("jdoPersistenceManagerFactoryJNDIName_absolute", jdoPersistenceManagerFactoryJNDIName_absolute);
					variables.put("databaseDriverName", dbCf.getDatabaseDriverName());
					variables.put("databaseURL", dbURL);
					variables.put("databaseUserName", dbCf.getDatabaseUserName());
					variables.put("databasePassword", dbCf.getDatabasePassword());

					File datasourceDSXML;
					try {
						datasourceDSXML = createDSXML(
								jdoConfigDir,
								new File(jdoCf.getDatasourceTemplateDSXMLFile()),
								jdoCf.getDatasourceConfigFile(organisationID),
								variables);
					} catch (Exception e) {
						throw new ModuleException("Generating datasource ds xml file from template \""+jdoCf.getDatasourceTemplateDSXMLFile()+"\" failed!", e);
					}

					File jdoDSXML;
					try {
						jdoDSXML = createDSXML(
								jdoConfigDir,
								new File(jdoCf.getJdoTemplateDSXMLFile()),
								jdoCf.getJdoConfigFile(organisationID),
								variables);
					} catch (Exception e) {
						throw new ModuleException("Generating jdo ds xml file from template \""+jdoCf.getJdoTemplateDSXMLFile()+"\" failed!", e);
					}

//					// Activate the jdo ds xml by renaming it.
//					File jdoDSXML = new File(
//							jdoConfigDir,
//							jdoCf.getJdoConfigFilePrefix() + organisationID + jdoCf.getJdoConfigFileSuffix()
//							);
//					tmpJDODSXML.renameTo(jdoDSXML);

					organisationCf = organisationConfigModule.addOrganisation(
							organisationID, organisationName);
//					, masterOrganisationID,
//							"java:/"+jdoPersistenceManagerJNDIName);
					if (userID != null && isServerAdmin) organisationCf.addServerAdmin(userID);
					resetOrganisationCfs();
					try {
						getConfig().saveConfFile(true); // TODO force all modules to be written???
					} catch (ConfigException e) {
						logger.fatal("Saving config failed!", e);
					}
					logger.info("Empty organisation \""+organisationID+"\" (\""+organisationName+"\") has been created. Waiting for deployment...");

					PersistenceManager pm = null;
					try {
						// Now, we need to wait until the deployment of the x-ds.xml is complete and our
						// jdo persistencemanager is existing in JNDI.
						int tryCount = createOrganisationConfigModule.getWaitForPersistenceManager_tryCount();

						int tryNr = 0;
						while (pm == null) {
							++tryNr;
							try {
								pm = waitForPersistenceManager(OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID); // org.getPersistenceManagerFactoryJNDIName());
							} catch (ModuleException x) {
								if (tryNr >= tryCount) throw x;

								logger.info("Obtaining PersistenceManagerFactory failed! Touching jdo-ds-file and its directory and trying it again...");
								long now = System.currentTimeMillis();
								datasourceDSXML.setLastModified(now);
								jdoDSXML.setLastModified(now);
								jdoDSXML.getParentFile().setLastModified(now);
							}
						}
						logger.info("PersistenceManagerFactory of organisation \""+organisationID+"\" (\""+organisationName+"\") has been deployed.");


						// Create the basic jdo objects:
						//		- LocalServer
						//		- LocalOrganisation
						//		- Authority "_Organisation_"
						//		- User user
						//		- User "_Other_"
						//		- UserRef "_Organisation_"+"_Other_"
						//		- UserRef "_Organisation_"+user

						if(logger.isDebugEnabled())
							logger.debug("Creating JDO object LocalServer...");
						Server server = mcf.getConfigModule().getLocalServer().createServer(pm);
						LocalServer localServer = new LocalServer(server);
						pm.makePersistent(localServer);
						if(logger.isDebugEnabled())
							logger.debug("pm.makePersistent(localServer) done.");
			
						if(logger.isDebugEnabled())
							logger.debug("Creating JDO object LocalOrganisation...");
						Organisation organisation = organisationCf.createOrganisation(pm, server);
						LocalOrganisation localOrganisation = new LocalOrganisation(organisation);
						pm.makePersistent(localOrganisation);
						if(logger.isDebugEnabled())
							logger.debug("pm.makePersistent(localOrganisation) done.");

						if(logger.isDebugEnabled())
							logger.debug("Creating JDO object AuthorityType with ID \""+AuthorityType.AUTHORITY_TYPE_ID_SYSTEM+"\"...");
						AuthorityType authorityType = new AuthorityType(organisationID, AuthorityType.AUTHORITY_TYPE_ID_SYSTEM);
						pm.makePersistent(authorityType);
						if(logger.isDebugEnabled())
							logger.debug("pm.makePersistent(authorityType) done.");

						if(logger.isDebugEnabled())
							logger.debug("Creating JDO object Authority with ID \""+Authority.AUTHORITY_ID_ORGANISATION+"\"...");
						Authority authority = new Authority(organisationID, Authority.AUTHORITY_ID_ORGANISATION, authorityType);
						pm.makePersistent(authority);
						if(logger.isDebugEnabled())
							logger.debug("pm.makePersistent(authority) done.");

						if(logger.isDebugEnabled())
							logger.debug("Creating JDO object User with ID \""+User.USERID_OTHER+"\"...");
						User otherUser = new User(organisationID, User.USERID_OTHER);
						new UserLocal(otherUser);
						pm.makePersistent(otherUser);
						if(logger.isDebugEnabled())
							logger.debug("pm.makePersistent(otherUser) done.");

						if(logger.isDebugEnabled())
							logger.debug("Creating JDO object User with ID \""+userID+"\"...");
						User user = new User(organisationID, userID);
						UserLocal userLocal = new UserLocal(user);
						userLocal.setPasswordPlain(password);
						pm.makePersistent(user);
						if(logger.isDebugEnabled())
							logger.debug("pm.makePersistent(user) done.");

						if(logger.isDebugEnabled())
							logger.debug("Creating instances of UserRef for both Users within the default authority...");
						authority.createUserRef(otherUser);
						UserRef userRef = authority.createUserRef(user);
						if(logger.isDebugEnabled())
							logger.debug("Creating instances of UserRef for both Users within the default authority done.");

						// import all roles
						if(logger.isDebugEnabled())
							logger.debug("Importing all roles and role groups...");
						RoleImportSet roleImportSet = roleImport_prepare(organisationID);
						roleImport_commit(roleImportSet, pm);
						if(logger.isDebugEnabled())
							logger.debug("Import of roles and role groups done.");

						// Give the user all RoleGroups.
						if(logger.isDebugEnabled())
							logger.debug("Assign all RoleGroups to the user \""+userID+"\"...");
						for (Iterator it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
							RoleGroup roleGroup = (RoleGroup)it.next();
							RoleGroupRef roleGroupRef = authority.createRoleGroupRef(roleGroup);
							userRef.addRoleGroupRef(roleGroupRef);
						}
		//				for (Iterator it = createOrganisationConfigModule.getInitialRoleGroups().iterator(); it.hasNext(); ) {
		//					RoleGroupCf roleGroupCf = (RoleGroupCf) it.next();
		//					RoleGroup roleGroup = roleGroupCf.createRoleGroup(pm);
		//					RoleGroupRef roleGroupRef = authority.createRoleGroupRef(roleGroup);
		//					userRef.addRoleGroupRef(roleGroupRef);
		//				}
						if(logger.isDebugEnabled())
							logger.debug("Assigning all RoleGroups to user \""+userID+"\" done.");
						
						// create system user
						if(logger.isDebugEnabled())
							logger.debug("Creating system user...");
						User systemUser = new User(organisationID, User.USERID_SYSTEM);
						new UserLocal(systemUser);
						pm.makePersistent(systemUser);
						if(logger.isDebugEnabled())
							logger.debug("System user created.");
			
						// Because flushing the authentication cache causes trouble to currently logged in
						// clients, we only do that if we are creating the first organisation of a new server.
						if (creatingFirstOrganisation)
							j2ee_flushAuthenticationCache();
			
						// Instantiate and fire all the CreateOrganisationListeners.
		//				for (Iterator it = createOrganisationConfigModule.getCreateOrganisationListeners().iterator(); it.hasNext(); ) {
		//					String className = (String)it.next();
		//					if (className.startsWith("#")) // comment
		//						continue;
		//	
		//					try {
		//						Class clazz = Class.forName(className);
		//						if (!CreateOrganisationListener.class.isAssignableFrom(clazz))
		//							throw new ClassCastException("CreateOrganisationListener does not implement interface \""+CreateOrganisationListener.class.getName()+"\"!");
		//							
		//						CreateOrganisationListener listener = (CreateOrganisationListener) clazz.newInstance();
		//						listener.organisationCreated(pm, org);
		//					} catch (Exception x) {
		//						LOGGER.fatal("notifying CreateOrganisationListener \""+className+"\" failed!", x);
		//					}
		//				}

					} finally {
						if (pm != null)
							pm.close();
					}

					// create the CacheManagerFactory for the new organisation
					try {
						CacheManagerFactory cmf = new CacheManagerFactory(ctx, organisationCf, cacheCfMod); // registers itself in JNDI

						// register the cache's JDO-listeners in the PersistenceManagerFactory
						PersistenceManagerFactory pmf = getPersistenceManagerFactory(organisationID);
						cmf.setupJdoCacheBridge(pmf);

						new OrganisationSyncManagerFactory(ctx, organisationID,
								getJ2EEVendorAdapter().getTransactionManager(ctx), pmf); // registers itself in JNDI
					} catch (Exception e) {
						logger.error("Creating CacheManagerFactory for organisation \""+organisationID+"\" failed!", e);
						throw new ResourceException(e.getMessage());
					}

					doCommit = true;
		    } finally {
		    	if (doCommit)
		    		transactionManager.commit();
		    	else {
		    		try {
		    			transactionManager.rollback();
		    		} catch (Throwable t) {
		    			logger.error("Rolling back transaction failed!", t);
		    		}

		    		// We drop the database after rollback(), because it might be the case that JDO tries to do sth. with
		    		// the database during rollback.
		    		try {
		    			if (dropDatabase && databaseAdapter != null)
		    				databaseAdapter.dropDatabase();
		    		} catch (Throwable t) {
		    			logger.error("Dropping database failed!", t);
		    		}

		    		try {
			    		if (jdoConfigDir != null) {
			    			if (!Utils.deleteDirectoryRecursively(jdoConfigDir))
			    				logger.error("Deleting JDO config directory \"" + jdoConfigDir.getAbsolutePath() + "\" failed!");;
			    		}
		    		} catch (Throwable t) {
		    			logger.error("Deleting JDO config directory \"" + jdoConfigDir.getAbsolutePath() + "\" failed!", t);
		    		}

		    		if (organisationCf != null) {
		    			try {
		    				if (!organisationConfigModule.removeOrganisation(organisationCf.getOrganisationID()))
		    					throw new IllegalStateException("Organisation was not registered in ConfigModule!");

		    				organisationConfigModule._getConfig().saveConfFile();
		    			} catch (Throwable t) {
		    				logger.error("Removing organisation \"" + organisationCf.getOrganisationID() + "\" from JFire server configuration failed!", t);
		    			}
		    		}
		    	}
		    }

			} catch (RuntimeException x) {
				throw x;
			} catch (ModuleException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}

		} // synchronized (this) {

//		String deployBaseDir = mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		DatastoreInitializer datastoreInitializer = new DatastoreInitializer(this, mcf, getJ2EEVendorAdapter());

		try {
			datastoreInitializer.initializeDatastore(
					this, mcf.getConfigModule().getLocalServer(), organisationID,
					jfireSecurity_createTempUserPassword(organisationID, User.USERID_SYSTEM));
		} catch (Exception x) {
			logger.error("Datastore initialization for new organisation \""+organisationID+"\" failed!", x);
		}
	}


	/**
	 * @throws OrganisationNotFoundException If the organisation does not exist.
	 */
	protected OrganisationCf getOrganisationConfig(String organisationID)
		throws ModuleException
	{
		OrganisationCf org = (OrganisationCf)getOrganisationCfsCloned().get(organisationID);
		if (org == null)
			throw new OrganisationNotFoundException("No organisation with [master]organisationID=\""+organisationID+"\" existent!");
		return org;
	}

	protected void addServerAdmin(String organisationID, String userID)
		throws ModuleException
	{
		OrganisationCf org = null;
		for (Iterator it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ){
			OrganisationCf o = (OrganisationCf)it.next();
			if (organisationID.equals(o.getOrganisationID())) { // ||
//					organisationID.equals(o.getMasterOrganisationID())) {
				org = o;
				break;
			}
		}

		if (org == null)
			throw new OrganisationNotFoundException("No organisation with [master]organisationID=\""+organisationID+"\" existent!");
		
		org.addServerAdmin(userID);

		resetOrganisationCfs();
	}

	protected boolean removeServerAdmin(String organisationID, String userID)
		throws ModuleException
	{
		OrganisationCf org = null;
		for (Iterator it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ){
			OrganisationCf o = (OrganisationCf)it.next();
			if (organisationID.equals(o.getOrganisationID())) { // ||
//					organisationID.equals(o.getMasterOrganisationID())) {
				org = o;
				break;
			}
		}

		if (org == null)
			throw new OrganisationNotFoundException("No organisation with [master]organisationID=\""+organisationID+"\" existent!");
		
		boolean res = org.removeServerAdmin(userID);

		resetOrganisationCfs();
		return res;
	}

	protected Config getConfig()
	{
		return mcf.getConfig();
	}
	
	protected boolean isOrganisationCfsEmpty()
	{
		return organisationConfigModule.getOrganisations().isEmpty();
	}
	
	protected synchronized List getOrganisationCfs(boolean sorted)
	{
		// We create a new ArrayList to avoid any problems that might occur if
		// resetOrganisationCfs() is executed (e.g. if a new organisation is added).
		ArrayList l = new ArrayList(getOrganisationCfsCloned().values());
		if (sorted)
			Collections.sort(l);
		return l;
	}

	public synchronized void flushModuleCache()
	{
		cachedModules = null;
	}
	
	/**
	 * key: ModuleType moduleType<br/>
	 * value: List modules
	 */
	protected Map cachedModules = null;

	public synchronized List getModules(ModuleType moduleType)
		throws ModuleException
	{
		try {
			if (cachedModules == null)
				cachedModules = new HashMap();
			
			List modules = (List)cachedModules.get(moduleType);
			if (modules == null) {
				File startDir = new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());
				modules = new ArrayList();
				findModules(startDir, moduleType, modules);
				Collections.sort(modules);
				cachedModules.put(moduleType, modules);
			}
			return modules;
		} catch (Exception x) {
			if (x instanceof ModuleException)
				throw (ModuleException)x;
			throw new ModuleException(x);
		}
	}

	private static class FileFilterDirectoriesExcludingEARs implements FilenameFilter
	{
		/**
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File dir, String name)
		{
			if (name.endsWith(".ear"))
				return false;
			File f = new File(dir, name);
			return f.isDirectory();
		}
	}
	private static FileFilterDirectoriesExcludingEARs fileFilterDirectoriesExcludingEARs = null;

	public static class FileFilterEARs implements FilenameFilter
	{
		/**
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".ear");
		}
	}
	private static FileFilterEARs fileFilterEARs = null;
	
	private void findModules(File directory, ModuleType moduleType, List modules)
		throws XMLReadException
	{
		if (fileFilterDirectoriesExcludingEARs == null)
			fileFilterDirectoriesExcludingEARs = new FileFilterDirectoriesExcludingEARs();
		String[] directories = directory.list(fileFilterDirectoriesExcludingEARs);
		if (directories != null) {
			for (int i = 0; i < directories.length; ++i)
				findModules(new File(directory, directories[i]), moduleType, modules);
		} // if (directories != null) {

		if (fileFilterEARs == null)
			fileFilterEARs = new FileFilterEARs();
		String[] ears = directory.list(fileFilterEARs);
		if (ears != null) {
			for (int i = 0; i < ears.length; ++i) {
				File ear = new File(directory, ears[i]);
				findModulesInEAR(ear, moduleType, modules);
			}
		} // if (ears != null) {
	}

	private void findModulesInEAR(File ear, ModuleType moduleType, List modules)
		throws XMLReadException
	{
// TODO So far, we only support ear directories, but no ear jars.
// EARApplicationMan should be extended to support both!
		if (!ear.isDirectory()) {
			logger.warn("Deployed EAR \""+ear.getAbsolutePath()+"\" is ignored, because only EAR directories are supported!");
			return;
		}
		EARApplicationMan earAppMan = new EARApplicationMan(ear, moduleType);
		for (Iterator it = earAppMan.getModules().iterator(); it.hasNext(); ) {
			ModuleDef md = (ModuleDef)it.next();
			modules.add(md);
		}
	}


	// ******************************************
	// *** Helper variables & methods ***
	// ******************************************

	/**
	 * This map holds clones of the real OrganisationCf instances within
	 * the ConfigModule.
	 * <br/><br/>
	 * key: String organisationID / String masterOrganisationID<br/>
	 * value: OrganisationCf org
	 */
	private Map organisationCfsCloned = null;
	
	protected synchronized void resetOrganisationCfs()
	{
		organisationCfsCloned = null;
	}

	protected synchronized Map getOrganisationCfsCloned()
	{
		if (organisationCfsCloned == null)
		{
			organisationConfigModule.acquireReadLock();
			try {
				organisationCfsCloned = new HashMap();
				for (Iterator it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
					OrganisationCf org = (OrganisationCf)((OrganisationCf)it.next()).clone();
					org.makeReadOnly();
					organisationCfsCloned.put(org.getOrganisationID(), org);
//					if (!org.getMasterOrganisationID().equals(org.getOrganisationID()))
//						organisationCfsCloned.put(org.getMasterOrganisationID(), org);
				}
			} finally {
				organisationConfigModule.releaseLock();
			}
		}
		return organisationCfsCloned;
	}

	/**
	 * Generate a -ds.xml file within the jdo config directory.
	 *
	 * @param jdoConfigDirectory The directory in which the temporary file will be created
	 * @param templateDSXMLFile The template file to use.
	 * @param finalDSXMLFileName The simple name (without path) of the ds.xml-file's final name. Note, that this
	 *		must end with "-ds.xml" in order to work (otherwise the J2EE server will ignore the file)!
	 * @param variables This map defines what variable has to be replaced by what value. The
	 *				key is the variable name (without brackets "{", "}"!) and the value is the
	 *				value for the variable to replace.
	 * @return An instance of File pointing to the newly created temporary ds.xml-file.
	 */
	protected File createDSXML(File jdoConfigDirectory, File templateDSXMLFile, String finalDSXMLFileName, Map variables)
		throws IOException, TemplateParseException
	{
		if (!jdoConfigDirectory.exists()) {
			logger.info("jdoConfigDirectory does not exist. Creating it: " + jdoConfigDirectory.getAbsolutePath());
			if (!jdoConfigDirectory.mkdirs()) {
				logger.error("Creating jdoConfigDirectory failed: " + jdoConfigDirectory.getAbsolutePath());
			}
		}

		File f;

		// Create and configure StreamTokenizer to read template file.
		FileReader fr = new FileReader(templateDSXMLFile);
		try {
			StreamTokenizer stk = new StreamTokenizer(fr);
			stk.resetSyntax();
			stk.wordChars(0, Integer.MAX_VALUE);
			stk.ordinaryChar('{');
			stk.ordinaryChar('}');
//			stk.whitespaceChars('{', '{');
//			stk.whitespaceChars('}', '}');
			
			// Create FileWriter for temporary file.
			f = File.createTempFile(".tmp-", "-ds_xml.tmp", jdoConfigDirectory);
			FileWriter fw = new FileWriter(f);
			try {

				// Read, parse and replace variables from template and write to FileWriter fw.
				boolean nextTokenIsVar = false;
				boolean varRead = false;
				while (stk.nextToken() != StreamTokenizer.TT_EOF) {
					String stringToWrite = null;
					if (stk.ttype == StreamTokenizer.TT_WORD) {
					
						if (nextTokenIsVar) {
							stringToWrite = (String)variables.get(stk.sval);
							if (stringToWrite == null)
								throw new TemplateParseException("Unknown variable: {"+stk.sval+"}");
							else
								varRead = true;
							
							nextTokenIsVar = false;
						}
						else
							stringToWrite = stk.sval;
						
					}
					else if (stk.ttype == '{') {
						varRead = false;
						nextTokenIsVar = true;
					}
					else if (stk.ttype == '}') {
						if (!varRead)
							throw new TemplateParseException("Found \"}\" without previous \"{\"!");
						varRead = false;
					}
					if (stringToWrite != null)
						fw.write(stringToWrite);
				} // while (stk.nextToken() != StreamTokenizer.TT_EOF) {

			} finally {
				fw.close();
			}
		} finally {
			fr.close();
		}

		File finalDSXMLFile = new File(jdoConfigDirectory, finalDSXMLFileName);
		if (!f.renameTo(finalDSXMLFile))
			throw new IOException("Renaming file \""+ f.getAbsolutePath() +"\" to \"" + f.getAbsolutePath() + "\" failed!");

		return finalDSXMLFile;
	}

	public static PersistenceManagerFactory getPersistenceManagerFactory(String organisationID)
	throws ModuleException
	{
		PersistenceManagerFactory pmf;
		try {
			InitialContext initCtx = new InitialContext();
			try {
				pmf = (PersistenceManagerFactory) initCtx.lookup(
						OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID);
			} finally {
				initCtx.close();
			}
		} catch (NamingException e) {
			throw new ModuleException(e);
		}
		return pmf;
//		OrganisationCf org = getOrganisationConfig(organisationID);
//		String persistenceManagerJNDIName = org.getPersistenceManagerFactoryJNDIName();
//		try {
//			InitialContext initCtx = new InitialContext();
//			try {
//				return (PersistenceManagerFactory)initCtx.lookup(persistenceManagerJNDIName);
//			} finally {
//				initCtx.close();
//			}
//		} catch (NamingException e) {
//			throw new ModuleException(e);
//		}
	}

	public PersistenceManager getPersistenceManager(String organisationID)
	throws ModuleException
	{
		PersistenceManagerFactory pmf = getPersistenceManagerFactory(organisationID);
		PersistenceManager pm = pmf.getPersistenceManager();
		return pm;
	}

	protected PersistenceManager waitForPersistenceManager(String persistenceManagerJNDIName)
		throws ModuleException
	{
		try {
			InitialContext initCtx = new InitialContext();
			try {
				PersistenceManagerFactory pmf = null;
				long waitStartDT = System.currentTimeMillis();

				int timeout = createOrganisationConfigModule.getWaitForPersistenceManager_timeout();
				int checkPeriod = createOrganisationConfigModule.getWaitForPersistenceManager_checkPeriod();
				while (pmf == null)
				{
					try {
						pmf = (PersistenceManagerFactory)initCtx.lookup(persistenceManagerJNDIName);
					} catch (NamingException x) {
						if (System.currentTimeMillis() < waitStartDT) { // System time has been changed!
							waitStartDT = System.currentTimeMillis();
							logger.warn("While waiting for deployment of PersistenceManagerFactory \""+persistenceManagerJNDIName+"\", the system time has been changed. Resetting wait time.");
						}
		
						if (System.currentTimeMillis() - waitStartDT > timeout) {
							logger.fatal("PersistenceManagerFactory \""+persistenceManagerJNDIName+"\" has not become accessible in JNDI within timeout (\""+timeout+"\" msec).");
							throw x;
						}
						else
							try {
								logger.info("PersistenceManagerFactory \""+persistenceManagerJNDIName+"\" is not yet accessible in JNDI. Waiting "+checkPeriod+" msec.");
								Thread.sleep(checkPeriod);
							} catch (InterruptedException e) {
								logger.error("Sleeping has been interrupted!", e);
							}
					}
				} // while (pmf == null)

				PersistenceManager pm = null;
				int pmTryCount = 0;
				while (pm == null) {
					try {
						pm = pmf.getPersistenceManager();
						if (pm == null)
							throw new NullPointerException("PersistenceManager coming out of factory should never be null!");
					} catch (Exception x) {
						logger.warn("getPersistenceManager() failed!", x);

						if (++pmTryCount > 3)
							throw x;
						Thread.sleep(3000);
					}
				}

				return pm;
			} finally {
				initCtx.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	protected SecurityRegistrar getSecurityRegistrar(JFireBasePrincipal principal)
		throws ModuleException
	{
		return smf.getSecurityRegistrar(principal);
	}

	protected CLRegistrar getCLRegistrar(JFireBasePrincipal principal)
		throws ModuleException
	{
		return clRegistrarFactory.getCLRegistrar(principal);
	}

	/**
	 * key: String userName(userID@organisationID)<br/>
	 * value: String password
	 */
	protected Map jfireSecurity_tempUserPasswords = new HashMap();

	protected boolean jfireSecurity_checkTempUserPassword(String organisationID, String userID, String password)
	{
		String pw = (String) jfireSecurity_tempUserPasswords.get(userID + '@' + organisationID);
		if (pw == null)
			return false;

		return pw.equals(password);
	}

	protected String jfireSecurity_createTempUserPassword(String organisationID, String userID)
	{
		String pw = (String) jfireSecurity_tempUserPasswords.get(userID + '@' + organisationID);
		if (pw == null) {
			pw = UserLocal.generatePassword(8, 16);
			jfireSecurity_tempUserPasswords.put(userID + '@' + organisationID, pw);
		}
		return pw;
	}

	/**
	 * This Map caches all the roles for all the users. It does NOT expire, because
	 * it relies on that jfireAuth_flushCache is executed whenever access rights
	 * change!
	 *
	 * key: String userID + @ + organisationID<br/>
	 * value: SoftReference of RoleSet roleSet
	 */
	protected Map jfireSecurity_roleCache = new HashMap();

	protected void jfireSecurity_flushCache(String organisationID, String userID)
	{
		if (User.USERID_OTHER.equals(userID)) {
			jfireSecurity_flushCache();
			return;
		}

		String userPK = userID + '@' + organisationID;
		synchronized (jfireSecurity_roleCache) {
			jfireSecurity_roleCache.remove(userPK);
		}
	}

	protected void jfireSecurity_flushCache()
	{
		synchronized (jfireSecurity_roleCache) {
			jfireSecurity_roleCache.clear();
		}
	}

	protected RoleSet jfireSecurity_getRoleSet(String organisationID, String userID)
		throws ModuleException
	{
		// TO DO This should be delegated to the SecurityRegistry!
		// ??? or maybe not ???
//		try {
			String userPK = userID + '@' + organisationID;

			RoleSet roleSet = null;
			// lookup in cache.
			synchronized (jfireSecurity_roleCache) {
				SoftReference ref = (SoftReference)jfireSecurity_roleCache.get(userPK);
				if (ref != null)
					roleSet = (RoleSet)ref.get();
			}

			if (roleSet != null)
				return roleSet;

			roleSet = new RoleSet();

			roleSet.addMember(new SimplePrincipal("_Guest_")); // EVERYONE has this role!
	
			
//					boolean doCommit = false;
//					TransactionManager tx = lookup.getTransactionManager();
//					boolean handleTx = tx.getStatus() == Status.STATUS_NO_TRANSACTION;
//					if (handleTx)
//						tx.begin();
//					try {
			PersistenceManager pm = getPersistenceManager(organisationID);
			try {
				if (User.USERID_SYSTEM.equals(userID)) {
					// user is system user and needs ALL roles
					roleSet.addMember(new SimplePrincipal("_ServerAdmin_"));
					for (Iterator it = pm.getExtent(Role.class, true).iterator(); it.hasNext(); ) {
						Role role = (Role) it.next();
						roleSet.addMember(new SimplePrincipal(role.getRoleID()));
					}
				}
				else {
					// user is normal user and has only those roles that are assigned

					pm.getExtent(UserRef.class, true);
					pm.getExtent(RoleRef.class, true);
					
					// If the user is marked as server admin, we give it the appropriate
					// role. For security reasons, this role is managed outside of the persistence
					// manager, because data within the organisations' database is belonging to this
					// organisation and can be changed by them. This role must not be set by the
					// organisation, but only by the administrator of the server.
					if (getOrganisationConfig(organisationID).isServerAdmin(userID))
						roleSet.addMember(new SimplePrincipal("_ServerAdmin_"));
	
					UserRef userRef;
					try {
						userRef = (UserRef) pm.getObjectById(
								UserRefID.create(
										Authority.AUTHORITY_ID_ORGANISATION, 
										organisationID, userID
								), true);
					} catch (JDOObjectNotFoundException x) {
						try {
							userRef = (UserRef) pm.getObjectById(
									UserRefID.create(
											Authority.AUTHORITY_ID_ORGANISATION, 
											organisationID, User.USERID_OTHER
									), true);
						} catch (JDOObjectNotFoundException e) {
							userRef = null;
						}
					}
	
					// get roleRefs
					if (userRef != null) {
						for (Iterator it = userRef.getRoleRefs().iterator(); it.hasNext(); ) {
							RoleRef roleRef = (RoleRef)it.next();
							roleSet.addMember(roleRef.getRolePrincipal());
						}
					} // if (userRef != null) {

				} // if (User.USERID_SYSTEM.equals(userID)) {

			} finally {
				pm.close();
			}
//						doCommit = true;
//					} finally {
//						if (handleTx) {
//							if (doCommit)
//								tx.commit();
//							else
//								tx.rollback();
//						}
//					}

			synchronized (jfireSecurity_roleCache) {
				jfireSecurity_roleCache.put(userPK, new SoftReference(roleSet));
			}
			return roleSet;

//			LOGGER.debug("getRoleSets()");
//			Group callerPrincipal = new SimpleGroup("CallerPrincipal");
//			callerPrincipal.addMember(principal);
//			return new Group[]{roleSet, callerPrincipal};
//		} catch (RuntimeException x) {
//			throw x;
//		} catch (Exception x) {
//			if (x instanceof LoginException)
//				throw (LoginException)x;
//			LOGGER.fatal("getRoleSets() failed!", x);
//			throw new LoginException(x.getMessage());
//		}
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManagerFactory#getInitialContextFactory(java.lang.String, boolean)
	 */
	public String getInitialContextFactory(String j2eeServerTypeRemote, boolean throwExceptionIfUnknownServerType)
	{
		J2eeServerTypeRegistryConfigModule.J2eeRemoteServer j2eeRemoteServerCf =
				j2eeLocalServerCf.getJ2eeRemoteServer(j2eeServerTypeRemote);
		if (j2eeRemoteServerCf == null) {
			if (throwExceptionIfUnknownServerType)
				throw new IllegalArgumentException("No configuration for remote j2eeServerType \""+j2eeServerTypeRemote+"\"!");

			return null;
		}
		return j2eeRemoteServerCf.getInitialContextFactory();
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManagerFactory#getLocalServer()
	 */
	public ServerCf getLocalServer()
	{
		return (ServerCf) mcf.getConfigModule().getLocalServer().clone();
	}

	/**
	 * @see org.nightlabs.jfire.servermanager.JFireServerManagerFactory#isUpAndRunning()
	 */
	public boolean isUpAndRunning()
	{
		return upAndRunning;
	}
}
