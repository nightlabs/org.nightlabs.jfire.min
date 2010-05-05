package org.nightlabs.jfire.table.config;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.table.config.id.ColumnToolTipDescriptionID;

/**
 * Directly corresponds to the 'layout' part of the {@link ColumnDescriptor}, where this class indicates the
 * name of the column to be displayed .
 *
 * @author khaireel at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=ColumnToolTipDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_ColumnToolTipDescription")
@FetchGroups({
	@FetchGroup(
		name="ColumnDescriptor.colToolTipDescription",
		members={@Persistent(name="columnDescriptor"), @Persistent(name="descriptions")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ColumnToolTipDescription extends I18nText {
	private static final long serialVersionUID = -8203534621564925338L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long columnDescriptorID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ColumnDescriptor columnDescriptor;

	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_ColumnToolTipDescription_descriptions",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, String> descriptions = new HashMap<String, String>();


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	public ColumnToolTipDescription() {}

	public ColumnToolTipDescription(ColumnDescriptor columnDescriptor) {
		this.columnDescriptor = columnDescriptor;
		this.columnDescriptorID = columnDescriptor.getColumnDescriptionID();
		this.organisationID = columnDescriptor.getOrganisationID();
	}

	public ColumnToolTipDescription(String organisationID, long columnDescriptionID) {
		this.organisationID = organisationID;
		this.columnDescriptorID = columnDescriptionID;
	}


	// ------------------------------------------------------------------------ || -------------------------------------------->>
	// [Section] Access to private persistent data.
	// ------------------------------------------------------------------------ || -------------------------------------------->>
	/**
	 * @return the organisationID of this ColumnName.
	 */
	public String getOrganisationID() { return organisationID; }

	/**
	 * @return the columnDescriptorID of this ColumnName.
	 */
	public long getColumnDescriptorID() { return columnDescriptorID; }

	/**
	 * @return the {@link ColumnDescriptor} of this ColumnName.
	 */
	public ColumnDescriptor getColumnDescriptor() { return columnDescriptor; }


	// ------------------------------------------------------------------------ || -------------------------------------------->>
	// [Section] The required implements from I18nText.
	// ------------------------------------------------------------------------ || -------------------------------------------->>
	@Override
	protected String getFallBackValue(String languageID) { return columnDescriptor == null ? languageID : ""; }

	@Override
	protected Map<String, String> getI18nMap() { return descriptions; }
}
