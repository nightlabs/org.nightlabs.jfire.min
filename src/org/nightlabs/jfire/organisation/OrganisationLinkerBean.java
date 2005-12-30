/*
 * Created on Dec 21, 2004
 */
package org.nightlabs.jfire.organisation;

import javax.ejb.CreateException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.RegistrationStatus;

import org.nightlabs.ModuleException;

/**
 * This EJB is used anonymously by a foreign organisation to initiate (or cancel)
 * a mutual registration. Therefore, all methods of this bean are called by a server
 * (usually a foreign one) and should NEVER be called by a client directly.
 * <br/><br/>
 * If you want to work with Organisations, use the OrganisationManager EJB or other beans!
 *
 * @ejb.bean name="jfire/ejb/JFireBasePublicBean/OrganisationLinker"
 *	jndi-name="jfire/ejb/JFireBasePublicBean/OrganisationLinkerBean"
 *	type="Stateless"
 *
 * @ejb.util generate = "physical"
 * @ejb.permission unchecked="true"
 */
public abstract class OrganisationLinkerBean implements javax.ejb.SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(OrganisationLinkerBean.class);

	/**
	 * @ejb.create-method  
	 */
	public void ejbCreate()
	throws CreateException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbCreate()");
	}

	/**
	 * This method is called by the organisation which wants to be registered here
	 * (means usually by another server).
	 * <br/><br/>
	 * Do NOT call this method by your client! Use OrganisationManager EJB instead!
	 *
	 * @ejb.interface-method
	 **/
	public void requestRegistration(String registrationID, Organisation applicantOrganisation, String grantOrganisationID, String grantOrganisationUserPassword)
		throws ModuleException
	{
		if (applicantOrganisation == null)
			throw new NullPointerException("applicantOrganisation");

		String applicantOrganisationID = applicantOrganisation.getOrganisationID();

		// We are not authenticated, thus we have to get the PersistenceManager manually.
		// Which is not much work either ;-)
		Lookup lookup = new Lookup(grantOrganisationID);
		PersistenceManager pm = lookup.getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);

			// We need to find out, whether the applicant organisation has already
			// been successfully registered (status accepted).
			// If there is currently a pending registration, it will be cancelled.
			RegistrationStatus.ensureRegisterability(
					pm, localOrganisation, applicantOrganisationID);

			localOrganisation.setPassword(
					applicantOrganisation.getOrganisationID(), grantOrganisationUserPassword);

//			NLJDOHelper.makeDirtyRecursively(applicantOrganisation); // makeDirty seems to have NO effect on a detached object!
			applicantOrganisation = (Organisation) pm.attachCopy(applicantOrganisation, false);

			RegistrationStatus registrationStatus = new RegistrationStatus(
					registrationID, applicantOrganisation);

//			// It seems, there is a bug in JPOX (foreign key constraint error) and therefore,
//			// we need to save the organisation first.
//
//			//...and again for the same reason, we need to persist the server as well...
//			try {
//				pm.getObjectById(ServerID.create(applicantOrganisation.getServer().getServerID()));
//			} catch (JDOObjectNotFoundException x) {
//				pm.makePersistent(applicantOrganisation.getServer());
//			}
//			pm.makePersistent(applicantOrganisation);
//
//
////			JDOHelper.makeDirty(applicantOrganisation, "changeDT");
////			pm.attachCopy(applicantOrganisation, false);

			localOrganisation.addPendingRegistration(registrationStatus);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method is called by the organisation that initiated the
	 * registration (means usually by another server).
	 * <br/><br/>
	 * Do NOT call this method from a client! Use OrganisationManager EJB
	 * instead!
	 *
	 * @ejb.interface-method
	 **/
	public void cancelRegistration(String registrationID, String applicantOrganisationID, String grantOrganisationID)
		throws ModuleException
	{
		if (registrationID == null)
			throw new NullPointerException("registrationID");
		
		// We are not authenticated, thus we have to get the PersistenceManager manually.
		// Which is not much work either ;-)
		Lookup lookup = new Lookup(grantOrganisationID);
		PersistenceManager pm = lookup.getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			RegistrationStatus registrationStatus = localOrganisation.getPendingRegistration(applicantOrganisationID);
			if (registrationStatus == null)
				throw new IllegalArgumentException("No pending registration existing for applicantOrganisation \""+applicantOrganisationID+"\" at grantOrganisation \""+grantOrganisationID+"\"!");

			if (!registrationID.equals(registrationStatus.getRegistrationID()))
				throw new IllegalArgumentException("Pending registration \""+registrationStatus.getRegistrationID()+"\" for applicantOrganisation \""+applicantOrganisationID+"\" at grantOrganisation \""+grantOrganisationID+"\" does not match given registrationID=\""+registrationID+"\"!");

			registrationStatus.cancel(null);
			localOrganisation.removePendingRegistration(applicantOrganisationID);
		} finally {
			pm.close();
		}
	}
}