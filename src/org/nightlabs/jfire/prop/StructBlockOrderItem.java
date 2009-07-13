/**
 * 
 */
package org.nightlabs.jfire.prop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.prop.IStruct.OrderMoveDirection;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;

import org.nightlabs.jfire.prop.id.StructBlockOrderItemID;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link StructBlockOrderItem}s are used within {@link StructLocal}s to order the {@link StructBlock}s
 * they reference. Each instance of {@link StructLocal} will have its own set of order items and therefore
 * can have individual ordering of the same StructBlocks (including those coming from the parent {@link Struct}).
 * The {@link StructBlockOrderItem} also define the order of the fields contained in the referenced block.
 * <p>
 * The order items are not public API, they are managed implicitly when blocks/fields
 * are added and when the methods {@link StructLocal#moveStructBlockInOrder(StructBlockID, org.nightlabs.jfire.prop.StructLocal.OrderMoveDirection)} 
 * or {@link StructLocal#moveStructFieldInOrder(StructFieldID, org.nightlabs.jfire.prop.StructLocal.OrderMoveDirection)}
 * are used. 
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.id.StructBlockOrderItemID"
 *		detachable="true"
 *		table="JFireBase_Prop_StructBlockOrderItem"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, structBlockOrderItemID"
 *
 * @jdo.fetch-group
 * 		name="IStruct.fullData" fetch-groups="default"
 * 		fields="structFieldOrderItems, structLocal"
 * 
 */
@PersistenceCapable(
	objectIdClass=StructBlockOrderItemID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructBlockOrderItem")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="structFieldOrderItems"), @Persistent(name="structLocal")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructBlockOrderItem implements Serializable, Comparator<StructField<?>>
{
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
	private long structBlockOrderItemID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="500"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=500)
	private String structBlockID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructLocal structLocal;

	/**
	 * Used to order the {@link StructField}s in the referenced StructBlock.
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.StructFieldOrderItem"
	 *		mapped-by="structBlockOrderItem"
	 *		dependent-element="true"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="structBlockOrderItem",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<StructFieldOrderItem> structFieldOrderItems;

	/**
	 * Initialized from {@link #structBlockID}.
	 * 
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient StructBlockID structBlockIDObj;

	/**
	 * @deprecated Only for JDO
	 */
	protected StructBlockOrderItem() {
	}

	public StructBlockOrderItem(String organisationID, long structBlockOrderItemID, StructBlock structBlock) {
		this.organisationID = organisationID;
		this.structBlockOrderItemID = structBlockOrderItemID;
		this.structBlockID = structBlock.getStructBlockIDObj().toString();
		this.structFieldOrderItems = new ArrayList<StructFieldOrderItem>();
	}

	/**
	 * @return The organisationID (Primary key).
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	/**
	 * @return The structBlockOrderItemID (Primary key).
	 */
	public long getStructBlockOrderItemID() {
		return structBlockOrderItemID;
	}
	
	/**
	 * @return The {@link StructLocal} this order item is part of.
	 */
	public StructLocal getStructLocal() {
		return structLocal;
	}
	
	/**
	 * Returns the id of the {@link StructBlock} referenced by this
	 * {@link StructBlockOrderItem}. The value is created from the String representation
	 * of the id as this is stored ({@link #structBlockID}).
	 * 
	 * @return Returns the id of the {@link StructBlock} referenced by this {@link StructBlockOrderItem}.
	 */
	public StructBlockID getStructBlockID() {
		if (structBlockIDObj == null) {
			structBlockIDObj = (StructBlockID) ObjectIDUtil.createObjectID(structBlockID);
		}
		return structBlockIDObj;
	}
	
	/**
	 * Add the given {@link StructFieldOrderItem} to the end of the list of field order items.
	 * @param structFieldOrderItem The {@link StructFieldOrderItem} to add.
	 */
	public void addStructFieldOrderItem(StructFieldOrderItem structFieldOrderItem) {
		this.structFieldOrderItems.add(structFieldOrderItem);
	}
	/**
	 * Remove the given {@link StructFieldOrderItem} from the list of field order items.
	 * @param structFieldOrderItem The item to remove.
	 */
	public void removeStructFieldOrderItem(StructFieldOrderItem structFieldOrderItem) {
		this.structFieldOrderItems.remove(structFieldOrderItem);
	}
	
	/**
	 * Returns the {@link StructFieldOrderItem} for the given {@link StructField} id.
	 * 
	 * @param itemID The {@link StructField} id to find the corresponding order item for.
	 * @return Returns the {@link StructFieldOrderItem} for the given {@link StructField} id, or <code>null</code> if it could not be found.
	 */
	public StructFieldOrderItem getStructFieldOrderItem(StructFieldID itemID) {
		StructFieldOrderItem moveItem = null;
		for (StructFieldOrderItem item : structFieldOrderItems) {
			if (item.getStructFieldID().equals(itemID)) {
				moveItem = item;
				break;
			}
		}
		return moveItem;
	}

	/**
	 * Moves the {@link StructFieldOrderItem} referenced by the given StructField one step up/down 
	 * in the ordering of this {@link StructBlockOrderItem}.
	 * <p>
	 * Note, that {@link StructLocal#moveStructFieldInOrder(StructBlockID, OrderMoveDirection)} delegates
	 * to this method.
	 * </p>
	 * 
	 * @param structFieldID The {@link StructField} id of the item to move.
	 * @param orderMoveDirection The direction up/down to move.
	 */
	public void moveStructFieldInOrder(StructFieldID structFieldID, OrderMoveDirection orderMoveDirection) {
		int orgIdx = -1;
		for (int i = 0; i < structFieldOrderItems.size(); i++) {
			if (structFieldOrderItems.get(i).getStructFieldID().equals(structFieldID)) {
				orgIdx = i;
				break;
			}
		}
		if (orgIdx < 0)
			return; // not found.
		int swapIdx = orgIdx;
		switch (orderMoveDirection) {
		case up:
			if (orgIdx == 0) {
				// can't move up, already first
				return;
			}
			swapIdx = orgIdx -1;
			break;

		case down:
			if (orgIdx == structFieldOrderItems.size() -1) {
				// can't move down, already last
				return;
			}
			swapIdx = orgIdx + 1;
			break;
		}
		Collections.swap(structFieldOrderItems, orgIdx, swapIdx);

	}

	@Override
	public int compare(StructField<?> field1, StructField<?> field2) {
		StructFieldID fieldID1 = field1 != null ? field1.getStructFieldIDObj() : null;
		StructFieldID fieldID2 = field2 != null ? field2.getStructFieldIDObj() : null;
		StructFieldOrderItem fieldItem1 = fieldID1 != null ? getStructFieldOrderItem(fieldID1) : null;
		StructFieldOrderItem fieldItem2 = fieldID2 != null ? getStructFieldOrderItem(fieldID2) : null;
		int idx1 = fieldItem1 != null ? structFieldOrderItems.indexOf(fieldItem1) : -1;
		int idx2 = fieldItem1 != null ? structFieldOrderItems.indexOf(fieldItem2) : -1;
		return new Integer(idx1).compareTo(idx2);
	}
}
