package org.nightlabs.jfire.serverupdate.base.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.FileLock;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassLoaderUtil 
{
	private static String[] jarFileExtensions = { "jar", "rar" , "ear"}; // should be sufficient
	private static Set<String> jarFileExtensionSet = null;
	
	private static final String[] excludes = {
		"/JFireReporting.ear/birt/plugins/org.apache.derby.core_"
	};
	
	public static boolean isJarBasedOnExtension(String fileName)
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
	
	public static void scanDir(Set<URL> classLoaderURLs, File directory) throws IOException
	{
		if (isExcluded(directory))
			return;

		for (File file : directory.listFiles()) {
			if (file.isDirectory())
				scanDir(classLoaderURLs, file);
			else {
				if (isJarBasedOnExtension(file.getName()) && !isExcluded(file)) {
					JarFile jarFile = null;
					try {
						try {
							jarFile = new JarFile(file);
						} catch (IOException x) {
							// ignore - it's probably not a JAR
							// ...better log:
							System.out.println("Launcher.scanDir: JAR could not be opened: " + file.getAbsolutePath());
							if (isDebugEnabled())
								x.printStackTrace();
						}

						if (jarFile != null) {
							classLoaderURLs.add(file.toURI().toURL());

							// A jar might contain nested JARs. Since we cannot directly put them into the class-loader, we extract them.
							scanJar(classLoaderURLs, jarFile);
						} // if (isJAR) {
					} finally {
						if (jarFile != null)
							jarFile.close();
					}
				} // if (isJarBasedOnExtension(f.getName())) {
			} // if (!f.isDirectory()) {
		} // for (File f : directory.listFiles()) {
	}
	
	private static void scanJar(Set<URL> classLoaderURLs, JarFile jarFile) throws IOException
	{
		for (Enumeration<JarEntry> jarEntryEnum = jarFile.entries(); jarEntryEnum.hasMoreElements(); ) {
			JarEntry jarEntry = jarEntryEnum.nextElement();

			if (isJarBasedOnExtension(jarEntry.getName())) {
				String jarFileName = jarFile.getName().replace(':', '_'); // for windows drive letters - urgs. Marco.
				File file = new File(getTempDir(), jarFileName);
				File tempFileParent;
				try {
					tempFileParent = new File(simplifyPath(file));
				} catch (Throwable x) {
					throw new RuntimeException("Getting simplified path failed for: " + file.getPath(), x);
				}
				File tempJarFile = new File(tempFileParent, jarEntry.getName());
				tempJarFile.getParentFile().mkdirs();

				OutputStream out = new FileOutputStream(tempJarFile);
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
						jf = new JarFile(tempJarFile);
					} catch (IOException x) {
						// ignore - it's probably not a JAR
						System.out.println("Launcher.scanJar: Nested JAR (extracted to temp file) could not be opened: " + tempJarFile.getAbsolutePath());
						if (isDebugEnabled())
							x.printStackTrace();
					}

					if (jf != null) {
						classLoaderURLs.add(tempJarFile.toURI().toURL());

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
	
	
	
	private static synchronized File getTempDir() throws IOException
	{
		int tryCounter = 0;
		while (tempDir == null) {
			if (++tryCounter > 1000) {
				System.err.println("Could not create unique temp directory!");
				throw new IllegalStateException("Creating unique temp directory failed!");
			}

			String tempDirString = System.getProperty("java.io.tmpdir");
			File dir = new File(tempDirString, "nl-liquibase-dn-" + Long.toString(System.currentTimeMillis(), 36));
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
	
	private static File tempDir;
	private static FileOutputStream tempDirLockFileOutputStream;
	private static FileLock tempDirFileLock;
	
	private static boolean isDebugEnabled()
	{
		return "true".equals(System.getProperty("debug"));
	}
	
	public static synchronized void deleteTempDir() throws IOException
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

	private static void deleteRecursively(File file)
	{
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				deleteRecursively(child);

			file.delete();
		}
		else
			file.delete();
	}
}
