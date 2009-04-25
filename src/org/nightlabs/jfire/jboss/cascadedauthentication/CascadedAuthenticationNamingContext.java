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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.jboss.security.SecurityAssociation;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CascadedAuthenticationNamingContext implements Context
{
	private static final Logger logger = Logger.getLogger(CascadedAuthenticationNamingContext.class);

	private Context delegate;
	private UserDescriptor userDescriptor;

	public CascadedAuthenticationNamingContext(Context _delegate, UserDescriptor _userDescriptor)
	throws NamingException
	{
		if (_delegate == null)
			throw new IllegalArgumentException("_delegate must not be null!");

		if (_userDescriptor == null)
			throw new IllegalArgumentException("_userDescriptor must not be null!");

		this.delegate = _delegate;
		this.userDescriptor = _userDescriptor;
	}

	private J2EEAdapter j2eeAdapter = null;

	protected J2EEAdapter getJ2EEAdapter()
	throws NamingException
	{
		if (j2eeAdapter == null) { // no need to do this synchronized - in the worst case we obtain it multiple times - doesn't have any negative consequences.
			InitialContext ctx = new InitialContext();
			try {
				j2eeAdapter = (J2EEAdapter) ctx.lookup(J2EEAdapter.JNDI_NAME);
			} finally {
				ctx.close();
			}
		}
		return j2eeAdapter;
	}

	protected UserDescriptor getUserDescriptor() {
		return userDescriptor;
	}

	protected Object wrapProxy(Object object)
	{
		long startTimestamp = System.currentTimeMillis();
		if (logger.isDebugEnabled())
			logger.debug("wrapProxy: Beginning for: " + object + " (" + (object == null ? null : object.getClass().getName()) + ")");

		if (object == null) {
			if (logger.isDebugEnabled())
				logger.debug("wrapProxy: object is null; nothing to do.");

			return null;
		}

		Class<? extends Object> objectClass = object.getClass();

		// Accessing simple local objects (e.g. String) via a proxy is not necessary. Hence, we
		// directly return the unwrapped object, if it is no proxy.
		if (!Proxy.isProxyClass(objectClass)) {
			if (logger.isDebugEnabled())
				logger.debug("wrapProxy: object is no proxy; nothing to do.");

			return object;
		}

		InvocationHandler wrappedProxyInvocationHandler = Proxy.getInvocationHandler(object);
		if (wrappedProxyInvocationHandler instanceof CascadedAuthenticationInvocationHandler) {
			CascadedAuthenticationNamingContext wrappedProxyNamingContext = ((CascadedAuthenticationInvocationHandler)wrappedProxyInvocationHandler).getNamingContext();

			// The userDescriptor is immutable. Hence, it is not necessary to wrap a second proxy around, if there
			// is already a proxy for cascaded authentication with an equal user descriptor.
			if (this.userDescriptor.equals(wrappedProxyNamingContext.getUserDescriptor()))
				return object;
		}

		long collectInterfacesStartTimestamp = System.currentTimeMillis();
		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		collectAllInterfaces(interfaces, objectClass);
		Class<?>[] interfaceArray = interfaces.toArray(new Class[interfaces.size()]);
		if (logger.isTraceEnabled())
			logger.trace("wrapProxy: Collecting interfaces took " + (System.currentTimeMillis() - collectInterfacesStartTimestamp) + " msec.");

		long createProxyStartTimestamp = System.currentTimeMillis();
		Object wrappingProxy = Proxy.newProxyInstance(
				objectClass.getClassLoader(),
				interfaceArray,
				new CascadedAuthenticationInvocationHandler(this, object)
		);
		if (logger.isTraceEnabled())
			logger.trace("wrapProxy: Creating proxy took " + (System.currentTimeMillis() - createProxyStartTimestamp) + " msec.");

		if (logger.isDebugEnabled())
			logger.debug("wrapProxy: Done in " + (System.currentTimeMillis() - startTimestamp) + " msec for: " + object + " (" + (object == null ? null : object.getClass().getName()) + ")");

		return wrappingProxy;
	}

	protected static class CascadedAuthenticationInvocationHandler implements InvocationHandler
	{
		private CascadedAuthenticationNamingContext namingContext;
		private Object wrappedProxy;

		public CascadedAuthenticationInvocationHandler(CascadedAuthenticationNamingContext namingContext, Object wrappedProxy) {
			this.namingContext = namingContext;
			this.wrappedProxy = wrappedProxy;
		}

		public CascadedAuthenticationNamingContext getNamingContext() {
			return namingContext;
		}

		public Object getWrappedProxy() {
			return wrappedProxy;
		}

		@Override
		public Object invoke(Object wrappingProxy, Method method, Object[] args) throws Throwable
		{
			return namingContext.invokeProxyMethod(wrappedProxy, wrappingProxy, method, args);
		}
	}

	protected Object invokeProxyMethod(Object wrappedProxy, Object wrappingProxy, Method method, Object[] args) throws Throwable
	{
		// First of all, short-cut the local methods toString, hashCode and equals.
		String methodName = method.getName();
		if (
				("hashCode".equals(methodName) || "toString".equals(methodName)) &&
				method.getParameterTypes().length == 0
		)
		{
			return method.invoke(wrappedProxy, args);
		}

		if ("equals".equals(methodName) && method.getParameterTypes().length == 1)
		{
			return method.invoke(wrappedProxy, args);
		}


		// Now, we find out our current identity to determine whether we need to change the identity.
		Principal oldPrincipal = SecurityAssociation.getPrincipal();
//		Object oldCredential = SecurityAssociation.getCredential();

		String oldUserName = oldPrincipal == null ? null : oldPrincipal.getName();
		if (oldUserName != null) {
			int idx = oldUserName.indexOf('?');
			if (idx >= 0)
				oldUserName = oldUserName.substring(0, idx);
		}

		String newUserName = userDescriptor.getUserName();
		int idx = newUserName.indexOf('?');
		if (idx >= 0)
			newUserName = newUserName.substring(0, idx);

		boolean changeIdentity = !newUserName.equals(oldUserName);

		LoginData loginData = null;
		LoginContext loginContext = null;
		if (changeIdentity) {
			if (logger.isDebugEnabled())
				logger.debug("invokeProxyMethod: calling loginContext.login()");

			loginData = new LoginData(userDescriptor.getUserName(), userDescriptor.getPassword());
			loginData.setDefaultValues();

			loginContext = getJ2EEAdapter().createLoginContext(loginData.getSecurityProtocol(), new JFireLogin(loginData).getAuthCallbackHandler());
			loginContext.login();
		}

		Object result;
		try {

			result = method.invoke(wrappedProxy, args);

		} finally {
			if (loginContext != null) {
				// We have to logout, because we must use restore-login-identity, since it otherwise doesn't
				// work with a mix of local beans (e.g. StoreManagerHelperLocal) and foreign-organisation
				// non-local beans (e.g. TradeManager on another organisation).
				if (logger.isDebugEnabled())
					logger.debug("invoke: calling loginContext.logout()");

				loginContext.logout();
			} // if (loginContext != null) {
		}

		return wrapProxy(result);
	}

	/**
	 * Collect all interfaces that are implemented directly or via a superclass or via
	 * other interfaces.
	 *
	 * @param interfaces the {@link Set} to be populated which must not be <code>null</code>.
	 * @param theClass the {@link Class} to analyse; can be <code>null</code>.
	 */
	private static void collectAllInterfaces(Set<Class<?>> interfaces, Class<?> theClass)
	{
		if (interfaces == null)
			throw new IllegalArgumentException("inferfaces must not be null!");

		Class<?> clazz = theClass;
		while (clazz != null) {
			for (Class<?> iface : clazz.getInterfaces()) {
				if (interfaces.add(iface))
					collectAllInterfaces(interfaces, iface);
			}

			clazz = clazz.getSuperclass();
		}
	}


	@Override
	public Object lookup(Name name) throws NamingException
	{
		return wrapProxy(delegate.lookup(name));
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		return wrapProxy(delegate.lookup(name));
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException
	{
		delegate.bind(name, obj);
	}

	@Override
	public void bind(String name, Object obj) throws NamingException
	{
		delegate.bind(name, obj);
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException
	{
		delegate.rebind(name, obj);
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException
	{
		delegate.rebind(name, obj);
	}

	@Override
	public void unbind(Name name) throws NamingException
	{
		delegate.unbind(name);
	}

	@Override
	public void unbind(String name) throws NamingException
	{
		delegate.unbind(name);
	}

	@Override
	public void rename(Name oldName, Name newName) throws NamingException
	{
		delegate.rename(oldName, newName);
	}

	@Override
	public void rename(String oldName, String newName) throws NamingException
	{
		delegate.rename(oldName, newName);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
	{
		return delegate.list(name);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException
	{
		return delegate.list(name);
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
	{
		return delegate.listBindings(name);
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException
	{
		return delegate.listBindings(name);
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException
	{
		delegate.destroySubcontext(name);
	}

	@Override
	public void destroySubcontext(String name) throws NamingException
	{
		delegate.destroySubcontext(name);
	}

	@Override
	public Context createSubcontext(Name name) throws NamingException
	{
		return delegate.createSubcontext(name);
	}

	@Override
	public Context createSubcontext(String name) throws NamingException
	{
		return delegate.createSubcontext(name);
	}

	@Override
	public Object lookupLink(Name name) throws NamingException
	{
		return wrapProxy(delegate.lookupLink(name));
	}

	@Override
	public Object lookupLink(String name) throws NamingException
	{
		return wrapProxy(delegate.lookupLink(name));
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException
	{
		return delegate.getNameParser(name);
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException
	{
		return delegate.getNameParser(name);
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException
	{
		return delegate.composeName(name, prefix);
	}

	@Override
	public String composeName(String name, String prefix) throws NamingException
	{
		return delegate.composeName(name, prefix);
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal) throws NamingException
	{
		return delegate.addToEnvironment(propName, propVal);
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException
	{
		return delegate.removeFromEnvironment(propName);
	}

	@Override
	public Hashtable<?,?> getEnvironment() throws NamingException
	{
		return delegate.getEnvironment();
	}

	@Override
	public void close() throws NamingException
	{
		delegate.close();
	}

	@Override
	public String getNameInNamespace() throws NamingException
	{
		return delegate.getNameInNamespace();
	}
}
