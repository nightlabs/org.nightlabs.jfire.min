package org.nightlabs.jfire.prop.config;

import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Base implementation for FieldBased UI of PropertySets.
 *
 * @see AbstractEditLayoutEntry
 * @see EditLayoutEntry
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Layout_PropertySetFieldBasedEditLayoutEntry")
@FetchGroups({
//	@FetchGroup(
//		fetchGroups={"default"},
//		name=PropertySetFieldBasedEditLayoutEntry2.FETCH_GROUP_GRID_DATA,
//		members=@Persistent(name="gridData")),
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySetFieldBasedEditLayoutEntry2.FETCH_GROUP_STRUCT_FIELD,
		members=@Persistent(name="object"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PropertySetFieldBasedEditLayoutEntry2
	extends AbstractEditLayoutEntry<StructField<?>>
	implements DetachCallback, AttachCallback, StoreCallback
{
	private static final long serialVersionUID = 20100108L;

	public static final String ENTRY_TYPE_STRUCT_FIELD_REFERENCE = "structFieldReference";
	public static final String FETCH_GROUP_STRUCT_FIELD = "PropertySetFieldBasedEditLayoutEntry.structField";
	public static final String FETCH_GROUP_STRUCT_FIELD_ID = "PropertySetFieldBasedEditLayoutEntry.structFieldID";

	public static class FieldName {
		public static final String structFieldID = "structFieldID";
	}

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private StructFieldID structFieldID;
	@Persistent
	private StructField<?> structField;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected PropertySetFieldBasedEditLayoutEntry2() {
	}

	public PropertySetFieldBasedEditLayoutEntry2(
			PropertySetFieldBasedEditLayoutConfigModule2 configModule,
			long entryID,
			String entryType)
	{
		super(configModule, entryID, entryType);
	}

	public StructFieldID getStructFieldID() {
		return structFieldID;
	}

	public void setStructFieldID(StructFieldID structFieldID) {
		this.structFieldID = structFieldID;
	}

	@Override
	public void jdoPostDetach(Object attached) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		if (pm.getFetchPlan().getGroups().contains(FETCH_GROUP_STRUCT_FIELD_ID)) {
			StructField<?> pStructField = ((PropertySetFieldBasedEditLayoutEntry2) attached).getObject();
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
		PropertySetFieldBasedEditLayoutEntry2 detachedEntry = (PropertySetFieldBasedEditLayoutEntry2) detached;
		if (detachedEntry.structFieldID == null)
			return;
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		setObject((StructField<?>) pm.getObjectById(detachedEntry.structFieldID));
	}

	@Override
	public void jdoPreAttach() {
	}

	@Override
	public void jdoPreStore() {
		jdoPostAttach(this);
	}

	@Override
	public StructField<?> getObject()
	{
		return structField;
	}

	@Override
	public void setObject(StructField<?> object)
	{
		this.structField = object;
	}

}
