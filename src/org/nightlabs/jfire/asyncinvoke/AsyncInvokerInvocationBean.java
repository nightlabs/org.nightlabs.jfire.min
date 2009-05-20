/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.asyncinvoke;

import java.io.Serializable;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 * @ejb.bean name="jfire/mdb/JFireBaseBean/AsyncInvokerInvocation"
 *		 acknowledge-mode="Auto-acknowledge"
 *		 destination-type="javax.jms.Queue"
 *		 transaction-type="Container"
 *		 destination-jndi-name="queue/jfire/JFireBaseBean/AsyncInvokerInvocationQueue"
 *
 * @ejb.transaction type="Required"
 *
 * @jboss.destination-jndi-name name="queue/jfire/JFireBaseBean/AsyncInvokerInvocationQueue"
 *
 * @!jboss.subscriber name="_LocalQueueReader_" password="test"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
/*
 * See http://www.jboss.org/file-access/default/members/jbossejb3/freezone/docs/tutorial/1.0.6/html/Message_Driven_Beans.html
 * TODO Maybe it would be better to configure some of these settings in a configuration file?! Marco.
 */
@MessageDriven(
		name="jfire/mdb/JFireBaseBean/AsyncInvokerInvocation",
		activationConfig={
				@ActivationConfigProperty(
						propertyName="acknowledgeMode",
						propertyValue="Auto-acknowledge"
				),
				@ActivationConfigProperty(
						propertyName="destinationType",
						propertyValue="javax.jms.Queue"
				),
				@ActivationConfigProperty(
						propertyName="destination",
						propertyValue="queue/jfire/JFireBaseBean/AsyncInvokerInvocationQueue"
				),
				@ActivationConfigProperty(
						propertyName="maxSession",
						propertyValue="15" // How many concurrent invocations are processed?
				),
				@ActivationConfigProperty(
						propertyName="dLQJNDIName",
						propertyValue="queue/jfire/JFireBaseBean/AsyncInvokerUndeliverableCallbackQueue" // If processing fails and even retrying doesn't help, deliver it to this queue.
				),
				@ActivationConfigProperty(
						propertyName="dLQMaxResent",
						propertyValue="5" // How often should it be retried before sending to the DLQ.
				),
		}
)
public class AsyncInvokerInvocationBean
extends AsyncInvokerBaseBean
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doInvoke(AsyncInvokeEnvelope envelope, Delegate invokerDelegate)
	{
		boolean rollbackOnly = false;
		boolean success = false;
		try {
			Serializable result = invokerDelegate.doInvocation(envelope);
			envelope.setResult(result);
			success = true;
		} catch (Throwable x) {
			logger().error("Invocation failed! asyncInvokeEnvelopeID=" + envelope.getAsyncInvokeEnvelopeID(), x);
			rollbackOnly = true; // put back into queue
			try {
//				envelope.setError(x);
				InvocationError invocationError = new InvocationError(envelope, x);
				invokerDelegate.enqueueErrorCallback(envelope, invocationError);
			} catch (Throwable x2) {
				logger().fatal("invokerDelegate.enqueueErrorCallback(...) failed! asyncInvokeEnvelopeID=" + envelope.getAsyncInvokeEnvelopeID(), x2);
			}
		}

		if (success) {
			SuccessCallback successCallback = envelope.getSuccessCallback();
			if (successCallback != null) {
				try {
					AsyncInvoke.enqueue(AsyncInvoke.QUEUE_SUCCESSCALLBACK, envelope, true);
				} catch (Throwable x) {
					logger().fatal("Failed to enqueue in AsyncInvoke.QUEUE_SUCCESSCALLBACK! asyncInvokeEnvelopeID=" + envelope.getAsyncInvokeEnvelopeID(), x);
					rollbackOnly = true; // put back into queue // TODO really? we already executed the invocation - shall we really do this?
				}
			} // if (successCallback != null) {
			else {
				try {
					invokerDelegate.deleteAsyncInvokeProblem(envelope);
				} catch (Throwable x) {
					logger().fatal("invokerDelegate.deleteAsyncInvokeProblem(...) failed! asyncInvokeEnvelopeID=" + envelope.getAsyncInvokeEnvelopeID(), x);
				}
			}
		} // if (success) {

		if (rollbackOnly)
			messageContext.setRollbackOnly();
	}
}
