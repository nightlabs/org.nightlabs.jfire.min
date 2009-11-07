package org.nightlabs.jfire.prop.datafield;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;
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
 * {@link DataField} that stores a {@link Date} value.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * 
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *                          detachable="true"
 *                          table="JFireBase_Prop_DateDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="date"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_DateDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members=@Persistent(name="date"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DateDataField
extends DataField
implements II18nTextDataField
{
	private static class DateI18nText extends I18nTextBuffer {
		private static final long serialVersionUID = 20090126L;
		private Date date;
		
		public DateI18nText(Date date) {
			this.date = date;
		}
		@Override
		public String getText(String languageID) {
			if (!containsLanguageID(languageID)) {
				if (date == null)
					setText(languageID, "");
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, new Locale(languageID));
				String text = df.format(date);
				setText(languageID, text);
			}
			return super.getText(languageID);
		}
	}
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20080116L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Date date;

	/**
	 * Create a new {@link DateDataField} for the given {@link DataBlock}
	 * that represents the given {@link StructField}.
	 * 
	 * @param dataBlock The {@link DataBlock} the new {@link DateDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link DateDataField} represents in the data structure.
	 */
	public DateDataField(DataBlock dataBlock, StructField<DateDataField> structField) {
		super(dataBlock, structField);
	}
	
	/**
	 * Used internally for cloning.
	 */
	protected DateDataField(String organisationID, long propertySetID, DataField cloneField) {
		super(organisationID, propertySetID, cloneField);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet) {
		DateDataField newField = new DateDataField(propertySet.getOrganisationID(), propertySet.getPropertySetID(), this);
		newField.setDate(getDate());
		return newField;
	}

	/**
	 * Returns the {@link Date} value of this {@link DataField}.
	 * @return The {@link Date} value of this {@link DataField}.
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * Set the {@link Date} value of this {@link DataField}. 
	 * @param date The {@link Date} value to set.
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return date == null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns an I18n Text giving the current Date formatted for each Locale.
	 * </p> 
	 */
	@Override
	public I18nText getI18nText() {
		return new DateI18nText(this.date);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calling this method is equal to calling {@link #getDate()}.
	 * </p>
	 */
	@Override
	public Object getData() {
		return getDate();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Supports {@link Date} and <code>null</code> input.
	 * </p>
	 */
	@Override
	public void setData(Object data) {
		if (date == null) {
			setDate(null);
			return;
		} else if (data instanceof Date) {
			setDate((Date) data);
		} else if (data instanceof DateDataField) {
			setData(((DateDataField) data).getDate());
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + " does not support input data of type " + data.getClass());
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Supports {@link Date} input.
	 * </p>
	 */
	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return 
			Date.class.isAssignableFrom(inputType) ||
			DateDataField.class.isAssignableFrom(inputType);
	}
}
