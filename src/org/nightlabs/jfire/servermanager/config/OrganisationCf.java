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

package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.server.Server;

/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class OrganisationCf
	implements Serializable, Comparable<OrganisationCf>, Cloneable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String DATASOURCE_PREFIX_RELATIVE = "jfire/datasource/";
	public static final String DATASOURCE_PREFIX_ABSOLUTE = "java:/jfire/datasource/";

	public static final String PERSISTENCE_MANAGER_FACTORY_PREFIX_RELATIVE =	"jfire/persistenceManagerFactory/";
	public static final String PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE =	"java:/jfire/persistenceManagerFactory/";

	private String organisationID;
	private String organisationName;
//	private String masterOrganisationID;
//	private String persistenceManagerFactoryJNDIName;
	private Set<String> serverAdmins = null;

	private boolean readOnly = false;

	/**
	 * The parent config module. This is only set if a config module
	 * owns this instance.
	 */
	private ConfigModule parentConfigModule = null;
	
	public OrganisationCf() { }

	public OrganisationCf(
			String _organisationID, String _organisationName)
//			String _masterOrganisationID) // , String _persistenceManagerJNDIName)
	{
		if (_organisationID == null)
			throw new NullPointerException("organisationID must not be null!");
		if (_organisationName == null)
			throw new NullPointerException("organisationName must not be null!");
//		if (_persistenceManagerJNDIName == null)
//			throw new NullPointerException("persistenceManagerFactoryJNDIName must not be null!");

		if (!ObjectIDUtil.isValidIDString(_organisationID))
			throw new IllegalArgumentException("organisationID \""+_organisationID+"\" is not a valid id!");

		this.organisationID = _organisationID;
		this.organisationName = _organisationName;
//		if (_masterOrganisationID == null)
//			_masterOrganisationID = _organisationID;
//		this.masterOrganisationID = _masterOrganisationID;
//		this.persistenceManagerFactoryJNDIName = _persistenceManagerJNDIName;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() 
	{
		return organisationID;
	}
	
	/**
	 * @param organisationID The organisationID to set.
	 */
	public void setOrganisationID(String _organisationID) 
	{
		assertWritable();
		if (_organisationID == null)
			throw new NullPointerException("organisationID must not be null!");
		if (!ObjectIDUtil.isValidIDString(_organisationID))
			throw new IllegalArgumentException("organisationID \""+_organisationID+"\" is not a valid id!");
		this.organisationID = _organisationID;
		setChanged();
	}
	
	/**
	 * @return Returns the organisationName.
	 */
	public String getOrganisationName() 
	{
		return organisationName;
	}
	
	/**
	 * @param organisationName The organisationName to set.
	 */
	public void setOrganisationName(String _organisationCaption)
	{
		assertWritable();
		if (_organisationCaption == null)
			throw new NullPointerException("organisationName must not be null!");
		this.organisationName = _organisationCaption;
		setChanged();
	}
	
//	/**
//	 * @return Returns the masterOrganisationID.
//	 */
//	public String getMasterOrganisationID() {
//		return masterOrganisationID;
//	}
//	/**
//	 * @param masterOrganisationID The masterOrganisationID to set.
//	 */
//	public void setMasterOrganisationID(String _masterOrganisationID) {
//		assertWritable();
//		if (_masterOrganisationID == null)
//			throw new NullPointerException("masterOrganisationID must not be null!");
//		this.masterOrganisationID = _masterOrganisationID;
//		thisString = null;
//	}
//	/**
//	 * @return Returns the persistenceManagerFactoryJNDIName.
//	 */
//	public String getPersistenceManagerFactoryJNDIName() {
//		return persistenceManagerFactoryJNDIName;
//	}
//	/**
//	 * @param persistenceManagerFactoryJNDIName The persistenceManagerFactoryJNDIName to set.
//	 */
//	public void setPersistenceManagerFactoryJNDIName(String _persistenceManagerJNDIName)
//	{
//		assertWritable();
//		if (_persistenceManagerJNDIName == null)
//			throw new NullPointerException("persistenceManagerFactoryJNDIName must not be null!");
//		this.persistenceManagerFactoryJNDIName = _persistenceManagerJNDIName;
//		thisString = null;
//	}

	/**
	 * If this instance is set readOnly, this method will return a copy of
	 * Set serverAdmins. Later, it won't return a copy, but a readonly Set
	 * that throws exceptions when write methods are executed.
	 * <br/><br/>
	 * Anyway, you should never use the returned Set
	 * for write accesses! Use addServerAdmin(...) and removeServerAdmin(...)
	 * for manipulations.
	 * <br/><br/>
	 * Note that this method will return <code>null</code> if no serverAdmins
	 * are existent. It will never return an empty Set as it is nulled if it
	 * becomes empty.
	 *
	 * @return Returns the serverAdmins.
	 * @see addServerAdmin(String userID)
	 * @see removeServerAdmin(String userID)
	 */
	public Set<String> getServerAdmins() 
	{
		if (readOnly && serverAdmins != null)
			return new HashSet<String>(serverAdmins);

		return serverAdmins;
	}
	
	/**
	 * After having set a new list of serverAdmins, don't manipulate
	 * the Set directly anymore! Use the methods addServerAdmin(...) and
	 * removeServerAdmin(...) instead!
	 * 
	 * @param serverAdmins The serverAdmins to set.
	 * @see addServerAdmin(String userID)
	 * @see removeServerAdmin(String userID)
	 */
	public void setServerAdmins(Set<String> _serverAdmins) 
	{
		assertWritable();
		this.serverAdmins = _serverAdmins;
		setChanged();
	}
	
	public void addServerAdmin(String userID)
	{
		assertWritable();
		if (serverAdmins == null)
			serverAdmins = new HashSet<String>();
		serverAdmins.add(userID);
		setChanged();
	}

	public boolean removeServerAdmin(String userID)
	{
		assertWritable();
		if (serverAdmins == null)
			return false;
		boolean res = serverAdmins.remove(userID);
		if (serverAdmins.size() == 0)
			serverAdmins = null;
		setChanged();
		return res;
	}
	
	public boolean isServerAdmin(String userID)
	{
		if (serverAdmins == null)
			return false;
		
		return serverAdmins.contains(userID);
	}

	public void makeReadOnly()
	{
		readOnly = true;
	}

	public void assertWritable()
	{
		if (readOnly)
			throw new IllegalStateException("This instance of OrganisationCf does not allow write!");
	}

	/**
	 * This method creates a JDO Organisation object with the given persistenceManager
	 * in case it does not yet exist.
	 *
	 * @param pm The PersistenceManager in which's datastore the Organisation should be
	 * 	created.
	 */
	public Organisation createOrganisation(PersistenceManager pm, Server server)
	{
		// Initialize meta data.
		pm.getExtent(Organisation.class, true);

		// Fetch/create Organisation instance.
		Organisation organisation;
		try {
			organisation = (Organisation)pm.getObjectById(OrganisationID.create(getOrganisationID()), true);
		} catch (JDOObjectNotFoundException x) {
			organisation = new Organisation(getOrganisationID()); // , getMasterOrganisationID());
			organisation.setServer(server);
			pm.makePersistent(organisation);
		}

		if (organisation.getPerson() == null) {
			Person person = new Person(organisationID, IDGenerator.nextID(PropertySet.class));
			PersonStruct.getPersonStruct(getOrganisationID(), pm);
			IStruct struct = StructLocal.getStructLocal(Person.class, StructLocal.DEFAULT_SCOPE, pm);
			struct.explodePropertySet(person);
			try {
				TextDataField f = (TextDataField) person.getDataField(PersonStruct.PERSONALDATA_COMPANY);
				f.setText(organisationName);
			} catch (Exception e) {
				throw new RuntimeException(e); // We only access predefined blocks/fields - an exception can definitely only happen if there's a fundamental programmer-caused problem.
			}
			person.setDisplayName(organisationName);
			person.setAutoGenerateDisplayName(true);
			struct.implodePropertySet(person);
			organisation.setPerson(person);
		}

		return organisation;
	}
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone()
	{
		OrganisationCf n;
		try {
			n = (OrganisationCf) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e); // should never happen since we implement clone()
		}

		if (this.serverAdmins != null) // deep copy
			n.serverAdmins = new HashSet<String>(this.serverAdmins);

//		OrganisationCf n = new OrganisationCf(
//				this.organisationID,
//				this.organisationName);
////				this.masterOrganisationID);
//				// this.persistenceManagerFactoryJNDIName);
//
//		if (this.serverAdmins != null)
//			n.serverAdmins = new HashSet<String>(this.serverAdmins);

		return n;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(OrganisationCf other) {
//		if (!(o instanceof OrganisationCf))
//			return -1;

//		OrganisationCf other = (OrganisationCf)o;
		if (this.organisationID == null) {
			if (other.organisationID == null)
				return 0;
			else
				return -1;
		}

		if (other.organisationID == null)
			return 1;

		return this.organisationID.compareTo(other.organisationID);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj == null)
			return false;

		return this.toString().equals(obj.toString());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	protected String thisString = null;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		if (thisString == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getClass().getName());
			sb.append('{');
			sb.append(organisationID);
			sb.append(',');
			sb.append(organisationName);
//			sb.append(',');
//			sb.append(masterOrganisationID);
//			sb.append(',');
//			sb.append(persistenceManagerFactoryJNDIName);
			sb.append(",serverAdmins{");
			if (serverAdmins != null) {
				for (Iterator it = serverAdmins.iterator(); it.hasNext(); ) {
					String userID = (String)it.next();
					sb.append(userID);
					if (it.hasNext())
						sb.append(',');
				}
			} // if (serverAdmins != null) {
			sb.append("}}");
			thisString = sb.toString();
		}

		return thisString;
	}
	

	/**
	 * Get the parentConfigModule.
	 * @return the parentConfigModule
	 */
	public ConfigModule getParentConfigModule()
	{
		return parentConfigModule;
	}
	

	/**
	 * Set the parentConfigModule.
	 * @param parentConfigModule the parentConfigModule to set
	 */
	public void setParentConfigModule(ConfigModule parentConfigModule)
	{
		this.parentConfigModule = parentConfigModule;
	}

	public void setChanged()
	{
		if(parentConfigModule != null)
			parentConfigModule.setChanged();
		thisString = null;
	}
}
