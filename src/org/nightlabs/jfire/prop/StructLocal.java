package org.nightlabs.jfire.prop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.exception.IllegalStructureModificationException;
import org.nightlabs.jfire.prop.i18n.StructLocalName;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.validation.IPropertySetValidator;

/**
 * A StructLocal instance reflects the local modifications of a {@link Struct}-instance.
 * A StructLocal 'inherits' all {@link StructBlock}s and {@link StructField}s
 * from its associated {@link Struct} and might not remove or change
 * those. However it can add new {@link StructField}s to existing
 * and new {@link StructBlock}s. It can also re-order the blocks and fields.
 * <p>
 * There might be several variants of {@link StructLocal}s of the same {@link Struct}.
 * These will be distinguised by their structLocalScope ({@link #getStructLocalScope()}).
 * </p>
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 *    objectid-class="org.nightlabs.jfire.prop.id.StructLocalID"
 *    detachable="true"
 *    table="JFireBase_Prop_StructLocal"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, linkClass, structScope, structLocalScope"
 *		add-interfaces="org.nightlabs.jfire.prop.id.IStructID"
 *		include-body="id/StructLocalID.body.inc"
 *
 * @jdo.implements
 * 		name="org.nightlabs.jfire.prop.IStruct"
 *
 * @jdo.query
 *    name="getAllStructLocalIDs"
 *    query="SELECT JDOHelper.getObjectId(this) import javax.jdo.JDOHelper"
 *
 * @jdo.fetch-group name="StructLocal.name" fetch-groups="default" fields="name"
 *
 * @jdo.fetch-group
 * 		name="IStruct.fullData" fetch-groups="default"
 * 		fields="localStructBlockList, struct, displayNameParts, name, propertySetValidators, structBlockOrderItems"
 */
@PersistenceCapable(
	objectIdClass=StructLocalID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructLocal")
@FetchGroups({
	@FetchGroup(
		fetchGroups={StructLocal.DEFAULT_SCOPE},
		name=StructLocal.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		fetchGroups={StructLocal.DEFAULT_SCOPE},
		name="IStruct.fullData",
		members={@Persistent(name="localStructBlockList"), @Persistent(name="struct"), @Persistent(name="displayNameParts"), @Persistent(name="name"), @Persistent(name="propertySetValidators"), @Persistent(name="structBlockOrderItems")})
})
@Queries(
	@javax.jdo.annotations.Query(
		name="getAllStructLocalIDs",
		value="SELECT JDOHelper.getObjectId(this) import javax.jdo.JDOHelper")
)
public class StructLocal extends AbstractStruct implements DetachCallback, AttachCallback {
	private static final long serialVersionUID = 20080709L;

	/**
	 * The default structLocalScope for new StructLocals of a Struct
	 */
	public static final String DEFAULT_SCOPE = "default";

	public static final String FETCH_GROUP_NAME = "StructLocal.name";

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		element-type="org.nightlabs.jfire.prop.StructBlock"
	 * 		table="JFireBase_Prop_StructLocal_localStructBlockList"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireBase_Prop_StructLocal_localStructBlockList",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<StructBlock> localStructBlockList;

//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	private transient Map<String, StructBlock> structBlockMap;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String linkClass;

	/**
	 * The structLocalScope of the Struct this StructLocal
	 * is associated to.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structScope;

	/**
	 * The structLocalScope of this StructLocal.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structLocalScope;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructLocalName name;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.DisplayNamePart"
	 *		table="JFireBase_Prop_StructLocal_displayNameParts"
	 *		dependent-element="true"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		dependentElement="true",
		table="JFireBase_Prop_StructLocal_displayNameParts",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DisplayNamePart> displayNameParts;


	/**
	 * A list of {@link StructBlockOrderItem} used to order all StructBlocks
	 * referenced in an instance of {@link StructLocal}. The {@link StructBlockOrderItem}
	 * also define the order of the fields contained in the referenced block.
	 * <p>
	 * The order items are not public API, they are managed implicitly when blocks/fields
	 * are added and when the methods {@link #moveStructBlockInOrder(StructBlockID, org.nightlabs.jfire.prop.StructLocal.OrderMoveDirection)}
	 * or {@link #moveStructFieldInOrder(StructFieldID, org.nightlabs.jfire.prop.StructLocal.OrderMoveDirection)}
	 * are used.
	 * </p>
	 * <p>
	 * Note that each {@link StructLocal} will have an own list of these items, that means
	 * that every structLocalScope of a structure can have a unique ordering.
	 * </p>
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.StructBlockOrderItem"
	 *		mapped-by="structLocal"
	 *		dependent-element="true"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="structLocal",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<StructBlockOrderItem> structBlockOrderItems;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Map<StructBlockID, StructBlockOrderItem> structBlockOrderItemMap;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		dependent="false"
	 */
	@Persistent(
		dependent="false",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Struct struct;

	/**
	 * This constructor is for JDO only.
	 */
	protected StructLocal() { }

	/**
	 * Create a new {@link StructLocal} based on the given {@link Struct} and
	 * with the given structLocalScope.
	 *
	 * @param struct The {@link Struct} the new {@link StructLocal} should be based on.
	 * @param organisationID The organisationID of the new {@link StructLocal}.
	 * @param structLocalScope The scope for the new {@link StructLocal}.
	 */
	public StructLocal(Struct struct, String structLocalScope) {
		this.linkClass = struct.getLinkClassInternal();
		this.organisationID = struct.getOrganisationID();
		this.displayNameParts = struct.displayNameParts;
		this.structScope = struct.getStructScope();
		this.structLocalScope = structLocalScope;
		this.struct = struct;
		this.name = new StructLocalName(this);
		this.structBlockOrderItems = new ArrayList<StructBlockOrderItem>();
		populate(struct);
	}

	/**
	 * @return The list of {@link StructBlockOrderItem}s for this StructLocal
	 * used to order the {@link StructBlock}s and {@link StructField}s of this {@link StructLocal}.
	 */
	protected List<StructBlockOrderItem> _getStructBlockOrderItems() {
		List<StructBlockOrderItem> res = structBlockOrderItems;
		if (res == null)
			throw new IllegalStateException("this.structBlockOrderItems is null! This should never happen! JDO implementation error?");

		return res;
	}

	/**
	 * Returns the struct ID of the struct.
	 * @return the struct ID.
	 */
	public StructLocalID getID() {
		return StructLocalID.create(organisationID, linkClass, structScope, structLocalScope);
	}

	/**
	 * Returns the scope of the {@link Struct} this {@link StructLocal}
	 * is associated to.
	 *
	 * @return The scope of the {@link Struct} this {@link StructLocal}
	 * is associated to.
	 */
	public String getStructScope() {
		return structScope;
	}

	/**
	 * Returns the structLocalScope of this StructLocal.
	 * Multiple {@link StructLocal}s can be derived from one {@link Struct}
	 * each with its own structLocalScope. The default structLocalScope is {@link #DEFAULT_SCOPE}.
	 *
	 * @return The structLocalScope of this StructLocal.
	 */
	public String getStructLocalScope() {
		return structLocalScope;
	}

	/**
	 * Populates the list of structBlocks of this StructLocal with the structBlocks of the given Struct.
	 * This is used by the constructor.
	 *
	 * @param struct The {@link Struct} whose blocks and fields should be referenced.
	 */
	private void populate(Struct struct) {
		localStructBlockList = new ArrayList<StructBlock>();
		propertySetValidators = new HashSet<IPropertySetValidator>(struct.getPropertySetValidators());
		for (StructBlock structBlock : struct.getStructBlocks()) {
			structBlockOrderItems.add(createStructBlockOrderItem(structBlock));
		}
	}

	/**
	 * Ensures that all referenced {@link StructBlock}s and {@link StructField}s
	 * have a corresponding order item.
	 */
	private void validateOrderItems() {
		initAllStructBlockList();
		Map<StructBlockID, StructBlockOrderItem> orderItemMap = getStructBlockOrderItemMap();
		for (StructBlock structBlock : allStructBlockList) {
			StructBlockOrderItem orderItem = orderItemMap.get(structBlock.getStructBlockIDObj());
			if (orderItem == null) {
				structBlockOrderItems.add(createStructBlockOrderItem(structBlock));
				continue;
			}
			for (StructField<? extends DataField> structField : structBlock.getStructFields()) {
				StructFieldOrderItem fieldOrderItem = orderItem.getStructFieldOrderItem(structField.getStructFieldIDObj());
				if (fieldOrderItem == null) {
					orderItem.addStructFieldOrderItem(
							new StructFieldOrderItem(orderItem, IDGenerator.nextID(StructFieldOrderItem.class), structField));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.AbstractStruct#getStruct()
	 */
	@Override
	protected Struct getStruct() {
		return struct;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#removeStructBlock(org.nightlabs.jfire.prop.StructBlock)
	 */
	@Override
	public void removeStructBlock(StructBlock structBlock) throws IllegalStructureModificationException {
		if (localStructBlockList.contains(structBlock)) {
			localStructBlockList.remove(structBlock);
			allStructBlockList.remove(structBlock);
			StructBlockOrderItem orderItem = getStructBlockOrderItemMap().get(structBlock.getStructBlockIDObj());
			if (orderItem != null)
				structBlockOrderItems.remove(orderItem);
			structBlockOrderItemMap = null;
			structBlock.setStruct(null);
		} else if (getStruct().getStructBlocks().contains(structBlock)) {
			throw new IllegalArgumentException("The given StructBlock is not defined in the StructLocal but in the Struct. Thus it cannot be removed.");
		} else {
			throw new IllegalArgumentException("This StructLocal instance does not contain the given StructBlock.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.AbstractStruct#addStructBlock(org.nightlabs.jfire.prop.StructBlock)
	 */
	@Override
	public void addStructBlock(StructBlock structBlock) throws DuplicateKeyException {
		super.addStructBlock(structBlock);
		allStructBlockList.add(structBlock);
		structBlockOrderItemMap = null;

		StructBlockOrderItem orderItem = createStructBlockOrderItem(structBlock);
		structBlockOrderItems.add(orderItem);
		structBlock.setStructBlockOrderItem(orderItem);
	}

	/**
	 * Creates a new {@link StructBlockOrderItem} for the given {@link StructBlock}
	 * and includes {@link StructFieldOrderItem}s for all its fields.
	 *
	 * @param structBlock The {@link StructBlock} to create the item for.
	 * @return A new {@link StructBlockOrderItem} for the given {@link StructBlock}.
	 */
	private StructBlockOrderItem createStructBlockOrderItem(StructBlock structBlock) {
		StructBlockOrderItem orderItem = new StructBlockOrderItem(getOrganisationID(), IDGenerator.nextID(StructBlockOrderItem.class), structBlock);
		for (StructField<?> structField : structBlock.getStructFields()) {
			orderItem.addStructFieldOrderItem(new StructFieldOrderItem(
					orderItem,
					IDGenerator.nextID(StructFieldOrderItem.class),
					structField
			));
		}
		return orderItem;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getDisplayNameParts()
	 */
	@Override
	public Collection<DisplayNamePart> getDisplayNameParts() {
		return displayNameParts;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.AbstractStruct#getLinkClassInternal()
	 */
	@Override
	protected String getLinkClassInternal() {
		return linkClass;
	}

	/**
	 * Returns the organisationID of this {@link StructLocal}
	 * This is the primary key field organisationID.
	 * @return The organisationID of this {@link StructLocal}
	 */
	@Override
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns the {@link #localStructBlockList}, which does
	 * not include the StructBlocks coming from the associated
	 * {@link Struct}.
	 * </p>
	 */
	@Override
	protected List<StructBlock> getStructBlockList() {
		return localStructBlockList;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation in {@link StructLocal} returns an <b>ordered</b> list of the {@link StructBlock} referenced by this structure.
	 * This will include all {@link StructBlock}s from the parent {@link Struct} an all
	 * locally added blocks.
	 * </p>
	 * <p>
	 * Note that this method will return a view of the list of {@link StructBlock}, it is
	 * a new {@link Collection} containing the blocks, modifying the {@link Collection} will
	 * not affect the {@link StructLocal}, modifying the {@link StructBlock}s and {@link StructField}s
	 * within will though.
	 * </p>
	 */
	@Override
	public List<StructBlock> getStructBlocks() {
		initAllStructBlockList();
		List<StructBlock> blocks = new ArrayList<StructBlock>(allStructBlockList);
		Collections.sort(blocks, getStructBlockComparator());
		return blocks;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private List<StructBlock> allStructBlockList = null;

	/**
	 * Inits the un-ordered list of all {@link StructBlock}s referenced by this {@link StructLocal}.
	 * This will include the blocks from the parent {@link Struct} and all locally added blocks.
	 */
	private void initAllStructBlockList() {
		if (allStructBlockList == null) {
			allStructBlockList = new LinkedList<StructBlock>(getStruct().getStructBlockList());
			allStructBlockList.addAll(localStructBlockList);
		}
	}

	/**
	 * @return The I18n name of this {@link StructLocal}.
	 */
	public StructLocalName getName() {
		return name;
	}

	/**
	 * @return The Map for quick access to {@link StructBlockOrderItem}s by {@link StructBlockID}.
	 */
	protected Map<StructBlockID, StructBlockOrderItem> getStructBlockOrderItemMap() {
		if (structBlockOrderItemMap == null) {
			structBlockOrderItemMap = new HashMap<StructBlockID, StructBlockOrderItem>();
			for (int i = 0; i < structBlockOrderItems.size(); i++) {
				StructBlockOrderItem orderItem = structBlockOrderItems.get(i);
				structBlockOrderItemMap.put(orderItem.getStructBlockID(), orderItem);
			}
		}
		return structBlockOrderItemMap;
	}

	/**
	 * @return A Comparator for the {@link StructBlock}s of this structure based
	 *         on the {@link #structBlockOrderItems}.
	 */
	protected Comparator<StructBlock> getStructBlockComparator() {
		return new Comparator<StructBlock>() {
			@Override
			public int compare(StructBlock block1, StructBlock block2) {
				StructBlockOrderItem orderItem1 = block1 != null ? getStructBlockOrderItemMap().get(block1.getStructBlockIDObj()) : null;
				StructBlockOrderItem orderItem2 = block2 != null ? getStructBlockOrderItemMap().get(block2.getStructBlockIDObj()) : null;
				return Integer.valueOf(_getStructBlockOrderItems().indexOf(orderItem1)).compareTo(_getStructBlockOrderItems().indexOf(orderItem2));
			}
		};
	}

	/**
	 * Moves the {@link StructBlock} referenced by the given structBlockID in the ordering
	 * of this StructLocal. The ordering is within the structLocalScope of this {@link StructLocal}.
	 * If a StructBlock with the given id can not be found in the structure this method does nothing.
	 *
	 * @param structBlockID The id of the {@link StructBlock} to move.
	 * @param orderMoveDirection The direction (up/down) to move the block.
	 */
	public void moveStructBlockInOrder(StructBlockID structBlockID, OrderMoveDirection orderMoveDirection) {
		validateOrderItems();
		StructBlockOrderItem orderItem = getStructBlockOrderItemMap().get(structBlockID);
		if (orderItem == null)
			return;
		int orgIdx = structBlockOrderItems.indexOf(orderItem);
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
			if (orgIdx == structBlockOrderItems.size() -1) {
				// can't move down, already last
				return;
			}
			swapIdx = orgIdx + 1;
			break;
		}
		Collections.swap(structBlockOrderItems, orgIdx, swapIdx);
	}

	/**
	 * Moves the StructField referenced by the given structFieldID in the order of the fields
	 * of its parent block. This ordering is within the structLocalScope of this {@link StructLocal}.
	 * If the StructField with the given id can not be found in the structure this method does nothing.
	 *
	 * @param structFieldID The id of the {@link StructField} to move.
	 * @param orderMoveDirection The direction (up/down) to move the field within its parent block.
	 */
	public void moveStructFieldInOrder(StructFieldID structFieldID, OrderMoveDirection orderMoveDirection) {
		validateOrderItems();
		StructBlockID structBlockID = StructBlockID.create(structFieldID.structBlockOrganisationID, structFieldID.structBlockID);
		StructBlockOrderItem orderItem = getStructBlockOrderItemMap().get(structBlockID);
		if (orderItem != null)
			orderItem.moveStructFieldInOrder(structFieldID, orderMoveDirection);
	}

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		table="JFireBase_Prop_StructLocal_propertySetValidators"
	 * 		dependent-element="true"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		dependentElement="true",
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_StructLocal_propertySetValidators",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<IPropertySetValidator> propertySetValidators;

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.AbstractStruct#getPropertySetValidatorSet()
	 */
	@Override
	protected Set<IPropertySetValidator> getPropertySetValidatorSet() {
		return propertySetValidators;
	}

	/**
	 * Post-processes the {@link StructLocal} after detach.
	 * If the StructLocal was detached with the {@link IStruct#FETCH_GROUP_ISTRUCT_FULL_DATA}
	 * fetch-group, the list of all StructBlocks will be inited as well as their
	 * ordering will be brought to a state where it can be edited remotely.
	 */
	@Override
	public void jdoPostDetach(Object _attached) {
		StructLocal attached = (StructLocal) _attached;
		// this is a set of String, but we don't need the type information here - so we need no cast
		Set<?> fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();

		if (fetchGroups.contains(IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA)) {
			initAllStructBlockList();
			populateStructBlockOrderItems();
		}
	}

	/**
	 * Populates the {@link StructBlock} referenced by this structure with the corresponding
	 * {@link StructBlockOrderItem} within this structure.
	 */
	private void populateStructBlockOrderItems() {
		if (allStructBlockList == null) {
			// not with full-data detached...
			return;
		}
		for (StructBlock structBlock : allStructBlockList) {
			StructBlockID structBlockID = structBlock.getStructBlockIDObj();
			StructBlockOrderItem orderItem = getStructBlockOrderItemMap().get(structBlockID);
			if (orderItem != null)
				structBlock.setStructBlockOrderItem(orderItem);
		}
	}

	/**
	 * Does nogthing.
	 */
	@Override
	public void jdoPreDetach() {
	}

	/**
	 * Get the {@link PersistenceManager} associated to an attached
	 * instance of {@link StructLocal}. If this method is invoked
	 * on an detached or new instance an {@link IllegalStateException}
	 * will be thrown.
	 *
	 * @return The {@link PersistenceManager} associated to this instance.
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance currently has no PersistenceManager assigned!");

		return pm;
	}

	/**
	 * Post-processes the {@link StructLocal} after attach.
	 * It will ensure that all referenced {@link StructBlock}s and {@link StructField}s
	 * have a corresponding order item in this structure.
	 */
	@Override
	public void jdoPostAttach(Object arg0) {
		validateOrderItems();
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void jdoPreAttach() {
////		PersistenceManager pm = getPersistenceManager();
//		// Put the local blocks into the persistent list in the correct order
//		List<StructBlock> localBlocks = new LinkedList<StructBlock>();
//		for (StructBlock block : getStructBlocks()) {
//			if (block.isLocal())
//				localBlocks.add(block);
//		}
//		localStructBlockList = localBlocks;
	}

	/**
	 * Set the {@link Struct} this {@link StructLocal} is based on.
	 * This is not intended to be invoked by anybody but the PropertyManagerBean.
	 *
	 * @param struct The {@link Struct} to set.
	 */
	protected void setStruct(Struct struct) {
		this.struct = struct;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((linkClass == null) ? 0 : linkClass.hashCode());
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((structLocalScope == null) ? 0 : structLocalScope.hashCode());
		result = prime * result + ((structScope == null) ? 0 : structScope.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final StructLocal other = (StructLocal) obj;
		if (linkClass == null) {
			if (other.linkClass != null)
				return false;
		} else if (!linkClass.equals(other.linkClass))
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID
				.equals(other.organisationID))
			return false;
		if (structLocalScope == null) {
			if (other.structLocalScope != null)
				return false;
		} else if (!structLocalScope.equals(other.structLocalScope))
			return false;
		if (structScope == null) {
			if (other.structScope != null)
				return false;
		} else if (!structScope.equals(other.structScope))
			return false;
		return true;
	}

	/**
	 * Retrieves the {@link StructLocal} referenced by the given primary-key values
	 * from the datastore associated with the given PeristenceManager.
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param organisationID The structOrganisationID of the {@link StructLocal} to retrieve.
	 * @param linkClass
	 * @param structScope
	 * @param structLocalScope
	 * @return
	 */
	public static StructLocal getStructLocal(
			PersistenceManager pm,
			String organisationID, String linkClass,
			String structScope, String structLocalScope) {
		pm.getExtent(StructLocal.class);
		return (StructLocal) pm.getObjectById(StructLocalID.create(organisationID, linkClass, structScope, structLocalScope));
	}

	public static StructLocal getStructLocal(
			PersistenceManager pm,
			String organisationID, Class<?> linkClass,
			String structScope, String structLocalScope)
	{
		return getStructLocal(pm, organisationID, linkClass.getName(), structScope, structLocalScope);
	}

//	public static StructLocal getStructLocal(Class<?> linkClass, String structScope, String structLocalScope, PersistenceManager pm) {
//		return getStructLocal(linkClass.getName(), structScope, structLocalScope, pm);
//	}

//	public static StructLocal getStructLocal(String linkClass, String structScope, String structLocalScope, PersistenceManager pm) {
//		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
//		return getStructLocal(pm, organisationID, linkClass, structScope, structLocalScope);
//	}

	public static Collection<StructLocalID> getAllStructLocalIDs(PersistenceManager pm) {
		Query q = pm.newNamedQuery(StructLocal.class, "getAllStructLocalIDs");
		return (Collection<StructLocalID>) q.execute();
	}


}
