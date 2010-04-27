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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.id.DataFieldID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.IDataFieldValidator;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * Base class for all types of data fields that can be stored in a {@link PropertySet}.
 * It contains the primary key of a data field that is composed of the
 * reference to the structure block and field the data field corresponds and
 * the reference to the PropertySet it is stored in.
 * <p>
 * Custom data field types should be build by extending this class and adding the
 * custom data. The jdo inheritance strategy should be "new-table" for custom fields
 * and the fetch-group {@value PropertySet#FETCH_GROUP_FULL_DATA} should be re-declared
 * and the custom fields should be added there.
 * </p>
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable identity-type="application"
 *                          objectid-class="org.nightlabs.jfire.prop.id.DataFieldID"
 *                          detachable="true"
 *                          table="JFireBase_Prop_DataField"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, propertySetID,
 *                            structBlockOrganisationID, structBlockID,
 *                            dataBlockID, structFieldOrganisationID,
 *                            structFieldID"
 *                            include-imports="id/DataFieldID.imports.inc"
 *                            include-body="id/DataFieldID.body.inc"
 *
 * @jdo.query name="getDataFieldInstanceCountByStructFieldType" query="SELECT
 *            UNIQUE count(this.structBlockOrganisationID) WHERE
 *            this.structBlockOrganisationID == paramStructBlockOrganisationID &&
 *            this.structBlockID == paramStructBlockID &&
 *            this.structFieldOrganisationID == paramStructFieldOrganisationID &&
 *            this.structFieldID == paramStructFieldID PARAMETERS String
 *            paramStructBlockOrganisationID, String paramStructBlockID, String
 *            paramStructFieldOrganisationID, String paramStructFieldID"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default"
 */
@PersistenceCapable(
	objectIdClass=DataFieldID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_DataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members={})
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries(
	@javax.jdo.annotations.Query(
		name="getDataFieldInstanceCountByStructFieldType",
		value="SELECT UNIQUE count(this.structBlockOrganisationID) WHERE this.structBlockOrganisationID == paramStructBlockOrganisationID && this.structBlockID == paramStructBlockID && this.structFieldOrganisationID == paramStructFieldOrganisationID && this.structFieldID == paramStructFieldID PARAMETERS String paramStructBlockOrganisationID, String paramStructBlockID, String paramStructFieldOrganisationID, String paramStructFieldID")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class DataField implements Serializable, Comparable<DataField>, IDataField
{
	private static final long serialVersionUID = 1L;

	protected DataField() {
	}

	/**
	 * Create a new {@link DataField} with its primary key fields
	 * set according to the given {@link DataBlock} and {@link StructField}-
	 *
	 * @param _dataBlock The {@link DataBlock} the new {@link DataField} should virtually be in.
	 * @param _structField The {@link StructField} the new {@link DataField} should represent.
	 */
	public DataField(DataBlock _dataBlock, StructField<? extends DataField> _structField) {
		// this.propBlock = _propBlock;
		this.structFieldOrganisationID = _structField.getStructFieldOrganisationID();
		this.structFieldID = _structField.getStructFieldID();
		// initPK();
		this.organisationID = _dataBlock.getOrganisationID();
		this.propertySetID = _dataBlock.getPropertySetID();
		this.structBlockOrganisationID = _dataBlock.getStructBlockOrganisationID();
		this.structBlockID = _dataBlock.getStructBlockID();
		this.dataBlockID = _dataBlock.getDataBlockID();
//		this.structField = _structField;
	}

	/**
	 * Create a new {@link DataField} that will be inside the {@link PropertySet} referenced
	 * by the given organisationID and propertySetID and be placed in a DataBlock according to the given DataBlock ID.
	 * @param organisationID The organisation ID referencing the {@link PropertySet} the new {@link DataField} should be in.
	 * @param propertySetID The propertySetID referencing the {@link PropertySet} the new {@link DataField} should be in.
	 * @param dataBlockID The ID of the DataBlock the new DataField (the clone) shall be placed in or -1 in the case the clone
	 * shall be placed in a DataBlock with the same ID like the DataField to be cloned (the cloneable).
	 * @param cloneField The {@link DataField} inside another PropertySet whose position in the referenced {@link PropertySet}
	 * the new {@link DataField} should have.
	 */
	public DataField(String organisationID, long propertySetID, int dataBlockID, DataField cloneField)
	{
		this.structFieldOrganisationID = cloneField.getStructFieldOrganisationID();
		this.structFieldID = cloneField.getStructFieldID();
		this.organisationID = organisationID;
		this.propertySetID = propertySetID;
		this.structBlockOrganisationID = cloneField.getStructBlockOrganisationID();
		this.structBlockID = cloneField.getStructBlockID();
		this.dataBlockID = dataBlockID == -1 ? cloneField.getDataBlockID() : dataBlockID;
	}

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
	 * This field is set when inflating and set to <code>null</code> when deflating. Thus it is never transmitted to the server.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private StructField<? extends DataField> structField;

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
	private int dataBlockID;

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

	private String managedBy;

	/**
	 * This field is meant to indicate the order index of the {@link DataBlock} that contains this {@link DataField}. All {@link DataField}s of the
	 * same {@link DataBlock} should hold the same index value. Additionally, the indices of the {@link DataBlock}s should be greater than 0 and
	 * subsequent.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int dataBlockIndex;

//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	private transient StructField structField;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IDataField#isEmpty()
	 */
	public abstract boolean isEmpty();

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the structBlockID.
	 */
	public String getStructBlockID() {
		return structBlockID;
	}

	/**
	 * @return Returns the structBlockOrganisationID.
	 */
	public String getStructBlockOrganisationID() {
		return structBlockOrganisationID;
	}

	/**
	 * @return Returns the structFieldID.
	 */
	public String getStructFieldID() {
		return structFieldID;
	}

	/**
	 * Returns the {@link StructFieldID} (id-object) of the associated {@link StructField}.
	 * @return The {@link StructFieldID} (id-object) of the associated {@link StructField}.
	 */
	public StructFieldID getStructFieldIDObj() {
		return StructFieldID.create(structBlockOrganisationID, structBlockID, structFieldOrganisationID, structFieldID);
	}

	/**
	 * @return Returns the structFieldOrganisationID.
	 */
	public String getStructFieldOrganisationID() {
		return structFieldOrganisationID;
	}

	/**
	 * @return Returns the propertySetID.
	 */
	public long getPropertySetID() {
		return this.propertySetID;
	}

	/**
	 * @return Returns the dataBlockID.
	 */
	public int getDataBlockID() {
		return dataBlockID;
	}

//	public StructField getStructField() {
//		return structField;
//	}

	/**
	 * Package visible modifier for the propertySetID.
	 *
	 * @param _propID
	 */
	void setPropertySetID(long _propID) {
		this.propertySetID = _propID;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private String structBlockKey = null;

	/**
	 * @return Returns the structBlockKey.
	 */
	public String getStructBlockKey() {
		if (structBlockKey == null)
			structBlockKey = structBlockOrganisationID + "/" + structBlockID;
		return structBlockKey;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private String propRelativePK = null;

	/**
	 * @return Returns the propRelativePK.
	 */
	public String getPropRelativePK() {
		if (propRelativePK == null)
			propRelativePK = structBlockOrganisationID + "/" + structBlockID + "/" + Integer.toString(dataBlockID) + "/"
					+ structFieldOrganisationID + "/" + structFieldID;

		return propRelativePK;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private String structFieldPK = null;

	/**
	 * @return Returns the structFieldPK.
	 */
	public String getStructFieldPK() {
		if (structFieldPK == null)
			structFieldPK = StructField.getPrimaryKey(structBlockOrganisationID, structBlockID,
					structFieldOrganisationID, structFieldID);
		return structFieldPK;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataField) {
			DataField other = (DataField) obj;
			return (this.organisationID.equals(other.organisationID)) && (this.propertySetID == other.propertySetID)
				&& (this.structBlockOrganisationID.equals(other.structBlockOrganisationID))
				&& (this.structBlockID.equals(other.structBlockID)) && (this.dataBlockID == other.dataBlockID)
				&& (this.structFieldOrganisationID.equals(other.structFieldOrganisationID))
				&& (this.structFieldID.equals(other.structFieldID));
		} else
			return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.organisationID+"/("+
		this.structBlockOrganisationID+"."+
		this.structBlockID+")/("+
		this.structFieldOrganisationID+"."+
		this.structFieldID + ")";
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private int priority = Integer.MAX_VALUE;

	/**
	 * @param priority
	 *          The priority to set.
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DataField o) {
		if (o.getStructBlockKey().equals(this.getStructBlockKey())) {
			// within same structBlock
			if (o.priority < this.priority)
				return 1;
			else if (o.priority > this.priority)
				return -1;
			else {
				if (this.equals(o)) {
					// to be consistent with equals
					return 0;
				} else
					return 1;
			}
		} else
			return 1;
	}

	/**
	 * Returns the count of {@link DataField}s referencing the given {@link StructField} that were already persisted.
	 *
	 * @param pm The {@link PersistenceManager} to use. (Connection to a certain datastore).
	 * @param structFieldID The {@link StructFieldID} persisted {@link DataField}s should be searched for.
	 * @return The count of {@link DataField}s referencing the given {@link StructField}
	 */
	public static long getDataFieldInstanceCountByStructFieldType(PersistenceManager pm, StructFieldID structFieldID) {
		Query q = pm.newNamedQuery(DataField.class, "getDataFieldInstanceCountByStructFieldType");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramStructBlockOrganisationID", structFieldID.structBlockOrganisationID);
		params.put("paramStructBlockID", structFieldID.structBlockID);
		params.put("paramStructFieldOrganisationID", structFieldID.structFieldOrganisationID);
		params.put("paramStructFieldID", structFieldID.structFieldID);
		return (Long) q.executeWithMap(params);
	}

	/**
	 * Subclasses should create a new {@link DataField} of their type here and <b>copy</b> their data to the new instance.<p>
	 * The clone should be connected to the given {@link PropertySet}, the constructor {@link #DataField(String, long, int, DataField)}
	 * is intended to help constructing the new instance.
	 * @param propertySet The {@link PropertySet} the new {@link DataField} instance should be linked to.
	 * @param dataBlockID The ID of the DataBlock the new DataField (the clone) shall be placed in or -1 in the case the clone shall be placed in a
	 * DataBlock with the same ID like the DataField to be cloned (the cloneable).
	 * @return A new {@link DataField} instance of this {@link DataField} with a copy of its data and linked to the given {@link PropertySet}.
	 */
	public abstract DataField cloneDataField(PropertySet propertySet, int dataBlockID);

	/**
	 * Returns the order index of the {@link DataBlock} that contains this {@link DataField}.
	 * @return The order index of the {@link DataBlock} that contains this {@link DataField}.
	 */
	int getDataBlockIndex() {
		return dataBlockIndex;
	}

	/**
	 * Sets the order index of the {@link DataBlock} that contains this {@link DataField}. All {@link DataField}s of a {@link DataBlock} should
	 * have the same value for this field. If that is not the case, the values are corrected upon exploding.
	 * @param dataBlockIndex The order index of the {@link DataBlock} to be set.
	 */
	void setDataBlockIndex(int dataBlockIndex) {
		this.dataBlockIndex = dataBlockIndex;
	}

	/**
	 * Validates this instance against the given {@link IStruct} and returns a list of all {@link ValidationFailureResult}
	 * occurred during the validation or <code>null</code> if the validation succeeded.
	 * @param struct The {@link IStruct} against which to validated.
	 * @return A list of all {@link ValidationFailureResult} occurred during the validation or <code>null</code> if the validation succeeded..
	 */
	public List<ValidationResult> validate(IStruct struct) {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		try {
			StructField<DataField> structField = (StructField<DataField>) struct.getStructField(this);
			for (IDataFieldValidator<DataField, StructField<DataField>> validator : structField.getDataFieldValidators()) {
				ValidationResult result = validator.validate(this, structField);
				if (result != null)
					results.add(result);
			}
		} catch (PropertyException e) {
			throw new IllegalArgumentException("StructField for this datafield was not found.");
		}

		if (results.isEmpty())
			return null;
		else
			return results;
	}

	/**
	 * Returns the {@link StructField} of this instance. This method returns null if it is called on a {@link DataField}
	 * that is part of a deflated structure.
	 *
	 * @return the {@link StructField} of this instance.
	 */
	public StructField<? extends DataField> getStructField() {
		return structField;
	}

	/**
	 * Sets the {@link StructField} of this instance. This should be done while inflating, on deflation it should be set to null again.
	 *
	 * @param structField The {@link StructField} to be set.
	 */
	public void setStructField(StructField<? extends DataField> structField) {
		this.structField = structField;
	}

	/**
	 * Get the managed-by property or <code>null</code>, if it is not managed by an import-interface.
	 * <p>
	 * If this field is "managed by" some special module (usually an import-interface, getting the data from another
	 * application), UI should not make it possible to modify this {@link DataField}. This prevents a user from
	 * entering some data and loosing it when the next import-run happens.
	 * </p>
	 * <p>
	 * A module wishing to use this feature should set the "managed-by" String to an identifier that is unique
	 * to that module. This allows for multiple modules to co-exist and manage different fields/PropertySets and
	 * - if implemented in such a way - prevent to overwrite each-other's data. The module might choose to use
	 * a format which is parseable and encodes for example the identifier of the corresponding object in the
	 * source-system.
	 * </p>
	 * <p>
	 * A good example for a managed-by-value would be "com.mycompany.MyJFireImportManager#sourceSystemObjectIdentifier", but
	 * as long as your managed-by-value is unlikely to conflict with another module, you're free to choose whatever
	 * you want.
	 * </p>
	 *
	 * @return the managed-by property or <code>null</code>.
	 */
	public String getManagedBy() {
		return managedBy;
	}
	/**
	 * Set the managed-by property or <code>null</code>, if it is not managed by an import-interface.
	 * @param managedBy the managed-by property or <code>null</code>.
	 * @see #getManagedBy()
	 */
	public void setManagedBy(String managedBy) {
		this.managedBy = managedBy;
	}
}