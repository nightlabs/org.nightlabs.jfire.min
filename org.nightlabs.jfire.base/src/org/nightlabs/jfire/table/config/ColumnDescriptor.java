package org.nightlabs.jfire.table.config;

import java.io.Serializable;
import java.util.Set;

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
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.table.config.id.ColumnDescriptorID;

/**
 * A {@link IColumnDescriptor} for use in a configurable IssueTable.
 *
 * @author khaireel at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=ColumnDescriptorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_ColumnDescriptor")
@FetchGroups({
	@FetchGroup(
		name=ColumnDescriptor.FETCH_GROUP_COL_NAME,
		members=@Persistent(name="colName")),
	@FetchGroup(
		name=ColumnDescriptor.FETCH_GROUP_COL_TOOLTIP_DESCRIPTION,
		members=@Persistent(name="colToolTipDescription")),
	@FetchGroup(
		name=ColumnDescriptor.FETCH_GROUP_COL_FETCH_GROUPS,
		members=@Persistent(name="colFetchGroups")),
	@FetchGroup(
		name=ColumnDescriptor.FETCH_GROUP_COL_FIELD_NAMES,
		members=@Persistent(name="colFieldNames"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ColumnDescriptor implements IColumnContentDescriptor, Serializable {
	private static final long serialVersionUID = -4299219049958226381L;

	public static final String FETCH_GROUP_COL_NAME = "ColumnDescriptor.colName";
	public static final String FETCH_GROUP_COL_TOOLTIP_DESCRIPTION = "ColumnDescriptor.colToolTipDescription";
	public static final String FETCH_GROUP_COL_FETCH_GROUPS = "ColumnDescriptor.colFetchGroups";
	public static final String FETCH_GROUP_COL_FIELD_NAMES = "ColumnDescriptor.colFieldNames";

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long columnDescriptionID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int colWeight;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int colStyle; // e.g. SWT.LEFT, SWT.RIGHT, etc.

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean isMoveable;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean isResizable;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ColumnContentProperty colContentProperty;

	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_ColumnDescriptor_colFetchGroups",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String[] colFetchGroups;

	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_ColumnDescriptor_colFieldNames",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<String> colFieldNames;

	@Persistent(
		dependent="true",
		mappedBy="columnDescriptor",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ColumnName colName;

	@Persistent(
		dependent="true",
		mappedBy="columnDescriptor",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ColumnToolTipDescription colToolTipDescription;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ColumnDescriptor() {}

	public ColumnDescriptor(ColumnDescriptorID columnDescriptorID) {
		this(columnDescriptorID.organisationID, columnDescriptorID.columnDescriptionID);
	}

	public ColumnDescriptor(String organisationID, long columnDescriptionID) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.columnDescriptionID = columnDescriptionID;

		// Multi-language special specifications.
		this.colName = new ColumnName(this);
		this.colToolTipDescription = new ColumnToolTipDescription(this);
	}

	/**
	 * Sets up the column-description information.
	 * Note(s): 1. That (as best as possible) arrays should not be empty or null.
	 *          2. Set up the {@link ColumnName} and the {@link ColumnToolTipDescription} separately.
	 */
	public void setDescriptorInfos
	(int colWeight, boolean isMoveable, boolean isResizable, int colStyle,
	 ColumnContentProperty colContentProperty, Set<String> colFieldNames, String[] colFetchGroups) {
		this.colWeight = colWeight;
		this.isMoveable = isMoveable;
		this.isResizable = isResizable;
		this.colStyle = colStyle;
		this.colContentProperty = colContentProperty;
		this.colFieldNames = colFieldNames;
		this.colFetchGroups = colFetchGroups;
	}

	/**
	 * Sets up the column-description information with some fields set to default values.
	 * Note(s): 1. That (as best as possible) arrays should not be empty or null.
	 *          2. Set up the {@link ColumnName} and the {@link ColumnToolTipDescription} separately.
	 */
	public void setDescriptorInfos
	(int colWeight, ColumnContentProperty colContentProperty, Set<String> colFieldNames, String[] colFetchGroups) {
		// Note: colStyle = SWT.LEFT = 16384.
		setDescriptorInfos(colWeight, true, true, 16384, colContentProperty, colFieldNames, colFetchGroups);
	}

	/**
	 * @return the {@link I18nText} representing the name of column of this {@link ColumnDescriptor}.
	 */
	public ColumnName getColumnName() { return colName; }

	/**
	 * @return the {@link I18nText} representing the tool-tip description (tex) of this {@link ColumnDescriptor}.
	 */
	public ColumnToolTipDescription getColumnToolTipDescription() { return colToolTipDescription; }

	/**
	 * @return the organisationID of this {@link ColumnDescriptor}.
	 */
	public String getOrganisationID() { return organisationID; }

	/**
	 * @return the {@link ColumnDescriptorID} of this {@link ColumnDescriptor}.
	 */
	public long getColumnDescriptionID() { return columnDescriptionID; }



	// ------------------------------------------------------------------------ || -------------------------------------------->>
	// [Section] Setters to the internal variables.
	// ------------------------------------------------------------------------ || -------------------------------------------->>
	public void setStyle(int colStyle) { this.colStyle = colStyle; }

	public void setMovable(boolean isMovable) { this.isMoveable = isMovable; }

	public void setResizable(boolean isResizable) { this.isResizable = isResizable; }

	public void setWeight(int colWeight) { this.colWeight = colWeight; }

	public void setFetchGroups(String[] colFetchGroups) { this.colFetchGroups = colFetchGroups; }

	public void setContentProperty(ColumnContentProperty colContentProperty) { this.colContentProperty = colContentProperty; }

	public void setFieldNames(Set<String> colFieldNames) { this.colFieldNames = colFieldNames; }




	// ------------------------------------------------------------------------ || -------------------------------------------->>
	// [Section] Implementation of the ITableColumnDescriptor.
	// ------------------------------------------------------------------------ || -------------------------------------------->>
	@Override
	public int getStyle() { return colStyle; }

	@Override
	public boolean isMovable() { return isMoveable; }

	@Override
	public boolean isResizable() { return isResizable; }

	@Override
	public int getWeight() { return colWeight; }

	@Override
	public String[] getFetchGroups() {	return colFetchGroups; }

	@Override
	public String getName() { return colName.getText(); }

	@Override
	public String getTooltipDescription() { return colToolTipDescription.getText(); }

	@Override
	public ColumnContentProperty getContentProperty() { return colContentProperty; }

	@Override
	public Set<String> getFieldNames() { return colFieldNames; }
}
