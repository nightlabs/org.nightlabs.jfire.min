package org.nightlabs.jfire.security.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
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
import org.nightlabs.util.Util;

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
public abstract class UserManagementSystem implements Serializable
{

	public static final String FETCH_GROUP_NAME = "UserManagementSystem.name";
	public static final String FETCH_GROUP_DESCRIPTION = "UserManagementSystem.description";
	public static final String FETCH_GROUP_TYPE = "UserManagementSystem.type";

	protected static final String GET_ACTIVE_USER_MANAGEMENT_SYSTEMS = "getActiveUserManagementSystems";
	
	public static Collection<? extends UserManagementSystem> getActiveUserManagementSystems(PersistenceManager pm)
	{
		javax.jdo.Query q = pm.newNamedQuery(UserManagementSystem.class, UserManagementSystem.GET_ACTIVE_USER_MANAGEMENT_SYSTEMS);
		@SuppressWarnings("unchecked")
		List<UserManagementSystem> activeUserManagementSystems = (List<UserManagementSystem>) q.execute();
		
		// We copy them into a new ArrayList in order to be able to already close the query (save resources).
		// That would only be a bad idea, if we had really a lot of them and we would not need to iterate all afterwards.
		// But as we need to iterate most of them anyway, we can fetch the whole result set already here.
		// Note that this has only a positive effect in long-running transactions (because the query will be closed at the end of the
		// transaction, anyway). However, it has no negative effect besides the one already mentioned and we don't know in
		// which contexts this method might be used => better close the query quickly.
		// Marco.
		activeUserManagementSystems = new ArrayList<UserManagementSystem>(activeUserManagementSystems);
		q.closeAll();
		return activeUserManagementSystems;
	}

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long userManagementSystemID;
	
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
	 *
	 * *** REV_marco ***
	 * TODO why do you use javax.NAMING.*Exception? What we do here has nothing to do with JNDI. IMHO
	 * we should use different (our own?!) exceptions.
	 * Marco.
	 * ...well, I read a bit more of your code and saw that you use JNDI classes for accessing the LDAP
	 * server. This explains why you came to use the JNDI exceptions here. But even though using JNDI is a good
	 * way to communicate with LDAP, I still don't like the JNDI exceptions here.
	 * After all, this UserManagementSystem is an abstract class that knows nothing about LDAP and an implementation
	 * might very well deal with other exceptions in its specific back-end. Hence, UMS-specific or generic java-security
	 * exceptions would be more appropriate here.
	 * It's the same with SQL and JDO: JDO might use SQL in the background just like our UMS might use LDAP with JNDI,
	 * but it might as well use sth. completely different. Just like JDO introduces its own exceptions, IMHO we should
	 * do the same or alternatively use the generic (not JNDI-related) java.security.auth.login.LoginException and its subclasses. 
	 */
	public abstract Session login(LoginData loginData) throws AuthenticationException, CommunicationException;

	/**
	 * The {@link Session} that was returned by the login method is passed here as is.
	 * Thus, a subclass can be used to keep additional information, specific to the UMSimplementation
	 * 
	 * @param session
	 *
	 * *** REV_marco ***
	 * TODO see my to-do-comment in {@link #login(LoginData)} above.
	 */
	public abstract void logout(Session session) throws CommunicationException;
	
	/**
	 * Constructor which generates object ID and sets UMS name
	 * 
	 * @param name for the UMS, if empty or null UMS class name will be used
	 * @param type used for creating and initializing this UMS 
	 */
	public UserManagementSystem(String name, UserManagementSystemType<?> type){
		this.userManagementSystemID = IDGenerator.nextID(UserManagementSystem.class);
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

	// *** REV_marco ***
	// This method was named getID(). The field was named umsID. The class is named UserManagementSystem.
	// Why this inconsistency? I renamed the field and the method so they are consistent with all other codes.
//	public long getID() {
//		return umsID;
//	}
	public long getUserManagementSystemID() {
		return userManagementSystemID;
	}
	
	// *** REV_marco ***
	// hashCode() and equals(...) were missing. They should be present in every entity
	// and they should take all and only primary key fields into account.
	// Btw. they can be auto-generated by Eclipse (though I usually modify the equals(...) method for
	// the sake of readability).
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (userManagementSystemID ^ (userManagementSystemID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		UserManagementSystem other = (UserManagementSystem) obj;
		return (
				Util.equals(this.userManagementSystemID, other.userManagementSystemID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}
}
