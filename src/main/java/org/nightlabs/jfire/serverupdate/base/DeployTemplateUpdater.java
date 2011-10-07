/**
 * 
 */
package org.nightlabs.jfire.serverupdate.base;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nightlabs.jfire.serverupdate.base.db.JDBCConfiguration;
import org.nightlabs.jfire.serverupdate.base.util.ServerUpdateUtil;
import org.nightlabs.jfire.serverupdate.launcher.Log;
import org.nightlabs.jfire.serverupdate.launcher.ServerUpdateParameters;
import org.nightlabs.jfire.serverupdate.launcher.config.Directory;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author abieber
 *
 */
public class DeployTemplateUpdater {

	private ServerUpdateParameters parameters;
	
	
	
	public DeployTemplateUpdater(ServerUpdateParameters parameters) {
		super();
		this.parameters = parameters;
	}


	public void checkDeploymentFiles() throws SAXException, IOException
	{
		Log.info("====================================================================");
		Log.info("                Checking deployment-files                           ");
		Log.info("====================================================================");

		Set<File> resolvedDeploymentDirs = new TreeSet<File>();
		
		File dataDir = getDataDir();
		if (dataDir == null) {
			// can't do anything here...
			return;
		}
		
		scanDeploymentDirs(resolvedDeploymentDirs, parameters.getConfig().getDeploymentDirectories());

		
		
		for (File deploymentDirectory : resolvedDeploymentDirs) {
			File[] jdoDeploymentDescriptorFileArray = deploymentDirectory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.getName().startsWith("db-"); //db-${organisationID}-ds.xml
				}
			});

			for (File ddFile : jdoDeploymentDescriptorFileArray) {
				Matcher m = Pattern.compile("db-(.*)-ds.xml").matcher(ddFile.getName());
				if (m.matches()) {
					String organisationID = m.group(1);
					JDBCConfiguration jdbcConfig = generateJDBCConfig(ddFile);
					Map<String, String> variables = createVariables(organisationID, jdbcConfig, ddFile);
					
					
					// check persistence.xml
					File persistenceFile = new File(ddFile.getParent(), "persistence-" + organisationID + ".xml");
					File persistenceTemplate = new File(dataDir, "jfire/template/jdo-datanucleus-1.0-persistence.template.xml");
					
					if (persistenceTemplate.exists() && persistenceFile.exists()) {
						File persistenceTempFile = File.createTempFile(organisationID, "persistence.xml");
						IOUtil.replaceTemplateVariables(persistenceTempFile, persistenceTemplate, IOUtil.CHARSET_NAME_UTF_8, variables);
						if (persistenceFile.length() != persistenceTempFile.length()) {
							StringBuilder sb = new StringBuilder();
							sb.append("????????????????????????????????????????????????????????????????????\n");
							sb.append("The template for " + persistenceFile.getName() + " seems to have changed\n");
							sb.append("????????????????????????????????????????????????????????????????????\n");
							sb.append("Should the file be changed to match the current template (y/n)?");
							if (ServerUpdateUtil.prompt(sb.toString(), "y")) {
								IOUtil.copyFile(persistenceTempFile, persistenceFile);
							}
						}
					}
					
					
				}
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
			URL dirURL = directory.getURL();
			File dirFile;
			try {
				dirFile = new File(Util.urlToUri(dirURL));
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Deployment-descriptor search-path is invalid: " + dirURL);
			}
			if (dirFile.isDirectory())
				resolvedDeploymentDirs.add(dirFile);

			if (directory.isRecursive())
				scanDeploymentDirChildren(resolvedDeploymentDirs, dirFile);
		}
	}	
	
	private File getDataDir()
	{
		for (Directory directory : parameters.getConfig().getDeploymentDirectories()) {
			URL dirURL = directory.getURL();
			File dirFile;
			try {
				dirFile = new File(Util.urlToUri(dirURL));
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Deployment-descriptor search-path is invalid: " + dirURL);
			}
			if (dirFile.isDirectory()) {
				File dataDir = new File(dirFile.getParent(), "data");
				if (dataDir.exists() && dataDir.isDirectory())
					return dataDir;
			}
		}
		return null;
	}
	
	// REV Marco: It would be nicer to extract this into a separate class.
	private JDBCConfiguration generateJDBCConfig(File ddFile)
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
		
		return new JDBCConfiguration(
				jndiNameNode.getTextContent(),
				connectionURLNode.getTextContent(),
				driverClassNode.getTextContent(),
				userNameNode.getTextContent(),
				passwordNode.getTextContent());


	}
	
	public static final String DATASOURCE_PREFIX_RELATIVE = "jfire/datasource/";
	public static final String DATASOURCE_PREFIX_ABSOLUTE = "java:/jfire/datasource/";

	public static final String PERSISTENCE_MANAGER_FACTORY_PREFIX_RELATIVE =	"jfire/persistenceManagerFactory/";
	public static final String PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE =	"java:/jfire/persistenceManagerFactory/";
	

	private Map<String, String> createVariables(
			String organisationID, JDBCConfiguration jdbcConfig, File deploymentDescriptorFile) {

		// get jdbc url
		String dbURL = jdbcConfig.getDatabaseURL();
		String datasourceJNDIName_relative = DATASOURCE_PREFIX_RELATIVE + organisationID;
		String datasourceJNDIName_absolute = DATASOURCE_PREFIX_ABSOLUTE + organisationID;
		String jdoPersistenceManagerFactoryJNDIName_relative = PERSISTENCE_MANAGER_FACTORY_PREFIX_RELATIVE + organisationID;
		String jdoPersistenceManagerFactoryJNDIName_absolute = PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID;

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
		variables.put("jdoPersistenceManagerFactoryJNDIName_relative", jdoPersistenceManagerFactoryJNDIName_relative);
		variables.put("jdoPersistenceManagerFactoryJNDIName_absolute", jdoPersistenceManagerFactoryJNDIName_absolute);
//		variables.put("databaseDriverName", dbCf.getDatabaseDriverName());
		variables.put("databaseDriverName_noTx", jdbcConfig.getDriverClass());
		variables.put("databaseDriverName_localTx", jdbcConfig.getDriverClass());
		variables.put("databaseDriverName_xa", jdbcConfig.getDriverClass());
		variables.put("databaseURL", dbURL);
		variables.put("databaseUserName", jdbcConfig.getUserName());
		variables.put("databasePassword", jdbcConfig.getPassword());

//		variables.put("deploymentDescriptorDirectory", deploymentDescriptorFile.getParent());
		// We write a relative path instead - this is much cleaner and allows moving the server to a different directory.
		variables.put("deploymentDescriptorDirectory", deploymentDescriptorFile.getParent());
		variables.put("deploymentDescriptorDirectory_absolute", deploymentDescriptorFile.getParent());
		try {
			variables.put("deploymentDescriptorDirectory_relative", IOUtil.getRelativePath(new File("."), deploymentDescriptorFile.getParent()));
		} catch (IOException e) {
			variables.remove("deploymentDescriptorDirectory_relative");
		}

		variables.put("deploymentDescriptorFileName", deploymentDescriptorFile.getName());
		
		return variables;
	}
}
