package org.nightlabs.jfire.prop.structfield;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.l10n.DateFormatter;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link StructField} that represents data fields holding a date value.
 * The {@link DateStructField} can configure the ui the data is shown/edited
 * by setting its flags ({@link #setDateTimeEditFlags(long)}.
 * 
 * @jdo.persistence-capable
 * 	identity-type="application"
 *  persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *  detachable="true"
 *  table="JFireBase_Prop_DateStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_DateStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DateStructField extends StructField<DateDataField>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long dateTimeEditFlags;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DateStructField() { }

	/**
	 * Create a new {@link DateStructField} for the given {@link StructBlock}.
	 * 
	 * @see StructField#StructField(StructBlock)
	 * @param structBlock The {@link StructBlock} the new {@link DateStructField} will be part of.
	 */
	public DateStructField(StructBlock structBlock) {
		super(structBlock);
	}
	/**
	 * Create a new {@link DateStructField} for the given {@link StructBlock} 
	 * and with primary-key values from the given {@link StructFieldID}.
	 * 
	 * @param structBlock The {@link StructBlock} the new {@link DateStructField} will be part of.
	 * @param structFieldID The {@link StructFieldID} the new {@link DateStructField} should take primary-key values from.
	 */
	public DateStructField(StructBlock structBlock, StructFieldID structFieldID) {
		super(structBlock, structFieldID.structFieldOrganisationID, structFieldID.structFieldID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#createDataFieldInstanceInternal(org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	protected DateDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new DateDataField(dataBlock, this);
	}
	
	/**
	 * Get the combination of edit flags set for this {@link DateStructField}.
	 * The different flag values are defined in {@link DateFormatter}.
	 *  
	 * @return The combination of edit flags of this {@link DateStructField}.
	 */
	public long getDateTimeEditFlags() {
		return dateTimeEditFlags;
	}

	/**
	 * Set the edit flags that should be used to show/edit the value of
	 * {@link DateDataField}s for this {@link DateStructField}. The flags
	 * can be a or-ed combination of the flags defined in {@link DateFormatter}. 
	 * 
	 * @param dateTimeEditFlags The edit flags to set.
	 */
	public void setDateTimeEditFlags(long dateTimeEditFlags) {
		this.dateTimeEditFlags = dateTimeEditFlags;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#getDataFieldClass()
	 */
	@Override
	public Class<DateDataField> getDataFieldClass() {
		return DateDataField.class;
	}
}