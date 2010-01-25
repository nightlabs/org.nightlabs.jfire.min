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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.IStruct.OrderMoveDirection;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.exception.StructFieldValueNotFoundException;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * {@link StructField} that represents a {@link DataField} that references a
 * selection out of a given set of possible values. The values {@link StructFieldValue}
 * are stored in a list in {@link SelectionStructField} {@link #getStructFieldValues()}.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 *
 * @jdo.persistence-capable
 *  identity-type="application"
 *  persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *  detachable="true"
 *  table="JFireBase_Prop_SelectionStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default" fields="structFieldValues, defaultValue"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_SelectionStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="structFieldValues"), @Persistent(name="defaultValue")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class SelectionStructField extends StructField<SelectionDataField>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field
	 *  	persistence-modifier="persistent"
	 *  	collection-type="collection"
	 *  	element-type="org.nightlabs.jfire.prop.structfield.StructFieldValue"
	 *  	mapped-by="structField"
	 *  	dependent-element="true"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="structField",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected List<StructFieldValue> structFieldValues;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Map<String, StructFieldValue> structFieldValuesMap;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructFieldValue defaultValue;
	
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean allowsEmptySelection = false;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SelectionStructField() { }

	public SelectionStructField(StructBlock _structBlock) {
		super(_structBlock);
		this.structFieldValues = new ArrayList<StructFieldValue>();
	}

	public SelectionStructField(StructBlock _structBlock, StructFieldID _structFieldID) {
		super(_structBlock, _structFieldID);
		this.structFieldValues = new ArrayList<StructFieldValue>();
	}

	/**
	 * @return Returns the structFieldValues.
	 */
	public List<StructFieldValue> getStructFieldValues() {
		return Collections.unmodifiableList(structFieldValues);
	}
	
	protected Map<String, StructFieldValue> getStructFieldValuesMap() {
		if (structFieldValuesMap == null) {
			structFieldValuesMap = new HashMap<String, StructFieldValue>();
			for (StructFieldValue value : structFieldValues) {
				structFieldValuesMap.put(value.getStructFieldValueID(), value);
			}
		}
		return structFieldValuesMap;
	}

	/**
	 * Returns the {@link StructFieldValue} with the given
	 * structFieldValueID. If no entry is found for this id a
	 * {@link StructFieldValueNotFoundException} is thrown.
	 *
	 * @param structFieldValueID
	 * @return
	 * @throws StructFieldValueNotFoundException
	 */
	public StructFieldValue getStructFieldValue(String structFieldValueID) throws StructFieldValueNotFoundException {
		StructFieldValue psfv = getStructFieldValuesMap().get(structFieldValueID);
		if (psfv == null) {
			throw new StructFieldValueNotFoundException("No StructFieldValue with ID \"" + structFieldValueID
					+ "\" existent in PropStructField \"" + getStructFieldID() + "\" in StructBlock \"" + getStructBlockID()
					+ "\"!");
		}
		return psfv;
	}

	/**
	 * Creates a new {@link StructFieldValue}, registers it in the Map and
	 * returns it for further modifying.
	 */
	public StructFieldValue newStructFieldValue() {
		notifyModifyListeners();
		StructFieldValue psfv = new StructFieldValue(this);
		structFieldValues.add(psfv);
		structFieldValuesMap = null;
		return psfv;
	}

	/**
	 * If you don't need to manually specify a <code>structFieldValueID</code>, you can
	 * use {@link #newStructFieldValue()} instead.
	 *
	 * @param structFieldValueID a local String ID within the scope of this <code>SelectionStructField</code>'s primary key fields.
	 */
	public StructFieldValue newStructFieldValue(String structFieldValueID) {
		notifyModifyListeners();
		StructFieldValue psfv = new StructFieldValue(this, structFieldValueID);
		structFieldValues.add(psfv);
		structFieldValuesMap = null;
		return psfv;
	}
	
		/**
	 * Removes the specified {@link StructFieldValue} from the this {@link SelectionStructField}s field values.
	 * @param value The {@link StructFieldValue} to be removed.
	 */
	public void removeStructFieldValue(StructFieldValue value) {
		structFieldValues.remove(value);
		structFieldValuesMap = null;
	}

	/**
	 * Moves the given StructFieldValue in the given direction within the list of values.
	 * @param value The value to move.
	 * @param orderMoveDirection The direction the value should be moved.
	 */
	public void moveStructFieldValue(StructFieldValue value, OrderMoveDirection orderMoveDirection) {
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
		StructFieldValue swapVal = structFieldValues.get(swapIdx);
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
	protected SelectionDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		SelectionDataField selectionDataField = new SelectionDataField(dataBlock, this);
		if (getDefaultValue() != null) {
			selectionDataField.setSelection(getDefaultValue());
		} else if (!allowsEmptySelection) {
			selectionDataField.setSelection(getStructFieldValues().get(0));
		} else {
			selectionDataField.setSelection(null);
		}
		
		return selectionDataField;
	}

	@Override
	public Class<SelectionDataField> getDataFieldClass() {
		return SelectionDataField.class;
	}

	/**
	 * Sets the default value for this instance.
	 * @param defaultValue The default value to set or <code>null</code> to remove an existing default value.
	 */
	public void setDefaultValue(StructFieldValue defaultValue) {
		if (defaultValue != null && !getStructFieldValues().contains(defaultValue))
			throw new IllegalArgumentException("Default value must be contained in the struct field values.");

		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the default value of this field.
	 * @return the default value of this field.
	 */
	public StructFieldValue getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Returns a boolean indicating whether empty selections should be allowed or not.
	 * @return a boolean indicating whether empty selections should be allowed or not.
	 */
	public boolean allowsEmptySelection() {
		return allowsEmptySelection;
	}
	
	/**
	 * Sets whether this {@link SelectionStructField} allows an empty selection to be set for {@link SelectionDataField}s.
	 * @param allowsEmptySelection A boolean indicating whether empty selections should be allowed or not.
	 */
	public void setAllowsEmptySelection(boolean allowsEmptySelection) {
		this.allowsEmptySelection = allowsEmptySelection;
	}
}
