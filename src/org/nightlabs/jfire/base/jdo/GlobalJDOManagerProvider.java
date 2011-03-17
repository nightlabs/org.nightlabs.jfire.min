package org.nightlabs.jfire.base.jdo;

import org.nightlabs.config.Config;
import org.nightlabs.jfire.base.GlobalJFireEjb3Provider;
import org.nightlabs.jfire.base.JFireEjb3Provider;
import org.nightlabs.jfire.base.jdo.cache.CacheCfMod;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.singleton.ISingletonProvider;
import org.nightlabs.singleton.SingletonProviderFactory;
import org.nightlabs.singleton.ISingletonProvider.ISingletonFactory;

/**
 * JDO manager provider using a global scope shared instance. {@link JDOLifecycleManager} is
 * created using the {@link #PROPERTY_KEY_JDO_LIFECYCLE_MANAGER} system property to
 * remain backwads compatible with the old implementation in {@link JDOLifecycleManager#sharedInstance()}.
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class GlobalJDOManagerProvider extends AbstractJDOManagerProvider {

	public static final String PROPERTY_KEY_JDO_LIFECYCLE_MANAGER = "org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager";
	
	//private static JDOManagerProvider sharedInstance;
	
	private static ISingletonProvider<JDOManagerProvider> sharedInstanceProvider;
	
	
	/**
	 * Get the global JDO manager provider shared instance.
	 * @return The shared instance.
	 */
	public static JDOManagerProvider sharedInstance() {
		if(sharedInstanceProvider == null) {
			sharedInstanceProvider = SingletonProviderFactory.createProvider();
			sharedInstanceProvider.setFactory(new ISingletonFactory<JDOManagerProvider>() {
				@Override
				public JDOManagerProvider makeInstance() {
					return new GlobalJDOManagerProvider();
				}
			});
		}
		
		return sharedInstanceProvider.getInstance();
	}
	
	/**
	 * Set the global JDO manager provider shared instance. If no shared instance
	 * is set explicitly, it will be created with the first call to {@link #sharedInstance()}.
	 * @param sharedInstance the sharedInstance to set
	 * @throws IllegalStateException If the shared instance was already created or set.
	 */
	public static void setSharedInstanceProvider(ISingletonProvider<JDOManagerProvider> provider) {
		if(sharedInstanceProvider != null) {
			throw new IllegalStateException("Shared instance provider is already set");
		}
		
		sharedInstanceProvider = provider;
	}

	private boolean autoOpenCache = true;
	private Class<?> jdoLifecycleManagerClass = null;
	
	/**
	 * Not to be initialized. Use {@link #sharedInstance()}.
	 */
	protected GlobalJDOManagerProvider() {
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.AbstractJDOManagerProvider#createLifecycleManager()
	 */
	@Override
	protected JDOLifecycleManager createLifecycleManager()
	{
		if (jdoLifecycleManagerClass == null) {
			String className = System.getProperty(PROPERTY_KEY_JDO_LIFECYCLE_MANAGER);
			if (className == null)
				throw new IllegalStateException("System property PROPERTY_KEY_JDO_LIFECYCLE_MANAGER (" + PROPERTY_KEY_JDO_LIFECYCLE_MANAGER + ") not set!");

			try {
				//jdoLifecycleManagerClass = Class.forName(className);
				// use the class loader of this class instead the context class loader:
				jdoLifecycleManagerClass = Class.forName(className, true, JDOLifecycleManager.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			return (JDOLifecycleManager) jdoLifecycleManagerClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Error creating JDOLifecycleManager instance", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.AbstractJDOManagerProvider#getEJBProvider()
	 */
	@Override
	protected JFireEjb3Provider getEJBProvider() {
		return GlobalJFireEjb3Provider.sharedInstance();
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.AbstractJDOManagerProvider#getCacheConfig()
	 */
	@Override
	protected CacheCfMod getCacheConfig() {
		CacheCfMod cacheCfMod = Config.sharedInstance().createConfigModule(CacheCfMod.class);
		Config.sharedInstance().save(); // TODO remove this as soon as we have a thread that periodically saves it.
		return cacheCfMod;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.AbstractJDOManagerProvider#afterInitialization()
	 */
	@Override
	protected void afterInitialization() {
		if(autoOpenCache) {
			getCache().open(SecurityReflector.getUserDescriptor().getSessionID());
		}
	}
	
	/**
	 * Set the autoOpenCache.
	 * @param autoOpenCache the autoOpenCache to set
	 */
	public void setAutoOpenCache(boolean autoOpenCache) {
		if(isInitialized()) {
			throw new IllegalStateException("Cache is already initialized");
		}
		this.autoOpenCache = autoOpenCache;
	}
	
	/**
	 * Get the autoOpenCache.
	 * @return the autoOpenCache
	 */
	public boolean isAutoOpenCache() {
		return autoOpenCache;
	}
}
