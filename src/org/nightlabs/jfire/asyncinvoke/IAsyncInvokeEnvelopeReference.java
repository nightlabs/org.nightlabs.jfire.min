package org.nightlabs.jfire.asyncinvoke;

import java.io.Serializable;

/**
 * This interface is solely used internally! Do not implement it in any of your classes.
 * The only class implementing it, is org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope
 * and our code expects all instances of this interface to be in fact instances of the
 * said class!
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public interface IAsyncInvokeEnvelopeReference extends Serializable
{
	String getAsyncInvokeEnvelopeID();
}
