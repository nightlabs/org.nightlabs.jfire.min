package org.nightlabs.jfire.jboss.ejb3;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.security.SecurityDomainManager;


/**
 * this class overrides JBoss's default AuthenticationInterceptorFactory, which later will be loading our
 * custom EJB Interceptor.
 * 
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 */
public class JFireEjb3AuthenticationInterceptorFactory implements AspectFactory {

	private static final Logger logger = Logger.getLogger(JFireEjb3AuthenticationInterceptorFactory.class);

	public Object createPerVM()
	{
		throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
	}

	public Object createPerClass(Advisor advisor)
	{
		logger.info("JFireEjb3AuthenticationInterceptorFactory created !!!");
		Object domain = null;
		Container container = (Container)advisor;
		try
		{
			InitialContext ctx = container.getInitialContext();
			SecurityDomain securityAnnotation = (SecurityDomain) advisor.resolveAnnotation(SecurityDomain.class);
			if (securityAnnotation != null)
			{

				domain =  SecurityDomainManager.getSecurityManager(securityAnnotation.value(),ctx);
			}
		}
		catch (NamingException e)
		{
			throw new RuntimeException(e);
		}
		AuthenticationManager manager = (AuthenticationManager) domain;
		return new JFireEjb3AuthenticationInterceptor(manager, container);
	}


	public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
	{
		throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
	}

	public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
	{
		throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
	}

	public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
	{
		throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
	}

	public String getName()
	{
		return getClass().getName();
	} 

}
