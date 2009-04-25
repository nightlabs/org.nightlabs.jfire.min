package org.nightlabs.jfire.asyncinvoke;

import java.io.Serializable;

import javax.ejb.Local;

@Local
public interface AsyncInvokerDelegateLocal extends Delegate
{
	void enqueueErrorCallback(AsyncInvokeEnvelope envelope,
			InvocationError error) throws Exception;

	/**
	 * Since it is documented in {@link Invocation#invoke()} that {@link AsyncInvokeProblem} must never be accessed there, it
	 * is not necessary to perform a sub-transaction here. Hence it is much cleaner not to do so, hence if this transaction fails,
	 * the queue-item will not be deleted and re-executed in a new try. In other words, popping from the queue and invocating is
	 * done in the same transaction.
	 */
	Serializable doInvocation(AsyncInvokeEnvelope envelope) throws Exception;

	void doErrorCallback(AsyncInvokeEnvelope envelope) throws Exception;

	/**
	 * This method is executed in a sub-transaction, because {@link #deleteAsyncInvokeProblem(AsyncInvokeEnvelope)} is
	 * called by the container-transaction afterwards in a separate sub-transaction as well. We therefore must have a separate transaction here in order
	 * to safely access the {@link AsyncInvokeProblem} (=> prevent deadlocks).
	 */
	void doSuccessCallback(AsyncInvokeEnvelope envelope, Object result)
			throws Exception;

	/**
	 * This method is executed in a sub-transaction, because {@link #markAsyncInvokeProblemUndeliverable(AsyncInvokeEnvelope)} is
	 * called by the container-transaction before in a separate sub-transaction as well. We therefore must have a separate transaction here in order
	 * to safely access the {@link AsyncInvokeProblem} (=> prevent deadlocks).
	 */
	UndeliverableCallbackResult doUndeliverableCallback(
			AsyncInvokeEnvelope envelope) throws Exception;

	/**
	 * Mark the {@link AsyncInvokeProblem} which is corresponding to the given <code>envelope</code> being undeliverable.
	 */
	void markAsyncInvokeProblemUndeliverable(
			org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope,
			boolean undeliverable) throws Exception;

	void deleteAsyncInvokeProblem(AsyncInvokeEnvelope envelope)
			throws java.lang.Exception;
}