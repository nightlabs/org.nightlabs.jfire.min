package org.nightlabs.jfire.jboss.jndi.rmissl;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

public class LoginInitialContextFactory
extends org.jboss.security.jndi.LoginInitialContextFactory
{

	@SuppressWarnings("unchecked")
	public Context getInitialContext(Hashtable arg0)
	throws NamingException
	{
		return new NamingContext(super.getInitialContext(arg0));
	}

	@SuppressWarnings("unchecked")
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
	throws Exception
	{
		return super.getObjectInstance(obj, name, nameCtx, environment);
	}

}
