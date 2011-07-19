package org.nightlabs.jfire.organisationinit;

import java.io.Serializable;

import org.nightlabs.jfire.init.IDependency;
import org.nightlabs.jfire.init.Resolution;

public class OrganisationInitDependency implements IDependency<OrganisationInit>, Serializable
{
	private static final long serialVersionUID = 1L;

	private String invocation;
	private Resolution resolution;

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

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + invocation + " (" + resolution.toString() + ")";
	}

	@Override
	public Resolution getResolution() {
		return resolution;
	}

	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}
}
