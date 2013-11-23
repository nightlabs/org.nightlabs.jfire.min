package org.nightlabs.jfire.editlock;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.editlock.id.EditLockID;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.editlock.id.EditLockID"
 *		detachable="true"
 *		table="JFireBase_EditLock"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, editLockID"
 *
 * @jdo.fetch-group name="EditLock.lockOwnerUser" fields="lockOwnerUser"
 * @jdo.fetch-group name="EditLock.editLockType" fields="editLockType"
 *
 * @jdo.query name="getEditLockIDsByLockedObjectIDStr" query="SELECT JDOHelper.getObjectId(this) WHERE this.lockedObjectIDStr == :lockedObjectIDStr"
 *
 * @jdo.query name="getEditLockCountByLockedObjectIDStr" query="SELECT count(this.editLockID) WHERE this.lockedObjectIDStr == :lockedObjectIDStr"
 *
 * @jdo.query
 *		name="getEditLockByLockedObjectIDStrAndLockOwner"
 *		query="
 *			SELECT UNIQUE WHERE
 *				this.lockedObjectIDStr == :lockedObjectIDStr &&
 *				this.lockOwnerUser.organisationID == :lockOwner_organisationID &&
 *				this.lockOwnerUser.userID == :lockOwner_userID &&
 *				this.lockOwnerSessionID == :lockOwner_sessionID"
 *
 * @jdo.query
 *		name="getEditLocksWithLastAcquireDTOlderThan"
 *		query="SELECT WHERE this.editLockType == :editLockType && this.lastAcquireDT < :lastAcquireDT"
 *
 * @jdo.query
 *		name="getEditLockTypesHavingEditLocks"
 *		query="SELECT this.editLockType"
 *
 * @!jdo.query // this unfortunately doesn't work :-(
 *		name="getExpiredEditLocks"
 *		query="SELECT WHERE this.lastUseDT + this.editLockType.editLockExpiryMSec < :now"
 */
@PersistenceCapable(
	objectIdClass=EditLockID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_EditLock")
@FetchGroups({
	@FetchGroup(
		name=EditLock.FETCH_GROUP_LOCK_OWNER_USER,
		members=@Persistent(name="lockOwnerUser")),
	@FetchGroup(
		name=EditLock.FETCH_GROUP_EDIT_LOCK_TYPE,
		members=@Persistent(name="editLockType"))
})
@Queries({
	@javax.jdo.annotations.Query(
		name="getEditLockIDsByLockedObjectIDStr",
		value="SELECT JDOHelper.getObjectId(this) WHERE this.lockedObjectIDStr == :lockedObjectIDStr"),
	@javax.jdo.annotations.Query(
		name="getEditLockCountByLockedObjectIDStr",
		value="SELECT count(this.editLockID) WHERE this.lockedObjectIDStr == :lockedObjectIDStr"),
	@javax.jdo.annotations.Query(
		name="getEditLockByLockedObjectIDStrAndLockOwner",
		value=" SELECT UNIQUE WHERE this.lockedObjectIDStr == :lockedObjectIDStr && this.lockOwnerUser.organisationID == :lockOwner_organisationID && this.lockOwnerUser.userID == :lockOwner_userID && this.lockOwnerSessionID == :lockOwner_sessionID"),
	@javax.jdo.annotations.Query(
		name="getEditLocksWithLastAcquireDTOlderThan",
		value="SELECT WHERE this.editLockType == :editLockType && this.lastAcquireDT < :lastAcquireDT"),
	@javax.jdo.annotations.Query(
		name="getEditLockTypesHavingEditLocks",
		value="SELECT this.editLockType")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class EditLock
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(EditLock.class);

	public static final String FETCH_GROUP_LOCK_OWNER_USER = "EditLock.lockOwnerUser";
	public static final String FETCH_GROUP_EDIT_LOCK_TYPE = "EditLock.editLockType";

	public static Set<EditLockID> getEditLockIDs(PersistenceManager pm, ObjectID objectID)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		if (objectID == null)
			throw new IllegalArgumentException("objectID must not be null!");

		Query q = pm.newNamedQuery(EditLock.class, "getEditLockIDsByLockedObjectIDStr");
		return new HashSet<EditLockID>((Collection<? extends EditLockID>) q.execute(objectID.toString()));
	}

	public static long getEditLockCount(PersistenceManager pm, ObjectID objectID)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		if (objectID == null)
			throw new IllegalArgumentException("objectID must not be null!");

		Query q = pm.newNamedQuery(EditLock.class, "getEditLockCountByLockedObjectIDStr");
		return ((Long) q.execute(objectID.toString())).longValue();
	}

	public static EditLock getEditLock(PersistenceManager pm, ObjectID objectID, UserID lockOwnerUserID, String lockOwnerSessionID)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		if (objectID == null)
			throw new IllegalArgumentException("objectID must not be null!");

		if (lockOwnerUserID == null)
			throw new IllegalArgumentException("lockOwnerUserID must not be null!");

		if (lockOwnerSessionID == null)
			throw new IllegalArgumentException("lockOwnerSessionID must not be null!");

		Query q = pm.newNamedQuery(EditLock.class, "getEditLockByLockedObjectIDStrAndLockOwner");
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("lockedObjectIDStr", objectID.toString());
		params.put("lockOwner_organisationID", lockOwnerUserID.organisationID);
		params.put("lockOwner_userID", lockOwnerUserID.userID);
		params.put("lockOwner_sessionID", lockOwnerSessionID);
		return (EditLock) q.executeWithMap(params);
	}

	/**
	 * This method acquires an {@link EditLock}. This means, either a new instance of {@link EditLock} will
	 * be created and persisted to the datastore, or a previously existing instance will be updated (see
	 * {@link EditLock#getLastAcquireDT()}.
	 * <p>
	 * There is no reference counting! If this method is called multiple times, each follow-up call is simply
	 * updating the <code>lastAcquireDT</code> timestamp and one single call of
	 * {@link #releaseEditLock(PersistenceManager, UserID, String, ObjectID, ReleaseReason)} is enough
	 * to remove the lock.
	 * </p>
	 *
	 * @param pm The <code>PersistenceManager</code> used to access the JDO datastore.
	 * @param lockOwnerUserID The ID of the user who is acquiring the lock.
	 * @param lockOwnerSessionID The ID of the user's session. As one user can use multiple instances of the client, this is used for distinction.
	 * @param editLockTypeID The ID of the {@link EditLockType} to which the acquired <code>EditLock</code> will belong. If the lock was already existing before, this
	 *		parameter is ignored.
	 * @param objectID The ID of the JDO object which is about to be locked.
	 * @param description A human readable description for the newly acquired lock. If the lock was already existing before, the description will be updated to this value.
	 * @return A wrapper object holding the newly created (or previously existing and refreshed) {@link EditLock} and additional information.
	 *
	 * @see #releaseEditLock(PersistenceManager, UserID, String, ObjectID, ReleaseReason)
	 */
	public static AcquireEditLockResult acquireEditLock(
			PersistenceManager pm, UserID lockOwnerUserID, String lockOwnerSessionID,
			EditLockTypeID editLockTypeID, ObjectID objectID, String description)
	{
		EditLock editLock = EditLock.getEditLock(pm, objectID, lockOwnerUserID, lockOwnerSessionID);
		boolean refresh;
		if (editLock != null) {
			editLock.setLastAcquireDT();
			if (logger.isDebugEnabled()) {
				logger.debug("editLock.getLastAquireDT() after update = "+editLock.getLastAcquireDT());
			}
			editLock.setDescription(description);
			refresh = true;
		}
		else {
			EditLockType editLockType = (EditLockType) pm.getObjectById(editLockTypeID);
			editLock = pm.makePersistent(new EditLock(editLockType, (User) pm.getObjectById(lockOwnerUserID), lockOwnerSessionID, objectID, description));
			refresh = false;
		}

		long editLockCount = EditLock.getEditLockCount(pm, objectID);

		AcquireEditLockResult res = new AcquireEditLockResult(editLock, editLockCount);
		editLock.getEditLockType().onAcquireEditLock(res, refresh);
		return res;
	}

	/**
	 * This method releases an {@link EditLock} which has been previously acquired by
	 * {@link #acquireEditLock(PersistenceManager, UserID, String, EditLockTypeID, ObjectID, String)}. The instance of {@link EditLock}
	 * is deleted from the datastore.
	 *
	 * @param pm The <code>PersistenceManager</code> used to access the JDO datastore.
	 * @param lockOwnerUserID The ID of the user who is acquiring the lock.
	 * @param lockOwnerSessionID The ID of the user's session. As one user can use multiple instances of the client, this is used for distinction.
	 * @param objectID The ID referencing the JDO object that has been locked.
	 * @param releaseReason Why was the lock released.
	 * @throws ModuleException This can be thrown by the callback-method {@link EditLockType#onReleaseEditLock(EditLock, ReleaseReason)}
	 */
	public static void releaseEditLock(PersistenceManager pm, UserID lockOwnerUserID, String lockOwnerSessionID, ObjectID objectID, ReleaseReason releaseReason)
	{
		EditLock editLock = EditLock.getEditLock(pm, objectID, lockOwnerUserID, lockOwnerSessionID);
		if (editLock == null)
			return;

		editLock.getEditLockType().onReleaseEditLock(editLock, releaseReason);
		pm.deletePersistent(editLock);
	}

	public static Collection<EditLock> getExpiredEditLocks(PersistenceManager pm)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		Set<EditLock> editLocks = new HashSet<EditLock>();

		for (EditLockType editLockType : getEditLockTypesHavingEditLocks(pm)) {
			if (logger.isDebugEnabled())
				logger.debug("getExpiredEditLocks: querying editLocks for editLockType: " + JDOHelper.getObjectId(editLockType));

			List<EditLock> w = getEditLocksWithLastAcquireDTOlderThan(pm, editLockType, new Date(System.currentTimeMillis() - editLockType.getEditLockExpiryClientLostMSec()));
			if (logger.isDebugEnabled())
				logger.debug("getExpiredEditLocks: editLockType has " + w.size() + " expired editLocks: " + JDOHelper.getObjectId(editLockType));

			editLocks.addAll(w);
		}
		return editLocks;

//		Query q = pm.newNamedQuery(EditLock.class, "getExpiredEditLocks");
//		return (List<EditLock>) q.execute(new Date());
	}

	public static List<EditLockType> getEditLockTypesHavingEditLocks(PersistenceManager pm)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		Query q = pm.newNamedQuery(EditLock.class, "getEditLockTypesHavingEditLocks");
		return (List<EditLockType>) q.execute(new Date());
	}

	public static List<EditLock> getEditLocksWithLastAcquireDTOlderThan(PersistenceManager pm, EditLockType editLockType, Date lastAcquireDT)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		if (lastAcquireDT == null)
			throw new IllegalArgumentException("lastAcquireDT must not be null!");

		Query q = pm.newNamedQuery(EditLock.class, "getEditLocksWithLastAcquireDTOlderThan");
		return (List<EditLock>) q.execute(editLockType, lastAcquireDT);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long editLockID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private EditLockType editLockType;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 * @jdo.column length="512"
	 */
	@Element(indexed="true")
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=512)
	private String lockedObjectIDStr;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	@Element(indexed="true")
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private User lockOwnerUser;

	/**
	 * @jdo.field persistence-modifier="persistent" indexed="true"
	 */
	@Element(indexed="true")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String lockOwnerSessionID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createDT;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date lastAcquireDT;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String description;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient ObjectID lockedObjectID = null;
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EditLock() { }

	public EditLock(EditLockType editLockType, User lockOwnerUser, String lockOwnerSessionID, ObjectID objectID, String description)
	{
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(EditLock.class), editLockType, lockOwnerUser, lockOwnerSessionID, objectID.toString(), description);
	}

	public EditLock(
			String organisationID, long editLockID,
			EditLockType editLockType,
			User lockOwnerUser, String lockOwnerSessionID, String lockedObjectIDStr, String description)
	{
		ObjectIDUtil.assertValidIDString(organisationID);
		this.organisationID = organisationID;
		this.editLockID = editLockID;
		this.editLockType = editLockType;
		this.lockOwnerUser = lockOwnerUser;
		this.lockOwnerSessionID = lockOwnerSessionID;
		this.lockedObjectIDStr = lockedObjectIDStr;
		this.description = description;
		this.createDT = new Date();
		this.lastAcquireDT = createDT;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	
	public long getEditLockID()
	{
		return editLockID;
	}

	public EditLockType getEditLockType()
	{
		return editLockType;
	}

	public User getLockOwnerUser()
	{
		return lockOwnerUser;
	}
	
	public String getLockOwnerSessionID()
	{
		return lockOwnerSessionID;
	}
	
	public String getLockedObjectIDStr()
	{
		return lockedObjectIDStr;
	}

	public ObjectID getLockedObjectID()
	{
		if (lockedObjectID == null)
			lockedObjectID = ObjectIDUtil.createObjectID(lockedObjectIDStr);

		return lockedObjectID;
	}

	public Date getCreateDT()
	{
		return createDT;
	}
	
	/**
	 * After a <code>EditLock</code> has been created, it can be "renewed" in order to prevent expiry.
	 * Initially, this last-use-timestamp is the same as the create-timestamp (returned by {@link #getCreateDT()}).
	 *
	 * @return The timestamp of the last time this <code>EditLock</code> has been marked as "in use".
	 */
	public Date getLastAcquireDT()
	{
		return lastAcquireDT;
	}
	
	/**
	 * This convenience method calls {@link #setLastAcquireDT(Date)} with the current time.
	 */
	public void setLastAcquireDT()
	{
		if (logger.isDebugEnabled()) {
			logger.debug("setLastAcquireDT: before update = "+getLastAcquireDT()+" for EditLock "+this);
		}

		setLastAcquireDT(new Date());

		if (logger.isDebugEnabled()) {
			logger.debug("setLastAcquireDT: after update = "+getLastAcquireDT()+" for EditLock "+this);
		}
	}
	
	/**
	 * @param lastAcquireDT The timestamp of the last time this EditLock has been used.
	 * @see #getLastAcquireDT()
	 */
	public void setLastAcquireDT(Date lastUseDT)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("setLastAcquireDT(lastUseDT="+lastUseDT+"): before update = "+getLastAcquireDT()+" for EditLock "+this);
		}

		this.lastAcquireDT = lastUseDT;

		if (logger.isDebugEnabled()) {
			logger.debug("setLastAcquireDT(lastUseDT="+lastUseDT+"): after update = "+getLastAcquireDT()+" for EditLock "+this);
		}
	}

	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof EditLock)) return false;
		EditLock o = (EditLock) obj;
		return Util.equals(o.organisationID, this.organisationID) && Util.equals(o.editLockID, this.editLockID);
	}
	
	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(editLockID);
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + editLockID + ']';
	}
}
