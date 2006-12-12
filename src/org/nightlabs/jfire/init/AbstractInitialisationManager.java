package org.nightlabs.jfire.init;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.util.ds.CycleException;
import org.nightlabs.util.ds.DirectedGraph;
import org.nightlabs.util.ds.PrefixTree;

public abstract class AbstractInitialisationManager<I extends AbstractInit<I, D>, D extends IDependency<I>> {
	protected void establishDependencies(List<I> inits, PrefixTree<I> initTrie) {
		for (I init : inits) { // foreach parsed Init
			for (D dep : init.getDependencies()) { // check all dependencies
				// and find the corresponding required inits in the trie
				Collection<I> reqInits = initTrie.getSubtrieElements(getTriePath(dep));
				if (reqInits.isEmpty()) { // dependency could not be resolved
					getLogger().warn("The server init dependency "+ dep +" does not exist.");
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
					+"Revise the serverinit.xml files and remove the cycle.", e.getCycleInfo());
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
