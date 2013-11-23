package org.nightlabs.jfire.crossorganisationregistrationinit;

import org.nightlabs.jfire.init.InvocationInitJarEntryHandler;
import org.nightlabs.jfire.init.Resolution;

public class CrossOrganisationRegistrationInitJarEntryHandler
extends InvocationInitJarEntryHandler<CrossOrganisationRegistrationInit, CrossOrganisationRegistrationInitDependency>
{
	@Override
	protected CrossOrganisationRegistrationInit createInvocationInit(String invocation, int priority) {
		return new CrossOrganisationRegistrationInit(invocation, priority);
	}

	@Override
	protected CrossOrganisationRegistrationInitDependency createInvocationInitDependency(String invocation, Resolution resolution) {
		return new CrossOrganisationRegistrationInitDependency(invocation, resolution);
	}

	@Override
	protected String getInitXmlFileName() {
		return "cross-organisation-registration-init.xml";
	}

	@Override
	protected String getRootNodeName() {
		return "cross-organisation-registration-initialisation";
	}

	@Override
	protected String getInitXmlNamespaceURI() {
		return "urn:jfire.org:cross-organisation-registration-init_1_1";
	}
}