/**
 * 
 */
package org.nightlabs.jfire.prop.validation;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.prop.validation.id.I18nValidationResultMessageID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @jdo.persistence-capable
 * 	identity-type="application"
 * 	objectid-class="org.nightlabs.jfire.prop.validation.id.I18nValidationResultMessageID"
 *  detachable="true"
 *  table="JFireBase_Prop_I18nValidationResultMessage"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, validationResultID"
 *  
 * @jdo.fetch-group name="I18nValidationResult.message" fields="names"
 * @jdo.fetch-group name "IStruct.fullData" fetch-groups="default" fields="names, validationResult"
 * 
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 */@PersistenceCapable(
	objectIdClass=I18nValidationResultMessageID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_I18nValidationResultMessage")
@FetchGroups({
	@FetchGroup(
		name="I18nValidationResult.message",
		members=@Persistent(name="names")),
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="names"), @Persistent(name="validationResult")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class I18nValidationResultMessage extends I18nText {

	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private long validationResultID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */		@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private I18nValidationResult validationResult;
	
	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireBase_Prop_I18nValidationResultMessage_names"
	 *		null-value="exception"
	 *      dependent-value="true"
	 *
	 * @jdo.join
	 */	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_I18nValidationResultMessage_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Value(dependent="true")

	private Map<String, String> names;
	
	/**
	 * @deprecated only for JDO.
	 */
	protected I18nValidationResultMessage() {
		super();
	}

	public I18nValidationResultMessage(I18nValidationResult validationResult) 
	{
		this.organisationID = validationResult.getOrganisationID();
		this.validationResultID = validationResult.getValidationResultID();
		this.validationResult = validationResult;
		this.names = new HashMap<String, String>();
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) 
	{
		return validationResult.getResultType().toString();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	/**
	 * Returns the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the validationResultID.
	 * @return the validationResultID
	 */
	public long getValidationResultID() {
		return validationResultID;
	}

	/**
	 * Returns the validationResult.
	 * @return the validationResult
	 */
	public I18nValidationResult getValidationResult() {
		return validationResult;
	}

}
