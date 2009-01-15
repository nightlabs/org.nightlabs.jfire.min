package org.nightlabs.jfire.asyncinvoke;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface DelegateR extends EJBObject, Delegate
{
	@Override
	public void enqueueErrorCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope, InvocationError error)
	throws java.lang.Exception, RemoteException;

	@Override
	public java.io.Serializable doInvocation( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope )
	throws java.lang.Exception, RemoteException;

	@Override
	public void doErrorCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope)
	throws java.lang.Exception, RemoteException;

	@Override
	public void doSuccessCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope,java.lang.Object result )
	throws java.lang.Exception, RemoteException;

	@Override
	public UndeliverableCallbackResult doUndeliverableCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope )
	throws java.lang.Exception, RemoteException;

	@Override
	public void markAsyncInvokeProblemUndeliverable(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope, boolean undeliverable)
	throws java.lang.Exception, RemoteException;

	@Override
	public void deleteAsyncInvokeProblem(AsyncInvokeEnvelope envelope)
	throws java.lang.Exception, RemoteException;
}
