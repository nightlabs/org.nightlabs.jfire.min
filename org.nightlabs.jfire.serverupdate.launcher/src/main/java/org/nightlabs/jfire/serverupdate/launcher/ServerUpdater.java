package org.nightlabs.jfire.serverupdate.launcher;

import java.lang.reflect.Method;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.nightlabs.classloader.url.NestedURLClassLoader;
import org.nightlabs.jfire.serverupdate.launcher.config.Directory;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ServerUpdater
{
	private static final String SERVER_UPDATER_DELEGATE_CLASS = "org.nightlabs.jfire.serverupdate.base.ServerUpdaterDelegate";

	public static void main(String[] args)
	throws Throwable
	{
		
		ServerUpdateParameters parameters = new ServerUpdateParameters();
		
		CmdLineParser parser = new CmdLineParser(parameters);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// handling of wrong arguments
			parser.printUsage(System.err);
			System.err.flush();
			System.exit(2);
			System.err.println();
		}
		
		if (parameters.isShowHelp()) {
			parser.printUsage(System.out);
			System.exit(0);
		}
		
		if (Log.isDebugEnabled()) {
			parameters.logValues(Log.DEBUG);
		}
		
		/*****************************************************************
							Loading the class loader
		 *****************************************************************/
		NestedURLClassLoader cl = new NestedURLClassLoader(ServerUpdater.class.getClassLoader());
		for (Directory dir : parameters.getConfig().getClasspath()) {
			cl.addURL(dir.getURL(), dir.isRecursive());
		}
		Thread.currentThread().setContextClassLoader(cl);

		Class<?> clazz;
		// The class to be loaded should definitely not be available to the current class loader.
		clazz = cl.loadClass(SERVER_UPDATER_DELEGATE_CLASS);

		// And thus, we ensure it now.
		if (cl != clazz.getClassLoader())
			throw new IllegalStateException(
					String.format(
							"The class %s should have been loaded by our ServerUpdateClassLoader, but it is loaded by %s!!!",
							SERVER_UPDATER_DELEGATE_CLASS,
							clazz.getClassLoader()
					)
			);

		Object instance = clazz.newInstance();
		Method m = clazz.getDeclaredMethod("execute", ServerUpdateParameters.class);
		m.invoke(instance, parameters);
	}
}