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
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/AsyncInvokerDelegate"
 *           jndi-name="jfire/ejb/JFireBaseBean/AsyncInvokerDelegate"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.interface extends="org.nightlabs.jfire.asyncinvoke.DelegateR" local-extends="org.nightlabs.jfire.asyncinvoke.DelegateL"
 * @!ejb.interface generate="local" // causes the Util class to have compile errors :-((( ... we now need to execute it pseudo-remotely anyway - otherwise there's a problem with cascaded authentication
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class AsyncInvokerDelegateBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AsyncInvokerDelegateBean.class);

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void enqueueErrorCallback(AsyncInvokeEnvelope envelope, InvocationError error)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			AsyncInvokeProblem asyncInvokeProblem = AsyncInvokeProblem.createAsyncInvokeProblem(pm, envelope);
			asyncInvokeProblem.addError(error);

			AsyncInvoke.enqueue(AsyncInvoke.QUEUE_ERRORCALLBACK, envelope, true);
		} finally {
			pm.close();
		}
	}

	/**
	 * Since it is documented in {@link Invocation#invoke()} that {@link AsyncInvokeProblem} must never be accessed there, it
	 * is not necessary to perform a sub-transaction here. Hence it is much cleaner not to do so, hence if this transaction fails,
	 * the queue-item will not be deleted and re-executed in a new try. In other words, popping from the queue and invocating is
	 * done in the same transaction.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Serializable doInvocation(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		if (logger.isDebugEnabled())
			logger.debug("doInvocation: principal.organisationID="+getOrganisationID()+" principal.userID="+getUserID()+" envelope.caller.organisationID=" + envelope.getCaller().getOrganisationID() + " envelope.caller.userID=" + envelope.getCaller().getUserID());

		Invocation invocation = envelope.getInvocation();
		invocation.setPrincipal(getPrincipal());
		return invocation.invoke();
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
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

	/**
	 * This method is executed in a sub-transaction, because {@link #deleteAsyncInvokeProblem(AsyncInvokeEnvelope)} is
	 * called by the container-transaction afterwards in a separate sub-transaction as well. We therefore must have a separate transaction here in order
	 * to safely access the {@link AsyncInvokeProblem} (=> prevent deadlocks).
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
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

	/**
	 * This method is executed in a sub-transaction, because {@link #markAsyncInvokeProblemUndeliverable(AsyncInvokeEnvelope)} is
	 * called by the container-transaction before in a separate sub-transaction as well. We therefore must have a separate transaction here in order
	 * to safely access the {@link AsyncInvokeProblem} (=> prevent deadlocks).
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void doUndeliverableCallback(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		UndeliverableCallback callback = envelope.getUndeliverableCallback();
		if (callback == null)
			return;

		if (logger.isDebugEnabled())
			logger.debug("doUndeliverableCallback: principal.organisationID="+getOrganisationID()+" principal.userID="+getUserID()+" envelope.caller.organisationID=" + envelope.getCaller().getOrganisationID() + " envelope.caller.userID=" + envelope.getCaller().getUserID());

		callback.setPrincipal(getPrincipal());
		callback.handle(envelope);
	}

	/**
	 * Mark the {@link AsyncInvokeProblem} which is corresponding to the given <code>envelope</code> being undeliverable.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
  public void markAsyncInvokeProblemUndeliverable(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope, boolean undeliverable)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			AsyncInvokeProblem.createAsyncInvokeProblem(pm, envelope).setUndeliverable(undeliverable);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void deleteAsyncInvokeProblem(AsyncInvokeEnvelope envelope)
  throws java.lang.Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			// WORKAROUND: TODO: FIXEM: DataNucleaus workaround to initialize table at datastoreinit to avoid bug 0000787
			pm.getExtent(AsyncInvokeProblem.class);

			AsyncInvokeProblem asyncInvokeProblem = envelope.getAsyncInvokeProblem(pm);
			if (asyncInvokeProblem != null)
				pm.deletePersistent(asyncInvokeProblem);
		} finally {
			pm.close();
		}
	}
}
