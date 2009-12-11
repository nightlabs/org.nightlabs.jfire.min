package org.nightlabs.jfire.person;

import java.util.LinkedList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.prop.id.StructFieldID;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_PersonSearchConfigModule")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PersonSearchConfigModule extends ConfigModule {

	private static final long serialVersionUID = 1L;
	
	@Persistent(
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private GridLayout simplePersonSearchGridLayout;
	
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_PersonSearchConfigModule_resultViewerStructFieldIds",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<String> resultViewerStructFieldIds;

	@Override
	public void init() {
		resultViewerStructFieldIds = new LinkedList<String>();
	}
	
	/**
	 * Returns the {@link GridLayout} to be used for displaying the simple person search compositie
	 * @param gridLayout
	 */
	public void setSimplePersonSearchGridLayout(GridLayout gridLayout) {
		this.simplePersonSearchGridLayout = gridLayout;
	}
	
	public GridLayout getSimplePersonSearchGridLayout() {
		return simplePersonSearchGridLayout;
	}
	
	public void setResultViewerStructFieldIds(List<StructFieldID> structFieldIds) {
		List<String> stringIds = new LinkedList<String>();
		
		for (StructFieldID id: structFieldIds) {
			stringIds.add(id.toString());
		}
		
		this.resultViewerStructFieldIds = stringIds;
	}
	
	public List<StructFieldID> getResultViewStructFieldIDs() {
		List<StructFieldID> structFieldIds = new LinkedList<StructFieldID>();
		
		for (String stringId : resultViewerStructFieldIds) {
			structFieldIds.add((StructFieldID) ObjectIDUtil.createObjectID(stringId));
		}
		
		return structFieldIds;
	}

}
