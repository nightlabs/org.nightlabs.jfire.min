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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.organisation.id.RegistrationStatusID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;


/**
 * Before two organisations can cooperate (buy/sell stuff from each other),
 * they need to be mutually registered. This registration has two steps:
 * <ul>
 *   <li>
 *     User A commands his Organisation A to register Organisation B.
 *     By doing that, a user for Organisation B is created within Organisation A
 *     and both Organisations have an instance of RegistrationStatus created.
 *   </li>
 *   <li>
 *     User B tells his Organisation B that it should either reject or accept
 *     the registration request of Organisation A. If it is accepted, a user
 *     for the Organisation A will be generated and the password will be
 *     transferred to Organisation A.
 *   </li>
 *   <li>
 *     Additionally, Organisation A is able to cancel its request before it
 *     has been accepted/rejected by Organisation B.
 *   </li>
 * </ul>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *   	objectid-class="org.nightlabs.jfire.organisation.id.RegistrationStatusID"
 *		detachable="true"
 *		table="JFireBase_RegistrationStatus"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.fetch-group name="RegistrationStatus.users" fields="openUser, closeUser"
 *
 * @jdo.query name="getRegistrationStatusCountForOrganisation" query="
 *		SELECT count(this.registrationID)
 *		WHERE this.organisationID == pOrganisationID
 *		PARAMETERS String pOrganisationID
 *		import java.lang.String"
 */
@PersistenceCapable(
	objectIdClass=RegistrationStatusID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_RegistrationStatus")
@FetchGroups(
	@FetchGroup(
		name=RegistrationStatus.FETCH_GROUP_USERS,
		members={@Persistent(name="openUser"), @Persistent(name="closeUser")})
)
@Queries(
	@javax.jdo.annotations.Query(
		name="getRegistrationStatusCountForOrganisation",
		value=" SELECT count(this.registrationID) WHERE this.organisationID == pOrganisationID PARAMETERS String pOrganisationID import java.lang.String")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RegistrationStatus
		implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static final String DIRECTION_OUTGOING = "outgoing";
	public static final String DIRECTION_INCOMING = "incoming";

	public static final String STATUS_PENDING = "pending";
	public static final String STATUS_ACCEPTED = "accepted";
	public static final String STATUS_REJECTED = "rejected";
	public static final String STATUS_CANCELLED = "cancelled";

	public static final String FETCH_GROUP_USERS = "RegistrationStatus.users";

	public static long getRegistrationStatusCount(PersistenceManager pm, String organisationID)
	{
		Query q = pm.newNamedQuery(RegistrationStatus.class, "getRegistrationStatusCountForOrganisation");
		//Long res = (Long) ((Collection)q.execute(organisationID)).iterator().next();
		Long res = (Long) q.execute(organisationID);
		return res.longValue();
	}

	public static void ensureRegisterability(
			PersistenceManager pm,
			LocalOrganisation localOrganisation,
			String otherOrganisationID)
		throws OrganisationAlreadyRegisteredException
	{
		Query query = pm.newQuery(RegistrationStatus.class, "this.organisationID == applicantOrganisationID && this.status == statusAccepted");
		query.declareImports("import "+String.class.getName());
		query.declareParameters("String applicantOrganisationID, String statusAccepted");
		Collection<?> c = (Collection<?>) query.execute(otherOrganisationID, RegistrationStatus.STATUS_ACCEPTED);

		if (!c.isEmpty())
			throw new OrganisationAlreadyRegisteredException("The organisation \""+otherOrganisationID+"\" is already registered (status accepted) at organisation \""+localOrganisation.getOrganisationID()+"\"!");

		RegistrationStatus registrationStatus = localOrganisation.getPendingRegistration(otherOrganisationID);
		if (registrationStatus != null) {
			// If there is already a pending registration, cancel it.
			// If it is not pending, it's at least sure that it is not accepted
			// (because of check above) and therefore, it doesn't matter if it
			// gets overwritten by a new one - and of course,
			// it doesn't need to be cancelled, either.
			if (RegistrationStatus.STATUS_PENDING.equals(registrationStatus.getStatus()))
				registrationStatus.cancel(null);
		}
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String registrationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String direction;

	/**
	 * The organisation which is initialised by accept(...)
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Organisation organisation = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private TemporaryOrganisation temporaryOrganisation = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String status;

	/**
	 * When has the request for registration been done, means the registration
	 * been opened.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date openDT;
	/**
	 * By whom was the registration done. Note, that this remains <tt>null</tt> in the
	 * organisation which is the receiver of the request, because the
	 * request needs to be done anonymously (logically - there's no user yet).
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User openUser = null;

	/**
	 * When was the pending state closed. This can either be done by the organisation
	 * that has been asked (accept / reject) or by the organisation that was asking
	 * (cancel).
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date closeDT = null;
	/**
	 * Who closed the pending registration state. This is <tt>null</tt> on the side,
	 * where the command is issued by the other organisation (and not a "real" user).
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User closeUser = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String initialContextFactory = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String initialContextURL = null;

	/**
	 * @deprecated This constructor is only existing for JDO.
	 */
	@Deprecated
	protected RegistrationStatus()
	{
	}

	/**
	 * Constructor to use if we are the ones who apply. There is no
	 * <code>Organisation</code> object existing yet, but we now our <code>User</code>.
	 *
	 * @param organisationID
	 * @param user
	 */
	public RegistrationStatus(
			String registrationID, String organisationID, User user,
			String initialContextFactory, String initialContextURL)
	{
		if (registrationID == null)
			throw new NullPointerException("registrationID");

		this.registrationID = registrationID;
		this.organisationID = organisationID;
		this.openDT = new Date();
		this.openUser = user;
		this.direction = DIRECTION_OUTGOING;
		this.status = STATUS_PENDING;
		this.initialContextFactory = initialContextFactory;
		this.initialContextURL = initialContextURL;
	}

	/**
	 * Constructor to use if another organisation applies for registration
	 * here. In this case, we know the <code>Organisation</code> object, but no user.
	 *
	 * @param organisation
	 * @param direction
	 */
	public RegistrationStatus(String registrationID, TemporaryOrganisation temporaryOrganisation)
	{
		if (registrationID == null)
			throw new NullPointerException("registrationID");

		if (temporaryOrganisation == null)
			throw new NullPointerException("temporaryOrganisation");

		this.registrationID = registrationID;
		this.temporaryOrganisation = temporaryOrganisation;
		this.organisationID = temporaryOrganisation.getOrganisationID();
		this.direction = DIRECTION_INCOMING;
		this.openDT = new Date();
		this.status = STATUS_PENDING;
	}
	/**
	 * @return Returns the registrationID.
	 */
	public String getRegistrationID()
	{
		return registrationID;
	}
	public TemporaryOrganisation getTemporaryOrganisation() {
		return temporaryOrganisation;
	}
	public Organisation getOrganisation() {
		return organisation;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the type.
	 */
	public String getDirection()
	{
		return direction;
	}
	/**
	 * @return Returns the status.
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * Close this registration and set the status to STATUS_ACCEPTED.
	 * This is only possible, if the current status is STATUS_PENDING.
	 *
	 * @param user Must NOT be <tt>null</tt>.
	 */
	public void accept(User user)
	{
		if (user == null)
			throw new NullPointerException("user");

		if (!STATUS_PENDING.equals(getStatus()))
			throw new IllegalStateException("Cannot accept, because status is not STATUS_PENDING!");

		// persist the organisation that's currently wrapped and held in serialized form
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager! This method must be called while the RegistrationStatus is attached to a datastore!");

		if (temporaryOrganisation != null) {
			// Obtain the Organisation object from the temporaryOrganisation.
			Organisation org = temporaryOrganisation.getOrganisation();

			// Delete the temporaryOrganisation.
			TemporaryOrganisation tempOrg = this.temporaryOrganisation;
			this.temporaryOrganisation = null;
			pm.deletePersistent(tempOrg);

			// Persist the Organisation (after making it completely dirty).
			NLJDOHelper.makeDirtyAllFieldsRecursively(org);
			org = pm.makePersistent(org);
			this.organisation = org;
		}

		this.closeDT = new Date();
		this.closeUser = user;
		this.status = STATUS_ACCEPTED;
	}

	/**
	 * Close this registration and set the status to STATUS_CANCELLED.
	 * This is only possible, if the current status is STATUS_PENDING.
	 * Note that <tt>cancel</tt> and <tt>reject</tt> will both delete the
	 * <tt>Organisation</tt> object out of the datastore (if it exists).
	 *
	 * @param user Can be <tt>null</tt>, because the user is not known in the remote
	 *  "granting" organisation.
	 *
	 * @see #reject(User)
	 * @see #cancelOrReject(User, String)
	 */
	public void cancel(User user)
	{
		cancelOrReject(user, STATUS_CANCELLED);
	}

	/**
	 * This method does nearly the same as cancel, except that it uses STATUS_REJECTED.
	 *
	 * @see #cancel(User)
	 * @see #cancelOrReject(User, String)
	 */
	public void reject(User user)
	{
		cancelOrReject(user, STATUS_REJECTED);
	}

	/**
	 * This method is called by <tt>cancel</tt> and by <tt>reject</tt> and does the actual
	 * work.
	 *
	 * @param user The user who is responsible (can be <tt>null</tt> if not known).
	 * @param status One of <tt>STATUS_CANCELLED</tt> or <tt>STATUS_REJECTED</tt>.
	 *
	 * @see #cancel(User)
	 * @see #reject(User)
	 */
	protected void cancelOrReject(User user, String status)
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of RegistrationStatus is currently not persistent!");

		if (!STATUS_PENDING.equals(getStatus()))
			throw new IllegalStateException("Cannot change status, because current status is not STATUS_PENDING!");

		this.closeDT = new Date();
		this.closeUser = user;
		this.status = status;

		if (this.temporaryOrganisation != null) {
			TemporaryOrganisation org = this.temporaryOrganisation;
			this.temporaryOrganisation = null;
			pm.deletePersistent(org);
		}
	}

//	private void nullifyOrganisation()
//	{
//		this.organisation = null;
//	}

	/**
	 * @return Returns the closeDT.
	 */
	public Date getCloseDT()
	{
		return closeDT;
	}
	/**
	 * @return Returns the closeUser.
	 */
	public User getCloseUser()
	{
		return closeUser;
	}
	/**
	 * @return Returns the openDT.
	 */
	public Date getOpenDT()
	{
		return openDT;
	}
	/**
	 * @return Returns the openUser.
	 */
	public User getOpenUser()
	{
		return openUser;
	}
	/**
	 * @return Returns the initialContextFactory.
	 */
	public String getInitialContextFactory()
	{
		return initialContextFactory;
	}
	/**
	 * @return Returns the initialContextURL.
	 */
	public String getInitialContextURL()
	{
		return initialContextURL;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((registrationID == null) ? 0 : registrationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final RegistrationStatus other = (RegistrationStatus) obj;
		return Util.equals(this.registrationID, other.registrationID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + registrationID + ',' + organisationID + ',' + direction + ']';
	}
}
