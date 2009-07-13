package org.nightlabs.jfire.asyncinvoke;

import java.io.Serializable;

import javax.jdo.listener.DeleteCallback;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.asyncinvoke.id.InvocationErrorID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.asyncinvoke.id.InvocationErrorID"
 *		detachable="true"
 *		table="JFireBase_InvocationError"
 *
 * @jdo.create-objectid-class field-order="asyncInvokeEnvelopeID, errorID"
 *
 * @jdo.fetch-group name="AsyncInvokeProblem.lastError" fields="asyncInvokeProblem"
 */
@PersistenceCapable(
	objectIdClass=InvocationErrorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_InvocationError")
@FetchGroups(
	@FetchGroup(
		name="AsyncInvokeProblem.lastError",
		members=@Persistent(name="asyncInvokeProblem"))
)
public class InvocationError
implements Serializable, DeleteCallback
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String asyncInvokeEnvelopeID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private int errorID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private AsyncInvokeProblem asyncInvokeProblem;

	/**
	 * This is used to pass the last exception to the error callback.
	 *
	 * @jdo.field persistence-modifier="persistent" serialized="true"
	 */
	@Persistent(
		serialized="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Object error = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String errorRootCauseClassName = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String errorClassName = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String errorMessage = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String errorStackTrace = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected InvocationError() { }

	public InvocationError(IAsyncInvokeEnvelopeReference asyncInvokeEnvelopeReference, Throwable error)
	{
		this.asyncInvokeEnvelopeID = asyncInvokeEnvelopeReference.getAsyncInvokeEnvelopeID();
		this.setError(error);
	}

	protected void init(AsyncInvokeProblem asyncInvokeProblem, int errorID)
	{
		if (this.asyncInvokeProblem != null)
			throw new IllegalStateException("Already initialized!");

		this.asyncInvokeProblem = asyncInvokeProblem;
		if (!this.asyncInvokeEnvelopeID.equals(asyncInvokeProblem.getAsyncInvokeEnvelopeID()))
			throw new IllegalArgumentException("this.asyncInvokeEnvelopeID != asyncInvokeProblem.asyncInvokeEnvelopeID");

		assert errorID >= 0 : "errorID >= 0";
		this.errorID = errorID;
	}

	public String getAsyncInvokeEnvelopeID()
	{
		return asyncInvokeEnvelopeID;
	}
	public int getErrorID()
	{
		return errorID;
	}
	public AsyncInvokeProblem getAsyncInvokeProblem()
	{
		return asyncInvokeProblem;
	}

	/**
	 * Get the last error. If there was no error or if the <code>Throwable</code> was not serializable, this returns <code>null</code>.
	 *
	 * @return the error or <code>null</code>.
	 * @see #getErrorClassName()
	 * @see #getErrorRootCauseClassName()
	 * @see #getErrorMessage()
	 * @see #getErrorStackTrace()
	 */
	public Throwable getError()
	{
		return (Throwable) error;
	}
	/**
	 * @param error The error to set.
	 */
	protected void setError(Throwable error)
	{
		this.error = null;
		this.errorClassName = null;
		this.errorRootCauseClassName = null;
		this.errorMessage = null;
		this.errorStackTrace = null;

		if (error == null)
			return;

		this.errorMessage = error.getMessage();
		this.errorClassName = error.getClass().getName();

		Throwable rootCause = ExceptionUtils.getRootCause(error);
		this.errorRootCauseClassName = rootCause == null ? null : rootCause.getClass().getName();

		this.errorStackTrace = Util.getStackTraceAsString(error);
		try {
			this.error = Util.cloneSerializable(error); // ensure that we can serialize it
		} catch (Throwable x) {
			Logger.getLogger(InvocationError.class).warn("Could not save the error! Probably it's not entirely serializable.", x);
		}
	}

	public String getErrorClassName()
	{
		return errorClassName;
	}

	public String getErrorRootCauseClassName()
	{
		return errorRootCauseClassName;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * Get the stacktrace of the last error or <code>null</code>, if there was no error.
	 * <p>
	 * In contrast to {@link #getError()}, this method always returns a non-<code>null</code> value, after an error occured. This is,
	 * because even if the occured error is not serializable (i.e. the {@link Throwable} itself or one of objects in the object-graph
	 * does not implement {@link Serializable}), the String-representation of its stack trace can be serialised.
	 * </p>
	 *
	 * @return the stacktrace of the last error assigned by {@link #setError(Throwable)}
	 */
	public String getErrorStackTrace()
	{
		return errorStackTrace;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((asyncInvokeEnvelopeID == null) ? 0 : asyncInvokeEnvelopeID.hashCode());
		result = prime * result + errorID;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof InvocationError)) return false;
		final InvocationError other = (InvocationError) obj;
		return Util.equals(this.asyncInvokeEnvelopeID, other.asyncInvokeEnvelopeID)
				&& Util.equals(this.errorID, other.errorID);
	}

	@Override
	public void jdoPreDelete()
	{
		if (this.equals(asyncInvokeProblem.getLastError()))
			asyncInvokeProblem.jdoPreDelete();
	}
}
