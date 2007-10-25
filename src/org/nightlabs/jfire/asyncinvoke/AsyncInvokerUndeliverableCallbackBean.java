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
	private static final long serialVersionUID = 1L;

	@Override
	protected void doInvoke(AsyncInvokeEnvelope envelope, Delegate invokerDelegate)
	{
		try {
			invokerDelegate.doUndeliverableCallback(envelope);
		} catch (Throwable x) {
			logger().fatal("UndeliverableCallback failed!", x);
			messageContext.setRollbackOnly();
		}
	}

}
