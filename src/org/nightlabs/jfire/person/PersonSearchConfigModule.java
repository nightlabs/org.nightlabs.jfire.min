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

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;
import org.nightlabs.jfire.prop.id.StructFieldID;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_PersonSearchConfigModule")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PersonSearchConfigModule
extends PropertySetFieldBasedEditLayoutConfigModule {

	private static final long serialVersionUID = 1L;
	
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_PersonSearchConfigModule_resultViewerStructFieldIds",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<String> resultViewerStructFieldIds;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String resultViewerUiIdentifier;

	@Override
	public void init() {
		super.init();
		resultViewerStructFieldIds = new LinkedList<String>();
	}
	
	/**
	 * Sets the IDs of the {@link StructField}s that should be displayed in the result viewer of the PersonSearch configured by this config module.
	 * @param structFieldIds The IDs of the the {@link StructField}s that shoule be displayed in the result viewer.
	 */
	public void setResultViewerStructFieldIds(List<StructFieldID> structFieldIds) {
		List<String> stringIds = new LinkedList<String>();
		
		for (StructFieldID id: structFieldIds) {
			stringIds.add(id.toString());
		}
		
		this.resultViewerStructFieldIds = stringIds;
	}
	
	/**
	 * Returns the IDs of the {@link StructField}s that should be displayed in the result viewer of the PersonSearch configured by this config module.
	 * @return the IDs of the {@link StructField}s that should be displayed in the result viewer of the PersonSearch configured by this config module.
	 */
	public List<StructFieldID> getResultViewStructFieldIDs() {
		List<StructFieldID> structFieldIds = new LinkedList<StructFieldID>();
		
		for (String stringId : resultViewerStructFieldIds) {
			structFieldIds.add((StructFieldID) ObjectIDUtil.createObjectID(stringId));
		}
		
		return structFieldIds;
	}
	
	/**
	 * Returns an identifier for the UI element that should display the results of the PersonSearch configured by this config module.
	 * @return an identifier for the UI element that should display the results of the PersonSearch configured by this config module.
	 */
	public String getResultViewerUiIdentifier() {
		return resultViewerUiIdentifier;
	}
	
	/**
	 * Sets an identifier for the UI element that should display the results of the PersonSearch configured by this config module.
	 * @param resultViewerUiIdentifier an identifier for the UI element that should display the results of the PersonSearch configured by this config module.
	 */
	public void setResultViewerUiIdentifier(String resultViewerUiIdentifier) {
		this.resultViewerUiIdentifier = resultViewerUiIdentifier;
	}
}
