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

package org.nightlabs.jfire.prop.structfield;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.i18n.MultiSelectionStructFieldValueName;
import org.nightlabs.jfire.prop.id.MultiSelectionStructFieldValueID;


/**
 * {@link MultiSelectionStructFieldValue} are items in the list of possible values a {@link MultiSelectionStructField} defines.
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de> (Original StructFieldValue code)
 * @author marco (Original StructFieldValue code)
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de --> (Original StructFieldValue code)
 */
@PersistenceCapable(
	objectIdClass=MultiSelectionStructFieldValueID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_MultiSelectionStructFieldValue")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="structField"), @Persistent(name="valueName")}),
	@FetchGroup(
		fetchGroups={"default"},
		name=MultiSelectionStructFieldValue.FETCH_GROUP_VALUE_NAME,
		members=@Persistent(name="valueName"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class MultiSelectionStructFieldValue implements Serializable //, InstanceCallbacks
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_VALUE_NAME = "StructFieldValue.valueName";
	
	@Persistent
	@PrimaryKey
	@Column(length=100)
	private String structBlockOrganisationID;
	
	@Persistent
	@PrimaryKey
	@Column(length=100)
	private String structBlockID;
	
	@Persistent
	@PrimaryKey
	@Column(length=100)
	private String structFieldOrganisationID;

	@Persistent
	@PrimaryKey
	@Column(length=100)
	private String structFieldID;

	@Persistent
	@PrimaryKey
	@Column(length=100)
	private String structFieldValueID;

	@Persistent
	private MultiSelectionStructField structField;
	
	@Persistent(
			dependent="true",
			mappedBy="structFieldValue") // XXX where should this point to?
	protected MultiSelectionStructFieldValueName valueName;
	
	/**
	 * For JDO only.
	 */
	protected MultiSelectionStructFieldValue()
	{
	}

	public MultiSelectionStructFieldValue(MultiSelectionStructField _structField)
	{
		this(_structField, null);
	}
	
	public MultiSelectionStructFieldValue(MultiSelectionStructField _structField, String _structFieldValueID)
	{
		this.structField = _structField;

		initPKs();
		// TODO: Temporary removed this. We need to think about this a little more, the PersonStruct should be initialized only for the root organisation and new organisations should be synchronized.
//		if (!Util.equals(this.structFieldOrganisationID, IDGenerator.getOrganisationID()))
//			throw new IllegalStateException("The SelectionStructField belongs to another organisation! Cannot create a StructFieldValue for it! SelectionStructField.organisationID=" + this.structFieldOrganisationID + " IDGenerator.getOrganisationID()=" + IDGenerator.getOrganisationID());

		if (_structFieldValueID == null)
			_structFieldValueID = ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(MultiSelectionStructFieldValue.class, _structField.getPrimaryKey()));

		ObjectIDUtil.assertValidIDString(_structFieldValueID);

		this.structFieldValueID = _structFieldValueID;
		this.valueName = new MultiSelectionStructFieldValueName(this);
	}
	
	private void initPKs()
	{
		if (getStructField() == null)
			throw new NullPointerException("structField is null!");

		this.structBlockOrganisationID = structField.getStructBlockOrganisationID();
		this.structBlockID = structField.getStructBlockID();
		this.structFieldOrganisationID = structField.getStructFieldOrganisationID();
		this.structFieldID = structField.getStructFieldID();
	}
	
	/**
	 * @return Returns the structField.
	 */
	public MultiSelectionStructField getStructField() 
	{
		return structField;
	}

	/**
	 * @return Returns the structBlockID.
	 */
	public String getStructBlockOrganisationID() {
		return structBlockOrganisationID;
	}

	/**
	 * @return Returns the structBlockID.
	 */
	public String getStructBlockID() {
		return structBlockID;
	}

	/**
	 * @return Returns the structFieldID.
	 */
	public String getStructFieldOrganisationID() {
		return structFieldOrganisationID;
	}
	
	/**
	 * @return Returns the structFieldID.
	 */
	public String getStructFieldID() {
		return structFieldID;
	}

	/**
	 * @return Returns the structFieldValueID.
	 */
	public String getStructFieldValueID() {
		return structFieldValueID;
	}
	
	/**
	 * Returns the I18n name of the value
	 * @return
	 */
	public MultiSelectionStructFieldValueName getValueName() {
		return valueName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() 
	{
		return MultiSelectionStructFieldValueID.create(structBlockOrganisationID, structBlockID, structFieldOrganisationID, structFieldID, structFieldValueID).hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiSelectionStructFieldValue other = (MultiSelectionStructFieldValue) obj;
		MultiSelectionStructFieldValueID thisid = MultiSelectionStructFieldValueID.create(structBlockOrganisationID, structBlockID, structFieldOrganisationID, structFieldID, structFieldValueID);
		MultiSelectionStructFieldValueID otherid = MultiSelectionStructFieldValueID.create(other.structBlockOrganisationID, other.structBlockID, other.structFieldOrganisationID, other.structFieldID, other.structFieldValueID);
		return thisid.equals(otherid);
	}
}
