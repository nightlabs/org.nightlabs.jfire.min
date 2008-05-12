package org.nightlabs.jfire.jboss.serverconfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Launcher for execution of the {@link org.nightlabs.jfire.serverconfigurator.ServerConfigurator} from the command line.
 * <p>
 * Since the JBoss does not start JFire due to configuration errors before the {@link ServerConfiguratorJBoss} has been
 * executed, it is necessary to launch the configurator already before the server is started the first time.
 * If the JFire-server is installed via the installer, this is no problem, since the installer already executes
 * the server-configurator. But in a development environment or when the jboss is installed from the zip file,
 * it is necessary to execute the server-configurator manually.
 * </p>
 * <p>
 * There are the scripts <i>init.sh</i> and <i>init.bat</i> in the project JBossLibs, which are deployed to
 * <i>${jboss}/bin/</i>. These scripts must be executed in JBoss' bin directory and simply launch the main
 * method of this class.
 * </p>
 * <p>
 * This <code>Launcher</code> scans the server's <i>lib</i> and <i>deploy</i> directories recursively and creates
 * a classpath from all JARs found. It then creates a {@link URLClassLoader} for this classpath, loads the class
 * {@link org.nightlabs.jfire.jboss.serverconfigurator.internal.InternalLauncher} via this new class-loader and then instantiates it and calls
 * {@link org.nightlabs.jfire.jboss.serverconfigurator.internal.InternalLauncher#run()}.
 * This way, the {@link org.nightlabs.jfire.jboss.serverconfigurator.internal.InternalLauncher} can use all classes deployed in the JBoss.
 * </p>
 * <p>
 * Nested JARs within the JARs found are extracted into temporary files (which are deleted on JVM exit) and added
 * to the classpath as well. This process is done recursively, so that JARs can be nested in multiple levels.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class Launcher
{
	private static String[] jarFileExtensions = { "jar", "rar", "war", "ear", "zip" };
	private static Set<String> jarFileExtensionSet = null;

	private static boolean isJarBasedOnExtension(String fileName)
	{
		if (jarFileExtensionSet == null) {
			Set<String> s = new HashSet<String>(jarFileExtensions.length);
			for (String jarFileExtension : jarFileExtensions)
				s.add(jarFileExtension);

			jarFileExtensionSet = s;
		}

		int lastDot = fileName.lastIndexOf('.');
		if (lastDot < 0)
			return false;

		String fileExtension = fileName.substring(lastDot + 1);
		return jarFileExtensionSet.contains(fileExtension);
	}

	/**
	 * Transfer data between streams.
	 * @param in The input stream
	 * @param out The output stream
	 * @param inputOffset How many bytes to skip before transferring
	 * @param inputLen How many bytes to transfer. -1 = all
	 * @return The number of bytes transferred
	 * @throws IOException if an error occurs.
	 */
	public static long transferStreamData(java.io.InputStream in, java.io.OutputStream out, long inputOffset, long inputLen)
	throws java.io.IOException
	{
		int bytesRead;
		int transferred = 0;
		byte[] buf = new byte[4096];
	
		//skip offset
		if(inputOffset > 0)
			if(in.skip(inputOffset) != inputOffset)
				throw new IOException("Input skip failed (offset "+inputOffset+")");
	
		while (true) {
			if(inputLen >= 0)
				bytesRead = in.read(buf, 0, (int)Math.min(buf.length, inputLen-transferred));
			else
				bytesRead = in.read(buf);
	
			if (bytesRead <= 0)
				break;
	
			out.write(buf, 0, bytesRead);
	
			transferred += bytesRead;
	
			if(inputLen >= 0 && transferred >= inputLen)
				break;
		}
		out.flush();
		return transferred;
	}
	
	/**
	 * Transfer all available data from an {@link InputStream} to an {@link OutputStream}.
	 * <p>
	 * This is a convenience method for <code>transferStreamData(in, out, 0, -1)</code>
	 * @param in The stream to read from
	 * @param out The stream to write to
	 * @return The number of bytes transferred
	 * @throws IOException In case of an error
	 */
	public static long transferStreamData(java.io.InputStream in, java.io.OutputStream out)
	throws java.io.IOException
	{
		return transferStreamData(in, out, 0, -1);
	}

	public static void main(String[] args) throws Exception
	{
		Launcher launcher = new Launcher();
		launcher.run();
	}

	/**
	 * Some regular expressions which cause directories & jars to be excluded, if at least one of them matches the path.
	 */
	private static final String[] excludes = {
		".*\\/JFireReporting\\.ear\\/birt\\/plugins\\/org\\.apache\\.derby\\.core_10\\.1\\.2\\.1.*"
	};

	private static final Pattern[] excludePatterns = createExcludePatterns();
	private static Pattern[] createExcludePatterns() {
		Pattern[] p = new Pattern[excludes.length];
		int idx = 0;
		for (String exclude : excludes)
			p[idx++] = Pattern.compile(exclude);

		return p;
	}

	private static boolean isExcluded(File directoryOrJarFile) {
		for (Pattern exclude : excludePatterns) {
			if (exclude.matcher(directoryOrJarFile.getAbsolutePath()).matches())
				return true;
		}
		return false;
	}

	private void scan(Set<URL> classLoaderURLs, JarFile jarFile) throws IOException
	{
		for (Enumeration<JarEntry> jarEntryEnum = jarFile.entries(); jarEntryEnum.hasMoreElements(); ) {
			JarEntry jarEntry = jarEntryEnum.nextElement();

			if (isJarBasedOnExtension(jarEntry.getName())) {
				int lastSlash = jarEntry.getName().lastIndexOf('/'); // this is always a slash - no matter if it's windows or linux. marco.
				String simpleName = lastSlash < 0 ? jarEntry.getName() : jarEntry.getName().substring(lastSlash + 1);
				File tempFile = File.createTempFile("__" + simpleName + '.', ".tmp"); // with the "__" and ".", the prefix is always long enough (minimum are 3 characters)
				tempFile.deleteOnExit();
				OutputStream out = new FileOutputStream(tempFile);
				try {
					InputStream in = jarFile.getInputStream(jarEntry);
					try {
						transferStreamData(in, out);
					} finally {
						in.close();
					}
				} finally {
					out.close();
				}

				JarFile jf = null;
				try {
					try {
						jf = new JarFile(tempFile);
					} catch (IOException x) {
						// ignore - it's probably not a JAR
					}

					if (jf != null) {
						classLoaderURLs.add(tempFile.toURI().toURL());

						// A jar might contain nested JARs. Since we cannot directly put them into the class-loader, we extract them.
						scan(classLoaderURLs, jf);
					} // if (isJAR) {
				} finally {
					if (jf != null)
						jf.close();
				}
			} // if (isJarBasedOnExtension(jarEntry.getName())) {
		}
	}

	private void scan(Set<URL> classLoaderURLs, File directory) throws IOException
	{
		if (isExcluded(directory))
			return;

		for (File f : directory.listFiles()) {
			if (f.isDirectory())
				scan(classLoaderURLs, f);
			else {
				if (isJarBasedOnExtension(f.getName()) && !isExcluded(f)) {
					JarFile jf = null;
					try {
						try {
							jf = new JarFile(f);
						} catch (IOException x) {
							// ignore - it's probably not a JAR
						}

						if (jf != null) {
							classLoaderURLs.add(f.toURI().toURL());

							// A jar might contain nested JARs. Since we cannot directly put them into the class-loader, we extract them.
							scan(classLoaderURLs, jf);
						} // if (isJAR) {
					} finally {
						if (jf != null)
							jf.close();
					}
				} // if (isJarBasedOnExtension(f.getName())) {
			} // if (!f.isDirectory()) {
		} // for (File f : directory.listFiles()) {
	}

	public void run() throws Exception
	{
//		System.out.println("Launcher.run: Sleeping 30 sec. ");
//		Thread.sleep(30000); // for debugging - so we have time to hook the debugger

		System.out.println("Launcher.run: getClass().getClassLoader(): " + getClass().getClassLoader());

		Set<URL> classLoaderURLs = new HashSet<URL>();

		File serverDefaultFolder = new File(new File(new File("..").getAbsoluteFile(), "server"), "default");
		File serverLibFolder = new File(serverDefaultFolder, "lib");
		File serverDeployFolder = new File(serverDefaultFolder, "deploy");
		File jfireLastFolder = new File(serverDeployFolder, "JFire.last");

		if (!jfireLastFolder.exists()) {
			System.err.println("The folder JFire.last cannot be found! Either it does not exist or you started the Launcher in the wrong directory! The Launcher must be started from the JBoss server's bin directory. This path does not exist: " + jfireLastFolder.getAbsolutePath());
			throw new IllegalStateException("Directory does not exist: " + jfireLastFolder.getAbsolutePath());
		}

		scan(classLoaderURLs, serverLibFolder);
		scan(classLoaderURLs, serverDeployFolder);

		System.out.println("Launcher.run: collected " + classLoaderURLs.size() + " JARs for classpath. Starting internal launcher.");

		// Create a new classLoader which has no parent classloader, but knows all JARs in the JBoss.
		// Obviously & fortunately, it still uses the bootstrap-loader serving java.lang.
		URLClassLoader classLoader = new URLClassLoader(classLoaderURLs.toArray(new URL[classLoaderURLs.size()]), null);
		Thread.currentThread().setContextClassLoader(classLoader);
		Class<?> internalLauncherClass;
		internalLauncherClass = classLoader.loadClass("org.nightlabs.jfire.jboss.serverconfigurator.internal.InternalLauncher");
		Object internalLauncher = internalLauncherClass.newInstance();
		Method runMethod = internalLauncherClass.getDeclaredMethod("run");
		runMethod.invoke(internalLauncher);
	}

}
