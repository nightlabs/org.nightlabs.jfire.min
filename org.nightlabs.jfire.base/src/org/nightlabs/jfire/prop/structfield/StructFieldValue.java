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

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.i18n.StructFieldValueName;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import org.nightlabs.jfire.prop.id.StructFieldValueID;


/**
 * {@link StructFieldValue} are items in the list of possible values a {@link SelectionStructField} defines.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author marco
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * 
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.id.StructFieldValueID"
 *		detachable="true"
 *		table="JFireBase_Prop_StructFieldValue"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 * 		field-order="structBlockOrganisationID, structBlockID, structFieldOrganisationID, structFieldID, structFieldValueID"
 * 
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default" fields="structField, valueName"
 * 
 * @jdo.fetch-group name="StructFieldValue.valueName" fetch-groups="default" fields="valueName"
 */
@PersistenceCapable(
	objectIdClass=StructFieldValueID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructFieldValue")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="structField"), @Persistent(name="valueName")}),
	@FetchGroup(
		fetchGroups={"default"},
		name=StructFieldValue.FETCH_GROUP_VALUE_NAME,
		members=@Persistent(name="valueName"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructFieldValue implements Serializable //, InstanceCallbacks
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	
	public static final String FETCH_GROUP_VALUE_NAME = "StructFieldValue.valueName";
	
	protected StructFieldValue()	{ }

	public StructFieldValue(SelectionStructField _structField)
	{
		this(_structField, null);
	}
	public StructFieldValue(SelectionStructField _structField, String _structFieldValueID)
	{
		this.structField = _structField;

		initPKs();
		// TODO: Temporary removed this. We need to think about this a little more, the PersonStruct should be initialized only for the root organisation and new organisations should be synchronized.
//		if (!Util.equals(this.structFieldOrganisationID, IDGenerator.getOrganisationID()))
//			throw new IllegalStateException("The SelectionStructField belongs to another organisation! Cannot create a StructFieldValue for it! SelectionStructField.organisationID=" + this.structFieldOrganisationID + " IDGenerator.getOrganisationID()=" + IDGenerator.getOrganisationID());

		if (_structFieldValueID == null)
			_structFieldValueID = ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(StructFieldValue.class, _structField.getPrimaryKey()));

		ObjectIDUtil.assertValidIDString(_structFieldValueID);

		this.structFieldValueID = _structFieldValueID;
		this.valueName = new StructFieldValueName(this);
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private SelectionStructField structField;
	
	/**
	 * @return Returns the structField.
	 */
	public SelectionStructField getStructField() {
		return structField;
	}


	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structBlockOrganisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structBlockID;
	

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structFieldOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structFieldID;

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String structFieldValueID;

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
	
	// ************ PRIMARY KEY END ************
	
	
	/**
	// ********** END inner class I18nText ************

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		mapped-by="structFieldValue"
	 *		dependent="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="structFieldValue",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected StructFieldValueName valueName;
	
	/**
	 * Returns the I18n name of the value
	 * @return
	 */
	public StructFieldValueName getValueName() {
		return valueName;
	}
}
