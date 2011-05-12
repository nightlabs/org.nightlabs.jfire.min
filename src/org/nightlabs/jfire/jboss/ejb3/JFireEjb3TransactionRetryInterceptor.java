package org.nightlabs.jfire.jboss.ejb3;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;

/**
 * Interceptor that will retry the invocation upon an error for a configurable amount of times 
 * in order to give the server the possibility to retry a transaction in case it failed 
 * because of a technical reason such as a deadlock in the datastore-backend.
 * <p>
 * Whether or not this incerceptor should attempt to retry a failed transaction, 
 * the number of retry-atempts as well as the time to wait between each try can be configured using system-properties.
 * </p> 
 * 
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber - alex [at] nightlabs [dot] de
 */
public class JFireEjb3TransactionRetryInterceptor  implements Interceptor
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

	/** Default Number of ms to sleep before each attempt to retry calling the metod again */
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
		Integer retryTimes = null;
		long retrySleepTime = defaultSleepTime;
		
		while (true) {
			try {
				result = invocation.invokeNext();
				break;
			} catch (final Exception e) {
				if (originalException == null)
					originalException = e;
				// If we should not retry, we have to re-throw the originalException.
				if (!doRetry)
				{
					logger.error("Caught an exception, but will not retry failed transaction.", originalException);
					throw originalException;
				}
				
				if (retryTimes == null) {
					retryTimes = getRetryTimes();
					retrySleepTime = getSleepTime();
				}
				
				logger.debug("before retry invoking sleeping for ms:" + retrySleepTime);
				Thread.sleep(retrySleepTime);
				if (retryCount >= retryTimes) {
					logger.error("Caught exception (not retrying again): " + e, e);
//					throw e;
					// We throw the first exception, because currently the following exceptions are all the same due
					// to the tx already being aborted and not restarted when retrying.
					// See: https://www.jfire.org/modules/bugs/view.php?id=1810
					throw originalException;
				}
				else
					logger.warn("Caught exception (will retry again): " + e, e);
				retryCount++;
				retrySleepTime+=100;
			}
		}
		return result;
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
