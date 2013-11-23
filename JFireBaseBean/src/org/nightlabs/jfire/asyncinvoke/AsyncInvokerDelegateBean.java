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

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

// FIXME Maac: what is this interface extends construction about??

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/AsyncInvokerDelegate"
 *           jndi-name="jfire/ejb/JFireBaseBean/AsyncInvokerDelegate"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.interface extends="org.nightlabs.jfire.asyncinvoke.DelegateR" local-extends="org.nightlabs.jfire.asyncinvoke.DelegateL"
 * @!ejb.interface generate="local" // causes the Util class to have compile errors :-(((
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class AsyncInvokerDelegateBean
extends BaseSessionBeanImpl
implements AsyncInvokerDelegateLocal
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AsyncInvokerDelegateBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal#enqueueErrorCallback(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope, org.nightlabs.jfire.asyncinvoke.InvocationError)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public void enqueueErrorCallback(AsyncInvokeEnvelope envelope, InvocationError error)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {
				AsyncInvokeProblem asyncInvokeProblem = AsyncInvokeProblem.createAsyncInvokeProblem(pm, envelope);
				asyncInvokeProblem.addError(error);
				AsyncInvoke.enqueue(AsyncInvoke.QUEUE_ERRORCALLBACK, envelope, true);
			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal#doInvocation(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Serializable doInvocation(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		if (logger.isDebugEnabled())
			logger.debug("doInvocation: principal.organisationID="+getOrganisationID()+" principal.userID="+getUserID()+" envelope.caller.organisationID=" + envelope.getCaller().getOrganisationID() + " envelope.caller.userID=" + envelope.getCaller().getUserID());

		Invocation invocation = envelope.getInvocation();
		invocation.setPrincipal(getPrincipal());
		return invocation.invoke();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal#doErrorCallback(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void doErrorCallback(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		ErrorCallback callback = envelope.getErrorCallback();
		if (callback == null)
			return;

		if (logger.isDebugEnabled())
			logger.debug("doErrorCallback: principal.organisationID="+getOrganisationID()+" principal.userID="+getUserID()+" envelope.caller.organisationID=" + envelope.getCaller().getOrganisationID() + " envelope.caller.userID=" + envelope.getCaller().getUserID());

		callback.setPrincipal(getPrincipal());
		callback.handle(envelope);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal#doSuccessCallback(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope, java.lang.Object)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public void doSuccessCallback(AsyncInvokeEnvelope envelope, Object result)
	throws Exception
	{
		SuccessCallback callback = envelope.getSuccessCallback();
		if (callback == null)
			return;

		if (logger.isDebugEnabled())
			logger.debug("doSuccessCallback: principal.organisationID="+getOrganisationID()+" principal.userID="+getUserID()+" envelope.caller.organisationID=" + envelope.getCaller().getOrganisationID() + " envelope.caller.userID=" + envelope.getCaller().getUserID());

		callback.setPrincipal(getPrincipal());
		callback.handle(envelope, result);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal#doUndeliverableCallback(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public UndeliverableCallbackResult doUndeliverableCallback(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		UndeliverableCallback callback = envelope.getUndeliverableCallback();
		if (callback == null)
			return null;

		if (logger.isDebugEnabled())
			logger.debug("doUndeliverableCallback: principal.organisationID="+getOrganisationID()+" principal.userID="+getUserID()+" envelope.caller.organisationID=" + envelope.getCaller().getOrganisationID() + " envelope.caller.userID=" + envelope.getCaller().getUserID());

		callback.setPrincipal(getPrincipal());
		UndeliverableCallbackResult result = callback.handle(envelope);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal#markAsyncInvokeProblemUndeliverable(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public void markAsyncInvokeProblemUndeliverable(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope, boolean undeliverable)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {
				AsyncInvokeProblem.createAsyncInvokeProblem(pm, envelope).setUndeliverable(undeliverable);
			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.AsyncInvokerDelegateLocal#deleteAsyncInvokeProblem(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public void deleteAsyncInvokeProblem(AsyncInvokeEnvelope envelope)
	throws java.lang.Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {
				AsyncInvokeProblem asyncInvokeProblem = envelope.getAsyncInvokeProblem(pm);
				if (asyncInvokeProblem != null)
					pm.deletePersistent(asyncInvokeProblem);

			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}
		} finally {
			pm.close();
		}
	}
}
