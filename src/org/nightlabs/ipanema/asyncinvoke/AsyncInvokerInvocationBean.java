/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.ipanema.asyncinvoke;

import java.io.Serializable;

import org.nightlabs.ipanema.asyncinvoke.AsyncInvokerDelegateLocal;

/**
 * @ejb.bean name="ipanema/mdb/JFireBaseBean/AsyncInvokerInvocation"
 *		 acknowledge-mode="Auto-acknowledge"
 *		 destination-type="javax.jms.Queue"
 *		 transaction-type="Container"
 *		 destination-jndi-name="queue/ipanema/JFireBaseBean/AsyncInvokerInvocationQueue"
 *
 * @ejb.transaction type="Required"
 *
 * @jboss.destination-jndi-name name="queue/ipanema/JFireBaseBean/AsyncInvokerInvocationQueue"
 *
 * @!jboss.subscriber name="_LocalQueueReader_" password="test"
 */
public class AsyncInvokerInvocationBean
extends AsyncInvokerBaseBean
{
	/**
	 * @see org.nightlabs.ipanema.asyncinvoke.AsyncInvokerBaseBean#doInvoke(org.nightlabs.ipanema.asyncinvoke.AsyncInvokeEnvelope, AsyncInvokerDelegateLocal)
	 */
	protected void doInvoke(AsyncInvokeEnvelope envelope, AsyncInvokerDelegateLocal invokerDelegate)
	{
		boolean success = false;
		try {
			Serializable result = invokerDelegate.doInvocation(envelope);
			envelope.setResult(result);
			success = true;
		} catch (Throwable x) {
			logger.error("Invocation failed!", x);
			messageContext.setRollbackOnly();
			try {
				envelope.setError(x);
				invokerDelegate.enqueueErrorCallback(envelope);
			} catch (Throwable x2) {
				logger.fatal("invokerDelegate.enqueueErrorCallback(...) failed!", x2);
			}
		}

		if (success) {
			SuccessCallback successCallback = envelope.getSuccessCallback();
			if (successCallback != null) {
				try {
					new AsyncInvoke().enqueue(AsyncInvoke.QUEUE_SUCCESSCALLBACK, envelope);
				} catch (Throwable x) {
					logger.fatal("Failed to enqueue in AsyncInvoke.QUEUE_SUCCESSCALLBACK!", x);
					messageContext.setRollbackOnly();
				}
			} // if (successCallback != null) {
		} // if (success) {
	}
}
