package org.nightlabs.jfire.person;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.QueryOption;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule2;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.search.config.StructFieldSearchEditLayoutEntry;
import org.nightlabs.util.CollectionUtil;

/**
 * {@link ConfigModule} that stores the configuration of the person search per use case. This configuration
 * comprises the layout of the search fields as well as an identifier for the viewer that should be used
 * to display the result of the search along with the IDs of the {@link StructField}s to be displayed
 * in this viewer.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_PersonSearchConfigModule")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups(
		@FetchGroup(
				fetchGroups={"default"},
				name=PersonSearchConfigModule.FETCH_GROUP_QUICK_SEARCH_ENTRY,
				members=@Persistent(name="quickSearchEntry"))
	)
	
public class PersonSearchConfigModule
extends PropertySetFieldBasedEditLayoutConfigModule2
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_QUICK_SEARCH_ENTRY = "PropertySetFieldBasedEditLayoutConfigModule2.quickSearchEntry";
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructFieldSearchEditLayoutEntry quickSearchEntry;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String resultViewerUiIdentifier;
	
//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	private IResultViewerConfiguration resultViewerConfiguration;

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
	
	@Override
	public StructFieldSearchEditLayoutEntry createEditLayoutEntry(String entryType) {
		StructFieldSearchEditLayoutEntry entry = new StructFieldSearchEditLayoutEntry(
				this, IDGenerator.nextID(AbstractEditLayoutEntry.class),
				entryType);
		return entry;
	}
	
	public List<StructFieldSearchEditLayoutEntry> getStructFieldSearchEditLayoutEntries() {
		return CollectionUtil.castList(getEditLayoutEntries());
	}
	
	public void setQuickSearchEntry(StructFieldSearchEditLayoutEntry quickSearchEntry) {
		this.quickSearchEntry = quickSearchEntry;
	}
	
	public StructFieldSearchEditLayoutEntry getQuickSearchEntry() {
		return quickSearchEntry;
	};
	
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

//nullValue=NullValue.EXCEPTION,
//table="J
	private StructFieldSearchEditLayoutEntry createSingleEntry(StructFieldID structFieldID, int horSpan, MatchType matchType) {
		return createEntry(Collections.singleton(structFieldID), horSpan, PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_STRUCT_FIELD_REFERENCE, matchType);
	}
	
	private StructFieldSearchEditLayoutEntry createMultiEntry(Collection<StructFieldID> structFieldIDs, int horSpan, MatchType matchType) {
		return createEntry(structFieldIDs, horSpan, PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_MULTI_STRUCT_FIELD_REFERENCE, matchType);
	}

	private StructFieldSearchEditLayoutEntry createSeparatorEntry(int horSpan) {
		StructFieldSearchEditLayoutEntry entry = createEditLayoutEntry(EditLayoutEntry.ENTRY_TYPE_SEPARATOR);
		GridData gd = new GridData(IDGenerator.nextID(GridData.class));
		gd.setHorizontalAlignment(GridData.FILL);
		gd.setHorizontalSpan(horSpan);
		entry.setGridData(gd);
		return entry;
	}

	protected void initialiseDefaultLayout() {
		getGridLayout().setNumColumns(3);
		getGridLayout().setMakeColumnsEqualWidth(true);
		
		if (!getEditLayoutEntries().isEmpty())
			clearEditLayoutEntries();
		
		addEditLayoutEntry(createSingleEntry(PersonStruct.PERSONALDATA_SALUTATION, 1, MatchType.EQUALS));
		
		Collection<StructFieldID> structFieldIDs = new LinkedList<StructFieldID>();
		structFieldIDs.add(PersonStruct.PERSONALDATA_FIRSTNAME);
		structFieldIDs.add(PersonStruct.PERSONALDATA_NAME);
		structFieldIDs.add(PersonStruct.PERSONALDATA_COMPANY);
		final StructFieldSearchEditLayoutEntry defaultEntry = createMultiEntry(structFieldIDs, 1, MatchType.CONTAINS);
		addEditLayoutEntry(defaultEntry);
		
		addEditLayoutEntry(createSingleEntry(PersonStruct.INTERNET_EMAIL, 1, MatchType.CONTAINS));

		addEditLayoutEntry(createSeparatorEntry(3));
		
		addEditLayoutEntry(createSingleEntry(PersonStruct.POSTADDRESS_ADDRESS, 3, MatchType.CONTAINS));
		
		addEditLayoutEntry(createSingleEntry(PersonStruct.POSTADDRESS_POSTCODE, 1, MatchType.CONTAINS));
		addEditLayoutEntry(createSingleEntry(PersonStruct.POSTADDRESS_CITY, 1, MatchType.CONTAINS));
		addEditLayoutEntry(createSingleEntry(PersonStruct.POSTADDRESS_COUNTRY, 1, MatchType.CONTAINS));
		
		setQuickSearchEntry(defaultEntry);
	}
}
