package org.nightlabs.jfire.prop.validation;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.prop.validation.id.DataFieldExpressionID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;

/**
 * Abstract base class for expressions on {@link DataField}s
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		detachable="true"
 * 		table="JFireBase_Prop_DataFieldExpression"
 * 		objectid-class="org.nightlabs.jfire.prop.validation.id.DataFieldExpressionID"
 *
 * @jdo.inheritance
 * 		strategy="new-table"
 *
 * @jdo.inheritance-discriminator
 * 		strategy="class-name"
 * 		column="className"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.fetch-group name="IExpression.fullData" fields="structFieldString"
 *
 * @param <DataFieldType> The type of the {@link DataField} about which this instance formulates an expression.
 */
@PersistenceCapable(
	objectIdClass=DataFieldExpressionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_DataFieldExpression")
@FetchGroups(
	@FetchGroup(
		name="IExpression.fullData",
		members=@Persistent(name="structFieldString"))
)
@Discriminator(
	column="className",
	strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class DataFieldExpression<DataFieldType extends DataField> implements IDataFieldExpression<DataFieldType>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long expressionID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String structFieldString;

	/**
	 * @jdo.field persistence-modifier="none"
	 */		@Persistent(persistenceModifier=PersistenceModifier.NONE)

	private StructFieldID structFieldID;
	
	/**
	 * @deprecated Only for JDO.
	 */
	@Deprecated
	protected DataFieldExpression() {
	}

	public DataFieldExpression(StructFieldID structFieldID) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(DataFieldExpression.class), structFieldID);
	}

	public DataFieldExpression(String organisationID, long expressionID, StructFieldID structFieldID) {
		super();
		this.organisationID = organisationID;
		this.expressionID = expressionID;		
		this.structFieldString = structFieldID.toString();
		this.structFieldID = structFieldID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getExpressionID() {
		return expressionID;
	}

	public StructFieldID getStructFieldID() {
		if (structFieldID == null)
			try {
				structFieldID = new StructFieldID(structFieldString);
			} catch (Exception e) {
				// should not happen since we created the string from an ID class
				throw new RuntimeException(e);
			}

		return structFieldID;
	}
	
	public void setStructFieldID(StructFieldID structFieldID) 
	{
		if (structFieldID == null)
			throw new IllegalArgumentException("Param structFieldID must not be null!");

		this.structFieldID = structFieldID;
		this.structFieldString = structFieldID.toString();
	}
}
