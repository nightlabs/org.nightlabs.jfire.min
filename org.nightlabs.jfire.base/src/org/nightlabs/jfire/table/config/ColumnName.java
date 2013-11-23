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
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.table.config.id.ColumnNameID;

/**
 * Directly corresponds to the 'layout' part of the {@link ColumnDescriptor}, where this class indicates the
 * name of the column to be displayed .
 *
 * @author khaireel at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=ColumnNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_ColumnName")
@FetchGroups({
	@FetchGroup(
		name="ColumnDescriptor.colName",
		members={@Persistent(name="columnDescriptor"), @Persistent(name="names")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ColumnName extends I18nText {
	private static final long serialVersionUID = -5483151762933475197L;

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
		table="JFireBase_ColumnName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ColumnName() {}

	protected ColumnName(ColumnDescriptor columnDescriptor) {
		this.columnDescriptor = columnDescriptor;
		this.columnDescriptorID = columnDescriptor.getColumnDescriptionID();
		this.organisationID = columnDescriptor.getOrganisationID();
	}

	protected ColumnName(String organisationID, long columnDescriptionID) {
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
	protected String getFallBackValue(String languageID) { return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(columnDescriptorID); }

	@Override
	protected Map<String, String> getI18nMap() { return names; }
}
