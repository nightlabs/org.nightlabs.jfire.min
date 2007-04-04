package org.nightlabs.jfire.worklock;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.worklock.id.WorklockID;
import org.nightlabs.jfire.worklock.id.WorklockTypeID;

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
	private static final Logger logger = Logger.getLogger(WorklockManagerBean.class);

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

//	/**
//	 * @ejb.interface-method
//	 * @ejb.transaction type="Required"
//	 * @ejb.permission role-name="_System_"
//	 */
//	public void initialise()
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * This method first searches for an existing {@link Worklock} on the JDO object
	 * referenced by the given <code>objectID</code> and owned by the current user.
	 * If none such <code>Worklock</code> exists, a new one will be created. If a previously
	 * existing one could be found, its {@link Worklock#setLastUseDT()} method will be called
	 * in order to renew it.
	 * <p>
	 * </p>
	 * @param worklockTypeID If a new <code>Worklock</code> is created, it will be assigned the {@link WorklockType}
	 *		referenced by this id. If the Worklock previously existed, this parameter is ignored.
	 * @param objectID The id of the JDO object that shall be locked.
	 * @param fetchGroups The fetch-groups used for detaching the created/queried {@link Worklock}.
	 * @param maxFetchDepth The maximum fetch-depth for detaching the created/queried {@link Worklock}.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public AcquireWorklockResult acquireWorklock(
			WorklockTypeID worklockTypeID, ObjectID objectID,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Worklock worklock = Worklock.getWorklock(pm, objectID, UserID.create(getPrincipal()));
			if (worklock != null)
				worklock.setLastUseDT();
			else {
				WorklockType worklockType = (WorklockType) pm.getObjectById(worklockTypeID);
				worklock = (Worklock) pm.makePersistent(new Worklock(worklockType, User.getUser(pm, getPrincipal()), objectID));
			}

			long worklockCount = Worklock.getWorklockCount(pm, objectID);

			return new AcquireWorklockResult(worklock, worklockCount);
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
