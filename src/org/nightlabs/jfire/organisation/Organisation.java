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
import java.util.Date;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.util.Util;

/**
 * @author marco
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *   	objectid-class="org.nightlabs.jfire.organisation.id.OrganisationID"
 *		detachable="true"
 *		table="JFireBase_Organisation"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.fetch-group name="Organisation.person" fields="person"
 * @jdo.fetch-group name="Organisation.server" fields="server"
 */
@PersistenceCapable(
	objectIdClass=OrganisationID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Organisation")
@FetchGroups({
	@FetchGroup(
		name=Organisation.FETCH_GROUP_PERSON,
		members=@Persistent(name="person")),
	@FetchGroup(
		name=Organisation.FETCH_GROUP_SERVER,
		members=@Persistent(name="server"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Organisation implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PERSON = "Organisation.person";
	public static final String FETCH_GROUP_SERVER = "Organisation.server";

	/**
	 * @see #getRootOrganisationID(InitialContext)
	 */
	public static final String ROOT_ORGANISATION_ID_JNDI_NAME = "jfire/system/RootOrganisationID"; // we need it accessible in the client as well
//	public static final String ROOT_ORGANISATION_ID_JNDI_NAME = "java:/jfire/system/RootOrganisationID"; // this would be only accessible within the server-JVM

	/**
	 * In every JFire network, there exists one special {@link org.nightlabs.jfire.organisation.Organisation}
	 * which defines network-wide standards. This organisation is called root-organisation.
	 * <p>
	 * The <tt>JFireServerManager</tt> has a {@link org.nightlabs.config.ConfigModule} in which
	 * this root-organisation is registered. When the server starts, it writes the organisationID
	 * of this special organisation into JNDI under the name {@link #ROOT_ORGANISATION_ID_JNDI_NAME}.
	 * </p>
	 * <p>
	 * Note, that in contrast to {@link #DEV_ORGANISATION_ID} (which is just a namespace),
	 * the network's root-organisation must exist and be online (i.e. be a real organisation in the same network).
	 * </p>
	 * <p>
	 * This method can be called in the client as well.
	 * </p>
	 * <p>
	 * If the server is in stand-alone-mode (i.e. working <b>without</b> a root-organisation), this method
	 * returns the current organisation (i.e. the one of the user who calls this method).
	 * </p>
	 *
	 * @param initialContext Access to JNDI.
	 * @return Returns the organisation-id of the network's root-organisation.
	 */
	public static String getRootOrganisationID(InitialContext initialContext)
	{
		UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		if (userDescriptor == null) // we check always for the current user in order to prevent bugs that only happen in stand-alone-mode
			throw new IllegalStateException("Anonymous user cannot call this method! You must be authenticated!");

		String rootOrganisationID = _getRootOrganisationID(initialContext);
		if (rootOrganisationID.equals(""))
			return userDescriptor.getOrganisationID();
		else
			return rootOrganisationID;
	}
	protected static String _getRootOrganisationID(InitialContext initialContext)
	{
		try {
			return (String) initialContext.lookup(ROOT_ORGANISATION_ID_JNDI_NAME);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns whether a root organisation exists. In stand-alone mode (only one organisation, no network), a root organisation does not exist and the
	 * root organisation ID is empty. You should however not test directly for the empty string but use this method instead.
	 * @param initialContext The {@link InitialContext} to access JNDI.
	 * @return A boolean indicating whether a root organisation exists.
	 */
	public static boolean hasRootOrganisation(InitialContext initialContext) {
		return ! _getRootOrganisationID(initialContext).equals(""); // order matters since we want to know when getRootOrganisationID() returns null which should never happen
	}

	/**
	 * This is a convenience method.
	 *
	 * @see #getRootOrganisationID(InitialContext)
	 */
	public static Organisation getRootOrganisation(InitialContext initialContext, PersistenceManager pm)
	{
		return getOrganisation(pm, getRootOrganisationID(initialContext));
	}

	/**
	 * Some objects are predefined by the developers and sometimes even an essential part of
	 * the system (and therefore known to the system itself). In order to give them a static organisation-id
	 * which allows them to be addressed by the client or another module, they use the
	 * organisationID <tt>dev.jfire.org</tt> defined by this constant.
	 * <p>
	 * Note, that this organisation does not exist and the ID serves only as a namespace. You
	 * should not confuse this with a network's root-organisation
	 * (see {@link #getRootOrganisationID(InitialContext)}).
	 * <p>
	 * If you want to extend JFire independently (and you're not a member of the development
	 * team), you can use your own domain as namespace for your modules.
	 */
	public static final String DEV_ORGANISATION_ID = "dev.jfire.org";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 * @jdo.column length="100"
//	 */
//	private String masterOrganisationID = null;


	protected Organisation() { }

	public Organisation(String _organisationID)
	{
		assertValidOrganisationID(_organisationID);

		this.organisationID = _organisationID;
		this.createDT = new Date();
		this.changeDT = new Date();
//		this.masterOrganisationID = _organisationID;
	}

	/**
	 * This method checks whether a given unique identifier is valid to identify an organisation
	 * in the jfire-system. This method delegates to {@link Server#isValidServerID(String)}, because
	 * the rules for server-ids and organisation-ids are the same.
	 *
	 * @param organisationID The organisation-id to be checked for validity
	 * @return <code>true</code> if it contains solely valid characters, <code>false</code> if the given <code>organisationID</code>
	 *		contains illegal chars and must not be used.
	 * @see #assertValidOrganisationID(String)
	 */
	public static boolean isValidOrganisationID(String organisationID)
	{
		return Server.isValidServerID(organisationID);
	}

	/**
	 * Check the validity of a given organisation-id. This method calls {@link #isValidOrganisationID(String)}
	 * and - if it returns <code>false</code> - throws an {@link IllegalArgumentException}.
	 *
	 * @param organisationID The organisation-id to be checked for validity
	 * @throws IllegalArgumentException if the given <code>organisationID</code> is not valid.
	 */
	public static void assertValidOrganisationID(String organisationID)
	throws IllegalArgumentException
	{
		if (!isValidOrganisationID(organisationID))
			throw new IllegalArgumentException("organisationID \"" + organisationID + "\" is not valid! Must be a valid host-style identifier (e.g. \"jfire.organisation.tld\").");
	}

//	public Organisation(String _organisationID, String _masterOrganisationID)
//	{
//		this.organisationID = _organisationID;
//		this.masterOrganisationID = _masterOrganisationID;
//	}

	public String getOrganisationID()
  {
		return organisationID;
	}

//	public void setOrganisationID(String _organisationID)
//	{
//		if (this.organisationID != null)
//			throw new IllegalStateException("You cannot change the organisationID after it has been initialized!");
//
//		this.organisationID = _organisationID;
//	}

//	/**
//	 * The property masterOrganisationID is identical with organisationID if this
//	 * is a real organisation. But, if this organisation is a representative, it
//	 * points to the master that it represents.
//	 *
//	 * @return Returns the masterOrganisationID.
//	 */
//	public String getMasterOrganisationID() {
//		return masterOrganisationID;
//	}
//
//	/**
//	 * @param masterOrganisationID The masterOrganisationID to set.
//	 * @see getMasterOrganisationID()
//	 */
//	public void setMasterOrganisationID(String _masterOrganisationID) {
//		this.masterOrganisationID = _masterOrganisationID;
//	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createDT;

	/**
	 * @return Returns the createDT.
	 */
	public Date getCreateDT() {
		return createDT;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date changeDT;

	/**
	 * @return Returns the changeDT.
	 */
	public Date getChangeDT() {
		return changeDT;
	}

	/**
	 * @param changeDT The changeDT to set.
	 */
	public void setChangeDT(Date changeDT) {
		this.changeDT = changeDT;
	}

	public void setChangeDT() {
		this.changeDT = new Date();
	}


	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Server server;

	/**
	 * @return Returns the server.
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * @param server The server to set.
	 */
	public void setServer(Server server) {
		this.server = server;
		setChangeDT();
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Person person = null;

	/**
	 * @return Returns the person.
	 */
	public Person getPerson() {
		return person;
	}
	/**
	 * @param person The person to set.
	 */
	public void setPerson(Person person) {
		if (person == null)
			throw new NullPointerException("person");

		this.person = person;
		setChangeDT();
	}

	/**
	 * Calls getOrganisation(pm, organisationID, true);
	 * @param pm
	 * @param organisationID
	 * @return the Organisation-Object for the given organisationID
	 */
	public static Organisation getOrganisation(PersistenceManager pm, String organisationID) {
		return getOrganisation(pm, organisationID, true);
	}

	/**
	 * @param pm
	 * @param organisationID
	 * @param throwExceptionIfNotFound If true throws a JDOObjectNotFoundException if Organisation not existent
	 * @return the Organisation-Object for the given organisationID
	 */
	public static Organisation getOrganisation(PersistenceManager pm, String organisationID, boolean throwExceptionIfNotFound)
	{
		pm.getExtent(Organisation.class);
		Organisation organisation = null;
		try {
			organisation = (Organisation) pm.getObjectById(OrganisationID.create(organisationID), true);
			organisation.getChangeDT(); // TODO WORKAROUND for JPOX bug
		} catch (JDOObjectNotFoundException x) {
			if (throwExceptionIfNotFound)
				throw x;
		}
		return organisation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Organisation other = (Organisation) obj;
		return Util.equals(this.organisationID, other.organisationID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ']';
	}
}
