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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.IDataBlockValidator;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * DataBlocks are non-persistent objects that correspond to a {@link StructBlock}
 * and are used to work with/manipulate {@link PropertySet}s via the block-field-API.
 * <p>
 * DataBlocks are created when a {@link PropertySet} is inflated, see {@link PropertySet}{@link #inflate(StructBlock)}}.
 * </p>
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author marco
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class DataBlock implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(DataBlock.class);

	private int index = -1;

	/**
	 * This constructor is for JDO only
	 */
	protected DataBlock() {
	}

	/**
	 * Constructor allowing control of the initialisation of fields.
	 *
	 * @param _propBlockGroup
	 * @param _propBlockID
	 */
	public DataBlock(DataBlockGroup _propBlockGroup, Integer _propBlockID) {
		this.dataBlockGroup = _propBlockGroup;

		this.organisationID = _propBlockGroup.getOrganisationID();
		this.propertySetID = _propBlockGroup.getPropertySetID();
		this.structBlockOrganisationID = _propBlockGroup.getStructBlockOrganisationID();
		this.structBlockID = _propBlockGroup.getStructBlockID();

		this.dataBlockID = _propBlockID;
	}

	/**
	 * Checks if this block has a data representation of the given structField.
	 * @param structField
	 * @return The data representation of the given structField or null if there is no such representation.
	 */
	private DataField getStructFieldRepresentation(StructField<? extends DataField> structField) {
		return dataFields.get(structField.getStructFieldKey());
	}

	/**
	 * Inflates this block by creating/instantiating empty {@link DataField}s for each {@link StructField} of the given {@link StructBlock}.
	 * @param structBlock The structBlock according to which this {@link DataBlock} should be inflated.
	 */
	void inflate(StructBlock structBlock) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Inflating DataBlock " + this.dataBlockID + " (index: " + index + ")");
		if (this.dataBlockGroup == null)
			return;
		setStructBlock(structBlock);
		for (StructField<? extends DataField> structField : structBlock.getStructFields()) {
			DataField dataField = getStructFieldRepresentation(structField);
			if (dataField == null) {
				dataField = structField.addNewDataFieldInstance(this);
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Setting index of new DataField to " + index);
				dataField.setDataBlockIndex(index);
			}
			dataField.setStructField(structField);
		}
	}

	private DataBlockGroup dataBlockGroup;

	private String organisationID;

	private long propertySetID;

	private String structBlockOrganisationID;

	private String structBlockID;

	/**
	 * This field is set when inflating and set to <code>null</code> when deflating. Thus it is never transmitted to the server.
	 */
	private StructBlock structBlock;

	private Integer dataBlockID;

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the dataBlockGroup.
	 */
	public DataBlockGroup getDataBlockGroup() {
		return dataBlockGroup;
	}

	/**
	 * @return Returns the dataBlockID.
	 */
	public Integer getDataBlockID() {
		return dataBlockID;
	}

	/**
	 * @return Returns the propertySetID.
	 */
	public long getPropertySetID() {
		return propertySetID;
	}

	/**
	 * Package visible modifier for the propertySetID.
	 *
	 * @param _propID
	 */
	void setPropertySetID(long _propID) {
		this.propertySetID = _propID;
	}

	/**
	 * @return Returns the structBlockOrganisationID.
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
	 * key: String StructField.getStructFieldKey<br />
	 * value: DataField propField
	 */
	protected Map<String, DataField> dataFields = new HashMap<String, DataField>();

	/**
	 * @return Returns the dataFields.
	 */
	public Collection<DataField> getDataFields() {
		return dataFields.values();
	}

	/**
	 * Adds the {@link DataField} to the structure if it does not exist already or
	 * replaces the already existing one otherwise. Is used to insert persistent {@link DataField}s
	 * into the inflated PropertySet.
	 *
	 * @param field The <code>field</code> to set.
	 */
	void addDataFieldToStructure(DataField field) {
		boolean found = false;
		String key = null;
		for (Map.Entry<String, DataField> entry : dataFields.entrySet()) {
			DataField containedField = entry.getValue();
			if (containedField.equals(field)) {
				key = entry.getKey();
				found = true;
				break;
			}
		}
		if (found) // found same field -> replace it
		{
			dataFields.put(key, field);
		} else // not found -> add it
		{
			dataFields.put(StructField.getStructFieldKey(field.getStructFieldOrganisationID(), field.getStructFieldID()), field);
		}
	}

	/**
	 * Adds the passed implementor of {@link DataField} to the list of fields.<br/>
	 * If a field corresponding to the structFieldKey of the passed dataField is already contained in this block a
	 * {@link DuplicateKeyException} is thrown.
	 *
	 * @param dataField The data field to add.
	 * @throws DuplicateKeyException When a corresponding data field is already contained in the block.
	 */
	public void addDataField(DataField dataField) throws DuplicateKeyException {
		if (dataField == null)
			throw new IllegalArgumentException("Parameter dataField must not be null!");
		String structFieldKey = StructField.getStructFieldKey(dataField.getStructFieldOrganisationID(), dataField.getStructFieldID());
		if (dataFields.containsKey(structFieldKey))
			throw new DuplicateKeyException("DataBlock " + this + " already contains a field with structFieldKey = " + structFieldKey);

		dataField.setDataBlockIndex(index);
		try {
			if (structBlock != null) {
				dataField.setStructField(
						structBlock.getStructField(dataField.getStructFieldOrganisationID(), dataField.getStructFieldID())
				);
			}
		} catch (final StructFieldNotFoundException e) {
			throw new IllegalStateException(e); // should never happen => rethrow
		}
		
		dataFields.put(structFieldKey, dataField);
		// add it to the persistent list in prop as well
		dataBlockGroup.getProperty().internalAddDataFieldToPersistentCollection(dataField);
	}

	/**
	 * Returns the {@link DataField} corresponding to the given {@link StructFieldID}.
	 * If the data field can not be found a {@link DataFieldNotFoundException}
	 * will be thrown.
	 *
	 * @param structFieldID The id of the {@link StructField} the data field should be searched for.
	 * @return The {@link DataField} corresponding to the given data field.
	 * @throws DataFieldNotFoundException If the data field can not be found.
	 */
	public DataField getDataField(StructFieldID structFieldID) throws DataFieldNotFoundException {
		return getDataField(structFieldID.structFieldOrganisationID, structFieldID.structFieldID);
	}

	/**
	 * Returns the {@link DataField} corresponding to the given primary key fields
	 * of a {@link StructField} within this block.
	 *
	 * If the data field can not be found a {@link DataFieldNotFoundException}
	 * will be thrown.
	 *
	 * @param structFieldOrganisationID The organisation ID of the {@link StructField} to search the data field for.
	 * @param structFieldID The ID of the {@link StructField} to search the data field for.
	 * @return The {@link DataField} corresponding to the given primary key fields
	 * 	       of a {@link StructField} within this block.
	 * @throws DataFieldNotFoundException
	 */
	public DataField getDataField(String structFieldOrganisationID, String structFieldID) throws DataFieldNotFoundException {
		String structFieldKey = StructField.getStructFieldKey(structFieldOrganisationID, structFieldID);
		if (LOGGER.isDebugEnabled()) {
			for (Map.Entry<String, DataField> entry : dataFields.entrySet()) {
				LOGGER.debug("getDataField, key:   " + entry.getKey());
				LOGGER.debug("getDataField, value: " + entry.getValue());
			}
		}
		DataField field = dataFields.get(structFieldKey);
		if (field == null)
			throw new DataFieldNotFoundException("No field " + structFieldKey + " found in this DataBlock for StructBlock "
					+ StructBlock.getPrimaryKey(structBlockOrganisationID, structBlockID));
		return field;
	}

	/**
	 * Scans all DataFields and removes all entries where isEmpty() returns true and returns <code>true</code> if no {@link DataField}s
	 * are left in this block thereafter or <code>false</code> otherwise.
	 * @returns <code>true</code> if no {@link DataField}s are left in this block after deflating or <code>false</code> otherwise.
	 */
	boolean deflate() {
		setStructBlock(null);

		for (Iterator<Map.Entry<String, DataField>> it = dataFields.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, DataField> entry = it.next();
			DataField field = entry.getValue();
			if (field.isEmpty()) {
				dataBlockGroup.getProperty().internalRemoveDataFieldFromPersistentCollection(field);
				it.remove();
			}
			field.setStructField(null);
		}

		return dataFields.isEmpty();
	}

	/**
	 * Set the index of this {@link DataBlock}.
	 * Note, that affects all {@link DataField}s contained in this block,
	 * their {@link DataField#setDataBlockIndex(int)} will also be invoked
	 * with the given index.
	 * @param index The index to set.
	 * @param setDataBlockIndexForDataFields True in the case the DataBlock index shall be set for each DataField of this DataBlock, otherwise false.
	 */
	void setIndex(final int index, final boolean setDataBlockIndexForDataFields) {
		if (setDataBlockIndexForDataFields) {
			for (DataField df : dataFields.values())
				df.setDataBlockIndex(index);
		}
		this.index = index;
	}

	/**
	 * Get the index of this {@link DataBlock}.
	 * @return The index of this {@link DataBlock}.
	 */
	int getIndex() {
		return index;
	}

	/**
	 * @see javax.jdo.InstanceCallbacks#jdoPostLoad()
	 */
	public void jdoPostLoad() {
		System.out.println(this.getClass().getName() + "|" + this + ": jdoPostLoad()");
		// initPK();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + getIndex();
	}

	/**
	 * Validates this instance against the given {@link IStruct} and returns a list of all {@link ValidationFailureResult}
	 * occurred during the validation or <code>null</code> if the validation succeeded.
	 * @param struct The {@link IStruct} against which to validated.
	 * @return A list of all {@link ValidationFailureResult} occurred during the validation or <code>null</code> if the validation succeeded..
	 */
	public List<ValidationResult> validate(IStruct struct) {
		List<ValidationResult> results = new LinkedList<ValidationResult>();

		StructBlock structBlock = struct.getStructBlock(getDataBlockGroup());
		for (IDataBlockValidator dataBlockValidator : structBlock.getDataBlockValidators()) {
			ValidationResult result = dataBlockValidator.validate(this, structBlock);
			if (result != null)
				results.add(result);
		}

		for (DataField field : getDataFields()) {
			List<ValidationResult> fieldValidationResults = field.validate(struct);
			if (fieldValidationResults != null)
				results.addAll(fieldValidationResults);
		}

		if (results.isEmpty())
			return null;
		else
			return results;
	}

	/**
	 * Sets the {@link StructBlock} of this instance. This should be set while inflating and set to <code>null</code> again when deflating.
	 * @param structBlock the {@link StructBlock} to be set.
	 */
	public void setStructBlock(StructBlock structBlock) {
		this.structBlock = structBlock;
	}

	/**
	 * Returns the {@link StructBlock} of this instance when it is inflated and null otherwise.
	 * @return the {@link StructBlock} of this instance or null.
	 */
	public StructBlock getStructBlock() {
		return structBlock;
	}

	/**
	 * Checks whether this {@link DataBlock} contains only empty {@link DataField}s.
	 * 
	 * @return <code>true</code> if this DataBlock contains only empty {@link DataField}s. 
	 */
	public boolean isEmpty() {
		for (DataField dataField : getDataFields()) {
			if (!dataField.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
