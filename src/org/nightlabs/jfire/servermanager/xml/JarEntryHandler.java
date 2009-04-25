package org.nightlabs.jfire.servermanager.xml;

import java.io.InputStream;

public interface JarEntryHandler {
	void handleJarEntry(EARApplication ear, String jarName, InputStream in) throws Exception;
}
