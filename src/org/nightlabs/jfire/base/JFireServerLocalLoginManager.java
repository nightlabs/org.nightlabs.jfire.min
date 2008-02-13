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

package org.nightlabs.jfire.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.UserLocal;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireServerLocalLoginManager
implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String PRINCIPAL_LOCALQUEUEWRITER = "_LocalQueueWriter_";
	public static final String PRINCIPAL_LOCALQUEUEREADER = "_LocalQueueReader_";

	public static final String JNDI_NAME = "java:/jfire/system/JFireServerLocalLoginManager";

	public static JFireServerLocalLoginManager getJFireServerLocalLoginManager(InitialContext initialContext)
	throws NamingException
	{
		return (JFireServerLocalLoginManager) initialContext.lookup(JNDI_NAME);
	}
	
	public JFireServerLocalLoginManager() {
		LocalPrincipal p = new LocalPrincipal(PRINCIPAL_LOCALQUEUEWRITER);
		p.addRole(PRINCIPAL_LOCALQUEUEWRITER);
		addPrincipal(p);

		p = new LocalPrincipal(PRINCIPAL_LOCALQUEUEREADER, "test"); // "#!JFire_QueueReaderPassword_39576!"); // TODO load this password from a config file
		p.addRole(PRINCIPAL_LOCALQUEUEREADER);
		addPrincipal(p);
	}

	public static class LocalPrincipal extends SimplePrincipal {
		/**
		 * The serial version of this class.
		 */
		private static final long serialVersionUID = 1L;
		
		private RoleSet roleSet = new RoleSet();

		private String password;

		public LocalPrincipal(String name)
		{
			this(name, null);
		}

		public LocalPrincipal(String name, String password)
		{
			super(name);
			if (password == null)
				password = UserLocal.createPassword(15, 20);
			this.password = password;
		}

		public void addRole(String role) {
			roleSet.addMember(new SimplePrincipal(role));
		}
		public RoleSet getRoleSet() {
			return roleSet;
		}
		public String getPassword()
		{
			return password;
		}
	}

	protected void addPrincipal(LocalPrincipal principal)
	{
		principales.put(principal.getName(), principal);
	}

	/**
	 * key: String userName<br/>
	 * value:
	 */
	protected Map<String, LocalPrincipal> principales = new HashMap<String, LocalPrincipal>();

	public LocalPrincipal getPrincipal(String userName)
	{
		return principales.get(userName);
	}
}
