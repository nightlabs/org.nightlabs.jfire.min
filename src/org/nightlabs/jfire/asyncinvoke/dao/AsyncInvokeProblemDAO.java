package org.nightlabs.jfire.asyncinvoke.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.asyncinvoke.AsyncInvokeManagerRemote;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;
import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class AsyncInvokeProblemDAO
extends BaseJDOObjectDAO<AsyncInvokeProblemID, AsyncInvokeProblem>
{
	private static AsyncInvokeProblemDAO _sharedInstance = null;

	public static synchronized AsyncInvokeProblemDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new AsyncInvokeProblemDAO();

		return _sharedInstance;
	}

	private AsyncInvokeManagerRemote asyncInvokeManager;

	@Override
	protected Collection<AsyncInvokeProblem> retrieveJDOObjects(
			Set<AsyncInvokeProblemID> asyncInvokeProblemIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		AsyncInvokeManagerRemote aim = asyncInvokeManager;
		if (aim == null)
			aim = JFireEjb3Factory.getRemoteBean(AsyncInvokeManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return aim.getAsyncInvokeProblems(asyncInvokeProblemIDs, fetchGroups, maxFetchDepth);
	}

	public List<AsyncInvokeProblem> getAsyncInvokeProblems(
			Set<AsyncInvokeProblemID> asyncInvokeProblemIDs,
			String[] fetchGroups,
			int maxFetchDepth,
			ProgressMonitor monitor)
	{
		return getJDOObjects(null, asyncInvokeProblemIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized List<AsyncInvokeProblem> getAsyncInvokeProblems(
			String[] fetchGroups,
			int maxFetchDepth,
			ProgressMonitor monitor)
	{
		Set<AsyncInvokeProblemID> asyncInvokeProblemIDs;
		try {
			try {
				asyncInvokeManager = JFireEjb3Factory.getRemoteBean(AsyncInvokeManagerRemote.class, SecurityReflector.getInitialContextProperties());
				asyncInvokeProblemIDs = asyncInvokeManager.getAsyncInvokeProblemIDs();
			} catch (Exception x) {
				throw new RuntimeException(x);
			}

			return getJDOObjects(null, asyncInvokeProblemIDs, fetchGroups, maxFetchDepth, monitor);
		} finally {
			asyncInvokeManager = null;
		}
	}

	public void deleteAsyncInvokeProblems(Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs)
	{
		try {
			AsyncInvokeManagerRemote aim = JFireEjb3Factory.getRemoteBean(AsyncInvokeManagerRemote.class, SecurityReflector.getInitialContextProperties());
			aim.deleteAsyncInvokeProblems(asyncInvokeProblemIDs);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public void retryAsyncInvokeProblems(Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs)
	{
		try {
			AsyncInvokeManagerRemote aim = JFireEjb3Factory.getRemoteBean(AsyncInvokeManagerRemote.class, SecurityReflector.getInitialContextProperties());
			aim.retryAsyncInvokeProblems(asyncInvokeProblemIDs);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
