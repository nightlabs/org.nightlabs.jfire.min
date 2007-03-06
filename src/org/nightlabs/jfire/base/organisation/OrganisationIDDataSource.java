package org.nightlabs.jfire.base.organisation;

import java.util.Collection;

import org.nightlabs.jfire.organisation.id.OrganisationID;

public interface OrganisationIDDataSource
{
	Collection<OrganisationID> getOrganisationIDs();
}
