package org.nightlabs.jfire.prop.view;

import java.util.Collections;
import java.util.LinkedList;
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

import org.nightlabs.jfire.prop.StructField;


@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_PersonTableViewerConfiguration")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups(
		@FetchGroup(
				fetchGroups={"default"},
				name=PropertySetViewerConfiguration.FETCH_GROUP_CONFIG_DATA,
				members=@Persistent(name="displayedStructFields"))
	)
public class PersonTableViewerConfiguration extends PropertySetViewerConfiguration {
	
	private static final long serialVersionUID = 20090222L;
	
	@Join
	@Persistent(
			persistenceModifier=PersistenceModifier.PERSISTENT,
			table="JFireBase_Prop_PersonTableViewerConfiguration_displayedStructFields",
			dependentElement="false")
	private List<StructField> displayedStructFields = Collections.emptyList();

	public PersonTableViewerConfiguration(String organisationID, long configurationID) {
		super(organisationID, configurationID);
	}
	
	public void setDisplayedStructFields(List<StructField> displayedStructFields) {
		this.displayedStructFields = new LinkedList<StructField>(displayedStructFields);
	}
	
	public List<StructField> getDisplayedStructFields() {
		return Collections.unmodifiableList(displayedStructFields);
	}
}
