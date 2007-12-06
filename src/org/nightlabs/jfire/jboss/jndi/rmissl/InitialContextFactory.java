package org.nightlabs.jfire.jboss.jndi.rmissl;


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import org.jboss.security.jndi.*;

public class InitialContextFactory extends LoginInitialContextFactory {

	
	
	
	public Context getInitialContext(Hashtable arg0)
			throws NamingException {
		// TODO Auto-generated method stub
		return new NamingContext(super.getInitialContext(arg0));
	
	}

	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable environment) throws Exception {
		// TODO Auto-generated method stub
		return super.getObjectInstance(obj, name, nameCtx, environment);
	}
	
	
	
	
	
	

}
