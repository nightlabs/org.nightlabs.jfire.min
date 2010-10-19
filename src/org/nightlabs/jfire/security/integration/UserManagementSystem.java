package org.nightlabs.jfire.security.integration;

import java.io.Serializable;
import java.util.Locale;

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
public abstract class UserManagementSystem implements Serializable{

	public static final String FETCH_GROUP_NAME = "UserManagementSystem.name";
	public static final String FETCH_GROUP_DESCRIPTION = "UserManagementSystem.description";
	public static final String FETCH_GROUP_TYPE = "UserManagementSystem.type";

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
	 * Perform the login via the UMS and return a {@link Session) descriptor if the login was successful.
	 * Return null or throw an exception, if the login failed (additional information should be logged – 
	 * the exception should be caught and logged)
	 * 
	 * @param loginData
	 * @return {@link Session) descriptor in case of successful login and null otherwise
	 */
	public abstract Session login(LoginData loginData);

	/**
	 * The {@link Session} that was returned by the login method is passed here as is.
	 * Thus, a subclass can be used to keep additional information, specific to the UMSimplementation
	 * 
	 * @param session
	 */
	public abstract void logout(Session session);
	
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
	 * @return UMS description or empty string if description is null (i.e. not loaded from datastore)
	 */
	public String getDescription() {
		return description!=null?description.getText(getLocale()):"";
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
		this.description.setText(getLocale(), description);
	}
	
	/**
	 * 
	 * @return UMS name or empty string if name is null (i.e. not loaded from datastore)
	 */
	public String getName() {
		return name!=null?name.getText(getLocale()):"";
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
		this.name.setText(getLocale(), name);
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
	
	/**
	 * FIXME:
	 * I found that {@link JFireLocale} could not be used when the user is not logged in, so
	 * I decided that System's default <code>Locale</code> should be retuned until this happen.
	 * Probably there's already some code like that somewhere so either point me there or
	 * we need to decide how to handle gettig default <code>Locale</code> before logging in. 
	 * 
	 * @return user <code>Locale</code> or in case no user is logged in default System's <code>Locale</code>
	 */
	private static Locale getLocale(){
		try{
			return NLLocale.getDefault();
		}catch(Exception e){
			return Locale.getDefault();
		}
	}

}
