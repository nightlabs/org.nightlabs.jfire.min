package org.nightlabs.jfire.prop;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.ConfigModuleInitialiser;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase;
import org.nightlabs.jfire.prop.config.id.PropertySetFieldBasedEditLayoutUseCaseID;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModuleInitialiser"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class PropertySetFieldBasedEditLayoutCfModIntialiser extends ConfigModuleInitialiser {

	private static final Logger logger = Logger.getLogger(PropertySetFieldBasedEditLayoutCfModIntialiser.class);

	/**
	 * @param organisationID
	 * @param configModuleClassName
	 * @param configModuleInitialiserID
	 */
	public PropertySetFieldBasedEditLayoutCfModIntialiser(
			String organisationID, String configModuleClassName,
			String configModuleInitialiserID) {
		super(organisationID, configModuleClassName, configModuleInitialiserID);
	}

	/**
	 * @param organisationID
	 * @param configModuleClassName
	 * @param configModuleInitialiserID
	 * @param priority
	 */
	public PropertySetFieldBasedEditLayoutCfModIntialiser(

			String organisationID, String configModuleClassName,
			String configModuleInitialiserID, Integer priority) {
		super(organisationID, configModuleClassName, configModuleInitialiserID,
				priority);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModuleInitialiser#initialiseConfigModule(javax.jdo.PersistenceManager, org.nightlabs.jfire.config.ConfigModule)
	 */
	@Override
	public void initialiseConfigModule(PersistenceManager pm, ConfigModule configModule) {
		if (!(configModule instanceof PropertySetFieldBasedEditLayoutConfigModule))
			return;
		PropertySetFieldBasedEditLayoutConfigModule cfMod = (PropertySetFieldBasedEditLayoutConfigModule) configModule;

		PropertySetFieldBasedEditLayoutUseCaseID useCaseID = PropertySetFieldBasedEditLayoutUseCaseID.create(getOrganisationID(), cfMod.getCfModID());
		logger.debug("Trying to lookup PropertySetFieldBasedEditLayoutUseCase " + useCaseID);
		PropertySetFieldBasedEditLayoutUseCase useCase = null;
		try {
			useCase = (PropertySetFieldBasedEditLayoutUseCase) pm.getObjectById(useCaseID);
		} catch (JDOObjectNotFoundException e) {
			logger.warn("Could not find PropertySetFieldBasedEditLayoutUseCase " + useCaseID + ", will skip initialisation of ConfigModule " + configModule);
			return;
		}
		if (!Person.class.isAssignableFrom(useCase.getStructLocal().getLinkClass())) {
			logger.debug("Initialiser invoked for unsupported use-case (not for Person-data) " + useCaseID + ".");
			return;
		}

		cfMod.getGridLayout().setNumColumns(6);

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

	private PropertySetFieldBasedEditLayoutEntry createEntry(PropertySetFieldBasedEditLayoutConfigModule cfMod, StructFieldID structFieldID, int horSpan) {
		PropertySetFieldBasedEditLayoutEntry entry = cfMod.createEditLayoutEntry(PropertySetFieldBasedEditLayoutEntry.ENTRY_TYPE_STRUCT_FIELD_REFERENCE);
		StructField<?> structField = (StructField<?>) JDOHelper.getPersistenceManager(this).getObjectById(structFieldID);
		entry.setStructField(structField);
		GridData gd = new GridData(IDGenerator.nextID(GridData.class));
		gd.setHorizontalAlignment(GridData.FILL);
		gd.setGrabExcessHorizontalSpace(true);
		gd.setHorizontalSpan(horSpan);
		entry.setGridData(gd);
		return entry;
	}

	private PropertySetFieldBasedEditLayoutEntry createSeparatorEntry(PropertySetFieldBasedEditLayoutConfigModule cfMod, int horSpan) {
		PropertySetFieldBasedEditLayoutEntry entry = cfMod.createEditLayoutEntry(EditLayoutEntry.ENTRY_TYPE_SEPARATOR);
		GridData gd = new GridData(IDGenerator.nextID(GridData.class));
		gd.setHorizontalAlignment(GridData.FILL);
		gd.setHorizontalSpan(horSpan);
		entry.setGridData(gd);
		return entry;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModuleInitialiser#initialiseDetachedConfigModule(org.nightlabs.jfire.config.ConfigModule)
	 */
	@Override
	public void initialiseDetachedConfigModule(ConfigModule configModule) {
	}

}
