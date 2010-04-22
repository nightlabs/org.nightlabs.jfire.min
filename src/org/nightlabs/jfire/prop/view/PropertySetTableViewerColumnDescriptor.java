package org.nightlabs.jfire.prop.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.view.id.PropertySetTableViewerColumnDescriptorID;

/**
 * Defines the configuration of a column in a PropertySetTable in the JFire client. This can be used
 * in PropertySetViewerConfigurations.
 * 
 * TODO: Maybe later we'll need to extract an interface here so that the viewer-configurations
 * might also be configured with columns that do not reference a StructField but properties of
 * objects related to a PropertySet (like ProductTypes etc.).
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		objectIdClass=PropertySetTableViewerColumnDescriptorID.class,
		detachable="true",
		table="JFireBase_Prop_PropertyTableViewerColumnDescriptor")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups(
		@FetchGroup(
				fetchGroups={"default"},
				name=PropertySetViewerConfiguration.FETCH_GROUP_CONFIG_DATA,
				members=@Persistent(name="structFields"))
	)
public class PropertySetTableViewerColumnDescriptor implements Serializable {
	
	private static final long serialVersionUID = 20100331L;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@PrimaryKey
	private String organisationID;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@PrimaryKey
	private long descriptorID;
	
	@Join
	@Persistent(
			persistenceModifier=PersistenceModifier.PERSISTENT,
			table="JFireBase_Prop_PropertyTableViewerColumnDescriptor_structFields",
			dependentElement="false")
	private List<StructField> structFields;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int columnWeight;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String columnHeaderSeparator;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String columnDataSeparator;
	
	public PropertySetTableViewerColumnDescriptor(String organisationID, long descriptorID) {
		this.organisationID = organisationID;
		this.descriptorID = descriptorID;
		structFields = new ArrayList<StructField>();
	}
	
	/**
	 * @return The list of {@link StructField}s that should be displayed in the column.
	 */
	public List<StructField> getStructFields() {
		return Collections.unmodifiableList(structFields);
	}

	/**
	 * @param structFields The list of {@link StructField}s that should be displayed in the column.
	 */
	public void setStructFields(List<StructField> structFields) {
		this.structFields = structFields;
	}
	
//	/**
//	 * Add a StructField to the list of StuctFields displayed in the column.
//	 * @param structField The StructField to add.
//	 */
//	public void addStructField(StructField structField) {
//		structFields.add(structField);
//	}
//	
//	/**
//	 * Remove a StructField from the list of StuctFields displayed in the column.
//	 * @param structField The StructField to remove.
//	 */
//	public void removeStructField(StructField structField) {
//		structFields.remove(structField);
//	}

	/**
	 * @return The weight of the column-width compared to the others in the table.
	 */
	public int getColumnWeight() {
		return columnWeight;
	}

	/**
	 * @param columnWeight The weight of the column-width compared to the others in the table.
	 */
	public void setColumnWeight(int columnWeight) {
		this.columnWeight = columnWeight;
	}

	/**
	 * @return The string used to separate the field-ids in the column header if more than one is
	 *         set.
	 */
	public String getColumnHeaderSeparator() {
		return columnHeaderSeparator;
	}

	/**
	 * @param columnHeaderSeparator The string used to separate the field-ids in the column header
	 *            if more than one is set.
	 */
	public void setColumnHeaderSeparator(String columnHeaderSeparator) {
		this.columnHeaderSeparator = columnHeaderSeparator;
	}

	/**
	 * @return The string used to separate the field-data in each row in this column if more than
	 *         one StructField is set.
	 */
	public String getColumnDataSeparator() {
		return columnDataSeparator;
	}

	/**
	 * @param columnDataSeparator The string used to separate the field-data in each row in this
	 *            column if more than one StructField is set.
	 */
	public void setColumnDataSeparator(String columnDataSeparator) {
		this.columnDataSeparator = columnDataSeparator;
	}
}
