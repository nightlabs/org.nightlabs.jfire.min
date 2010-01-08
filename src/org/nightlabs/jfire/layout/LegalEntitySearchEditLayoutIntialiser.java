package org.nightlabs.jfire.layout;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule2;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 *
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		detachable="true",
		identityType=IdentityType.APPLICATION)
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class LegalEntitySearchEditLayoutIntialiser
	extends AbstractEditLayoutConfigModuleInitialiser
{
	private static final Logger logger = Logger.getLogger(LegalEntitySearchEditLayoutIntialiser.class);

	/**
	 * @param organisationID
	 * @param configModuleInitialiserID
	 */
	public LegalEntitySearchEditLayoutIntialiser(
			String organisationID, String configModuleInitialiserID)
	{
		super(organisationID, PropertySetFieldBasedEditLayoutConfigModule2.class.getName(),
				configModuleInitialiserID, UseCaseNames.LEGAL_ENTITY_SEARCH, null);
	}

	/**
	 * @param organisationID
	 * @param configModuleInitialiserID
	 * @param priority
	 */
	public LegalEntitySearchEditLayoutIntialiser(
			String organisationID, String configModuleInitialiserID, Integer priority)
	{
		super(organisationID, PropertySetFieldBasedEditLayoutConfigModule2.class.getName(),
				configModuleInitialiserID, priority, UseCaseNames.LEGAL_ENTITY_SEARCH, null);
	}

	private PropertySetFieldBasedEditLayoutEntry2 createEntry(
			PropertySetFieldBasedEditLayoutConfigModule2 cfMod,
			StructFieldID structFieldID,
			int horSpan)
	{
		PropertySetFieldBasedEditLayoutEntry2 entry = cfMod.createEditLayoutEntry(PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_STRUCT_FIELD_REFERENCE);
		StructField<?> structField = (StructField<?>) JDOHelper.getPersistenceManager(this).getObjectById(structFieldID);
		entry.setObject(structField);
		GridData gd = new GridData(IDGenerator.nextID(GridData.class));
		gd.setHorizontalAlignment(GridData.FILL);
		gd.setGrabExcessHorizontalSpace(true);
		gd.setHorizontalSpan(horSpan);
		entry.setGridData(gd);
		return entry;
	}

	private PropertySetFieldBasedEditLayoutEntry2 createSeparatorEntry(PropertySetFieldBasedEditLayoutConfigModule2 cfMod, int horSpan) {
		PropertySetFieldBasedEditLayoutEntry2 entry = cfMod.createEditLayoutEntry(EditLayoutEntry.ENTRY_TYPE_SEPARATOR);
		GridData gd = new GridData(IDGenerator.nextID(GridData.class));
		gd.setHorizontalAlignment(GridData.FILL);
		gd.setHorizontalSpan(horSpan);
		entry.setGridData(gd);
		return entry;
	}

	@Override
	protected void initialiseLayoutConfigModule(AbstractEditLayoutConfigModule<?, ?> confMod)
	{
		PropertySetFieldBasedEditLayoutConfigModule2 cfMod =
			(PropertySetFieldBasedEditLayoutConfigModule2) confMod;

		cfMod.getGridLayout().setNumColumns(6);
		if (!cfMod.getEditLayoutEntries().isEmpty())
			cfMod.clearEditLayoutEntries();

		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.PERSONALDATA_SALUTATION, 1));
		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.PERSONALDATA_FIRSTNAME, 2));
		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.PERSONALDATA_NAME, 3));

		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.PERSONALDATA_COMPANY, 6));

		cfMod.addEditLayoutEntry(createSeparatorEntry(cfMod, 6));

		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.POSTADDRESS_ADDRESS, 2));
		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.POSTADDRESS_POSTCODE, 1));
		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.POSTADDRESS_CITY, 2));
		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.POSTADDRESS_COUNTRY, 1));

		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.PHONE_PRIMARY, 3));
		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.INTERNET_EMAIL, 3));
		cfMod.addEditLayoutEntry(createSeparatorEntry(cfMod, 6));

		cfMod.addEditLayoutEntry(createEntry(cfMod, PersonStruct.COMMENT_COMMENT, 6));
	}

}
