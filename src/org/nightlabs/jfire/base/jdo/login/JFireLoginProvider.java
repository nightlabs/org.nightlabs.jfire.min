/**
 * 
 */
package org.nightlabs.jfire.base.jdo.login;

import java.lang.reflect.Constructor;


/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class JFireLoginProvider {

	public static final String PROPERTY_KEY_INTITIAL_CONTEXT_PROPERTY_PROVIDER = "org.nightlabs.jfire.base.jdo.login.InitialContextPropertyProvider";
	
	/**
	 * 
	 */
	public JFireLoginProvider() {
	}
	
	
	/**
	 * This is used, if we're not using JNDI, but a System property (i.e. in the client)
	 */
	private static IJFireLoginProvider sharedInstance = null;

	/**
	 * This method calls {@link #lookupInitialContextPropertyProvider()}.
	 */
	public static IJFireLoginProvider sharedInstance()
	{
		return lookupInitialContextPropertyProvider();
	}	
	
	
	public static IJFireLoginProvider lookupInitialContextPropertyProvider()
	{
		if (sharedInstance == null) {
			String className = System.getProperty(PROPERTY_KEY_INTITIAL_CONTEXT_PROPERTY_PROVIDER);
			if (className == null)
				return null;;

			Class clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			Constructor constructor = null;
			try {
				constructor = clazz.getDeclaredConstructor(new Class[] {});
			} catch (Exception e) {
				throw new IllegalStateException("Could not find default constructor for "+clazz.getName());
			}
			try {
				constructor.setAccessible(true);
				sharedInstance = (IJFireLoginProvider) constructor.newInstance(new Object[] {});
			} catch (Exception e) {
				throw new IllegalStateException("Could not instantiate "+clazz.getName());
			}
		}

		return sharedInstance;
	}
	

}
