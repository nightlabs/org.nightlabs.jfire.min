package org.nightlabs.jfire.installer;

import java.io.File;

import org.nightlabs.installer.base.InstallationException;

public class EnvironmentHelper
extends org.nightlabs.installer.EnvironmentHelper
{
	public static File getEnclosingJar()
	throws InstallationException
	{
		String className = "com.simontuffs.onejar.Boot"; // this class is top-level, i.e. not nested //$NON-NLS-1$
		Class<?> notNestedClass;
		try {
			notNestedClass = Class.forName(className);
		} catch (ClassNotFoundException x) {
			throw new InstallationException("The class \"" + className + "\" could not be found! It should be directly (not nested) in the top-JAR!", x); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return org.nightlabs.installer.EnvironmentHelper.getEnclosingJar(notNestedClass);
	}
}
