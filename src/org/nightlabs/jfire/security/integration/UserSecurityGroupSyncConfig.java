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
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.security.integration.id.UserSecurityGroupSyncConfigID;

/**
 * Provides a mapping of JFire {@link UserSecurityGroup}s to authorization-related object(s)
 * of external {@link UserManagementSystem} for synchronization purposes.
 * 
 * If concrete {@link UserManagementSystem} supports authorization which needs synchronization
 * with JFire than UserManagementSystem-specific module should provide implementation(s) for this class
 * specifying concrete type of authorization-related object. Actual synchronization is also supposed
 * to be implemented in this UserManagementSystem-specific module preferably using generic sync API 
 * from {@link UserManagementSystem}.
 * 
 * Every {@link UserSecurityGroup} could have multiple {@link UserSecurityGroupSyncConfig}s for different
 * {@link UserManagementSystem}s.
 * 
 * Since we assume that there will be not many {@link UserSecurityGroup}s created in JFire
 * and not many {@link UserManagementSystem}s used for sync we recommend to keep all the 
 * persistent data in one superclass table. 
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
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
			name=UserSecurityGroupSyncConfig.FETCH_GROUP_USER_SECURITY_GROUP,
			members=@Persistent(name="userSecurityGroup")
			),
	@FetchGroup(
			name=UserSecurityGroupSyncConfig.FETCH_GROUP_USER_MANAGEMENT_SYSTEM,
			members=@Persistent(name="userManagementSystem")
			)
})
@Queries({
	@javax.jdo.annotations.Query(
			name="UserSecurityGroupSyncConfig.getAllSyncConfigsForUserManagementSystem",
			value="SELECT where JDOHelper.getObjectId(this.userManagementSystem) == :userManagementSystemId ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			),
	@javax.jdo.annotations.Query(
			name="UserSecurityGroupSyncConfig.getSyncConfigForGroup",
			value="SELECT where JDOHelper.getObjectId(this.userManagementSystem) == :userManagementSystemId && JDOHelper.getObjectId(this.userSecurityGroup) == :userSecurityGroupId ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			),
	@javax.jdo.annotations.Query(
			name="UserSecurityGroupSyncConfig.getAllSyncConfigsForGroup",
			value="SELECT where JDOHelper.getObjectId(this.userSecurityGroup) == :userSecurityGroupId ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			)
	})
public abstract class UserSecurityGroupSyncConfig<T extends UserManagementSystem, UserManagementSystemSecurityType> implements Serializable{

	public static final String FETCH_GROUP_USER_SECURITY_GROUP = "UserSecurityGroupSyncConfig.userSecurityGroup";
	public static final String FETCH_GROUP_USER_MANAGEMENT_SYSTEM = "UserSecurityGroupSyncConfig.userManagementSystem";

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Executes a named {@link Query} which returns a {@link UserSecurityGroupSyncConfig} for given {@link UserSecurityGroup}.
	 * 
	 * @param pm {@link PersistenceManager} to be used for execution
	 * @param userManagementSystemId The ID of {@link UserManagementSystem}
	 * @param userSecurityGroupId The ID of {@link UserSecurityGroup}
	 * @return found {@link UserSecurityGroupSyncConfig} or <code>null</code>
	 */
	public static UserSecurityGroupSyncConfig<?, ?> getSyncConfigForGroup(
			PersistenceManager pm, UserManagementSystemID userManagementSystemId, UserSecurityGroupID userSecurityGroupId
			) {
		javax.jdo.Query q = pm.newNamedQuery(
				UserSecurityGroupSyncConfig.class, 
				"UserSecurityGroupSyncConfig.getSyncConfigForGroup"
				);
		UserSecurityGroupSyncConfig<?, ?> syncConfig = (UserSecurityGroupSyncConfig<?, ?>) q.execute(userManagementSystemId, userSecurityGroupId);
		q.closeAll();
		return syncConfig;
	}

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

	/**
	 * Executes a named {@link Query} to check if given {@link UserSecurityGroup} has any {@link UserSecurityGroupSyncConfig}s
	 * referencing it.
	 * 
	 * @param pm {@link PersistenceManager} to be used for execution
	 * @param userSecurityGroupId The ID of {@link UserSecurityGroup}
	 * @return <code>true</code> if referencing {@link UserSecurityGroupSyncConfig}s are found
	 */
	public static boolean syncConfigsExistForGroup(
			PersistenceManager pm, UserSecurityGroupID userSecurityGroupId
			) {
		javax.jdo.Query q = pm.newNamedQuery(
				UserSecurityGroupSyncConfig.class, 
				"UserSecurityGroupSyncConfig.getAllSyncConfigsForGroup"
				);
		@SuppressWarnings("unchecked")
		Collection<UserSecurityGroupSyncConfig<?, ?>> syncConfigs = (Collection<UserSecurityGroupSyncConfig<?, ?>>) q.execute(userSecurityGroupId);
		return syncConfigs.isEmpty();
	}

	
	@PrimaryKey
	private long userSecurityGroupSyncConfigID;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * {@link UserSecurityGroup} to be synchronized
	 */
	@Persistent
	@ForeignKey(deleteAction=ForeignKeyAction.CASCADE)
	private UserSecurityGroup userSecurityGroup;
	
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
	 * @param userSecurityGroup {@link UserSecurityGroup} which will be synchronized according to this configuration, not <code>null</code>
	 * @param userManagementSystem {@link UserManagementSystem} to synchronize with, not <code>null</code>
	 */
	public UserSecurityGroupSyncConfig(UserSecurityGroup userSecurityGroup, T userManagementSystem) {
		if (userSecurityGroup == null){
			throw new IllegalArgumentException("UserSecurityGroup can not be null!");
		}
		if (userManagementSystem == null){
			throw new IllegalArgumentException("UserManagementSystem can not be null!");
		}
		this.userSecurityGroupSyncConfigID = IDGenerator.nextID(UserSecurityGroupSyncConfig.class);
		this.organisationID = IDGenerator.getOrganisationID();
		this.userSecurityGroup = userSecurityGroup;
		this.userManagementSystem = userManagementSystem;
	}
	
	/**
	 * Get {@link UserSecurityGroup} of this sync config.
	 * 
	 * @return {@link UserSecurityGroup} of this sync config, not <code>null</code>
	 */
	public UserSecurityGroup getUserSecurityGroup() {
		return userSecurityGroup;
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
