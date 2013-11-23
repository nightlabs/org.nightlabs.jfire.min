package org.nightlabs.jfire.prop.datafield;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;

/**
 * {@link DataField} that stores a {@link Boolean} value.
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_BooleanDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySet.FETCH_GROUP_FULL_DATA,
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class BooleanDataField extends DataField 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Boolean value = null;
	
	/**
	 * @deprecated For JDO only.
	 */
	@Deprecated
	public BooleanDataField() {
	}

	/**
	 * @param dataBlock
	 * @param structField
	 */
	public BooleanDataField(DataBlock dataBlock,
			StructField<? extends DataField> structField) {
		super(dataBlock, structField);
	}

	/**
	 * @param organisationID
	 * @param propertySetID
	 * @param cloneField
	 */
	public BooleanDataField(String organisationID, long propertySetID,
			int dataBlockID, DataField cloneField) {
		super(organisationID, propertySetID, dataBlockID, cloneField);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet, int dataBlockID) {
		BooleanDataField newField = new BooleanDataField(
				propertySet.getOrganisationID(),
				propertySet.getPropertySetID(),
				dataBlockID,
				this
			);
		newField.setValue(getValue());
		return newField;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return value == null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IDataField#getData()
	 */
	@Override
	public Object getData() {
		return getValue();
	}

	public void setValue(Boolean value) {
		this.value = value;
	}
	
	public Boolean getValue() {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IDataField#setData(java.lang.Object)
	 */
	@Override
	public void setData(Object data) {
		if (data == null) {
			setValue(null);
		} else if (data instanceof Boolean) {
			setValue((Boolean) data);
		} else if (data instanceof BooleanDataField) {
			setValue(((BooleanDataField) data).getValue());
		} else if (data instanceof String) {
			setValue(Boolean.valueOf((String)data));
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + " does not support input data of type " + data.getClass());
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IDataField#supportsInputType(java.lang.Class)
	 */
	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return 
			Boolean.class.isAssignableFrom(inputType) ||
			BooleanDataField.class.isAssignableFrom(inputType) ||
			String.class.isAssignableFrom(inputType);
	}
}
