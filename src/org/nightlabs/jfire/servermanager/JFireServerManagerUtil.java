/**
 * 
 */
package org.nightlabs.jfire.servermanager;

import javax.naming.NamingException;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.InitException;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class JFireServerManagerUtil {

	/**
	 * Returns a JFireServerManager for the organisationID of
	 * the session on the current thread. The reciever is
	 * responsible for closing the manager.
	 * 
	 * @return A JFireServerManager for the session on the current thread.
	 */
	public static JFireServerManager getJFireServerManager()
	throws InitException, ModuleException, NamingException
	{
		return new Lookup(SecurityReflector.getUserDescriptor().getOrganisationID()).getJFireServerManager();
	}

}
