package org.nightlabs.jfire.asyncinvoke;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/AsyncInvokeManager"
 *           jndi-name="jfire/ejb/JFireBaseBean/AsyncInvokeManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class AsyncInvokeManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
//	private static final Logger logger = Logger.getLogger(AsyncInvokeManagerBean.class);
	private static final long serialVersionUID = 1L;

	@Override
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException {
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
	public void ejbCreate() throws CreateException { }

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	@Implement
	@Override
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<AsyncInvokeProblemID> getAsyncInvokeProblemIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			javax.jdo.Query q = pm.newQuery(AsyncInvokeProblem.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<AsyncInvokeProblemID>((Collection<? extends AsyncInvokeProblemID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<AsyncInvokeProblem> getAsyncInvokeProblems(Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, asyncInvokeProblemIDs, AsyncInvokeProblem.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Delete the {@link AsyncInvokeProblem} instances specified by <code>asyncInvokeProblemIDs</code>.
	 * Only undeliverable problems can be deleted. Every problem where {@link AsyncInvokeProblem#isUndeliverable()}
	 * returns <code>false</code> is silently ignored (i.e. not deleted).
	 *
	 * @param asyncInvokeProblemIDs the identifiers of the objects to be deleted.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public void deleteAsyncInvokeProblems(Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Set<AsyncInvokeProblem> problems = NLJDOHelper.getObjectSet(pm, asyncInvokeProblemIDs, AsyncInvokeProblem.class);
			for (Iterator<AsyncInvokeProblem> it = problems.iterator(); it.hasNext();) {
				AsyncInvokeProblem problem = it.next();
				if (!problem.isUndeliverable())
					it.remove();
			}

			pm.deletePersistentAll(problems);
		} finally {
			pm.close();
		}
	}

	/**
	 * Retry invocation of the {@link AsyncInvokeProblem} instances specified by <code>asyncInvokeProblemIDs</code>.
	 * Only undeliverable problems can be rescheduled. Every problem where {@link AsyncInvokeProblem#isUndeliverable()}
	 * returns <code>false</code> is silently ignored.
	 *
	 * @param asyncInvokeProblemIDs the identifiers of the invocations that shall be retried.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public void retryAsyncInvokeProblems(Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs)
	throws ModuleException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				Set<AsyncInvokeProblem> problems = NLJDOHelper.getObjectSet(pm, asyncInvokeProblemIDs, AsyncInvokeProblem.class);
				for (Iterator<AsyncInvokeProblem> it = problems.iterator(); it.hasNext();) {
					AsyncInvokeProblem problem = it.next();
					if (problem.isUndeliverable()) {
						problem.setUndeliverable(false);
						AsyncInvoke.enqueue(AsyncInvoke.QUEUE_INVOCATION, (AsyncInvokeEnvelope) problem.getAsyncInvokeEnvelope(), true);
					}
				}
			} finally {
				pm.close();
			}
		} catch (Exception x) { throw new ModuleException(x); }
	}
}
