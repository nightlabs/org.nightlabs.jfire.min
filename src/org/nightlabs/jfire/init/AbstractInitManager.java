package org.nightlabs.jfire.init;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.datastructure.CycleException;
import org.nightlabs.datastructure.DirectedGraph;
import org.nightlabs.datastructure.PrefixTree;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.ra.ManagedConnectionFactoryImpl;
import org.nightlabs.jfire.servermanager.xml.EARApplicationSet;
import org.nightlabs.jfire.servermanager.xml.EARModuleType;
import org.nightlabs.jfire.servermanager.xml.JarEntryHandler;
import org.nightlabs.jfire.servermanager.xml.XMLReadException;

public abstract class AbstractInitManager<I extends AbstractInit<I, D>, D extends IDependency<I>>
{
	private Logger logger = Logger.getLogger(AbstractInitManager.class);

	protected void scan(
			ManagedConnectionFactoryImpl mcf,
			String[] jarEntryNames, JarEntryHandler[] jarEntryHandlers
	)
	{
		scan(mcf, null, jarEntryNames, jarEntryHandlers);
	}

	protected void scan(
			JFireServerManager jfsm,
			String[] jarEntryNames, JarEntryHandler[] jarEntryHandlers
	)
	{
		scan(null, jfsm, jarEntryNames, jarEntryHandlers);
	}

	private void scan(
			ManagedConnectionFactoryImpl mcf,
			JFireServerManager jfsm,
			String[] jarEntryNames, JarEntryHandler[] jarEntryHandlers
	)
	{
		String deployBaseDir;
		if (jfsm != null)
			deployBaseDir = jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		else
			deployBaseDir = mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();

		File jfireModuleBaseDir = new File(deployBaseDir);

		EARApplicationSet earApplicationSet;
		try {
			earApplicationSet = new EARApplicationSet(jfireModuleBaseDir, EARModuleType.ejb);
		} catch (XMLReadException e) {
			throw new RuntimeException(e);
		}
		earApplicationSet.handleJarEntries(jarEntryNames, jarEntryHandlers);

//		Set<String> jarEntryNameSet = null;
//
//		// Scan all JARs within all EARs for organisation-init.xml files.
//		File[] ears = jfireModuleBaseDir.listFiles(earFileFilter);
//		for (int i = 0; i < ears.length; ++i) {
//			File ear = ears[i];
//			EARApplication earApplicationMan;
//			try {
//				earApplicationMan = new EARApplication(ear, EARModuleType.ejb);
//			} catch (XMLReadException x) {
//				logger.error("Reading application.xml from EAR-JAR '"+ear.getAbsolutePath()+"' failed!", x);
//				continue;
//			}
//
//			if (!ear.isDirectory()) {
//				try {
//					JarFile earJarFile = new JarFile(ear);
//					try {
//						for (ModuleDef moduleDef : earApplicationMan.getModules()) {
//							JarEntry moduleJarEntry = (JarEntry) earJarFile.getEntry(moduleDef.getResourceURI());
//							if (moduleJarEntry == null)
//								throw new IllegalStateException("EAR-JAR-file \"" + ear.getAbsolutePath() + "\" does not contain \"" + moduleDef.getResourceURI() + "\" referenced in \"META-INF/application.xml\"!");
//
//							JarInputStream moduleJarInputStream = new JarInputStream(earJarFile.getInputStream(moduleJarEntry));
//							try {
//								JarEntry je;
//								while (null != (je = moduleJarInputStream.getNextJarEntry())) {
//									if (jarEntryNameSet == null)
//										jarEntryNameSet = CollectionUtil.array2HashSet(jarEntryNames);
//
//									if (jarEntryNameSet.contains(je.getName())) {
//										ByteArrayOutputStream bout = new ByteArrayOutputStream();
//										IOUtil.transferStreamData(moduleJarInputStream, bout);
//										bout.close();
//										byte[] buffer = bout.toByteArray();
//
//										for (JarEntryHandler jarEntryHandler : jarEntryHandlers) {
//											InputStream in = new ByteArrayInputStream(buffer);
//											try {
//												jarEntryHandler.handleJarEntry(ear.getName(), moduleDef.getResourceURI(), in);
//											} finally {
//												in.close();
//											}
//										}
//									}
//									moduleJarInputStream.closeEntry();
//								}
//							} finally {
//								moduleJarInputStream.close();
//							}
//						}
//					} finally {
//						earJarFile.close();
//					}
//				} catch (Exception x) {
//					logger.error("Reading from EAR-JAR '"+ear.getAbsolutePath()+"' failed!", x);
//				}
//			}
//			else {
//	//			File[] jars = ear.listFiles(jarFileFilter);
//	//			for (int m = 0; m < jars.length; ++m) {
//	//				File jar = jars[m];
//				for (ModuleDef moduleDef : earApplicationMan.getModules()) {
//					File jar = new File(ear, moduleDef.getResourceURI());
//					try {
//						if (!jar.exists())
//							throw new IllegalStateException("EAR-directory \"" + ear.getAbsolutePath() + "\" does not contain \"" + moduleDef.getResourceURI() + "\" referenced in \"META-INF/application.xml\"!");
//
//						JarFile jf = new JarFile(jar);
//						try {
//							JarEntry je = null;
//							for (String jarEntryName : jarEntryNames) {
//								je = jf.getJarEntry(jarEntryName);
//								if (je != null)
//									break;
//							}
//
//							if (je != null) {
//								for (JarEntryHandler jarEntryHandler : jarEntryHandlers) {
//									InputStream in = jf.getInputStream(je);
//									try {
//										jarEntryHandler.handleJarEntry(ear.getName(), jar.getName(), in);
//									} finally {
//										in.close();
//									}
//								}
//							} // if (je != null) {
//						} finally {
//							jf.close();
//						}
//					} catch (Exception e) {
//						logger.error("Reading from JAR '"+jar.getAbsolutePath()+"' failed!", e);
//					}
//				}
//			}
//		}
	}

	protected void establishDependencies(List<I> inits, PrefixTree<I> initTrie) throws InitException {
		for (I init : inits) { // foreach parsed Init
			for (D dep : init.getDependencies()) { // check all dependencies
				// and find the corresponding required inits in the trie
				Collection<I> reqInits = initTrie.getSubtrieElements(getTriePath(dep));
				if (reqInits.isEmpty()) { // dependency could not be resolved
					if (dep.getResolution() == Resolution.Optional)
						logger.debug("Optional dependency "+dep+" could not be resolved (declared by init " + init + ").");
					else {
						logger.error("Required dependency "+dep+" could not be resolved (declared by init " + init + ").");
						throw new InitException("Required dependency "+dep+" could not be resolved (declared by init " + init + ").");
					}
				}
				for (I reqInit : reqInits)
					// and add them as required init
					init.addRequiredInit(reqInit);
			}
		}
	}

	protected List<I> resolveDependencies(List<I> inits, Comparator<I> comp) throws DependencyCycleException {
		DirectedGraph<I> dependencyGraph = new DirectedGraph<I>(inits);

		// Now sort them topologically to ensure all dependencies are fulfilled
		try {
			inits = dependencyGraph.sortElementsTopologically(comp);
		} catch (CycleException e) {
			throw new DependencyCycleException("Server initialisation failed because a dependency cycle has been detected."
					+"Revise the *init.xml files and remove the cycle.", e.getCycleInfo());
		}

		return inits;
	}

	protected void printInits(List<I> inits) {
		for (I init : inits)
			getLogger().debug(init.toStringWithDependencies());
	}

	protected abstract String[] getTriePath(D dependency);

	protected abstract Logger getLogger();
}
