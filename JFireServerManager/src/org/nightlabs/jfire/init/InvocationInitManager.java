package org.nightlabs.jfire.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.datastructure.PrefixTree;
import org.nightlabs.jfire.servermanager.xml.JarEntryHandler;

public abstract class InvocationInitManager<I extends InvocationInit<I, D>, D extends InvocationInitDependency<I>>
extends AbstractInitManager<I, D>
{
	private static final Logger logger = Logger.getLogger(InvocationInitManager.class);

	/**
	 * All found inits.
	 */
	private List<I> inits = new ArrayList<I>();

	public InvocationInitManager(String j2eeDeployBaseDirectory) throws InitException
	{
		InvocationInitJarEntryHandler<I, D> handler = createInvocationInitJarEntryHandler();
		if (handler == null)
			throw new IllegalStateException("Implementation error in class " + getClass().getName() + ": createInvocationInitJarEntryHandler() returned null!");

		scan(
				j2eeDeployBaseDirectory,
				new String[] { "META-INF/" + handler.getInitXmlFileName() },
				new JarEntryHandler[] { handler }
		);
		this.inits = handler.getInits();
		final PrefixTree<I> initTrie = handler.getInitTrie();
		// Now all meta data files have been read.

		// substitute the temporary dependency definitions by links to the actual inits
		try {
			establishDependencies(inits, initTrie);
		} catch (InitException e1) {
			throw new InitException("Initialisation failed: " + e1.getMessage(), e1);
		}

		// Now all inits have references of their required and dependent inits.
		try {
			inits = resolveDependencies(inits, new InvocationInitComparator<I>());
		} catch (DependencyCycleException e) {
			throw new InitException(e + "Information regarding the cycle: "+ e.getCycleInfo(), e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("************************************************");
			logger.debug("Inits of " + handler.getInitXmlFileName() + " files in execution order:");
			printInits(inits, logger);
			logger.debug("************************************************");
		}
	}

	protected abstract InvocationInitJarEntryHandler<I, D> createInvocationInitJarEntryHandler();

	public List<I> getInits()
	{
		return Collections.unmodifiableList(inits);
	}

	@Override
	protected String[] getTriePath(D dependency) {
		return dependency.getInvocationPath();
	}
}
