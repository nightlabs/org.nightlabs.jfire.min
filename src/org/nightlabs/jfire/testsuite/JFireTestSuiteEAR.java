/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import java.io.File;

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

}
