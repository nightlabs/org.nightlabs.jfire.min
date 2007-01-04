package org.nightlabs.jfire.datastoreinit;

import org.nightlabs.jfire.init.AbstractInit;
import org.nightlabs.jfire.init.IDependency;

public class DatastoreInit extends AbstractInit<DatastoreInit, DatastoreInitDependency> {

	private String module;
	private String archive;
	private String bean;
	private String method;
	private int priority;

	public DatastoreInit(String module, String archive, String bean, String method, int priority) {
		this.module = module;
		this.archive = archive;
		this.bean = bean;
		this.method = method;
		this.priority = priority;
	}

	@Override
	protected String getName() {
	
		return this.getClass().getSimpleName() + ": " + module + '/' + archive + '/' + bean + '#' + method
			+ " (priority: " + priority + ")";		
	}

	public String getBean() {
		return bean;
	}

	public void setBean(String bean) {
		this.bean = bean;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority
	 *          The priority to set.
	 */
	public void setPriority(int priority) {
		if (priority < 0 || priority > 999)
			throw new IllegalArgumentException("Priority out of range! must be 0..999");

		this.priority = priority;
	}

	public String getArchive() {
		return archive;
	}

	public String getModule() {
		return module;
	}
}

class DatastoreInitDependency implements IDependency<DatastoreInit> {
	private String module;
	private String archive;
	private String bean;
	private String method;
	private Resolution resolution;
	
	public DatastoreInitDependency(String module, String archive, String bean, String method, Resolution res) {
		this.module = module;
		this.archive = archive;
		this.bean = bean;
		this.method = method;
		this.resolution = res;
	}

	public String getArchive() {
		return archive;
	}

	public void setArchive(String archive) {
		this.archive = archive;
	}

	public String getBean() {
		return bean;
	}

	public void setBean(String bean) {
		this.bean = bean;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + module + '/'
			+ archive + '/' + bean + '#' + method + " (" + resolution.toString() + ")";
	}

	public Resolution getResolution() {
		return resolution;
	}
	
	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}
}