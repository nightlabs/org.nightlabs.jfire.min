package org.nightlabs.jfire.server.data.dir.jboss;

import java.io.File;

import org.jboss.system.ServiceMBeanSupport;

public class JFireServerDataDirectoryService
extends ServiceMBeanSupport
implements JFireServerDataDirectoryServiceMBean
{
	@Override
	protected void startService() throws Exception {
		super.startService();
		String propertyKeyJBossServerDataDir = "jboss.server.data.dir";
		String jbossServerDataDir = System.getProperty(propertyKeyJBossServerDataDir);
		if (jbossServerDataDir == null)
			throw new IllegalStateException("System property \"" + propertyKeyJBossServerDataDir + "\" is not set!");

		File jbossServerDataDirFile = new File(jbossServerDataDir);
		File jfireServerDataDirFile = new File(jbossServerDataDirFile, "jfire");

		System.setProperty(PROPERTY_KEY_JFIRE_DATA_DIRECTORY, jfireServerDataDirFile.getAbsolutePath());
	}

	@Override
	protected void stopService() throws Exception {
		System.getProperties().remove(PROPERTY_KEY_JFIRE_DATA_DIRECTORY);
		super.stopService();
	}

}
