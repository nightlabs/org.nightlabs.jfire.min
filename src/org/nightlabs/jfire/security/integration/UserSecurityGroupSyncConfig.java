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
import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.security.integration.id.UserSecurityGroupSyncConfigID;

/**
 * Provides a mapping of JFire {@link UserSecurityGroup}s to authorization-related object(s)
 * of external {@link UserManagementSystem} for synchronization purposes.
 * 
 * If concrete {@link UserManagementSystem} supports authorization which needs synchronization
 * with JFire than UserManagementSystem-specific module should provide implementation(s) for this class
 * specifying concrete type of authorization-related object. Actual synchronization is also supposed
 * to be implemented by this UserManagementSystem-specific module preferably using generic sync API 
 * from {@link UserManagementSystem}.
 * 
 * Every {@link UserSecurityGroup} could have multiple {@link UserSecurityGroupSyncConfig}s for different
 * {@link UserManagementSystem}s but only one {@link UserSecurityGroupSyncConfigContainer} which holds 
 * all of this {@link UserSecurityGroupSyncConfig}s.
 * 
 * Since we assume that there will be not many {@link UserSecurityGroup}s created in JFire
 * and not many {@link UserManagementSystem}s used for sync we recommend to keep all the 
 * persistent data in one superclass table. 
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 * @param <T> extends {@link UserManagementSystem}, to specify concrete type of {@link UserManagementSystem}
 * @param <UserManagementSystemSecurityType> type of an object which maps to {@link UserSecurityGroup}
 */
@PersistenceCapable(
		objectIdClass=UserSecurityGroupSyncConfigID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_UserSecurityGroupSyncConfig")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(column="className", strategy=DiscriminatorStrategy.CLASS_NAME)
@FetchGroups({
	@FetchGroup(
			name=UserSecurityGroupSyncConfig.FETCH_GROUP_USER_MANAGEMENT_SYSTEM,
			members=@Persistent(name="userManagementSystem")
			)
})
@Queries({
	@javax.jdo.annotations.Query(
			name="UserSecurityGroupSyncConfig.getAllSyncConfigsForUserManagementSystem",
			value="SELECT where JDOHelper.getObjectId(this.userManagementSystem) == :userManagementSystemId ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			)
	})
public abstract class UserSecurityGroupSyncConfig<T extends UserManagementSystem, UserManagementSystemSecurityType> implements Serializable{

	public static final String FETCH_GROUP_USER_MANAGEMENT_SYSTEM = "UserSecurityGroupSyncConfig.userManagementSystem";

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Executes a named {@link Query} which returns a {@link Collection} of {@link UserSecurityGroupSyncConfig}s for given {@link UserManagementSystem}.
	 * 
	 * @param pm {@link PersistenceManager} to be used for execution
	 * @param userManagementSystemId The ID of {@link UserManagementSystem}
	 * @return {@link Collection} of found {@link UserSecurityGroupSyncConfig}s or empty {@link Collection} if nothing found
	 */
	public static Collection<UserSecurityGroupSyncConfig<?,?>> getAllSyncConfigsForUserManagementSystem(
			PersistenceManager pm, UserManagementSystemID userManagementSystemId
			) {
		javax.jdo.Query q = pm.newNamedQuery(
				UserSecurityGroupSyncConfig.class, 
				"UserSecurityGroupSyncConfig.getAllSyncConfigsForUserManagementSystem"
				);
		@SuppressWarnings("unchecked")
		Collection<UserSecurityGroupSyncConfig<?, ?>> syncConfigs = (Collection<UserSecurityGroupSyncConfig<?, ?>>) q.execute(userManagementSystemId);
		syncConfigs = new ArrayList<UserSecurityGroupSyncConfig<?,?>>(syncConfigs);
		q.closeAll();
		return syncConfigs;
	}

	
	@PrimaryKey
	private long userSecurityGroupSyncConfigID;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * {@link UserSecurityGroup} which holds this {@link UserSecurityGroupSyncConfig}
	 */
	@Persistent(defaultFetchGroup="true")
	@ForeignKey(deleteAction=ForeignKeyAction.CASCADE)
	private UserSecurityGroupSyncConfigContainer syncConfigsContainer;
	
	/**
	 * {@link UserManagementSystem} which will synchronize with selected {@link #userSecurityGroup}
	 */
	@Persistent
	@ForeignKey(deleteAction=ForeignKeyAction.CASCADE)
	private T userManagementSystem;
	
	/**
	 * Indicates if synchronization is enabled for underlying {@link #userSecurityGroup} to/from specified {@link #userManagementSystem}.
	 * Default value is <code>true</code>.
	 */
	@Persistent
	private boolean syncEnabled = true;
	
	
	/**
	 * @deprecated For JDO only!
	 */
	public UserSecurityGroupSyncConfig(){}
	
	/**
	 * Constructs new synchronization config for given {@link UserSecurityGroup} with given {@link UserManagementSystem}.
	 * 
	 * @param container {@link UserSecurityGroupSyncConfigContainer} which holds this {@link UserSecurityGroupSyncConfig}, not <code>null</code>
	 * @param userManagementSystem {@link UserManagementSystem} to synchronize with, not <code>null</code>
	 */
	public UserSecurityGroupSyncConfig(UserSecurityGroupSyncConfigContainer container, T userManagementSystem) {
		if (container == null){
			throw new IllegalArgumentException("UserSecurityGroupSyncConfigContainer can not be null!");
		}
		if (userManagementSystem == null){
			throw new IllegalArgumentException("UserManagementSystem can not be null!");
		}
		this.userSecurityGroupSyncConfigID = IDGenerator.nextID(UserSecurityGroupSyncConfig.class);
		this.organisationID = IDGenerator.getOrganisationID();
		this.syncConfigsContainer = container;
		this.userManagementSystem = userManagementSystem;
	}
	
	/**
	 * Get {@link UserSecurityGroupSyncConfigContainer} which holds this {@link UserSecurityGroupSyncConfig}
	 * 
	 * @return
	 */
	public UserSecurityGroupSyncConfigContainer getContainer() {
		return syncConfigsContainer;
	}
	
	/**
	 * Get {@link UserSecurityGroup} of this sync config via its {@link UserSecurityGroupSyncConfigContainer}.
	 * 
	 * @return {@link UserSecurityGroup} of this sync config, not <code>null</code>
	 */
	public UserSecurityGroup getUserSecurityGroup() {
		return syncConfigsContainer.getUserSecurityGroup();
	}

	
	/**
	 * Get object representing authorization model unit of a specific {@link UserManagementSystem}.
	 * Could be anything beginning with a simple {@link String} with some ID or a complete wrapper
	 * object around specific {@link UserManagementSystem} authorization object.
	 * 
	 * @return object representing authorization model unit of a specific {@link UserManagementSystem}
	 */
	public abstract UserManagementSystemSecurityType getUserManagementSystemSecurityObject();

	
	/**
	 * Get {@link UserManagementSystem} which is configured for synchronization within this config.
	 * 
	 * @return {@link UserManagementSystem}, not <code>null</code>
	 */
	public T getUserManagementSystem() {
		return userManagementSystem;
	}
	
	/**
	 * Check if syncronization is enabled within this config. Default value is <code>true</code>.
	 * 
	 * @return <code>true</code> if synchronization is enabled
	 */
	public boolean isSyncEnabled() {
		return syncEnabled;
	}
	
	/**
	 * Set if synchronization is enabled within this config. Default value is <code>true</code>.
	 * 
	 * @param syncEnabled <code>true</code> if sync should be enabled
	 */
	public void setSyncEnabled(boolean syncEnabled) {
		this.syncEnabled = syncEnabled;
	}
	
	/**
	 * @return {@link #userSecurityGroupSyncConfigID}
	 */
	public long getUserSecurityGroupSyncConfigID() {
		return userSecurityGroupSyncConfigID;
	}
	
	/**
	 * @return {@link #organisationID}
	 */
	public String getOrganisationID() {
		return organisationID;
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
				+ (int) (userSecurityGroupSyncConfigID ^ (userSecurityGroupSyncConfigID >>> 32));
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
		UserSecurityGroupSyncConfig<?, ?> other = (UserSecurityGroupSyncConfig<?, ?>) obj;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		if (userSecurityGroupSyncConfigID != other.userSecurityGroupSyncConfigID)
			return false;
		return true;
	}
	
}
