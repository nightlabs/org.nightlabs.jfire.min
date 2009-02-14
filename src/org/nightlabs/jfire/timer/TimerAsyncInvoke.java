package org.nightlabs.jfire.timer;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;
import org.nightlabs.jfire.asyncinvoke.ErrorCallback;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.asyncinvoke.InvocationError;
import org.nightlabs.jfire.asyncinvoke.SuccessCallback;
import org.nightlabs.jfire.asyncinvoke.UndeliverableCallback;
import org.nightlabs.jfire.asyncinvoke.UndeliverableCallbackResult;
import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.jfire.timer.id.TaskID;

public class TimerAsyncInvoke
extends AsyncInvoke
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(TimerAsyncInvoke.class);

	protected TimerAsyncInvoke()
	{
	}

	public static class InvocationParam
	implements Serializable
	{
		private static final long serialVersionUID = 1L;
		public InvocationParam(Task task)
		{
			taskID = (TaskID) JDOHelper.getObjectId(task);
			if (taskID == null)
				throw new IllegalStateException("Could not obtain taskID from Task: " + task);

			bean = task.getBean();
			if (bean == null)
				throw new IllegalStateException("Task.bean is null: " + taskID);

			method = task.getMethod();
			if (method == null)
				throw new IllegalStateException("Task.method is null: " + taskID);

			activeExecID = task.getActiveExecID();
			if (activeExecID == null)
				throw new IllegalStateException("Task.activeExecID is null: " + taskID);
		}

		private TaskID taskID;
		private String bean;
		private String method;
		private String activeExecID;

		public TaskID getTaskID()
		{
			return taskID;
		}
		public String getBean()
		{
			return bean;
		}
		public String getMethod()
		{
			return method;
		}
		public String getActiveExecID()
		{
			return activeExecID;
		}
	}

	public static class TimerInvocation
	extends Invocation
	{
		private static final long serialVersionUID = 1L;

		private InvocationParam invocationParam;
		public InvocationParam getInvocationParam()
		{
			return invocationParam;
		}

		public TimerInvocation(InvocationParam invocationParam)
		{
			if (invocationParam == null)
				throw new IllegalArgumentException("invocationParam must not be null!");

			this.invocationParam = invocationParam;
		}

		@Override
		public Serializable invoke()
		throws Exception
		{
//			try {
//			Thread.sleep(5000); // give the other transaction some time to finish (and write all data)
//			// TO DO isn't there a better solution? Isn't this a JPOX bug anyway?!
//			// NOT necessary anymore - using XA transactions for AsyncInvoke now. Marco.
//			} catch (InterruptedException x) {
//			// ignore
//			}

			try {
				TimerManagerLocal timerManager = TimerManagerUtil.getLocalHome().create();
				if (!timerManager.setExecutingIfActiveExecIDMatches(invocationParam.getTaskID(), invocationParam.getActiveExecID())) {
					logger.info("Cancelled execution of task " + invocationParam.getTaskID() + " because the activeExecID does not match. invocationParam.getActiveExecID()=\""+invocationParam.getActiveExecID()+"\".");
					return null;
				}

				logger.info("Timer invocation: organisationID=\""+getOrganisationID()+"\" userID=\""+getUserID()+"\" taskID=\"" + invocationParam.getTaskID() + "\" bean=\"" + invocationParam.getBean() + "\" method=\""+invocationParam.getMethod()+"\"");
				long startDT = System.currentTimeMillis();
				InitialContext initCtx = new InitialContext();
//				InitialContext ini
				try {
					Object homeRef = initCtx.lookup(invocationParam.getBean());
					Method homeCreate = homeRef.getClass().getMethod("create", (Class[]) null);
					Object bean = homeCreate.invoke(homeRef, (Object[]) null);
					Method beanMethod = bean.getClass().getMethod(invocationParam.getMethod(), new Class[] { TaskID.class });
					beanMethod.invoke(bean, new Object[] { invocationParam.getTaskID() });

					try {
						if (bean instanceof EJBObject)
							((EJBObject)bean).remove();

						if (bean instanceof EJBLocalObject)
							((EJBLocalObject)bean).remove();
					} catch (Exception x) {
						logger.warn(
								"Could not remove bean! TaskID=\""+invocationParam.getTaskID()+"\"" +
								" Bean=\""+invocationParam.getBean()+"\"", x);
					}
				} finally {
					initCtx.close();
				}

				return new Long(System.currentTimeMillis() - startDT); // we pass the duration
			} catch (Throwable t) {
				logger.error("Invocation of ejb method failed: taskID=\"" + invocationParam.getTaskID() + "\" bean=\"" + invocationParam.getBean() + "\" method=\""+invocationParam.getMethod()+"\"", t);

				if (t instanceof Exception)
					throw (Exception)t;
				else if (t instanceof Error)
					throw (Error)t;
				else
					throw new RuntimeException(t);
			}
		}
	}

	public static class TimerSuccessCallback
	extends SuccessCallback
	{
		private static final long serialVersionUID = 1L;

		private InvocationParam invocationParam;
		public InvocationParam getInvocationParam()
		{
			return invocationParam;
		}

		public TimerSuccessCallback(InvocationParam invocationParam)
		{
			if (invocationParam == null)
				throw new IllegalArgumentException("invocationParam must not be null!");

			this.invocationParam = invocationParam;
		}

		@Override
		public void handle(AsyncInvokeEnvelope envelope, Object result)
		throws Exception
		{
			long durationMSec = result == null ? -1 : ((Long)result).longValue();

			PersistenceManager pm = getPersistenceManager();
			try {
				NLJDOHelper.enableTransactionSerializeReadObjects(pm);
				try {

					Task task = (Task) pm.getObjectById(invocationParam.getTaskID());
					if (!invocationParam.getActiveExecID().equals(task.getActiveExecID())) {
						return; // no changes, if we're not active anymore!!!
					}

					if (durationMSec < 0) {
						// We were too fast (the invocation was called already before the TimerManagerBean wrote the
						// new data to the database. Hence, we re-enqueue it. Shouldn't happen with NLJDOHelper.setTransactionSerializeReadObjects(pm, true); anymore, but we better leave this safety check.
						logger.error("Task " + invocationParam.getTaskID() + " was re-enqueued, because the previous invocation was too fast. Should not happen!");
						enqueue(QUEUE_INVOCATION, envelope, false);
						return;
					}

					task.lastExecSuccessful(durationMSec);
				} finally {
					NLJDOHelper.disableTransactionSerializeReadObjects(pm);
				}
			} finally {
				pm.close();
			}
		}
	}

	public static class TimerErrorCallback
	extends ErrorCallback
	{
		private static final long serialVersionUID = 1L;

		private InvocationParam invocationParam;
		public InvocationParam getInvocationParam()
		{
			return invocationParam;
		}

		public TimerErrorCallback(InvocationParam invocationParam)
		{
			if (invocationParam == null)
				throw new IllegalArgumentException("invocationParam must not be null!");

			this.invocationParam = invocationParam;
		}

		@Override
		public void handle(AsyncInvokeEnvelope envelope)
		throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				NLJDOHelper.enableTransactionSerializeReadObjects(pm);
				try {

					Task task = (Task) pm.getObjectById(invocationParam.getTaskID());
					if (!invocationParam.getActiveExecID().equals(task.getActiveExecID())) {
						return; // no changes, if we're not active anymore!!!
					}

					AsyncInvokeProblem asyncInvokeProblem = envelope.getAsyncInvokeProblem(pm);
					if (asyncInvokeProblem == null) {
						logger.warn(
								"AsyncInvokeProblem does not exist: " + AsyncInvokeProblemID.create(envelope.getAsyncInvokeEnvelopeID()),
								new Exception("StackTrace")
						);
						return;
					}

					InvocationError invocationError = asyncInvokeProblem.getLastError();
					if (invocationError.getError() != null) // maybe it was not serializable
						task.lastExecFailed(invocationError.getError());
					else
						task.lastExecFailed(invocationError.getErrorMessage(), invocationError.getErrorStackTrace());

				} finally {
					NLJDOHelper.disableTransactionSerializeReadObjects(pm);
				}
			} finally {
				pm.close();
			}
		}
	}

	public static class TimerUndeliverableCallback
	extends UndeliverableCallback
	{
		private static final long serialVersionUID = 1L;

		private InvocationParam invocationParam;
		public InvocationParam getInvocationParam()
		{
			return invocationParam;
		}

		public TimerUndeliverableCallback(InvocationParam invocationParam)
		{
			if (invocationParam == null)
				throw new IllegalArgumentException("invocationParam must not be null!");

			this.invocationParam = invocationParam;
		}

		@Override
		public UndeliverableCallbackResult handle(AsyncInvokeEnvelope envelope)
		throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				NLJDOHelper.enableTransactionSerializeReadObjects(pm);
				try {

					Task task = (Task) pm.getObjectById(invocationParam.getTaskID());
					if (!invocationParam.getActiveExecID().equals(task.getActiveExecID())) {
						return new UndeliverableCallbackResult(true); // return, because no changes to be done, if we're not active anymore!!!
					}

					task.setActiveExecID(null);
				} finally {
					NLJDOHelper.disableTransactionSerializeReadObjects(pm);
				}
				return new UndeliverableCallbackResult(true); // not necessary to keep AsyncInvokeProblems for timer tasks - they're periodically (re)executed anyway.
			} finally {
				pm.close();
			}
		}
	}

	protected static void exec(Task task, boolean enableXA)
	throws AsyncInvokeEnqueueException
	{
		UserDescriptor caller = new UserDescriptor(
				task.getUser().getOrganisationID(),
				task.getUser().getUserID(),
				task.getWorkstation() == null ? null : task.getWorkstation().getWorkstationID(),
						ObjectIDUtil.makeValidIDString("TimerAsyncInvoke", true));

		if(logger.isDebugEnabled())
			logger.debug("exec: organisationID=" + caller.getOrganisationID() + " userID=" + caller.getUserID());

		InvocationParam invocationParam = new InvocationParam(task);

		TimerInvocation invocation = new TimerInvocation(invocationParam);
		TimerSuccessCallback successCallback = new TimerSuccessCallback(invocationParam);
		TimerErrorCallback errorCallback = new TimerErrorCallback(invocationParam);
		TimerUndeliverableCallback undeliverableCallback = new TimerUndeliverableCallback(invocationParam);

		AsyncInvokeEnvelope envelope = new AsyncInvokeEnvelope(
				caller,
				invocation, successCallback, errorCallback, undeliverableCallback);
		enqueue(QUEUE_INVOCATION, envelope, enableXA);
	}
}
