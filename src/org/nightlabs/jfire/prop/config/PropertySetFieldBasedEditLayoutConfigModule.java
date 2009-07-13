/**
 * 
 */
package org.nightlabs.jfire.prop.config;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.idgenerator.IDGenerator;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireBase_Prop_PropertySetFieldBasedEditLayoutConfigModule"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutConfigModule.gridLayout" fields="gridLayout"
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutConfigModule.editLayoutEntries" fields="editLayoutEntries"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_PropertySetFieldBasedEditLayoutConfigModule")
@FetchGroups({
	@FetchGroup(
		name=PropertySetFieldBasedEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT,
		members=@Persistent(name="gridLayout")),
	@FetchGroup(
		name=PropertySetFieldBasedEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES,
		members=@Persistent(name="editLayoutEntries"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PropertySetFieldBasedEditLayoutConfigModule extends ConfigModule {

	private static final long serialVersionUID = 20090119L;
	
	public static final String FETCH_GROUP_GRID_LAYOUT = "PropertySetFieldBasedEditLayoutConfigModule.gridLayout";
	public static final String FETCH_GROUP_EDIT_LAYOUT_ENTRIES = "PropertySetFieldBasedEditLayoutConfigModule.editLayoutEntries";

	public static class FieldName {
		public static final String gridLayout = "gridLayout";
		public static final String editLayoutEntries = "editLayoutEntries";
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private GridLayout gridLayout;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry"
	 *		mapped-by="configModule"
	 *		null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		mappedBy="configModule",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<PropertySetFieldBasedEditLayoutEntry> editLayoutEntries;
	
	/**
	 * Constructs a new {@link PropertySetFieldBasedEditLayoutConfigModule}
	 */
	public PropertySetFieldBasedEditLayoutConfigModule() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModule#init()
	 */
	@Override
	public void init() {
		gridLayout = new GridLayout(IDGenerator.nextID(GridLayout.class));
		editLayoutEntries = new LinkedList<PropertySetFieldBasedEditLayoutEntry>();
	}
	
	public GridLayout getGridLayout() {
		return gridLayout;
	}
	
	public List<PropertySetFieldBasedEditLayoutEntry> getEditLayoutEntries() {
		return Collections.unmodifiableList(editLayoutEntries);
	}
	
	public PropertySetFieldBasedEditLayoutEntry createEditLayoutEntry(String entryType) {
		PropertySetFieldBasedEditLayoutEntry entry = new PropertySetFieldBasedEditLayoutEntry(this, IDGenerator.nextID(PropertySetFieldBasedEditLayoutEntry.class), entryType);
		return entry;
	}
	
	public void addEditLayoutEntry(PropertySetFieldBasedEditLayoutEntry editLayoutEntry) {
		editLayoutEntries.add(editLayoutEntry);
	}
	
	public boolean removeEditLayoutEntry(PropertySetFieldBasedEditLayoutEntry editLayoutEntry) {
		return editLayoutEntries.remove(editLayoutEntry);
	}
	
	public boolean moveEditLayoutEntryUp(PropertySetFieldBasedEditLayoutEntry editLayoutEntry) {
		int idx = editLayoutEntries.indexOf(editLayoutEntry);
		if (idx <= 0)
			return false;
		Collections.swap(editLayoutEntries, idx-1, idx);
		return true;
	}
	
	public boolean moveEditLayoutEntryDown(PropertySetFieldBasedEditLayoutEntry editLayoutEntry) {
		int idx = editLayoutEntries.indexOf(editLayoutEntry);
		if (idx < 0 || idx >= editLayoutEntries.size() -1)
			return false;
		Collections.swap(editLayoutEntries, idx, idx+1);
		return true;
	}
}
