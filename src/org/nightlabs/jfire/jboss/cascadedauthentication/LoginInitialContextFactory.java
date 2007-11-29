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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnp.interfaces.NamingContextFactory;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class LoginInitialContextFactory extends NamingContextFactory
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(LoginInitialContextFactory.class);

	public LoginInitialContextFactory()
	{
	}

	/**
	 * @see org.jnp.interfaces.NamingContextFactory#getInitialContext(java.util.Hashtable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Context getInitialContext(Hashtable env) throws NamingException
	{
		if(logger.isDebugEnabled())
			logger.debug("getInitialContext("+env.toString()+")");

		// Get the login principal and credentials from the JNDI env
		Object credentials = env.get(Context.SECURITY_CREDENTIALS);
		Object principal = env.get(Context.SECURITY_PRINCIPAL);
		// Get the principal username
		String username = principal != null ? principal.toString() : null;
		String password = credentials != null ? credentials.toString() : null;

		return new CascadedAuthenticationNamingContext(super.getInitialContext(env), new UserDescriptor(username, password));
	}

	/**
	 * @see org.jnp.interfaces.NamingContextFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getObjectInstance(
			Object obj,
			Name name,
			Context nameCtx,
			Hashtable environment) throws Exception
	{
		if(logger.isDebugEnabled())
			logger.debug("getObjectInstance("+obj.toString()+", "+name.toString()+", "+nameCtx.toString()+", "+environment.toString());
		return super.getObjectInstance(obj, name, nameCtx, environment);
	}
}
