package org.nightlabs.jfire.prop.structfield;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.BooleanDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * {@link StructField} that represents a {@link DataField} that stores a {@link Boolean}.
 * The corresponding {@link DataField} is {@link BooleanDataField}.
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_BooleanStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class BooleanStructField 
extends StructField<BooleanDataField> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The default value for initialization, by default <code>false</code>
	 */
	private Boolean defaultValue = false;
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	public BooleanStructField() {
	}

	/**
	 * @param structBlock
	 * @param structFieldID
	 */
	public BooleanStructField(StructBlock structBlock,
			StructFieldID structFieldID) {
		super(structBlock, structFieldID);
	}

	/**
	 * @param structBlock
	 */
	public BooleanStructField(StructBlock structBlock) {
		super(structBlock);
	}

	/**
	 * @param structBlock
	 * @param structFieldOrganisationID
	 * @param structFieldID
	 */
	public BooleanStructField(StructBlock structBlock,
			String structFieldOrganisationID, String structFieldID) {
		super(structBlock, structFieldOrganisationID, structFieldID);
	}

	/**
	 * @see org.nightlabs.jfire.prop.StructField#createDataFieldInstanceInternal(org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	protected BooleanDataField createDataFieldInstanceInternal(
			DataBlock dataBlock) 
	{
		BooleanDataField booleanDataField = new BooleanDataField(dataBlock, this);
		booleanDataField.setValue(getDefaultValue());
		return booleanDataField;
	}

	/**
	 * @see org.nightlabs.jfire.prop.StructField#getDataFieldClass()
	 */
	@Override
	public Class<BooleanDataField> getDataFieldClass() {
		return BooleanDataField.class;
	}

	/**
	 * Sets the default value for new {@link BooleanDataField} created by this struct field.
	 * @param value the default value to set
	 */
	public void setDefaultValue(Boolean value) {
		defaultValue = value;
	}
	
	/**
	 * Returns the default value for new {@link BooleanDataField} created by this struct field.
	 * @return the default value
	 */
	public Boolean getDefaultValue() {
		return defaultValue;
	}
}
