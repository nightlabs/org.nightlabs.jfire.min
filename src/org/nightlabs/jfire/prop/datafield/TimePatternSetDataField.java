package org.nightlabs.jfire.prop.datafield;

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
import org.nightlabs.timepattern.TimePatternSet;

/**
 * {@link DataField} that stores a {@link TimePatternSet}.
 *
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *                          detachable="true"
 *                          table="JFireBase_Prop_TimePatternSetDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="timepatternSet"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_TimePatternSetDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members=@Persistent(name="timePatternSet"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TimePatternSetDataField
extends DataField
implements II18nTextDataField
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20090116L;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected TimePatternSet timePatternSet;

	/**
	 * For JDO only.
	 */
	protected TimePatternSetDataField() {
	}

	/**
	 * Create a new {@link TimePatternSetDataField} for the given {@link DataBlock}
	 * that represents the given {@link StructField}.
	 *
	 * @param dataBlock The {@link DataBlock} the new {@link PhoneNumberDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link PhoneNumberDataField} represents in the data structure.
	 */
	public TimePatternSetDataField(DataBlock dataBlock, StructField<TimePatternSetDataField> structField) {
		super (dataBlock, structField);
	}

	/**
	 * Used for cloning.
	 */
	protected TimePatternSetDataField(String organisationID, long propertySetID, int dataBlockID, DataField cloneField) {
		super(organisationID, propertySetID, dataBlockID, cloneField);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet, int)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet, int dataBlockID) {
		TimePatternSetDataField newField = new TimePatternSetDataField(propertySet.getOrganisationID(), propertySet.getPropertySetID(), dataBlockID, this);
		newField.setTimePatternSet(this.timePatternSet);
		return newField;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return timePatternSet == null;
	}

	/**
	 * Set the {@link TimePatternSet} for this {@link TimePatternSetDataField}.
	 * @param timePatternSet The {@link TimePatternSet} to set.
	 */
	public void setTimePatternSet(TimePatternSet timePatternSet) {
		this.timePatternSet = timePatternSet;
	}

	/**
	 * Returns the {@link TimePatternSet} of this {@link TimePatternSetDataField}.
	 * @return The {@link TimePatternSet} of this {@link TimePatternSetDataField}.
	 */
	public TimePatternSet getTimePatternSet() {
		return timePatternSet;
	}

	@Override
	public StaticI18nText getI18nText() {
		return new StaticI18nText(timePatternSet.toString());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calling this method is equal to {@link #getTimePatternSet()}.
	 * </p>
	 */
	@Override
	public Object getData() {
		return getTimePatternSet();
	}

	@Override
	public void setData(Object data) {
		if (data == null) {
			setTimePatternSet(null);
		} else if (data instanceof TimePatternSetDataField) {
			TimePatternSetDataField dataField = (TimePatternSetDataField) data;
			this.setTimePatternSet(dataField.getTimePatternSet());
		} else if (data instanceof TimePatternSet) {
			this.setTimePatternSet((TimePatternSet) data);
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + " does not support input data of type " + data.getClass());
		}
	}

	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return
			TimePatternSet.class.isAssignableFrom(inputType) ||
			TimePatternSetDataField.class.isAssignableFrom(inputType);
	}
}
