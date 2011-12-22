package org.nightlabs.jfire.security.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
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

import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.security.integration.id.UserSecurityGroupSyncConfigContainerID;

/**
 * This class provides a container entity for {@link UserSecurityGroupSyncConfig} instances, specifically to make it easier to 
 * deal with these entities on UI side. One {@link UserSecurityGroupSyncConfigContainer} relates exactly to one 
 * {@link UserSecurityGroup}, that's why it has "the same" ID.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
		objectIdClass=UserSecurityGroupSyncConfigContainerID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_UserSecurityGroupSyncConfigContainer")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
			name=UserSecurityGroupSyncConfigContainer.FETCH_GROUP_USER_SECURITY_GROUP,
			members=@Persistent(name="userSecurityGroup")
			),
	@FetchGroup(
			name=UserSecurityGroupSyncConfigContainer.FETCH_GROUP_USER_SECURITY_GROUP_SYNC_CONFIGS,
			members=@Persistent(name="syncConfigs")
			)
})
@Queries({
	@javax.jdo.annotations.Query(
			name="UserSecurityGroupSyncConfigContainer.getSyncConfigContainerForGroup",
			value="SELECT where JDOHelper.getObjectId(this.userSecurityGroup) == :userSecurityGroupId ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			)
	})
public class UserSecurityGroupSyncConfigContainer implements Serializable{

	public static final String FETCH_GROUP_USER_SECURITY_GROUP = "UserSecurityGroupSyncConfigContainer.userSecurityGroup";
	public static final String FETCH_GROUP_USER_SECURITY_GROUP_SYNC_CONFIGS = "UserSecurityGroupSyncConfigContainer.syncConfigs";

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Executes a named {@link Query} which returns a {@link UserSecurityGroupSyncConfigContainer} for given {@link UserSecurityGroup}.
	 * 
	 * @param pm {@link PersistenceManager} to be used for execution
	 * @param userSecurityGroupId The ID of {@link UserSecurityGroup}
	 * @return found {@link UserSecurityGroupSyncConfig} or <code>null</code>
	 */
	public static UserSecurityGroupSyncConfigContainer getSyncConfigContainerForGroup(
			PersistenceManager pm, UserSecurityGroupID userSecurityGroupId
			) {
		javax.jdo.Query q = pm.newNamedQuery(
				UserSecurityGroupSyncConfigContainer.class, 
				"UserSecurityGroupSyncConfigContainer.getSyncConfigContainerForGroup"
				);
		@SuppressWarnings("unchecked")
		Collection<UserSecurityGroupSyncConfigContainer> syncConfigsContainers = (Collection<UserSecurityGroupSyncConfigContainer>) q.execute(userSecurityGroupId);
		syncConfigsContainers = new ArrayList<UserSecurityGroupSyncConfigContainer>(syncConfigsContainers);
		q.closeAll();
		if (!syncConfigsContainers.isEmpty()){
			return syncConfigsContainers.iterator().next();
		}
		return null;
	}

	/**
	 * Executes a named {@link Query} to check if given {@link UserSecurityGroup} has any {@link UserSecurityGroupSyncConfigContainer}s
	 * referencing it.
	 * 
	 * @param pm {@link PersistenceManager} to be used for execution
	 * @param userSecurityGroupId The ID of {@link UserSecurityGroup}
	 * @return <code>true</code> if referencing {@link UserSecurityGroupSyncConfigContainer}s are found
	 */
	public static boolean syncConfigsExistForGroup(
			PersistenceManager pm, UserSecurityGroupID userSecurityGroupId
			) {
		javax.jdo.Query q = pm.newNamedQuery(
				UserSecurityGroupSyncConfigContainer.class, 
				"UserSecurityGroupSyncConfigContainer.getSyncConfigContainerForGroup"
				);
		@SuppressWarnings("unchecked")
		Collection<UserSecurityGroupSyncConfigContainer> syncConfigsContainers = (Collection<UserSecurityGroupSyncConfigContainer>) q.execute(userSecurityGroupId);
		syncConfigsContainers = new ArrayList<UserSecurityGroupSyncConfigContainer>(syncConfigsContainers);
		q.closeAll();
		UserSecurityGroupSyncConfigContainer container = null;
		if (!syncConfigsContainers.isEmpty()){
			container = syncConfigsContainers.iterator().next();
		}
		return container != null && !container.getSyncConfigs().isEmpty();
	}

	
	@PrimaryKey
	private String containerID;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * {@link UserSecurityGroup} which owns this {@link UserSecurityGroupSyncConfigContainer}
	 */
	@Persistent
	@ForeignKey(deleteAction=ForeignKeyAction.CASCADE)
	private UserSecurityGroup userSecurityGroup;
	
	/**
	 * {@link UserSecurityGroupSyncConfig} which relate to underlying {@link UserSecurityGroup} which owns this container
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT, mappedBy="syncConfigsContainer")
	private Set<UserSecurityGroupSyncConfig<?, ?>> syncConfigs;
	
	
	/**
	 * @deprecated For JDO only!
	 */
	public UserSecurityGroupSyncConfigContainer(){}
	
	/**
	 * Creates new {@link UserSecurityGroupSyncConfigContainer} for given {@link UserSecurityGroup}
	 * 
	 * @param userSecurityGroup {@link UserSecurityGroup} which will be synchronized according to this configuration, not <code>null</code>
	 */
	public UserSecurityGroupSyncConfigContainer(UserSecurityGroup userSecurityGroup) {
		if (userSecurityGroup == null){
			throw new IllegalArgumentException("UserSecurityGroup can not be null!");
		}
		this.containerID = userSecurityGroup.getUserSecurityGroupID();
		this.organisationID = userSecurityGroup.getOrganisationID();
		this.userSecurityGroup = userSecurityGroup;
		this.syncConfigs = new HashSet<UserSecurityGroupSyncConfig<?,?>>();
	}
	
	/**
	 * Get {@link UserSecurityGroup} which owns this {@link UserSecurityGroupSyncConfigContainer}.
	 * 
	 * @return {@link UserSecurityGroup} which owns this container, not <code>null</code>
	 */
	public UserSecurityGroup getUserSecurityGroup() {
		return userSecurityGroup;
	}
	
	/**
	 * Get all {@link UserSecurityGroupSyncConfig}s from this container
	 * 
	 * @return all {@link UserSecurityGroupSyncConfig}s from this container, not <code>null</code>
	 */
	public Set<UserSecurityGroupSyncConfig<?, ?>> getSyncConfigs() {
		return Collections.unmodifiableSet(syncConfigs);
	}
	
	/**
	 * Get {@link UserSecurityGroupSyncConfig} from this container by given {@link UserManagementSystemID}
	 * 
	 * @param userManagementSystemID
	 * @return found {@link UserSecurityGroupSyncConfig} or <code>null</code>
	 */
	public UserSecurityGroupSyncConfig<?, ?> getSyncConfigForUserManagementSystem(UserManagementSystemID userManagementSystemID){
		if (userManagementSystemID != null){
			for (UserSecurityGroupSyncConfig<?, ?> syncConfig : syncConfigs){
				if (userManagementSystemID.equals(syncConfig.getUserManagementSystem().getUserManagementSystemObjectID())){
					return syncConfig;
				}
			}
		}
		return null;
	}
	
	/**
	 * Add new {@link UserSecurityGroupSyncConfig} to this container
	 * 
	 * @param syncConfig to be added to this container
	 * @return <code>true</code> if element was added successfully, <code>false</code> otherwise
	 */
	public boolean addSyncConfig(UserSecurityGroupSyncConfig<?, ?> syncConfig){
		return syncConfigs.add(syncConfig);
	}

	/**
	 * Removes {@link UserSecurityGroupSyncConfig} from this container
	 * 
	 * @param syncConfig {@link UserSecurityGroupSyncConfig} to be removed
	 * @return <code>true</code> if element was removed successfully, <code>false</code> otherwise
	 */
	public boolean removeSyncConfig(UserSecurityGroupSyncConfig<?, ?> syncConfig){
		return syncConfigs.remove(syncConfig);
	}

	/**
	 * Removes {@link UserSecurityGroupSyncConfig}s from this container
	 * 
	 * @param syncConfigsToRemove {@link Set} of {@link UserSecurityGroupSyncConfig}s to be removed
	 * @return <code>true</code> if elements were removed successfully, <code>false</code> otherwise
	 */
	public boolean removeSyncConfigs(Set<UserSecurityGroupSyncConfig<?, ?>> syncConfigsToRemove){
		return syncConfigs.removeAll(syncConfigsToRemove);
	}

	/**
	 * @return {@link #containerID}
	 */
	public String getContainerID() {
		return containerID;
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
				+ ((containerID == null) ? 0 : containerID.hashCode());
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
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
		UserSecurityGroupSyncConfigContainer other = (UserSecurityGroupSyncConfigContainer) obj;
		if (containerID == null) {
			if (other.containerID != null)
				return false;
		} else if (!containerID.equals(other.containerID))
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}

}