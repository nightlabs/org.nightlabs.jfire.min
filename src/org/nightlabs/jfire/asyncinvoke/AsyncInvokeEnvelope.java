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
import java.util.Date;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.math.Base62Coder;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class AsyncInvokeEnvelope
implements Serializable, IAsyncInvokeEnvelopeReference
{
	public static final long serialVersionUID = 5;

	private static class AsyncInvokeEnvelopeIDGenerator {
		private static String prefix = Base62Coder.sharedInstance().encode(System.currentTimeMillis()) + '.' + Base62Coder.sharedInstance().encode((long)(Math.random() * Base62Coder.sharedInstance().decode("zzzz"))) + '.';
		private static long nextLocalID = 0;
		public static synchronized String nextAsyncInvokeEnvelopeID()
		{
			return prefix + Base62Coder.sharedInstance().encode(nextLocalID++);
		}
	};

	private String asyncInvokeEnvelopeID = AsyncInvokeEnvelopeIDGenerator.nextAsyncInvokeEnvelopeID();

	private Date createDT = new Date();

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

//	/**
//	 * This is used to pass the last exception to the error callback.
//	 */
//	private Throwable error = null;
//
//	private String errorRootCauseClassName = null;
//
//	private String errorClassName = null;
//
//	private String errorMessage = null;
//
//	private String errorStackTrace = null;

	/**
	 * The callback which is triggered when the invocation has been given up after
	 * too many errors. Can be <tt>null</tt>.
	 */
	private UndeliverableCallback undeliverableCallback = null;

	protected static SecurityReflector.UserDescriptor getUserDescriptor()
	throws NamingException
	{
		return SecurityReflector.getUserDescriptor();
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
		assert caller != null : "caller != null";
		assert invocation != null : "invocation != null";

		this.caller = caller;
		this.invocation = invocation;
		this.successCallback = successCallback;
		this.errorCallback = errorCallback;
		this.undeliverableCallback = undeliverableCallback;
	}

	public String getAsyncInvokeEnvelopeID()
	{
		return asyncInvokeEnvelopeID;
	}

	public Date getCreateDT()
	{
		return createDT;
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

	public AsyncInvokeProblem getAsyncInvokeProblem(PersistenceManager pm)
	{
		pm.getExtent(AsyncInvokeProblem.class);
		try {
			return (AsyncInvokeProblem) pm.getObjectById(AsyncInvokeProblemID.create(getAsyncInvokeEnvelopeID()));
		} catch (JDOObjectNotFoundException x) {
			return null;
		}
	}

//	/**
//	 * Get the last error. If there was no error or if the <code>Throwable</code> was not serializable, this returns <code>null</code>.
//	 *
//	 * @return the error or <code>null</code>.
//	 * @see #getErrorClassName()
//	 * @see #getErrorRootCauseClassName()
//	 * @see #getErrorMessage()
//	 * @see #getErrorStackTrace()
//	 */
//	public Throwable getError()
//	{
//		return error;
//	}
//	/**
//	 * @param error The error to set.
//	 */
//	public void setError(Throwable error)
//	{
//		this.error = null;
//		this.errorClassName = null;
//		this.errorRootCauseClassName = null;
//		this.errorMessage = null;
//		this.errorStackTrace = null;
//
//		if (error == null)
//			return;
//
//		this.errorMessage = error.getMessage();
//		this.errorClassName = error.getClass().getName();
//
//		Throwable rootCause = ExceptionUtils.getRootCause(error);
//		this.errorRootCauseClassName = rootCause == null ? null : rootCause.getClass().getName();
//
//		this.errorStackTrace = Util.getStackTraceAsString(error);
//		try {
//			this.error = Util.cloneSerializable(error); // ensure that we can serialize it
//		} catch (Throwable x) {
//			Logger.getLogger(AsyncInvokeEnvelope.class).warn("Could not save the error! Probably it's not entirely serializable.", x);
//		}
//	}
//
//	public String getErrorClassName()
//	{
//		return errorClassName;
//	}
//
//	public String getErrorRootCauseClassName()
//	{
//		return errorRootCauseClassName;
//	}
//
//	public String getErrorMessage()
//	{
//		return errorMessage;
//	}
//
//	/**
//	 * Get the stacktrace of the last error or <code>null</code>, if there was no error.
//	 * <p>
//	 * In contrast to {@link #getError()}, this method always returns a non-<code>null</code> value, after an error occured. This is,
//	 * because even if the occured error is not serializable (i.e. the {@link Throwable} itself or one of objects in the object-graph
//	 * does not implement {@link Serializable}), the String-representation of its stack trace can be serialised.
//	 * </p>
//	 *
//	 * @return the stacktrace of the last error assigned by {@link #setError(Throwable)}
//	 */
//	public String getErrorStackTrace()
//	{
//		return errorStackTrace;
//	}
}
