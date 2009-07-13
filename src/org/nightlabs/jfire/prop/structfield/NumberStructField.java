package org.nightlabs.jfire.prop.structfield;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;


/**
 * {@link StructField} that represents a {@link DataField} a number in the
 * format of a int along with its number of digits (precision) the number should have. 
 * The actual value for a {@link DataField} can then be calculated using the value of the number
 * stored in the {@link DataField} divided by 10^digits defined in the {@link NumberStructField}.
 * 
 * @jdo.persistence-capable
 * 	identity-type="application"
 *  persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *  detachable="true"
 *  table="JFireBase_Prop_NumberStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default"
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_NumberStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class NumberStructField extends StructField<NumberDataField>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The following members define the possible data range for this number struct field.
	 * This is done by specifying the number of digits to be used and then give an integer
	 * spinnerMax and spinnerMin value. The actual max and min values are then calculated as:
	 *   - actual min: min / (10^digits)
	 *   - actual max: max / (10^digits)
	 *
	 * So if you give 3 digits and specify max = 4000 and min = 1000, then the actual
	 * values are 4.0 and 1.0.
	 */

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int max = 10;
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int min = 0;
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int digits = 0;
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean bounded = false;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected NumberStructField() { }
	
	/**
	 * Create a new {@link NumberStructField} for the given {@link StructBlock}.
	 * @param structBlock The {@link StructBlock} the new {@link NumberStructField} will be part of.
	 */
	public NumberStructField(StructBlock structBlock) {
		super(structBlock);
	}

	/**
	 * Create a new {@link NumberStructField} for the given {@link StructBlock} 
	 * and with primary-key values from the given {@link StructFieldID}.
	 * 
	 * @param structBlock The {@link StructBlock} the new {@link NumberStructField} will be part of.
	 * @param structFieldID The {@link StructFieldID} the new {@link NumberStructField} should take primary-key values from.
	 */
	public NumberStructField(StructBlock structBlock, StructFieldID structFieldID) {
		super(structBlock, structFieldID.structFieldOrganisationID, structFieldID.structFieldID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#createDataFieldInstanceInternal(org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	protected NumberDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new NumberDataField(dataBlock, this, digits);
	}

	/**
	 * Checks if {@link DataField}s for this {@link NumberStructField} represent integer
	 * (as opposite to decimal) values. This is determined by the number 
	 * of digigts (0: integer, >0: decimal).
	 * 
	 * @return Whether {@link DataField}s for this {@link NumberStructField} represent integer values.
	 */
	public boolean isInteger() {
		return digits == 0;
	}

	/**
	 * Returns whether {@link DataField}s for this {@link NumberStructField} are bounded
	 * to a certain minimun and maximum value.
	 * <p>
	 * This can set using {@link #setBounded(boolean)} and the bounds can be 
	 * controlled using {@link #setMin(int)} and {@link #setMax(int)}.
	 * </p>
	 * @return whether {@link DataField}s for this {@link NumberStructField} are bounded
	 * to a certain minimun and maximum value.
	 */
	public boolean isBounded() {
		return bounded;
	}
	
	/**
	 * Define whether {@link DataField}s for this {@link NumberStructField} should be 
	 * bounded to a certain minimum and maximum value (defined by {@link #setMin(int)} and {@link #setMax(int)})
	 * 
	 * @param bounded Whether {@link DataField} for this {@link NumberStructField} should be bounded.
	 */
	public void setBounded(boolean bounded) {
		this.bounded = bounded;

		notifyModifyListeners();
	}

	/**
	 * Returns the value set for the maximum {@link DataField}s of this {@link NumberStructField} might have.
	 * Note, that this is only relevant if {@link #isBounded()} is <code>true</code>.
	 * <p>
	 * Also note, that this is not the actual maximum value as it does not take the number
	 * of digits into account. The actual maximum can be determined using {@link #getActualMax()}. 
	 * </p>
	 * 
	 * @return The maximum value for {@link DataField}s for this {@link NumberStructField}.
	 */
	public int getMax() {
		return max;
	}

	/**
	 * Returns the value set for the minimum {@link DataField}s of this {@link NumberStructField} might have.
	 * Note, that this is only relevant if {@link #isBounded()} is <code>true</code>.
	 * <p>
	 * Also note, that this is not the actual minimum value as it does not take the number
	 * of digits into account. The actual minimum can be determined using {@link #getActualMin()}. 
	 * </p>
	 * 
	 * @return The minimum value for {@link DataField}s for this {@link NumberStructField}.
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Set the maximum value a {@link DataField}s of this {@link NumberStructField} might have.
	 * Note, that this is only relevant if {@link #isBounded()} is <code>true</code>.
	 * <p>
	 * Also note, that this does not include the number of digits set for this 
	 * {@link NumberStructField} so this is not the actual maximum value.
	 * The actual maximum can be determined by {@link #getActualMax()}.
	 * </p>
	 * 
	 * @param max The maximum value {@link DataField}s for this {@link NumberStructField} might have.
	 */
	public void setMax(int max) {
		checkBounds(min, max);
		this.max = max;

		notifyModifyListeners();
	}

	/**
	 * Set the minimum value a {@link DataField}s of this {@link NumberStructField} might have.
	 * Note, that this is only relevant if {@link #isBounded()} is <code>true</code>.
	 * <p>
	 * Also note, that this does not include the number of digits set for this 
	 * {@link NumberStructField} so this is not the actual minimum value.
	 * The actual maximum can be determined by {@link #getActualMin()}.
	 * </p>
	 * 
	 * @param max The minimum value {@link DataField}s for this {@link NumberStructField} might have.
	 */
	public void setMin(int min) {
		checkBounds(min, max);
		this.min = min;

		notifyModifyListeners();
	}

	/**
	 * Returns the actual maximum value a {@link DataField} for this {@link StructField} might have.
	 * Note, that this is only relevant if {@link #isBounded()} is <code>true</code>.
	 * The actual maximum value is calculated by taking the maximum value set by {@link #setMax(int)}
	 * and dividing it by 10^digits.
	 *  
	 * @return The actual maximum value a {@link DataField} for this {@link StructField} might have.
	 */
	public double getActualMax() {
		return max / Math.pow(10, digits);
	}

	/**
	 * Returns the actual minimum value a {@link DataField} for this {@link StructField} might have.
	 * Note, that this is only relevant if {@link #isBounded()} is <code>true</code>.
	 * The actual minimum value is calculated by taking the maximum value set by {@link #setSpinnerMIn(int)}
	 * and dividing it by 10^digits.
	 *  
	 * @return The actual maximum value a {@link DataField} for this {@link StructField} might have.
	 */
	public double getActualMin() {
		return min / Math.pow(10, digits);
	}

	/**
	 * Sets the number of digits {@link DataField}s for this {@link NumberStructField} should have.
	 * The number of digits define the acutal value of the DataFields as it is
	 * calculated taking the integer value and dividing it by 10^digits.
	 * 
	 * @param digits The number of digits {@link DataField}s for this {@link NumberStructField} should have. 
	 */
	public void setDigits(int digits) {
		if (digits < 0)
			throw new IllegalArgumentException("Digits must be non-negative.");

		this.digits = digits;

		notifyModifyListeners();
	}

	public int getDigits() {
		return digits;
	}

	private void checkBounds(int min, int max) {
		if (bounded && min >= max)
			throw new IllegalArgumentException("Minimum must be less than maximum.");
	}

	@Override
	public String toString() {
		if (isInteger())
			return "Integer of range [" + getMin() + ", " + getMax() + "]";
		else
			return "Decimal of range [" + getActualMin() + ", " + getMax() + "]";
	}

	@Override
	public Class<NumberDataField> getDataFieldClass() {
		return NumberDataField.class;
	}

}