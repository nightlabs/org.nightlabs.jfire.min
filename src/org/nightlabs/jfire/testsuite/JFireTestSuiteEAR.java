/**
 *
 */
package org.nightlabs.jfire.testsuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
 */
public class JFireTestSuiteEAR
{
	private static final Logger logger = Logger.getLogger(JFireTestSuiteEAR.class);
	public static final String MODULE_NAME = "JFireTestSuiteEAR";

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

	private static Properties jfireTestSuiteProperties;

	private static Properties readJFireTestSuitePropertiesFile(File file) throws IOException
	{
		if (logger.isDebugEnabled())
			logger.debug("readJFireTestSuitePropertiesFile: file=" + file.getAbsolutePath());

		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(file);
		try {
			properties.load(in);
		} finally {
			in.close();
		}

		if (logger.isTraceEnabled()) {
			for (Map.Entry<?, ?> me : properties.entrySet())
				logger.trace("readJFireTestSuitePropertiesFile: " + me.getKey() + '=' + me.getValue());
		}

		return properties;
	}

	private static List<File> getIncludeFilesFromProperties(Properties properties)
	{
		List<File> includeFiles = new LinkedList<File>();
		Properties includeProps = getProperties(properties, "include.");
		SortedSet<String> includePropKeys = new TreeSet<String>();
		for (Object key : includeProps.keySet())
			includePropKeys.add((String) key);

		for (String includePropKey : includePropKeys) {
			String includeValue = includeProps.getProperty(includePropKey);

			String includeFileName = includeValue;
			for (Map.Entry<?, ?> me : System.getProperties().entrySet()) {
				String systemPropertyKey = (String) me.getKey();
				String systemPropertyValue = (String) me.getValue();
				includeFileName = includeFileName.replaceAll(Pattern.quote("${" + systemPropertyKey + "}"), systemPropertyValue);
			}

			if (logger.isDebugEnabled())
				logger.debug("getIncludeFilesFromProperties: includeKey=include." + includePropKey + " includeValue=" + includeValue + " includeFileName=" + includeFileName);

			includeFiles.add(new File(includeFileName));
		}
		return includeFiles;
	}

	private static void readJFireTestSuitePropertiesRecursively(
			Properties jfireTestSuiteProperties,
			File propertiesFile, // null in case of EAR-file, then the following arg is assigned
			String propertiesInputStreamName, InputStream propertiesInputStream, // null in case of EAR-directory, then the previous argument is assigned
			Set<File> includeFilesProcessed
	)
	throws IOException
	{
		// read the properties file
		Properties props;
		if (propertiesFile != null) {
			propertiesInputStreamName = propertiesFile.getAbsolutePath();
			props = readJFireTestSuitePropertiesFile(propertiesFile);
		}
		else {
			props = new Properties();
			props.load(propertiesInputStream);
		}

		// put all new (included) values and overwrite the previously defined ones
		jfireTestSuiteProperties.putAll(props);

		// recursively process further include files which are defined in the include file we just read
		for (File includeFile : getIncludeFilesFromProperties(props)) {
			if (!includeFile.exists()) {
				logger.info("readJFireTestSuitePropertiesRecursively: includeFile \"" + includeFile.getAbsolutePath() + "\" defined in propertiesFile \"" + propertiesInputStreamName + "\" does not exist!");
				continue;
			}

			if (!includeFilesProcessed.add(includeFile)) {
				logger.warn("readJFireTestSuitePropertiesRecursively: includeFile \"" + includeFile.getAbsolutePath() + "\" defined in propertiesFile \"" + propertiesInputStreamName + "\" has already been processed! Skipping in order to prevent circular include loops!");
				continue;
			}

			readJFireTestSuitePropertiesRecursively(jfireTestSuiteProperties, includeFile, null, null, includeFilesProcessed);
		}
	}

	/**
	 * Get the main properties of {@link JFireTestSuite}.
	 * They are located in the file jfireTestSuite.properties in the ear directory.
	 */
	public static Properties getJFireTestSuiteProperties()
	throws IOException
	{
		if (jfireTestSuiteProperties == null) {
			synchronized (JFireTestSuiteEAR.class) {
				JarFile earJarFile = null;
				try {
					if (jfireTestSuiteProperties == null) {
						String jfireTestSuitePropertiesRelativePath = "jfireTestSuite.properties";

						File jfireTestSuitePropertiesRealFile = null; // only assigned if the EAR is a directory and the properties thus a real file.
						InputStream jfireTestSuitePropertiesInputStream = null; // only assigned if the EAR is a JAR file.
						String jfireTestSuitePropertiesInputStreamName = null;

						File earFile = getEARFile();
						if (earFile.isDirectory()) {
							File fileInEAR = new File(earFile, jfireTestSuitePropertiesRelativePath);
							if (!fileInEAR.exists())
								throw new FileNotFoundException("The file \"" + jfireTestSuitePropertiesRelativePath + "\" does not exist within the EAR directory \"" + earFile.getAbsolutePath() + "\"!");

							jfireTestSuitePropertiesRealFile = fileInEAR;
						}
						else {
							// the EAR is a file and the properties are a JarEntry (not a real file).
							earJarFile = new JarFile(earFile);
							JarEntry je = (JarEntry) earJarFile.getEntry(jfireTestSuitePropertiesRelativePath);
							if (je == null)
								throw new FileNotFoundException("The file \"" + jfireTestSuitePropertiesRelativePath + "\" does not exist within the EAR jar-file \"" + earFile.getAbsolutePath() + "\"!");

							jfireTestSuitePropertiesInputStream = earJarFile.getInputStream(je);
							jfireTestSuitePropertiesInputStreamName = earFile.getAbsolutePath() + '/' + je.getName();
						}

						Properties newJFireTestSuiteProps = new Properties();

						// keep track of which files have already been processed in order to prevent processing them twice
						Set<File> includeFilesProcessed = new HashSet<File>();

						readJFireTestSuitePropertiesRecursively(
								newJFireTestSuiteProps,
								jfireTestSuitePropertiesRealFile, // is null, if it is an EAR-file (i.e. only assigned in case of EAR-directory).
								jfireTestSuitePropertiesInputStreamName,
								jfireTestSuitePropertiesInputStream, // is null, if it is an EAR-directory (i.e. only assigned in case of EAR-JAR-file).
								includeFilesProcessed);

						jfireTestSuiteProperties = newJFireTestSuiteProps;
					} // if (jfireTestSuiteProperties == null) {

				} finally {
					if (earJarFile != null)
						earJarFile.close();
				}
			} // synchronized (JFireTestSuiteEAR.class) {

			if (logger.isTraceEnabled()) {
				for (Map.Entry<?, ?> me : jfireTestSuiteProperties.entrySet())
					logger.trace("getJFireTestSuiteProperties: " + me.getKey() + '=' + me.getValue());
			}
		} // if (jfireTestSuiteProperties == null) {
		return jfireTestSuiteProperties;
	}

	public static Collection<Matcher> getPropertyKeyMatches(Properties properties, Pattern pattern)
	{
		Collection<Matcher> matches = new ArrayList<Matcher>();
		for (Iterator<?> iter = properties.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			Matcher m = pattern.matcher(key);
			if(m.matches())
				matches.add(m);
		}
		return matches;
	}

	public static Properties getProperties(Properties properties, String keyPrefix)
	{
		Properties newProperties = new Properties();
		Collection<Matcher> matches = getPropertyKeyMatches(properties, Pattern.compile("^"+Pattern.quote(keyPrefix)+"(.*)$"));
		for (Matcher m : matches)
			newProperties.put(m.group(1), properties.get(m.group(0)));
		return newProperties;
	}


}
