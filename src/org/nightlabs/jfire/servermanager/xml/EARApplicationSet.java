package org.nightlabs.jfire.servermanager.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EARApplicationSet
{
	private static Collection<EARApplication> scanDeployDirectory(File deployDirectory, EARModuleType ... moduleTypes)
	throws XMLReadException
	{
		List<EARApplication> ears = new ArrayList<EARApplication>();
		for (File f : deployDirectory.listFiles()) {
			if (f.getName().endsWith(".ear")) {
				EARApplication earApplicationMan = new EARApplication(f, moduleTypes);
				ears.add(earApplicationMan);
			}
			else if (f.isDirectory()) {
				Collection<EARApplication> recursiveEars = scanDeployDirectory(f, moduleTypes);
				ears.addAll(recursiveEars);
			}
		}
		return ears;
	}

	private EARModuleType[] moduleTypes;

	private Collection<EARApplication> earApplications;

	public EARApplicationSet(File deployDirectory, EARModuleType ... moduleTypes) throws XMLReadException {
		this.earApplications = scanDeployDirectory(deployDirectory, moduleTypes);
	}

	public EARModuleType[] getModuleTypes() {
		return moduleTypes;
	}

	public Collection<EARApplication> getEarApplications() {
		return earApplications;
	}

	public void handleJarEntries(String[] jarEntryNames, JarEntryHandler[] jarEntryHandlers)
	{
		for (EARApplication earApplication : earApplications) {
			earApplication.handleJarEntries(jarEntryNames, jarEntryHandlers);
		}
	}
}
