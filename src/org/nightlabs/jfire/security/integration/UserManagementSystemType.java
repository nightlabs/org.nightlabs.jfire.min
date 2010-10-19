package org.nightlabs.jfire.security.integration;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import javax.jdo.FetchPlan;
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

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemTypeID;
import org.nightlabs.util.NLLocale;

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
	private long umsTypeID;
	
	/**
	 * Human readable name for the UMSType
	 */
	@Persistent(
		dependent="true",
		mappedBy="umsType")
	private UserManagementSystemTypeName name;
	
	/**
	 * Description of the UMSType
	 */
	@Persistent(
		dependent="true",
		mappedBy="umsType")
	private UserManagementSystemTypeDescription description;
	
	
	/**
	 * Since every UMSType is a singleton we keep all the possible instances here and 
	 * UMSType class name is used as a key.
	 * Concrete UMS module is responsible for requesting to put in this map all UMS types it provides 
	 * by calling {@link #loadSingleInstance(PersistenceManager, Class)} method. 
	 */
	protected static final HashMap<String, UserManagementSystemType<?>> _instances = 
		new HashMap<String, UserManagementSystemType<?>>();
	
	/**
	 * Retrieve UMSType instance for specified class
	 * 
	 * @param <T>
	 * @param clazz - class object of the specific UMSType
	 * @return a singleton instance of needed UMSType
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends UserManagementSystemType<?>> T getInstance(Class<T> clazz){
		
		// FIXME: not sure what needs to be done if there's no UMSType instance for requested class
		
		return (T) _instances.get(clazz.getName());
	}
	
	/**
	 * Loads an instance of requested UMSType instance and put it into {@link #_instances} map.
	 * Loading all object IDs for the specified class and takes first if there's more than one.
	 * So it's guaranteed that only one UMSType instance will be available. Made protected so only
	 * concrete UMSType subclasses can call it after the specific instance is created inside them.
	 * 
	 * @param <T>
	 * @param pm
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected static synchronized <T extends UserManagementSystemType<?>> T loadSingleInstance(
			PersistenceManager pm, Class<T> clazz
			){
		
		if (_instances.get(clazz.getName()) != null){
			return (T) _instances.get(clazz.getName());
		}
		
		Query q = pm.newNamedQuery(UserManagementSystemType.class, GET_ALL_USER_MANAGEMENT_SYSTEM_TYPES_IDS);
		q.setClass(clazz);
		
		Collection<UserManagementSystemTypeID> typesIds = (Collection<UserManagementSystemTypeID>) q.execute();
		T singleInstance = null;
		if (typesIds.size() > 0){

			int oldFetchSize = pm.getFetchPlan().getFetchSize();
			pm.getFetchPlan().setFetchSize(-1);
			pm.getFetchPlan().setGroups(FetchPlan.DEFAULT, UserManagementSystemType.FETCH_GROUP_NAME);
			
			singleInstance = (T) pm.detachCopy(pm.getObjectById(typesIds.iterator().next()));
			_instances.put(clazz.getName(), singleInstance);
			
			pm.getFetchPlan().setMaxFetchDepth(oldFetchSize!=0?oldFetchSize:-1);
			pm.getFetchPlan().removeGroup(UserManagementSystemType.FETCH_GROUP_NAME);
		}
		
		return singleInstance;
	}
	
	/**
	 * Constructor generating object ID and setting default name.
	 */
	public UserManagementSystemType(String name) {
		this.umsTypeID = IDGenerator.nextID(UserManagementSystemType.class);
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
	 * @return UMSType name or empty string if name is null (i.e. not loaded from datastore)
	 */
	public String getName() {
		return name!=null?name.getText(getLocale()):"";
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
		this.name.setText(getLocale(), name);
	}
	
	/**
	 * 
	 * @return UMSType description or empty string if description is null
	 */
	public String getDescription() {
		return description!=null?description.getText(getLocale()):"";
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
		this.description.setText(getLocale(), description);
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
	public long getID() {
		return umsTypeID;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.integration.UserManagementSystem#getLocale()
	 */
	private static Locale getLocale(){
		try{
			return NLLocale.getDefault();
		}catch(Exception e){
			return Locale.getDefault();
		}
	}

}
