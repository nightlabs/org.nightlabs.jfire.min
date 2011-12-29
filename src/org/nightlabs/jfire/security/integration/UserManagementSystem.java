package org.nightlabs.jfire.security.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import javax.jdo.annotations.PersistenceModifier;
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

/**
 * An abstract represention of an external User Management Systems (UMS)
 * used for authentication. Basically it does nothing except for providing
 * common interface, object's ID, name and description. All the work is done 
 * by the subclasses. 
 * 
 * Note that generic API for login/logout is rather simple and has no big 
 * limitations on actual implementations although you of course might prefer 
 * having your own inside specific modules. However it is recommended to 
 * make use of this API in order to have all UMS-specific implementations 
 * consistent and easy to understand.
 * 
 * Specific {@link UserManagementSystem}s could also have synchronization
 * configured, please implement it according to {@link SynchronizableUserManagementSystem}
 * for consistency.
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
			name="UserManagementSystem.getActiveUserManagementSystems",
			value="SELECT WHERE this.isActive == true ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			),
		@Query(
			name="UserManagementSystem.getLeadingUserManagementSystems",
			value="SELECT WHERE this.isLeading == :isLeading ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			)
})
public abstract class UserManagementSystem implements Serializable, Comparable<UserManagementSystem>{
	
	/**
	 * Key for a System property which is used to configure {@link UserManagementSystem} synchronization process
	 * to fetch user-related data from specific {@link UserManagementSystem} ignoring who is the leading system.
	 * It might help in certain situations (e.g. when you want to use JFire as leading system, 
	 * but initially have some users in the UMS which you want to import).
	 */
	public static final String SHOULD_FETCH_USER_DATA = "org.nightlabs.jfire.security.integration.fetchUserData";

	public static final String FETCH_GROUP_NAME = "UserManagementSystem.name";
	public static final String FETCH_GROUP_DESCRIPTION = "UserManagementSystem.description";
	public static final String FETCH_GROUP_TYPE = "UserManagementSystem.type";

	@SuppressWarnings("unchecked")
	public static <T extends UserManagementSystem> Collection<T> getActiveUserManagementSystems(
			PersistenceManager pm
			) {
		
		javax.jdo.Query q = pm.newNamedQuery(
				UserManagementSystem.class, 
				"UserManagementSystem.getActiveUserManagementSystems"
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
				"UserManagementSystem.getLeadingUserManagementSystems"
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
	@ForeignKey(deleteAction=ForeignKeyAction.CASCADE)
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
	 * @deprecated For JDO only!
	 */
	@Deprecated
	protected UserManagementSystem(){}

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

		this.type = type;
		
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
	 * Set description for as a {@link String} to system's defaul {@link Locale}
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
	 * Set description as a {@link I18nText}
	 * 
	 * @param description
	 */
	public void setDescription(I18nText description){
		if (this.description == null){
			this.description = new UserManagementSystemDescription(this);
		}
		if (description != null){
			this.description.copyFrom(description);
		}
	}

	/**
	 *
	 * @return UMS I18nText name
	 */
	public I18nText getName() {
		return name;
	}

	/**
	 * Set name as a {@link String} to system's default {@link Locale}. If null or empty string is provided than class simple name is used as default.
	 *
	 * @param name
	 */
	public void setName(String name){
		if (this.name == null){
			this.name = new UserManagementSystemName(this);
		}
		if (name == null || "".equals(name)){
			name = getClass().getSimpleName();
		}
		this.name.setText(NLLocale.getDefault(), name);
	}
	
	/**
	 * Set name as {@link I18nText}. If null or empty string is provided than class simple name is used as default.
	 * 
	 * @param name
	 */
	public void setName(I18nText name){
		if (this.name == null){
			this.name = new UserManagementSystemName(this);
		}
		if (name != null){
			this.name.copyFrom(name);
		}else{
			setName(getClass().getSimpleName());
		}
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
	
	/**
	 * Check whether we should fetch User-related data (User/Person itself and/or authorization data)
	 * from external {@link UserManagementSystem}. Will return <code>true</code> if either 
	 * this {@link UserManagementSystem} is leading or a {@link UserManagementSystem#SHOULD_FETCH_USER_DATA}
	 * system property is set to <code>true</code>.
	 * 
	 * @return <code>true</code> if User-related data should be fetched
	 */
	public boolean shouldFetchUserData(){
		// we can fetch user data from UserManagementSystem in both scenarios:
		// - when JFire is a leading system we're doing it because it might help in certain situations 
		//   (e.g. when you want to use JFire as leading system, but initially have some users in the 
		//   LDAP which you want to import)
		// - when UMS is a leading system it's done when user still does not exist in JFire
		boolean fetchUserFromUMS;
		if (isLeading()){
			fetchUserFromUMS = true;
		}else{
			String fetchPropertyValue = System.getProperty(UserManagementSystem.SHOULD_FETCH_USER_DATA);
			if (fetchPropertyValue != null && !fetchPropertyValue.isEmpty()){
				fetchUserFromUMS = Boolean.parseBoolean(
						System.getProperty(UserManagementSystem.SHOULD_FETCH_USER_DATA)
						);
			}else{
				fetchUserFromUMS = true;
			}
		}
		return fetchUserFromUMS;
	}
	
	
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private UserManagementSystemID userManagementSystemIDObject;
	/**
	 * Convivnient method for obtaining {@link UserManagementSystemID} at once instead of creating it every time.
	 * 
	 * @return {@link UserManagementSystemID} object
	 */
	public UserManagementSystemID getUserManagementSystemObjectID(){
		if (userManagementSystemIDObject == null){
			userManagementSystemIDObject = UserManagementSystemID.create(organisationID, userManagementSystemID);
		}
		return userManagementSystemIDObject;
	}

	/**
	 * Active {@link UserManagementSystem}s go first. If both are either active or non-active than 
	 * their {@link UserManagementSystemName}s are compared if non-null (delegate to corresponding method 
	 * in {@link UserManagementSystemName} class). Otherwise comparation of {@link #userManagementSystemID}s is made. 
	 */
	@Override
	public int compareTo(UserManagementSystem userManagementSystem) {
		if (this.isActive && !userManagementSystem.isActive){
			return -1;
		}else if (!this.isActive && userManagementSystem.isActive){
			return 1;
		}else {
			if (this.name != null && userManagementSystem.getName() != null){
				return this.name.compareTo(userManagementSystem.getName());
			}else{
				long otherUserManagementSystemID = userManagementSystem.getUserManagementSystemID();
				if (this.userManagementSystemID < otherUserManagementSystemID){
					return -1;
				}else if (this.userManagementSystemID > otherUserManagementSystemID){
					return 1;
				}else {
					return 0;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime
				* result
				+ (int) (userManagementSystemID ^ (userManagementSystemID >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserManagementSystem other = (UserManagementSystem) obj;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		if (userManagementSystemID != other.userManagementSystemID)
			return false;
		return true;
	}
	
}
