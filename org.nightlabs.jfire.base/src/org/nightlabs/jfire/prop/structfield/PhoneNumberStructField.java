package org.nightlabs.jfire.prop.structfield;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;

/**
 * {@link StructField} that represents a {@link DataField} storing phone number. 
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *                          detachable="true" table="JFireBase_Prop_PhoneNumberStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default"
 *
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_PhoneNumberStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PhoneNumberStructField extends StructField<PhoneNumberDataField>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PhoneNumberStructField() { }

	public PhoneNumberStructField(StructBlock _structBlock, StructFieldID _structFieldID) {
		super(_structBlock, _structFieldID.structFieldOrganisationID, _structFieldID.structFieldID);
	}

	public PhoneNumberStructField(StructBlock _structBlock) {
		super(_structBlock);
	}

	@Override
	protected PhoneNumberDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new PhoneNumberDataField(dataBlock, this);
	}

	@Override
	public Class<PhoneNumberDataField> getDataFieldClass() {
		return PhoneNumberDataField.class;
	}

}
