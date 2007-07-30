package org.nightlabs.jfire.asyncinvoke;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import org.nightlabs.annotation.Implement;

public interface DelegateR extends EJBObject, Delegate
{
	@Implement
	public void enqueueErrorCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope )
  throws java.lang.Exception, RemoteException;

	@Implement
	public java.io.Serializable doInvocation( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope )
  throws java.lang.Exception, RemoteException;

	@Implement
	public void doErrorCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope,java.lang.Throwable error )
  throws java.lang.Exception, RemoteException;

	@Implement
	public void doSuccessCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope,java.lang.Object result )
  throws java.lang.Exception, RemoteException;

	@Implement
	public void doUndeliverableCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope )
  throws java.lang.Exception, RemoteException;
}
