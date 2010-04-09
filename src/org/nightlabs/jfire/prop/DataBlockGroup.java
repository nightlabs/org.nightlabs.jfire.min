/* *****************************************************************************
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockRemovalException;
import org.nightlabs.jfire.prop.exception.DataBlockUniqueException;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.validation.IDataBlockValidator;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * DataBlockGroups are non-persistent objects that are used when
 * working with/manipulating {@link PropertySet}s via the block-field-API.
 * <p>
 * One instance of this class exists per structBlockID and groups all
 * instances of DataBlock with the same structBlockID, so a {@link PropertySet}
 * can have multiple instances of a {@link DataBlock} for the same {@link StructBlock}.
 * </p>
 * <p>
 * DataBlockGroups are created when a {@link PropertySet} is inflated, see {@link PropertySet#inflate(IStruct)}}.
 * </p>
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author marco
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */

public class DataBlockGroup implements Serializable, Comparable<DataBlockGroup> {
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(DataBlockGroup.class);

	protected DataBlockGroup() {
	}

	/**
	 * Create a new {@link DataBlockGroup} for the given {@link PropertySet}
	 * and corresponding to the {@link StructBlock} referenced by the given
	 * {@link StructBlock} primary key fields.
	 *
	 * @param _prop The {@link PropertySet} the {@link DataBlockGroup} should be created for.
	 * @param _structBlockOrganisationID The organisation ID of the {@link StructBlock} this {@link DataBlockGroup} should represent.
	 * @param _structBlockID The ID of the {@link StructBlock} this {@link DataBlockGroup} should represent.
	 */
	public DataBlockGroup(PropertySet _prop, String _structBlockOrganisationID, String _structBlockID) {
		this.prop = _prop;
		this.structBlockOrganisationID = _structBlockOrganisationID;
		this.structBlockID = _structBlockID;
		this.structBlockKey = StructBlock.getPrimaryKey(_structBlockOrganisationID, _structBlockID);
		this.organisationID = _prop.getOrganisationID();
		this.propertySetID = _prop.getPropertySetID();
		this.dataBlocks = new LinkedList<DataBlock>();
	}

	/**
	 * Adds the given field {@link DataField} to the block
	 * it belongs to, i.e. it will be added only to the
	 * non-persistent structure of the {@link PropertySet}.
	 *
	 * @param field The {@link DataField} to add.
	 */
	void addDataFieldToStructure(DataField field) {
		DataBlock fieldBlock = dataBlockMap.get(field.getDataBlockID());
		if (fieldBlock == null)
			fieldBlock = new DataBlock(this, field.getDataBlockID());
		fieldBlock.addDataFieldToStructure(field);
	}

	/**
	 * Returns the {@link StructBlock} this {@link DataBlockGroup} represents
	 * in the given structure.
	 * <p>
	 * Note, that an {@link IllegalStateException} will be thrown if the block
	 * can not be found in the given structure.
	 * </p>
	 * @param structure The structure to searched the {@link StructBlock} of this {@link DataBlockGroup} in.
	 * @return The {@link StructBlock} this {@link DataBlockGroup} represents.
	 */
	public StructBlock getStructBlock(IStruct structure) {
		try {
			return structure.getStructBlock(getStructBlockOrganisationID(), getStructBlockID());
		} catch (StructBlockNotFoundException e) {
			IllegalStateException ill = new IllegalStateException("Caught Exception when accessing PersonStructFactory");
			ill.initCause(e);
			throw ill;
		}
	}

	private String organisationID;
	private long propertySetID;
	private String structBlockOrganisationID;
	private String structBlockID;

	private String structBlockKey;

	private List<DataBlock> dataBlocks;

	/**
	 * @return Returns the structBlockKey.
	 */
	public String getStructBlockKey() {
		return structBlockKey;
	}

	private PropertySet prop;

	/**
	 * @return Returns the prop.
	 */
	public PropertySet getProperty() {
		return prop;
	}

	/**
	 * @param prop The prop to set.
	 */
	void setProperty(PropertySet prop) {
		this.prop = prop;
	}

	/**
	 * key: Integer dataBlockID<br/>
	 * value: DataBlock dataBlock
	 */
	protected Map<Integer, DataBlock> dataBlockMap = new HashMap<Integer, DataBlock>();

	/**
	 * Checks whether this {@link DataBlockGroup} contains no {@link DataBlock}s.
	 * @return Whether this {@link DataBlockGroup} contains no {@link DataBlock}s.
	 */
	public boolean isEmpty() {
		if (dataBlockMap == null)
			return true;
		return dataBlockMap.isEmpty();
	}

	/**
	 * @param dataBlockID
	 *          The ID of the desired DataBlock.
	 * @return Returns the desired DataBlock, if existent.
	 * @throws DataBlockNotFoundException
	 *           if the desired DataBlock does not exist.
	 *
	 * @see #getDataBlock(int, boolean))
	 */
	public DataBlock getDataBlock(int dataBlockID) throws DataBlockNotFoundException {
		return getDataBlock(dataBlockID, true);
	}

	/**
	 * Returns the data block with the given index.
	 * <p>
	 * Note that the given index does not correspond to
	 * {@link DataBlock#getDataBlockID()}, but is the index
	 * in the list of {@link DataBlock}s of this {@link DataBlockGroup}.
	 * </p>
	 *
	 * @param index The index of the data block to be returned
	 * @return The data block with the given index
	 * @throws DataBlockNotFoundException If no such {@link DataBlock} exists.
	 */
	public DataBlock getDataBlockByIndex(int index) throws DataBlockNotFoundException {
		if (index < 0 || index >= dataBlocks.size())
			throw new DataBlockNotFoundException("DataBlock with index " + index + " was not found.");

		return dataBlocks.get(index);
	}

	/**
	 * Returns the data block with the given ID.
	 * <p>
	 * Note that the given ID corresponds to
	 * {@link DataBlock#getDataBlockID()} and is not
	 * the index in the list of {@link DataBlock}s of this {@link DataBlockGroup}.
	 * </p>
	 *
	 * @param dataBlockID
	 *          The ID of the desired DataBlock.
	 * @param throwExceptionIfNotFound
	 *          Controls whether or not to throw an exception, if the desired
	 *          DataBlock does not exist.
	 * @return the desired DataBlock, if existent, otherwise it may return
	 *         null or throw an exception.
	 * @throws DataBlockNotFoundException
	 *           if the desired DataBlock does not exist and
	 *           throwExceptionIfNotFound is true.
	 *
	 * @see #getDataBlock(int)
	 */
	public DataBlock getDataBlock(int dataBlockID, boolean throwExceptionIfNotFound) throws DataBlockNotFoundException {
		DataBlock pdb = null;
		if (dataBlockMap != null) {
			pdb = dataBlockMap.get(Integer.valueOf(dataBlockID));
		}

		if (throwExceptionIfNotFound && pdb == null)
			throw new DataBlockNotFoundException("DataBlock(organisationID=\"" + organisationID + "\", propertySetID=\"" + propertySetID + "\", structBlockID=\""
					+ structBlockID + "\", propBlockID=\"" + dataBlockID + "\") not existent!");
		return pdb;
	}

	/**
	 * Adds a new DataBlock to this group. All fields within the Block are
	 * initialized as well.<br/> If the second Block for a unique StructBlock
	 * should be created a DataBlockUniqueException is thrown.<br>
	 * TODO delete this method later
	 * @param structBlock The structBlock according to which a new {@link DataBlock} should be added.
	 * @param index The index at which the new {@link DataBlock} should be inserted or <code>-1</code> if it should be appended at the end.
	 * @return the just created {@link DataBlock}.
	 * @throws DataBlockUniqueException If the corresponding {@link StructBlock} is marked unique.
	 */
	public DataBlock addDataBlock_old(StructBlock structBlock, int index) throws DataBlockUniqueException {
		if (index < -1 || index > dataBlockMap.size())
			throw new IllegalArgumentException("index < -1 || index > dataBlockMap.size() !!!");

		int tries = 0;
		int newBlockID;
		do {
			newBlockID = (int) (Integer.MAX_VALUE * Math.random());
			if (tries++ > 10000)
				throw new RuntimeException("Could not generate new dataBlockID in 10000 tries.");
		} while (dataBlockMap.containsKey(newBlockID));

		if (dataBlockMap.size() > 0) { // not the first DataBlock
			if (structBlock.isUnique())
				throw new DataBlockUniqueException("Attempt to add second DataBlock for StructBlock " + structBlock.getPrimaryKey()	+ " which is unique!");
		}
		// this constructor initializes fields as well
		DataBlock newBlock = new DataBlock(this, newBlockID);

		if (index == -1)
			index = dataBlockMap.size();
		newBlock.setIndex(index, true);

		dataBlocks.add(index, newBlock);
		dataBlockMap.put(newBlock.getDataBlockID(), newBlock);
		storeDataBlockIndices();

		newBlock.inflate(structBlock);

		return newBlock;
	}

	/**
	 * Adds a new DataBlock to this group. All fields within the Block are initialized as well.<br/> If the second Block for a unique StructBlock
	 * should be created a DataBlockUniqueException is thrown.
	 * @param structBlock The structBlock according to which a new {@link DataBlock} should be added.
	 * @param index The index at which the new {@link DataBlock} should be inserted or <code>-1</code> if it should be appended at the end.
	 * @return the just created {@link DataBlock}.
	 * @throws DataBlockUniqueException If the corresponding {@link StructBlock} is marked unique.
	 */
	public DataBlock addDataBlock(StructBlock structBlock, int index) throws DataBlockUniqueException {
		if (index < -1 || index > dataBlockMap.size()) {
			throw new IllegalArgumentException("index < -1 || index > dataBlockMap.size() !!!");
		}
		if (dataBlockMap.size() > 0) { // not the first DataBlock
			if (structBlock.isUnique()) {
				throw new DataBlockUniqueException("Attempt to add second DataBlock for StructBlock " + structBlock.getPrimaryKey()	+ " which is unique!");
			}
		}
		int tries = 0;
		int newBlockID;
		do {
			newBlockID = (int) (Integer.MAX_VALUE * Math.random());
			if (tries++ > 10000) {
				throw new RuntimeException("Could not generate new dataBlockID in 10000 tries.");
			}
		} while (dataBlockMap.containsKey(newBlockID));

		// This constructor initializes fields as well
		final DataBlock newDataBlock = new DataBlock(this, newBlockID);

		if (index == -1) {
			index = dataBlockMap.size();
		}
		newDataBlock.setIndex(index, false);	// The computed index is set for the new DataBlock only, not for its DataFields. This is performed later after inflation.
		dataBlocks.add(index, newDataBlock);
		if (dataBlocks.size() > index + 1) {
			for (int j = index + 1; j < dataBlocks.size(); j++) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Adjusting moved data block " + j + "...");
				dataBlocks.get(j).setIndex(j, true);	// Adjust the ID of all DataBlocks and associated DataFields that have changed their position in this DataBlockGroup due to the insertion of the new DataBlock.
			}
		}
		dataBlockMap.put(newDataBlock.getDataBlockID(), newDataBlock);
		newDataBlock.inflate(structBlock);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Added new DataBlock (index " + index + ") to DataBlockGroup " + this.getStructBlockKey());

		return newDataBlock;
	}

	/**
	 * Deletes the given {@link DataBlock} from this {@link DataBlockGroup} and removes all of its {@link DataField}s from the persistent
	 * list of {@link DataField}s in the corresponding {@link PropertySet}.
	 *
	 * @param dataBlock The {@link DataBlock} to be deleted
	 * @return The deleted {@link DataBlock} or <code>null</code> if it didn't exist in this {@link DataBlockGroup}.
	 * @throws DataBlockRemovalException If the given {@link DataBlock} is the last remaining on in its group and hence cannot be deleted.
	 */
	public DataBlock removeDataBlock(DataBlock dataBlock) throws DataBlockRemovalException {
//		if (dataBlockMap.size() == 1)
//			throw new DataBlockRemovalException("The given DataBlock could not be deleted since it is the last one for its DataBlockGroup.");

		DataBlock removedBlock = dataBlockMap.remove(dataBlock.getDataBlockID());
		dataBlocks.remove(removedBlock);

		for (DataField dataField : removedBlock.getDataFields())
			prop.internalRemoveDataFieldFromPersistentCollection(dataField);

		storeDataBlockIndices();

		return removedBlock;
	}

	/**
	 * Stores the indices of the {@link DataBlock}s in this {@link DataBlockGroup} according to their order in {@link #dataBlocks}.
	 */
	private void storeDataBlockIndices() {
		int blockIndex = 0;
		for (DataBlock dataBlock : dataBlocks)
			dataBlock.setIndex(blockIndex++, true);
	}

	/**
	 * Adds the given {@link DataBlock} to this {@link DataBlockGroup}. This method should only be used during the inflation process
	 * and is hence package-private.
	 *
	 * @param dataBlock The {@link DataBlock} to be added.
	 */
	void addDataBlock(DataBlock dataBlock) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Adding DataBlock " + dataBlock.getDataBlockID() + " (index " + dataBlock.getIndex() + ") to DataBlockGroup " + this.getStructBlockKey());
		dataBlockMap.put(dataBlock.getDataBlockID(), dataBlock);
		dataBlocks.add(dataBlock);
	}

	/**
	 * @return Returns the dataBlockMap.
	 */
	public List<DataBlock> getDataBlocks() {
		return dataBlocks;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the propertySetID.
	 */
	public long getPropertySetID() {
		return propertySetID;
	}

	/**
	 * Package visible modifier for the propertySetID
	 *
	 * @param _propID
	 */
	void setPropertySetID(long _propID) {
		this.propertySetID = _propID;
	}

	/**
	 * @return Returns the structBlockID.
	 */
	public String getStructBlockID() {
		return structBlockID;
	}

	/**
	 * @return Returns the structBlockID.
	 */
	public String getStructBlockOrganisationID() {
		return structBlockOrganisationID;
	}

	/**
	 * Scans all DataBlocks and its containing DataFields and removes all entries
	 * where isEmpty() returns true.
	 */
	void deflate() {
		int blockIndex = 0;

		for (Iterator<DataBlock> it = getDataBlocks().iterator(); it.hasNext(); ) {
			DataBlock block = it.next();
			boolean isBlockEmpty = block.deflate();

			if (isBlockEmpty) {
				it.remove();
				dataBlockMap.remove(block.getDataBlockID());
			} else {
				block.setIndex(blockIndex++, true);
			}
		}
	}

	/**
	 * Inflates this {@link DataBlockGroup} by inflating each of its {@link DataBlock}s. This in turn adds all non-existing fields for each block.
	 * @param structure The {@link StructBlock} according to which this {@link DataBlockGroup} should be inflated.
	 */
	void inflate(StructBlock structBlock) {
		if (dataBlockMap.isEmpty()) {
			try {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("DataBlockMap is empty.");
				addDataBlock(structBlock, -1);
			} catch (final DataBlockUniqueException e) {
				// weird state, should never happen
				throw new RuntimeException(e);
			}
		}
		for (DataBlock dataBlock : dataBlockMap.values()) {
			dataBlock.inflate(structBlock);
		}
		dataBlocks.clear();
		dataBlocks.addAll(dataBlockMap.values());
		Collections.sort(dataBlocks, new Comparator<DataBlock>() {
			public int compare(DataBlock o1, DataBlock o2) {
				return Integer.valueOf(o1.getIndex()).compareTo(o2.getIndex());
			}
		});
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("*********************** Sorted DataBlocks ***********************");
			for (DataBlock db : dataBlocks)
				LOGGER.debug(db.getDataBlockID());
			LOGGER.debug("*****************************************************************");
		}
	}

	private int priority = Integer.MAX_VALUE;

	/**
	 * @param priority
	 *          The priority to set.
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Validates this instance against the given {@link IStruct} and returns a list of all {@link ValidationFailureResult}
	 * occurred during the validation or <code>null</code> if the validation succeeded.
	 * @param struct The {@link IStruct} against which to validated.
	 * @return A list of all {@link ValidationFailureResult} occurred during the validation or <code>null</code> if the validation succeeded..
	 */
	public List<ValidationResult> validate(IStruct struct) {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		StructBlock structBlock = struct.getStructBlock(this);
		for (DataBlock dataBlock : getDataBlocks()) {
			for (IDataBlockValidator validator : structBlock.getDataBlockValidators()) {
				ValidationResult blockValidatorResult = validator.validate(dataBlock, structBlock);
				if (blockValidatorResult != null)
					results.add(blockValidatorResult);
			}

			List<ValidationResult> blockValidationResults = dataBlock.validate(struct);
			if (blockValidationResults != null)
				results.addAll(blockValidationResults);
		}

		if (results.isEmpty())
			return null;
		else
			return results;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DataBlockGroup other) {
		if (other.priority < this.priority)
			return 1;
		else if (other.priority > this.priority)
			return -1;
		else {
			if (this.equals(other)) {
				// to be consistent with equals
				return 0;
			} else
				return 1;
		}
	}
}
