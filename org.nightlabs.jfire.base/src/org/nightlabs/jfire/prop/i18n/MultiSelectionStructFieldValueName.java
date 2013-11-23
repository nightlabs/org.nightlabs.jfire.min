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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.id.MultiSelectionStructFieldValueNameID;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;

/**
 * {@link I18nText} used as name for {@link MultiSelectionStructFieldValue}s.
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de> (Original StructFieldValueName code)
 */
@PersistenceCapable(
	objectIdClass=MultiSelectionStructFieldValueNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_MultiSelectionStructFieldValueName")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="names"), @Persistent(name="structFieldValue")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="MultiSelectionStructFieldValue.valueName",
		members=@Persistent(name="names"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class MultiSelectionStructFieldValueName extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
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
	private MultiSelectionStructFieldValue structFieldValue;
	
	/**
	 * key: String languageID<br/>
	 * value: String structFieldName
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_MultiSelectionStructFieldValueName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * For JDO only.
	 */
	protected MultiSelectionStructFieldValueName() 
	{
	}
	
	/**
	 * Create a new {@link MultiSelectionStructFieldValueName} for the given {@link MultiSelectionStructFieldValue}.
	 * @param structFieldValue The {@link MultiSelectionStructFieldValue} the new {@link MultiSelectionStructFieldValueName} will be associated with.
	 */
	public MultiSelectionStructFieldValueName(MultiSelectionStructFieldValue structFieldValue) 
	{
		this.structFieldValue = structFieldValue;
		initPKs();
	}
	
	/**
	 * Set the primary-key fields from the associated {@link MultiSelectionStructFieldValue}.
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
	 * @return The {@link MultiSelectionStructFieldValue} this {@link MultiSelectionStructFieldValueName} is associated to.
	 */
	public MultiSelectionStructFieldValue getStructFieldValue() 
	{
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
