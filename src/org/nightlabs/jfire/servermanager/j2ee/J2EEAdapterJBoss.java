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

import java.lang.reflect.Method;
import java.rmi.Remote;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class J2EEAdapterJBoss implements J2EEAdapter
{
	public void flushAuthenticationCache()
	throws Exception
	{
		ObjectName jaasMgr =
			new ObjectName("jboss.security:service=JaasSecurityManager");
		Object[] params = { "jfire" }; // String securityDomain
		String[] signature = { "java.lang.String" };
		invoke(jaasMgr, "flushAuthenticationCache", params, signature);
	}
	
	protected Object invoke(
		ObjectName name,
		String method,
		Object[] args,
		String[] sig)
		throws Exception
	{
		return invoke(getServer(), name, method, args, sig);
	}
	
	protected Object invoke(
		Remote server,
		ObjectName name,
		String method,
		Object[] args,
		String[] sig)
		throws Exception
	{
		// This should work, too, but I couldn't test it yet.
		//((org.jboss.jmx.adaptor.rmi.RMIAdaptor) server).
		//invoke(name, method, args, sig);

		Class [] argTypes = new Class [] 
			{ObjectName.class, String.class, Object[].class, String[].class};	
		Method m = server.getClass().getMethod("invoke", argTypes);
		return m.invoke(server,new Object[]{name, method, args, sig});
	}
	
	protected Remote getServer() throws Exception
	{
		if (initialContext == null)
			initialContext = new InitialContext();
	
		if (server == null) {
	//		String serverName = System.getProperty("testAdvantage.jboss.server.name");
	//
	//		if (serverName == null) {
	//			serverName = InetAddress.getLocalHost().getHostName();
	//		}
			server = (Remote) initialContext.lookup("jmx/invoker/RMIAdaptor");
		}
		return server;
	}
	
	protected Remote server;
	protected InitialContext initialContext = null;

	/**
	 * @see org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter#registerNotificationListenerServerStarted(org.nightlabs.jfire.servermanager.j2ee.ServerStartNotificationListener)
	 */
	public void registerNotificationListenerServerStarted(ServerStartNotificationListener listener)
		throws Exception
	{
		MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);

		server.addNotificationListener(
				new ObjectName("jboss.system:type=Server"),
				new NotificationListener(){
					public void handleNotification(Notification notification, Object handback)
					{
						if (org.jboss.system.server.Server.START_NOTIFICATION_TYPE.equals(notification.getType())) {
							((ServerStartNotificationListener)handback).serverStarted();
						}
					}
				}, null,
				listener
		);
	}

	public TransactionManager getTransactionManager(InitialContext initialContext)
		throws Exception
	{
		return (TransactionManager)initialContext.lookup("java:/TransactionManager");
	}

	private SecurityReflector userResolver = null;
	/**
	 * @see org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter#getUserResolver()
	 */
	public SecurityReflector getUserResolver()
	{
		if (userResolver == null)
			userResolver = new SecurityReflectorJBoss();

		return userResolver;
	}

	public void shutdown()
	{
		System.exit(-1);
	}
}
