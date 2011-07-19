package org.nightlabs.jfire.organisationinit;

import java.io.Serializable;

import org.nightlabs.jfire.init.IDependency;
import org.nightlabs.jfire.init.Resolution;

public class OrganisationInitDependency implements IDependency<OrganisationInit>, Serializable
{
	private static final long serialVersionUID = 1L;

	private String invocation;
//	private String module;
//	private String archive;
//	private String bean;
//	private String method;
	private Resolution resolution;

//	public OrganisationInitDependency(String module, String archive, String bean, String method, Resolution res) {
//		this.module = module;
//		this.archive = archive;
//		this.bean = bean;
//		this.method = method;
//		this.resolution = res;
//	}
	public OrganisationInitDependency(String invocation, Resolution res) {
		if (invocation == null)
			throw new IllegalArgumentException("invocation == null");
		
		this.invocation = invocation;
		this.resolution = res;
	}
	
	public String getInvocation() {
		return invocation;
	}
	
	public void setInvocation(String invocation) {
		this.invocation = invocation;
		this.invocationPath = null;
	}
	
	private String[] invocationPath;
	
	public String[] getInvocationPath() {
		if (invocationPath == null) {
			this.invocationPath = invocation.split(".");
		}
		return invocationPath;
	}

//	public String getArchive() {
//		return archive;
//	}
//
//	public void setArchive(String archive) {
//		this.archive = archive;
//	}
//
//	public String getBean() {
//		return bean;
//	}
//
//	public void setBean(String bean) {
//		this.bean = bean;
//	}
//
//	public String getMethod() {
//		return method;
//	}
//
//	public void setMethod(String method) {
//		this.method = method;
//	}
//
//	public String getModule() {
//		return module;
//	}
//
//	public void setModule(String module) {
//		this.module = module;
//	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + invocation + " (" + resolution.toString() + ")";
	}

	public Resolution getResolution() {
		return resolution;
	}

	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}
}
