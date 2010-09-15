package org.nightlabs.jfire.jboss.ejb3;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;

/**
*
*  
* @author Fitas Amine - fitas [at] nightlabs [dot] de
*/
public class JFireEjb3TransactionRetryInterceptor  implements Interceptor
{
	private static final Logger logger = Logger.getLogger(JFireEjb3TransactionRetryInterceptor.class);
	public static final int TRANSACTION_RETRY_TIMES = 3;	

	public JFireEjb3TransactionRetryInterceptor()
	{	
		logger.debug("JFireEjbTransactionInterceptor has been initialized !!!");
	}


	public String getName()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public Object invoke(Invocation invocation) throws Throwable {
		return invokeRetry(invocation, TRANSACTION_RETRY_TIMES);
	}

	private Object invokeRetry(Invocation invocation, int times) throws Throwable {
		Object result = null;
		boolean retry = true;
		int retryCount = 0;
		while( retry == true )
		{
			try
			{
				result = invocation.invokeNext();
				break;
			}
			catch (Exception e)
			{
				if (retryCount >= times)
					throw e;
				retryCount++;	
			}    	  
		}
		return result;
	}

}
