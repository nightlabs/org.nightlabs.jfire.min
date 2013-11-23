package org.nightlabs.jfire.prop.i18n;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import org.nightlabs.jfire.prop.id.StructFieldNameID;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link I18nText} used as name for {@link StructField}s.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.id.StructFieldNameID"
 *		detachable="true"
 *		table="JFireBase_Prop_StructFieldName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="structBlockOrganisationID, structBlockID, structFieldOrganisationID, structFieldID"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default" fields="field, names"
 * @jdo.fetch-group name="StructField.name" fields="field, names"
 */
@PersistenceCapable(
	objectIdClass=StructFieldNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructFieldName")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="field"), @Persistent(name="names")}),
	@FetchGroup(
		name="StructField.name",
		members={@Persistent(name="field"), @Persistent(name="names")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructFieldName extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structBlockOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structBlockID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structFieldOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structFieldID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructField<? extends DataField> field;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireBase_Prop_StructFieldName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_StructFieldName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names;

	/**
	 * For JDO only.
	 */
	protected StructFieldName() {
	}

	/**
	 * Create a new {@link StructFieldName} for the given {@link StructField}.
	 * @param field The {@link StructField} the new {@link StructFieldName} will be associated to.
	 */
	public StructFieldName(StructField<? extends DataField> field)
	{
		this.field = field;
		this.structBlockOrganisationID = field.getStructBlockOrganisationID();
		this.structBlockID = field.getStructBlockID();
		this.structFieldOrganisationID = field.getStructFieldOrganisationID();
		this.structFieldID = field.getStructFieldID();
		this.names = new HashMap<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return field.getStructFieldID();
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	/**
	 * Get the structBlockID.
	 * @return the structBlockID
	 */
	public String getStructBlockID()
	{
		return structBlockID;
	}

	/**
	 * Get the structBlockOrganisationID.
	 * @return the structBlockOrganisationID
	 */
	public String getStructBlockOrganisationID()
	{
		return structBlockOrganisationID;
	}

	/**
	 * Get the structFieldID.
	 * @return the structFieldID
	 */
	public String getStructFieldID()
	{
		return structFieldID;
	}

	/**
	 * Get the structFieldOrganisationID.
	 * @return the structFieldOrganisationID
	 */
	public String getStructFieldOrganisationID()
	{
		return structFieldOrganisationID;
	}
}