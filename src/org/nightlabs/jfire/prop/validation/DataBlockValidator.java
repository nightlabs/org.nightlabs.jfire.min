package org.nightlabs.jfire.prop.validation;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.StructBlock;

import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.prop.validation.id.DataBlockValidatorID;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;

/**
 * Abstract base class for {@link DataBlockValidator}s that are able to validate a {@link DataBlock}.
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		objectid-class="org.nightlabs.jfire.prop.validation.id.DataBlockValidatorID"
 *    	detachable="true"
 *    	table="JFireBase_Prop_DataBlockValidator"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 **/@PersistenceCapable(
	objectIdClass=DataBlockValidatorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_DataBlockValidator")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public abstract class DataBlockValidator implements IDataBlockValidator 
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
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private StructBlock structBlock; 
	
	/**
	 * @deprecated only for JDO
	 */
	protected DataBlockValidator() {
		super();
	}
	
//	/**
//	 * Creates a DataBlockValidator.
//	 * 
//	 * @param organisationID
//	 * @param validatorID
//	 */
//	public DataBlockValidator(String organisationID, long validatorID) {
//		if (organisationID == null)
//			throw new IllegalArgumentException("Param organisationID must not be null");
//		
//		this.organisationID = organisationID;
//		this.validatorID = validatorID;
//	}
	
	/**
	 * Creates a DataBlockValidator.
	 * 
	 * @param organisationID
	 * @param validatorID
	 */
	public DataBlockValidator(String organisationID, long validatorID, StructBlock structBlock) {
		if (organisationID == null)
			throw new IllegalArgumentException("Param organisationID must not be null");
		
		this.organisationID = organisationID;
		this.validatorID = validatorID;
		this.structBlock = structBlock;
	}
	
//	/**
//	 * Sets the {@link StructBlock} where this DataBlockValidator belongs to.
//	 * @param structBlock the {@link StructBlock} to set.
//	 */
//	public void setStructBlock(StructBlock structBlock)
//	{
//		if (structBlock == null)
//			throw new IllegalArgumentException("structBlock must not be null!");
//		
//		if (this.structBlock != null)
//			throw new IllegalStateException("Field structBlock is already initialised! Cannot call this method twice!");
//		
//		this.structBlock = structBlock;
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
