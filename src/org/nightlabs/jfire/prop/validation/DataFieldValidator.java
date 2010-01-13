/**
 * 
 */
package org.nightlabs.jfire.prop.validation;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.validation.id.DataFieldValidatorID;

/**
 * Abstract base class for {@link DataFieldValidator}s that are able to validate a {@link DataField}.
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		objectid-class="org.nightlabs.jfire.prop.validation.id.DataFieldValidatorID"
 *    	detachable="true"
 *    	table="JFireBase_Prop_DataFieldValidator"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 **/@PersistenceCapable(
	objectIdClass=DataFieldValidatorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_DataFieldValidator")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public abstract class DataFieldValidator<DataFieldType extends DataField, StructFieldType extends StructField<DataFieldType>>
implements IDataFieldValidator<DataFieldType, StructFieldType>, Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private long validatorID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@SuppressWarnings("unused")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructField<?> structField;
	
	/**
	 * @deprecated only for JDO
	 */
	@Deprecated
	protected DataFieldValidator() {
		super();
	}

//	/**
//	 *
//	 * @param organisationID
//	 * @param validatorID
//	 */
//	public DataFieldValidator(String organisationID, long validatorID)
//	{
//		if (organisationID == null)
//			throw new IllegalArgumentException("Param organisationID must not be null");
//
//		this.organisationID = organisationID;
//		this.validatorID = validatorID;
//	}
	
	/**
	 * 
	 * @param organisationID
	 * @param validatorID
	 * @param structField
	 */
	public DataFieldValidator(String organisationID, long validatorID, StructField<?> structField) {
		if (organisationID == null)
			throw new IllegalArgumentException("Param organisationID must not be null");
				
		this.organisationID = organisationID;
		this.validatorID = validatorID;
		this.structField = structField;
	}
	
//	/**
//	 *
//	 * @param structField
//	 */
//	public void setStructField(StructField<?> structField)
//	{
//		if (structField == null)
//			throw new IllegalArgumentException("structField must not be null!");
//
//		if (this.structField != null)
//			throw new IllegalStateException("Field structField is already initialised! Cannot call this method twice!");
//
//		this.structField = structField;
//	}
	
	/**
	 * Returns the organisationID.
	 * @return the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the validatorID.
	 * @return the validatorID
	 */
	public long getValidatorID() {
		return validatorID;
	}
}
