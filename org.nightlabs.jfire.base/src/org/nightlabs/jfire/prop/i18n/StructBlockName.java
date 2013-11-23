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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.StructBlock;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.prop.id.StructBlockNameID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link I18nText} used as name for {@link StructBlock}s.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.id.StructBlockNameID"
 *		detachable="true"
 *		table="JFireBase_Prop_StructBlockName"
 *
 * @jdo.create-objectid-class
 * 		field-order="structBlockOrganisationID, structBlockID"
 * 
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default" fields="names, block"
 * 
 */
@PersistenceCapable(
	objectIdClass=StructBlockNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructBlockName")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="names"), @Persistent(name="block")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructBlockName extends I18nText implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structBlockOrganisationID = null;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structBlockID = null;
	
	/**
	 * key: String languageID<br/>
	 * value: String structBlockName
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireBase_Prop_StructBlockName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_StructBlockName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();
	
	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructBlock block;
	
	/**
	 * For JDO only.
	 */
	protected StructBlockName() {
	}
	
	/**
	 * Create a new {@link StructBlockName} for the given {@link StructBlock}.
	 * @param block The {@link StructBlock} the new {@link StructBlockName} is associated to.
	 */
	public StructBlockName(StructBlock block) {
		this.block = block;
		this.structBlockOrganisationID = block.getStructBlockOrganisationID();
		this.structBlockID = block.getStructBlockID();
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
		return block.getStructBlockID();
	}
	
	/**
	 * @return The organisationID of the associated {@link StructBlock}.
	 */
	public String getStructBlockOrganisationID() {
		return structBlockOrganisationID;
	}
	
	/**
	 * @return The structBlockID of the associated {@link StructBlock}.
	 */
	public String getStructBlockID() {
		return structBlockID;
	}
}
