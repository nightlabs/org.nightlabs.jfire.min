package org.nightlabs.jfire.crossorganisationregistrationinit;

import java.io.Serializable;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.organisation.RegistrationStatus;
import org.nightlabs.jfire.organisation.id.RegistrationStatusID;

public class Context
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String otherOrganisationID;
	private RegistrationStatusID registrationStatusID;

	public Context(RegistrationStatus registrationStatus)
	{
		this.otherOrganisationID = registrationStatus.getOrganisationID();
		this.registrationStatusID = (RegistrationStatusID) JDOHelper.getObjectId(registrationStatus);

		if (this.otherOrganisationID == null)
			throw new IllegalArgumentException("registrationStatus.getOrganisationID() returned null!");

		if (this.registrationStatusID == null)
			throw new IllegalArgumentException("JDOHelper.getObjectId(registrationStatus) returned null! The parameter seems not to be persistent!");
	}

	/**
	 * Get the <b>other</b> organisation's ID.
	 *
	 * @return the id of the other organisation.
	 */
	public String getOtherOrganisationID() {
		return otherOrganisationID;
	}

	public RegistrationStatusID getRegistrationStatusID() {
		return registrationStatusID;
	}
}
