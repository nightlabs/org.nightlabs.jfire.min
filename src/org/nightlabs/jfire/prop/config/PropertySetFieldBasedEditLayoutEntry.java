package org.nightlabs.jfire.prop.config;

import java.io.Serializable;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @deprecated use {@link PropertySetFieldBasedEditLayoutEntry2} instead!
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntryID"
 *		detachable="true"
 *		table="JFireBase_Prop_PropertySetFieldBasedEditLayoutEntry"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="propertySetFieldBasedEditLayoutEntryID"
 *
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutEntry.gridData" fetch-groups="default" fields="gridData"
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutEntry.structField" fetch-groups="default" fields="structField"
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutConfigModule.editLayoutEntries" fields="configModule"
 */
@Deprecated
@PersistenceCapable(
	objectIdClass=PropertySetFieldBasedEditLayoutEntryID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_PropertySetFieldBasedEditLayoutEntry")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySetFieldBasedEditLayoutEntry.FETCH_GROUP_GRID_DATA,
		members=@Persistent(name="gridData")),
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySetFieldBasedEditLayoutEntry.FETCH_GROUP_STRUCT_FIELD,
		members=@Persistent(name="structField")),
	@FetchGroup(
		name="PropertySetFieldBasedEditLayoutConfigModule.editLayoutEntries",
		members=@Persistent(name="configModule"))
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PropertySetFieldBasedEditLayoutEntry implements Serializable, DetachCallback, AttachCallback, StoreCallback {

	private static final long serialVersionUID = 20090120L;

	public static final String ENTRY_TYPE_STRUCT_FIELD_REFERENCE = "structFieldReference";
	public static final String ENTRY_TYPE_SEPARATOR = "separator";

	public static final String FETCH_GROUP_GRID_DATA = "PropertySetFieldBasedEditLayoutEntry.gridData";
	public static final String FETCH_GROUP_STRUCT_FIELD = "PropertySetFieldBasedEditLayoutEntry.structField";
	public static final String FETCH_GROUP_STRUCT_FIELD_ID = "PropertySetFieldBasedEditLayoutEntry.structFieldID";


	public static class FieldName {
		public static final String gridData = "gridData";
		public static final String structField = "structField";
		public static final String structFieldID = "structFieldID";
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long propertySetFieldBasedEditLayoutEntryID;

	/**
 	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PropertySetFieldBasedEditLayoutConfigModule configModule;

	/**
 	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String entryType;

	/**
 	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private GridData gridData;

	/**
 	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructField<?> structField;

	/**
 	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private StructFieldID structFieldID;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected PropertySetFieldBasedEditLayoutEntry() {
	}

	public PropertySetFieldBasedEditLayoutEntry(
			PropertySetFieldBasedEditLayoutConfigModule configModule,
			long propertySetFieldBasedEditLayoutEntryID,
			String entryType) {
		this.configModule = configModule;
		this.propertySetFieldBasedEditLayoutEntryID = propertySetFieldBasedEditLayoutEntryID;
		if (entryType == null)
			throw new IllegalArgumentException("entryType must not be null");
		this.entryType = entryType;
	}

	public long getPropertySetFieldBasedEditLayoutEntryID() {
		return propertySetFieldBasedEditLayoutEntryID;
	}

	public GridData getGridData() {
		return gridData;
	}

	public void setGridData(GridData gridData) {
		this.gridData = gridData;
	}

	public StructField<?> getStructField() {
		return structField;
	}

	public void setStructField(StructField<?> structField) {
		this.structField = structField;
	}

	public StructFieldID getStructFieldID() {
		return structFieldID;
	}

	public void setStructFieldID(StructFieldID structFieldID) {
		this.structFieldID = structFieldID;
	}

	public String getEntryType() {
		return entryType;
	}

	@Override
	public void jdoPostDetach(Object attached) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		if (pm.getFetchPlan().getGroups().contains(FETCH_GROUP_STRUCT_FIELD_ID)) {
			StructField<?> pStructField = ((PropertySetFieldBasedEditLayoutEntry) attached).getStructField();
			if (pStructField == null)
				this.structFieldID = null;
			else
				this.structFieldID = (StructFieldID) JDOHelper.getObjectId(pStructField);
		}
	}

	@Override
	public void jdoPreDetach() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		Set<?> groups = pm.getFetchPlan().getGroups();
		if (groups.contains(FETCH_GROUP_STRUCT_FIELD_ID) && groups.contains(FETCH_GROUP_STRUCT_FIELD))
			throw new IllegalStateException("For consistency reasons you can't detach both " + FETCH_GROUP_STRUCT_FIELD + " and " + FETCH_GROUP_STRUCT_FIELD_ID + " of an " + this.getClass().getSimpleName());
	}

	@Override
	public void jdoPostAttach(Object detached) {
		PropertySetFieldBasedEditLayoutEntry detachedEntry = (PropertySetFieldBasedEditLayoutEntry) detached;
		if (detachedEntry.structFieldID == null)
			return;
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		this.structField = (StructField<?>) pm.getObjectById(detachedEntry.structFieldID);
	}

	@Override
	public void jdoPreAttach() {
	}

	@Override
	public void jdoPreStore() {
		jdoPostAttach(this);
	}

}
