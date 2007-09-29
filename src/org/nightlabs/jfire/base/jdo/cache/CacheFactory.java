/**
 * 
 */
package org.nightlabs.jfire.base.jdo.cache;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.nightlabs.base.ui.cache.ICache;
import org.nightlabs.base.ui.cache.ICacheFactory;

/**
 * This implementation of {@link ICacheFactory} uses the JFire JDO {@link Cache}
 * as backing to implement an object cache.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class CacheFactory implements ICacheFactory {
	
	public static final String CACHE_FACTORY_ID = CacheFactory.class.getName();
	

	/**
	 * The dummy/delegate implementation of {@link ICache} 
	 */
	public static class CacheDelegate implements ICache {
		
		private String[] dummyFetchGroups = {};
		
		private Cache cache;
		
		public CacheDelegate(Cache cache) {
			this.cache = cache;
		}

		public Object get(String scope, Object key) {
			return cache.get(scope, key, dummyFetchGroups, -1);
		}

		public Object get(Object key) {
			return get(null, key);
		}

		public void put(String scope, Object key, Object object) {
			cache.put(scope, key, object, dummyFetchGroups, -1);
		}

		public void put(Object key, Object object) {
			put(null, key, object);
		}

		public void remove(Object key) {
			cache.removeByObjectID(key, false);
		}

		public void removeAll() {
			cache.removeAll();
		}

		public void removeByKeyClass(Class keyClass) {
			cache.removeByObjectIDClass(keyClass);
		}
	}
	
	/**
	 * 
	 */
	public CacheFactory() {
	}

	private CacheDelegate delegate;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.cache.ICacheFactory#createCache()
	 */
	public ICache createCache() {
		if (delegate == null)
			delegate = new CacheDelegate(Cache.sharedInstance());
		return delegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement arg0, String arg1,
			Object arg2) throws CoreException {
		// Yet nothing to do 
	}

	public String getID() {
		return CacheFactory.CACHE_FACTORY_ID;
	}
}
