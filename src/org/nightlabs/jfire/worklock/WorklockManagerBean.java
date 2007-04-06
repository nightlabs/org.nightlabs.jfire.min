package org.nightlabs.jfire.worklock;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.worklock.id.WorklockID;
import org.nightlabs.jfire.worklock.id.WorklockTypeID;
import org.nightlabs.timepattern.TimePatternFormatException;

/**
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/WorklockManager"
 *		jndi-name="jfire/ejb/JFireBaseBean/WorklockManager
 *		type="Stateless"
 *
 * @ejb.util generate="physical"
 */
public abstract class WorklockManagerBean
extends BaseSessionBeanImpl
implements SessionBean 
{
//	private static final Logger logger = Logger.getLogger(WorklockManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}

	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"  
	 */
	public void ejbCreate() throws CreateException
	{
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void cleanupWorklocks(TaskID taskID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			for (Worklock worklock : Worklock.getExpiredWorklocks(pm)) {
				worklock.getWorklockType().onReleaseWorklock(worklock, ReleaseReason.clientLost);
				pm.deletePersistent(worklock);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise()
	throws TimePatternFormatException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			TaskID taskID = TaskID.create(
					// Organisation.DEVIL_ORGANISATION_ID, // the task can be modified by the organisation and thus it's maybe more logical to use the real organisationID - not devil
					getOrganisationID(),
					Task.TASK_TYPE_ID_SYSTEM, "JFireBase-WorklockCleanup");
			Task task;
			try {
				task = (Task) pm.getObjectById(taskID);
				task.getActiveExecID();
			} catch (JDOObjectNotFoundException x) {
				task = new Task(
						taskID.organisationID, taskID.taskTypeID, taskID.taskID,
						User.getUser(pm, getOrganisationID(), User.USERID_SYSTEM),
						WorklockManagerHome.JNDI_NAME,
						"cleanupWorklocks");

				task.getName().setText(Locale.ENGLISH.getLanguage(), "Worklock Cleanup");
				task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This Task cleans up expired worklocks.");

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

		} finally {
			pm.close();
		}
	}

	/**
	 * This method first searches for an existing {@link Worklock} on the JDO object
	 * referenced by the given <code>objectID</code> and owned by the current user.
	 * If none such <code>Worklock</code> exists, a new one will be created. If a previously
	 * existing one could be found, its {@link Worklock#setLastAcquireDT()} method will be called
	 * in order to renew it.
	 * <p>
	 * </p>
	 * @param worklockTypeID If a new <code>Worklock</code> is created, it will be assigned the {@link WorklockType}
	 *		referenced by this id. If the Worklock previously existed, this parameter is ignored.
	 * @param objectID The id of the JDO object that shall be locked.
	 * @param description The worklock's description which will be shown to the user and should make clear what is locked (e.g. the name of the object referenced by <code>objectID</code>).
	 * @param fetchGroups The fetch-groups used for detaching the created/queried {@link Worklock}.
	 * @param maxFetchDepth The maximum fetch-depth for detaching the created/queried {@link Worklock}.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public AcquireWorklockResult acquireWorklock(
			WorklockTypeID worklockTypeID, ObjectID objectID, String description,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Worklock worklock = Worklock.getWorklock(pm, objectID, UserID.create(getPrincipal()), getSessionID());
			if (worklock != null) {
				worklock.setLastAcquireDT();
				worklock.setDescription(description);
			}
			else {
				WorklockType worklockType = (WorklockType) pm.getObjectById(worklockTypeID);
				worklock = (Worklock) pm.makePersistent(new Worklock(worklockType, User.getUser(pm, getPrincipal()), getSessionID(), objectID, description));
			}

			long worklockCount = Worklock.getWorklockCount(pm, objectID);

			return new AcquireWorklockResult((Worklock) pm.detachCopy(worklock), worklockCount);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void releaseWorklock(ObjectID objectID, ReleaseReason releaseReason)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Worklock worklock = Worklock.getWorklock(pm, objectID, UserID.create(getPrincipal()), getSessionID());
			if (worklock == null)
				return;

			worklock.getWorklockType().onReleaseWorklock(worklock, releaseReason);
			pm.deletePersistent(worklock);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<WorklockID> getWorklockIDs(ObjectID objectID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return Worklock.getWorklockIDs(pm, objectID);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Worklock> getWorklocks(Collection<WorklockID> worklockIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, worklockIDs, Worklock.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

}
