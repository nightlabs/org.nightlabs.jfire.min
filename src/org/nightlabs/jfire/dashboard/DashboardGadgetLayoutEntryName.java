/**
 * 
 */
package org.nightlabs.jfire.dashboard;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;

/**
 * @author abieber
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDashboard_DashboardGadgetLayoutEntryName")
@FetchGroup(
		name = AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES, 
		members = {
				@Persistent(name = "names"), 
				@Persistent(name = "entry")}
)
public class DashboardGadgetLayoutEntryName extends I18nText {

	private static final long serialVersionUID = 20111212L;

	@PrimaryKey
	private long dashboardGadgetLayoutEntryNameID;
	
	@Persistent
	private DashboardGadgetLayoutEntry<?> entry;

	@Persistent(
			nullValue = NullValue.EXCEPTION, 
			defaultFetchGroup = "true", 
			persistenceModifier = PersistenceModifier.PERSISTENT, 
			table = "JFireDashboard_DashboardGadgetLayoutEntryName_names")
	@Join
	private Map<String, String> names;

	/**
	 * 
	 */
	public DashboardGadgetLayoutEntryName(DashboardGadgetLayoutEntry<?> entry) {
		this.entry = entry;
		this.dashboardGadgetLayoutEntryNameID = IDGenerator.nextID(DashboardGadgetLayoutEntryName.class);
		this.names = new HashMap<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return String.valueOf(entry.getEditLayoutEntryID());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}
	
	public DashboardGadgetLayoutEntry<?> getEntry() {
		return entry;
	}

	public long getDashboardGadgetLayoutEntryNameID() {
		return dashboardGadgetLayoutEntryNameID;
	}
	
}
