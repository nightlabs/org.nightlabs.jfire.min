package org.nightlabs.jfire.security.integration;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.util.NLLocale;

/**
 * An abstract represention of an external User Management Systems (UMS)
 * used for authentication. Basically it does nothing except for providing
 * common interface, object's ID, name and description. All the work including
 * user-data synchronization is done by subclasses.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
	objectIdClass=UserManagementSystemID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UserManagementSystem")
@FetchGroups({
		@FetchGroup(
				name=UserManagementSystem.FETCH_GROUP_NAME,
				members=@Persistent(name="name")
				),
		@FetchGroup(
				name=UserManagementSystem.FETCH_GROUP_DESCRIPTION,
				members=@Persistent(name="description")
				),
		@FetchGroup(
				name=UserManagementSystem.FETCH_GROUP_TYPE,
				members=@Persistent(name="type")
				)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(column="className", strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries(
		@Query(
				name=UserManagementSystem.GET_ACTIVE_USER_MANAGEMENT_SYSTEMS, 
				value="SELECT this WHERE this.isActive == true ORDER BY JDOHelper.getObjectId(this) ASCENDING"
					)
		)
public abstract class UserManagementSystem implements Serializable{

	public static final String FETCH_GROUP_NAME = "UserManagementSystem.name";
	public static final String FETCH_GROUP_DESCRIPTION = "UserManagementSystem.description";
	public static final String FETCH_GROUP_TYPE = "UserManagementSystem.type";

	public static final String GET_ACTIVE_USER_MANAGEMENT_SYSTEMS = "getActiveUserManagementSystems";

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long umsID;
	
	/**
	 * Type of the UMS, see {@link UserManagementSystemType} for details
	 */
	@Persistent
	@ForeignKey(deleteAction=ForeignKeyAction.CASCADE)
	private UserManagementSystemType<?> type;
	
	/**
	 * Human readable name for the UMS
	 */
	@Persistent(dependent="true", mappedBy="ums")
	private UserManagementSystemName name;
	
	/**
	 * Description of the UMS
	 */
	@Persistent(dependent="true",mappedBy="ums")
	private UserManagementSystemDescription description;
	
	/**
	 * Indicates whether this UMS should be used for authentication
	 */
	@Persistent
	private boolean isActive = false;

	
	/**
	 * Perform the login via the UMS and return a {@link Session) descriptor if the login was successful.
	 * Return null or throw an exception, if the login failed (additional information should be logged – 
	 * the exception should be caught and logged)
	 * 
	 * @param loginData
	 * @return {@link Session) descriptor in case of successful login and null otherwise
	 */
	public abstract Session login(LoginData loginData) throws AuthenticationException, CommunicationException;

	/**
	 * The {@link Session} that was returned by the login method is passed here as is.
	 * Thus, a subclass can be used to keep additional information, specific to the UMSimplementation
	 * 
	 * @param session
	 */
	public abstract void logout(Session session) throws CommunicationException;
	
	/**
	 * Constructor which generates object ID and sets UMS name
	 * 
	 * @param name for the UMS, if empty or null UMS class name will be used
	 * @param type used for creating and initializing this UMS 
	 */
	public UserManagementSystem(String name, UserManagementSystemType<?> type){
		this.umsID = IDGenerator.nextID(UserManagementSystem.class);
		this.organisationID = IDGenerator.getOrganisationID();
		this.type = type;
		setName(name);
	}
	
	/**
	 * 
	 * @return type of the UMS
	 */
	public UserManagementSystemType<?> getType() {
		return type;
	}
	
	/**
	 * 
	 * @return UMS I18nText description
	 */
	public I18nText getDescription() {
		return description;
	}
	
	/**
	 * Set description for the UMS
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		if (this.description == null){
			this.description = new UserManagementSystemDescription(this);
		}
		this.description.setText(NLLocale.getDefault(), description);
	}
	
	/**
	 * 
	 * @return UMS I18nText name
	 */
	public I18nText getName() {
		return name;
	}
	
	/**
	 * Set UMS name. If null or empty string is provided than class full name is used as default.
	 * 
	 * @param name
	 */
	public void setName(String name){
		if (this.name == null){
			this.name = new UserManagementSystemName(this);
		}
		if (name == null || "".equals(name)){
			name = this.getClass().getName();
		}
		this.name.setText(NLLocale.getDefault(), name);
	}
	
	/**
	 * Set this UMS active which means it will be used for authentication
	 * 
	 * @param isActive
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	/**
	 * 
	 * @return whether this UMS is active
	 */
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * 
	 * @return organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * 
	 * @return umsID
	 */
	public long getID() {
		return umsID;
	}
	
}
