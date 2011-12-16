/**
 * 
 */
package org.nightlabs.jfire.dashboard;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.ConfigModuleInitialiser;
import org.nightlabs.jfire.dashboard.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * Default {@link DashboardLayoutConfigModuleInitialiser}, that initialises the
 * Welcome-gadget. It uses the priority 0 so it will be always executed first.
 * 
 * @author abieber
 */
@PersistenceCapable(
		identityType = IdentityType.APPLICATION,
		detachable = "true")
@Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
public class DashboardLayoutConfigModuleInitialiser extends ConfigModuleInitialiser {

	/**
	 */
	public DashboardLayoutConfigModuleInitialiser() {
		super(IDGenerator.getOrganisationID(),
				DashboardLayoutConfigModule.class.getName(),
				"DashboardGadgetFactoryWelcome", -1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialiseConfigModule(PersistenceManager pm,ConfigModule configModule) {
		createWelcomeEntry((DashboardLayoutConfigModule) configModule);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialiseDetachedConfigModule(ConfigModule configModule) {
		createWelcomeEntry((DashboardLayoutConfigModule) configModule);
	}

	@SuppressWarnings("unchecked")
	private void createWelcomeEntry(DashboardLayoutConfigModule cfMod) {
		DashboardGadgetLayoutEntry welcomeEntry = cfMod.createEditLayoutEntry("DashboardGadgetFactoryWelcome");
		cfMod.getGridLayout().setNumColumns(2);
		cfMod.getGridLayout().setMakeColumnsEqualWidth(true);
		cfMod.addEditLayoutEntry(welcomeEntry);
		welcomeEntry.getGridData().setHorizontalSpan(2);
		welcomeEntry.getGridData().setHorizontalAlignment(GridData.FILL);
		welcomeEntry.getGridData().setGrabExcessHorizontalSpace(true);
		initializeWelcomeGadgetName(welcomeEntry.getEntryName());
	}

	public static void initializeWelcomeGadgetName(I18nText gadgetName) {		
		gadgetName.readFromProperties(
				Messages.BUNDLE_NAME, 
				DashboardLayoutConfigModuleInitialiser.class.getClassLoader(), 
				DashboardLayoutConfigModuleInitialiser.class.getName() + ".welcomeGadget.title");
	}

}
