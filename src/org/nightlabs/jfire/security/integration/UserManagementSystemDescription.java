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

	@PrimaryKey
	private long umsID;

	/**
	 * Described UserManagementSystem
	 */
	@Persistent(defaultFetchGroup="true")
	private UserManagementSystem ums;


	public UserManagementSystemDescription(UserManagementSystem ums) {
		this.organisationID = ums.getOrganisationID();
		this.umsID = ums.getID();
		this.ums = ums;
	}

	@Join
	@Persistent(
		table="JFireBase_UserManagementSystemDescription_names",
		defaultFetchGroup="true")
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
		return umsID+"@"+organisationID;
	}
	
	/**
	 * @return organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * 
	 * @return described UserManagementSystem
	 */
	public UserManagementSystem getUserManagementSystem() {
		return ums;
	}

}
