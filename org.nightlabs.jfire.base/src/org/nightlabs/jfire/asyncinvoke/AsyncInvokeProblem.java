package org.nightlabs.jfire.asyncinvoke;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DetachCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * Instances of this class track problems that happened when executing an asynchronous invocation
 * as documented here: https://www.jfire.org/modules/phpwiki/index.php/Framework%20AsyncInvoke
 * <p>
 * An instance of this class is created and persisted when the first invocation failed (before the error-callback is triggered).
 * Every subsequent invocation error updates this instance then.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID"
 *		detachable="true"
 *		table="JFireBase_AsyncInvokeProblem"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.fetch-group name="AsyncInvokeProblem.asyncInvokeEnvelope" fields="asyncInvokeEnvelope"
 * @jdo.fetch-group name="AsyncInvokeProblem.errors" fields="errors"
 * @jdo.fetch-group name="AsyncInvokeProblem.lastError" fields="lastError"
 */
@PersistenceCapable(
	objectIdClass=AsyncInvokeProblemID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_AsyncInvokeProblem")
@FetchGroups({
	@FetchGroup(
		name=AsyncInvokeProblem.FETCH_GROUP_ASYNC_INVOKE_ENVELOPE,
		members=@Persistent(name="asyncInvokeEnvelope")),
	@FetchGroup(
		name=AsyncInvokeProblem.FETCH_GROUP_ERRORS,
		members=@Persistent(name="errors")),
	@FetchGroup(
		name=AsyncInvokeProblem.FETCH_GROUP_LAST_ERROR,
		members=@Persistent(name="lastError"))
})
public class AsyncInvokeProblem
implements Serializable, DetachCallback, DeleteCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_ASYNC_INVOKE_ENVELOPE = "AsyncInvokeProblem.asyncInvokeEnvelope";
	public static final String FETCH_GROUP_ERRORS = "AsyncInvokeProblem.errors";
	public static final String FETCH_GROUP_LAST_ERROR = "AsyncInvokeProblem.lastError";

	/**
	 * This is a virtual fetch-group processed in the detach-callback.
	 */
	public static final String FETCH_GROUP_ERROR_COUNT = "AsyncInvokeProblem.errors";

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String asyncInvokeEnvelopeID;

	/**
	 * @jdo.field persistence-modifier="persistent" serialized="true"
	 */
	@Persistent(
		serialized="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Object asyncInvokeEnvelope;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean undeliverable;

	/**
	 * @jdo.field
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.asyncinvoke.InvocationError"
	 *		dependent-element="true"
	 *		mapped-by="asyncInvokeProblem"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="asyncInvokeProblem")
	private List<InvocationError> errors;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private int errorCount = -1;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private InvocationError lastError;

	public static AsyncInvokeProblem createAsyncInvokeProblem(PersistenceManager pm, IAsyncInvokeEnvelopeReference asyncInvokeEnvelopeReference)
	{
		AsyncInvokeProblem asyncInvokeProblem;
		try {
			asyncInvokeProblem = (AsyncInvokeProblem) pm.getObjectById(AsyncInvokeProblemID.create(asyncInvokeEnvelopeReference.getAsyncInvokeEnvelopeID()));
		} catch (JDOObjectNotFoundException x) {
			asyncInvokeProblem = pm.makePersistent(new AsyncInvokeProblem(asyncInvokeEnvelopeReference));
		}
		return asyncInvokeProblem;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected AsyncInvokeProblem() { }

	public AsyncInvokeProblem(IAsyncInvokeEnvelopeReference asyncInvokeEnvelopeReference)
	{
		this.asyncInvokeEnvelopeID = asyncInvokeEnvelopeReference.getAsyncInvokeEnvelopeID();
		this.asyncInvokeEnvelope = asyncInvokeEnvelopeReference;
		ObjectIDUtil.assertValidIDString(asyncInvokeEnvelopeID, "asyncInvokeEnvelopeID");

		errors = new ArrayList<InvocationError>();
	}

	public String getAsyncInvokeEnvelopeID()
	{
		return asyncInvokeEnvelopeID;
	}
	public IAsyncInvokeEnvelopeReference getAsyncInvokeEnvelope()
	{
		return (IAsyncInvokeEnvelopeReference) asyncInvokeEnvelope;
	}

	public List<InvocationError> getErrors()
	{
		return Collections.unmodifiableList(errors);
	}

	public InvocationError getLastError()
	{
		return lastError;
	}

	public void addError(InvocationError error) {
		errorCount = errors.size();
		error.init(this, errorCount);
		errors.add(error);
		lastError = error;
		++errorCount;
	}

	public boolean isUndeliverable()
	{
		return undeliverable;
	}

	public void setUndeliverable(boolean undeliverable)
	{
		this.undeliverable = undeliverable;
	}

	@Override
	public int hashCode()
	{
		return 31 + ((asyncInvokeEnvelopeID == null) ? 0 : asyncInvokeEnvelopeID.hashCode());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof AsyncInvokeProblem)) return false;
		final AsyncInvokeProblem other = (AsyncInvokeProblem) obj;
		return Util.equals(this.asyncInvokeEnvelopeID, other.asyncInvokeEnvelopeID);
	}

	public int getErrorCount()
	{
		if (errorCount < 0)
			errorCount = errors.size();

		return errorCount;
	}

	@Override
	public void jdoPostDetach(Object o)
	{
		AsyncInvokeProblem detached = this;
		AsyncInvokeProblem attached = (AsyncInvokeProblem) o;
		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		if (attached.errorCount >= 0 || pm.getFetchPlan().getGroups().contains(FETCH_GROUP_ERROR_COUNT))
			detached.errorCount = attached.getErrorCount();
	}

	@Override
	public void jdoPreDetach()
	{
		// nothing to do
	}

	@Override
	public void jdoPreDelete()
	{
		try {
			lastError = null;
			JDOHelper.getPersistenceManager(this).flush();
		} catch (Exception x) {
			Logger.getLogger(AsyncInvokeProblem.class).warn("error in pre-delete-callback!", x);
		}
	}
}
