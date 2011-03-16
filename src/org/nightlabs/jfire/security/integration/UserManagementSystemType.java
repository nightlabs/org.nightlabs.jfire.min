package org.nightlabs.jfire.security.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemTypeID;
import org.nightlabs.util.NLLocale;
import org.nightlabs.util.Util;

/**
 * <p>Every implementation of {@link UserManagementSystem} is represented by a persistent singleton-instance
 * of this class in the datastore. It provides meta-data for the <code>UserManagementSystem</code>
 * (additionally to its Java class) and is the factory for creating new instances.</p>
 *
 * <p>Being a factory, multiple differently pre-configured flavours of the same <code>UserManagementSystem<code>
 * subclass can be created in {@link #createUserManagementSystem()} by persisting different implementations
 * of <code>UserManagementSystemType</code>.</p>
 *
 * <p>For example, the {@link LDAPServer} would always be the same class (no subclassing),
 * but one UMSType can create a pre-configured LDAPServer instance for a SAMBA-LDAP-Schema,
 * one for Microsoft's default Domain-Controller-Schema and another UMSType creates LDAPServers
 * pre-configured for InetOrgPerson.</p>
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 * @param <T> represents concrete UMS
 */
@PersistenceCapable(
	objectIdClass=UserManagementSystemTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UserManagementSystemType")
@FetchGroups({
	@FetchGroup(
		name=UserManagementSystemType.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=UserManagementSystemType.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME, column="className")
@Queries(
		@javax.jdo.annotations.Query(
				name=UserManagementSystemType.GET_ALL_USER_MANAGEMENT_SYSTEM_TYPES_IDS,
				value="SELECT JDOHelper.getObjectId(this)"
					)
		)
public abstract class UserManagementSystemType<T extends UserManagementSystem> implements Serializable{

	public final static String FETCH_GROUP_NAME = "UserManagementSystemType.name";
	public final static String FETCH_GROUP_DESCRIPTION = "UserManagementSystemType.description";

	public final static String GET_ALL_USER_MANAGEMENT_SYSTEM_TYPES_IDS = "getAllUserManagementSystemTypesIds";

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long userManagementSystemTypeID;

	/**
	 * Human readable name for the UMSType
	 */
	@Persistent(
		dependent="true",
		mappedBy="userManagementSystemType")
	private UserManagementSystemTypeName name;

	/**
	 * Description of the UMSType
	 */
	@Persistent(
		dependent="true",
		mappedBy="userManagementSystemType")
	private UserManagementSystemTypeDescription description;

	/**
	 * Loads an instance of requested UMSType instance. Loads all object IDs for the specified class 
	 * and takes first if there's more than one. So it's guaranteed that only one UMSType instance 
	 * of the specified class will be available.
	 *
	 * @param <T> specific UserManagementSystemType 
	 * @param pm
	 * @param clazz
	 * @return instance of UserManagemenSystemType of the specific class
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T extends UserManagementSystemType<?>> T loadSingleInstance(
			PersistenceManager pm, Class<T> clazz
			){

		Query q = pm.newNamedQuery(UserManagementSystemType.class, GET_ALL_USER_MANAGEMENT_SYSTEM_TYPES_IDS);
		q.setClass(clazz);

		Collection<UserManagementSystemTypeID> typesIds = (Collection<UserManagementSystemTypeID>) q.execute();
		typesIds = new ArrayList<UserManagementSystemTypeID>(typesIds);
		q.closeAll();
		
		T singleInstance = null;
		if (typesIds.size() > 0){
			singleInstance = (T) pm.getObjectById(typesIds.iterator().next());
		}

		return singleInstance;
	}

	/**
	 * Constructor generating object ID and setting default name.
	 */
	public UserManagementSystemType(String name) {
		this.userManagementSystemTypeID = IDGenerator.nextID(UserManagementSystemType.class);
		this.organisationID = IDGenerator.getOrganisationID();
		setName(name);
	}

	/**
	 * Factory method for creation of specifically configured UMS instances.
	 * @return created UMS instance
	 */
	public abstract T createUserManagementSystem();


	/**
	 *
	 * @return UMSType I18nText name
	 */
	public I18nText getName() {
		return name;
	}

	/**
	 * Set UMSType name. If null or empty string is provided than class full name is used as default.
	 *
	 * @param name
	 */
	public void setName(String name) {
		if (this.name == null){
			this.name = new UserManagementSystemTypeName(this);
		}
		if (name == null || "".equals(name)){
			name = this.getClass().getName();
		}
		this.name.setText(NLLocale.getDefault(), name);
	}

	/**
	 *
	 * @return UMSType I18nText description
	 */
	public I18nText getDescription() {
		return description;
	}

	/**
	 * Set UMSType description
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		if (this.description == null){
			this.description = new UserManagementSystemTypeDescription(this);
		}
		this.description.setText(NLLocale.getDefault(), description);
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
	 * @return umsTypeID
	 */
	public long getUserManagementSystemTypeID() {
		return userManagementSystemTypeID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (userManagementSystemTypeID ^ (userManagementSystemTypeID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		UserManagementSystemType<?> other = (UserManagementSystemType<?>) obj;
		return (
				Util.equals(this.userManagementSystemTypeID, other.userManagementSystemTypeID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}
	
}
