package org.nightlabs.jfire.prop.datafield;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.StaticI18nText;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link DataField} that stores a phone number in form of three parts:
 * country-code, area-code and local number.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *                          detachable="true"
 *                          table="JFireBase_Prop_PhoneNumberDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="countryCode,areaCode,localNumber"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_PhoneNumberDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members={@Persistent(name="countryCode"), @Persistent(name="areaCode"), @Persistent(name="localNumber")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PhoneNumberDataField
extends DataField
implements II18nTextDataField
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20090116L;

	/** @jdo.field persistence-modifier="persistent" @jdo.column length="25" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String countryCode = "";
	/** @jdo.field persistence-modifier="persistent" @jdo.column length="25" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String areaCode = "";
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String localNumber = "";
	
	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient StaticI18nText textBuffer = null;	

	/**
	 * Create a new {@link PhoneNumberDataField} for the given {@link DataBlock}
	 * that represents the given {@link StructField}.
	 *
	 * @param dataBlock The {@link DataBlock} the new {@link PhoneNumberDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link PhoneNumberDataField} represents in the data structure.
	 */
	public PhoneNumberDataField(DataBlock dataBlock, StructField<PhoneNumberDataField> structField) {
		super (dataBlock, structField);
	}

	/**
	 * Used for cloning.
	 */
	protected PhoneNumberDataField(String organisationID, long propertySetID, DataField cloneField) {
		super(organisationID, propertySetID, cloneField);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet) {
		PhoneNumberDataField newField = new PhoneNumberDataField(propertySet.getOrganisationID(), propertySet.getPropertySetID(), this);
		newField.countryCode = countryCode;
		newField.areaCode = areaCode;
		newField.localNumber = localNumber;
		return newField;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		// empty when all parts empty
		return
				(countryCode == null || "".equals(countryCode)) &&
				(areaCode == null || "".equals(areaCode)) &&
				(localNumber == null || "".equals(localNumber))
		;
	}
	/**
	 * Returns the country-code part of the phone number stored by this {@link PhoneNumberDataField}.
	 * Note, that the value might be <code>null</code>.
	 * @return The country-code part of the phone number stored by this data field.
	 */
	public String getCountryCode() {
		return countryCode;
	}
	/**
	 * Set the country-code part of the phone number stored by this {@link PhoneNumberDataField}.
	 * @param countryCode The country-code to set.
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
		getI18nText().setStaticText(getPhoneNumberAsString());
	}

	/**
	 * Returns the area-code part of the phone number stored by this {@link PhoneNumberDataField}.
	 * Note that the value might be <code>null</code>.
	 * @return The area-code part of the phone number stored by this data field.
	 */
	public String getAreaCode() {
		return areaCode;
	}

	/**
	 * Set the area-code part of the phone number stored by this {@link PhoneNumberDataField}.
	 * @param areaCode The area-code to set.
	 */
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
		getI18nText().setStaticText(getPhoneNumberAsString());
	}

	/**
	 * Returns the local-number of the phone number stored by this {@link PhoneNumberDataField}.
	 * Note that the value might be <code>null</code>.
	 * @return The local-number part of the phone number stored by this data field.
	 */
	public String getLocalNumber() {
		return localNumber;
	}

	/**
	 * Set the local-number part of the phone number stored by this {@link PhoneNumberDataField}.
	 * @param number The local-number to set.
	 */
	public void setLocalNumber(String number) {
		this.localNumber = number;
		getI18nText().setStaticText(getPhoneNumberAsString());
	}

	/**
	 * Tries to split the given phone number into the three parts
	 * of a {@link PhoneNumberDataField} and set the values of
	 * this {@link PhoneNumberDataField} accordingly.
	 *
	 * @param phoneNumber The phone number to parse.
	 */
	public void parsePhoneNumber(String phoneNumber) {
		// TODO: Sorry, no time implementing right now
		// @Daniel: Isn't this TO DO done after your and my change? Marco.

		// Added this - I think it was missing. Marco.
		setCountryCode(null);
		setAreaCode(null);
		setLocalNumber(null);

		// Split phoneNumber into countryCode, areaCode, localNumber
		if (phoneNumber != null) {
			String[] parts = phoneNumber.split("-");
			if (parts.length >= 3) {
				setCountryCode(parts[0]);
				setAreaCode(parts[1]);
//				setLocalNumber(parts[2]);

// The localNumber might contain '-' chars (extensions are common!). Hence we need to add all of these parts!
				StringBuilder sb = new StringBuilder();
				for (int i = 2; i < parts.length; ++i) {
					if (sb.length() > 0)
						sb.append('-');

					sb.append(parts[i]);
				}
				setLocalNumber(sb.toString());
			}
			else if (parts.length == 2) {
				setAreaCode(parts[0]);
				setLocalNumber(parts[1]);
			}
			else if (parts.length == 1 || parts.length == 0) {
				setLocalNumber(phoneNumber);
			}
		}
	}

	/**
	 * Returns the phone number represented by this {@link PhoneNumberDataField} as String.
	 * @return The phone number represented by this {@link PhoneNumberDataField} as String.
	 */
	public String getPhoneNumberAsString() {
		final StringBuilder sb = new StringBuilder();
		if (countryCode != null && countryCode.trim().length() > 0)
			sb.append("+").append(countryCode);

		if (areaCode != null && areaCode.trim().length() > 0) {
			if (countryCode != null && !countryCode.trim().isEmpty())
				sb.append("-");
			sb.append(areaCode);
		}

		if (localNumber != null && localNumber.trim().length() > 0) {
			if (areaCode != null && !areaCode.trim().isEmpty())
				sb.append("-");
			sb.append(localNumber);
		}

		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns an {@link I18nText} that returns the {@link #getPhoneNumberAsString()}
	 * for every language.
	 * </p>
	 */
	@Override
	public StaticI18nText getI18nText()
	{
		if (textBuffer == null) {
			textBuffer = new StaticI18nText(getPhoneNumberAsString());
		}
		return textBuffer;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calling this method is equal to calling {@link #getPhoneNumberAsString()}.
	 * </p>
	 */
	@Override
	public Object getData() {
		return getPhoneNumberAsString();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Supports only other {@link PhoneNumberDataField}s and Strings. 
	 * For strings {@link #parsePhoneNumber(String)} will be used.
	 * </p>
	 */
	@Override
	public void setData(Object data) {
		if (data ==  null) {
			parsePhoneNumber(null);
		} else if (data instanceof PhoneNumberDataField) {
			PhoneNumberDataField other = (PhoneNumberDataField) data;
			this.setCountryCode(other.getCountryCode());
			this.setAreaCode(other.getAreaCode());
			this.setLocalNumber(other.getLocalNumber());
		} else if (data instanceof String) {
			parsePhoneNumber((String) data);
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + " does not support input data of type " + data.getClass());
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 
	 * </p>
	 */
	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return 
			PhoneNumberDataField.class.isAssignableFrom(inputType) ||
			String.class.isAssignableFrom(inputType);
	}
}
