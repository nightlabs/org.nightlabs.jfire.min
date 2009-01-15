package org.nightlabs.jfire.asyncinvoke;

public class UndeliverableCallbackResult
{
	private boolean deleteAsyncInvokeProblem = false;

	public UndeliverableCallbackResult() {
	}

	public UndeliverableCallbackResult(boolean deleteAsyncInvokeProblem) {
		this.deleteAsyncInvokeProblem = deleteAsyncInvokeProblem;
	}

	/**
	 * Whether to delete the {@link AsyncInvokeProblem}. If this is <code>true</code>, the
	 * <code>AsyncInvokeProblem</code> is deleted.
	 * <p>
	 * This feature is for example used by the timer tasks. Since timers are executed periodically
	 * anyway, it's not necessary to keep an <code>AsyncInvokeProblem</code> for the administrator
	 * to execute it again.
	 * </p>
	 *
	 * @return a flag indicating whether the associated {@link AsyncInvokeProblem} shall be deleted.
	 */
	public boolean isDeleteAsyncInvokeProblem() {
		return deleteAsyncInvokeProblem;
	}
	public void setDeleteAsyncInvokeProblem(boolean deleteAsyncInvokeProblem) {
		this.deleteAsyncInvokeProblem = deleteAsyncInvokeProblem;
	}
}
