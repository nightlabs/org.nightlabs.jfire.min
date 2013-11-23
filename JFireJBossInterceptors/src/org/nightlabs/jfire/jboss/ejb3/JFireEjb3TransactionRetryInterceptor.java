package org.nightlabs.jfire.jboss.ejb3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.logging.Logger;

/**
 * Interceptor that will retry the invocation upon an error for a configurable amount of times
 * in order to give the server the possibility to retry a transaction in case it failed
 * because of a technical reason such as a deadlock in the datastore-backend.
 * <p>
 * Whether or not this interceptor should attempt to retry a failed transaction,
 * the number of retry-attempts as well as the time to wait between each try can be configured using system-properties.
 * </p>
 *
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber - alex [at] nightlabs [dot] de
 */
public class JFireEjb3TransactionRetryInterceptor implements Interceptor
{
	/**
	 * Name of the system-property to configure whether this interceptor should retry failed transactions.
	 * Set the value of this property to a boolean value.
	 */
	public static final String SYSTEM_PROPERY_NAME_RETRY_TRANSACTIONS = JFireEjb3TransactionRetryInterceptor.class.getName() + "#retryTransactions";
	/**
	 * Name of the system-property to configure how many times this interceptor should retry a failed transaction.
	 * Set the value of this property to an int value.
	 */
	public static final String SYSTEM_PROPERY_NAME_RETRY_COUNT = JFireEjb3TransactionRetryInterceptor.class.getName() + "#retryCount";
	/**
	 * Name of the system-property to configure the amount of milliseconds this interceptor should sleep between retrying failed transaction.
	 * Set the value of this property to a long value.
	 */
	public static final String SYSTEM_PROPERY_NAME_RETRY_SLEEP = JFireEjb3TransactionRetryInterceptor.class.getName() + "#retrySleep";

	private static final Logger logger = Logger.getLogger(JFireEjb3TransactionRetryInterceptor.class);

	/**Default max retry times before finally give-up*/
	private static final int defaultRetryCount = 6;

	/** Default number of ms to sleep before each attempt to retry calling the method again */
	private static transient long defaultSleepTime = 200;

	public JFireEjb3TransactionRetryInterceptor()
	{
		logger.debug("JFireEjbTransactionInterceptor has been initialized !!!");
	}

	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public Object invoke(final Invocation invocation) throws Throwable {
		Object result = null;
		int retryCount = 0;
		Throwable originalException = null;
		Boolean doRetry = isRetryTransactions();
		
		if (!doRetry) {
			return invocation.invokeNext();
		}
		
		Integer retryTimes = getRetryTimes();
		long retrySleepTime = getSleepTime();

		while (true) {
			try {
				Invocation invocationCopy = copyMethodInvocation(invocation);
				if (invocation == invocationCopy) {
					logger.debug("invoke: copyMethodInvocation(...) did not copy. Deactivating retry.");
					doRetry = false;
				}

				result = invocationCopy.invokeNext();
				break;
			} catch (final Exception e) {
				if (originalException == null) {
					originalException = e;
				}
				// If we should not retry, we have to re-throw the originalException.
				if (!doRetry)
				{
					logger.error("Caught an exception, but will not retry failed transaction.", originalException);
					throw originalException;
				}

				logger.debug("before retry invoking sleeping for ms:" + retrySleepTime);
				Thread.sleep(retrySleepTime);
				if (retryCount >= retryTimes) {
					logger.error("Failed to successfully retry transaction due to exception: " + e, e);
//					throw e;
					// We throw the first exception, because currently the following exceptions are all the same due
					// to the tx already being aborted and not restarted when retrying.
					// See: https://www.jfire.org/modules/bugs/view.php?id=1810
					throw originalException;
				} else {
					logger.warn("Caught exception (will retry again): " + e, e);
				}

				retryCount++;
				retrySleepTime+=100;
			}
		}
		return result;
	}

	private static Field methodInvocation_argumentsField;

	/**
	 * Copy the given invocation, if it is a {@link MethodInvocation}. Other implementations
	 * of {@link Invocation} are not copied. Additionally to calling {@link Invocation#copy()}, this method
	 * clones the method arguments via Java native serialization, in order to guarantee that a repetition of the
	 * EJB method call starts with exactly the same arguments (without modifications done by a previous [rolled back]
	 * invocation).
	 * @param invocation the object to be copied.
	 * @return the copy, if possible &amp; adequate; the original, if copying was not possible or not adequate.
	 */
	private Invocation copyMethodInvocation(final Invocation invocation)
	{
		// We only copy instances of MethodInvocation.
		if (!(invocation instanceof MethodInvocation))
			return invocation;

		MethodInvocation isrc = (MethodInvocation) invocation;
		try {
			// Read the method arguments.
			Field argumentsField = methodInvocation_argumentsField;
			if (argumentsField == null) {
				argumentsField = MethodInvocation.class.getDeclaredField("arguments");
				argumentsField.setAccessible(true);
				methodInvocation_argumentsField = argumentsField;
			}
			Object argumentsObject = argumentsField.get(isrc);

			// Clone the method arguments. If the arguments are not cloneable, we return the original invocation
			// instead of throwing an exception.
			Object clonedArgumentsObject;
			try {
				clonedArgumentsObject = cloneSerializable(argumentsObject, argumentsObject == null ? null : argumentsObject.getClass().getClassLoader());
			} catch (Exception x) {
				// Do we have a NotSerializableException? In this case, we do not throw the exception, but return the original.
				if (findCauseOfType(x, NotSerializableException.class) != null) {
					logger.info("copyInvocation: arguments not serializable! Will return original and skip copying! " + x);
					return invocation;
				}

				throw x;
			}

			// We were able to clone the arguments, thus copy the invocation, now. Unfortunately, this API method
			// does not clone the arguments, itself.
			Invocation copy = invocation.copy();
			if (!(copy instanceof MethodInvocation)) {
				logger.warn("copyInvocation: invocation.copy() returned an instance of " + (copy == null ? null : copy.getClass().getName()) + " instead of a MethodInvocation! Will return original and skip copying!");
				return invocation;
			}

			MethodInvocation idest = (MethodInvocation) copy;
			argumentsField.set(idest, clonedArgumentsObject);
			return copy;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Throwable findCauseOfType(final Throwable x, final Class<? extends Throwable> searchedType)
	{
		Throwable t = x;
		do {
			if (searchedType.isInstance(t))
				return t;

			t = t.getCause();
		} while (t != null);

		return null;
	}

	/**
	 * This method clones a given object by serializing it (into a <code>DataBuffer</code>)
	 * and deserializing it again. It uses java native serialization, thus the object needs
	 * to implement {@link Serializable}.
	 * <p>
	 * Copied from org.nightlabs.util.Util, because we have no access to that class from here.
	 * </p>
	 *
	 * @param <T> Any class. It is not defined as "&lt;T extends Serializable&gt;" in order to allow
	 *		interfaces (e.g. <code>java.util.Map</code>) that don't extend {@link Serializable} but whose
	 *		implementations usually do.
	 * @param original The original object. It will be serialized and therefore needs to implement <code>Serializable</code>.
	 * @param classLoader The class loader to use to resolve loaded classes or <code>null</code> to use the
	 * 		default lookup mechanism.
	 * @return The copy (deserialized clone) of the given <code>original</code>.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T cloneSerializable(final T original, final ClassLoader classLoader)
	{
		if (original == null) {
			return null;
		}

		byte[] buffer;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			try {
				out.writeObject(original);
			} finally {
				out.close();
			}
			buffer = bout.toByteArray();
		} catch (IOException x) { // there should never be a problem (under normal circumstances) as the ByteArrayOutputStream works in RAM only.
			throw new RuntimeException(x);
		}

		try {
			ObjectInputStream in =
					classLoader == null
					? new ObjectInputStream(new ByteArrayInputStream(buffer))
					: new ClassLoaderObjectInputStream(new ByteArrayInputStream(buffer), classLoader);
			try {
				return (T) in.readObject();
			} finally {
				in.close();
			}
		} catch (ClassNotFoundException x) { // we deserialize an object of the same type as our parameter => the class should always be known
			throw new RuntimeException(x);
		} catch (IOException x) { // there should never be a problem (under normal circumstances) as the databuffer should nearly always work in RAM only.
			throw new RuntimeException(x);
		}
	}
	/**
	 * An {@link ObjectInputStream} instance that uses the given
	 * {@link ClassLoader} to resolve classes that are to be deserialized.
	 * <p>
	 * Copied from org.nightlabs.util.Util, because we have no access to that class from here.
	 * </p>
	 * @author Marc Klinger - marc[at]nightlabs[dot]de
	 */
	private static class ClassLoaderObjectInputStream extends ObjectInputStream
	{
		private final ClassLoader classLoader;
		public ClassLoaderObjectInputStream(final InputStream in, final ClassLoader classLoader) throws IOException {
			super(in);
			this.classLoader = classLoader;
		}
		/* (non-Javadoc)
		 * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
		 */
		@Override
		protected Class<?> resolveClass(final ObjectStreamClass desc)
				throws IOException, ClassNotFoundException
		{
			if(classLoader == null) {
				return super.resolveClass(desc);
			}
			String name = desc.getName();
			try {
			    return Class.forName(name, false, classLoader);
			} catch (ClassNotFoundException ex) {
				return super.resolveClass(desc);
			}
		}
	}

	/**
	 * @return Whether this interceptor should retry transactions. This defaults
	 *         to <code>true</code>, but will read
	 *         {@link #SYSTEM_PROPERY_NAME_RETRY_TRANSACTIONS} if set.
	 */
	public static boolean isRetryTransactions() {
		String doRetry = System.getProperty(SYSTEM_PROPERY_NAME_RETRY_TRANSACTIONS);
		return doRetry == null || doRetry.isEmpty() || Boolean.parseBoolean(doRetry);
	}

	/**
	 * @return The number of retries. Defaults to {@link #defaultRetryCount} but
	 *         will read {@link #SYSTEM_PROPERY_NAME_RETRY_COUNT} if set.
	 */
	public static int getRetryTimes() {
		try {
			return Integer.parseInt(System.getProperty(SYSTEM_PROPERY_NAME_RETRY_COUNT));
		} catch (Exception e) {
			return defaultRetryCount;
		}
	}

	/**
	 * @return The number ms to sleep between retries. Defaults to {@link #defaultSleepTime} but
	 *         will read {@link #SYSTEM_PROPERY_NAME_RETRY_SLEEP} if set.
	 */
	public static long getSleepTime() {
		try {
			return Long.parseLong(System.getProperty(SYSTEM_PROPERY_NAME_RETRY_SLEEP));
		} catch (Exception e) {
			return defaultSleepTime;
		}
	}

//	private Object invokeRetry(Invocation invocation, int times) throws Throwable {
//		Object result = null;
//		boolean retry = true;
//		int retryCount = 0;
//		while( retry == true )
//		{
//			try
//			{
//				result = invocation.invokeNext();
//				break;
//			}
//			catch (Exception e)
//			{
//				logger.debug("JFireEjbTransactionInterceptor Exception is caught reInvoke again !!");
//				if (retryCount >= times)
//					throw e;
//				retryCount++;
//			}
//		}
//		return result;
//	}

}
