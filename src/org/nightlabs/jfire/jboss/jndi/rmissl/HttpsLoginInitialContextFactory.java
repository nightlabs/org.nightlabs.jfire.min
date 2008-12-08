package org.nightlabs.jfire.jboss.jndi.rmissl;

import java.security.Principal;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.naming.HttpNamingContextFactory;
import org.jboss.security.SecurityConstants;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

/**
 * This LoginInitialContextFactory first performs a JAAS-login given the
 * {@link Context#SECURITY_PRINCIPAL} as well as the {@link Context#SECURITY_CREDENTIALS} from the
 * context properties.
 * Then it returns an {@link NamingContext} that forces the use of SSL-encrypted and compression-
 * enabled Beanproxies.
 *
 * @author Marius Heinzmann marius@nightlabs.de
 */
public class HttpsLoginInitialContextFactory
	extends HttpNamingContextFactory
{
	/* (non-Javadoc)
	 * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Context getInitialContext(final Hashtable env)
	throws NamingException
	{
		// copied from org.jboss.security.jndi.LoginInitialContextFactory
		// -------- begin copy --------
		// Get the login configuration name to use, initially set to default.
		String protocol = SecurityConstants.DEFAULT_APPLICATION_POLICY;
		Object prop = env.get(Context.SECURITY_PROTOCOL);
		if( prop != null )
			protocol = prop.toString();

		// Get the login principal and credentials from the JNDI env
		Object credentials = env.get(Context.SECURITY_CREDENTIALS);
		Object principal = env.get(Context.SECURITY_PRINCIPAL);
		try
		{
			// Get the principal username
			String username;
			if( principal instanceof Principal )
			{
				Principal p = (Principal) principal;
				username = p.getName();
			}
			else
			{
				username = principal.toString();
			}
			UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
					credentials);
			// Do the JAAS login
			LoginContext lc = new LoginContext(protocol, handler);
			lc.login();
		}
		catch(LoginException e)
		{
			AuthenticationException ex = new AuthenticationException("Failed to login using protocol="+protocol);
			ex.setRootCause(e);
			throw ex;
		}
		// -------- end copy --------

		// Now return the context using our modified naming context
		return new NamingContext(super.getInitialContext(env));
	}

}
