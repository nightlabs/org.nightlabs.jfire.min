package org.nightlabs.jfire.base.jdo;

import org.nightlabs.config.Config;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.cache.CacheCfMod;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * JDO manager provider using a global scope shared instance. {@link JDOLifecycleManager} is
 * created using the {@link #PROPERTY_KEY_JDO_LIFECYCLE_MANAGER} system property to
 * remain backwads compatible with the old implementation in {@link JDOLifecycleManager#sharedInstance()}.
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class GlobalJDOManagerProvider implements JDOManagerProvider {

	public static final String PROPERTY_KEY_JDO_LIFECYCLE_MANAGER = "org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager";
	
	private static JDOManagerProvider sharedInstance;
	
	/**
	 * Get the global JDO manager provider shared instance.
	 * @return The shared instance.
	 */
	public static JDOManagerProvider sharedInstance() {
		if(sharedInstance == null) {
			sharedInstance = new GlobalJDOManagerProvider();
		}
		return sharedInstance;
	}
	
	/**
	 * Set the global JDO manager provider shared instance. If no shared instance
	 * is set explicitly, it will be created with the first call to {@link #sharedInstance()}.
	 * @param sharedInstance the sharedInstance to set
	 * @throws IllegalStateException If the shared instance was already created or set.
	 */
	public static void setSharedInstance(JDOManagerProvider sharedInstance) {
		if(GlobalJDOManagerProvider.sharedInstance != null) {
			throw new IllegalStateException("Shared instance is already set");
		}
		GlobalJDOManagerProvider.sharedInstance = sharedInstance;
	}

	private boolean initialized = false;
	private Cache cache;
	private boolean autoOpenCache = true;
	private JDOLifecycleManager lifecycleManager;
	private Class<?> jdoLifecycleManagerClass = null;
	private JDOObjectID2PCClassMap objectID2PCClassMap;
	
	/**
	 * Not to be initialized. Use {@link #sharedInstance()}.
	 */
	protected GlobalJDOManagerProvider() {
	}

	private JDOLifecycleManager createJDOLifecycleManager()
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
	
	/**
	 * Initialize all members. Currently, the only reason to do this here
	 * and not already in the constructor is the {@link #autoOpenCache} flag
	 * which may only be set after calling {@link #sharedInstance()}.
	 */
	protected void initialize() {
		if(!initialized) {
			CacheCfMod cacheCfMod = Config.sharedInstance().createConfigModule(CacheCfMod.class);
			Config.sharedInstance().save(); // TODO remove this as soon as we have a thread that periodically saves it.
			cache = new Cache(cacheCfMod);
			cache.setJdoManagerProvider(this);
			lifecycleManager = createJDOLifecycleManager();
			lifecycleManager.setJdoManagerProvider(this);
			objectID2PCClassMap = new JDOObjectID2PCClassMap();
			if(autoOpenCache) {
				cache.open(SecurityReflector.getUserDescriptor().getSessionID());
			}
		}
		initialized = true;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOManagerProvider#getCache()
	 */
	@Override
	public Cache getCache() {
		initialize();
		return cache;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOManagerProvider#getLifecycleManager()
	 */
	@Override
	public JDOLifecycleManager getLifecycleManager() {
		initialize();
		return lifecycleManager;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOManagerProvider#getObjectID2PCClassMap()
	 */
	@Override
	public JDOObjectID2PCClassMap getObjectID2PCClassMap() {
		initialize();
		return objectID2PCClassMap;
	}
	
	/**
	 * Set the autoOpenCache.
	 * @param autoOpenCache the autoOpenCache to set
	 */
	public void setAutoOpenCache(boolean autoOpenCache) {
		if(initialized) {
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
