package org.nightlabs.jfire.asyncinvoke;

public interface Delegate
{
   public void enqueueErrorCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope )
      throws java.lang.Exception;

   public java.io.Serializable doInvocation( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope )
      throws java.lang.Exception;

   public void doErrorCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope,java.lang.Throwable error )
      throws java.lang.Exception;

   public void doSuccessCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope,java.lang.Object result )
      throws java.lang.Exception;

   public void doUndeliverableCallback( org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope envelope )
      throws java.lang.Exception;
}