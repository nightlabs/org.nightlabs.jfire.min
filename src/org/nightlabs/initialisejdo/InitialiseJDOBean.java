package org.nightlabs.initialisejdo;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.multitxjob.MultiTxJob;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.CacheDirTag;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.reflect.ReflectUtil;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class InitialiseJDOBean
extends BaseSessionBeanImpl
implements InitialiseJDORemote
{
	private static final Logger logger = Logger.getLogger(InitialiseJDOBean.class);

	private static final String MULTI_TX_JOB_ID = InitialiseJDOBean.class.getName();

	private static final String[] NESTED_JAR_SUFFIXES = {
		".ear",
		".jar",
		".rar",
		".war",
		".zip"
	};

	/**
	 * These packages definitely contain no JDO persistence-capable classes and can be ignored. The entries
	 * here are regular expressions.
	 */
	private static final String[] IGNORED_PACKAGE_REGEX = {
		"java\\..*",
		"javax\\..*",
		"org\\.nightlabs\\.config",
		"org\\.nightlabs\\.concurrent",
		"org\\.nightlabs\\.jfire\\.reporting\\.platform",
		"com\\.thoughtworks\\.xstream\\.?.*",
	};

	private static final Pattern[] IGNORED_PACKAGE_REGEX_COMPILED;
	static {
		IGNORED_PACKAGE_REGEX_COMPILED = new Pattern[IGNORED_PACKAGE_REGEX.length];
		int idx = -1;
		for (String s : IGNORED_PACKAGE_REGEX)
			IGNORED_PACKAGE_REGEX_COMPILED[++idx] = Pattern.compile(s);
	}

	private static final String CLASS_SUFFIX = ".class";

	private void populateClassLoaderURLs(File tmpBaseDir, File dirOrFile, Collection<URL> urls, Set<String> packageNames) throws IOException
	{
		boolean createCacheDirTag = false;
		try {
			if (tmpBaseDir == null) {
				createCacheDirTag = true;
				tmpBaseDir = new File(IOUtil.createUserTempDir("jfire_server.", null), "nested-jars");
			}

			if (!tmpBaseDir.isDirectory())
				tmpBaseDir.mkdirs();

			if (!tmpBaseDir.isDirectory())
				throw new IOException("Could not create directory: " + tmpBaseDir.getAbsolutePath());
		} catch (IOException e) {
			throw e;
		}

		if (createCacheDirTag) {
			CacheDirTag cacheDirTag = new CacheDirTag(tmpBaseDir);
			try {
				cacheDirTag.tag("JFire - http://www.jfire.org", true, false);
			} catch (IOException e) {
				logger.warn("populateClassLoaderURLs: " + e, e);
			}
		}

		File[] children = dirOrFile.listFiles();
		if (children != null) {
			for (File child : children)
				populateClassLoaderURLs(new File(tmpBaseDir, dirOrFile.getName()), child, urls, packageNames);
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
						if (logger.isDebugEnabled())
							logger.debug("populateClassLoaderURLs: Ignoring file (not a zip file or it cannot be opened for other reasons): " + file.getAbsolutePath());
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

							populateClassLoaderURLs(new File(tmpBaseDir, file.getName()), tmpEarDir, urls, packageNames);
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

	private static class ChildFirstURLClassLoader extends URLClassLoader
	{
		private ClassLoader parent;

		public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, null);
			this.parent = parent;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			try {
				return super.findClass(name);
			} catch (ClassNotFoundException x) {
				return parent.loadClass(name);
			}
		}
	}

	@RolesAllowed("_System_")
	@Override
	public void searchClasses()
	throws Throwable
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			// Pop all MultiTxJobParts to guarantee a clean situation afterwards (no old stuff lingering around).
			while (MultiTxJob.popMultiTxJobPartData(pm, MULTI_TX_JOB_ID) != null);

			// Determine the number of init methods in order to know how many buckets (= MultiTxJobParts) we need.
			int initClassesMethodCount = 0;
			for (Method method : InitialiseJDORemote.class.getDeclaredMethods()) {
				if (method.getName().startsWith("initClasses_"))
					++initClassesMethodCount;
			}

			if (initClassesMethodCount < 1)
				throw new IllegalStateException("No initClasses_* method found!");

			// Search for all classes.
			// We sort the classes to prevent Heisenbugs (guarantee the same order for every initialisation).
			Set<Class<?>> persistenceCapableClasses = new TreeSet<Class<?>>(new Comparator<Class<?>>() {
				@Override
				public int compare(Class<?> o1, Class<?> o2) {
					if (o1 == o2)
						return 0;

					return o1.getName().compareTo(o2.getName());
				}
			});

			// We scan *all* with a special class-loader in order to forget the classes that were looked at but are not needed in RAM.
			File jeeDeployBaseDir;
			JFireServerManager jfsm = getJFireServerManager();
			try {
				jeeDeployBaseDir = new File(jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());
			} finally {
				jfsm.close();
			}

			Collection<URL> classLoaderURLs = new ArrayList<URL>();
			Set<String> packageNames = new TreeSet<String>();
			populateClassLoaderURLs(null, jeeDeployBaseDir, classLoaderURLs, packageNames);
			ClassLoader cl = new ChildFirstURLClassLoader(classLoaderURLs.toArray(new URL[classLoaderURLs.size()]), getClass().getClassLoader());

			Collection<Class<?>> c;
			iteratePackages: for (String packageName : packageNames) {
				for (Pattern pattern : IGNORED_PACKAGE_REGEX_COMPILED) {
					if (pattern.matcher(packageName).matches())
						continue iteratePackages;
				}

				try {
					c = ReflectUtil.listClassesInPackage(packageName, cl, false);
					persistenceCapableClasses.addAll(c);
				} catch (Throwable x) {
					if (x instanceof OutOfMemoryError)
						throw x;

					if (logger.isTraceEnabled())
						logger.trace("searchClasses: Error listing classes in package \"" + packageName + "\": " + x, x);
					else if (logger.isDebugEnabled())
						logger.debug("searchClasses: Error listing classes in package \"" + packageName + "\": " + x);
				}
			}

			cl = null; // allow garbage collection

			// Filter out all classes that are not annotated with PersistenceCapable.
			for (Iterator<Class<?>> it = persistenceCapableClasses.iterator(); it.hasNext(); ) {
				Class<?> clazz = it.next();
				boolean isPersistenceCapable = false;
				Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
				for (Annotation annotation : declaredAnnotations) {
					if (javax.jdo.annotations.PersistenceCapable.class == annotation.annotationType())
						isPersistenceCapable = true;
				}

				if (!isPersistenceCapable) {
					if(logger.isTraceEnabled())
						logger.trace("searchClasses: Ignoring non-persistence-capable class: " + clazz.getName());

					it.remove();
				}
			}

			Collection<String> persistenceCapableClassNames = new ArrayList<String>(persistenceCapableClasses.size());
			for (Class<?> clazz : persistenceCapableClasses)
				persistenceCapableClassNames.add(clazz.getName());

			persistenceCapableClasses = null; // allow garbage collection


			// Split the classes into multiple packages
			int classCountPerBucket = persistenceCapableClassNames.size() / initClassesMethodCount;
			if (classCountPerBucket < 1)
				classCountPerBucket = 1;

			Iterator<String> it = persistenceCapableClassNames.iterator();
			for (int multiTxJobPartIndex = 0; multiTxJobPartIndex < initClassesMethodCount; ++multiTxJobPartIndex) {
				ArrayList<String> pcClassNames = new ArrayList<String>(classCountPerBucket);

				for (int i = 0; i < classCountPerBucket; ++i) {
					if (!it.hasNext())
						break;

					pcClassNames.add(it.next());
				}

				if (multiTxJobPartIndex + 1 == initClassesMethodCount) {
					while (it.hasNext())
						pcClassNames.add(it.next());
				}

				MultiTxJob.createMultiTxJobPart(pm, MULTI_TX_JOB_ID, pcClassNames);
			}

			if (it.hasNext())
				throw new IllegalStateException("The iterator should have been iterated completely!");
		} finally {
			pm.close();
		}
	}

	private void initClasses()
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			@SuppressWarnings("unchecked")
			Collection<String> c = (Collection<String>) MultiTxJob.popMultiTxJobPartData(pm, MULTI_TX_JOB_ID);

			// If we don't have enough classes, there're not enough MultiTxJobParts, hence we have to check for null.
			if (c == null)
				return;

			for (String className : c) {
				if(logger.isDebugEnabled())
					logger.debug("initClasses: Initializing meta-data for class: " + className);

				Class<?> clazz = Class.forName(className);

				try {
					pm.getExtent(clazz);
				} catch (Exception x) {
					logger.warn("initClasses: Initializing meta-data for class \"" + clazz.getName() + "\" failed: " + x.getClass().getName() + ": " + x.getMessage(), x);
				}
			}

		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_System_")
	@Override
	public void checkAfterInitClasses() throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			if (MultiTxJob.popMultiTxJobPartData(pm, MULTI_TX_JOB_ID) != null)
				throw new IllegalStateException("All MultiTxJobParts should have been processed, but there is at least one left!!!");
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_System_")
	@Override
	public void initClasses_00() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_01() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_02() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_03() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_04() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_05() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_06() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_07() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_08() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_09() throws Exception { initClasses(); }


	@RolesAllowed("_System_")
	@Override
	public void initClasses_10() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_11() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_12() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_13() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_14() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_15() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_16() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_17() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_18() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_19() throws Exception { initClasses(); }


	@RolesAllowed("_System_")
	@Override
	public void initClasses_20() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_21() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_22() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_23() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_24() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_25() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_26() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_27() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_28() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_29() throws Exception { initClasses(); }


	@RolesAllowed("_System_")
	@Override
	public void initClasses_30() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_31() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_32() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_33() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_34() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_35() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_36() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_37() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_38() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_39() throws Exception { initClasses(); }


	@RolesAllowed("_System_")
	@Override
	public void initClasses_40() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_41() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_42() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_43() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_44() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_45() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_46() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_47() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_48() throws Exception { initClasses(); }

	@RolesAllowed("_System_")
	@Override
	public void initClasses_49() throws Exception { initClasses(); }

}
