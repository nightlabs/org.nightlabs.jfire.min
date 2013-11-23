package org.nightlabs.jfire.jboss.transaction;

import javax.jdo.JDOOptimisticVerificationException;

import org.jboss.ejb.plugins.TxRetryExceptionHandler;

/**
 * This retry-handler always returns <code>true</code> (i.e. retries no matter what exception happened).
 * We might later change this to only retry in case of transaction collisions (i.e. database dead-locks or
 * exceptions like {@link JDOOptimisticVerificationException} and {@link org.hibernate.StaleObjectStateException}).
 * <p>
 * See <a href="https://www.jfire.org/modules/bugs/view.php?id=110">JFire issue 110</a>.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class RetryHandler
implements TxRetryExceptionHandler
{
	@Override
	public boolean retry(Exception exception) {
		return true;
	}
}
