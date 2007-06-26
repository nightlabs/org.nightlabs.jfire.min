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

package org.nightlabs.jfire.testsuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.util.reflect.ReflectUtil;


/**
 * @ejb.bean name="jfire/ejb/JFireTestSuite/JFireTestManager"	
 *					 jndi-name="jfire/ejb/JFireTestSuite/JFireTestManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class JFireTestManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireTestManagerBean.class);

	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"	
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It runs all {@link TestSuite}s.
	 * 
	 * @throws Exception When something went wrong. 
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Supports"
	 */
	public void initialise()
	throws Exception 
	{
		runTestSuites();
	}

	/**
	 * Runs all TestSuites and TestCases found in the classpath under org.nightlabs.jfire.testsuite. 
	 */
	public static void runTestSuites() 
	throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ModuleException, IOException 
	{
		logger.debug("Scanning classpath for TestSuites and TestCases");
		Collection<Class> classes = ReflectUtil.listClassesInPackage("org.nightlabs.jfire.testsuite", true);
		logger.debug("Found " + classes.size() + " classes");
		List<Class> testSuites = new LinkedList<Class>();
		Map<Class, List<Class>> suites2TestCases = new HashMap<Class, List<Class>>();
		for (Class<?> clazz : classes) {
			if (TestSuite.class.isAssignableFrom(clazz)) {
				logger.debug("Found testSuite " + clazz.getName());
				testSuites.add(clazz);
			} else if (TestCase.class.isAssignableFrom(clazz)) {
				JFireTestSuite testSuiteAnnotation = (JFireTestSuite) clazz.getAnnotation(JFireTestSuite.class);
				Class suiteClass = DefaultTestSuite.class; // Default value, if not annotated.
				if (testSuiteAnnotation != null) {					
					suiteClass = testSuiteAnnotation.value();
				}
				List<Class> suiteClasses = suites2TestCases.get(suiteClass);
				if (suiteClasses == null) {
					suiteClasses = new LinkedList<Class>();
					suites2TestCases.put(suiteClass, suiteClasses);
				}
				suiteClasses.add(clazz);
			}
		}

		// now iterate all registrations and find TestCases that have a Suite set that's not in the classpath
		for (Class suiteClass : suites2TestCases.keySet()) {
			if (!testSuites.contains(suiteClass)) {
				testSuites.add(suiteClass);
			}
		}
		
		List<TestSuite> runSuites = new LinkedList<TestSuite>();
		for (Class<? extends TestSuite> clazz : testSuites) {
			if (suites2TestCases.containsKey(clazz)) {
				List<Class> suiteClasses = suites2TestCases.get(clazz);
//				Constructor<Class<? extends TestCase>[]> con =
				Constructor c = null;
				try {
					c = clazz.getConstructor(new Class[] {Class[].class});
				} catch (Exception e) {
					logger.error("Could not find (Class<? extends TestCase> ... classes) constructor for TestSuite " + clazz.getName(), e);
					continue;
				}
				TestSuite testSuite = null;
				try {
					testSuite = (TestSuite) c.newInstance(new Object[] {suiteClasses.toArray(new Class[suiteClasses.size()])});
				} catch (Exception e) {
					logger.error("Could not instantiate TestSuite " + clazz.getName(), e);
					continue;
				}
				runSuites.add(testSuite);
			}
		}
		
		// find the listener and configure them
		Properties mainProps = new Properties();
		FileInputStream in = new FileInputStream(new File(JFireTestSuiteEAR.getEARDir(), "jfireTestSuite.properties"));
		try {
			mainProps.load(in);
		} finally {
			in.close();
		}
		Collection<Matcher> listenerMatches = getPropertyKeyMatches(mainProps, Pattern.compile("(listener\\.(?:[^.]*?)\\.)class"));
		List<JFireTestListener> listeners = new LinkedList<JFireTestListener>();
		for (Matcher matcher : listenerMatches) {
			Properties listenerProps = getProperties(mainProps, matcher.group(1));
			Class clazz = null;
			try {
				clazz = Class.forName(listenerProps.getProperty("class"));
			} catch (Exception e) {
				logger.error("Could not relove listener class " + listenerProps.getProperty("class"), e);
				continue;
			}
			JFireTestListener listener = null;
			try {
				listener = (JFireTestListener) clazz.newInstance();
			} catch (Exception e) {
				logger.error("Could not instantiate test listener " + clazz.getName(), e);
				continue;
			}
			listener.configure(listenerProps);
			listeners.add(listener);
		}

		// Run the suites
		for (JFireTestListener listener : listeners) {
			try {
				listener.startTestRun();
			} catch (Exception e) {
				logger.error("Error notifing JFireTestListener!", e);
				continue;
			}
		}
		for (TestSuite suite : runSuites) {
			JFireTestRunner runner = new JFireTestRunner();
			for (JFireTestListener listener : listeners) {
				runner.addListener(listener);
			}
			runner.run(suite);
		}
		for (JFireTestListener listener : listeners) {
			try {
				listener.endTestRun();
			} catch (Exception e) {
				logger.error("Error notifing JFireTestListener!", e);
				continue;
			}
		}
	}
	
	
	public static Collection<Matcher> getPropertyKeyMatches(Properties properties, Pattern pattern)
	{
		Collection<Matcher> matches = new ArrayList<Matcher>();
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			Matcher m = pattern.matcher(key);
			if(m.matches())
				matches.add(m);
		}
		return matches;
	}
	
	public static Properties getProperties(Properties properties, String keyPrefix)
	{
		Properties newProperties = new Properties();
		Collection<Matcher> matches = getPropertyKeyMatches(properties, Pattern.compile("^"+Pattern.quote(keyPrefix)+"(.*)$"));
		for (Matcher m : matches)
			newProperties.put(m.group(1), properties.get(m.group(0)));
		return newProperties;
	}
	
}
