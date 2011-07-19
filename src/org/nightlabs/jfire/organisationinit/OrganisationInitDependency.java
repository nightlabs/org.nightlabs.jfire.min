package org.nightlabs.jfire.organisationinit;

import org.nightlabs.jfire.init.InvocationInitDependency;
import org.nightlabs.jfire.init.Resolution;

public class OrganisationInitDependency extends InvocationInitDependency<OrganisationInit>
{
	private static final long serialVersionUID = 1L;

	public OrganisationInitDependency(String invocation, Resolution res) {
		super(invocation, res);
	}
}
