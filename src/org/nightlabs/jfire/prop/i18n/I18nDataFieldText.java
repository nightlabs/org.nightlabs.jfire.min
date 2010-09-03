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
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.prop.i18n.id.I18nDataFieldTextID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link I18nText} used to store internationalised text in {@link I18nTextDataField}s.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.i18n.id.I18nDataFieldTextID"
 *		detachable="true"
 *		table="JFireBase_Prop_I18nDataFieldText"
 *
 * @jdo.create-objectid-class field-order="organisationID, propertySetID,
 *                            structBlockOrganisationID, structBlockID,
 *                            dataBlockID, structFieldOrganisationID,
 *                            structFieldID"
 * 
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="I18nTextDataField.fieldText" fields="dataField, textValues"
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="dataField, textValues"
 */
@PersistenceCapable(
	objectIdClass=I18nDataFieldTextID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_I18nDataFieldText")
@FetchGroups({
	@FetchGroup(
		name="I18nTextDataField.fieldText",
		members={@Persistent(name="dataField"), @Persistent(name="textValues")}),
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySet.FETCH_GROUP_FULL_DATA,
		members={@Persistent(name="dataField"), @Persistent(name="textValues")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class I18nDataFieldText extends I18nText {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long propertySetID;

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
	 */
	@PrimaryKey
	private Integer dataBlockID;

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
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private I18nTextDataField dataField;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected I18nDataFieldText() {
	}

	/**
	 * Create a new {@link I18nDataFieldText} for the given {@link I18nTextDataField}.
	 * @param textDataField The {@link I18nTextDataField} the new {@link I18nDataFieldText} should be associated with.
	 */
	public I18nDataFieldText(I18nTextDataField textDataField) {
		this.organisationID = textDataField.getOrganisationID();
		this.structBlockOrganisationID = textDataField.getStructBlockOrganisationID();
		this.structBlockID = textDataField.getStructBlockID();
		this.propertySetID = textDataField.getPropertySetID();
		this.structFieldOrganisationID = textDataField.getStructFieldOrganisationID();
		this.structFieldID = textDataField.getStructFieldID();
		this.dataBlockID = textDataField.getDataBlockID();
		this.dataField = textDataField;
	}
	
	/**
	 * key: String languageID<br/>
	 * value: String text
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireBase_Prop_I18nDataFieldText_textValues"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_I18nDataFieldText_textValues",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> textValues = new HashMap<String, String>();

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return textValues;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return "";
	}

	/**
	 * @return the dataBlockID
	 */
	public Integer getDataBlockID() {
		return dataBlockID;
	}

	/**
	 * @return the dataField
	 */
	public I18nTextDataField getDataField() {
		return dataField;
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the propertySetID
	 */
	public long getPropertySetID() {
		return propertySetID;
	}

	/**
	 * @return the structBlockID
	 */
	public String getStructBlockID() {
		return structBlockID;
	}

	/**
	 * @return the structBlockOrganisationID
	 */
	public String getStructBlockOrganisationID() {
		return structBlockOrganisationID;
	}

	/**
	 * @return the structFieldID
	 */
	public String getStructFieldID() {
		return structFieldID;
	}

	/**
	 * @return the structFieldOrganisationID
	 */
	public String getStructFieldOrganisationID() {
		return structFieldOrganisationID;
	}
}
