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
	private static final Logger logger = Logger.getLogger(AbstractInitManager.class);

	/**
	 * @deprecated Use {@link #scan(String, String[], JarEntryHandler[])}
	 */
	@Deprecated
	protected void scan(ManagedConnectionFactoryImpl mcf, String[] jarEntryNames, JarEntryHandler[] jarEntryHandlers)
	{
		scan(getDeployBaseDir(mcf, null), jarEntryNames, jarEntryHandlers);
	}

	/**
	 * @deprecated Use {@link #scan(String, String[], JarEntryHandler[])}
	 */
	@Deprecated
	protected void scan(JFireServerManager jfsm, String[] jarEntryNames, JarEntryHandler[] jarEntryHandlers)
	{
		scan(getDeployBaseDir(null, jfsm), jarEntryNames, jarEntryHandlers);
	}

	private String getDeployBaseDir(ManagedConnectionFactoryImpl mcf, JFireServerManager jfsm) {
		if (jfsm != null) {
			return jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		}
		return mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
	}

	protected void scan(String deployBaseDir, String[] jarEntryNames, JarEntryHandler[] jarEntryHandlers)
	{
		File jfireModuleBaseDir = new File(deployBaseDir);

		EARApplicationSet earApplicationSet;
		try {
			earApplicationSet = new EARApplicationSet(jfireModuleBaseDir, EARModuleType.ejb);
		} catch (XMLReadException e) {
			throw new RuntimeException(e);
		}
		earApplicationSet.handleJarEntries(jarEntryNames, jarEntryHandlers);
	}

	protected void establishDependencies(List<I> inits, PrefixTree<I> initTrie) throws InitException {
		for (I init : inits) { // foreach parsed Init
			for (D dep : init.getDependencies()) { // check all dependencies
				// and find the corresponding required inits in the trie
				Collection<I> reqInits = initTrie.getSubtrieElements(getTriePath(dep));
				if (reqInits.isEmpty()) { // dependency could not be resolved
					if (dep.getResolution() == Resolution.Optional) {
						logger.debug("Optional dependency "+dep+" could not be resolved (declared by init " + init + ").");
					} else {
						logger.error("Required dependency "+dep+" could not be resolved (declared by init " + init + ").");
						throw new InitException("Required dependency "+dep+" could not be resolved (declared by init " + init + ").");
					}
				}
				for (I reqInit : reqInits) {
					// and add them as required init
					if (init != reqInit) // suppress self.
						init.addRequiredInit(reqInit);
				}
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

	protected void printInits(List<I> inits, Logger logger) {
		for (I init : inits) {
			logger.debug(init.toStringWithDependencies());
		}
	}

	protected abstract String[] getTriePath(D dependency);
}
