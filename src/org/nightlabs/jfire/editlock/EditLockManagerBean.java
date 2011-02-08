package org.nightlabs.jfire.editlock;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.editlock.id.EditLockID;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.timepattern.TimePatternFormatException;
import org.nightlabs.util.CollectionUtil;

/**
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/EditLockManager"
 *		jndi-name="jfire/ejb/JFireBaseBean/EditLockManager"
 *		type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class EditLockManagerBean
extends BaseSessionBeanImpl
implements EditLockManagerRemote, EditLockManagerLocal
{
	private static final Logger logger = Logger.getLogger(EditLockManagerBean.class);
	private static final long serialVersionUID = 1L;

	@EJB
	private EditLockManagerLocal editLockManagerLocal;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockManagerRemote#cleanupEditLocks(org.nightlabs.jfire.timer.id.TaskID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void cleanupEditLocks(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
//			EditLockManagerLocal helperBean = JFireEjb3Factory.getLocalBean(EditLockManagerLocal.class, null);
			EditLockManagerLocal helperBean = editLockManagerLocal;
			if (helperBean == null)
				throw new IllegalStateException("Dependency injection of EditLockManagerLocal did not work!");

			Thread.sleep(60000L * 10);

//			for (EditLock editLock : EditLock.getExpiredEditLocks(pm)) {
//				editLock.getEditLockType().onReleaseEditLock(editLock, ReleaseReason.clientLost);
//				pm.deletePersistent(editLock);
			Collection<? extends EditLockID> expiredEditLockIDs = CollectionUtil.castCollection(helperBean.cleanupEditLocks_getExpiredEditLocks());
			for (EditLockID editLockID : expiredEditLockIDs) {
				try {
					helperBean.cleanupEditLocks_releaseEditLock(editLockID);
				} catch (Throwable t) {
					logger.error("cleanupEditLocks: " + t, t);
					// TODO we should report about this in the new (still to be written right now) user notification system.
				}
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockManagerLocal#cleanupEditLocks_getExpiredEditLocks()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	@Override
	public Collection<? extends EditLockID> cleanupEditLocks_getExpiredEditLocks()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			List<EditLockID> expiredEditLockIDs = NLJDOHelper.getObjectIDList(EditLock.getExpiredEditLocks(pm));
			return expiredEditLockIDs;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockManagerLocal#cleanupEditLocks_releaseEditLock(org.nightlabs.jfire.editlock.id.EditLockID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	@Override
	public void cleanupEditLocks_releaseEditLock(EditLockID editLockID)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			EditLock editLock = (EditLock) pm.getObjectById(editLockID);
			editLock.getEditLockType().onReleaseEditLock(editLock, ReleaseReason.clientLost);
			pm.deletePersistent(editLock);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise()
	throws TimePatternFormatException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Task.class);
			TaskID taskID = TaskID.create(
					// Organisation.DEV_ORGANISATION_ID, // the task can be modified by the organisation and thus it's maybe more logical to use the real organisationID - not dev
					getOrganisationID(),
					Task.TASK_TYPE_ID_SYSTEM, "JFireBase-EditLockCleanup");
			Task task;
			try {
				task = (Task) pm.getObjectById(taskID);
				task.getActiveExecID();
			} catch (JDOObjectNotFoundException x) {
				task = new Task(
						taskID.organisationID, taskID.taskTypeID, taskID.taskID,
						User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
						EditLockManagerLocal.class,
						"cleanupEditLocks"
				);

				task.getName().setText(Locale.ENGLISH.getLanguage(), "EditLock Cleanup");
				task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This Task cleans up expired editLocks.");

				task.getTimePatternSet().createTimePattern(
						"*", // year
						"*", // month
						"*", // day
						"*", // dayOfWeek
						"*", //  hour
						"*/15"); // minute

				task.setEnabled(true);
				pm.makePersistent(task);
			}

			// WORKAROUND JPOX Bug: to avoid ConcurrentModificationsException in JPOX
			pm.getExtent(EditLock.class);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockManagerRemote#acquireEditLock(org.nightlabs.jfire.editlock.id.EditLockTypeID, org.nightlabs.jdo.ObjectID, java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public AcquireEditLockResult acquireEditLock(
			EditLockTypeID editLockTypeID, ObjectID objectID, String description,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			if (logger.isDebugEnabled()) {
				logger.debug("acquireEditLock: " + editLockTypeID + " " + objectID);
			}
			return EditLock.acquireEditLock(pm, UserID.create(getPrincipal()), getSessionID(), editLockTypeID, objectID, description).detachCopy(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockManagerRemote#releaseEditLock(org.nightlabs.jdo.ObjectID, org.nightlabs.jfire.editlock.ReleaseReason)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void releaseEditLock(ObjectID objectID, ReleaseReason releaseReason)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			EditLock.releaseEditLock(pm, UserID.create(getPrincipal()), getSessionID(), objectID, releaseReason);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockManagerRemote#getEditLockIDs(org.nightlabs.jdo.ObjectID)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<EditLockID> getEditLockIDs(ObjectID objectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return EditLock.getEditLockIDs(pm, objectID);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockManagerRemote#getEditLocks(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<EditLock> getEditLocks(Collection<EditLockID> editLockIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, editLockIDs, EditLock.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
