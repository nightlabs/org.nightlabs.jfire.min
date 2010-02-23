package org.nightlabs.jfire.prop.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.prop.StructField;

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
	table="JFireBase_Prop_PropertySetFieldBasedEditLayoutEntry2")
@FetchGroups({
//	@FetchGroup(
//		fetchGroups={"default"},
//		name=PropertySetFieldBasedEditLayoutEntry2.FETCH_GROUP_GRID_DATA,
//		members=@Persistent(name="gridData")),
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySetFieldBasedEditLayoutEntry2.FETCH_GROUP_STRUCT_FIELDS,
		members=@Persistent(name="structFields"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PropertySetFieldBasedEditLayoutEntry2
	extends AbstractEditLayoutEntry<Set<StructField>>
{
	private static final long serialVersionUID = 20100108L;

	public static final String ENTRY_TYPE_STRUCT_FIELD_REFERENCE = "structFieldReference";
	public static final String ENTRY_TYPE_MULTI_STRUCT_FIELD_REFERENCE = "multiStructFieldReference";
	public static final String FETCH_GROUP_STRUCT_FIELDS = "PropertySetFieldBasedEditLayoutEntry.structFields";

	public static class FieldName {
		public static final String structFieldID = "structFieldID";
	}
	
	@Join
	@Persistent(
			table="JFireBase_Prop_PropertySetFieldBasedEditLayoutEntry_structFields",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<StructField> structFields;
	
	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected PropertySetFieldBasedEditLayoutEntry2() {
	}

	public PropertySetFieldBasedEditLayoutEntry2(
			PropertySetEditLayoutConfigModule configModule,
			long entryID,
			String entryType)
	{
		super(configModule, entryID, entryType);
	}
	
	public Set<StructField> getStructFields() {
		return Collections.unmodifiableSet(structFields);
	}
	
	public void setStructFields(Set<StructField> structFields) {
		 this.structFields = new HashSet<StructField>(structFields);
	}

	@Override
	public Set<StructField> getObject() {
		return getStructFields();
	}

	@Override
	public void setObject(Set<StructField> object) {
		setStructFields(object);
	}

	@Override
	public String getName() {
		if (getEntryType().equals(EditLayoutEntry.ENTRY_TYPE_SEPARATOR)) {
			return "Separator"; //$NON-NLS-1$
		}
		
		StringBuilder name = new StringBuilder();
		
		for (StructField field : getStructFields()) {
			name.append(field.getName().getText()).append(", ");
		}

		name.delete(name.length()-2, name.length());

		return name.toString();
	}
}
