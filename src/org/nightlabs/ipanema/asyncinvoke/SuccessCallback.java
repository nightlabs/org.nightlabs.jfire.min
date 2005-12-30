/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.ipanema.asyncinvoke;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class SuccessCallback
extends BaseInvocation
{

	public SuccessCallback()
	{
	}

	public abstract void handle(AsyncInvokeEnvelope envelope, Object result)
	throws Exception;

}
