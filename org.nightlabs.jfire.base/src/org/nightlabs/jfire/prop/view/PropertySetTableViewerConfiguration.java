package org.nightlabs.jfire.prop.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Configuration used to configure the columns of a PropertySetViewer of type
 * PropertySetTableViewer, it stores instances of {@link PropertySetTableViewerColumnDescriptor}s.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_PropertySetTableViewerConfiguration")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups(
		@FetchGroup(
				fetchGroups={"default"},
				name=PropertySetViewerConfiguration.FETCH_GROUP_CONFIG_DATA,
				members=@Persistent(name="columnDescriptors"))
	)
public class PropertySetTableViewerConfiguration extends PropertySetViewerConfiguration {
	
	private static final long serialVersionUID = 20090331L;
	
	@Join
	@Persistent(
			persistenceModifier=PersistenceModifier.PERSISTENT,
			table="JFireBase_Prop_PersonTableViewerConfiguration_columnDescriptors",
			dependentElement="false")
	private List<PropertySetTableViewerColumnDescriptor> columnDescriptors;

	public PropertySetTableViewerConfiguration(String organisationID, long configurationID) {
		super(organisationID, configurationID);
		columnDescriptors = new ArrayList<PropertySetTableViewerColumnDescriptor>();
	}
	
	public List<PropertySetTableViewerColumnDescriptor> getColumnDescriptors() {
		return Collections.unmodifiableList(columnDescriptors);
	}
	
//	public void addColumnDescriptor(PropertySetTableViewerColumnDescriptor columnDescriptor) {
//		columnDescriptors.add(columnDescriptor);
//	}
//	
//	public void removeColumnDescriptor(PropertySetTableViewerColumnDescriptor columnDescriptor) {
//		columnDescriptors.remove(columnDescriptor);
//	}
	
	public void setColumnDescriptors(List<PropertySetTableViewerColumnDescriptor> columnDescriptors) {
		this.columnDescriptors = columnDescriptors;
	}
	
	/**
	 * @return All {@link StructFieldID}s used in this config.
	 */
	@SuppressWarnings("unchecked")
	public Collection<StructFieldID> getAllStructFieldIDs() {
		Collection<StructFieldID> allStructFieldIDs = new LinkedList<StructFieldID>();
		for (PropertySetTableViewerColumnDescriptor columnDescriptor : getColumnDescriptors()) {
			for (StructField structField : columnDescriptor.getStructFields()) {
				allStructFieldIDs.add((StructFieldID) JDOHelper.getObjectId(structField));
			}
		}
		return allStructFieldIDs;
	}
	
}
