package org.nightlabs.jfire.base;

import java.util.Hashtable;

import org.nightlabs.jfire.security.GlobalSecurityReflector;


/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class GlobalJFireEjb3Provider extends AbstractJFireEjb3Provider {
	private static JFireEjb3Provider sharedInstance;

	/**
	 * Get the global shared instance.
	 * @return The shared instance
	 */
	public static JFireEjb3Provider sharedInstance() {
		if(sharedInstance == null) {
			sharedInstance = new GlobalJFireEjb3Provider();
		}
		return sharedInstance;
	}
	
	/**
	 * Set the global EJB provider shared instance. If no shared instance
	 * is set explicitly, it will be created with the first call to {@link #sharedInstance()}.
	 * @param sharedInstance the sharedInstance to set
	 * @throws IllegalStateException If the shared instance was already created or set.
	 */
	public static void setSharedInstance(JFireEjb3Provider sharedInstance) {
		if(GlobalJFireEjb3Provider.sharedInstance != null) {
			throw new IllegalStateException("Shared instance is already set");
		}
		GlobalJFireEjb3Provider.sharedInstance = sharedInstance;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.JFireEJB3ProviderImpl#getInitialContextProperties()
	 */
	@Override
	protected Hashtable<?, ?> getInitialContextProperties() {
		return GlobalSecurityReflector.sharedInstance().getInitialContextProperties();
	}
}
