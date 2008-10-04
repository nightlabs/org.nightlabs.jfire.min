package org.nightlabs.jfire.jboss.transaction;

import javax.transaction.Transaction;

import org.apache.log4j.Logger;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;

/**
 * This interceptor forces rollback whenever an EJB method throws an exception. This is
 * necessary, because all JFire code relies on this behaviour. It is thus
 * guaranteed that an EJB method which fails with an exception never ends up with
 * an inconsistent datastore.
 * <p>
 * This method catches only {@link Exception} (not {@link Throwable}), because the
 * JavaEE contract seems to dictate rollback in case of any non-<code>Exception</code>
 * <code>Throwable</code> anyway.
 * </p>
 * <p>
 * <b>Important:</b> We will probably remove this interceptor and instead declare the rollback-behaviour
 * in our <code>ejb-jar.xml</code> files when we switched to EJB 3. Check the
 * <a href="https://www.jfire.org/modules/newbb/forum_4.html">Announcements</a> forum if you want to learn
 * about this change.
 * </p>
 * <p>
 * See <a href="https://www.jfire.org/modules/bugs/view.php?id=863">JFire issue 863</a>.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class ForceRollbackOnExceptionInterceptor
extends AbstractInterceptor
{
	private static final Logger logger = Logger.getLogger(ForceRollbackOnExceptionInterceptor.class);

	@Override
	public Object invoke(Invocation mi) throws Exception
	{
		try {
			return getNext().invoke(mi);
		} catch (Exception x) {
			Transaction tx = mi.getTransaction();
			if (tx != null) {
				logger.info("invoke: Caught exception and forcing rollback.");
				tx.setRollbackOnly();
			}
			else
				logger.info("invoke: Caught exception, but won't force rollback, because there is no transaction.");

			// we sleep a little bit before escalating, because the problem might need a bit time to solve (e.g. a subsystem to restart)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException y) {
				// ignore
			}

			throw x;
		}
	}
}
