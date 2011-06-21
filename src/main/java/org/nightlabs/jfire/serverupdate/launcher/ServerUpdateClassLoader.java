package org.nightlabs.jfire.serverupdate.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.nightlabs.jfire.serverupdate.launcher.config.Directory;
import org.nightlabs.jfire.serverupdate.launcher.config.ServerUpdateConfig;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ServerUpdateClassLoader extends URLClassLoader
{
	private SortedSet<String> packageNames;

	private ServerUpdateClassLoader(URL[] urls, ClassLoader parentClassLoader, SortedSet<String> packageNames) {
		super(urls, parentClassLoader);
		this.packageNames = Collections.unmodifiableSortedSet(packageNames);
	}

	public Set<String> getPackageNames() {
		return packageNames;
	}

	private static ServerUpdateClassLoader sharedInstance = null;

	public static synchronized ServerUpdateClassLoader createSharedInstance(ServerUpdateConfig config, ClassLoader parentClassLoader)
	{
		if (sharedInstance != null)
			throw new IllegalStateException("createSharedInstance(...) was already called!");

		List<URL> urls = new ArrayList<URL>();
		SortedSet<String> packageNames = new TreeSet<String>();
		populateClassLoaderURLs(config, urls, packageNames);

		sharedInstance = new ServerUpdateClassLoader(urls.toArray(new URL[urls.size()]), parentClassLoader, packageNames);
		return sharedInstance;
	}

	public static synchronized ServerUpdateClassLoader sharedInstance()
	{
		if (sharedInstance == null)
			throw new IllegalStateException("createSharedInstance(...) was not called!");

		return sharedInstance;
	}

	private static void populateClassLoaderURLs(ServerUpdateConfig config, Collection<URL> urls, SortedSet<String> packageNames)
	{
		if (Log.isDebugEnabled()) {
			Log.debug("====================================================================");
			Log.debug("                    Creating Class Loader                           ");
			Log.debug("====================================================================");
		}

		try {
			// We sort the URLs alphabetically in order to guarantee a specific order and prevent Heisenbugs that
			// might result from files/directories being found in different order depending on the file system.
			// But we do this for each class-path-entry to be able to specify the order in the configuration.
			Comparator<URL> urlComparator = new Comparator<URL>() {
				@Override
				public int compare(URL url1, URL url2) {
					return url1.toString().compareTo(url2.toString());
				}
			};

			for (Directory cpDir : config.getClasspath()) {
				SortedSet<URL> classLoaderURLSet = new TreeSet<URL>(urlComparator);
				// TODO extend the scanDir method to take the recursive flag into account!
				populateClassLoaderURLs((File)null, cpDir.getFile(), classLoaderURLSet, packageNames, cpDir.isRecursive());
				urls.addAll(classLoaderURLSet);
			}

			if (Log.isDebugEnabled()) {
				for (URL url : urls) {
					Log.debug(" > %s", url.getFile());
				}
			}
		} catch (Exception e) {
			Log.error(e.getMessage());
			throw new RuntimeException(e);
		}

		return ;
	}

	private static final String[] NESTED_JAR_SUFFIXES = {
		".ear",
		".jar",
		".rar",
		".war",
		".zip"
	};

	private static final String CLASS_SUFFIX = ".class";
	private static final String UPDAT_XML_SUFFIX = ".jsu.xml";

	private static void populateClassLoaderURLs(File tmpBaseDir, File dirOrFile, Collection<URL> urls, Set<String> packageNames, boolean recursive) throws IOException
	{
		try {
			if (tmpBaseDir == null)
				tmpBaseDir = new File(IOUtil.createUserTempDir("jfire_server.", null), "nested-jars");

			if (!tmpBaseDir.isDirectory())
				tmpBaseDir.mkdirs();

			if (!tmpBaseDir.isDirectory())
				throw new IOException("Could not create directory: " + tmpBaseDir.getAbsolutePath());
		} catch (IOException e) {
			throw e;
		}

		if (recursive) {
			File[] children = dirOrFile.listFiles();
			if (children != null) {
				for (File child : children)
					populateClassLoaderURLs(new File(tmpBaseDir, dirOrFile.getName()), child, urls, packageNames, true);
			}
		}

		if (dirOrFile.isFile()) {
			File file = dirOrFile;

			if (isZipAccordingToSuffix(file.getName())) {
				ZipFile zf = null;
				try {
					try {
						zf = new ZipFile(file);
					} catch (Exception x) {
						// ignore - it's probably no zip file
						if (Log.isDebugEnabled())
							Log.debug("populateClassLoaderURLs: Ignoring file (not a zip file or it cannot be opened for other reasons): " + file.getAbsolutePath());
					}

					if (zf != null) {
						urls.add(file.toURI().toURL());

						boolean containsNestedZip = false;
						for (Enumeration<? extends ZipEntry> zfEnum = zf.entries(); zfEnum.hasMoreElements(); ) {
							ZipEntry ze = zfEnum.nextElement();
							String zeName = ze.getName();
							if (zeName.endsWith(CLASS_SUFFIX)) {
								String className = zeName.substring(0, zeName.length() - CLASS_SUFFIX.length());
								className = className.replace('/', '.');
								int lastDotIdx = className.lastIndexOf('.');
								if (lastDotIdx < 0)
									packageNames.add(""); // empty default package - should never happen in JFire anyway
								else {
									String packageName = className.substring(0, lastDotIdx);
									packageNames.add(packageName);
								}
							}
							if (zeName.endsWith(UPDAT_XML_SUFFIX)) {
								String className = zeName.substring(0, zeName.length() - UPDAT_XML_SUFFIX.length());
								int lastDotIdx = className.lastIndexOf('/');
								if (lastDotIdx < 0)
									packageNames.add(""); // empty default package - should never happen in JFire anyway
								else {
									String packageName = className.substring(0, lastDotIdx);
									packageName = packageName.replace('/', '.');
									packageNames.add(packageName);
								}
							}							
							else if (isZipAccordingToSuffix(zeName))
								containsNestedZip = true;
						}

						if (containsNestedZip) {
							File tmpEarDir = new File(tmpBaseDir, file.getName());
							try {
								IOUtil.unzipArchiveIfModified(file, tmpEarDir);
							} catch (IOException e) {
								throw e;
							}

							// The contents of a zip (e.g. an EAR) must always be processed recursively - no matter if the argument 'recursive' is true or false.
							populateClassLoaderURLs(new File(tmpBaseDir, file.getName()), tmpEarDir, urls, packageNames, true);
						}
					}
				} finally {
					if (zf != null)
						zf.close();
				}
			}
		}
	}

	private static boolean isZipAccordingToSuffix(String fileName)
	{
		for (String suffix : NESTED_JAR_SUFFIXES) {
			if (fileName.endsWith(suffix))
				return true;
		}
		return false;
	}
}
