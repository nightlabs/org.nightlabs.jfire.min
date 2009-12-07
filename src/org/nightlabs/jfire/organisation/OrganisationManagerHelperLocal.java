package org.nightlabs.jfire.organisation;

import javax.ejb.Local;

import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;

@Local
public interface OrganisationManagerHelperLocal
{
	/**
	 * Some database servers (e.g. PostgreSQL) have problems with structural modifications
	 * of the database and persisting data in the same transaction. This requires to
	 * initialise all JDO meta-data already before in a separate transaction. Therefore
	 * this (newly added 2009-12-07) step0 calls <code>pm.getExtent(...)</code> for all
	 * persistent classes of the JFire Base module in order to create tables in advance.
	 *
	 * @throws Exception if sth. goes wrong.
	 */
	void internalInitializeEmptyOrganisation_step0() throws Exception;

	void internalInitializeEmptyOrganisation_step1(ServerCf localServerCf,
			OrganisationCf organisationCf, String userID, String password) throws Exception;

	void internalInitializeEmptyOrganisation_step2() throws Exception;

	void internalInitializeEmptyOrganisation_step3(ServerCf localServerCf,
			OrganisationCf organisationCf, String userID) throws Exception;
}