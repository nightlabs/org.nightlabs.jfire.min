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

package org.nightlabs.jfire.prop.structfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.IStruct.OrderMoveDirection;
import org.nightlabs.jfire.prop.datafield.MultiSelectionDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * {@link StructField} that represents a {@link DataField} that references a
 * selection out of a given set of possible values. The values {@link MultiSelectionStructFieldValue}
 * are stored in a list in {@link MultiSelectionStructField} {@link #getStructFieldValues()}.
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de> (Original SelectionStructField code)
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de --> (Original SelectionStructField code)
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_MultiSelectionStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name=IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA,
		members={@Persistent(name="structFieldValues"), @Persistent(name="defaultValues")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class MultiSelectionStructField extends StructField<MultiSelectionDataField>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	@Persistent(
		dependentElement="true",
		mappedBy="structField")
	protected List<MultiSelectionStructFieldValue> structFieldValues;
	
	@Persistent
	private Set<MultiSelectionStructFieldValue> defaultValues;
	
	@Persistent
	private int minimumSelectionCount = 0;

//	@Persistent(persistenceModifier=PersistenceModifier.NONE)
//	private transient Map<String, MultiSelectionStructFieldValue> structFieldValuesMap;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected MultiSelectionStructField()
	{
	}

	public MultiSelectionStructField(StructBlock _structBlock) 
	{
		super(_structBlock);
		this.structFieldValues = new ArrayList<MultiSelectionStructFieldValue>();
	}

	public MultiSelectionStructField(StructBlock _structBlock, StructFieldID _structFieldID) 
	{
		super(_structBlock, _structFieldID);
		this.structFieldValues = new ArrayList<MultiSelectionStructFieldValue>();
	}

	/**
	 * @return Returns the structFieldValues.
	 */
	public List<MultiSelectionStructFieldValue> getStructFieldValues() 
	{
		return Collections.unmodifiableList(structFieldValues);
	}
	
//	protected Map<String, MultiSelectionStructFieldValue> getStructFieldValuesMap() {
//		if (structFieldValuesMap == null) {
//			structFieldValuesMap = new HashMap<String, MultiSelectionStructFieldValue>();
//			for (MultiSelectionStructFieldValue value : structFieldValues) {
//				structFieldValuesMap.put(value.getStructFieldValueID(), value);
//			}
//		}
//		return structFieldValuesMap;
//	}

//	/**
//	 * Returns the {@link StructFieldValue} with the given
//	 * structFieldValueID. If no entry is found for this id a
//	 * {@link StructFieldValueNotFoundException} is thrown.
//	 */
//	public MultiSelectionStructFieldValue getStructFieldValue(String structFieldValueID) throws StructFieldValueNotFoundException {
//		MultiSelectionStructFieldValue psfv = getStructFieldValuesMap().get(structFieldValueID);
//		if (psfv == null) {
//			throw new StructFieldValueNotFoundException("No StructFieldValue with ID \"" + structFieldValueID
//					+ "\" existent in PropStructField \"" + getStructFieldID() + "\" in StructBlock \"" + getStructBlockID()
//					+ "\"!");
//		}
//		return psfv;
//	}

	/**
	 * Creates a new {@link StructFieldValue}, registers it in the Map and
	 * returns it for further modifying.
	 */
	public MultiSelectionStructFieldValue newStructFieldValue() {
		notifyModifyListeners();
		MultiSelectionStructFieldValue psfv = new MultiSelectionStructFieldValue(this);
		structFieldValues.add(psfv);
//		structFieldValuesMap = null;
		return psfv;
	}

	/**
	 * If you don't need to manually specify a <code>structFieldValueID</code>, you can
	 * use {@link #newStructFieldValue()} instead.
	 *
	 * @param structFieldValueID a local String ID within the scope of this <code>SelectionStructField</code>'s primary key fields.
	 */
	public MultiSelectionStructFieldValue newStructFieldValue(String structFieldValueID) {
		notifyModifyListeners();
		MultiSelectionStructFieldValue psfv = new MultiSelectionStructFieldValue(this, structFieldValueID);
		structFieldValues.add(psfv);
//		structFieldValuesMap = null;
		return psfv;
	}
	
	/**
	 * Removes the specified {@link StructFieldValue} from the this {@link MultiSelectionStructField}s field values.
	 * @param value The {@link StructFieldValue} to be removed.
	 */
	public void removeStructFieldValues(Collection<MultiSelectionStructFieldValue> values) {
		structFieldValues.removeAll(values);
		if(defaultValues != null)
			defaultValues.removeAll(values);
//		structFieldValuesMap = null;
	}

	/**
	 * Moves the given StructFieldValue in the given direction within the list of values.
	 * @param value The value to move.
	 * @param orderMoveDirection The direction the value should be moved.
	 */
	public void moveStructFieldValue(MultiSelectionStructFieldValue value, OrderMoveDirection orderMoveDirection) {
		int orgIdx = -1;
		for (int i = 0; i < structFieldValues.size(); i++) {
			if (structFieldValues.get(i).equals(value)) {
				orgIdx = i;
				break;
			}
		}
		if (orgIdx < 0)
			return; // not found.
		int swapIdx = orgIdx;
		switch (orderMoveDirection) {
		case up:
			if (orgIdx == 0) {
				// can't move up, already first
				return;
			}
			swapIdx = orgIdx -1;
			break;

		case down:
			if (orgIdx == structFieldValues.size() -1) {
				// can't move down, already last
				return;
			}
			swapIdx = orgIdx + 1;
			break;
		}
		MultiSelectionStructFieldValue swapVal = structFieldValues.get(swapIdx);
		structFieldValues.set(swapIdx, value);
		structFieldValues.set(orgIdx, swapVal);
//		Collections.swap(structFieldValues, orgIdx, swapIdx);
	}
	
	/**
	 * Adds an instance of {@link SelectionDataField}
	 *
	 * @see org.nightlabs.jfire.prop.StructField#getRepresentationDataClass()
	 */
	@Override
	protected MultiSelectionDataField createDataFieldInstanceInternal(DataBlock dataBlock) 
	{
		MultiSelectionDataField selectionDataField = new MultiSelectionDataField(dataBlock, this);
		if (getDefaultValues() != null) {
			selectionDataField.setSelection(getDefaultValues());
		} else if (minimumSelectionCount > 0) {
			selectionDataField.setSelection(getStructFieldValues().subList(0, Math.min(minimumSelectionCount-1, getStructFieldValues().size()-1)));
		} else {
			selectionDataField.setSelection((Collection<MultiSelectionStructFieldValue>)null);
		}
		
		return selectionDataField;
	}

	@Override
	public Class<MultiSelectionDataField> getDataFieldClass() {
		return MultiSelectionDataField.class;
	}

	/**
	 * Sets the default values for this instance.
	 * @param defaultValue The default values to set or <code>null</code> to remove existing default value.s
	 */
	public void setDefaultValues(Collection<MultiSelectionStructFieldValue> defaultValues) 
	{
		if (defaultValues == null || defaultValues.isEmpty()) {
			this.defaultValues = null;
			return;
		}
		if (!getStructFieldValues().containsAll(defaultValues))
			throw new IllegalArgumentException("Default values must be contained in the struct field values.");
		this.defaultValues = new HashSet<MultiSelectionStructFieldValue>(defaultValues);
	}

	/**
	 * Returns the default values of this field.
	 * @return the default values of this field.
	 */
	public Set<MultiSelectionStructFieldValue> getDefaultValues()
	{
		return defaultValues == null ? null : Collections.unmodifiableSet(defaultValues);
	}

	public int getMinimumSelectionCount() 
	{
		return minimumSelectionCount;
	}
	
	public void setMinimumSelectionCount(int minimumSelectionCount) 
	{
		this.minimumSelectionCount = minimumSelectionCount;
	}
}
