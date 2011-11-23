/* ********************************************************************
 * NightLabsBase - Utilities by NightLabs                             *
 * Copyright (C) 2004-2008 NightLabs GmbH - http://NightLabs.org      *
 *                                                                    *
 * This library is free software; you can redistribute it and/or      *
 * modify it under the terms of the GNU Lesser General Public         *
 * License as published by the Free Software Foundation; either       *
 * version 2.1 of the License, or (at your option) any later version. *
 *                                                                    *
 * This library is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  *
 * Lesser General Public License for more details.                    *
 *                                                                    *
 * You should have received a copy of the GNU Lesser General Public   *
 * License along with this library; if not, write to the              *
 *     Free Software Foundation, Inc.,                                *
 *     51 Franklin St, Fifth Floor,                                   *
 *     Boston, MA  02110-1301  USA                                    *
 *                                                                    *
 * Or get it online:                                                  *
 *     http://www.gnu.org/copyleft/lesser.html                        *
 **********************************************************************/
package org.nightlabs.jfire.prop;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.exception.DataBlockRemovalException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FieldInheriter} implementation for {@link PropertySet}s. This is used for inheriting data from a given mother
 * PropertySet to a given child PropertySet.
 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
 * @author Marco Schulze
 */
public class PropertySetFieldInheritor implements FieldInheriter {

	/** Logger used by this class. */
	private static final Logger logger = LoggerFactory.getLogger(PropertySetFieldInheritor.class);

	/** PropertySet of the mother. */
	private PropertySet propSet_Mother;

	/** PropertySet of the child. */
	private PropertySet propSet_Child;

	/** Map keeping track of StructBlock keys and corresponding DataBlockGroups of the child. */
	private Map<String, DataBlockGroup> structBlockKeyToDataBlockGroup_Child = new HashMap<String, DataBlockGroup>();

	/**
	 * Map whereby key is the DataBlockID of the cloneable DataField (that belongs to the mother) and value an instance of
	 * CloneDataBlockDescriptor that keeps track of the ID and index of the newly generated DataBlock the clone (that belongs
	 * to the child) is placed in.
	 */
	private Map<Integer, CloneDataBlockDescriptor> cloneableDBIDToCloneDBDescriptor
		= new HashMap<Integer, CloneDataBlockDescriptor>();

	/**
	 * Helper class used for keeping track of the ID and the index of the DataBlock a cloned DataField (that belongs to the child)
	 * is placed in. Instances of this class are set as value of the map cloneableDataBlockIDToCloneDataBlockDescriptor.
	 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
	 */
	private class CloneDataBlockDescriptor {
		private int cloneDataBlockID;
		private int cloneDataBlockIndex;

		public CloneDataBlockDescriptor(final int cloneDataBlockID, final int cloneDataBlockIndex) {
			this.cloneDataBlockID = cloneDataBlockID;
			this.cloneDataBlockIndex = cloneDataBlockIndex;
		}
		public int getCloneDataBlockID() {
			return cloneDataBlockID;
		}
		public int getCloneDataBlockIndex() {
			return cloneDataBlockIndex;
		}
	}

	@Override
	public void copyFieldValue(final Inheritable mother, final Inheritable child, final Class<?> motherClass,
		final Class<?> childClass, final Field motherField, final Field childField, final FieldMetaData motherFieldMetaData,
		final FieldMetaData childFieldMetaData) {

		if (!(mother instanceof PropertySet) || !(child instanceof PropertySet))
			return;

		propSet_Mother = (PropertySet) mother;
		propSet_Child = (PropertySet) child;

		final Map<String, List<DataBlock>> structBlockKeyToDataBlocks_Mother = new HashMap<String, List<DataBlock>>();
		final Map<String, List<DataBlock>> structBlockKeyToDataBlocks_Child = new HashMap<String, List<DataBlock>>();
		final Collection<DataBlockGroup> dataBlockGroups_Mother = propSet_Mother.getDataBlockGroups();
		Collection<DataBlockGroup> dataBlockGroups_Child = propSet_Child.getDataBlockGroups();

		logger.debug("copyFieldValue: dataBlockGroups_Mother.size={}", dataBlockGroups_Mother.size()); //$NON-NLS-1$
		logger.debug("copyFieldValue: dataBlockGroups_Child.size={}", dataBlockGroups_Child.size()); //$NON-NLS-1$

		for (final Iterator<DataBlockGroup> it = dataBlockGroups_Mother.iterator(); it.hasNext();) {
			final DataBlockGroup dbg = it.next();
			final List<DataBlock> dataBlocksSorted_Mother = sortDBs(dbg.getDataBlocks());
			structBlockKeyToDataBlocks_Mother.put(dbg.getStructBlockKey(), dataBlocksSorted_Mother);
		}

		for (final Iterator<DataBlockGroup> it = dataBlockGroups_Child.iterator(); it.hasNext();) {
			final DataBlockGroup dbg = it.next();
			final List<DataBlock> dataBlocksSorted_Child = sortDBs(dbg.getDataBlocks());
			structBlockKeyToDataBlocks_Child.put(dbg.getStructBlockKey(), dataBlocksSorted_Child);
			structBlockKeyToDataBlockGroup_Child.put(dbg.getStructBlockKey(), dbg);
		}

		// In the case there are no mother data block groups at all.
		if (dataBlockGroups_Mother.size() == 0) {
			processMissingDBsMother(structBlockKeyToDataBlocks_Child);
			return;
		}

		for (Map.Entry<String, List<DataBlock>> entry : structBlockKeyToDataBlocks_Mother.entrySet()) {
			processDBsMother(structBlockKeyToDataBlocks_Child, entry);
		}
	}

	/**
	 * Called in case there are no mother DataBlockGroups at all.
	 * @param structBlockKeyToDataBlocks_Child The map keeping track of the child DataBlocks for each DataBlockGroup.
	 */
	private void processMissingDBsMother(final Map<String, List<DataBlock>> structBlockKeyToDataBlocks_Child) {
		for (final List<DataBlock> dataBlocks_Child : structBlockKeyToDataBlocks_Child.values()) {
			if (dataBlocks_Child.size() > 0) {
				// Delete redundant child data blocks until one data block is left (for each data block group).
				while (dataBlocks_Child.size() > 1) {
					dataBlocks_Child.remove(dataBlocks_Child.size() - 1);
				}
				// Delete the content of the fields of the remaining data block (for each data block group).
				final Collection<DataField> dataFieldsChild = dataBlocks_Child.get(0).getDataFields();
				for (final Iterator<DataField> it2 = dataFieldsChild.iterator(); it2.hasNext();) {
					it2.next().setData(null);
				}
			}
		}
	}

	/**
	 * Traverses all mother DataBlockGroups and compares the amount of appropriate DataBocks with the amount of DataBlocks of
	 * the corresponding DataBlockGroup of the child.
	 * <ul>
	 * <li>If the mother has more DataBlocks the missing amount of DataFields (not blocks) will be added to the child.</li>
	 * <li>If the mother has less DataBlocks all redundant DataBlocks (in this case blocks) will be removed from the child.</li>
	 * <li>If mother and child have the same amount of DataBlocks for this DataBlockGroup, data will be copied from mother to child
	 * (only considering non-cloned DataFields as clones do already contain the data of their corresponding cloneable).</li>
	 * </ul>
	 * @param structBlockKeyToDataBlocks_Child The map keeping track of the child DataBlocks for each DataBlockGroup.
	 * @param entry The currently considered entry of structBlockKeyToDataBlocks_Mother.
	 */
	private void processDBsMother(final Map<String, List<DataBlock>> structBlockKeyToDataBlocks_Child,
		Map.Entry<String, List<DataBlock>> entry) {

		final String structBlockKey = entry.getKey();
		final List<DataBlock> dataBlocks_Mother = entry.getValue();
		List<DataBlock> dataBlocks_Child = structBlockKeyToDataBlocks_Child.get(structBlockKey);

		if (dataBlocks_Child != null) {
			final int diffDBs = dataBlocks_Mother.size() - dataBlocks_Child.size();
			// Do only consider a certain part of DataBlocks of the mother in the case it has more DataBlocks than the
			// child as all DataFields of the additional blocks are cloned (see PropertySetFieldInheritor#addMissingDataFields).
			final int amountDBsToConsider = diffDBs > 0 ? dataBlocks_Mother.size() - diffDBs : dataBlocks_Mother.size();

			if (diffDBs < 0)
				dataBlocks_Child = processRedundantDBs(structBlockKeyToDataBlocks_Child, structBlockKey, dataBlocks_Child, diffDBs);
			else if (diffDBs > 0)
				dataBlocks_Child = processMissingDBs(structBlockKeyToDataBlocks_Child, structBlockKey);

			logger.debug("processDBsMother: #dataBlocks(toConsiderExcludingClones)={}", amountDBsToConsider); //$NON-NLS-1$
			logger.debug("processDBsMother: #dataBlocks(afterAddingRemoving(mother))={}", dataBlocks_Mother.size()); //$NON-NLS-1$
			logger.debug("processDBsMother: #dataBlocks(afterAddingRemoving(child))={}", dataBlocks_Child.size());	//$NON-NLS-1$

			// As now both mother and child have the same amount of DataBlocks (and DataFields) for the currently considered
			// DataBlockGroup (struct block), copy the content of each mother DataField to the corresponding child DataField.
			processDBs(dataBlocks_Mother, dataBlocks_Child);
		}
	}

	/**
	 * Copies data from mother to child. If an appropriate DataField cannot be found on child side one will be cloned from mother
	 * side and added to the suitable child DataBlock.
	 * <p>
	 * Note, mother and child must have the same amount of DataBlocks when this method is called, otherwise an exception
	 * will be thrown.
	 * </p>
	 * @param dataBlocks_Mother The mother DataBlocks for the currently considered DataBlockGroup.
	 * @param dataBlocks_Child The child DataBlocks for the currently considered DataBlockGroup.
	 */
	private void processDBs(final List<DataBlock> dataBlocks_Mother, List<DataBlock> dataBlocks_Child) {
		if (dataBlocks_Mother.size() != dataBlocks_Child.size())
			throw new IllegalArgumentException("dataBlocks_Mother.size() != dataBlocks_Child.size() :: " //$NON-NLS-1$
				+ dataBlocks_Mother.size() + " != " + dataBlocks_Child.size()); //$NON-NLS-1$

		Iterator<DataBlock> it_child = dataBlocks_Child.iterator();
		for (final DataBlock db_Mother : dataBlocks_Mother) {
			DataBlock db_Child = it_child.next();

			for (DataField df_Mother : db_Mother.getDataFields()) {
				DataField df_Child;
				try {
					df_Child = db_Child.getDataField(df_Mother.getStructFieldOrganisationID(), df_Mother.getStructFieldID());
					df_Child.setData(df_Mother.getData());
				} catch (final DataFieldNotFoundException exception) {
					df_Child = df_Mother.cloneDataField(propSet_Child, db_Child.getDataBlockID());
					try {
						db_Child.addDataField(df_Child);
					} catch (DuplicateKeyException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	/**
	 * The mother has more DataBlocks than the child with respect to the currently considered DataBlockGroup. Therefore, add
	 * the missing amount of DataFields by cloning the appropriate mother DataFields and adding them to the PropertySet of
	 * the child. Note, a new DataBlockID has to be created as it is part of the primary key of the table
	 * jfirebase_prop_propertyset_datafields, otherwise an integrity constraint exception would occur. All other parts of
	 * the primary key of the clone will take over the value of the cloneable DataField (except the PropertySetID).
	 * @param structBlockKeyToDataBlocks_Child The map keeping track of the child DataBlocks for each DataBlockGroup.
	 * @param structBlockKey The key of the currently considered DataBlockGroup.
	 * @return the adapted list of child DataBlocks
	 */
	private List<DataBlock> processMissingDBs(final Map<String, List<DataBlock>> structBlockKeyToDataBlocks_Child,
		final String structBlockKey) {

		addMissingDataFields(structBlockKey);
		// As DataBlocks and DataFields have been added to the child some kind of re-initialisation is necessary.
		Collection<DataBlockGroup> dataBlockGroups_Child = propSet_Child.getDataBlockGroups();	// possibly not necessary, but...

		for (final Iterator<DataBlockGroup> it = dataBlockGroups_Child.iterator(); it.hasNext();) {
			final DataBlockGroup dbg = it.next();
			structBlockKeyToDataBlocks_Child.put(dbg.getStructBlockKey(), dbg.getDataBlocks());
		}
		List<DataBlock> dataBlocks_Child = structBlockKeyToDataBlocks_Child.get(structBlockKey);

		return dataBlocks_Child;
	}

	/**
	 * The mother has more DataBlocks than the child with respect to the currently considered DataBlockGroup. Therefore, add the
	 * missing amount of DataFields by cloning the appropriate mother DataFields and adding them to the PropertySet of the child.
	 * <p>
	 * A new DataBlock will be created and added to the considered DataBlockGroup of the child for each set of cloned
	 * DataFields that belong to a common DataBlock. The indices of the new DataBlocks are set according to the indices of the
	 * DataBlocks whose DataFields have been cloned. E.g., if the DataFields dfMother_2_1 and dfMother_2_2 of the DataBlock
	 * dbMother_2 have to be cloned, the cloned DataFields dfChild_2_1 and dfChild_2_2 will be placed in a common DataBlock
	 * dbChild_2.
	 * </p><p>
	 * Note, a new DataBlockID has to be created as it is part of the primary key of the table jfirebase_prop_propertyset_datafields,
	 * otherwise an integrity constraint exception would occur. All other parts of the primary key of the clone will take over the
	 * appropriate values of the cloneable DataField (except the PropertySetID that is set to the ID of the PropertySet of the child).
	 * </p>
	 * @param structBlockKey The key of the currently considered StructBlock.
	 */
	private void addMissingDataFields(final String structBlockKey) {
		OUTER: for (final Map.Entry<StructFieldID, List<DataField>> entry : propSet_Mother.getDataFieldsMap().entrySet()) {
			final StructFieldID structFieldID = entry.getKey();

			logger.debug("addMissingDataFields: #dataFields(structFieldID={}(mother))={}", structFieldID, entry.getValue().size());	//$NON-NLS-1$

			// Test whether the considered StructField belongs to the currently considered StructBlock like e.g.
			// dev.jfire.org/SimpleProductType.description (this test is necessary as we consider all persistent
			// DataFields when we call PropertySet#getDataFieldsMap).
			if (!(structFieldID.structBlockOrganisationID + "/" + structFieldID.structBlockID).equals(structBlockKey)) //$NON-NLS-1$
				continue OUTER;

			final List<DataField> dataFields_Mother = entry.getValue();
			final List<DataField> dataFields_Child = propSet_Child.getDataFieldsMap().get(structFieldID);

			if (dataFields_Child != null) {
				final int diffDataFields = dataFields_Mother.size() - dataFields_Child.size();
				for (int j = 0; j < diffDataFields; j++) {
					final DataField cloneable = dataFields_Mother.get(dataFields_Mother.size() - diffDataFields + j);
					if (!cloneableDBIDToCloneDBDescriptor.containsKey(cloneable.getDataBlockID())) {
						final CloneDataBlockDescriptor cdbDesc = new CloneDataBlockDescriptor(
							new Long(System.currentTimeMillis()).intValue(), cloneable.getDataBlockIndex());
						cloneableDBIDToCloneDBDescriptor.put(cloneable.getDataBlockID(), cdbDesc);
					}
					// Transfer new DataBlockID as parameter, i.e., adapt DataField constructor accordingly and create new clone
					// method (adapt each DataField type). => Done.
					final DataField clone = cloneable.cloneDataField(propSet_Child, cloneableDBIDToCloneDBDescriptor.get(
						cloneable.getDataBlockID()).getCloneDataBlockID());
					clone.setDataBlockIndex(cloneable.getDataBlockIndex());	// Keep DataBlock index as set in mother!
					propSet_Child.internalAddDataFieldToPersistentCollection(clone);

					logDataFieldPrimaryKeyContent(clone, false);
					logDataFieldPrimaryKeyContent(cloneable, true);
				}
			}
		}

		for (final CloneDataBlockDescriptor cdbDesc : cloneableDBIDToCloneDBDescriptor.values()) {
			// Create a new DataBlock that will be placed in the currently considered child DataBlockGroup and set its
			// ID according to the DataBlockID of the appropriate clones.
			final DataBlock db = new DataBlock(structBlockKeyToDataBlockGroup_Child.get(structBlockKey), cdbDesc.getCloneDataBlockID());
			db.setIndex(cdbDesc.getCloneDataBlockIndex(), true);
			// Add the new DataBlock to the list of DataBlocks of the currently considered child DataBlockGroup.
			structBlockKeyToDataBlockGroup_Child.get(structBlockKey).addDataBlock(db);

			logger.debug("addMissingDataFields: indexNewDataBlock(child)={}", cdbDesc.getCloneDataBlockIndex()); //$NON-NLS-1$
		}
		logDBs_Child();
	}

	/**
	 * The mother has less DataBlocks than the child with respect to the currently considered DataBlockGroup. Therefore, delete
	 * all redundant child DataBlocks (and their data fields) by removing them from the group.
	 * @param structBlockKeyToDataBlocks_Child The map keeping track of the child DataBlocks for each DataBlockGroup.
	 * @param structBlockKey The key of the currently considered DataBlockGroup.
	 * @param dataBlocks_Child The child DataBlocks for the currently considered DataBlockGroup.
	 * @param diffDataBlocks The amount of DataBlocks to be removed (negative value).
	 * @return the adapted list of child DataBlocks
	 */
	private List<DataBlock> processRedundantDBs(final Map<String, List<DataBlock>> structBlockKeyToDataBlocks_Child,
		final String structBlockKey, List<DataBlock> dataBlocks_Child, final int diffDataBlocks) {

		removeRedundantDBs(dataBlocks_Child, diffDataBlocks);
		// As DataBlocks and DataFields have been removed from the child some kind of re-initialisation is necessary.
		Collection<DataBlockGroup> dataBlockGroups_Child = propSet_Child.getDataBlockGroups();	// possibly not necessary, but...

		for (final Iterator<DataBlockGroup> it = dataBlockGroups_Child.iterator(); it.hasNext();) {
			final DataBlockGroup dbg = it.next();
			structBlockKeyToDataBlocks_Child.put(dbg.getStructBlockKey(), dbg.getDataBlocks());
		}
		dataBlocks_Child = structBlockKeyToDataBlocks_Child.get(structBlockKey);

		return dataBlocks_Child;
	}

	/**
	 * The mother has less DataBlocks than the child with respect to the currently considered DataBlockGroup. Therefore, delete
	 * all redundant child DataBlocks (and their data fields) by removing them from the group.
	 * @param dataBlocks_Child The complete list of DataBlocks of the child for the currently considered DataBlockGroup before
	 * removing redundant DataBlocks.
	 * @param diffDataBlocks The amount of DataBlocks to be removed (negative value).
	 */
	private void removeRedundantDBs(final List<DataBlock> dataBlocks_Child, final int diffDataBlocks) {
		for (int i = 0; i < -diffDataBlocks; i++) {
			final DataBlock db = dataBlocks_Child.get(dataBlocks_Child.size() - 1);
			try {
				db.getDataBlockGroup().removeDataBlock(db);
			} catch (final DataBlockRemovalException e) {
				throw new RuntimeException(e);
			}
			dataBlocks_Child.remove(db);
		}
	}

	/**
	 * Sorts the given list of DataBlocks according to the indices of each DataBlock and returns a new list containing the
	 * sorted DataBlocks.
	 * @param dataBlocks A list of DataBlocks to be sorted.
	 * @return a new list containing the sorted DataBlocks
	 */
	private List<DataBlock> sortDBs(final List<DataBlock> dataBlocks) {
		final List<DataBlock> dataBlocksSorted = new ArrayList<DataBlock>(dataBlocks);
		Collections.sort(dataBlocksSorted, new Comparator<DataBlock>() {
			@Override
			public int compare(DataBlock o1, DataBlock o2) {
				int idx1 = o1.getIndex();
				int idx2 = o2.getIndex();
				return idx1 < idx2 ? -1 : (idx1 == idx2 ? 0 : +1);
			}
		});

		logger.debug("Sorted DataBlock indices: "); //$NON-NLS-1$
		for (DataBlock db : dataBlocksSorted)
			logger.debug("{}", db.getIndex()); //$NON-NLS-1$

		return dataBlocksSorted;
	}

	private void logDataFieldPrimaryKeyContent(final DataField dataField, final boolean isCloneable) {
		if (isCloneable)
			logger.debug("*********************** DataField PK (mother) ***********************"); //$NON-NLS-1$
		else
			logger.debug("*********************** DataField PK (child) ***********************"); //$NON-NLS-1$
		logger.debug("logDataFieldPrimaryKeyContent: dataBlockID={}", dataField.getDataBlockID()); //$NON-NLS-1$
		logger.debug("logDataFieldPrimaryKeyContent: organisationID={}", dataField.getOrganisationID()); //$NON-NLS-1$
		logger.debug("logDataFieldPrimaryKeyContent: propertySetID={}", dataField.getPropertySetID()); //$NON-NLS-1$
		logger.debug("logDataFieldPrimaryKeyContent: structBlockKey={}", dataField.getStructBlockKey()); //$NON-NLS-1$
		logger.debug("logDataFieldPrimaryKeyContent: structFieldPK={}", dataField.getStructFieldPK()); //$NON-NLS-1$
		if (isCloneable)
			logger.debug("*********************** DataField PK (mother) ***********************"); //$NON-NLS-1$
		else
			logger.debug("*********************** DataField PK (child) ***********************"); //$NON-NLS-1$
	}

	private void logDBs_Child() {
		final Iterator<DataBlockGroup> it = propSet_Child.getDataBlockGroups().iterator();
		logger.debug("*********************** DataBlocks (child) ***********************"); //$NON-NLS-1$
		while (it.hasNext()) {
			final List<DataBlock> dataBlocks = it.next().getDataBlocks();
			for (DataBlock db : dataBlocks) {
				logger.debug("logDBs_Child: dataBlockID={}", db.getDataBlockID()); //$NON-NLS-1$
				logger.debug("logDBs_Child: dataBlockIndex={}", db.getIndex()); //$NON-NLS-1$
			}
		}
		logger.debug("*********************** DataBlocks (child) ***********************"); //$NON-NLS-1$
	}
}
