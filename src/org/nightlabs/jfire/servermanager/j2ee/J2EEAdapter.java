/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.servermanager.j2ee;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.jms.Connection;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.transaction.UserTransaction;

import org.nightlabs.jfire.security.ISecurityReflector;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;


/**
 * This interface defines an adapter for vendor specific functionality.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface J2EEAdapter extends Serializable {
	/**
	 * The appropriate {@link J2EEAdapter} will be bound to JNDI under this name.
	 * This is done by {@link JFireServerManagerFactoryImpl}.
	 */
	public static final String JNDI_NAME = "java:/jfire/system/J2EEAdapter";

	public void flushAuthenticationCache()
		throws J2EEAdapterException;

	/**
	 * This method is called by the <tt>JFireServerManagerFactoryImpl</tt> when
	 * it has been created. It must somehow make sure that the method
	 * <tt>ServerStartNotificationListener.serverStarted()</tt> is called when the
	 * J2EE server has deployed all modules that existed while booting it.
	 *
	 * @param listener the listener to be notified as soon as the deployment has completed and a server-started-event is propagated.
	 * @throws J2EEAdapterException in case registering the listener fails.
	 */
	public void registerNotificationListenerServerStarted(ServerStartNotificationListener listener)
		throws J2EEAdapterException;

	public UserTransaction getUserTransaction(InitialContext initialContext)
		throws J2EEAdapterException;

	public ISecurityReflector getSecurityReflector();

	/**
	 * This method shuts the server down. It should do so nicely.
	 */
	public void shutdown();

	/**
	 * Just like {@link #shutdown()} this method initiates a shutdown, but signals that the server should be restarted by the service wrapper.
	 */
	public void reboot();

	LoginContext createLoginContext(String securityProtocol, CallbackHandler callbackHandler) throws LoginException;

	/**
	 * Get all roles that have been declared via {@link RolesAllowed} in all EJBs.
	 *
	 * @return a {@link Set} of roles; never <code>null</code>.
	 * @throws J2EEAdapterException in case obtaining the roles fails.
	 */
	Set<String> getAllEjb3Roles() throws J2EEAdapterException;

	/**
	 * Create a connection to the JMS which allows for sending (and receiving) messages via queues or topics.
	 *
	 * @param transacted whether to use a transaction. If <code>true</code>, a distributed XA transaction will be used
	 * and <code>acknowledgeMode</code> will be ignored (according to JEE spec, see {@link Connection#createSession(boolean, int)}.
	 * If <code>false</code>, no transaction will be used. Note, that without a transaction, messages sent to a queue
	 * (or topic) will be <b>immediately</b> processed (and thus not yet see data written in the current transaction).
	 * @param acknowledgeMode indicates whether the consumer or the client will acknowledge any messages it receives;
	 * ignored if the session is transacted. See {@link Connection#createSession(boolean, int)}.
	 * @return a connection to the JMS.
	 * @throws J2EEAdapterException in case there is a problem accessing the JMS.
	 */
	JMSConnection createJMSConnection(boolean transacted, int acknowledgeMode) throws J2EEAdapterException;

	/**
	 * Determine whether we are running in a clustered environment. Certain features use this to automatically
	 * toggle themselves on when needed in a cluster.
	 * @return <code>true</code>, if the server is running in cluster-mode. <code>false</code>, if the server runs
	 * in single-JVM-mode.
	 */
	public boolean isInCluster();

	/**
	 * <p>
	 * Get access to the cluster-wide JNDI. If the JavaEE server implementation does not differentiate
	 * between local-node JNDI and cluster-wide JNDI or if {@link #isInCluster()} returns
	 * <code>false</code>, this is the same as <code>new InitialContext()</code>.
	 * </p>
	 * <p>
	 * Important: You should call {@link InitialContext#close()} on the resulting instance when you don't need
	 * it anymore. This is not essentially necessary, because garbage collection usually is sufficient, but it
	 * is urgently recommended to release resources as early as possible.
	 * </p>
	 *
	 * @return an {@link InitialContext} connected to the cluster-wide JNDI.
	 * @throws NamingException
	 */
	public InitialContext createClusterInitialContext() throws NamingException;
}
