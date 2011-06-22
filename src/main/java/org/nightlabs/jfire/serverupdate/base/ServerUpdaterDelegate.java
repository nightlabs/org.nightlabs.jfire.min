package org.nightlabs.jfire.serverupdate.base;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.nightlabs.datastructure.Pair;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jdo.moduleregistry.UpdateHistoryItemSQL;
import org.nightlabs.jfire.serverupdate.base.db.JDBCConfiguration;
import org.nightlabs.jfire.serverupdate.launcher.Log;
import org.nightlabs.jfire.serverupdate.launcher.ServerUpdateClassLoader;
import org.nightlabs.jfire.serverupdate.launcher.ServerUpdateParameters;
import org.nightlabs.jfire.serverupdate.launcher.config.Directory;
import org.nightlabs.util.reflect.ReflectUtil;
import org.nightlabs.util.reflect.ReflectUtil.ResourceFilter;
import org.nightlabs.version.Version;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ServerUpdaterDelegate
{
	/**
	 * These packages definitely contain nothing of interest to the update system
	 * (i.e. UpdateProcedure/DatabaseManager/etc implementations) and are therefore ignored.
	 */
	private static final String[] IGNORED_PACKAGE_REGEX = {
		"java\\..*",
		"javax\\..*",
		"org\\.nightlabs\\.config",
		"org\\.nightlabs\\.concurrent",
		"org\\.nightlabs\\.jfire\\.reporting\\.platform",
		"com\\.thoughtworks\\.xstream\\.?.*",
	};

	private static final Pattern[] IGNORED_PACKAGE_REGEX_COMPILED;
	static {
		IGNORED_PACKAGE_REGEX_COMPILED = new Pattern[IGNORED_PACKAGE_REGEX.length];
		int idx = -1;
		for (String s : IGNORED_PACKAGE_REGEX)
			IGNORED_PACKAGE_REGEX_COMPILED[++idx] = Pattern.compile(s);
	}

	/** All found and successfully instantiated UpdateProcedures **/
	private UpdateProcedureSet updateProcedureSet = new UpdateProcedureSet();
	
//	private Map<Class<? extends UpdateProcedure>, UpdateProcedureDelegate> upClazz2updMap = new HashMap<Class<? extends UpdateProcedure>, UpdateProcedureDelegate>();
	
	//Properties keys
	private Map<String, JDBCConfiguration> jndiName2JDBCConnectionMap = new HashMap<String, JDBCConfiguration>();

	/**
	 * Filled by {@link #analyseModuleVersionUpdates()}. Holds the update from to for each module for each db to udpate.
	 */
	private Map<String, Map<String, Pair<Version, Version>>> neccessaryUpdateSteps = new HashMap<String, Map<String,Pair<Version,Version>>>();
	
	private ServerUpdateParameters parameters;

	public void execute(ServerUpdateParameters parameters)
	throws Throwable
	{
		this.parameters = parameters;
		
		searchDatasourceDeploymentDescriptors();

		searchClasses();
		
		analyseUpdateSteps();

		updateDatabases();
	}

	private void searchDatasourceDeploymentDescriptors() throws SAXException, IOException
	{
		Log.info("====================================================================");
		Log.info("                Searching datasource deployment descriptors         ");
		Log.info("====================================================================");

		Set<File> resolvedDeploymentDirs = new TreeSet<File>();
		scanDeploymentDirs(resolvedDeploymentDirs, parameters.getConfig().getDeploymentDirectories());

		for (File deploymentDirectory : resolvedDeploymentDirs) {
			File[] jdoDeploymentDescriptorFileArray = deploymentDirectory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.getName().startsWith("db-"); //db-${organisationID}-ds.xml
				}
			});

			for (File jdoDDFile : jdoDeploymentDescriptorFileArray) {
				generateLocalTXDatasourceConfiguration(jdoDDFile, jndiName2JDBCConnectionMap);
			}
		}
	}

	private static void scanDeploymentDirChildren(Set<File> resolvedDeploymentDirs, File deploymentDir)
	{
		File[] children = deploymentDir.listFiles();
		if (children == null)
			return;

		for (File child : children) {
			if (child.isDirectory()) {
				resolvedDeploymentDirs.add(child);
				scanDeploymentDirChildren(resolvedDeploymentDirs, child);
			}
		}
	}

	private static void scanDeploymentDirs(Set<File> resolvedDeploymentDirs, Collection<Directory> deploymentDirs)
	{
		for (Directory directory : deploymentDirs) {
			if (directory.getFile().isDirectory())
				resolvedDeploymentDirs.add(directory.getFile());

			if (directory.isRecursive())
				scanDeploymentDirChildren(resolvedDeploymentDirs, directory.getFile());
		}
	}

	private void updateDatabases() {
		
		Log.info("====================================================================");
		Log.info("                        Updating Databases                          ");
		Log.info("====================================================================");

		//For each database server
		for (String jndiName : jndiName2JDBCConnectionMap.keySet()) {
			Log.info("********************************************************************");
			Log.info("*** Updating " + jndiName);
			Map<String, Pair<Version, Version>> moduleUpdateSteps = neccessaryUpdateSteps.get(jndiName);
			JDBCConfiguration configuration = jndiName2JDBCConnectionMap.get(jndiName);
			UpdateContext updateContext = new UpdateContext(configuration);
			Connection connection = updateContext.getConnection();
			try {
				try {
					//For update procedures
					for (UpdateProcedure updateProcedure : updateProcedureSet) {
						// Check if updateProcedure has to run according to the schemaVersion in DB
						Pair<Version, Version> moduleUpdate = moduleUpdateSteps.get(updateProcedure.getModuleID());
						if (moduleUpdate.getFirst().compareTo(updateProcedure.getFromVersion()) > 0) {
							// The UpdateProcedures fromVersion is smaller than currently stored schemaVersion, we do not execute this UpdateProcedure
							continue;
						}
						
						updateProcedure.setUpdateContext(updateContext);
						
						//UpdateHistoryItemSQL
						UpdateHistoryItemSQL updateHistoryItemSQL =
							new UpdateHistoryItemSQL(updateContext.getConnection(), updateProcedure.getModuleID(), updateProcedure.getClass().getName());
	
						if (!updateHistoryItemSQL.isUpdateDone()) {
							if (Log.isDebugEnabled()) {
								Log.debug("====================================================================");
								Log.debug("    Executing Update for " + updateProcedure.getModuleID() + " " + updateProcedure.getFromVersion() + " --> " + updateProcedure.getToVersion());
								Log.debug("====================================================================");
							}
	
							try {
								updateHistoryItemSQL.beginUpdate();
								updateProcedure.run(parameters);
								updateHistoryItemSQL.endUpdate(!parameters.isDryRun() && !parameters.isTryRun());
							} catch (Exception e) {
								e.printStackTrace(System.out);
								break;
							}
						}
					}
					Log.info("*** Updating " + jndiName + " finished");
					Log.info("********************************************************************");
					
				} finally {
					connection.close();
				}
			}
			catch (SQLException e) {
				Log.error("SQLException: " + e.getMessage());
			}
		} //for
	}

	private void analyseUpdateSteps() {
		Log.info("====================================================================");
		Log.info("                        Analysing UpdateSteps                       ");
		Log.info("====================================================================");
		
		analyseModuleVersionUpdates();
		
		//Check for holes and overlaps
		try {
			updateProcedureSet.assertConsistency();
		}
		catch (InconsistentUpdateProceduresException e) {
			Log.error("====================================================================");
			Log.error("Update Terminated - There's an inconsistency in the UpdateProcedureSet");
			Log.error("====================================================================");
			Log.error(e.getMessage());
			System.exit(1);
		}
		
	}

	private void analyseModuleVersionUpdates() {
		Map<String, Version> newlyDeployedShemaVersions = new TreeMap<String, Version>();
		for (String moduleID : updateProcedureSet.getModuleIDs()) {
			UpdateProcedure firstProcedure = updateProcedureSet.subSet(moduleID).first();
			try {
				ModuleMetaData moduleMetaData = ModuleMetaData.createModuleMetaDataFromManifest(moduleID, ((LiquibaseUpdateProcedure)firstProcedure).getChangeLogURL());
				// FIXME: What to do if this fails (returns null)
				newlyDeployedShemaVersions.put(moduleID, moduleMetaData.getSchemaVersionObj());
			} catch (Exception e) {
				throw new IllegalStateException("Could not read schemaVersion for module " + moduleID, e);
			}
		}
		
		try {
			for (String jndiName : jndiName2JDBCConnectionMap.keySet()) {
				Log.info("********************************************************************");
				JDBCConfiguration configuration = jndiName2JDBCConnectionMap.get(jndiName);
				UpdateContext updateContext = new UpdateContext(configuration);
				Connection connection = updateContext.getConnection();
				try {
					Log.info("*** UpdateSteps for " + jndiName);
					for (Map.Entry<String, Version> newlyDeployedVersion : newlyDeployedShemaVersions.entrySet()) {
						ModuleMetaData persistentModuleMetaData = ModuleMetaData.getModuleMetaData(connection, newlyDeployedVersion.getKey());
						// FIXME: What to do if this fails (returns null)
						if (persistentModuleMetaData == null) {
							persistentModuleMetaData = new ModuleMetaData(newlyDeployedVersion.getKey(), new Version(1, 0, 0, 0));
						}
						Map<String, Pair<Version, Version>> moduleUpdates = neccessaryUpdateSteps.get(jndiName);
						if (moduleUpdates == null) {
							moduleUpdates = new HashMap<String, Pair<Version,Version>>();
							neccessaryUpdateSteps.put(jndiName, moduleUpdates);
						}
						moduleUpdates.put(newlyDeployedVersion.getKey(), new Pair<Version, Version>(persistentModuleMetaData.getSchemaVersionObj(), newlyDeployedVersion.getValue()));
						Log.info("*** Module " + newlyDeployedVersion.getKey() + ": from " + persistentModuleMetaData.getSchemaVersionObj() + " to " + newlyDeployedVersion.getValue());						
					}
				} finally {
					connection.close();
				}
				Log.info("********************************************************************");
			}
		} catch (Exception e) {
			throw new IllegalStateException("Computing neccessary UpdateSteps failed", e);
		}
	}

	private void searchClasses()
	throws Throwable
	{
		Log.info("====================================================================");
		Log.info("                Searching UpdateProcedures (ChangeLog-files)        ");
		Log.info("====================================================================");

		ServerUpdateClassLoader classLoader = ServerUpdateClassLoader.sharedInstance();

		Set<String> packageNames = classLoader.getPackageNames();

//		Collection<Class<?>> c;
		Collection<URL> r;
		iteratePackages: for (String packageName : packageNames) {
			
			for (Pattern pattern : IGNORED_PACKAGE_REGEX_COMPILED) {
				if (pattern.matcher(packageName).matches())
					continue iteratePackages;
			}

//			c = Collections.emptySet(); // initialise c, in case the ReflectUtil method fails.
//			try {
//				c = ReflectUtil.listClassesInPackage(packageName, classLoader, false);
//			} catch (Throwable x) {
//				if (x instanceof OutOfMemoryError)
//					throw x;
//
//				if (Log.isDebugEnabled())
//					Log.debug("searchClasses: Error listing classes in package \"" + packageName + "\": " + x);
//			}
//
//			for (Class<?> clazz : c) {
//				// We ignore all abstract classes as they cannot be instantiated (and thus not used), anyway.
//				if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0)
//					continue;
//
//				/****************************************************/
//				/* 				Update Procedures					*/
//				/****************************************************/
//				if (LiquibaseUpdateProcedure.class != clazz && UpdateProcedure.class.isAssignableFrom(clazz)) {
//					Log.info("*** Found UpdateProcedure: " + clazz.getName());
//					@SuppressWarnings("unchecked")
//					Class<? extends UpdateProcedure> updateProcedureClazz = (Class<? extends UpdateProcedure>) clazz;
//					try {
//						UpdateProcedure updateProcedure = (UpdateProcedure)Class.forName(updateProcedureClazz.getName()).newInstance();
//						updateProcedureSet.add(updateProcedure);
//					} catch (Exception e) {
//						e.printStackTrace();
//						throw new RuntimeException("Can not instantiate the update procedure class: " + updateProcedureClazz.getName());
//					}
//				}
//
////				/****************************************************/
////				/* 			 Update Procedure Delegates		        */
////				/****************************************************/
////				if (UpdateProcedureDelegate.class.isAssignableFrom(clazz)) {
////					Log.info("*** Found UpdateProcedureDelegate: " + clazz.getName());
////					@SuppressWarnings("unchecked")
////					Class<? extends UpdateProcedureDelegate> updateProcedureDelegateClass = (Class<? extends UpdateProcedureDelegate>) clazz;
////					try {
////						UpdateProcedureDelegate updateProcedureDelegate = (UpdateProcedureDelegate)Class.forName(updateProcedureDelegateClass.getName()).newInstance();
////						updateProcedureDelegates.add(updateProcedureDelegate);
////					} catch (Exception e) {
////						e.printStackTrace();
////						throw new RuntimeException("Can not instantiate the UpdateProcedureDelegate class: " + updateProcedureDelegateClass.getName());
////					}
////				}
////
////				/****************************************************/
////				/* 				 DatabaseManagers					*/
////				/****************************************************/
////				if (DatabaseManager.class.isAssignableFrom(clazz)) {
////					Log.info("*** Found DatabaseManager: " + clazz.getName());
////					@SuppressWarnings("unchecked")
////					Class<? extends DatabaseManager> databaseManagerClass = (Class<? extends DatabaseManager>) clazz;
////					try {
////						DatabaseManager databaseManager = databaseManagerClass.newInstance();
////						databaseManagers.add(databaseManager);
////					} catch (Exception e) {
////						e.printStackTrace();
////						throw new RuntimeException("Can not instantiate the DatabaseManager class: " + databaseManagerClass.getName());
////					}
////				}
//			}
			
			r = Collections.emptySet(); // initialise c, in case the ReflectUtil method fails.
			try {
				r = ReflectUtil.listResourcesInPackage(packageName, classLoader, new ResourceFilter() {

					@Override
					public boolean accept(URL resourceURL) {
						return resourceURL.getFile().endsWith(".jsu.xml");
					}
				}, false);
			} catch (Throwable x) {
				if (x instanceof OutOfMemoryError)
					throw x;

				if (Log.isDebugEnabled())
					Log.debug("searchClasses: Error listing classes in package \"" + packageName + "\": " + x);
			}
			for (URL updateXML : r) {
				Log.info("*** Found xml-update-change-set: " + updateXML);
				try {
					LiquibaseUpdateProcedure updateProcedure = new LiquibaseUpdateProcedure(updateXML);
					updateProcedureSet.add(updateProcedure);
				} catch (Exception e) {
					Log.error("*** Could not create LiquibaseUpdateProcedure for %s", updateXML);
					e.printStackTrace();
				}
			}
		}
	}

	// REV Marco: It would be nicer to extract this into a separate class.
	private void generateLocalTXDatasourceConfiguration(File ddFile, Map<String, JDBCConfiguration> jndiName2JDBCConnectionMap)
	throws SAXException, IOException
	{
		InputSource inputSource;

		try {
			inputSource = new InputSource(new FileInputStream(ddFile));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Although checked with .exists() file " + ddFile + " does not seem to exist. ", e);
		}

		DOMParser parser = new DOMParser();
		parser.parse(inputSource);
		Document document = parser.getDocument();

		Node localTxDatasourceNode = NLDOMUtil.findElementNode("local-tx-datasource", document.getDocumentElement());
		Node jndiNameNode = NLDOMUtil.findElementNode("jndi-name", localTxDatasourceNode);
		Node connectionURLNode = NLDOMUtil.findElementNode("connection-url", localTxDatasourceNode);
		Node driverClassNode = NLDOMUtil.findElementNode("driver-class", localTxDatasourceNode);
		Node userNameNode = NLDOMUtil.findElementNode("user-name", localTxDatasourceNode);
		Node passwordNode = NLDOMUtil.findElementNode("password", localTxDatasourceNode);

		Log.info("*** Found JDBCConnection with JNDI Name: " + jndiNameNode.getTextContent());
		Log.info("***   connectionURL: " + connectionURLNode.getTextContent());
		Log.info("***   driverClass: " + driverClassNode.getTextContent());
		
		jndiName2JDBCConnectionMap.put(jndiNameNode.getTextContent(), new JDBCConfiguration(
				connectionURLNode.getTextContent(),
				driverClassNode.getTextContent(),
				userNameNode.getTextContent(),
				passwordNode.getTextContent()));

	}
}
