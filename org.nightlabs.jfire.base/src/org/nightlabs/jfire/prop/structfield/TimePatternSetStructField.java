package org.nightlabs.jfire.prop.structfield;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.TimePatternSetDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @jdo.persistence-capable
 * 	identity-type="application"
 *  persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *  detachable="true"
 *  table="JFireBase_Prop_TimePatternSetStructField"
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
	table="JFireBase_Prop_TimePatternSetStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TimePatternSetStructField extends StructField<TimePatternSetDataField>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TimePatternSetStructField() { }

	public TimePatternSetStructField(StructBlock structBlock) {
		super(structBlock);
	}

	public TimePatternSetStructField(StructBlock structBlock, StructFieldID structFieldID) {
		super(structBlock, structFieldID);
	}

	@Override
	protected TimePatternSetDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new TimePatternSetDataField(dataBlock, this);
	}

	@Override
	public Class<TimePatternSetDataField> getDataFieldClass() {
		return TimePatternSetDataField.class;
	}

}