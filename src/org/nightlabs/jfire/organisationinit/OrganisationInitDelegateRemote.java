package org.nightlabs.jfire.organisationinit;

import javax.ejb.Remote;

import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.crossorganisationregistrationinit.CrossOrganisationRegistrationInit;

@Remote
public interface OrganisationInitDelegateRemote
{
	void invokeOrganisationInitInNestedTransaction(OrganisationInit init)
			throws Exception;

	void invokeCrossOrganisationRegistrationInitInNestedTransaction(
			CrossOrganisationRegistrationInit init, Context context)
			throws Exception;
}