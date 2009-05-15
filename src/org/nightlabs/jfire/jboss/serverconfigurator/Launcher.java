package org.nightlabs.jfire.jboss.serverconfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
 * <p>
 * System properties supported by the <code>Launcher</code>:
 * <ul>
 *		<li>debug: <code>true</code> or <code>false</code> (default is <code>false</code>)</li>
 * </ul>
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class Launcher
{
//	private static String[] jarFileExtensions = { "jar", "rar", "sar", "war", "ear", "zip" };
	private static String[] jarFileExtensions = { "jar", "rar" , "ear"}; // should be sufficient
	private static Set<String> jarFileExtensionSet = null;

	public static boolean isDebugEnabled()
	{
		return "true".equals(System.getProperty("debug"));
	}

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

	public static void main(String[] args)
	{
		try {
			System.out.println("Starting JFire server configurator launcher...");
			Launcher launcher = new Launcher();
			launcher.run();
			System.out.println("Server configurator successfully invoked.\n");
			System.exit(0);
		} catch(Throwable e) {
			System.err.println("\n\nError invoking server configurator:");
			e.printStackTrace();
			System.err.println("\n\n");
			System.exit(1);
		}
	}

	/**
	 * Some Strings which cause directories & jars to be excluded, if at least one of them is contained in the path.
	 */
	private static final String[] excludes = {
		"/JFireReporting.ear/birt/plugins/org.apache.derby.core_"
	};

	private static boolean isExcluded(File directoryOrJarFile) {
		String directoryOrJarFileString = directoryOrJarFile.getAbsolutePath().replace('\\', '/');
		for (String exclude : excludes) {
			if (directoryOrJarFileString.contains(exclude)) {
				if (isDebugEnabled())
					System.out.println("Launcher.isExcluded: \"" + directoryOrJarFileString + "\" is excluded because of \"" + exclude + "\"");

				return true;
			}
		}
		return false;
	}

	private File tempDir;
	private FileOutputStream tempDirLockFileOutputStream;
	private FileLock tempDirFileLock;

	private synchronized File getTempDir() throws IOException
	{
		int tryCounter = 0;
		while (tempDir == null) {
			if (++tryCounter > 1000) {
				System.err.println("Could not create unique temp directory!");
				throw new IllegalStateException("Creating unique temp directory failed!");
			}

			String tempDirString = System.getProperty("java.io.tmpdir");
			File dir = new File(tempDirString, "jfire-serverconfigurator-" + Long.toString(System.currentTimeMillis(), 36));
			if (!dir.exists()) {
				if (dir.mkdir()) {
					File tempDirLockFile = new File(dir, "lock.lck");
					tempDirLockFileOutputStream = new FileOutputStream(tempDirLockFile);
					tempDirFileLock = tempDirLockFileOutputStream.getChannel().tryLock();
					if(tempDirFileLock == null)
						tempDirLockFileOutputStream.close();
					else
						tempDir = dir;
				}
			}
		}
		return tempDir;
	}

	private synchronized void deleteTempDir() throws IOException
	{
		if (tempDirFileLock != null) {
			tempDirFileLock.release();
			tempDirFileLock = null;
		}

		if (tempDirLockFileOutputStream != null) {
			tempDirLockFileOutputStream.close();
			tempDirLockFileOutputStream = null;
		}

		if (tempDir != null) {
			deleteRecursively(tempDir);
			tempDir = null;
		}
	}

	private void deleteRecursively(File file)
	{
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				deleteRecursively(child);

			file.delete();
		}
		else
			file.delete();
	}

	// This method has been copied from IOUtil since we can't load this class yet in this launcher. Marco.
	private static String simplifyPath(File path)
	{
		LinkedList<String> dirs = new LinkedList<String>();

		String pathStr = path.getAbsolutePath();
		boolean startWithSeparator = pathStr.startsWith(File.separator);

		StringTokenizer tk = new StringTokenizer(pathStr, File.separator, false);
		while (tk.hasMoreTokens()) {
			String dir = tk.nextToken();
			if (".".equals(dir))
				;// nothing
			else if ("..".equals(dir)) {
				if (!dirs.isEmpty())
					dirs.removeLast();
			}
			else
				dirs.addLast(dir);
		}

		StringBuffer sb = new StringBuffer();
		for (String dir : dirs) {
			if (startWithSeparator || sb.length() > 0)
				sb.append(File.separator);
			sb.append(dir);
		}

		return sb.toString();
	}

	private void scanJar(Set<URL> classLoaderURLs, JarFile jarFile) throws IOException
	{
		for (Enumeration<JarEntry> jarEntryEnum = jarFile.entries(); jarEntryEnum.hasMoreElements(); ) {
			JarEntry jarEntry = jarEntryEnum.nextElement();

			if (isJarBasedOnExtension(jarEntry.getName())) {
				String jarFileName = jarFile.getName().replace(':', '_'); // for windows drive letters - urgs. Marco.
				File f = new File(getTempDir(), jarFileName);
				File tempFileParent;
				try {
					tempFileParent = new File(simplifyPath(f));
				} catch (Throwable x) {
					throw new RuntimeException("Getting simplified path failed for: " + f.getPath(), x);
				}
				File tempFile = new File(tempFileParent, jarEntry.getName());
				tempFile.getParentFile().mkdirs();

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
						System.out.println("Launcher.scanJar: Nested JAR (extracted to temp file) could not be opened: " + tempFile.getAbsolutePath());
						if (isDebugEnabled())
							x.printStackTrace();
					}

					if (jf != null) {
						classLoaderURLs.add(tempFile.toURI().toURL());

						// A jar might contain nested JARs. Since we cannot directly put them into the class-loader, we extract them.
						scanJar(classLoaderURLs, jf);
					} // if (isJAR) {
				} finally {
					if (jf != null)
						jf.close();
				}
			} // if (isJarBasedOnExtension(jarEntry.getName())) {
		}
	}

	private void scanDir(Set<URL> classLoaderURLs, File directory) throws IOException
	{
		if (isExcluded(directory))
			return;

		for (File f : directory.listFiles()) {
			if (f.isDirectory())
				scanDir(classLoaderURLs, f);
			else {
				if (isJarBasedOnExtension(f.getName()) && !isExcluded(f)) {
					JarFile jf = null;
					try {
						try {
							jf = new JarFile(f);
						} catch (IOException x) {
							// ignore - it's probably not a JAR
							// ...better log:
							System.out.println("Launcher.scanDir: JAR could not be opened: " + f.getAbsolutePath());
							if (isDebugEnabled())
								x.printStackTrace();
						}

						if (jf != null) {
							classLoaderURLs.add(f.toURI().toURL());

							// A jar might contain nested JARs. Since we cannot directly put them into the class-loader, we extract them.
							scanJar(classLoaderURLs, jf);
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
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					deleteTempDir();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
		try {
			if (isDebugEnabled()) {
				System.out.println("Launcher.run: getClass().getClassLoader(): " + getClass().getClassLoader());
			}
			long scanStart = System.currentTimeMillis();

			Set<URL> classLoaderURLSet = new HashSet<URL>();

			File serverDefaultFolder = new File(new File(new File("..").getAbsoluteFile(), "server"), "default");
			File serverLibFolder = new File(serverDefaultFolder, "lib");
			File serverDeployFolder = new File(serverDefaultFolder, "deploy");
			File jfireLastFolder = new File(serverDeployFolder, "JFire.last");
			File globalLibFolder = new File(serverDefaultFolder, "../../lib");

			if (!jfireLastFolder.exists()) {
				System.err.println("The folder JFire.last cannot be found! Either it does not exist or you started the Launcher in the wrong directory! The Launcher must be started from the JBoss server's bin directory. This path does not exist: " + jfireLastFolder.getAbsolutePath());
				throw new IllegalStateException("Directory does not exist: " + jfireLastFolder.getAbsolutePath());
			}

			scanDir(classLoaderURLSet, serverLibFolder);
			scanDir(classLoaderURLSet, serverDeployFolder);
			scanDir(classLoaderURLSet, globalLibFolder);

			if (isDebugEnabled()) {
				System.out.println("Launcher.run: collected " + classLoaderURLSet.size() + " JARs for classpath in " + (System.currentTimeMillis() - scanStart) + " msec. Sorting the classpath.");
			}

			List<URL> classLoaderURLList = new ArrayList<URL>(classLoaderURLSet);

			if (isDebugEnabled()) {
				System.out.println("Launcher.run: found JARs before sorting:");
				for (URL url : classLoaderURLList) {
					System.out.println("  * " + url.toString());
				}
			}

			long sortStart = System.currentTimeMillis();

			// We sort the URLs alphabetically in order to guarantee a specific order and prevent Heisenbugs that
			// might result from files/directories being found in different order depending on the file system.
			Collections.sort(classLoaderURLList, new Comparator<URL>() {
				@Override
				public int compare(URL url1, URL url2) {
					return url1.toString().compareTo(url2.toString());
				}
			});


			if (isDebugEnabled()) {
				System.out.println("Launcher.run: sorting classpath took " + (System.currentTimeMillis() - sortStart) + " msec. Starting internal launcher.");
				System.out.println("Launcher.run: found JARs after sorting:");
				for (URL url : classLoaderURLList) {
					System.out.println("  * " + url.toString());
				}
			}

			// Create a new classLoader which has no parent classloader, but knows all JARs in the JBoss.
			// Obviously & fortunately, it still uses the bootstrap-loader serving java.lang.
			URLClassLoader classLoader = new URLClassLoader(classLoaderURLList.toArray(new URL[classLoaderURLList.size()]), null);
			Thread.currentThread().setContextClassLoader(classLoader);
			Class<?> internalLauncherClass;
			internalLauncherClass = classLoader.loadClass("org.nightlabs.jfire.jboss.serverconfigurator.internal.InternalLauncher");
			Object internalLauncher = internalLauncherClass.newInstance();
			Method runMethod = internalLauncherClass.getDeclaredMethod("run");
			runMethod.invoke(internalLauncher);
		} finally {
			deleteTempDir();
		}
	}

}
