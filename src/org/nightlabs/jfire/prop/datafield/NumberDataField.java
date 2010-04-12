package org.nightlabs.jfire.prop.datafield;

import java.math.BigDecimal;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.i18n.StaticI18nText;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;

/**
 * {@link DataField} that can store a number in integer or decimal format.
 * It therefore stores an integer value and the number of digits (precision) this
 * value has.
 *
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *                          detachable="true"
 *                          table="JFireBase_Prop_NumberDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="intValue, digits"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_NumberDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members={@Persistent(name="intValue"), @Persistent(name="digits")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class NumberDataField
extends DataField
implements II18nTextDataField
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20090116L;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	// TODO create and use longValue instead
	private int intValue;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int digits;

	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean empty = true;

	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient StaticI18nText textBuffer = null;

	/**
	 * Create a new {@link NumberDataField} for the given {@link DataBlock}
	 * that represents the given {@link StructField}.
	 *
	 * @param dataBlock The {@link DataBlock} the new {@link ImageDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link ImageDataField} represents in the data structure.
	 * @param digits The number of digits the new {@link NumberDataField} should have.
	 */
	public NumberDataField(DataBlock dataBlock, StructField<NumberDataField> structField, int digits) {
		super (dataBlock, structField);
		this.digits = digits;
	}

	/**
	 * Used for cloning.
	 */
	protected NumberDataField(String organisationID, long propertySetID, int dataBlockID, DataField cloneField) {
		super(organisationID, propertySetID, dataBlockID, cloneField);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet, int)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet, int dataBlockID) {
		NumberDataField newField = new NumberDataField(propertySet.getOrganisationID(), propertySet.getPropertySetID(), dataBlockID, this);
		newField.intValue = intValue;
		newField.digits = digits;
		newField.empty = empty;
		return newField;
	}

	/**
	 * Returns the integer value of this field. Integer is meant here as opposite to decimal.
	 * This value only reflects the actual value of the field if digits is set to null.
	 * Otherwise, the actual value can be acquired by dividing the returned
	 * value by 10^digits or by calling {@link #getDoubleValue()}.
	 *
	 * @return The integer value of this field.
	 * @deprecated Create and use getLongValue() instead.
	 */
	@Deprecated
	public int getIntValue() {
		return intValue;
	}

	/**
	 * Returns the number of digits the value of this {@link NumberDataField} has.
	 * This can be used to compute the actual value of the field by dividing the integer value of
	 * the field ({@link #getIntValue()}) by 10^this value.
	 *
	 * @return The number of digits of the value of this {@link NumberDataField}.
	 */
	public int getDigits() {
		return digits;
	}

	/**
	 * Returns the decimal value of this {@link NumberDataField},
	 * it is computed using the integer value of the field and its number of digits:
	 * intValue/10^digits.
	 *
	 * @return The decimal value of this {@link NumberDataField},
	 */
	public double getDoubleValue() {
		return intValue/Math.pow(10, digits);
	}

	private String getValueAsString() {
		if (isInteger())
			return Long.toString(getIntValue());
		else
			return Double.toString(getDoubleValue());
	}

	/**
	 * Set the integer value of this {@link NumberDataField}.
	 * Note, that this only is the actual value of the field if the number of digits is 0.
	 * Otherwise the value will be intValue/10^digits.
	 *
	 * @param intValue The integer value to set.
	 */
	public void setValue(int intValue) {
		this.intValue = intValue;
		empty = false;
		getI18nText().setStaticText(getValueAsString());
	}

	/**
	 * Check whether this {@link NumberDataField} represents an integer value (digits == 0).
	 * @return Whether this {@link NumberDataField} represents an integer value (digits == 0).
	 */
	public boolean isInteger() {
		return digits == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return empty;
	}

	@Override
	public StaticI18nText getI18nText() {
		if (textBuffer == null)
			textBuffer = new StaticI18nText(getValueAsString());
		return textBuffer;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns instance of BigDecimal representing the value of this field.
	 */
	@Override
	public BigDecimal getData() {
		if (isInteger())
			return new BigDecimal(getIntValue());
		else
			return BigDecimal.valueOf(getDoubleValue());
	}

	@Override
	public void setData(Object data) {
		if (data == null) {
			setValue(0);
		} else if (data instanceof BigDecimal) {
			setValue(((BigDecimal) data).intValueExact());
		} else if (data instanceof Number) {
			Number num = (Number) data;
			if (isInteger())
				setValue(num.intValue());
			else
				setValue((int) (num.doubleValue() * Math.pow(10, digits)));
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + " does not support input data of type " + data.getClass());
		}
	}

	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return
			Number.class.isAssignableFrom(inputType) ||
			NumberDataField.class.isAssignableFrom(inputType);
	}
}
