package org.nightlabs.jfire.server.data.dir;

import java.io.File;

public class JFireServerDataDirectory
{
	private JFireServerDataDirectory() { }

	public static final String PROPERTY_KEY_JFIRE_DATA_DIRECTORY = "jfire.server.data.dir";

	public static File getJFireServerDataDirFile()
	{
		String dataDirString = System.getProperty(PROPERTY_KEY_JFIRE_DATA_DIRECTORY);
		if (dataDirString == null)
			throw new IllegalStateException("System property \"" + PROPERTY_KEY_JFIRE_DATA_DIRECTORY + "\" is not set!");

		return new File(dataDirString);
	}
}
