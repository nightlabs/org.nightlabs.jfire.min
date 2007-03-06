package org.nightlabs.jfire.base.organisation;

import org.nightlabs.jfire.organisation.Organisation;

public interface OrganisationFilter
{
	boolean includeOrganisation(Organisation organisation);
}
