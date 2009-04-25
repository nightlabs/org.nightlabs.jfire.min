package org.nightlabs.jfire.asyncinvoke;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;

@Remote
public interface AsyncInvokeManagerRemote
{
	Set<AsyncInvokeProblemID> getAsyncInvokeProblemIDs();

	List<AsyncInvokeProblem> getAsyncInvokeProblems(
			Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Delete the {@link AsyncInvokeProblem} instances specified by <code>asyncInvokeProblemIDs</code>.
	 * Only undeliverable problems can be deleted. Every problem where {@link AsyncInvokeProblem#isUndeliverable()}
	 * returns <code>false</code> is silently ignored (i.e. not deleted).
	 *
	 * @param asyncInvokeProblemIDs the identifiers of the objects to be deleted.
	 */
	void deleteAsyncInvokeProblems(
			Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs);

	/**
	 * Retry invocation of the {@link AsyncInvokeProblem} instances specified by <code>asyncInvokeProblemIDs</code>.
	 * Only undeliverable problems can be rescheduled. Every problem where {@link AsyncInvokeProblem#isUndeliverable()}
	 * returns <code>false</code> is silently ignored.
	 *
	 * @param asyncInvokeProblemIDs the identifiers of the invocations that shall be retried.
	 * @throws AsyncInvokeEnqueueException If creating the async invoke envelope or enqueueing the invokation failed
	 */
	void retryAsyncInvokeProblems(
			Collection<AsyncInvokeProblemID> asyncInvokeProblemIDs)
			throws AsyncInvokeEnqueueException;

	String ping(String message);
}