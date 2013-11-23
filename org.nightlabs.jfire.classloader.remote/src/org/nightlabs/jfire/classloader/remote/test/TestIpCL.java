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

package org.nightlabs.jfire.classloader.remote.test;

//import java.io.File;
//import java.net.URL;
//import java.util.Properties;
//
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import javax.security.auth.login.LoginException;
//
//import org.nightlabs.classloader.DelegatingClassLoader;
//import org.nightlabs.config.Config;
//import org.nightlabs.j2ee.InitialContextProvider;
//import org.nightlabs.jfire.classloader.remote.JFireRCDLDelegate;
//
//
//
///**
// * FIXME Remove the use of deprecated members.
// * 
// * @author marco
// */
//@SuppressWarnings("deprecation")
//public class TestIpCL
//{
//	public static void main(String[] args)
//	{
//		System.out.println(
//				"sun.boot.class.path: " + System.getProperty("sun.boot.class.path")
//			);
//
//		System.out.println(
//				"java.system.class.loader: " + System.getProperty("java.system.class.loader")
//			);
//		
//		URL url = Config.class.getProtectionDomain().getCodeSource().getLocation();
//		System.out.println(url);
//
//
////		Config config;
//		try {
////			Config.createSharedInstance("Config.xml", true, "/tmp");
//			JFireRCDLDelegate.createSharedInstance(new InitialContextProvider() {
//						public InitialContext getInitialContext() throws LoginException,
//								NamingException
//						{
//							return new InitialContext(getInitialContextProperties());
//						}
//
//						public Properties getInitialContextProperties()
//								throws LoginException
//						{
//							Properties props = new Properties();
//
//							props.put(Context.INITIAL_CONTEXT_FACTORY,	"org.jboss.security.jndi.LoginInitialContextFactory");
//							props.put(Context.PROVIDER_URL, "jnp://127.0.0.1:1099");
//							props.put(Context.SECURITY_PRINCIPAL, "marco@schulze.nightlabs.de");
//							props.put(Context.SECURITY_CREDENTIALS, "zeus");
//
//							return props;
//						}
//					},
//					new File("/tmp/jfireclassloadercache")).register(getSystemClassLoader());
//				
//
////			Class.forName("org.nightlabs.jfire.classloader.TestClassLoader");
////			config = Config.createSharedInstance("config.xml", true, "/tmp", true);
////
////			System.setProperty("java.system.class.loader", "org.nightlabs.jfire.classloader.JFireRemoteClassLoader");
////
////			JFireRemoteClassLoader objClassLoader = new JFireRemoteClassLoader((JFireRemoteClassLoaderSettings)config.createConfigModule(JFireRemoteClassLoaderSettings.class));
////
////			Field objSCL = ClassLoader.class.getDeclaredField("scl");
////			objSCL.setAccessible(true);
////			objSCL.set(null, objClassLoader);
////
////			Thread.currentThread().setContextClassLoader(objClassLoader);
//
//			Class<?> clazz = Class.forName("org.nightlabs.yak.location.Area");
//			clazz.newInstance();
////			Class.forName("org.nightlabs.jfire.classloader.test.TestClass");
//			System.out.println("**********************");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
////		// Add the system class paths...
////		String[] strProperties = new String[] { "java.class.path", "java.library.path", "java.ext.dirs" };
////		for(int intCounter = 0; intCounter < strProperties.length; ++intCounter)
////		{
////			String strProperty = System.getProperty(strProperties[intCounter]);
////			if(strProperty != null)
////			{
////				if(strProperty.indexOf(System.getProperty("path.separator")) != -1)
////				{
////					String[] strPaths = strProperty.split(System.getProperty("path.separator"));
////					for(int intCounterP = 0; intCounterP < strPaths.length; ++intCounterP)
////					{
////						objClassLoader.addClassPath(strPaths[intCounterP]);
////					}
////				} else {
////					objClassLoader.addClassPath(strProperty);
////				}
////			}
////		}
//
////		objClassLoader.addClassPath(System.getProperty("java.home") + File.separatorChar + "lib", HybridClassLoader.DIRECTORIES | HybridClassLoader.ARCHIVES);
////		objClassLoader.loadClass("hybrid.core.BotAdapter");
//
////		objHybrid = objClassLoader.loadClass("hybrid.core.Hybrid");
////		if(objHybrid != null)
////		{
////			objConstructor = objHybrid.getConstructor(new Class[] {String.class, Boolean.class});
////			objConstructor.newInstance(new Object[] {strConfigFile, new Boolean(boolDebugMode)});
////		}
//	}
//	
//	protected static DelegatingClassLoader getSystemClassLoader()
//	{
//		ClassLoader sysCL = ClassLoader.getSystemClassLoader();
//		if (!(sysCL instanceof DelegatingClassLoader))
//			throw new ClassCastException("The system class loader is an instance of "+sysCL.getClass().getName()+" and NOT org.nightlabs.classloader.DelegatingClassLoader!!! Read the documentation of it and make it the system class loader!");
//		return (DelegatingClassLoader)sysCL;
//	}
//	
//}
