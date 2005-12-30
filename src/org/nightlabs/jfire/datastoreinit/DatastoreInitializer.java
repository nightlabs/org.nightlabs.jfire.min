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

package org.nightlabs.jfire.datastoreinit;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.datastoreinit.xml.DatastoreInitMan;
import org.nightlabs.jfire.datastoreinit.xml.Dependency;
import org.nightlabs.jfire.datastoreinit.xml.Init;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.ServerCf;

import org.nightlabs.ModuleException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DatastoreInitializer
{
	public static final Logger LOGGER = Logger.getLogger(DatastoreInitializer.class);
	
	private FileFilter earFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".ear");
		}
	};

	private FileFilter jarFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".jar");
		}
	};

	private List inits = new ArrayList();

	public DatastoreInitializer(File jfireModuleBaseDir)
	{
		// Scan all JARs within all EARs for datastoreinit.xml files.
		File[] ears = jfireModuleBaseDir.listFiles(earFileFilter);
		for (int i = 0; i < ears.length; ++i) {
			File ear = ears[i];

			File[] jars = ear.listFiles(jarFileFilter);
			for (int m = 0; m < jars.length; ++m) {
				File jar = jars[m];
				try {
					JarFile jf = new JarFile(jar);
					try {
						JarEntry je = jf.getJarEntry("META-INF/datastoreinit.xml");
						if (je != null) {
							InputStream in = jf.getInputStream(je);
							try {
								DatastoreInitMan dim = new DatastoreInitMan(ear.getName(), jar.getName(), in);
								for (Iterator it = dim.getInits().iterator(); it.hasNext(); ) {
									Init init = (Init) it.next();
									inits.add(init);
								}
							} finally {
								in.close();
							}
						} // if (je != null) {
					} finally {
						jf.close();
					}
				} catch (Exception e) {
					LOGGER.error("Reading from JAR '"+jar.getAbsolutePath()+"' failed!", e);
				}
			}
		}
		// Now all meta data files have been read.

		// Sort the inits by priority
		Collections.sort(inits, new Comparator() {
			public int compare(Object obj0,Object obj1) {
				Init init0 = (Init)obj0;
				Init init1 = (Init)obj1;
				if (init0.getPriority() == init1.getPriority())
					return 0;

				if (init0.getPriority() < init1.getPriority())
					return -1;

				return 1;
			}
		});

		inits = sortByDependencies();
	}

	/**
	 * used to detect circular references in addInit(..)
	 */
	private HashSet initsInAddProcess = new HashSet();
	private LinkedList initsInAddProcessStack = new LinkedList();

	private void addInit(List res, Set resKeySet, Init init)
	{
		String initKey = init.getDatastoreInitMan().getJFireEAR() + '/' +
				init.getDatastoreInitMan().getJFireJAR() + '/' +
				init.getBean() + '/' + init.getMethod();

		if (initsInAddProcess.contains(initKey)) {
			LOGGER.error("Circular reference in init dependencies: "+initKey);
			// TODO include initsInAddProcessStack in log!

			return;
		}

		initsInAddProcess.add(initKey);
		initsInAddProcessStack.addLast(initKey);
		try {

			for (Iterator itDep = init.getDependencies().iterator(); itDep.hasNext(); ) {
				Dependency dep = (Dependency) itDep.next();

				String depKey = dep.getModule() + '/' +
						dep.getArchive() + '/' + dep.getBean() + '/' + dep.getMethod();

				if (!resKeySet.contains(depKey)) {

					boolean found = false;
					for (Iterator it2 = inits.iterator(); it2.hasNext(); ) {
						Init init2 = (Init) it2.next();
						if (dep.getModule().equals(init2.getDatastoreInitMan().getJFireEAR())
								&&
								dep.getArchive().equals(init2.getDatastoreInitMan().getJFireJAR())
								&&
								dep.getBean().equals(init2.getBean())
								&&
								dep.getMethod().equals(init2.getMethod())) {
							addInit(res, resKeySet, init2);
							found = true;
							break;
						}
					}

					if (!found)
						LOGGER.error("Init "+initKey+" has dependency "+depKey+" which cannot be found!");

				} // if (!resKeySet.contains(depKey)) {
			} // for (Iterator it = init.getDependencies().iterator(); it.hasNext(); ) {

			resKeySet.add(initKey);
			res.add(init);

		} finally {
			initsInAddProcess.remove(init);
			initsInAddProcessStack.removeLast();
		}
	}

	private List sortByDependencies()
	{
		initsInAddProcess.clear();
		initsInAddProcessStack.clear();

		List res = new ArrayList();
		Set resKeySet = new HashSet();

		// Stores the String {ear}/{jar}/{bean}/{method}
		HashSet initsBefore = new HashSet();

		// Resolve dependencies and resort the inits to fulfill the dependencies
		for (Iterator itInits = inits.iterator(); itInits.hasNext(); ) {
			Init init = (Init) itInits.next();
			addInit(res, resKeySet, init);
		}

		return res;
	}

	public void initializeDatastore(
			JFireServerManagerFactory ismf,
			ServerCf localServer,
			String organisationID, String systemUserPassword)
		throws ModuleException
	{
		try {
			String username = User.SYSTEM_USERID + '@' + organisationID;
	
			Hashtable props = new Properties();
			String initialContextFactory = ismf.getInitialContextFactory(localServer.getJ2eeServerType(), true);
			props.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
			props.put(InitialContext.PROVIDER_URL, localServer.getInitialContextURL());
			props.put(InitialContext.SECURITY_PRINCIPAL, username);
			props.put(InitialContext.SECURITY_CREDENTIALS, systemUserPassword);
			props.put(InitialContext.SECURITY_PROTOCOL, "jfire");

			InitialContext initCtx = new InitialContext(props);
			try {
				for (Iterator it = inits.iterator(); it.hasNext(); ) {
					Init init = (Init)it.next();
					try {
						Object homeRef = initCtx.lookup(init.getBean());
						Method homeCreate = homeRef.getClass().getMethod("create", null);
						Object bean = homeCreate.invoke(homeRef, null);
						Method beanMethod = bean.getClass().getMethod(init.getMethod(), null);
						beanMethod.invoke(bean, null);

						try {
							if (bean instanceof EJBObject)
								((EJBObject)bean).remove();
	
							if (bean instanceof EJBLocalObject)
								((EJBLocalObject)bean).remove();
						} catch (Exception x) {
							LOGGER.warn(
									"Init could not remove bean! EAR=\""+init.getDatastoreInitMan().getJFireEAR()+"\"" +
											" JAR=\""+init.getDatastoreInitMan().getJFireJAR()+"\"" +
											" Bean=\""+init.getBean()+"\"", x);
						}

					} catch (Exception x) {
						LOGGER.error(
								"Init failed! EAR=\""+init.getDatastoreInitMan().getJFireEAR()+"\"" +
										" JAR=\""+init.getDatastoreInitMan().getJFireJAR()+"\"" +
										" Bean=\""+init.getBean()+"\" Method=\""+init.getMethod()+"\"", x);
					}
				}

			} finally {
		   	initCtx.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

}
