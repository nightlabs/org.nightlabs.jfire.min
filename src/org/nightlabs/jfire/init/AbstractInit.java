package org.nightlabs.jfire.init;

import java.util.ArrayList;
import java.util.Collection;

import org.nightlabs.annotation.Implement;
import org.nightlabs.datastructure.IDirectedGraphNode;


/**
 * @author Tobias Langner <!-- tobias[DOT]langner[AT]nightlabs[DOT]de -->
 */
public abstract class AbstractInit<I extends AbstractInit, D extends IDependency<I>>
implements IDirectedGraphNode<I>
{
	/**
	 * Contains all inits that are required by this {@link AbstractInit}, i.e. this init is dependent on them.
	 */
	private Collection<I> requiredInits = new ArrayList<I>();
	
	/**
	 * Contains all {@link AbstractInit}s that are dependent on this init, i.e. this init is a requirement for them.
	 */
	private Collection<I> dependentInits = new ArrayList<I>();
	
	/**
	 * Contains the temporary dependencies that are later resolved to fill the two collections above.
	 */
	private Collection<D> dependencies = new ArrayList<D>();

	/**
	 * Adds a required init, i.e. an init that has to be executed before the execution of this instance.	 * 
	 * @param requiredInit The init that this instance requires.
	 */
	public void addRequiredInit(I requiredInit) {
		this.requiredInits.add(requiredInit);
		requiredInit.addDependentInit(this);
	}
	
	/**
	 * Adds a dependent init, i.e. an init that needs this instance to be executed before its own execution.	 * 
	 * @param dependentInit The init that is dependent on this instance. 
	 */
	protected void addDependentInit(I dependentInit) {
		this.dependentInits.add(dependentInit);
	}
	
	/**
	 * Adds a temporary dependency that is later resolved to the actual {@link AbstractInit}.	 * 
	 * @param dependency the dependency to be added.
	 */
	public void addDependency(D dependency) {
		dependencies.add(dependency);
	}
	
	/**
	 * Returns a collection of the inits that this instance requires to be executed before its own execution.
	 * @return a collection of the inits that this instance requires to be executed before its own execution.
	 */
	public Collection<I> getRequiredInits() {
		return requiredInits;
	}
	
	/**
	 * Returns a collection of the inits that are dependent on this instance, i.e. have to be executed after this instance.
	 * @return
	 */
	protected Collection<I> getDependentInits() {
		return dependentInits;
	}
	
	/**
	 * @return Returns the dependencies.
	 */
	public Collection<D> getDependencies() {
		return dependencies;
	}

	@Implement
	public Collection<I> getChildren() {
		return getDependentInits();
	}
	
	@Implement
	public I getValue() {
		return (I) this;
	}
	
	public String toStringWithDependencies() {
		String toReturn = getName();
		if (requiredInits.isEmpty())
			return toReturn + " (autonomous)";
		
		toReturn += " depends on:";
		for (I init : requiredInits)
			toReturn += "\n  - " + init;
		return toReturn;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * This method should return a string identifying the Init instance.
	 * @return a string identifying the Init instance.
	 */
	protected abstract String getName();
}