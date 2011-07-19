package org.nightlabs.jfire.init;

public abstract class InvocationInit extends AbstractInit<InvocationInit, InvocationInitDependency>
{
	private static final long serialVersionUID = 1L;

	private String invocation;
	private int priority;

	public InvocationInit(String invocation, int priority)
	{
		if (invocation == null)
			throw new IllegalArgumentException("invocation == null");

		this.invocation = invocation;
		this.priority = priority;
		getBean(); // test validity of invocation.
	}

	@Override
	protected String getName() {
		return this.getClass().getSimpleName() + ": " + invocation + " (priority: " + priority + ")";
	}
	
	public String getInvocation() {
		return invocation;
	}
	
	public void setInvocation(String invocation) {
		this.invocation = invocation;
		this.invocationPath = null;
	}
	
	private String[] invocationPath;
	
	public String[] getInvocationPath() {
		if (invocationPath == null) {
			this.invocationPath = invocation.split(".");
		}
		return invocationPath;
	}

	public String getBean()
	{
		int idx = invocation.lastIndexOf('.');
		if (idx < 0)
			throw new IllegalStateException("There is no '.' in the invocation!");

		return invocation.substring(0, idx);
	}

	public String getMethod()
	{
		String[] path = getInvocationPath();
		return path[path.length - 1];
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority
	 *          The priority to set.
	 */
	public void setPriority(int priority) {
		if (priority < 0 || priority > 999)
			throw new IllegalArgumentException("Priority out of range! must be 0..999");

		this.priority = priority;
	}
}