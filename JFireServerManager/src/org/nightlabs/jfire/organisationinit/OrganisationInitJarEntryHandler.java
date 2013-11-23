package org.nightlabs.jfire.organisationinit;

import org.nightlabs.jfire.init.InvocationInitJarEntryHandler;
import org.nightlabs.jfire.init.Resolution;

public class OrganisationInitJarEntryHandler extends InvocationInitJarEntryHandler<OrganisationInit, OrganisationInitDependency>
{
	@Override
	protected OrganisationInit createInvocationInit(String invocation, int priority) {
		return new OrganisationInit(invocation, priority);
	}

	@Override
	protected OrganisationInitDependency createInvocationInitDependency(String invocation, Resolution resolution) {
		return new OrganisationInitDependency(invocation, resolution);
	}

	@Override
	protected String getInitXmlFileName() {
		return "organisation-init.xml";
	}

	@Override
	protected String getRootNodeName() {
		return "organisation-initialisation";
	}

	@Override
	protected String getInitXmlNamespaceURI() {
		return "urn:jfire.org:organisation-init_1_1";
	}
}