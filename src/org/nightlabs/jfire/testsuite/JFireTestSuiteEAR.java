/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;

/**
 * EAR descriptor for JFireTestSuite.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFireTestSuiteEAR {

	protected JFireTestSuiteEAR() {}
	
	public static final String MODULE_NAME = "JFireTestSuite"; 
	
	public static File getEARDir() 
	throws ModuleException 
	{
		JFireServerManager jFireServerManager;
		try {
			jFireServerManager = JFireServerManagerUtil.getJFireServerManager();
		} catch (Exception e) {
			throw new ModuleException("Could not get JFireServerManager!", e);
		}
		try {
			File earDir = new File(
						new File(jFireServerManager.getJFireServerConfigModule()
								.getJ2ee().getJ2eeDeployBaseDirectory()
							),
					MODULE_NAME + ".ear"
				);
			return earDir;
		} finally {
			jFireServerManager.close();
		}
	}

	private static Properties jfireTestSuiteProperties;
	
	/**
	 * Get the main properties of {@link JFireTestSuite}.
	 * They are located in the file jfireTestSuite.properties in the ear directory.
	 */
	public static Properties getJFireTestSuiteProperties() throws ModuleException, IOException {
		if (jfireTestSuiteProperties == null) {
			synchronized (JFireTestSuiteEAR.class) {
				if (jfireTestSuiteProperties == null)
					// find the listener and configure them
					jfireTestSuiteProperties = new Properties();
				FileInputStream in = new FileInputStream(new File(JFireTestSuiteEAR.getEARDir(), "jfireTestSuite.properties"));
				try {
					jfireTestSuiteProperties.load(in);
				} finally {
					in.close();
				}
			}
		}
		return jfireTestSuiteProperties;
	}
	
	public static Collection<Matcher> getPropertyKeyMatches(Properties properties, Pattern pattern)
	{
		Collection<Matcher> matches = new ArrayList<Matcher>();
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
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
