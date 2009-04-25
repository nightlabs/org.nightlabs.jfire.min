package org.nightlabs.jfire.organisation;

import javax.ejb.Remote;

@Remote
public interface OrganisationLinkerRemote 
{
	/**
	 * This method is called by the organisation which wants to be registered here
	 * (means usually by another server).
	 * <br/><br/>
	 * Do NOT call this method by your client! Use OrganisationManager EJB instead!
	 *
	 * @throws OrganisationAlreadyRegisteredException
	 */
	void requestRegistration(String registrationID,
			Organisation applicantOrganisation, String grantOrganisationID,
			String grantOrganisationUserPassword)
			throws OrganisationAlreadyRegisteredException;

	/**
	 * This method is called by the organisation that initiated the
	 * registration (means usually by another server) - the so-called applicant-organisation.
	 * <br/><br/>
	 * Do NOT call this method from a client! Use OrganisationManager EJB
	 * instead!
	 */
	void cancelRegistration(String registrationID,
			String applicantOrganisationID, String grantOrganisationID);
}