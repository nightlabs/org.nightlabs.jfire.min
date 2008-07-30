package org.nightlabs.jfire.jboss.transaction;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.jboss.invocation.Invocation;
import org.jboss.proxy.ejb.GenericEJBInterceptor;

/**
 * This interceptor retries to invoke an EJB method, if it fails.
 * See <a href="https://www.jfire.org/modules/bugs/view.php?id=110">JFire issue 110</a>.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class RetryTransactionInterceptor
extends GenericEJBInterceptor
{
	private static final Logger logger = Logger.getLogger(RetryTransactionInterceptor.class);

	private static String getTransactionStatusAsString(int status)
	{
		for (Field field : Status.class.getDeclaredFields()) {
			if ((field.getModifiers() & Modifier.STATIC) == 0)
				continue;
			if ((field.getModifiers() & Modifier.PUBLIC) == 0)
				continue;

			Object fieldValue;
			try {
				fieldValue = field.get(null);
			} catch (Exception e) {
				logger.error("getTransactionStatusAsString: Unable to obtain field value! field=" + field.toGenericString(), e);
				return String.valueOf(status);
			}
			if (fieldValue instanceof Integer) {
				if (((Integer)fieldValue).intValue() == status)
					return field.getName();
			}
		}
		logger.error("getTransactionStatusAsString: No constant found for status=" + status);
		return String.valueOf(status);
	}

	@Override
	public Object invoke(Invocation invocation) throws Throwable
	{
		int invocationCounter = 0;
		while (true) {
			try {
				++invocationCounter;
				return getNext().invoke(invocation);
			} catch (Throwable x) {
				logger.warn("invoke: EJB method invocation failed! invocationCounter=" + invocationCounter +" method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName(), x);
				if (invocationCounter >= 3) // we try each invocation 3 times
					throw x;

				// In the long run, we might check what kind of exception we encountered and only retry in certain conditions
				// (e.g. a dead lock or a JDOOptimisticVerificationException), but for now, we'll retry always if we are not
				// right now in a transaction (in this case, retrying doesn't make much sense, because the transaction should
				// be rolled back BEFORE retrying).

				// Are we in a transaction right now?
				InitialContext initialContext = new InitialContext();
				try {
					UserTransaction tx = (UserTransaction)initialContext.lookup("UserTransaction");
					int status = tx.getStatus();
					if (Status.STATUS_NO_TRANSACTION != status) {
						logger.warn("invoke: Transaction status is \"" + getTransactionStatusAsString(status) + "\" (" + status + ") instead of \"STATUS_NO_TRANSACTION\" => Will not retry!");
						throw x;
					}
				} finally {
					initialContext.close();
				}
			}
		}
	}
}
