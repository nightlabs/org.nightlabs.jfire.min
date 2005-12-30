/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.ipanema.asyncinvoke;

/**
 * This callback is triggered after the last invocation retry has failed. Hence,
 * this callback happens either none or one time for an invocation and therefore is
 * definite.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class UndeliverableCallback
extends BaseInvocation
{

	public UndeliverableCallback()
	{
	}

	public abstract void handle(AsyncInvokeEnvelope envelope)
	throws Exception;
}
