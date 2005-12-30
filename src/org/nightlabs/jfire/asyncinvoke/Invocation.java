/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.jfire.asyncinvoke;

import java.io.Serializable;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class Invocation
extends BaseInvocation
{

	public Invocation()
	{
	}

	/**
	 * This method is called by the framework and you must call whatever bean
	 * method(s) you need.
	 *
	 * @return
	 * @throws Exception
	 */
	public abstract Serializable invoke()
	throws Exception;

}
