package org.nightlabs.jfire.security.integration;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemDescriptionID;

/**
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
	objectIdClass=UserManagementSystemDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UserManagementSystemDescription")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class UserManagementSystemDescription extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	// *** REV_marco ***
	// Renamed this for the same reason as in class UserManagementSystem.
	@PrimaryKey
	private long userManagementSystemID;

	// *** REV_marco ***
	// Please don't abbreviate! The modern IDEs write a lot of code for you, thus there's no need to abbreviate.
	// Abbreviations make the code harder to understand - even to yourself when you read it a year later again ;-)
	/**
	 * Described UserManagementSystem
	 */
	@Persistent(defaultFetchGroup="true")
//	private UserManagementSystem ums;
	private UserManagementSystem userManagementSystem;


	public UserManagementSystemDescription(UserManagementSystem userManagementSystem) {
		this.organisationID = userManagementSystem.getOrganisationID();
		this.userManagementSystemID = userManagementSystem.getUserManagementSystemID();
		this.userManagementSystem = userManagementSystem;
	}

	@Join
	@Persistent(
		table="JFireBase_UserManagementSystemDescription_names",
		defaultFetchGroup="true"
	)
	private Map<String, String> names = new HashMap<String, String>();

	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		// *** REV_marco ***
		// For a description, an empty value is OK. For a name, that's different.
//		return umsID+"@"+organisationID;
		return ""; 
	}
	
	/**
	 * @return organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	public long getUserManagementSystemID() {
		return userManagementSystemID;
	}
	
	/**
	 * 
	 * @return described UserManagementSystem
	 */
	public UserManagementSystem getUserManagementSystem() {
		return userManagementSystem;
	}

}
