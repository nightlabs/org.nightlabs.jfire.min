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

package org.nightlabs.jfire.prop;

import java.io.Serializable;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import org.nightlabs.jfire.prop.id.DisplayNamePartID;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link DisplayNamePart}s are used to build the display name of a {@link PropertySet}
 * when it should be managed automatically.
 * <p>
 * A {@link PropertySet} has a list of {@link DisplayNamePart}s and creates the display name
 * by simply iterating this list and adding the corresponding data fields actual value
 * (see {@link #getStructField()}) and the {@link DisplayNamePart}s {@link #getStructFieldSuffix()}.
 * </p>
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.id.DisplayNamePartID"
 *		detachable="true"
 *		table="JFireBase_Prop_DisplayNamePart"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, displayNamePartID"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default" fields="structField"
 *
 */
@PersistenceCapable(
	objectIdClass=DisplayNamePartID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_DisplayNamePart")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members=@Persistent(name="structField"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DisplayNamePart implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * This constructor is for JDO only
	 */
	protected DisplayNamePart() {
	}

	/**
	 * Create a new {@link DisplayNamePart} with the given primary key.
	 * The {@link DisplayNamePart} will add the value of the
	 * {@link DataField} referencing the given {@link StructField}
	 * and the given suffix.
	 *
	 * @param organisationID The organisation ID of the new {@link DisplayNamePart}.
	 * @param displayNamePartID The displayNamePartID of the new {@link DisplayNamePart}.
	 * @param field The {@link StructField} whose data should be included in the display name.
	 * @param suffix The suffix that should be appended to the data of the field, might be <code>null</code>.
	 */
	public DisplayNamePart(
			String organisationID, String displayNamePartID,
			StructField<? extends DataField> field, String suffix
		)
	{
		this.organisationID = organisationID;
		this.displayNamePartID = displayNamePartID;
		this.structField = field;
		this.structFieldSuffix = suffix;
	}

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String displayNamePartID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructField<? extends DataField> structField;

	/**
	 * Returns the {@link StructField} of wich this
	 * {@link DisplayNamePart} will take the data field value
	 * of to create a part of the display name.
	 */
	public StructField<? extends DataField> getStructField() {
		return this.structField;
	}

	public void setStructField(StructField<? extends DataField> structField) {
		this.structField = structField;
	}

	private String structFieldSuffix;

	/**
	 * Returns the suffix that should be added to the
	 * value of the data field corresponding to {@link #getStructField()}
	 * in the {@link PropertySet} this is part of.
	 * @return The suffix to be added to the value of the data field.
	 */
	public String getStructFieldSuffix() {
		return structFieldSuffix;
	}

	/**
	 * Set the suffix for this {@link DisplayNamePart}.
	 * Might be <code>null</code>.
	 *
	 * @param structFieldSuffix The suffix to set.
	 */
	public void setStructFieldSuffix(String structFieldSuffix) {
		this.structFieldSuffix = structFieldSuffix;
	}

	/**
	 * @return The dipslayNamePartID of this {@link DisplayNamePart}.
	 */
	public String getDisplayNamePartID() {
		return displayNamePartID;
	}

	/**
	 * @return The organisationID of this {@link DisplayNamePart}.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
}