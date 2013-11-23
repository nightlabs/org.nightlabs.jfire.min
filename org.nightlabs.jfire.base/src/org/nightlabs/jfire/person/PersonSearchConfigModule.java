package org.nightlabs.jfire.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.search.config.StructFieldSearchEditLayoutEntry;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerColumnDescriptor;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerConfiguration;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * {@link ConfigModule} that stores the configuration of the person search per use case. This
 * configuration comprises the layout of the search fields as well as the viewer that should be used
 * to display the result of the search along the viewers configuration.
 * <p>
 * This config-module stores an additional entry for the so-called quick-entry search, that is the
 * search-entry that should be used to quickly start the person-search ({@link #setQuickSearchEntry(StructFieldSearchEditLayoutEntry)}).
 * </p>
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_PersonSearchConfigModule")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	
public class PersonSearchConfigModule
extends PropertySetSearchEditLayoutConfigModule
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialiseDefaultLayout() {
		initialiseDefaultSearchFields();
		initialiseDefaultResultViewerConfig();
	}

	/**
	 * Initialises the default layout-config for the search fields that will have fields for the
	 * salutation, name, firstname, company, email and post-address related fields.
	 */
	private void initialiseDefaultSearchFields() {
		getGridLayout().setNumColumns(3);
		getGridLayout().setMakeColumnsEqualWidth(true);
		
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
	
	protected String getDefaultResultViewerIdentifier() {
		// FIXME: Well this string is very special and only known in the rcp-client, this should come from somewhere else
		return "org.nightlabs.jfire.base.ui.person.search.PersonTableViewer";
	}
	
	/**
	 * Initializes a default result-viewer-config which is a {@link PropertySetTableViewerConfiguration}.
	 */
	protected void initialiseDefaultResultViewerConfig() {
		
		String resultViewerIdentifier = getDefaultResultViewerIdentifier();
		
		PropertySetTableViewerConfiguration resultViewerConfig = new PropertySetTableViewerConfiguration(resultViewerIdentifier,
				IDGenerator.nextID(PropertySetViewerConfiguration.class));
		
		List<PropertySetTableViewerColumnDescriptor> columnDescriptors = new ArrayList<PropertySetTableViewerColumnDescriptor>();
		columnDescriptors.add(createColumnDescriptor(1, PersonStruct.PERSONALDATA_COMPANY));
		columnDescriptors.add(createColumnDescriptor(2, PersonStruct.PERSONALDATA_NAME, PersonStruct.PERSONALDATA_FIRSTNAME));
		columnDescriptors.add(createColumnDescriptor(2, PersonStruct.POSTADDRESS_POSTCODE, PersonStruct.POSTADDRESS_CITY));
		columnDescriptors.add(createColumnDescriptor(1, PersonStruct.POSTADDRESS_ADDRESS));
		columnDescriptors.add(createColumnDescriptor(1, PersonStruct.PHONE_PRIMARY));
		columnDescriptors.add(createColumnDescriptor(1, PersonStruct.INTERNET_EMAIL));
		
		resultViewerConfig.setColumnDescriptors(columnDescriptors);
		
		setResultViewerConfiguration(resultViewerIdentifier, resultViewerConfig);
		setResultViewerUiIdentifier(resultViewerIdentifier);
	}

	/**
	 * Creates a {@link PropertySetTableViewerColumnDescriptor} with the given column-weight that
	 * defines the column to display the given StructFields.
	 * 
	 * @param weight The column-weight.
	 * @param structFieldIDs The ids of the StructFields to display in the column.
	 * @return A newly created {@link PropertySetTableViewerColumnDescriptor}.
	 */
	@SuppressWarnings("unchecked")
	protected PropertySetTableViewerColumnDescriptor createColumnDescriptor(int weight, StructFieldID... structFieldIDs) {
		PropertySetTableViewerColumnDescriptor columnDescriptor = new PropertySetTableViewerColumnDescriptor(SecurityReflector
				.getUserDescriptor().getOrganisationID(), IDGenerator.nextID(PropertySetTableViewerColumnDescriptor.class));
		IStruct structLocal = PersonStruct.getPersonStructLocal(JDOHelper.getPersistenceManager(this));
		List<StructField> structFields = new ArrayList<StructField>(structFieldIDs.length);
		for (StructFieldID structFieldID : structFieldIDs) {
			try {
				structFields.add(structLocal.getStructField(structFieldID));
			} catch (PropertyException e) {
				throw new RuntimeException(e);
			}
		}
		columnDescriptor.setStructFields(structFields);
		columnDescriptor.setColumnWeight(weight);
		columnDescriptor.setColumnHeaderSeparator(", ");
		columnDescriptor.setColumnDataSeparator(", ");
		return columnDescriptor;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns {@link Person}.class
	 * </p>
	 */
	@Override
	public Class<? extends PropSearchFilter> getFilterClass() {
		return PersonSearchFilter.class;
	}
}
