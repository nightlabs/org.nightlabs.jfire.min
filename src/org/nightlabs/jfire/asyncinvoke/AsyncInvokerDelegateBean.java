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

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
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
 */
public abstract class AsyncInvokerDelegateBean
extends BaseSessionBeanImpl
implements SessionBean
{
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void enqueueErrorCallback(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		AsyncInvoke.enqueue(AsyncInvoke.QUEUE_ERRORCALLBACK, envelope, true);
	}

	/**
	 * @throws ModuleException
	 *
	 * @!ejb.interface-method view-type="local"
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
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
	 * @throws ModuleException
	 *
	 * @!ejb.interface-method view-type="local"
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void doErrorCallback(AsyncInvokeEnvelope envelope, Throwable error)
	throws Exception
	{
		ErrorCallback callback = envelope.getErrorCallback();
		if (callback == null)
			return;

		if (logger.isDebugEnabled())
			logger.debug("doErrorCallback: principal.organisationID="+getOrganisationID()+" principal.userID="+getUserID()+" envelope.caller.organisationID=" + envelope.getCaller().getOrganisationID() + " envelope.caller.userID=" + envelope.getCaller().getUserID());

		callback.setPrincipal(getPrincipal());
		callback.handle(envelope, error);
	}

	/**
	 * @throws ModuleException
	 *
	 * @!ejb.interface-method view-type="local"
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
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
	 * @throws ModuleException
	 *
	 * @!ejb.interface-method view-type="local"
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
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
}
