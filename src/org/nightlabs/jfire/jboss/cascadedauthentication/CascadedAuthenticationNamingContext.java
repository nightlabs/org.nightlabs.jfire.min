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
import java.lang.reflect.Proxy;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jboss.proxy.ClientContainer;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CascadedAuthenticationNamingContext implements Context
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CascadedAuthenticationNamingContext.class);
	
	protected Context delegate;
	protected UserDescriptor userDescriptor = null;

	public CascadedAuthenticationNamingContext(Context _delegate, UserDescriptor _userDescriptor)
			throws NamingException
	{
		this.delegate = _delegate;
		this.userDescriptor = _userDescriptor;
	}

	protected Object afterLookup(Object obj)
	{
		InvocationHandler invocationHandler = Proxy.getInvocationHandler(obj);
//		if(logger.isDebugEnabled())
//			logger.debug("Lookup: InvocationHandler: "+invocationHandler.toString());
		ClientContainer clientContainer = (ClientContainer)invocationHandler;

		if(logger.isDebugEnabled())
			logger.debug("afterLookup: mark invocation context: username=" + userDescriptor.userName + " clientContainer=" +
					clientContainer + " context=" + // (clientContainer.context == null ? null : clientContainer.context.getClass().getName())+'@'+System.identityHashCode(clientContainer.context) + "#"+
					clientContainer.context.toString());

		clientContainer.context.setValue(UserDescriptor.CONTEXT_KEY, userDescriptor);
		return obj;
	}

	/**
	 * @see javax.naming.Context#lookup(javax.naming.Name)
	 */
	public Object lookup(Name name) throws NamingException
	{
		UserDescriptor.setUserDescriptor(userDescriptor); // TODO do we really need this???
		try {
			return afterLookup(delegate.lookup(name));
		} finally {
			UserDescriptor.unsetUserDescriptor();
		}
	}

	/**
	 * @see javax.naming.Context#lookup(java.lang.String)
	 */
	public Object lookup(String name) throws NamingException
	{
		UserDescriptor.setUserDescriptor(userDescriptor); // TODO do we really need this???
		try {
			return afterLookup(delegate.lookup(name));
		} finally {
			UserDescriptor.unsetUserDescriptor();
		}
	}

	/**
	 * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
	 */
	public void bind(Name name, Object obj) throws NamingException
	{
		delegate.bind(name, obj);
	}

	/**
	 * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
	 */
	public void bind(String name, Object obj) throws NamingException
	{
		delegate.bind(name, obj);
	}

	/**
	 * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
	 */
	public void rebind(Name name, Object obj) throws NamingException
	{
		delegate.rebind(name, obj);
	}

	/**
	 * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
	 */
	public void rebind(String name, Object obj) throws NamingException
	{
		delegate.rebind(name, obj);
	}

	/**
	 * @see javax.naming.Context#unbind(javax.naming.Name)
	 */
	public void unbind(Name name) throws NamingException
	{
		delegate.unbind(name);
	}

	/**
	 * @see javax.naming.Context#unbind(java.lang.String)
	 */
	public void unbind(String name) throws NamingException
	{
		delegate.unbind(name);
	}

	/**
	 * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
	 */
	public void rename(Name oldName, Name newName) throws NamingException
	{
		delegate.rename(oldName, newName);
	}

	/**
	 * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
	 */
	public void rename(String oldName, String newName) throws NamingException
	{
		delegate.rename(oldName, newName);
	}

	/**
	 * @see javax.naming.Context#list(javax.naming.Name)
	 */
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
	{
		return delegate.list(name);
	}

	/**
	 * @see javax.naming.Context#list(java.lang.String)
	 */
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException
	{
		return delegate.list(name);
	}

	/**
	 * @see javax.naming.Context#listBindings(javax.naming.Name)
	 */
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
	{
		return delegate.listBindings(name);
	}

	/**
	 * @see javax.naming.Context#listBindings(java.lang.String)
	 */
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException
	{
		return delegate.listBindings(name);
	}

	/**
	 * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
	 */
	public void destroySubcontext(Name name) throws NamingException
	{
		delegate.destroySubcontext(name);
	}

	/**
	 * @see javax.naming.Context#destroySubcontext(java.lang.String)
	 */
	public void destroySubcontext(String name) throws NamingException
	{
		delegate.destroySubcontext(name);
	}

	/**
	 * @see javax.naming.Context#createSubcontext(javax.naming.Name)
	 */
	public Context createSubcontext(Name name) throws NamingException
	{
		return delegate.createSubcontext(name);
	}

	/**
	 * @see javax.naming.Context#createSubcontext(java.lang.String)
	 */
	public Context createSubcontext(String name) throws NamingException
	{
		return delegate.createSubcontext(name);
	}

	/**
	 * @see javax.naming.Context#lookupLink(javax.naming.Name)
	 */
	public Object lookupLink(Name name) throws NamingException
	{
		// TODO is it correct to do that cascading authentication stuff here?!
		UserDescriptor.setUserDescriptor(userDescriptor); // TODO do we really need this???
		try {
			return afterLookup(delegate.lookupLink(name));
		} finally {
			UserDescriptor.unsetUserDescriptor();
		}
	}

	/**
	 * @see javax.naming.Context#lookupLink(java.lang.String)
	 */
	public Object lookupLink(String name) throws NamingException
	{
		// TODO is it correct to do that cascading authentication stuff here?!
		UserDescriptor.setUserDescriptor(userDescriptor); // TODO do we really need this???
		try {
			return afterLookup(delegate.lookupLink(name));
		} finally {
			UserDescriptor.unsetUserDescriptor();
		}
	}

	/**
	 * @see javax.naming.Context#getNameParser(javax.naming.Name)
	 */
	public NameParser getNameParser(Name name) throws NamingException
	{
		return delegate.getNameParser(name);
	}

	/**
	 * @see javax.naming.Context#getNameParser(java.lang.String)
	 */
	public NameParser getNameParser(String name) throws NamingException
	{
		return delegate.getNameParser(name);
	}

	/**
	 * @see javax.naming.Context#composeName(javax.naming.Name, javax.naming.Name)
	 */
	public Name composeName(Name name, Name prefix) throws NamingException
	{
		return delegate.composeName(name, prefix);
	}

	/**
	 * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
	 */
	public String composeName(String name, String prefix) throws NamingException
	{
		return delegate.composeName(name, prefix);
	}

	/**
	 * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
	 */
	public Object addToEnvironment(String propName, Object propVal) throws NamingException
	{
		return delegate.addToEnvironment(propName, propVal);
	}

	/**
	 * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
	 */
	public Object removeFromEnvironment(String propName) throws NamingException
	{
		return delegate.removeFromEnvironment(propName);
	}

	/**
	 * @see javax.naming.Context#getEnvironment()
	 */
	public Hashtable<?,?> getEnvironment() throws NamingException
	{
		return delegate.getEnvironment();
	}

	/**
	 * @see javax.naming.Context#close()
	 */
	public void close() throws NamingException
	{
		delegate.close();
	}

	/**
	 * @see javax.naming.Context#getNameInNamespace()
	 */
	public String getNameInNamespace() throws NamingException
	{
		return delegate.getNameInNamespace();
	}
}
