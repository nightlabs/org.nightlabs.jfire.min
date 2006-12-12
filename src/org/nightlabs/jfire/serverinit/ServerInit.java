package org.nightlabs.jfire.serverinit;

import org.nightlabs.jfire.init.AbstractInit;
import org.nightlabs.jfire.init.IDependency;


public class ServerInit extends AbstractInit<ServerInit, ServerInitDependency> {

	private String module;
	private String archive;
	private String initialiserClass;	
	private int priority;
	
	public ServerInit(String module, String archive, String theClass, int priority) {		
		this.module = module;
		this.archive = archive;
		this.initialiserClass = theClass;
		this.priority = priority;
	}
	
	@Override
	protected String getName() {
		return this.getClass().getSimpleName() + ": " + module + '/'
			+ archive + '/' + initialiserClass + " (priority: " + priority + ")";
	}
	
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority The priority to set.
	 */
	public void setPriority(int priority)
	{
		if (priority < 0 || priority > 999)
			throw new IllegalArgumentException("Priority out of range! must be 0..999");

		this.priority = priority;
	}

	public String getInitialiserClass() {
		return initialiserClass;
	}

	public void setInitialiserClass(String initialiserClass) {
		this.initialiserClass = initialiserClass;
	}

	public String getModule() {
		return module;
	}
	
	public String getArchive() {
		return archive;
	}
}

class ServerInitDependency implements IDependency<ServerInit> {
	private String module;
	private String archive;
	private String intialiserClass;
	
	public ServerInitDependency(String module, String archive, String initialiserClass) {
		this.module = module;
		this.archive = archive;
		this.intialiserClass = initialiserClass;
	}
	
	public String getArchive() {
		return archive;
	}
	public void setArchive(String archive) {
		this.archive = archive;
	}	
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getIntialiserClass() {
		return intialiserClass;
	}
	public void setIntialiserClass(String intialiserClass) {
		this.intialiserClass = intialiserClass;
	}
}