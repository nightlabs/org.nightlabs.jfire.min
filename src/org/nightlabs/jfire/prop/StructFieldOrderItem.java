/**
 * 
 */
package org.nightlabs.jfire.prop;

import java.io.Serializable;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import org.nightlabs.jfire.prop.id.StructFieldOrderItemID;

/**
 * A {@link StructFieldOrderItem} is used to reference a {@link StructField}
 * within a {@link StructBlockOrderItem}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.id.StructFieldOrderItemID"
 *		detachable="true"
 *		table="JFireBase_Prop_StructFieldOrderItem"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, structFieldOrderItemID"
 * 
 * @jdo.fetch-group
 * 		name="IStruct.fullData" fetch-groups="default"
 * 		fields="structBlockOrderItem"
 * 
 */
@PersistenceCapable(
	objectIdClass=StructFieldOrderItemID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructFieldOrderItem")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members=@Persistent(name="structBlockOrderItem"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructFieldOrderItem implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String organisationID;
	
	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long structFieldOrderItemID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructBlockOrderItem structBlockOrderItem;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="500"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=500)
	private String structFieldID;
	
	/**
	 * Initialized from {@link #structFieldID}.
	 * 
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient StructFieldID structFieldIDObj;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected StructFieldOrderItem() {
	}
	
	public StructFieldOrderItem(StructBlockOrderItem structBlockOrderItem, long structFieldOrderItemID, StructField<?> structField) {
		this.organisationID = structBlockOrderItem.getOrganisationID();
		this.structFieldOrderItemID = structFieldOrderItemID;
		this.structFieldID = structField.getStructFieldIDObj().toString();
		this.structBlockOrderItem = structBlockOrderItem;
	}
	/**
	 * @return The organisationID (Primary key).
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	/**
	 * @return The structFieldOrderItemID (Primary key).
	 */	
	public long getStructFieldOrderItemID() {
		return structFieldOrderItemID;
	}
	
	/**
	 * @return The {@link StructBlockOrderItem} this field item is part of.
	 */
	public StructBlockOrderItem getStructBlockOrderItem() {
		return structBlockOrderItem;
	}
	
	/**
	 * Returns the id of the {@link StructField} referenced by this
	 * {@link StructFieldOrderItem}. The value is created from the String representation
	 * of the id as this is stored ({@link #structFieldID}).
	 * 
	 * @return Returns the id of the {@link StructField} referenced by this {@link StructFieldOrderItem}.
	 */
	public StructFieldID getStructFieldID() {
		if (structFieldIDObj == null) {
			structFieldIDObj = (StructFieldID) ObjectIDUtil.createObjectID(structFieldID);
		}
		return structFieldIDObj;
	}

}
