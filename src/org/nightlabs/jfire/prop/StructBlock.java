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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.exception.IllegalStructureModificationException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.i18n.StructBlockName;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.DataBlockValidator;
import org.nightlabs.jfire.prop.validation.IDataBlockValidator;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.NLLocale;

/**
 * {@link StructBlock} are the top level of the two-level structure of a {@link PropertySet}.
 * They are basically a container for {@link StructField}s.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 * 	identity-type="application"
 * 	objectid-class="org.nightlabs.jfire.prop.id.StructBlockID"
 * 	detachable="true"
 * 	table="JFireBase_Prop_StructBlock"
 *
 * @jdo.inheritance  strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="structBlockOrganisationID, structBlockID"
 * 		include-body="id/StructBlockID.body.inc"
 *
 * @jdo.fetch-group
 * 		name="IStruct.fullData"
 * 		fetch-groups="default"
 * 		fields="blockName, struct, structFields, dataBlockValidators"
 */
@PersistenceCapable(
	objectIdClass=StructBlockID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructBlock")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="blockName"), @Persistent(name="struct"), @Persistent(name="structFields"), @Persistent(name="dataBlockValidators")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructBlock implements Serializable
{
	/**
	 * The serial version of this class.
	 */
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
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		dependent="true"
	 *		mapped-by="block"
	 */
	@Persistent(
		dependent="true",
		mappedBy="block",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected StructBlockName blockName;
	
	/** 
	 * How many columns should the display of a DataBlock of this StructBlock have.
	 * TODO: Later we might replace this by a complex entity.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int displayFieldColumnCount;

	/**
	 * This constructor is for JDO only.
	 */
	protected StructBlock()
	{
		this.blockName = new StructBlockName(this);
		this.structFields = new LinkedList<StructField<? extends DataField>>();
	}

	/**
	 * Create a new {@link StructBlock} for the given structure and with the given structBlockID.
	 * The organisationID of the new {@link StructBlock} will be the one of the given {@link IStruct}.
	 *
	 * @param struct The {@link IStruct} the new {@link StructBlock} should be contained in.
	 * @param structBlockID The structBlockID for the new {@link StructBlock}.
	 */
	public StructBlock(IStruct struct, StructBlockID structBlockID)
	{
		this(struct, structBlockID.structBlockOrganisationID, structBlockID.structBlockID);
	}

	/**
	 * Create a new {@link StructBlock} for the given {@link IStruct} using the given
	 * primary key fields.
	 *
	 * @param struct The {@link IStruct} the new {@link StructBlock} should be contained in.
	 * @param _structBlockOrganisationID The organisationID for the new {@link StructBlock}.
	 * @param _structBlockID The structBlockID for the new {@link StructBlock}.
	 */
	public StructBlock(IStruct struct, String _structBlockOrganisationID, String _structBlockID)
	{
		this.struct = struct;
		this.structBlockOrganisationID = _structBlockOrganisationID;
		this.structBlockID = _structBlockID;
		this.primaryKey = getPrimaryKey(structBlockOrganisationID, structBlockID);
		this.blockName = new StructBlockName(this);
		this.structFields = new LinkedList<StructField<? extends DataField>>();
		this.dataBlockValidators = new LinkedList<DataBlockValidator>();
		this.displayFieldColumnCount = 1;
	}

	/**
	 * @return  Returns the structBlockOrganisationID.
	 */
	public String getStructBlockOrganisationID()
	{
		return structBlockOrganisationID;
	}

	/**
	 * @return  Returns the structBlockID.
	 */
	public String getStructBlockID()
	{
		return structBlockID;
	}

	/**
	 * Returns the {@link StructBlockID} (id-object) of this {@link StructBlock}.
	 * @return The {@link StructBlockID} (id-object) of this {@link StructBlock}.
	 */
	public StructBlockID getStructBlockIDObj()
	{
		return StructBlockID.create(structBlockOrganisationID, structBlockID);
	}

	/**
	 * Returns a '/' separated String of the given primary key fields.
	 * @param _structBlockOrganisationID The organisationID to use.
	 * @param _structBlockID The structBlockID to use.
	 * @return A '/' separated String of the given primary key fields.
	 */
	public static String getPrimaryKey(String _structBlockOrganisationID, String _structBlockID)
	{
		return _structBlockOrganisationID + "/" + _structBlockID;
	}

	/**
	 * @return  Returns the primaryKey.
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * @param structBlockID  The structBlockOrganisationID to set.
	 */
	public void setOrganisationID(String structBlockOrganisationID)
	{
		if (JDOHelper.getObjectId(this) != null && this.structBlockOrganisationID != null)
			throw new IllegalStateException(
					"structBlockOrganisationID structImmutable. Can not change for a persistent object it after it has been defined!");

		this.structBlockOrganisationID = structBlockOrganisationID;
	}

	/**
	 * @param structBlockID  The structBlockID to set.
	 */
	public void setID(String structBlockID)
	{
		if (JDOHelper.getObjectId(this) != null && this.structBlockID != null)
			throw new IllegalStateException("Cannot change structBlockID for a persistent object after it has been defined!");

		this.structBlockID = structBlockID;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	 private IStruct struct;

	/**
	 * Retuns the Struct this StructBlock belongs to.
	 * @return The Struct this StructBlock belongs to.
	 */
	public IStruct getStruct()
	{
		return struct;
	}

	/**
	 * Sets the Struct of this StructBlock.
	 * @param struct The IStruct to set.
	 */
	void setStruct(IStruct struct) {
		this.struct = struct;
	}

	/**
	 * Map of struct fields for this block. Holds subclasses of {@link StructField}.
	 *
	 * key: String
	 * StructField.getStructFieldKey(structFieldOrganisatinID, structFieldID)
	 * value: StructField structField
	 *
	 * @jdo.field	persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected transient Map<String, StructField<? extends DataField>> structFieldMap;

	/**
	 * Returns the {@link #structFieldMap} containing all structFields of this block.
	 * @return the {@link #structFieldMap} containing all structFields of this block.
	 */
	public Map<String, StructField<? extends DataField>> getStructFieldMap() {
		if (structFieldMap == null)
			initialiseStructFieldMap();

		return structFieldMap;
	}

	/**
	 * Fills the {@link #structFieldMap} with the (prkey, field) pairs from the
	 * {@link #structFields}.
	 */
	private void initialiseStructFieldMap() {
		if (structFieldMap != null)
			return;

		// Bugfix by Marco: The old implemetation was not thread-safe. This one is (in the worst case, it creates multiple maps).
		Map<String, StructField<? extends DataField>> m = new HashMap<String, StructField<? extends DataField>>(structFields.size());
//		if (structFields.isEmpty()) {
//			structFieldMap = new HashMap<String, StructField<? extends DataField>>();
//			return;
//		}	else {
//			structFieldMap = new HashMap<String, StructField<? extends DataField>>(structFields.size());
//		}

		for (StructField<? extends DataField> field : structFields) {
			m.put(field.getStructFieldKey(), field);
		}
		structFieldMap = m;
	}

	/**
	 * List of all Struct Fields.
	 *
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 * 	collection-type="collection"
	 * 	element-type="org.nightlabs.jfire.prop.StructField"
	 *  dependent-element="true"
	 *  mapped-by="structBlock"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="structBlock",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected LinkedList<StructField<? extends DataField>> structFields;

	/**
	 * Returns the <b>ordered</b> {@link StructField}s.
	 * The order is determined by the {@link StructBlockOrderItem} set to this instance of StructBlock.
	 *
	 * @return  Returns the structFields.
	 */
	public List<StructField<? extends DataField>> getStructFields()
	{
		// Can't sort an unmodifiable list :-(
		// can't return backed list.
		ArrayList<StructField<? extends DataField>> fields = new ArrayList<StructField<? extends DataField>>(structFields);
		Collections.sort(fields, getStructFieldComparator());
		return fields;
	}

	/**
	 * A shortcut to {@link #getStructField(String, String)}
	 *
	 * @throws StructFieldNotFoundException
	 */
	public StructField<? extends DataField> getStructField(StructFieldID structFieldID)
			throws StructFieldNotFoundException
	{
		return getStructField(structFieldID.structFieldOrganisationID, structFieldID.structFieldID);
	}

	/**
	 * Looks for the {@link StructField} with the given primary key and returns it when found.
	 * If not found a {@link StructFieldNotFoundException} is thrown.
	 *
	 * @param structFieldOrganisationID The organisationID of the {@link StructField} to search.
	 * @param structFieldID The structFieldID of the {@link StructField} to search.
	 * @return The {@link StructField} with the given primary key fields inside this block.
	 * @throws StructFieldNotFoundException If the {@link StructField} could not be found.
	 */
	public StructField<? extends DataField> getStructField(String structFieldOrganisationID, String structFieldID)
			throws StructFieldNotFoundException
	{
		if (structFieldMap == null)
			initialiseStructFieldMap();

		String structFieldKey = StructField.getStructFieldKey(structFieldOrganisationID, structFieldID);
		StructField<? extends DataField> psf = structFieldMap.get(structFieldKey);
		if (psf == null)
		{
			throw new StructFieldNotFoundException("No PropStructField with ID \"" + structFieldKey
					+ "\" existent in StructBlock \"" + getStructBlockID() + "\"!");
		}
		return psf;
	}

	/**
	 * Adds the given {@link StructField} to this {@link StructBlock}.
	 * @param structField The {@link StructField} to add.
	 * @throws DuplicateKeyException If a StructField with the same primary key as the given one already exists in this {@link DataBlock}.
	 */
	public void addStructField(StructField<? extends DataField> structField) throws DuplicateKeyException
	{
		if (structField == null)
			throw new IllegalStateException("Parameter structField must not be null!");

		if (structFieldMap == null)
			initialiseStructFieldMap();

		String structFieldKey = structField.getStructFieldKey();
		if (structFieldMap.containsKey(structFieldKey))
			throw new DuplicateKeyException("StructField with key " + structFieldKey
					+ " is already part of this StructBlock");

		structFieldMap.put(structFieldKey, structField);
		structFields.add(structField);

		// if orderItem is not set, the order will be inconsestent
		if (orderItem != null) {
			orderItem.addStructFieldOrderItem(
					new StructFieldOrderItem(orderItem, IDGenerator.nextID(StructFieldOrderItem.class), structField));
		}
	}

	/**
	 * Removes the given {@link StructField} from this {@link StructBlock} only when it was not yet persisted.
	 * @param field The {@link StructField} to remove.
	 * @throws IllegalStructureModificationException If the field to remove was already persisted.
	 */
	public void removeStructField(StructField<? extends DataField> field) throws IllegalStructureModificationException
	{
		if (structFieldMap == null)
			initialiseStructFieldMap();

//		if (JDOHelper.isNew(field) || JDOHelper.getObjectId(field) == null) {
//			structFieldMap.remove(field.getStructFieldKey());
//			structFields.remove(field);
//		} else
//			throw new IllegalStructureModificationException("Cannot remove persistent StructField.");
		// I think, there's no need to disallow it. Hence I commented out the above and copied the remove operations. Marco.
		structFieldMap.remove(field.getStructFieldKey());
		structFields.remove(field);

		if (orderItem != null) {
			orderItem.removeStructFieldOrderItem(orderItem.getStructFieldOrderItem(field.getStructFieldIDObj()));
		}

	}

	/**
	 * Returns the i18n name of this {@link StructBlock}.
	 * @return The i18n name of this {@link StructBlock}.
	 */
	public StructBlockName getName() {
		return blockName;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected boolean unique = false;

	// Unique default value is false
	/**
	 * @param unique  The unique to set.
	 */
	public void setUnique(boolean unique)
	{
		this.unique = unique;
	}

	/**
	 * If the unique property is <code>true</code> only one instance
	 * of the {@link DataBlock} will be allowed for the {@link DataBlockGroup}
	 * it is in.
	 *
	 * @return  Returns the unique.
	 */
	public boolean isUnique()
	{
		return unique;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private StructBlockOrderItem orderItem = null;

	/**
	 * Set the {@link StructBlockOrderItem} of this block
	 * within a special {@link StructLocal}.
	 * <p>
	 * The item is used to order the fields of this block
	 * and will be set when a StructLocal is detached with
	 * the fetch-group {@link IStruct#FETCH_GROUP_ISTRUCT_FULL_DATA}.
	 * </p>
	 * @param orderItem The orderItem to set.
	 */
	protected void setStructBlockOrderItem(StructBlockOrderItem orderItem) {
		this.orderItem = orderItem;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (structFields == null || structFields.size() == 0)
			return "{ }";

		String toReturn = "";
		for (StructField<? extends DataField> field : structFields)
			toReturn += field.toString() + ", ";

		if (blockName == null)
			blockName = new StructBlockName(this);

		return blockName.getText(NLLocale.getDefault().getLanguage()) + ": { " + toReturn.substring(0, toReturn.length()-2) + " }";
	}

	/**
	 * Returns the {@link Comparator} for {@link StructField}s
	 * of this {@link StructBlock}.
	 * @return The {@link Comparator} for {@link StructField}s
	 *         of this {@link StructBlock}.
	 */
	protected Comparator<StructField<?>> getStructFieldComparator() {
		if (this.orderItem != null)
			return this.orderItem;
		return new Comparator<StructField<?>>() {
			@Override
			public int compare(StructField<?> arg0, StructField<?> arg1) {
				return 0;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((structBlockID == null) ? 0 : structBlockID.hashCode());
		result = PRIME * result + ((structBlockOrganisationID == null) ? 0 : structBlockOrganisationID.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof StructBlock))
			return false;
		final StructBlock other = (StructBlock) obj;
		if (structBlockID == null)
		{
			if (other.structBlockID != null)
				return false;
		}
		else if (!structBlockID.equals(other.structBlockID))
			return false;
		if (structBlockOrganisationID == null)
		{
			if (other.structBlockOrganisationID != null)
				return false;
		}
		else if (!structBlockOrganisationID.equals(other.structBlockOrganisationID))
			return false;
		return true;
	}

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		dependent-element="true"
	 *		mapped-by="structBlock"
	 *		element-type="org.nightlabs.jfire.prop.validation.DataBlockValidator"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="structBlock",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DataBlockValidator> dataBlockValidators;

	/**
	 * Adds an implementation of IDataBlockValidator to the StructBlock,
	 * which will be used to validate it.
	 * @param validator the IDataBlockValidator to add.
	 */
	public void addDataBlockValidator(IDataBlockValidator validator) {
		dataBlockValidators.add((DataBlockValidator) validator);
	}

	/**
	 * Removes a previously added IDataBlockValidator from the StructBlock.
	 * @param validator the IDataBlockValidator to remove
	 */
	public void removeDataBlockValidator(IDataBlockValidator validator) {
		dataBlockValidators.remove(validator);
	}

	/**
	 * Returns the list of previously added IDataBlockValidator.
	 * @return the List of {@link IDataBlockValidator}s.
	 */
	public List<IDataBlockValidator> getDataBlockValidators() {
		return CollectionUtil.castList(Collections.unmodifiableList(dataBlockValidators));
	}

	/**
	 * Returns a boolean indicating whether this {@link StructBlock} is defined in a {@link StructLocal}.
	 * @return a boolean indicating whether this {@link StructBlock} is defined in a {@link StructLocal}.
	 */
	public boolean isLocal() {
		IStruct struct = getStruct();
		if (struct instanceof Struct)
			return false;
		else if (struct instanceof StructLocal)
			return true;
		else
			throw new IllegalStateException("StructBlock is neither defined in Struct nor in StructLocal.");
	}

	/**
	 * How many columns should the display of a DataBlock of this StructBlock
	 * have. I.e. how many DataFields should be displayed next to each other.
	 * 
	 * @return The number of columns a display of this StructBlock should have.
	 */
	public int getDisplayFieldColumnCount() {
		return displayFieldColumnCount;
	}
	
	/**
	 * @param displayFieldColumnCount How many columns should the display of a DataBlock of this StructBlock have.
	 */
	public void setDisplayFieldColumnCount(int displayFieldColumnCount) {
		this.displayFieldColumnCount = displayFieldColumnCount;
	}
}