/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.ipanema.asyncinvoke;

import org.nightlabs.ipanema.asyncinvoke.AsyncInvokerDelegateLocal;

/**
 * @ejb.bean name="ipanema/mdb/JFireBaseBean/AsyncInvokerErrorCallback"
 *		 acknowledge-mode="Auto-acknowledge"
 *		 destination-type="javax.jms.Queue"
 *		 transaction-type="Container"
 *		 destination-jndi-name="queue/ipanema/JFireBaseBean/AsyncInvokerErrorCallbackQueue"
 *
 * @ejb.transaction type="Required"
 *
 * @jboss.destination-jndi-name name="queue/ipanema/JFireBaseBean/AsyncInvokerErrorCallbackQueue"
 *
 * @!jboss.subscriber name="_LocalQueueReader_" password="test"
 */
public class AsyncInvokerErrorCallbackBean
extends AsyncInvokerBaseBean
{

	/**
	 * @see org.nightlabs.ipanema.asyncinvoke.AsyncInvokerBaseBean#doInvoke(org.nightlabs.ipanema.asyncinvoke.AsyncInvokeEnvelope, org.nightlabs.ipanema.asyncinvoke.AsyncInvokerDelegateLocal)
	 */
	protected void doInvoke(AsyncInvokeEnvelope envelope, AsyncInvokerDelegateLocal invokerDelegate)
	{
		try {
			invokerDelegate.doErrorCallback(envelope, envelope.getError());
		} catch (Throwable x) {
			logger.fatal("ErrorCallback failed!", x);
			messageContext.setRollbackOnly();
		}
	}

}
