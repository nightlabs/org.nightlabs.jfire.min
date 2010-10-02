package org.nightlabs.jfire.security;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * VM-wide security reflector instance accessor. This implements the lookup behaviour formally implemented
 * in the now deprecated class {@link SecurityReflector}.
 * <p>
 * A global security reflector implementation can be injected in 3 ways:
 * <ol>
 * <li>By calling {@link #setSharedInstance(ISecurityReflector)}. This method can only be called once and only
 * before a shared instance was created using one of the other methods. The usage of this method is only
 * recommended for non-default implementations.</li>
 * <li>By system property {@link #PROPERTY_KEY_SECURITY_REFLECTOR_CLASS}. The system property must contain
 * the fully qualified name of a class implementing {@link ISecurityReflector}. The class must be available
 * using the class loader of {@link GlobalSecurityReflector}. This is usually the default way to inject a 
 * security reflector for client implementations.</li>
 * <li>By JNDI lookup with JNDI name {@link #JNDI_NAME}. The JNDI instance will not be stored as shared instance
 * but looked up from JNDI every time it is needed. This is usually the default way to inject a 
 * security reflector for server implementations.</li>
 * </ol>
 * </p>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class GlobalSecurityReflector {
	public static final String JNDI_NAME = "java:/jfire/system/SecurityReflector";

	public static final String PROPERTY_KEY_SECURITY_REFLECTOR_CLASS = "org.nightlabs.jfire.security.SecurityReflector";

	/**
	 * This is used, if we're not using JNDI, but a System property (i.e. in the client)
	 */
	private static ISecurityReflector sharedInstance = null;

	/**
	 * Get the global shared instance. If no shared instance yet exists, 
	 * {@link #lookupSecurityReflector(InitialContext)} will be invoked
	 * with initialContext == null.
	 */
	public static ISecurityReflector sharedInstance()
	{
		if(sharedInstance == null) {
			return lookupSecurityReflector(null);
		} else {
			return sharedInstance;
		}
	}
	
	/**
	 * Set the global security reflector shared instance. If no shared instance
	 * is set explicitly, it will be created with the first call to {@link #sharedInstance()} or
	 * {@link #lookupSecurityReflector(InitialContext)}.
	 * @param sharedInstance the sharedInstance to set
	 * @throws IllegalStateException If the shared instance was already created or set.
	 */
	public static void setSharedInstance(ISecurityReflector sharedInstance) {
		if(GlobalSecurityReflector.sharedInstance != null) {
			throw new IllegalStateException("Shared instance is already set");
		}
		GlobalSecurityReflector.sharedInstance = sharedInstance;
	}

	public static ISecurityReflector lookupSecurityReflector(InitialContext initialContext)
	{
		// FIXME this is even worse than goto! Marc
		createLocalVMSharedInstance:
		if (sharedInstance == null) {
			String className = System.getProperty(PROPERTY_KEY_SECURITY_REFLECTOR_CLASS);
			if (className == null)
				break createLocalVMSharedInstance;

			Class<?> clazz;
			try {
				//clazz = Class.forName(className);
				// use the class loader of this class instead the context class loader:
				clazz = Class.forName(className, true, GlobalSecurityReflector.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			try {
				sharedInstance = (ISecurityReflector) clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		if (sharedInstance != null)
			return sharedInstance;

		boolean closeInitialContext = initialContext == null;
		try {
			if (closeInitialContext)
				initialContext = new InitialContext();

			try {
				return (ISecurityReflector) initialContext.lookup(JNDI_NAME);
			} finally {
				if (closeInitialContext)
					initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException("The SecurityReflector has neither been specified by the system property \""+PROPERTY_KEY_SECURITY_REFLECTOR_CLASS+"\" nor is it bound to JNDI!", x);
		}
	}
}
