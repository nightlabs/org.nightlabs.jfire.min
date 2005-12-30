/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.jfire.asyncinvoke;


/**
 * This callback is triggered, when an invocation does not succeed. This happens at every
 * retry, means this callback is not definite.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class ErrorCallback
extends BaseInvocation
{
	public ErrorCallback()
	{
	}

	public abstract void handle(AsyncInvokeEnvelope envelope, Throwable error)
	throws Exception;
}
