/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.ipanema.asyncinvoke;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;

import org.nightlabs.ipanema.base.BaseSessionBeanImpl;

import org.nightlabs.ModuleException;

/**
 * @ejb.bean name="ipanema/ejb/JFireBaseBean/AsyncInvokerDelegate"	
 *           jndi-name="ipanema/ejb/JFireBaseBean/AsyncInvokerDelegate"
 *           type="Stateless" 
 *           transaction-type="Container"
 *
 * @!ejb.interface generate="local" // causes the Util class to have compile errors :-(((
 *
 * @ejb.util generate="physical"
 */
public abstract class AsyncInvokerDelegateBean
extends BaseSessionBeanImpl
implements SessionBean
{
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
	 * @ejb.interface-method view-type = "local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void enqueueErrorCallback(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		new AsyncInvoke().enqueue(AsyncInvoke.QUEUE_ERRORCALLBACK, envelope);
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method view-type = "local"
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Serializable doInvocation(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		Invocation invocation = envelope.getInvocation();
		invocation.setPrincipal(getPrincipal());
		return invocation.invoke();
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method view-type = "local"
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void doErrorCallback(AsyncInvokeEnvelope envelope, Throwable error)
	throws Exception
	{
		ErrorCallback callback = envelope.getErrorCallback();
		if (callback == null)
			return;

		callback.setPrincipal(getPrincipal());
		callback.handle(envelope, error);
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method view-type = "local"
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void doSuccessCallback(AsyncInvokeEnvelope envelope, Object result)
	throws Exception
	{
		SuccessCallback callback = envelope.getSuccessCallback();
		if (callback == null)
			return;

		callback.setPrincipal(getPrincipal());
		callback.handle(envelope, result);
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method view-type = "local"
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void doUndeliverableCallback(AsyncInvokeEnvelope envelope)
	throws Exception
	{
		UndeliverableCallback callback = envelope.getUndeliverableCallback();
		if (callback == null)
			return;

		callback.setPrincipal(getPrincipal());
		callback.handle(envelope);
	}
}
