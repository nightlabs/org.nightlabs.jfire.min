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

package org.nightlabs.jfire.prop.i18n;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.prop.id.StructFieldValueNameID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link I18nText} used as name for {@link StructFieldValue}s.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.id.StructFieldValueNameID"
 *		detachable="true"
 *		table="JFireBase_Prop_StructFieldValueName"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class field-order="structBlockOrganisationID, structBlockID, structFieldID, structFieldOrganisationID, structFieldValueID"
 * 
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="StructFieldValue.valueName" fetch-groups="default" fields="names"
 */
@PersistenceCapable(
	objectIdClass=StructFieldValueNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructFieldValueName")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members=@Persistent(name="names")),
	@FetchGroup(
		fetchGroups={"default"},
		name="StructFieldValue.valueName",
		members=@Persistent(name="names"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructFieldValueName extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
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
	 * For JDO only.
	 */
	protected StructFieldValueName() {
	}

	/**
	 * Create a new {@link StructFieldValueName} for the given {@link StructFieldValue}.
	 * @param structFieldValue The {@link StructFieldValue} the new {@link StructFieldValueName} will be associated with.
	 */
	public StructFieldValueName(StructFieldValue structFieldValue) {
		this.structFieldValue = structFieldValue;
		initPKs();
	}

	/**
	 * Set the primary-key fields from the associated {@link StructFieldValue}.
	 */
	private void initPKs()
	{
		if (structFieldValue == null)
			throw new NullPointerException("structField is null!");
		
		this.structBlockOrganisationID = structFieldValue.getStructBlockOrganisationID();
		this.structBlockID = structFieldValue.getStructBlockID();
		this.structFieldOrganisationID = structFieldValue.getStructFieldOrganisationID();
		this.structFieldID = structFieldValue.getStructFieldID();
		this.structFieldValueID = structFieldValue.getStructFieldValueID();
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructFieldValue structFieldValue;
	
	/**
	 * key: String languageID<br/>
	 * value: String structFieldName
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireBase_Prop_StructFieldValueName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_StructFieldValueName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return structFieldValue.getStructFieldValueID();
	}
	
	/**
	 * @return The {@link StructFieldValue} this {@link StructFieldValueName} is associated to.
	 */
	public StructFieldValue getStructFieldValue() {
		return structFieldValue;
	}

	/**
	 * Get the structBlockID.
	 * @return the structBlockID
	 */
	public String getStructBlockID() {
		return structBlockID;
	}

	/**
	 * Get the structBlockOrganisationID.
	 * @return the structBlockOrganisationID
	 */
	public String getStructBlockOrganisationID() {
		return structBlockOrganisationID;
	}

	/**
	 * Get the structFieldID.
	 * @return the structFieldID
	 */
	public String getStructFieldID() {
		return structFieldID;
	}

	/**
	 * Get the structFieldOrganisationID.
	 * @return the structFieldOrganisationID
	 */
	public String getStructFieldOrganisationID() {
		return structFieldOrganisationID;
	}

	/**
	 * Get the structFieldValueID.
	 * @return the structFieldValueID
	 */
	public String getStructFieldValueID() {
		return structFieldValueID;
	}

}
