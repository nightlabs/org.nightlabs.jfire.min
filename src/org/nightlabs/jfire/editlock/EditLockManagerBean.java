package org.nightlabs.jfire.editlock;

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

import org.nightlabs.ModuleException;
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

/**
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/EditLockManager"
 *		jndi-name="jfire/ejb/JFireBaseBean/EditLockManager
 *		type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class EditLockManagerBean
extends BaseSessionBeanImpl
implements SessionBean 
{
//	private static final Logger logger = Logger.getLogger(EditLockManagerBean.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	public void cleanupEditLocks(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			for (EditLock editLock : EditLock.getExpiredEditLocks(pm)) {
				editLock.getEditLockType().onReleaseEditLock(editLock, ReleaseReason.clientLost);
				pm.deletePersistent(editLock);
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
						User.getUser(pm, getOrganisationID(), User.USERID_SYSTEM),
						EditLockManagerHome.JNDI_NAME,
						"cleanupEditLocks");

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

		} finally {
			pm.close();
		}
	}

	/**
	 * This method first searches for an existing {@link EditLock} on the JDO object
	 * referenced by the given <code>objectID</code> and owned by the current user.
	 * If none such <code>EditLock</code> exists, a new one will be created. If a previously
	 * existing one could be found, its {@link EditLock#setLastAcquireDT()} method will be called
	 * in order to renew it.
	 * <p>
	 * </p>
	 * @param editLockTypeID If a new <code>EditLock</code> is created, it will be assigned the {@link EditLockType}
	 *		referenced by this id. If the EditLock previously existed, this parameter is ignored.
	 * @param objectID The id of the JDO object that shall be locked.
	 * @param description The editLock's description which will be shown to the user and should make clear what is locked (e.g. the name of the object referenced by <code>objectID</code>).
	 * @param fetchGroups The fetch-groups used for detaching the created/queried {@link EditLock}.
	 * @param maxFetchDepth The maximum fetch-depth for detaching the created/queried {@link EditLock}.
	 * @throws ModuleException 
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public AcquireEditLockResult acquireEditLock(
			EditLockTypeID editLockTypeID, ObjectID objectID, String description,
			String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return EditLock.acquireEditLock(pm, UserID.create(getPrincipal()), getSessionID(), editLockTypeID, objectID, description).detachCopy(pm);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void releaseEditLock(ObjectID objectID, ReleaseReason releaseReason)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			EditLock.releaseEditLock(pm, UserID.create(getPrincipal()), getSessionID(), objectID, releaseReason);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<EditLockID> getEditLockIDs(ObjectID objectID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return EditLock.getEditLockIDs(pm, objectID);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<EditLock> getEditLocks(Collection<EditLockID> editLockIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, editLockIDs, EditLock.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

}
