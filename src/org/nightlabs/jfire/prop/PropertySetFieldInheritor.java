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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.jfire.prop.exception.DataBlockRemovalException;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 *
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class PropertySetFieldInheritor implements FieldInheriter {

	/** Logger used by this class. */
	private static final Logger LOGGER = Logger.getLogger(PropertySetFieldInheritor.class);
	/** PropertySet of the mother. */
	private PropertySet propSet_Mother;
	/** PropertySet of the child. */
	private PropertySet propSet_Child;
	/** Map keeping track of StructBlock keys and corresponding DataBlockGroups of the child. */
	private Map<String, DataBlockGroup> structBlockKeyToDataBlockGroup_Child = new HashMap<String, DataBlockGroup>();
	/** Map whereby key is the DataBlockID of the cloneable DataField (that belongs to the mother) and value an instance of CloneDataBlockDescriptor that keeps track of the ID and index of the newly generated DataBlock the clone (that belongs to the child) is placed in. */
	private Map<Integer, CloneDataBlockDescriptor> cloneableDataBlockIDToCloneDataBlockDescriptor = new HashMap<Integer, CloneDataBlockDescriptor>();

	/** Helper class used for keeping track of the ID and the index of the DataBlock a cloned DataField (that belongs to the child) is placed in. Instances of this class are set as value of the map cloneableDataBlockIDToCloneDataBlockDescriptor. */
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
	public void copyFieldValue(final Inheritable mother, final Inheritable child, final Class<?> motherClass, final Class<?> childClass,
		final Field motherField, final Field childField, final FieldMetaData motherFieldMetaData, final FieldMetaData childFieldMetaData) {

		if (!(mother instanceof PropertySet) || !(child instanceof PropertySet))
			return;

		propSet_Mother = (PropertySet) mother;
		propSet_Child = (PropertySet) child;

		final Map<String, List<DataBlock>> structBlockKeyToDataBlocks_Mother = new HashMap<String, List<DataBlock>>();
		final Map<String, List<DataBlock>> structBlockKeyToDataBlocks_Child = new HashMap<String, List<DataBlock>>();
		final Collection<DataBlockGroup> dataBlockGroups_Mother = propSet_Mother.getDataBlockGroups();
		Collection<DataBlockGroup> dataBlockGroups_Child = propSet_Child.getDataBlockGroups();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Amount of DataBlockGroups (mother): " + dataBlockGroups_Mother.size());
			LOGGER.debug("Amount of DataBlockGroups (child):  " + dataBlockGroups_Child.size());
		}

		for (final Iterator<DataBlockGroup> it = dataBlockGroups_Mother.iterator(); it.hasNext();) {
			final DataBlockGroup dbg = it.next();
			final List<DataBlock> dataBlocksSorted_Mother = sortDataBlocks(dbg.getDataBlocks());
			structBlockKeyToDataBlocks_Mother.put(dbg.getStructBlockKey(), dataBlocksSorted_Mother);
		}

		for (final Iterator<DataBlockGroup> it = dataBlockGroups_Child.iterator(); it.hasNext();) {
			final DataBlockGroup dbg = it.next();
			final List<DataBlock> dataBlocksSorted_Child = sortDataBlocks(dbg.getDataBlocks());
			structBlockKeyToDataBlocks_Child.put(dbg.getStructBlockKey(), dataBlocksSorted_Child);
			structBlockKeyToDataBlockGroup_Child.put(dbg.getStructBlockKey(), dbg);
		}

		// In the case there are no mother data block groups at all.
		if (dataBlockGroups_Mother.size() == 0) {
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
			return;
		}

		// Traverse all DataBlockGroups of the mother and compare the amount of appropriate DataBocks with the amount of DataBlocks of the corresponding DataBlockGroup of the child.
		// If the mother has more DataBlocks add the missing amount of DataFields (not blocks) to the child. If the mother has less DataBlocks remove all redundant DataBlocks (in this case blocks) from the child.
		// Finally, if mother and child have the same amount of DataBlocks for this DataBlockGroup data is copied from mother to child (only considering non-cloned DataFields as clones do already contain the data of their corresponding cloneable).
		for (Map.Entry<String, List<DataBlock>> entry : structBlockKeyToDataBlocks_Mother.entrySet()) {
			final String structBlockKey = entry.getKey();
			final List<DataBlock> dataBlocks_Mother = entry.getValue();
			List<DataBlock> dataBlocks_Child = structBlockKeyToDataBlocks_Child.get(structBlockKey);
			if (dataBlocks_Child != null) {
				final int diffDataBlocks = dataBlocks_Mother.size() - dataBlocks_Child.size();
				final int dataBlocksToConsider = diffDataBlocks > 0 ? dataBlocks_Mother.size() - diffDataBlocks : dataBlocks_Mother.size();	// Do only consider a certain part of DataBlocks of the mother in the case it has more DataBlocks than the child as all DataFields of the additional blocks are cloned (see PropertySetFieldInheritor#addMissingDataFields).
				if (diffDataBlocks < 0) {
					// The mother has less DataBlocks than the child with respect to the currently considered DataBlockGroup.
					// Therefore, delete all redundant child DataBlocks (and their data fields) by removing them from the group.
					removeRedundantDataBlocks(dataBlocks_Child, diffDataBlocks);
					// As DataBlocks and DataFields have been removed from the child some kind of re-initialisation is necessary.
					dataBlockGroups_Child = propSet_Child.getDataBlockGroups();	// possibly not necessary, but...
					for (final Iterator<DataBlockGroup> it = dataBlockGroups_Child.iterator(); it.hasNext();) {
						final DataBlockGroup dbg = it.next();
						structBlockKeyToDataBlocks_Child.put(dbg.getStructBlockKey(), dbg.getDataBlocks());
					}
					dataBlocks_Child = structBlockKeyToDataBlocks_Child.get(structBlockKey);
				}
				else if (diffDataBlocks > 0) {
					// The mother has more DataBlocks than the child with respect to the currently considered DataBlockGroup.
					// Therefore, add the missing amount of DataFields by cloning the appropriate mother DataFields and adding them to the PropertySet of the child.
					// Note, a new DataBlockID has to be created as it is part of the primary key of the table JFireBase_Prop_PropertySet_dataFields. Otherwise
					// an integrity constraint exception would occur. All other parts of the primary key of the clone will take over the value of the cloneable DataField (except the PropertySetID).
					addMissingDataFields(structBlockKey);
					// As DataBlocks and DataFields have been added to the child some kind of re-initialisation is necessary.
					dataBlockGroups_Child = propSet_Child.getDataBlockGroups();	// possibly not necessary, but...
					for (final Iterator<DataBlockGroup> it = dataBlockGroups_Child.iterator(); it.hasNext();) {
						final DataBlockGroup dbg = it.next();
						structBlockKeyToDataBlocks_Child.put(dbg.getStructBlockKey(), dbg.getDataBlocks());
					}
					dataBlocks_Child = structBlockKeyToDataBlocks_Child.get(structBlockKey);
				}

				// As now both mother and child have the same amount of DataBlocks (and DataFields) for the currently considered DataBlockGroup (struct block) copy the content of each mother DataField to the corresponding child DataField.
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Amount of DataBlocks to consider (excluding clones): " + dataBlocksToConsider);
					LOGGER.debug("Amount of DataBlocks after adding/removing (mother): " + dataBlocks_Mother.size());
					LOGGER.debug("Amount of DataBlocks after adding/removing (child):  " + dataBlocks_Child.size());
				}
				if (dataBlocks_Mother.size() == dataBlocks_Child.size()) {
					OUTER: for (int i = 0; i < dataBlocksToConsider; i++) {
						// 1st possibility (not working yet as list of DataBlocks is not sorted correctly, see PropertySetFieldInheritor#sortDataBlocks. Why?).
//						if (LOGGER.isDebugEnabled()) {
//							final DataBlock db_Mother = dataBlocks_Mother.get(i);
//							final DataBlock db_Child = dataBlocks_Child.get(i);
//							LOGGER.debug("Will now copy data from DataBlock index " + db_Mother.getIndex() + " (mother) to DataBlock index " + db_Child.getIndex() + " (child)");
//						}
//						final Iterator<DataField> it1 = dataBlocks_Mother.get(i).getDataFields().iterator();
//						final Iterator<DataField> it2 = dataBlocks_Child.get(i).getDataFields().iterator();
//						while (it1.hasNext() && it2.hasNext()) {
//							it2.next().setData(it1.next().getData());
//						}
						// 2nd possibility (working, but...)
						for (final DataBlock db_Mother : dataBlocks_Mother) {
							if (db_Mother.getIndex() == i) {
								for (final DataBlock db_Child : dataBlocks_Child) {
									if (db_Child.getIndex() == i) {
										if (LOGGER.isDebugEnabled())
											LOGGER.debug("Will now copy data from DataBlock index " + db_Mother.getIndex() + " (mother) to DataBlock index " + db_Child.getIndex() + " (child)");
										final Iterator<DataField> it1 = db_Mother.getDataFields().iterator();
										final Iterator<DataField> it2 = db_Child.getDataFields().iterator();
										while (it1.hasNext() && it2.hasNext())
											it2.next().setData(it1.next().getData());
										continue OUTER;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Sorts the given list of DataBlocks according to the indices of each DataBlock and returns a new list containing the sorted DataBlocks.
	 * @param dataBlocks A list of DataBlocks to be sorted.
	 * @return a new list containing the sorted DataBlocks.
	 */
	private List<DataBlock> sortDataBlocks(final List<DataBlock> dataBlocks) {
		final List<DataBlock> dataBlocksSorted = new ArrayList<DataBlock>();
		OUTER: for (DataBlock db : dataBlocks) {
			final int idx = db.getIndex();
			if (idx >= dataBlocksSorted.size()) {
				for (int j = 0; j < dataBlocksSorted.size(); j++) {
					if (dataBlocksSorted.get(j).getIndex() > idx) {
						dataBlocksSorted.add(j, db);
						continue OUTER;
					}
				}
				dataBlocksSorted.add(db);
			}
			else
				dataBlocksSorted.add(idx, db);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Sorted DataBlock indices: ");
			for (DataBlock db : dataBlocksSorted)
				LOGGER.debug(db.getIndex());
		}
		// TODO This list is not sorted as the indices of certain DataBlocks change during the performance of this method. Why? At the moment a 2nd possibility is used (see PropertySetFieldInheritor#copyFieldValue).
		return dataBlocksSorted;
	}

	/**
	 * The mother has more DataBlocks than the child with respect to the currently considered DataBlockGroup. Therefore, add the missing amount of DataFields
	 * by cloning the appropriate mother DataFields and adding them to the PropertySet of the child. Note, a new DataBlockID has to be created as it is part
	 * of the primary key of the table JFireBase_Prop_PropertySet_dataFields. Otherwise an integrity constraint exception would occur. All other parts of the
	 * primary key of the clone will take over the appropriate values of the cloneable DataField (except the PropertySetID that is set to the ID of the PropertySet of the child).<p>
	 * Furthermore, a new DataBlock will be created and added to the considered DataBlockGroup of the child for each set of cloned DataFields that belong to a common DataBlock.
	 * The indices of the new DataBlocks are set according to the indices of the DataBlocks whose DataFields have been cloned. E.g., if the DataFields df_mother_2_1 and
	 * df_mother_2_2 of the DataBlock db_mother_2 have to be cloned, the cloned DataFields df_child_2_1 and df_child_2_2 will be placed in a common DataBlock db_child_2.
	 * @param structBlockKey The key of the currently considered StructBlock.
	 */
	private void addMissingDataFields(final String structBlockKey) {
		OUTER: for (final Map.Entry<StructFieldID, List<DataField>> entry : propSet_Mother.getDataFieldsMap().entrySet()) {
			final StructFieldID structFieldID = entry.getKey();
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Amount of data fields of StructFieldID " + structFieldID + " (mother): " + entry.getValue().size());
			if (!(structFieldID.structBlockOrganisationID + "/" + structFieldID.structBlockID).equals(structBlockKey))	// Test whether the considered StructField belongs to the currently considered StructBlock like e.g. dev.jfire.org/SimpleProductType.description (this test is necessary as we consider all persistent DataFields when we call PropertySet#getDataFieldsMap).
				continue OUTER;
			final List<DataField> dataFields_Mother = entry.getValue();
			final List<DataField> dataFields_Child = propSet_Child.getDataFieldsMap().get(structFieldID);
			if (dataFields_Child != null) {
				final int diffDataFields = dataFields_Mother.size() - dataFields_Child.size();
				for (int j = 0; j < diffDataFields; j++) {
					final DataField cloneable = dataFields_Mother.get(dataFields_Mother.size() - diffDataFields + j);
					if (!cloneableDataBlockIDToCloneDataBlockDescriptor.containsKey(cloneable.getDataBlockID())) {
//						do {
//							newBlockID = (int) (Integer.MAX_VALUE * Math.random());
//							if (tries++ > 10000)
//								throw new RuntimeException("Could not generate new dataBlockID in 10000 tries.");
//						} while (dataBlockMap.containsKey(newBlockID));
						final CloneDataBlockDescriptor cdbDesc = new CloneDataBlockDescriptor(new Long(System.currentTimeMillis()).intValue(), cloneable.getDataBlockIndex());	// TODO Generate a new DataBlockID by using the code above and testing whether it is not already available. Using the current method results in negative values for new DataBlocks.
						cloneableDataBlockIDToCloneDataBlockDescriptor.put(cloneable.getDataBlockID(), cdbDesc);
					}
					final DataField clone = cloneable.cloneDataField(propSet_Child, cloneableDataBlockIDToCloneDataBlockDescriptor.get(cloneable.getDataBlockID()).getCloneDataBlockID());	// Transfer new DataBlockID as parameter, i.e. adapt DataField constructor accordingly and create new clone method (adapt each DataField type). Done.
					clone.setDataBlockIndex(cloneable.getDataBlockIndex());	// Keep DataBlock index as set in mother!
					propSet_Child.internalAddDataFieldToPersistentCollection(clone);

					logDataFieldPrimaryKeyContent(clone, false);
					logDataFieldPrimaryKeyContent(cloneable, true);
				}
			}
		}
		for (final CloneDataBlockDescriptor cdbDesc : cloneableDataBlockIDToCloneDataBlockDescriptor.values()) {
			final DataBlock db = new DataBlock(structBlockKeyToDataBlockGroup_Child.get(structBlockKey), cdbDesc.getCloneDataBlockID());	// Create a new DataBlock that will be placed in the currently considered DataBlockGroup of the child and set its ID according to the DataBlockID of the appropriate clones.
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Index of new DataBlock (child): " + cdbDesc.getCloneDataBlockIndex());
			db.setIndex(cdbDesc.getCloneDataBlockIndex(), true);
			structBlockKeyToDataBlockGroup_Child.get(structBlockKey).addDataBlock(db);	// Add the new DataBlock to the list of DataBlocks of the currently considered DataBlockGroup of the child.
		}
		logDataBlocks_Child();
	}

	/**
	 * The mother has less DataBlocks than the child with respect to the currently considered DataBlockGroup. Therefore, delete all redundant child DataBlocks
	 * (and their data fields) by removing them from the group.
	 * @param dataBlocks_Child The complete list of DataBlocks of the child for the currently considered DataBlockGroup before removing redundant DataBlocks.
	 * @param diffDataBlocks The amount of DataBlocks to be removed (negative value).
	 */
	private void removeRedundantDataBlocks(final List<DataBlock> dataBlocks_Child, final int diffDataBlocks) {
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

	private void logDataFieldPrimaryKeyContent(final DataField dataField, final boolean isCloneable) {
		if (LOGGER.isDebugEnabled()) {
			if (isCloneable)
				LOGGER.debug("*********************** DataField PK (mother) ***********************");
			else
				LOGGER.debug("*********************** DataField PK (child) ***********************");
			LOGGER.debug("DataBlockID:    " + dataField.getDataBlockID());
			LOGGER.debug("OrganisationID: " + dataField.getOrganisationID());
			LOGGER.debug("PropertySetID:  " + dataField.getPropertySetID());
			LOGGER.debug("StructBlockKey: " + dataField.getStructBlockKey());
			LOGGER.debug("StructFieldPK:  " + dataField.getStructFieldPK());
			if (isCloneable)
				LOGGER.debug("*********************** DataField PK (mother) ***********************");
			else
				LOGGER.debug("*********************** DataField PK (child) ***********************");
		}
	}

	private void logDataBlocks_Child() {
		if (LOGGER.isDebugEnabled()) {
			final Iterator<DataBlockGroup> it = propSet_Child.getDataBlockGroups().iterator();
			LOGGER.debug("*********************** DataBlocks (child) ***********************");
			while (it.hasNext()) {
				final List<DataBlock> dataBlocks = it.next().getDataBlocks();
				for (DataBlock db : dataBlocks) {
					LOGGER.debug("DataBlockID:     " + db.getDataBlockID());
					LOGGER.debug("DataBlock index: " + db.getIndex());
				}
			}
			LOGGER.debug("*********************** DataBlocks (child) ***********************");
		}
	}
}
