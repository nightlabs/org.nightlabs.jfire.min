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

import javax.ejb.CreateException;
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
public abstract class OrganisationLinkerBean implements javax.ejb.SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(OrganisationLinkerBean.class);

	/**
	 * @ejb.create-method  
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}

	/**
	 * This method is called by the organisation which wants to be registered here
	 * (means usually by another server).
	 * <br/><br/>
	 * Do NOT call this method by your client! Use OrganisationManager EJB instead!
	 *
	 * @throws OrganisationAlreadyRegisteredException 
	 *
	 * @ejb.interface-method
	 **/
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
			RegistrationStatus.ensureRegisterability(
					pm, localOrganisation, applicantOrganisationID);

			localOrganisation.setPassword(
					applicantOrganisation.getOrganisationID(), grantOrganisationUserPassword);

//			NLJDOHelper.makeDirtyRecursively(applicantOrganisation); // makeDirty seems to have NO effect on a detached object!
			applicantOrganisation = pm.makePersistent(applicantOrganisation);

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
