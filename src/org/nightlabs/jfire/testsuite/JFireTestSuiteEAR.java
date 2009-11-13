package org.nightlabs.jfire.testsuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;

/**
 * EAR descriptor for JFireTestSuite.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author marco schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireTestSuiteEAR
{
	public static final String MODULE_NAME = JFireTestSuiteEAR.class.getSimpleName();
	public static final String CONFIG_SYSTEM_PROPERTY = "org.nightlabs.jfire.testsuite.config";
	private static final String DEFAULT_PROPERTIES_FILENAME = "jfireTestSuite.properties";
	private static final Logger logger = Logger.getLogger(JFireTestSuiteEAR.class);

	private static Properties jfireTestSuiteProperties;
	
	protected JFireTestSuiteEAR() {}

	private static File getEARFile()
	{
		JFireServerManager jFireServerManager = JFireServerManagerUtil.getJFireServerManager();
		try {
			File earFile = new File(
						new File(jFireServerManager.getJFireServerConfigModule()
								.getJ2ee().getJ2eeDeployBaseDirectory()
							),
					MODULE_NAME + ".ear"
				);
			return earFile;
		} finally {
			jFireServerManager.close();
		}
	}

	/**
	 * Get the main properties of {@link JFireTestSuite}.
	 * They are located in the file jfireTestSuite.properties in the ear directory.
	 */
	public static synchronized Properties getJFireTestSuiteProperties()
	throws IOException
	{
		if (jfireTestSuiteProperties == null) {
			Properties properties = new Properties();
			loadEARProperties(properties);
			loadUserHomeProperties(properties);
			loadSystemPropertyProperties(properties);

			if (logger.isTraceEnabled()) {
				for (Map.Entry<?, ?> me : properties.entrySet())
					logger.trace("JFire Test Suite configuration: " + me.getKey() + '=' + me.getValue());
			}
			
			jfireTestSuiteProperties = properties;
		}
		return jfireTestSuiteProperties;
	}

	private static void loadSystemPropertyProperties(final Properties properties) throws FileNotFoundException, IOException 
	{
		// try to get the configuration from system property
		String filenameBySystemProperties = System.getProperty(CONFIG_SYSTEM_PROPERTY);
		if(filenameBySystemProperties != null && !filenameBySystemProperties.isEmpty()) {
			File f = new File(filenameBySystemProperties);
			if(f.isFile() && f.canRead()) {
				logger.info("Using JFire Test Suite configuration file from system properties: "+f.getAbsolutePath());
				InputStream in = new FileInputStream(f);
				try {
					properties.load(in);
				} finally {
					in.close();
				}
			} else {
				logger.error("Invalid JFire Test Suite configuration file given in system properties: "+f.getAbsolutePath());
			}
		}
	}

	private static void loadUserHomeProperties(final Properties properties) throws FileNotFoundException, IOException 
	{
		// try to load the file from user home
		File f = new File(System.getProperty("user.home"), DEFAULT_PROPERTIES_FILENAME);
		if(f.isFile() && f.canRead()) {
			logger.info("Using JFire Test Suite configuration file in user home: "+f.getAbsolutePath());
			InputStream in = new FileInputStream(f);
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		} else {
			logger.info("No JFire Test Suite configuration file found in user home ("+DEFAULT_PROPERTIES_FILENAME+")");
		}
	}

	private static void loadEARProperties(final Properties properties) throws FileNotFoundException, IOException 
	{
		// get default values from ear
		File earFile = getEARFile();
		if (earFile.isDirectory()) {
			// the EAR is a directory
			File fileInEAR = new File(earFile, DEFAULT_PROPERTIES_FILENAME);
			if (!fileInEAR.exists())
				throw new FileNotFoundException("The file \"" + DEFAULT_PROPERTIES_FILENAME + "\" does not exist within the EAR directory \"" + earFile.getAbsolutePath() + "\"!");
			logger.info("Using JFire Test Suite configuration file in EAR directory: "+fileInEAR.getAbsolutePath());
			InputStream in = new FileInputStream(fileInEAR);
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		}
		else {
			// the EAR is a file and the properties are a JarEntry (not a real file).
			JarFile earJarFile = new JarFile(earFile);
			JarEntry je = (JarEntry) earJarFile.getEntry(DEFAULT_PROPERTIES_FILENAME);
			if (je == null)
				throw new FileNotFoundException("The file \"" + DEFAULT_PROPERTIES_FILENAME + "\" does not exist within the EAR jar-file \"" + earFile.getAbsolutePath() + "\"!");
			logger.info("Using JFire Test Suite configuration file in EAR JAR: "+earFile.getAbsolutePath()+" "+DEFAULT_PROPERTIES_FILENAME);
			InputStream in = earJarFile.getInputStream(je);
			try {
				properties.load(in);
			} finally {
				in.close();
				earJarFile.close();
			}
		}
	}

	public static Collection<Matcher> getPropertyKeyMatches(final Pattern pattern) throws IOException
	{
		return org.nightlabs.util.Properties.getPropertyKeyMatches(getJFireTestSuiteProperties(), pattern);
	}

	public static Properties getProperties(final String keyPrefix) throws IOException
	{
		return org.nightlabs.util.Properties.getProperties(getJFireTestSuiteProperties(), keyPrefix);
	}
}
