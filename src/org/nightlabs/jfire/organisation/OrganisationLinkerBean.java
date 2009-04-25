/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.organisation;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.Lookup;

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
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 * @ejb.permission unchecked="true"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
// It is essential that unknown business partners can access this bean (otherwise they could not *initiate* a
// cross-organisation-contact). Therefore, we permit access to all. Marco.
@PermitAll
public abstract class OrganisationLinkerBean implements OrganisationLinkerRemote
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(OrganisationLinkerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.organisation.OrganisationLinkerRemote#requestRegistration(java.lang.String, org.nightlabs.jfire.organisation.Organisation, java.lang.String, java.lang.String)
	 */
	@Override
	public void requestRegistration(String registrationID, Organisation applicantOrganisation, String grantOrganisationID, String grantOrganisationUserPassword)
	throws OrganisationAlreadyRegisteredException
	{
		if (applicantOrganisation == null)
			throw new IllegalArgumentException("applicantOrganisation must not be null!");

		String applicantOrganisationID = applicantOrganisation.getOrganisationID();

		logger.info("***");
		logger.info("requestRegistration(...) entered");
		logger.info("  registrationID="+registrationID);
		logger.info("  grantOrganisationID="+grantOrganisationID);
		logger.info("  applicantOrganisationID="+applicantOrganisationID);
		logger.info("***");

		// We are not authenticated, thus we have to get the PersistenceManager manually.
		// Which is not much work either ;-)
		Lookup lookup = new Lookup(grantOrganisationID);
		PersistenceManager pm = lookup.getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);

			// We need to find out, whether the applicant organisation has already
			// been successfully registered (status accepted).
			// If there is currently a pending registration, it will be cancelled.
			RegistrationStatus.ensureRegisterability(pm, localOrganisation, applicantOrganisationID);

			localOrganisation.setPassword(applicantOrganisation.getOrganisationID(), grantOrganisationUserPassword);

			// Since this method is executed anonymously, it is essential that nothing is written to "real" tables.
			// If we persisted the organisation here, it would be possible to overwrite anonymously Server objects and thus
			// compromise the grant-organisation's data. Therefore we capsule it in a TemporaryOrganisation object.
			TemporaryOrganisation temporaryApplicantOrganisation = new TemporaryOrganisation(applicantOrganisation);
			temporaryApplicantOrganisation = pm.makePersistent(temporaryApplicantOrganisation);

			RegistrationStatus registrationStatus = new RegistrationStatus(registrationID, temporaryApplicantOrganisation);
			localOrganisation.addPendingRegistration(registrationStatus);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.organisation.OrganisationLinkerRemote#cancelRegistration(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void cancelRegistration(String registrationID, String applicantOrganisationID, String grantOrganisationID)
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
				throw new NoPendingRegistrationExistingException("No pending registration existing for applicantOrganisation \""+applicantOrganisationID+"\" at grantOrganisation \""+grantOrganisationID+"\"!");

			if (!registrationID.equals(registrationStatus.getRegistrationID()))
				throw new IllegalArgumentException("Pending registration \""+registrationStatus.getRegistrationID()+"\" for applicantOrganisation \""+applicantOrganisationID+"\" at grantOrganisation \""+grantOrganisationID+"\" does not match given registrationID=\""+registrationID+"\"!");

			registrationStatus.cancel(null);
			localOrganisation.removePendingRegistration(applicantOrganisationID);
		} finally {
			pm.close();
		}
	}
}
