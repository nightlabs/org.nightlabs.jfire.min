package org.nightlabs.jfire.organisation;

import javax.ejb.Local;

import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;

@Local
public interface OrganisationManagerHelperLocal 
{
	void internalInitializeEmptyOrganisation_step1(ServerCf localServerCf,
			OrganisationCf organisationCf, String userID, String password);

	void internalInitializeEmptyOrganisation_step2();

	void internalInitializeEmptyOrganisation_step3(ServerCf localServerCf,
			OrganisationCf organisationCf, String userID);
}