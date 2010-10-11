package org.nightlabs.jfire.base.jdo;

import org.nightlabs.jfire.base.JFireEjb3Provider;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.cache.CacheCfMod;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;

public abstract class AbstractJDOManagerProvider implements JDOManagerProvider {

	private boolean initialized = false;
	private Cache cache;
	private JDOLifecycleManager lifecycleManager;
	private JDOObjectID2PCClassMap objectID2PCClassMap;
	
	protected Cache createCache() {
		return new Cache(getCacheConfig());
	}
	
	protected JDOLifecycleManager createLifecycleManager() {
		return new JDOLifecycleManager();
	}
	
	protected JDOObjectID2PCClassMap createObjectID2PCClassMap() {
		return new JDOObjectID2PCClassMap();
	}
	
	protected abstract CacheCfMod getCacheConfig();
	protected abstract JFireEjb3Provider getEJBProvider();

	private synchronized void initialize() {
		if(!initialized) {
			beforeInitialization();
			cache = createCache();
			cache.setEjbProvider(getEJBProvider());
			cache.setJdoManagerProvider(this);
			lifecycleManager = createLifecycleManager();
			lifecycleManager.setJdoManagerProvider(this);
			objectID2PCClassMap = createObjectID2PCClassMap();
			initialized = true;
			afterInitialization();
		}
	}

	protected void beforeInitialization() {
	}

	protected void afterInitialization() {
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOManagerProvider#close()
	 */
	@Override
	public synchronized void close() {
		if(initialized) {
			cache.close();
			cache = null;
			lifecycleManager = null;
			objectID2PCClassMap = null;
			initialized = false;
		}
	}
	
	protected boolean isInitialized() {
		return initialized;
	}
}
