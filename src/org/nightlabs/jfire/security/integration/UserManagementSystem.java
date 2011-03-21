package org.nightlabs.jfire.security.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import javax.security.auth.login.LoginException;

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
@Queries({
		@Query(
			name=UserManagementSystem.GET_ACTIVE_USER_MANAGEMENT_SYSTEMS,
			value="SELECT WHERE this.isActive == true ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			),
		@Query(
			name=UserManagementSystem.GET_USER_MANAGEMENT_SYSTEMS_BY_LEADING,
			value="SELECT WHERE this.isLeading == :isLeading ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			)
})
public abstract class UserManagementSystem implements Serializable{
	
	/**
	 * Key for a System property which is used to configure UserMagementSystem synchronization process
	 * to fetch user data from specific UserManagementSystem ignoring who is the leading system.
	 * It might help in certain situations (e.g. when you want to use JFire as leading system, 
	 * but initially have some users in the UMS which you want to import).
	 */
	public static final String SHOULD_FETCH_USER_DATA = "org.nightlabs.jfire.security.integration.fetchUserData";

	public static final String FETCH_GROUP_NAME = "UserManagementSystem.name";
	public static final String FETCH_GROUP_DESCRIPTION = "UserManagementSystem.description";
	public static final String FETCH_GROUP_TYPE = "UserManagementSystem.type";

	private static final String GET_ACTIVE_USER_MANAGEMENT_SYSTEMS = "getActiveUserManagementSystems";
	private static final String GET_USER_MANAGEMENT_SYSTEMS_BY_LEADING = "getLeadingUserManagementSystems";

	@SuppressWarnings("unchecked")
	public static <T extends UserManagementSystem> Collection<T> getActiveUserManagementSystems(
			PersistenceManager pm
			) {
		
		javax.jdo.Query q = pm.newNamedQuery(
				UserManagementSystem.class, 
				UserManagementSystem.GET_ACTIVE_USER_MANAGEMENT_SYSTEMS
				);
		List<T> activeUserManagementSystems = (List<T>) q.execute();
		
		// We copy them into a new ArrayList in order to be able to already close the query (save resources).
		// That would only be a bad idea, if we had really a lot of them and we would not need to iterate all afterwards.
		// But as we need to iterate most of them anyway, we can fetch the whole result set already here.
		// Note that this has only a positive effect in long-running transactions (because the query will be closed at the end of the
		// transaction, anyway). However, it has no negative effect besides the one already mentioned and we don't know in
		// which contexts this method might be used => better close the query quickly.
		// Marco.
		activeUserManagementSystems = new ArrayList<T>(activeUserManagementSystems);
		q.closeAll();
		return activeUserManagementSystems;
	}

	@SuppressWarnings("unchecked")
	public static <T extends UserManagementSystem> Collection<T> getUserManagementSystemsByLeading(
			PersistenceManager pm, boolean isLeading, Class<T> umsClass
			) {
		
		javax.jdo.Query q = pm.newNamedQuery(
				UserManagementSystem.class, 
				UserManagementSystem.GET_USER_MANAGEMENT_SYSTEMS_BY_LEADING
				);
		List<T> userManagementSystems = (List<T>) q.execute(isLeading);
		userManagementSystems = new ArrayList<T>(userManagementSystems);
		q.closeAll();

		// filter by class
		for (Iterator<T> iterator = userManagementSystems.iterator(); iterator.hasNext();) {
			T ums = iterator.next();
			if (!umsClass.isInstance(ums)){
				iterator.remove();
			}
		}
		
		return userManagementSystems;
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
	@ForeignKey(deleteAction=ForeignKeyAction.CASCADE) // Isn't
	private UserManagementSystemType<?> type;

	/**
	 * Human readable name for the UMS
	 */
	@Persistent(dependent="true", mappedBy="userManagementSystem")
	private UserManagementSystemName name;

	/**
	 * Description of the UMS
	 */
	@Persistent(dependent="true",mappedBy="userManagementSystem")
	private UserManagementSystemDescription description;

	/**
	 * Indicates whether this UMS should be used for authentication
	 */
	@Persistent
	private boolean isActive = false;

	/**
	 * Indicates whether this UMS should be used as leading system. If <code>false</code> than JFire is
	 * considered as leading system.
	 */
	@Persistent
	private boolean isLeading = false;

	/**
	 * Perform the login via the UMS and return a {@link Session) descriptor if the login was successful.
	 * Return null or throw an exception, if the login failed (additional information should be logged –
	 * the exception should be caught and logged)
	 *
	 * @param loginData
	 * @return {@link Session) descriptor in case of successful login and null otherwise
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 */
	public abstract Session login(LoginData loginData) throws LoginException, UserManagementSystemCommunicationException;

	/**
	 * The {@link Session} that was returned by the login method is passed here as is.
	 * Thus, a subclass can be used to keep additional information, specific to the UMSimplementation
	 *
	 * @param session
	 * @throws UserManagementSystemCommunicationException
	 */
	public abstract void logout(Session session) throws UserManagementSystemCommunicationException;

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
	 * Create an instance of <code>UserManagementSystem</code>.
	 * @param userManagementSystemID optional unique ID of this new instance. If <code>null</code>, an ID will be created automatically.
	 * @param type the {@link UserManagementSystemType} for which this UMS is an instance. Required (must not be <code>null</code>).
	 */
	public UserManagementSystem(UserManagementSystemID userManagementSystemID, UserManagementSystemType<?> type) {

		if (type == null){
			throw new IllegalArgumentException("UserManagemenSystemType can't be null!");
		}

		if (userManagementSystemID == null) {
			this.organisationID = IDGenerator.getOrganisationID();
			this.userManagementSystemID = IDGenerator.nextID(UserManagementSystem.class);
		}
		else {
			this.organisationID = userManagementSystemID.organisationID;
			this.userManagementSystemID = userManagementSystemID.userManagementSystemID;
		}
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
	 * Set leading system state to this UMS
	 * @param isLeadingSystem
	 */
	public void setLeading(boolean isLeading) {
		this.isLeading = isLeading;
	}
	
	/**
	 * 
	 * @return whether this UMS should be used as leading system
	 */
	public boolean isLeading() {
		return isLeading;
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
	 * @return userManagementSystemID
	 */
	public long getUserManagementSystemID() {
		return userManagementSystemID;
	}

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
