/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.jfire.asyncinvoke;

import org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal;

/**
 * @ejb.bean name="jfire/mdb/JFireBaseBean/AsyncInvokerSuccessCallback"
 *		 acknowledge-mode="Auto-acknowledge"
 *		 destination-type="javax.jms.Queue"
 *		 transaction-type="Container"
 *		 destination-jndi-name="queue/jfire/JFireBaseBean/AsyncInvokerSuccessCallbackQueue"
 *
 * @ejb.transaction type="Required"
 *
 * @jboss.destination-jndi-name name="queue/jfire/JFireBaseBean/AsyncInvokerSuccessCallbackQueue"
 *
 * @!jboss.subscriber name="_LocalQueueReader_" password="test"
 */
public class AsyncInvokerSuccessCallbackBean
extends AsyncInvokerBaseBean
{

	/**
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerBaseBean#doInvoke(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope, org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal)
	 */
	protected void doInvoke(AsyncInvokeEnvelope envelope, AsyncInvokerDelegateLocal invokerDelegate)
	{
		try {
			invokerDelegate.doSuccessCallback(envelope, envelope.getResult());
		} catch (Throwable x) {
			logger.fatal("SuccessCallback failed!", x);
			messageContext.setRollbackOnly();
		}
	}

}
