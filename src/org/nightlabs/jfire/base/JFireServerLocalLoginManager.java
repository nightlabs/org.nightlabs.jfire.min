/*
 * Created on Mar 26, 2005
 */
package org.nightlabs.jfire.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.security.SimplePrincipal;
import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JFireServerLocalLoginManager
implements Serializable
{
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
				password = UserLocal.generatePassword(15, 20);
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
	protected Map principales = new HashMap(); 

	public LocalPrincipal getPrincipal(String userName)
	{
		return (LocalPrincipal) principales.get(userName);
	}

}
