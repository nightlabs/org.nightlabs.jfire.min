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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.security.SecurityReflector;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class AsyncInvokeEnvelope
implements Serializable
{
	public static final long serialVersionUID = 1;

	private SecurityReflector.UserDescriptor caller;

	/**
	 * The invocation that should be done. Must not be null!
	 */
	private Invocation invocation;

	/**
	 * After invocation, the result will be stored here to be handed over to <tt>successCallback</tt>.
	 */
	private Serializable result = null;

	/**
	 * If not null, this callback will be triggered after a successful invocation.
	 */
	private SuccessCallback successCallback = null;

	/**
	 * The callback which is triggered every time the invocation fails. Can be <tt>null</tt>.
	 */
	private ErrorCallback errorCallback = null;

	/**
	 * This is used to pass the last exception to the error callback.
	 */
	private Throwable error = null;

	/**
	 * The callback which is triggered when the invocation has been given up after
	 * too many errors. Can be <tt>null</tt>.
	 */
	private UndeliverableCallback undeliverableCallback = null;

	protected static SecurityReflector.UserDescriptor getUserDescriptor()
	throws NamingException
	{
		InitialContext initialContext = new InitialContext();
		try {
			return SecurityReflector.lookupSecurityReflector(initialContext).whoAmI();
		} finally {
			initialContext.close();
		}
	}

	public AsyncInvokeEnvelope(
			Invocation invocation,
			SuccessCallback successCallback, ErrorCallback errorCallback,
			UndeliverableCallback undeliverableCallback)
	throws NamingException
	{
		this(getUserDescriptor(),
				invocation,
				successCallback, errorCallback,
				undeliverableCallback);
	}

	public AsyncInvokeEnvelope(
			SecurityReflector.UserDescriptor caller, Invocation invocation,
			SuccessCallback successCallback, ErrorCallback errorCallback,
			UndeliverableCallback undeliverableCallback)
	{
		this.caller = caller;
		this.invocation = invocation;
		this.successCallback = successCallback;
		this.errorCallback = errorCallback;
		this.undeliverableCallback = undeliverableCallback;
	}

	/**
	 * @return Returns the caller.
	 */
	public SecurityReflector.UserDescriptor getCaller()
	{
		return caller;
	}
	/**
	 * @return Returns the successCallback.
	 */
	public SuccessCallback getSuccessCallback()
	{
		return successCallback;
	}
	/**
	 * @param successCallback The successCallback to set.
	 */
	public void setSuccessCallback(SuccessCallback successCallback)
	{
		this.successCallback = successCallback;
	}
	/**
	 * @return Returns the errorCallback.
	 */
	public ErrorCallback getErrorCallback()
	{
		return errorCallback;
	}
	/**
	 * @param errorCallback The errorCallback to set.
	 */
	public void setErrorCallback(ErrorCallback errorCallback)
	{
		this.errorCallback = errorCallback;
	}
	/**
	 * @return Returns the undeliverableCallback.
	 */
	public UndeliverableCallback getUndeliverableCallback()
	{
		return undeliverableCallback;
	}
	/**
	 * @param undeliverableCallback The undeliverableCallback to set.
	 */
	public void setUndeliverableCallback(
			UndeliverableCallback undeliverableCallback)
	{
		this.undeliverableCallback = undeliverableCallback;
	}
	/**
	 * @return Returns the invocation.
	 */
	public Invocation getInvocation()
	{
		return invocation;
	}
	/**
	 * @param invocation The invocation to set.
	 */
	public void setInvocation(Invocation invocation)
	{
		this.invocation = invocation;
	}

	/**
	 * @return Returns the result.
	 */
	public Serializable getResult()
	{
		return result;
	}
	/**
	 * @param result The result to set.
	 */
	public void setResult(Serializable invocationResult)
	{
		this.result = invocationResult;
	}
	/**
	 * @return Returns the error.
	 */
	public Throwable getError()
	{
		return error;
	}
	/**
	 * @param error The error to set.
	 */
	public void setError(Throwable error)
	{
		this.error = error;
	}
}
