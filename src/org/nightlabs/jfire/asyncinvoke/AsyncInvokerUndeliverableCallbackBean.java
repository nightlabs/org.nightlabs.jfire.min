/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.jfire.asyncinvoke;

import org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal;

/**
 * @ejb.bean name="jfire/mdb/JFireBaseBean/AsyncInvokerUndeliverableCallback"
 *		 acknowledge-mode="Auto-acknowledge"
 *		 destination-type="javax.jms.Queue"
 *		 transaction-type="Container"
 *		 destination-jndi-name="queue/jfire/JFireBaseBean/AsyncInvokerUndeliverableCallbackQueue"
 *
 * @ejb.transaction type="Required"
 *
 * @jboss.destination-jndi-name name="queue/jfire/JFireBaseBean/AsyncInvokerUndeliverableCallbackQueue"
 * @!jboss.destination-jndi-name name="queue/DLQ"
 *
 * @!jboss.subscriber name="_LocalQueueReader_" password="test"
 */
public class AsyncInvokerUndeliverableCallbackBean
extends AsyncInvokerBaseBean
{

	/**
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerBaseBean#doInvoke(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope, org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal)
	 */
	protected void doInvoke(AsyncInvokeEnvelope envelope, AsyncInvokerDelegateLocal invokerDelegate)
	{
		try {
			invokerDelegate.doUndeliverableCallback(envelope);
		} catch (Throwable x) {
			logger.fatal("UndeliverableCallback failed!", x);
			messageContext.setRollbackOnly();
		}
	}

}
