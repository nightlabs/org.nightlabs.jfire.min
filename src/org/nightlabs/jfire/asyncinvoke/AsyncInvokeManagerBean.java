package org.nightlabs.jfire.asyncinvoke;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.PersistenceManager;

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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class AsyncInvokeManagerBean
extends BaseSessionBeanImpl
implements AsyncInvokeManagerRemote
{
	//	private static final Logger logger = Logger.getLogger(AsyncInvokeManagerBean.class);
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokeManagerRemote#getAsyncInvokeProblemIDs()
	 */
	@RolesAllowed("org.nightlabs.jfire.asyncinvoke.administrateAsyncInvokes")
	@SuppressWarnings("unchecked")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokeManagerRemote#getAsyncInvokeProblems(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.asyncinvoke.administrateAsyncInvokes")
	@Override
	public List<AsyncInvokeProblem> getAsyncInvokeProblems(Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, asyncInvokeProblemIDs, AsyncInvokeProblem.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokeManagerRemote#deleteAsyncInvokeProblems(java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.asyncinvoke.administrateAsyncInvokes")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokeManagerRemote#retryAsyncInvokeProblems(java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.asyncinvoke.administrateAsyncInvokes")
	@Override
	public void retryAsyncInvokeProblems(Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs) throws AsyncInvokeEnqueueException
	{
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
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokeManagerRemote#ping(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public String ping(String message) {
		return super.ping(message);
	}
}
