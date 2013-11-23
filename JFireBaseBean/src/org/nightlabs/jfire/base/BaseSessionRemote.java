package org.nightlabs.jfire.base;

import java.util.Hashtable;

public interface BaseSessionRemote {
	/**
	 * Return the parameter as result and thus serve as a ping (test whether a bean proxy is still alive)
	 * for remote clients.
	 * <p>
	 * This ping method is used by {@link JFireEjb3Factory#getRemoteBean(Class, Hashtable)} to test a
	 * cached EJB proxy. If the method cannot be successfully executed on the EJB proxy, it is discarded
	 * and a new one created.
	 * </p>
	 *
	 * @param message the message (can be <code>null</code>).
	 * @return the result - same as message.
	 */
	String ping(String message);
}
