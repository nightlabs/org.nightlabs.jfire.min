/**
 * 
 */
package org.nightlabs.jfire.prop.validation;

import java.io.Serializable;

import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.prop.validation.id.I18nValidationResultID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * This class represents an localized validation result of an {@link ScriptDataBlockValidator}.
 *
 * @jdo.persistence-capable
 * 	identity-type="application"
 * 	objectid-class="org.nightlabs.jfire.prop.validation.id.I18nValidationResultID"
 *  detachable="true"
 *  table="JFireBase_Prop_I18nValidationResult"
 *
 * @jdo.create-objectid-class
 *  
 * @jdo.fetch-group name="I18nValidationResult.message" fields="message"
 * @jdo.fetch-group name "IStruct.fullData" fetch-groups="default" fields="message"
 * 
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 */@PersistenceCapable(
	objectIdClass=I18nValidationResultID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_I18nValidationResult")
@FetchGroups({
	@FetchGroup(
		name=I18nValidationResult.FETCH_GROUP_MESSAGE,
		members=@Persistent(name="message")),
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members=@Persistent(name="message"))
})

public class I18nValidationResult
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_MESSAGE = "I18nValidationResult.message";
	
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

    private ValidationResultType resultType;
    
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * 			  dependent="true"
	 */		@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)

    private I18nValidationResultMessage message;
    
    /**
     * @deprecated only for JDO
     */
    protected I18nValidationResult() {
    	super();
    }
    
	/**
	 * @param organisationID
	 * @param validationResultID
	 * @param resultType
	 */
	public I18nValidationResult(String organisationID, long validationResultID,
			ValidationResultType resultType) 
	{
		super();
		this.organisationID = organisationID;
		this.validationResultID = validationResultID;
		this.resultType = resultType;
		this.message = new I18nValidationResultMessage(this);
	}

	/**
	 * Returns the resultType.
	 * @return the resultType
	 */
	public ValidationResultType getResultType() {
		return resultType;
	}
	
	/**
	 * Sets the ValidationResultType.
	 * @param type the ValidationResultType to set.
	 */
	public void setValidationResultType(ValidationResultType type) {
		this.resultType = type;
	}
	
	/**
	 * Returns the message.
	 * @return the message
	 */
	public I18nValidationResultMessage getI18nValidationResultMessage() {
		return message;
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
    
}
