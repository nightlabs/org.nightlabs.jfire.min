package org.nightlabs.jfire.prop.search.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.QueryOption;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutEntry;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * Base-class that defines the ui-layout of the search fields that should be used for a PropertySet
 * search for a certain use-case along with the type of viewer that should be used to display the
 * search results in this use-case as well as a individual configuration for that viewer.
 * <p>
 * This class constrains the type of EditLayoutEntries used to {@link StructFieldSearchEditLayoutEntry}
 * </p>
 * <p>
 * Subclasses have to constrain the FilterClass that configurations of that type can handle and
 * provide an intial layout. This class provides helper-methods to create the intial layout. 
 * </p>
 * 
 * @author Tobias Langner
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		table="JFireBase_Prop_PropertySetSearchEditLayoutConfigModule",
		detachable="true")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups({
			@FetchGroup(
					fetchGroups={"default"},
					name=PropertySetSearchEditLayoutConfigModule.FETCH_GROUP_RESULT_VIEWER_CONFIGURATIONS,
					members=@Persistent(name="resultViewerConfigurations")),
			@FetchGroup(
					fetchGroups={"default"},
					name=PropertySetSearchEditLayoutConfigModule.FETCH_GROUP_QUICK_SEARCH_ENTRY,
					members=@Persistent(name="quickSearchEntry"))
	})
public abstract class PropertySetSearchEditLayoutConfigModule extends PropertySetEditLayoutConfigModule {

	private static final long serialVersionUID = 20100329L;
	
	public static final String FETCH_GROUP_RESULT_VIEWER_CONFIGURATIONS = "PropertySetSearchEditLayoutConfigModule.resultViewerConfigurations";
	public static final String FETCH_GROUP_QUICK_SEARCH_ENTRY = "PropertySetSearchEditLayoutConfigModule.quickSearchEntry";

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String resultViewerUiIdentifier;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructFieldSearchEditLayoutEntry quickSearchEntry;
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates an empty {@link StructFieldSearchEditLayoutEntry}.
	 * </p>
	 */
	@Override
	public StructFieldSearchEditLayoutEntry createEditLayoutEntry(String entryType) {
		StructFieldSearchEditLayoutEntry entry = new StructFieldSearchEditLayoutEntry(
				this, IDGenerator.nextID(AbstractEditLayoutEntry.class),
				entryType);
		return entry;
	}

	/**
	 * @return The entries for the search-fields configured by this config-module.
	 */
	public List<StructFieldSearchEditLayoutEntry> getStructFieldSearchEditLayoutEntries() {
		return CollectionUtil.castList(getEditLayoutEntries());
	}

	/** Map storing all {@link PropertySetViewerConfiguration}s made for this config module per viewer-id */
	@Join
	@Persistent(
			persistenceModifier=PersistenceModifier.PERSISTENT,
			table="JFireBase_Prop_PropertySetSearchEditLayoutConfigModule_resultViewerConfigurations",
			dependentElement="true")
	private Map<String, PropertySetViewerConfiguration> resultViewerConfigurations = new HashMap<String, PropertySetViewerConfiguration>();

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to sub-class implementaton of {@link #initialiseDefaultLayout()}.
	 * </p>
	 */
	@Override
	public void init() {
		super.init();
		initialiseDefaultLayout();
	}

	/**
	 * Returns an identifier for the UI element that should display the results of the PersonSearch
	 * configured by this config module.
	 * 
	 * @return an identifier for the UI element that should display the results of the PersonSearch
	 *         configured by this config module.
	 */
	public String getResultViewerUiIdentifier() {
		return resultViewerUiIdentifier;
	}

	/**
	 * Sets an identifier for the UI element that should display the results of the PersonSearch
	 * configured by this config module.
	 * 
	 * @param resultViewerUiIdentifier an identifier for the UI element that should display the
	 *            results of the PersonSearch configured by this config module.
	 */
	public void setResultViewerUiIdentifier(String resultViewerUiIdentifier) {
		this.resultViewerUiIdentifier = resultViewerUiIdentifier;
	}

	/**
	 * Sets the {@link PropertySetViewerConfiguration} of the result viewer with the given
	 * identifier.
	 * 
	 * @param resultViewerIdentifier the identifier of the result viewer whose configuration is to
	 *            be set.
	 * @param config the configuration to be set for the result viewer with the given identifier.
	 */
	public void setResultViewerConfiguration(String resultViewerIdentifier, PropertySetViewerConfiguration config) {
		resultViewerConfigurations.put(resultViewerIdentifier, config);
	}

	/**
	 * Returns the {@link PropertySetViewerConfiguration} for the result viewer with the given ID.
	 * 
	 * @param resultViewerIdentifier The identifier of the result viewer whose configuration is to
	 *            be returned.
	 */
	public PropertySetViewerConfiguration getResultViewerConfiguration(String resultViewerIdentifier) {
		return resultViewerConfigurations.get(resultViewerIdentifier);
	}

	/**
	 * Returns the set of ids of all the {@link PropertySetViewerConfiguration}s registered in this
	 * config-module.
	 * 
	 * @return the set of ids of all the {@link PropertySetViewerConfiguration}s registered in this
	 *         config-module.
	 */
	public Set<String> getResultViewerConfigurationIDs() {
		return Collections.unmodifiableSet(resultViewerConfigurations.keySet());
	}

	/**
	 * Remove the {@link PropertySetViewerConfiguration} with the given id from this config-module.
	 * <p>
	 * Note, that if the {@link PropertySetViewerConfiguration} for the viewer primary selected for
	 * this config-module is removed, that {@link #resultViewerUiIdentifier} is set to one of the
	 * ids still known to this config-module.
	 * </p>
	 * 
	 * @param resultViewerIdentifier The id of the configuration to remove.
	 */
	public void removeResultViewerConfiguration(String resultViewerIdentifier) {
		resultViewerConfigurations.remove(resultViewerIdentifier);
		if (Util.equals(resultViewerIdentifier, resultViewerUiIdentifier)) {
			if (resultViewerConfigurations.size() > 0) {
				resultViewerUiIdentifier = getResultViewerConfigurationIDs().iterator().next();
			}
		}
	}

	/**
	 * Sets the {@link StructFieldSearchEditLayoutEntry} that should be used to build the so-called
	 * person-quick-search. 
	 * 
	 * @param quickSearchEntry The entry for the person-quick-search to set.
	 */
	public void setQuickSearchEntry(StructFieldSearchEditLayoutEntry quickSearchEntry) {
		this.quickSearchEntry = quickSearchEntry;
	}

	/**
	 * Returns the {@link StructFieldSearchEditLayoutEntry} that should be used to build the
	 * so-called person-quick-search. The person-quick-search is used to quickly start or perform
	 * the person search instead of using a ui with all fields linked in this configuration.
	 * 
	 * @param quickSearchEntry The entry for the person-quick-search to set.
	 * @return
	 */
	public StructFieldSearchEditLayoutEntry getQuickSearchEntry() {
		return quickSearchEntry;
	};

	/**
	 * Returns the subclass of {@link PropSearchFilter} whose result a viewer configured with
	 * instances of this config module can display.
	 * 
	 * @return the subclass of {@link PropSearchFilter} whose result a viewer configured with
	 *         instances of this config module can display.
	 */
	public abstract Class<? extends PropSearchFilter> getFilterClass();

	/**
	 * <p>
	 * Initialise the default layout for this config module retrieved by {@link #getGridLayout()} by
	 * setting its layout's properties and configuring subclasses of {@link AbstractEditLayoutEntry}
	 * using the methods
	 * {@link #addEditLayoutEntry(org.nightlabs.jfire.prop.config.PropertySetEditLayoutEntry)} and
	 * {@link #removeEditLayoutEntry(org.nightlabs.jfire.prop.config.PropertySetEditLayoutEntry)}.
	 * </p>
	 * <p>
	 * See {@link PersonSearchConfigModule} for an example.
	 * </p>
	 * 
	 * @see PersonSearchConfigModule
	 */
	protected abstract void initialiseDefaultLayout();
	
	

	/**
	 * Creates an entry for the given parameters. This is a helper method used to create entries for
	 * the initial layout (see {@link #initialiseDefaultLayout()}).
	 */
	@SuppressWarnings("unchecked")
	private StructFieldSearchEditLayoutEntry createEntry(Collection<StructFieldID> structFieldIDs, int horSpan, String entryType, MatchType matchType) {
		StructFieldSearchEditLayoutEntry entry = createEditLayoutEntry(entryType);
		Set<StructField> structFields = NLJDOHelper.getObjectSet(JDOHelper.getPersistenceManager(this), structFieldIDs, null, (QueryOption[]) null);
		entry.setObject(structFields);
		entry.setMatchType(matchType);
		GridData gd = new GridData(IDGenerator.nextID(GridData.class));
		gd.setHorizontalAlignment(GridData.FILL);
		gd.setGrabExcessHorizontalSpace(true);
		gd.setHorizontalSpan(horSpan);
		entry.setGridData(gd);
		return entry;
	}

	/**
	 * Helper method for {@link #initialiseDefaultLayout()} that creates a search entry with a
	 * single StructFieldID.
	 */
	protected StructFieldSearchEditLayoutEntry createSingleEntry(StructFieldID structFieldID, int horSpan, MatchType matchType) {
		return createEntry(Collections.singleton(structFieldID), horSpan, PropertySetEditLayoutEntry.ENTRY_TYPE_STRUCT_FIELD_REFERENCE, matchType);
	}
	
	/**
	 * Helper method for {@link #initialiseDefaultLayout()} that creates a search entry with a
	 * multiple StructFieldIDs.
	 */
	protected StructFieldSearchEditLayoutEntry createMultiEntry(Collection<StructFieldID> structFieldIDs, int horSpan, MatchType matchType) {
		return createEntry(structFieldIDs, horSpan, PropertySetEditLayoutEntry.ENTRY_TYPE_MULTI_STRUCT_FIELD_REFERENCE, matchType);
	}

	/**
	 * Helper method for {@link #initialiseDefaultLayout()} that creates a separator entry.
	 */
	protected StructFieldSearchEditLayoutEntry createSeparatorEntry(int horSpan) {
		StructFieldSearchEditLayoutEntry entry = createEditLayoutEntry(EditLayoutEntry.ENTRY_TYPE_SEPARATOR);
		GridData gd = new GridData(IDGenerator.nextID(GridData.class));
		gd.setHorizontalAlignment(GridData.FILL);
		gd.setHorizontalSpan(horSpan);
		entry.setGridData(gd);
		return entry;
	}
	
}
