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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.jboss.cascadedauthentication;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.jboss.invocation.Invocation;
import org.jboss.proxy.Interceptor;
import org.jboss.proxy.ejb.GenericEJBInterceptor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @deprecated Not used anymore, because {@link CascadedAuthenticationNamingContext} now uses its own {@link Proxy}.
 */
@Deprecated
public class CascadedAuthenticationClientInterceptor extends GenericEJBInterceptor
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(CascadedAuthenticationClientInterceptor.class);

	public static final long serialVersionUID = 1L;

	protected static Class<?> delegateClass = null;
	public static final String DELEGATECLASSNAME = CascadedAuthenticationClientInterceptorDelegate.class.getName();

	public static void reloadProperties()
	{
		try {
			File f = new File("CascadedAuthenticationClientInterceptor.properties");
			if (!f.exists()) {
				delegateClass = null;
				logger.warn("reloadProperties: file does not exist (ignoring): "+f.getAbsolutePath());
			}
			else {
				logger.info("reloadProperties: reading file "+f.getAbsolutePath());
				Properties props = new Properties();
				FileInputStream in = new FileInputStream(f);
				try {
					props.load(in);
				} finally {
					in.close();
				}
				String enable = (String)props.get("enable");
				if(logger.isDebugEnabled())
					logger.debug("reloadProperties: enable="+enable);
				if ("yes".equals(enable) || "true".equals(enable)) {
					logger.info("reloadProperties: enabling cascaded authentication.");
					delegateClass = Class.forName(DELEGATECLASSNAME);
					if (!Interceptor.class.isAssignableFrom(delegateClass))
						throw new ClassCastException("Class \""+DELEGATECLASSNAME+"\" does not implement interface \""+Interceptor.class.getName()+"\"!");
				}
				else {
					logger.info("reloadProperties: disabling cascaded authentication.");
					delegateClass = null;
				}
			} // if (f.exists()) {
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static {
		reloadProperties();
	}

	public CascadedAuthenticationClientInterceptor()
	{
	}

	protected transient Interceptor delegateInstance = null;

	@Override
	public Object invoke(Invocation invocation) throws Throwable
	{
		Class<?> _delegateClass = delegateClass;

		if (delegateInstance != null) {
			if (_delegateClass == null)
				delegateInstance = null;
			else if (_delegateClass != delegateInstance.getClass())
				delegateInstance = null;
		}

		if (delegateInstance == null && _delegateClass != null) {
			Interceptor di = (Interceptor) _delegateClass.newInstance();
			Interceptor next = getNext();
			di.setNext(next);
//			this.setNext(di);
			delegateInstance = di;
		}

		if(logger.isDebugEnabled())
			logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> (delegate enabled: " + (delegateInstance != null ? "yes" : "no") + ")");
		try {

			if (delegateInstance != null)
				return delegateInstance.invoke(invocation);
			else
				return getNext().invoke(invocation);

		} finally {
			if(logger.isDebugEnabled())
				logger.debug("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		}
	}

}
