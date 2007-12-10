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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.JFireServerLocalLoginManager;
import org.nightlabs.jfire.base.PersistenceManagerProvider;
import org.nightlabs.jfire.base.SimplePrincipal;
import org.nightlabs.jfire.classloader.CLRegistrar;
import org.nightlabs.jfire.classloader.CLRegistrarFactory;
import org.nightlabs.jfire.classloader.CLRegistryCfMod;
import org.nightlabs.jfire.datastoreinit.DatastoreInitException;
import org.nightlabs.jfire.datastoreinit.DatastoreInitManager;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jdo.cache.CacheCfMod;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationManagerFactory;
import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleRef;
import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserRef;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.UserRefID;
import org.nightlabs.jfire.security.registry.SecurityRegistrar;
import org.nightlabs.jfire.security.registry.SecurityRegistrarFactoryImpl;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.serverinit.ServerInitManager;
import org.nightlabs.jfire.servermanager.DuplicateOrganisationException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.NoServerAdminException;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.config.CreateOrganisationConfigModule;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.JDOCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.OrganisationConfigModule;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.createorganisation.BusyCreatingOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgressID;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStep;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeployedFileAlreadyExistsException;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.j2ee.JMSConnectionFactoryLookup;
import org.nightlabs.jfire.servermanager.j2ee.ServerStartNotificationListener;
import org.nightlabs.jfire.servermanager.xml.EARApplicationMan;
import org.nightlabs.jfire.servermanager.xml.EJBJarMan;
import org.nightlabs.jfire.servermanager.xml.EJBRoleGroupMan;
import org.nightlabs.jfire.servermanager.xml.ModuleDef;
import org.nightlabs.jfire.servermanager.xml.RoleDef;
import org.nightlabs.jfire.servermanager.xml.RoleGroupDef;
import org.nightlabs.jfire.servermanager.xml.XMLReadException;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;
import org.xml.sax.SAXException;

/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireServerManagerFactoryImpl
	implements
		ConnectionFactory,
		JFireServerManagerFactory,
		PersistenceManagerProvider,
		ServerStartNotificationListener
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireServerManagerFactoryImpl.class);
	
	private final ManagedConnectionFactoryImpl mcf;
	private final ConnectionManager cm;
	private Reference ref;

	private volatile boolean upAndRunning = false;
	private volatile boolean shuttingDown = false;

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
		
		J2EEAdapter j2EEAdapter;
		try {
			j2EEAdapter = getJ2EEVendorAdapter();
		} catch (ModuleException e) {
			logger.error("Creating J2EEAdapter failed!", e);
			throw new ResourceException(e.getMessage());
		}
		try 
		{
			try
			{
				initialContext.bind(J2EEAdapter.JNDI_NAME, j2EEAdapter);
			}
			catch (NameAlreadyBoundException nabe)
			{
				initialContext.rebind(J2EEAdapter.JNDI_NAME, j2EEAdapter);
			}
		}
		catch (Exception e) {
			logger.error("Binding J2EEAdapter into JNDI failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try {
			SecurityReflector userResolver = j2EEAdapter.getSecurityReflector();
			if (userResolver == null)
				throw new NullPointerException("J2EEVendorAdapter "+j2EEAdapter.getClass()+".getUserResolver() returned null!");
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

		String property_CacheManagerFactoryCreate_key = CacheManagerFactory.class.getName() + ".create";
		String property_CacheManagerFactoryCreate_value = System.getProperty(property_CacheManagerFactoryCreate_key);
		if ("false".equals(property_CacheManagerFactoryCreate_value)) {
			logger.warn("The system property \"" + property_CacheManagerFactoryCreate_key + "\" has been set to \"" + property_CacheManagerFactoryCreate_value + "\"; the CacheManagerFactory will *not* be created!");
		}
		else {
			for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
				OrganisationCf organisation = it.next();
				String organisationID = organisation.getOrganisationID();

				try {
					new CacheManagerFactory(
							this, initialContext, organisation, cacheCfMod, new File(mcf.getSysConfigDirectory())); // registers itself in JNDI
				} catch (Exception e) {
					logger.error("Creating CacheManagerFactory for organisation \""+organisationID+"\" failed!", e);
					throw new ResourceException(e.getMessage());
				}
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

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				shuttingDown = true;
			}
		});

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
	 */
	public boolean configureServerAndShutdownIfNecessary(final long delayMSec)
	throws ModuleException
	{
		try {

			boolean rebootRequired = ServerConfigurator.configureServer(mcf.getConfigModule());
			
			if (rebootRequired) {
				shuttingDown = true;

				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
				logger.warn("*** REBOOT REQUIRED ***");
	
				logger.warn("The invoked Server Configurator indicates that the server needs to be rebooted! Hence, I will shutdown the server NOW!");
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

	/**
	 * The creation of an organisation is not allowed, before the datastore inits are run.
	 * If you, dear reader, believe that this is a problem, please tell me. Marco :-)
	 */
	private boolean createOrganisationAllowed = false;

	public void serverStarted()
	{
		logger.info("Caught SERVER STARTED event!");

		try {

			InitialContext ctx = new InitialContext();
			try {				
				if (configureServerAndShutdownIfNecessary(0))
					return;

				ServerInitManager serverInitManager = new ServerInitManager(this, mcf, getJ2EEVendorAdapter());
				DatastoreInitManager datastoreInitManager = new DatastoreInitManager(this, mcf, getJ2EEVendorAdapter());

				// do the server inits that are to be performed before the datastore inits
				logger.info("Performing early server inits...");
				serverInitManager.performEarlyInits(ctx);

				// OLD INIT STUFF
				// DatastoreInitialization
				//DatastoreInitializer datastoreInitializer = new DatastoreInitializer(this, mcf, getJ2EEVendorAdapter());

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
						new PersistentNotificationManagerFactory(ctx, organisationID, this,
								getJ2EEVendorAdapter().getTransactionManager(ctx), pmf); // registers itself in JNDI

//						new OrganisationSyncManagerFactory(
//								ctx, organisationID,
//								getJ2EEVendorAdapter().getTransactionManager(ctx), pmf); // registers itself in JNDI
					} catch (NameAlreadyBoundException e) {
						// ignore - might happen, if an organisation is created in an early-server-init
					} catch (Exception e) {
						logger.error("Creating PersistentNotificationManagerFactory for organisation \""+organisationID+"\" failed!", e);
						throw new ResourceException(e.getMessage());
					}

					logger.info("Initialising datastore of organisation \""+organisationID+"\"...");
					try {
						datastoreInitManager.initialiseDatastore(this, mcf.getConfigModule().getLocalServer(), organisationID,
							jfireSecurity_createTempUserPassword(organisationID, User.USERID_SYSTEM));

						// OLD INIT STUFF
//						datastoreInitializer.initializeDatastore(
//								this, mcf.getConfigModule().getLocalServer(), organisationID,
//								jfireSecurity_createTempUserPassword(organisationID, User.USERID_SYSTEM));
						

						logger.info("Datastore initialisation of organisation \""+organisationID+"\" done.");
					} catch (Exception x) {
						logger.error("Datastore initialisation of organisation \""+organisationID+"\" failed!", x);
					}
				}

				createOrganisationAllowed = true;

				// do the server inits that are to be performed after the datastore inits
				logger.info("Performing late server inits...");
				serverInitManager.performLateInits(ctx);

				// OLD INIT STUFF
				// Server Initialization
				// new ServerInitialiser(this, mcf, getJ2EEVendorAdapter()).initializeServer(ctx);
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
	{
		try {
			return (JFireServerManager)getConnection();
		} catch (ResourceException e) {
			throw new RuntimeException(e);
		}
	}
	
	public JFireServerManager getJFireServerManager(JFirePrincipal jfirePrincipal)
	{
		try {
			JFireServerManager ism = (JFireServerManager)getConnection();
			if (jfirePrincipal != null)
				((JFireServerManagerImpl)ism).setJFirePrincipal(jfirePrincipal);
			return ism;
		} catch (ResourceException e) {
			throw new RuntimeException(e);
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
	throws ConfigException
	{
		if (cfMod.getLocalServer() == null)
			throw new NullPointerException("localServer of config module must not be null!");

		if (cfMod.getDatabase() == null)
			throw new NullPointerException("database of config module must not be null!");

		if (cfMod.getJdo() == null)
			throw new NullPointerException("jdo of config module must not be null!");

		mcf.testConfiguration(cfMod);

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
			
			// ensure a reasonable SMTP-Config is set. // TODO shouldn't this code better be in the init method of the JFireServerConfigModule?! or in ManagedConnectionFactory.testConfiguration(...)? IMHO that's wrong here. Marco.
//			if (cfMod.getSmtp() == null) {
//				if (orgCfMod.getSmtp() == null) {
//					logger.warn("There are no SMTP settings set! Using fallback values. ", new NullPointerException());
//					SmtpMailServiceCf fallback = new SmtpMailServiceCf();
//					fallback.init();
//					cfMod.setSmtp(fallback);
//				} else {
//					cfMod.setSmtp(orgCfMod.getSmtp());
//				}
//			}
			// I think the above code (checking the SMTP-config) is not necessary, because it actually is already done in the init() method.
				
			try {
				BeanUtils.copyProperties(orgCfMod, cfMod);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e); // should never happen => RuntimeException
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e); // should never happen => RuntimeException
			}

		} finally {
			orgCfMod.releaseLock();
		}

		getConfig().save(true); // TODO force all modules to be written???

		try {
			InitialContext initialContext = new InitialContext();
			try {
				String newRootOrganisationID = cfMod.getRootOrganisation().getOrganisationID();
				String oldRootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				if (!newRootOrganisationID.equals(oldRootOrganisationID)) {
					initialContext.rebind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, newRootOrganisationID);
				}
			} finally {
				initialContext.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e); // should never happen => RuntimeException
		}
	}

	protected J2EEAdapter j2eeVendorAdapter = null;
	public synchronized J2EEAdapter getJ2EEVendorAdapter()
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
		Map<String, Throwable> exceptions = new HashMap<String, Throwable>(); // key: File jar; value: Throwable exception
		roleImport_prepare_collect(startDir, globalEJBRoleGroupMan, exceptions);

		globalEJBRoleGroupMan.removeRole(User.USERID_SYSTEM); // the _System_ role should never be imported so that no real user can ever get this role!

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
	
	private void roleImport_prepare_collect(File directory, EJBRoleGroupMan globalEJBRoleGroupMan, Map<String, Throwable> exceptions)
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

	private transient Object roleImport_commit_mutex = new Object();

	/**
	 * @param roleImportSet
	 * @param pm can be <tt>null</tt>. If <tt>null</tt>, it will be obtained according to <tt>roleImportSet.getOrganisationID()</tt>.
	 * @throws ModuleException
	 */
	protected void roleImport_commit(RoleImportSet roleImportSet, PersistenceManager pm)
	{
		synchronized (roleImport_commit_mutex) {
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
	}

	private transient Object createOrganisation_mutex = new Object();

	/**
	 * This method generates a database-name out of the organisationID. Therefore,
	 * it replaces all characters which are not allowed in a database name by '_'.
	 * <p>
	 * <b>Warning:</b> This method allows name clashes, because e.g. both "a.b" and "a-b"
	 * are translated to "a_b".
	 * </p>
	 *
	 * @param organisationID The organisationID to be translated.
	 * @return the database name resulting from the given <code>organisationID</code>.
	 */
	protected String createDatabaseName(String organisationID, boolean appendDatabasePrefixAndSuffix)
	{
		StringBuffer databaseName = new StringBuffer((int) (1.5 * organisationID.length()));

		databaseName.append(organisationID.replaceAll("[^A-Za-z0-9_]", "_"));

// the following alternative code prevents name clashes. it translates all non-allowed characters are into their
// hex-value with the prefix "_" and the suffix "_". 
//
//		for (char c : organisationID.toCharArray()) {
//			if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9'))
//				databaseName.append(c);
//			else {
//				databaseName.append('_');
//				databaseName.append(ObjectIDUtil.longObjectIDFieldToString(c));
//				databaseName.append('_');
//			}
//		}

		if (appendDatabasePrefixAndSuffix) {
			DatabaseCf dbCf = mcf.getConfigModule().getDatabase();
			databaseName.insert(0, dbCf.getDatabasePrefix());
			databaseName.append(dbCf.getDatabaseSuffix());
		}

		return databaseName.toString();
	}

	private Map<CreateOrganisationProgressID, CreateOrganisationProgress> createOrganisationProgressMap = Collections.synchronizedMap(new HashMap<CreateOrganisationProgressID, CreateOrganisationProgress>());
	private CreateOrganisationProgressID createOrganisationProgressID = null;
	private transient Object createOrganisationProgressID_mutex = new Object();

	// TODO this method should already check all parameter - simply as much as possible in order reduce the possibility of a later failure.
	protected CreateOrganisationProgressID createOrganisationAsync(
			final String organisationID,
			final String organisationName, final String userID, final String password, final boolean isServerAdmin)
	throws BusyCreatingOrganisationException
	{
		synchronized (createOrganisationProgressID_mutex) {
			if (createOrganisationProgressID != null) {
				String busyOrganisationID = createOrganisationProgressMap.get(createOrganisationProgressID).getOrganisationID();
				throw new BusyCreatingOrganisationException(organisationID, CollectionUtil.array2HashSet(new String[] { busyOrganisationID }));
			}

			final CreateOrganisationProgress createOrganisationProgress = new CreateOrganisationProgress(organisationID);
			createOrganisationProgressMap.put(createOrganisationProgress.getCreateOrganisationProgressID(), createOrganisationProgress);
			createOrganisationProgressID = createOrganisationProgress.getCreateOrganisationProgressID();

			Thread thread = new Thread() {
				@Override
				public void run()
				{
					try {
						createOrganisation(createOrganisationProgress, organisationID, organisationName, userID, password, isServerAdmin);
					} catch (Throwable e) {
						logger.error("createOrganisationAsync.Thread.run: creating organisation \"" + organisationID + "\" failed!", e);
					}
				}
			};
			thread.start();

			return createOrganisationProgress.getCreateOrganisationProgressID();
		}
	}

	protected CreateOrganisationProgress getCreateOrganisationProgress(CreateOrganisationProgressID createOrganisationProgressID)
	{
		return createOrganisationProgressMap.get(createOrganisationProgressID);
	}

	protected void createOrganisationProgress_addCreateOrganisationStatus(
			CreateOrganisationProgressID createOrganisationProgressID, CreateOrganisationStatus createOrganisationStatus)
	{
		CreateOrganisationProgress createOrganisationProgress = createOrganisationProgressMap.get(createOrganisationProgressID);
		if (createOrganisationProgress == null)
			throw new IllegalArgumentException("No CreateOrganisationProgress known with this id: " + createOrganisationProgressID);

		createOrganisationProgress.addCreateOrganisationStatus(createOrganisationStatus);
	}

	/**
	 * This method creates a new organisation. What exactly happens, is documented in our wiki:
	 * https://www.jfire.org/modules/phpwiki/index.php/NewOrganisationCreation
	 * @param createOrganisationProgress <code>null</code> or an instance of {@link CreateOrganisationProgress} in order to track the status.
	 * @param organisationID The ID of the new organsitation, which must not be <code>null</code>. Example: "RioDeJaneiro.NightLabs.org"
	 * @param organisationName The "human" name of the organisation. Example: "NightLabs GmbH, Rio de Janeiro"
	 * @param userID The userID of the first user to be created. This will be the new organisation's administrator.
	 * @param password The password of the organisation's first user.
	 * @param isServerAdmin Whether the organisation's admin will have server-administrator privileges. This must be <tt>true</tt> if you create the first organisation on a server.
	 */
	protected void createOrganisation(
			CreateOrganisationProgress createOrganisationProgress, String organisationID,
			String organisationName, String userID, String password, boolean isServerAdmin)
//			String masterOrganisationID
//			) 
		throws ModuleException
	{
		if (!createOrganisationAllowed)
			throw new IllegalStateException("This method cannot be called yet. The creation of organisations is not allowed, before the datastore inits are run. If you get this exception in an early-server-init, you should switch to a late-server-init.");

		// check the parameters (only some here - some will be checked below)
		if (createOrganisationProgress == null)
			throw new IllegalArgumentException("createOrganisationProgress must not be null!");
		
		if (organisationID == null)
			throw new IllegalArgumentException("organisationID must not be null!");

		if ("".equals(organisationID))
			throw new IllegalArgumentException("organisationID must not be an empty string!");

		if (organisationID.indexOf('.') < 0)
			throw new IllegalArgumentException("organisationID is invalid! Must have domain-style form (e.g. \"jfire.nightlabs.de\")!");

		if (!Organisation.isValidOrganisationID(organisationID))
			throw new IllegalArgumentException("organisationID is not valid! Make sure it does not contain special characters. It should have a domain-style form!");

		if (organisationID.length() > 50)
			throw new IllegalArgumentException("organisationID has "+organisationID.length()+" chars and is too long! Maximum is 50 characters.");
//TODO Though the database definition currently allows 100 chars, we'll probably have to reduce it to 50 because of
//primary key constraints (max 1024 bytes with InnoDB) and the fact that MySQL uses 3 bytes per char when activating
//UTF8!

		if (organisationName == null)
			throw new IllegalArgumentException("organisationName must not be null!");

		if ("".equals(organisationName))
			throw new IllegalArgumentException("organisationName must not be an empty string!");

		if (!organisationID.equals(createOrganisationProgress.getOrganisationID()))
			throw new IllegalArgumentException("organisationID does not match createOrganisationProgress.getOrganisationID()!");

		DatastoreInitManager datastoreInitManager;
		try {
			datastoreInitManager = new DatastoreInitManager(this, mcf, getJ2EEVendorAdapter());
		} catch (DatastoreInitException e) {
			logger.error("Creation of DatastoreInitManager failed!", e);
			throw new ModuleException(e);
		}

		// the steps before DatastoreInit are defined in org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStep
		int stepsBeforeDatastoreInit = 10;
		int stepsDuringDatastoreInit = 2 * datastoreInitManager.getInits().size(); // 2 * because we track begin and end

		createOrganisationProgress.setStepsTotal(stepsBeforeDatastoreInit + stepsDuringDatastoreInit);

		try { // finally will clear this.createOrganisationProgressID, if it matches our current one
			synchronized (createOrganisation_mutex) { // TODO this is not nice and might cause the below error in extremely rare situations (because between this line and the next, createOrganisationAsync might be called)
				synchronized (createOrganisationProgressID_mutex) {
					if (createOrganisationProgressID != null && !createOrganisationProgressID.equals(createOrganisationProgress.getCreateOrganisationProgressID())) {
						String busyOrganisationID = createOrganisationProgressMap.get(createOrganisationProgressID).getOrganisationID();
						BusyCreatingOrganisationException x = new BusyCreatingOrganisationException(organisationID, CollectionUtil.array2HashSet(new String[] { busyOrganisationID }));
						logger.error("THIS SHOULD NEVER HAPPEN!", x);
						throw x;
					}

					createOrganisationProgressMap.put(createOrganisationProgress.getCreateOrganisationProgressID(), createOrganisationProgress);
					createOrganisationProgressID = createOrganisationProgress.getCreateOrganisationProgressID();
				}

				try {
					// check the parameters (only some here - some are already checked above)

					if (userID == null)
						throw new IllegalArgumentException("userID must not be null!");

					if ("".equals(userID))
						throw new IllegalArgumentException("userID must not be an empty string!");

					if (!ObjectIDUtil.isValidIDString(userID))
						throw new IllegalArgumentException("userID is not a valid ID! Make sure it does not contain special characters!");

					if (userID.length() > 50)
						throw new IllegalArgumentException("userID has "+userID.length()+" chars and is too long! Maximum is 50 characters.");

					if (password == null)
						throw new IllegalArgumentException("password must NOT be null!");

					if (password.length() < 4)
						throw new IllegalArgumentException("password is too short! At least 4 characters are required! At least 8 characters are recommended!");

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
					Map<String, OrganisationCf> organisationCfsCloned = getOrganisationCfsCloned();
					if (organisationCfsCloned.get(organisationID) != null)
						throw new DuplicateOrganisationException("An organisation with the name \""+organisationID+"\" already exists on this server!");

					//boolean creatingFirstOrganisation = isOrganisationCfsEmpty();

					InitialContext initialContext = new InitialContext();
					try {
						if (Organisation.hasRootOrganisation(initialContext)) {
							// TODO we now have root-organisation-support, hence the root-organisation should be asked, whether the given organisationID is really unique.
							// The root-organisation should block the requested organisationID for about 1 hour. It should assign it (for 1 hour) to this server,
							// so that no other server can simultaneously create an organisation with the same id, but this server can try it again, if it fails.

//							throw new DuplicateOrganisationException("An organisation with the name \""+organisationID+"\" already exists!");
						}

//						TransactionManager transactionManager = getJ2EEVendorAdapter().getTransactionManager(ctx);
						File jdoConfigDir = null;
						DatabaseAdapter databaseAdapter = null;
						boolean dropDatabase = false; // will be set true, AFTER the databaseAdapter has really created the database - this prevents a database to be dropped that was already previously existing
						OrganisationCf organisationCf = null;
						boolean doCommit = false;
//						transactionManager.begin();
						try {

							DatabaseCf dbCf = mcf.getConfigModule().getDatabase();
							JDOCf jdoCf = mcf.getConfigModule().getJdo();

							try {
								Class.forName(dbCf.getDatabaseDriverName_noTx());
							} catch (ClassNotFoundException e) {
								throw new ConfigException("Database driver class (no-tx) \""+dbCf.getDatabaseDriverName_noTx()+"\" could not be found!", e);
							}
							try {
								Class.forName(dbCf.getDatabaseDriverName_localTx());
							} catch (ClassNotFoundException e) {
								throw new ConfigException("Database driver class (local-tx) \""+dbCf.getDatabaseDriverName_localTx()+"\" could not be found!", e);
							}
							try {
								Class.forName(dbCf.getDatabaseDriverName_xa());
							} catch (ClassNotFoundException e) {
								throw new ConfigException("Database driver class (xa) \""+dbCf.getDatabaseDriverName_xa()+"\" could not be found!", e);
							}

							// create database
							String databaseName = createDatabaseName(organisationID, true);
							String dbURL = dbCf.getDatabaseURL(databaseName);

							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(
											CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_createDatabase_begin,
											databaseName, dbURL));

							databaseAdapter = dbCf.instantiateDatabaseAdapter();

							try {
								databaseAdapter.createDatabase(mcf.getConfigModule(), dbURL);
								dropDatabase = true;
							} catch (Exception x) {
								throw new ModuleException("Creating database with DatabaseAdapter \"" + databaseAdapter.getClass().getName() + "\" failed!", x);
							}
							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(
											CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_createDatabase_end,
											databaseName, dbURL));

							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(
											CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_deployJDO_begin,
											databaseName, dbURL));

							jdoConfigDir = new File(jdoCf.getJdoConfigDirectory(organisationID)).getAbsoluteFile();
							File datasourceDSXML = new File(jdoConfigDir, dbCf.getDatasourceConfigFile(organisationID));
							File jdoDSXML = new File(jdoConfigDir, jdoCf.getJdoConfigFile(organisationID));

							// creating deployment descriptor for datasource
							createDeploymentDescriptor(organisationID, datasourceDSXML,
									new File(dbCf.getDatasourceTemplateDSXMLFile()), null, DeployOverwriteBehaviour.EXCEPTION);

							// creating deployment descriptor for JDO PersistenceManagerFactory
							createDeploymentDescriptor(organisationID, jdoDSXML,
									new File(jdoCf.getJdoTemplateDSXMLFile()), null, DeployOverwriteBehaviour.EXCEPTION);

							organisationCf = organisationConfigModule.addOrganisation(
									organisationID, organisationName);

							if (userID != null && isServerAdmin) organisationCf.addServerAdmin(userID);
							resetOrganisationCfs();
							try {
								getConfig().save(true); // TODO really force all modules to be written???
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
										jdoConfigDir.setLastModified(now);
									}
								}
								logger.info("PersistenceManagerFactory of organisation \""+organisationID+"\" (\""+organisationName+"\") has been deployed.");

							} finally {
								if (pm != null) {
									pm.close();
									pm = null;
								}
							}

							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(
											CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_deployJDO_end,
											databaseName, dbURL));

							// populating essential data (Server, Organisation, User etc.) via OrganisationManagerBean.
							// we cannot reference the classes directly, because the project JFireBaseBean is dependent on JFireServerManager.
							// therefore, we reference it via the names.
							ServerCf localServerCf = mcf.getConfigModule().getLocalServer();
							Properties props = InvokeUtil.getInitialContextProperties(
									this, localServerCf, organisationID, User.USERID_SYSTEM,
									jfireSecurity_createTempUserPassword(organisationID, User.USERID_SYSTEM));
							InitialContext authInitCtx = new InitialContext(props);
							try {
								Object bean = InvokeUtil.createBean(authInitCtx, "jfire/ejb/JFireBaseBean/OrganisationManager");
								Method beanMethod = bean.getClass().getMethod(
										"internalInitializeEmptyOrganisation",
										new Class[] { CreateOrganisationProgressID.class, ServerCf.class, OrganisationCf.class, String.class, String.class }
								);
								beanMethod.invoke(bean, new Object[] { createOrganisationProgress.getCreateOrganisationProgressID(), localServerCf, organisationCf, userID, password});
								InvokeUtil.removeBean(bean);
							} finally {
								authInitCtx.close();
							}

							// there has been a role import, hence we need to flush the cache
							// (actually, it would be sufficient to flush it for the new organisation only, but there's no API yet and this doesn't harm)
							jfireSecurity_flushCache();

							// Because flushing the authentication cache causes trouble to currently logged in
							// clients, we only do that if we are creating the first organisation of a new server.
							// ***
							// it seems, the problem described above doesn't exist anymore. but in case it pops up again,
							// we need to uncomment the following line again
//							if (creatingFirstOrganisation)
							j2ee_flushAuthenticationCache();

							// create the CacheManagerFactory for the new organisation
							try {
								CacheManagerFactory cmf = new CacheManagerFactory(
										this, initialContext, organisationCf, cacheCfMod, new File(mcf.getSysConfigDirectory())); // registers itself in JNDI

								// register the cache's JDO-listeners in the PersistenceManagerFactory
								PersistenceManagerFactory pmf = getPersistenceManagerFactory(organisationID);
								cmf.setupJdoCacheBridge(pmf);

//								new OrganisationSyncManagerFactory(ctx, organisationID,
//								getJ2EEVendorAdapter().getTransactionManager(ctx), pmf); // registers itself in JNDI

								new PersistentNotificationManagerFactory(initialContext, organisationID, this,
										getJ2EEVendorAdapter().getTransactionManager(initialContext), pmf); // registers itself in JNDI
							} catch (Exception e) {
								logger.error("Creating CacheManagerFactory or PersistentNotificationManagerFactory for organisation \""+organisationID+"\" failed!", e);
								throw new ResourceException(e.getMessage());
							}

							doCommit = true;
						} finally {
							if (doCommit) {
//								transactionManager.commit();
							}
							else {
//								try {
//								transactionManager.rollback();
//								} catch (Throwable t) {
//								logger.error("Rolling back transaction failed!", t);
//								}

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
										if (!IOUtil.deleteDirectoryRecursively(jdoConfigDir))
											logger.error("Deleting JDO config directory \"" + jdoConfigDir.getAbsolutePath() + "\" failed!");;
									}
								} catch (Throwable t) {
									logger.error("Deleting JDO config directory \"" + jdoConfigDir.getAbsolutePath() + "\" failed!", t);
								}

								if (organisationCf != null) {
									try {
										if (!organisationConfigModule.removeOrganisation(organisationCf.getOrganisationID()))
											throw new IllegalStateException("Organisation was not registered in ConfigModule!");

										resetOrganisationCfs();
										organisationConfigModule._getConfig().save();
									} catch (Throwable t) {
										logger.error("Removing organisation \"" + organisationCf.getOrganisationID() + "\" from JFire server configuration failed!", t);
									}
								}
							}
							databaseAdapter.close(); databaseAdapter = null;
						} // } finally {
					} finally {
						initialContext.close(); initialContext = null;
					}

				} catch (RuntimeException x) {
					createOrganisationProgress.addCreateOrganisationStatus(
							new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_error, x));
					throw x;
				} catch (ModuleException x) {
					createOrganisationProgress.addCreateOrganisationStatus(
							new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_error, x));
					throw x;
				} catch (Exception x) {
					createOrganisationProgress.addCreateOrganisationStatus(
							new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_error, x));
					throw new ModuleException(x);
				}

			} // synchronized (this) {

//			String deployBaseDir = mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();

			try {
				datastoreInitManager.initialiseDatastore(this, mcf.getConfigModule().getLocalServer(), organisationID,
						jfireSecurity_createTempUserPassword(organisationID, User.USERID_SYSTEM), createOrganisationProgress);
			} catch (ModuleException e) {
				logger.error("Datastore initialization for new organisation \""+organisationID+"\" failed!", e);
			}

		// OLD INIT STUFF
		// DatastoreInitializer datastoreInitializer = new DatastoreInitializer(this, mcf, getJ2EEVendorAdapter());

//		try {
//			datastoreInitializer.initializeDatastore(
//					this, mcf.getConfigModule().getLocalServer(), organisationID,
//					jfireSecurity_createTempUserPassword(organisationID, User.USERID_SYSTEM));
//		} catch (Exception x) {
//			logger.error("Datastore initialization for new organisation \""+organisationID+"\" failed!", x);
//		}
		} finally {
			synchronized (createOrganisationProgressID_mutex) {
				if (Util.equals(createOrganisationProgressID, createOrganisationProgress.getCreateOrganisationProgressID()))
					createOrganisationProgressID = null;

				createOrganisationProgress.done();
			}
		}
	}


	/**
	 * @throws OrganisationNotFoundException If the organisation does not exist.
	 */
	protected OrganisationCf getOrganisationConfig(String organisationID)
	throws OrganisationNotFoundException
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
		ArrayList<OrganisationCf> l = new ArrayList<OrganisationCf>(getOrganisationCfsCloned().values());
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
	protected Map<ModuleType, List<ModuleDef>> cachedModules = null;

	public synchronized List getModules(ModuleType moduleType)
		throws ModuleException
	{
		try {
			if (cachedModules == null)
				cachedModules = new HashMap<ModuleType, List<ModuleDef>>();
			
			List<ModuleDef> modules = cachedModules.get(moduleType);
			if (modules == null) {
				File startDir = new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());
				modules = new ArrayList<ModuleDef>();
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
	
	private void findModules(File directory, ModuleType moduleType, List<ModuleDef> modules)
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

	private void findModulesInEAR(File ear, ModuleType moduleType, List<ModuleDef> modules)
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
	private Map<String, OrganisationCf> organisationCfsCloned = null;
	
	protected synchronized void resetOrganisationCfs()
	{
		organisationCfsCloned = null;
	}

	public boolean containsOrganisation(String organisationID)
	{
		return getOrganisationCfsCloned().containsKey(organisationID);
	}

	protected synchronized Map<String, OrganisationCf> getOrganisationCfsCloned()
	{
		if (organisationCfsCloned == null)
		{
			organisationConfigModule.acquireReadLock();
			try {
				organisationCfsCloned = new HashMap<String, OrganisationCf>();
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

	public void undeploy(File deployment)
	throws IOException
	{
		if (deployment.isAbsolute())
			logger.warn("deployment should not be an absolute file: " + deployment.getPath(), new IllegalArgumentException("deployment should not be an absolute file: " + deployment.getPath()));

		if (!deployment.isAbsolute()) {
			deployment = new File(
					new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(),
					deployment.getPath());
		}

		if (!deployment.exists()) {
			logger.warn("deployment does not exist: " + deployment.getPath(), new IllegalArgumentException("deployment does not exist: " + deployment.getPath()));
			return;
		}

		if (!IOUtil.deleteDirectoryRecursively(deployment)) {
			if (deployment.exists())
				throw new IOException("The deployment could not be undeployed: " + deployment.getPath());
			else
				logger.warn("deleting deployment failed, but it does not exist anymore (which is fine): " + deployment.getPath(), new IOException("deleting deployment failed, but it does not exist anymore (which is fine): " + deployment.getPath()));
		}
	}

	public void createDeploymentJar(String organisationID, File deploymentJar, Collection<DeploymentJarItem> deploymentJarItems, DeployOverwriteBehaviour deployOverwriteBehaviour)
	throws IOException
	{
		if (deploymentJar.isAbsolute())
			logger.warn("deploymentJar should not be an absolute file: " + deploymentJar.getPath(), new IllegalArgumentException("deploymentJar should not be an absolute file: " + deploymentJar.getPath()));

		if (!deploymentJar.isAbsolute()) {
			deploymentJar = new File(
					new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(),
					deploymentJar.getPath());
		}

		if (deploymentJar.exists()) {
			switch (deployOverwriteBehaviour) {
				case EXCEPTION:
					throw new DeployedFileAlreadyExistsException(deploymentJar);
				case KEEP:
					logger.warn("File " + deploymentJar + " already exists. Will not change anything!");
					return; // silently return
				case OVERWRITE:
					// nothing
					break;
				default:
					throw new IllegalStateException("Unknown deployOverwriteBehaviour: " + deployOverwriteBehaviour);
			}
		}

		logger.info("Creating deploymentJar: \""+deploymentJar.getAbsolutePath()+"\"");

		// create a temporary directory
		File tmpDir;
		do {
			tmpDir = new File(
					IOUtil.getTempDir(),
					"jfire_" +
					Base62Coder.sharedInstance().encode(System.currentTimeMillis(), 1) + '-' +
					Base62Coder.sharedInstance().encode((int)(Math.random() * Integer.MAX_VALUE), 1) + ".tmp");
		} while (tmpDir.exists()); // should never happen, but it's safer ;-)

		// in case there is no DeploymentJarItem, we create the tmpDir in any case
		if (!tmpDir.mkdirs())
			throw new IOException("Could not create temporary directory: " + tmpDir);

		try { // ensure cleanup

			// we create the manifest only, if it is not contained in the deploymentJarItems
			File manifestFileRelative = new File("META-INF/MANIFEST.MF");

			// create deployment descriptors within the temporary directory
			boolean createManifest = true;
			for (DeploymentJarItem deploymentJarItem : deploymentJarItems) {
				if (manifestFileRelative.equals(deploymentJarItem.getDeploymentJarEntry()))
					createManifest = false;

				File deploymentDescriptorFile = new File(tmpDir, deploymentJarItem.getDeploymentJarEntry().getPath());
				createDeploymentDescriptor(
						organisationID,
						deploymentDescriptorFile,
						deploymentJarItem.getTemplateFile(),
						deploymentJarItem.getAdditionalVariables(),
						DeployOverwriteBehaviour.EXCEPTION); // it should not exist as we deploy into a temporary directory
			}

			// create manifest
			if (createManifest) {
				File manifestFile = new File(tmpDir, manifestFileRelative.getPath());
				if (!manifestFile.getParentFile().mkdirs())
					throw new IOException("Could not create META-INF directory: " + manifestFile.getParentFile());
	
				Manifest manifest = new Manifest();
				manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
				manifest.getMainAttributes().putValue("Created-By", "JFire - http://www.jfire.org");
				FileOutputStream out = new FileOutputStream(manifestFile);
				try {
					manifest.write(out);
				} finally {
					out.close();
				}
			} // if (createManifest) {

			File deploymentDirectory = deploymentJar.getParentFile();
			if (!deploymentDirectory.exists()) {
				logger.info("deploymentDirectory does not exist. Creating it: " + deploymentDirectory.getAbsolutePath());
				if (!deploymentDirectory.mkdirs())
					logger.error("Creating deploymentDirectory failed: " + deploymentDirectory.getAbsolutePath());
			}

			if (deploymentJar.exists()) {
				logger.warn("deploymentJar already exists. Replacing it: " + deploymentJar.getAbsolutePath());
				if (!deploymentJar.delete())
					throw new IOException("Deleting deploymentJar failed: " + deploymentJar.getAbsolutePath());
			}

			IOUtil.zipFolder(deploymentJar, tmpDir);
		} finally {
			IOUtil.deleteDirectoryRecursively(tmpDir);
		}
	}

	/**
	 * @param organisationID The organisation for which a new deployment-descriptor is created.
	 * @param deploymentDescriptorFile The deployment-descriptor-file (relative recommended) that shall be created. The parent-directories are implicitely created.
	 *		If this is relative, it will be created inside the deploy-directory of the jee server (i.e. within a subdirectory, if it contains a path, and as sibling
	 *		to JFire.last).
	 * @param templateFile The template file.
	 * @param additionalVariables Additional variables that shall be available besides the default variables. They override default values, if they contain colliding keys.
	 * @param deployOverwriteBehaviour TODO
	 * @throws IOException If writing/reading fails.
	 */
	public void createDeploymentDescriptor(
			String organisationID, File deploymentDescriptorFile, File templateFile, Map<String, String> additionalVariables, DeployOverwriteBehaviour deployOverwriteBehaviour)
	throws IOException
	{
		JFireServerConfigModule cfMod = mcf.getConfigModule();
		DatabaseCf dbCf = cfMod.getDatabase();

//		if (deploymentDescriptorFile.isAbsolute()) // this method is used by createDeploymentJar with an absolute file, hence we cannot warn here.
//			logger.warn("deploymentDescriptorFile should not be an absolute file: " + deploymentDescriptorFile.getPath(), new IllegalArgumentException("deploymentDescriptorFile should not be an absolute file: " + deploymentDescriptorFile.getPath()));

		if (!deploymentDescriptorFile.isAbsolute()) {
			deploymentDescriptorFile = new File(
					new File(cfMod.getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(),
					deploymentDescriptorFile.getPath());
		}

		if (deploymentDescriptorFile.exists()) {
			switch (deployOverwriteBehaviour) {
				case EXCEPTION:
					throw new DeployedFileAlreadyExistsException(deploymentDescriptorFile);
				case KEEP:
					logger.warn("File " + deploymentDescriptorFile + " already exists. Will not change anything!");
					return;
				case OVERWRITE:
					logger.warn("File " + deploymentDescriptorFile + " already exists. Will overwrite this file!");
					break;
				default:
					throw new IllegalStateException("Unknown deployOverwriteBehaviour: " + deployOverwriteBehaviour);
			}
		}

//		String organisationID_simpleChars = organisationID.replace('.', '_');
//
//		// generate databaseName
//		StringBuffer databaseNameSB = new StringBuffer();
//		databaseNameSB.append(dbCf.getDatabasePrefix());
//		databaseNameSB.append(organisationID_simpleChars);
//		databaseNameSB.append(dbCf.getDatabaseSuffix());
//		String databaseName = databaseNameSB.toString();
		String databaseName = createDatabaseName(organisationID, true);

		// get jdbc url
		String dbURL = dbCf.getDatabaseURL(databaseName);
		String datasourceJNDIName_relative = OrganisationCf.DATASOURCE_PREFIX_RELATIVE + organisationID;
		String datasourceJNDIName_absolute = OrganisationCf.DATASOURCE_PREFIX_ABSOLUTE + organisationID;
		String jdoPersistenceManagerFactoryJNDIName_relative = OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_RELATIVE + organisationID;
		String jdoPersistenceManagerFactoryJNDIName_absolute = OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID;

		Map<String, String> variables = new HashMap<String, String>();
		variables.put("organisationID", organisationID);
//		variables.put("datasourceJNDIName_relative", datasourceJNDIName_relative);
//		variables.put("datasourceJNDIName_absolute", datasourceJNDIName_absolute);
		variables.put("datasourceJNDIName_relative_noTx", datasourceJNDIName_relative + "/no-tx");
		variables.put("datasourceJNDIName_absolute_noTx", datasourceJNDIName_absolute + "/no-tx");
		variables.put("datasourceJNDIName_relative_localTx", datasourceJNDIName_relative + "/local-tx");
		variables.put("datasourceJNDIName_absolute_localTx", datasourceJNDIName_absolute + "/local-tx");
		variables.put("datasourceJNDIName_relative_xa", datasourceJNDIName_relative + "/xa");
		variables.put("datasourceJNDIName_absolute_xa", datasourceJNDIName_absolute + "/xa");
		variables.put("datasourceMetadataTypeMapping", dbCf.getDatasourceMetadataTypeMapping());
		variables.put("jdoPersistenceManagerFactoryJNDIName_relative", jdoPersistenceManagerFactoryJNDIName_relative);
		variables.put("jdoPersistenceManagerFactoryJNDIName_absolute", jdoPersistenceManagerFactoryJNDIName_absolute);
//		variables.put("databaseDriverName", dbCf.getDatabaseDriverName());
		variables.put("databaseDriverName_noTx", dbCf.getDatabaseDriverName_noTx());
		variables.put("databaseDriverName_localTx", dbCf.getDatabaseDriverName_localTx());
		variables.put("databaseDriverName_xa", dbCf.getDatabaseDriverName_xa());
		variables.put("databaseURL", dbURL);
		variables.put("databaseName", databaseName);
		variables.put("databaseUserName", dbCf.getDatabaseUserName());
		variables.put("databasePassword", dbCf.getDatabasePassword());

		variables.put("deploymentDescriptorDirectory", deploymentDescriptorFile.getParent());
		variables.put("deploymentDescriptorFileName", deploymentDescriptorFile.getName());

		if (additionalVariables != null)
			variables.putAll(additionalVariables); // we put them afterwards to allow overriding

		_createDeploymentDescriptor(
				deploymentDescriptorFile,
				templateFile,
				variables);
	}

	private static enum ParserExpects {
		NORMAL,
		BRACKET_OPEN,
		VARIABLE,
		BRACKET_CLOSE
	}

	/**
	 * Generate a -ds.xml file (or any other deployment descriptor) from a template.
	 *
	 * @param deploymentDescriptorFile The file (absolute!) that shall be created out of the template.
	 * @param templateFile The template file to use. Must not be <code>null</code>.
	 * @param variables This map defines what variable has to be replaced by what value. The
	 *				key is the variable name (without brackets "{", "}"!) and the value is the
	 *				value for the variable to replace. This must not be <code>null</code>.
	 */
	private void _createDeploymentDescriptor(File deploymentDescriptorFile, File templateFile, Map<String, String> variables)
		throws IOException
	{
		if (!deploymentDescriptorFile.isAbsolute())
			throw new IllegalArgumentException("deploymentDescriptorFile is not absolute: " + deploymentDescriptorFile.getPath());

		logger.info("Creating deploymentDescriptor \""+deploymentDescriptorFile.getAbsolutePath()+"\" from template \""+templateFile.getAbsolutePath()+"\".");
		File deploymentDirectory = deploymentDescriptorFile.getParentFile();

		if (!deploymentDirectory.exists()) {
			logger.info("deploymentDirectory does not exist. Creating it: " + deploymentDirectory.getAbsolutePath());
			if (!deploymentDirectory.mkdirs())
				logger.error("Creating deploymentDirectory failed: " + deploymentDirectory.getAbsolutePath());
		}

		// Create and configure StreamTokenizer to read template file.
		FileReader fr = new FileReader(templateFile);
		try {
			StreamTokenizer stk = new StreamTokenizer(fr);
			stk.resetSyntax();
			stk.wordChars(0, Integer.MAX_VALUE);
			stk.ordinaryChar('$');
			stk.ordinaryChar('{');
			stk.ordinaryChar('}');
			stk.ordinaryChar('\n');

			// Create FileWriter
			FileWriter fw = new FileWriter(deploymentDescriptorFile);
			try {

				// Read, parse and replace variables from template and write to FileWriter fw.
				String variableName = null;
				StringBuffer tmpBuf = new StringBuffer();
				ParserExpects parserExpects = ParserExpects.NORMAL;
				while (stk.nextToken() != StreamTokenizer.TT_EOF) {
					String stringToWrite = null;

					if (stk.ttype == StreamTokenizer.TT_WORD) {
						switch (parserExpects) {
							case VARIABLE:
								parserExpects = ParserExpects.BRACKET_CLOSE;
								variableName = stk.sval;
								tmpBuf.append(variableName);
							break;
							case NORMAL:
								stringToWrite = stk.sval;
							break;
							default:
								parserExpects = ParserExpects.NORMAL;
								stringToWrite = tmpBuf.toString() + stk.sval;
								tmpBuf.setLength(0);
						}
					}
					else if (stk.ttype == '\n') {
						stringToWrite = new String(new char[] { (char)stk.ttype });

						// These chars are not valid within a variable, so we reset the variable parsing, if we're currently parsing one.
						// This helps keeping the tmpBuf small (to check for rowbreaks is not really necessary).
						if (parserExpects != ParserExpects.NORMAL) {
							parserExpects = ParserExpects.NORMAL;
							stringToWrite = tmpBuf.toString() + stringToWrite;
							tmpBuf.setLength(0);
						}
					}
					else if (stk.ttype == '$') {
						if (parserExpects != ParserExpects.NORMAL) {
							stringToWrite = tmpBuf.toString();
							tmpBuf.setLength(0);
						}
						tmpBuf.append((char)stk.ttype);
						parserExpects = ParserExpects.BRACKET_OPEN;
					}
					else if (stk.ttype == '{') {
						switch (parserExpects) {
							case NORMAL:
								stringToWrite = new String(new char[] { (char)stk.ttype });
							break;
							case BRACKET_OPEN:
								tmpBuf.append((char)stk.ttype);
								parserExpects = ParserExpects.VARIABLE;
							break;
							default:
								parserExpects = ParserExpects.NORMAL;
								stringToWrite = tmpBuf.toString() + (char)stk.ttype;
								tmpBuf.setLength(0);
						}
					}
					else if (stk.ttype == '}') {
						switch (parserExpects) {
							case NORMAL:
								stringToWrite = new String(new char[] { (char)stk.ttype });
							break;
							case BRACKET_CLOSE:
								parserExpects = ParserExpects.NORMAL;
								tmpBuf.append((char)stk.ttype);

								if (variableName == null)
									throw new IllegalStateException("variableName is null!!!");

								stringToWrite = variables.get(variableName);
								if (stringToWrite == null) {
									logger.warn("Variable " + tmpBuf.toString() + " occuring in template \"" + templateFile + "\" is unknown!");
									stringToWrite = tmpBuf.toString();
								}
								tmpBuf.setLength(0);
							break;
							default:
								parserExpects = ParserExpects.NORMAL;
								stringToWrite = tmpBuf.toString() + (char)stk.ttype;
								tmpBuf.setLength(0);
						}
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
	}

	public static PersistenceManagerFactory getPersistenceManagerFactory(String organisationID)
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
			throw new RuntimeException(e);
		}
		return pmf;
	}

	public PersistenceManager getPersistenceManager(String organisationID)
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
	private Map<String, String> jfireSecurity_tempUserPasswords = new HashMap<String, String>();

	protected boolean jfireSecurity_checkTempUserPassword(String organisationID, String userID, String password)
	{
		String pw;
		synchronized(jfireSecurity_tempUserPasswords) {
			pw = jfireSecurity_tempUserPasswords.get(userID + '@' + organisationID);
			if (pw == null)
				return false;
		}
		return pw.equals(password);
	}

	protected String jfireSecurity_createTempUserPassword(String organisationID, String userID)
	{
		synchronized(jfireSecurity_tempUserPasswords) {
			String pw = (String) jfireSecurity_tempUserPasswords.get(userID + '@' + organisationID);
			if (pw == null) {
				pw = UserLocal.createPassword(15, 20);
				jfireSecurity_tempUserPasswords.put(userID + '@' + organisationID, pw);
			}
			return pw;
		}
	}

	/**
	 * This Map caches all the roles for all the users. It does NOT expire, because
	 * it relies on that {@link #jfireSecurity_flushCache()} or {@link #jfireSecurity_flushCache(String, String)}
	 * is executed whenever access rights change!
	 *
	 * key: String userID + @ + organisationID<br/>
	 * value: SoftReference of RoleSet roleSet
	 */
	protected Map<String, SoftReference<RoleSet>> jfireSecurity_roleCache = new HashMap<String, SoftReference<RoleSet>>();

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
				SoftReference<RoleSet> ref = jfireSecurity_roleCache.get(userPK);
				if (ref != null)
					roleSet = ref.get();
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
					roleSet.addMember(new SimplePrincipal(User.USERID_SYSTEM)); // ONLY the system user has this role - no real user can get it as its virtual (it is ignored during import)
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
				jfireSecurity_roleCache.put(userPK, new SoftReference<RoleSet>(roleSet));
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

	public List<J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> getJ2eeRemoteServers()
	{
		return Collections.unmodifiableList(j2eeLocalServerCf.getJ2eeRemoteServers());
	}
	public J2eeServerTypeRegistryConfigModule.J2eeRemoteServer getJ2eeRemoteServer(String j2eeServerType)
	{
		return j2eeLocalServerCf.getJ2eeRemoteServer(j2eeServerType);
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

	public boolean isShuttingDown()
	{
		return shuttingDown;
	}
}
