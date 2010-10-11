package org.nightlabs.jfire.base.jdo;

import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;

/**
 * Provides access to various JDO managers.
 * <p>
 * Implementations must make sure that the {@link JDOLifecycleManager} 
 * instance and {@link Cache} instance work together as the current 
 * implementation depends on a 1-1 instance relationship.
 * </p>
 * <p>
 * Consumers must make sure to use only one instance of this provider
 * per scope. Simple client implementations are encouraged to use
 * {@link GlobalJDOManagerProvider} as it mimics the old (now deprecated)
 * {@link Cache#sharedInstance()}, {@link JDOLifecycleManager#sharedInstance()}
 * and {@link JDOObjectID2PCClassMap#sharedInstance()} behaviour.
 * </P>
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public interface JDOManagerProvider {
	/**
	 * Get the cache.
	 * @return the cache
	 */
	Cache getCache();
	
	/**
	 * Get the lifecycle manager.
	 * @return The lifecycle manager
	 */
	JDOLifecycleManager getLifecycleManager();
	
	/**
	 * Get the ObjectId PersistenceCapable class map
	 * @return The ObjectId PersistenceCapable class map
	 */
	JDOObjectID2PCClassMap getObjectID2PCClassMap();
	
	/**
	 * Close this manager provider and all manager instances owned by this provider.
	 */
	void close();
}
