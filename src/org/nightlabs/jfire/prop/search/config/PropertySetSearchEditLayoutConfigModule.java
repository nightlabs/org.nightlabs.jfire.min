package org.nightlabs.jfire.prop.search.config;

import java.util.HashMap;
import java.util.Map;

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
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutConfigModule;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		table="JFireBase_Prop_PropertySetSearchEditLayoutConfigModule",
		detachable="true")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups(
			@FetchGroup(
					fetchGroups={"default"},
					name=PropertySetSearchEditLayoutConfigModule.FETCH_GROUP_RESULT_VIEWER_CONFIGURATIONS,
					members=@Persistent(name="resultViewerConfigurations"))
	)
public abstract class PropertySetSearchEditLayoutConfigModule extends PropertySetEditLayoutConfigModule {

	private static final long serialVersionUID = 20100206L;
	
	public static final String FETCH_GROUP_RESULT_VIEWER_CONFIGURATIONS = "PropertySetSearchEditLayoutConfigModule.resultViewerConfigurations";

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String resultViewerUiIdentifier;
	
	@Join
	@Persistent(
			persistenceModifier=PersistenceModifier.PERSISTENT,
			table="JFireBase_Prop_PropertySetSearchEditLayoutConfigModule_resultViewerConfigurations",
			dependentElement="true")
	private Map<String, PropertySetViewerConfiguration> resultViewerConfigurations = new HashMap<String, PropertySetViewerConfiguration>();
	
	@Override
	public void init() {
		super.init();
		initialiseDefaultLayout();
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
	
	/**
	 * Sets the {@link PropertySetViewerConfiguration} of the result viewer with the given identifier.
	 * 
	 * @param resultViewerIdentifier the identifier of the result viewer whose configuration is to be set.
	 * @param config the configuration to be set for the result viewer with the given identifier.
	 */
	public void setResultViewerConfiguration(String resultViewerIdentifier, PropertySetViewerConfiguration config) {
		resultViewerConfigurations.put(resultViewerIdentifier, config);
	}
	
	/**
	 * Returns the {@link PropertySetViewerConfiguration} for the result viewer with the given ID.
	 * 
	 * @param resultViewerIdentifier The identifier of the result viewer whose configuration is to be returned.
	 */
	public PropertySetViewerConfiguration getResultViewerConfiguration(String resultViewerIdentifier) {
		return resultViewerConfigurations.get(resultViewerIdentifier);
	}
	
	/**
	 * Returns the subclass of {@link PropertySet} whose instances' search is configured by this config module.
	 * 
	 * @return the subclass of {@link PropertySet} whose instances' search is configured by this config module.
	 */
	public abstract Class<? extends PropertySet> getCandidateClass();
	
	/**
	 * <p>Initialise the default layout for this config module retrieved by {@link #getGridLayout()} by setting its layout's properties and configuring
	 * subclasses of {@link AbstractEditLayoutEntry} using the methods {@link #addEditLayoutEntry(org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2)}
	 * and {@link #removeEditLayoutEntry(org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2)}.</p>
	 * <p>See {@link PersonSearchConfigModule} for an example.</p>
	 * 
	 * @see PersonSearchConfigModule
	 */
	protected abstract void initialiseDefaultLayout();
}
