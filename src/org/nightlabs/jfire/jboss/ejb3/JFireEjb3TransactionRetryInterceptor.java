package org.nightlabs.jfire.jboss.ejb3;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;

/**
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class JFireEjb3TransactionRetryInterceptor  implements Interceptor
{
	private static final Logger logger = Logger.getLogger(JFireEjb3TransactionRetryInterceptor.class);
	public static final int TRANSACTION_RETRY_TIMES = 3;

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
		Throwable firstException = null;
		while (true) {
			try {
				result = invocation.invokeNext();
				break;
			} catch (final Exception e) {
				if (firstException == null)
					firstException = e;

				if (retryCount >= TRANSACTION_RETRY_TIMES) {
					logger.error("Caught exception (not retrying again): " + e, e);
//					throw e;
					// We throw the first exception, because currently the following exceptions are all the same due
					// to the tx already being aborted and not restarted when retrying.
					// See: https://www.jfire.org/modules/bugs/view.php?id=1810
					throw firstException;
				}
				else
					logger.warn("Caught exception (will retry again): " + e, e);

				retryCount++;
			}
		}
		return result;
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
