package org.nightlabs.jfire.base;

import java.util.Hashtable;

/**
 * Interface defining a way to obtain instances of stateless JavaEE session beans (EJBs).
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author marco schulze - marco at nightlabs dot de
 */
public interface JFireEjb3Provider {
	/**
	 * Get a remote instance of an Enterprise Java Bean.
	 * <p> 
	 * <b>Note:</b> The usage of this method is not recommended unless the usage of an injected security reflector is not
	 * an option. Use {@link #getRemoteBean(Class)} instead.
	 * </p>
	 * <p>
	 * This method returns the same EJB-proxy
	 * when called multiple times with the same EJB-interface, because it caches the instances
	 * (with the arguments of this method used as key).
	 * <p>
	 * <b>Important:</b> Though this method works with stateful session beans,
	 * you <b>must not</b> use it for them, because the result (multiple threads will use the same proxy)
	 * might lead to arbitrary errors. Since JFire uses only stateless session beans by convention, it is safe
	 * to use this util method with all JFire EJBs.
	 * </p>
	 * <p>
	 * Since there is no portable (i.e. functional on all JavaEE-servers) JNDI-location of EJBs, this method relies on
	 * <i>NightLabs' UnifiedEjbJndi</i>: All EJBs must be accessible in JNDI at the location
	 * <code>"ejb/byRemoteInterface/${qualifiedRemoteInterfaceName}"</code> (e.g.
	 * <code>"ejb/byRemoteInterface/com.mycompany.myproject.MyRemoteInterface"</code>).
	 * For JBoss, you can achieve this by simply
	 * deploying the <code>UnifiedEjbJndiJBoss.sar</code> MBean service archive.
	 * </p>
	 * <p>
	 * The EJB instances are cached and expire after a certain time (usually 30 minutes). Additionally, every EJB instance that
	 * contains a public ping method is tested periodically (usually 30 seconds) to check whether the EJB is still usable. This
	 * ping method must have the following signature:
	 * {@code
	 *		public String ping(String message);
	 * }
	 * Furthermore, every authenticated user should be allowed to call this method. The exceptions declared by the method do not matter.
	 * If such a ping method does not exist, the ping-check is silently skipped. See {@link BaseSessionBeanImpl#ping(String)} for more details
	 * about this ping method.
	 * </p>
	 *
	 * @param <T> the type of the EJB-remote-interface.
	 * @param ejbRemoteInterface the remote EJB-business-interface (e.g. {@link org.nightlabs.jfire.security.JFireSecurityManagerRemote}) for which to get an instance.
	 * @param environment the environment (aka. JNDI initial context properties) specifying the coordinates where to get the bean from (e.g. which server to connect to, which user to authenticate as etc.).
	 * @return an instance of an EJB-proxy implementing the interface specified by the argument <code>ejbRemoteInterface</code>.
	 */
	// TODO this method is actually not needed anymore as the environment aka. initial context properties are given by the security reflector
	// it is kept for backwards compatibility with JFireEjb3Factory
	<T> T getRemoteBean(Class<T> ejbRemoteInterface, Hashtable<?, ?> environment);

	/**
	 * Get a remote instance of an Enterprise Java Bean. This method returns the same EJB-proxy
	 * when called multiple times with the same EJB-interface, because it caches the instances
	 * (with the arguments of this method used as key).
	 * <p>
	 * <b>Important:</b> Though this method works with stateful session beans,
	 * you <b>must not</b> use it for them, because the result (multiple threads will use the same proxy)
	 * might lead to arbitrary errors. Since JFire uses only stateless session beans by convention, it is safe
	 * to use this util method with all JFire EJBs.
	 * </p>
	 * <p>
	 * Since there is no portable (i.e. functional on all JavaEE-servers) JNDI-location of EJBs, this method relies on
	 * <i>NightLabs' UnifiedEjbJndi</i>: All EJBs must be accessible in JNDI at the location
	 * <code>"ejb/byRemoteInterface/${qualifiedRemoteInterfaceName}"</code> (e.g.
	 * <code>"ejb/byRemoteInterface/com.mycompany.myproject.MyRemoteInterface"</code>).
	 * For JBoss, you can achieve this by simply
	 * deploying the <code>UnifiedEjbJndiJBoss.sar</code> MBean service archive.
	 * </p>
	 * <p>
	 * The EJB instances are cached and expire after a certain time (usually 30 minutes). Additionally, every EJB instance that
	 * contains a public ping method is tested periodically (usually 30 seconds) to check whether the EJB is still usable. This
	 * ping method must have the following signature:
	 * {@code
	 *		public String ping(String message);
	 * }
	 * Furthermore, every authenticated user should be allowed to call this method. The exceptions declared by the method do not matter.
	 * If such a ping method does not exist, the ping-check is silently skipped. See {@link BaseSessionBeanImpl#ping(String)} for more details
	 * about this ping method.
	 * </p>
	 *
	 * @param <T> the type of the EJB-remote-interface.
	 * @param ejbRemoteInterface the remote EJB-business-interface (e.g. {@link org.nightlabs.jfire.security.JFireSecurityManagerRemote}) for which to get an instance.
	 * @return an instance of an EJB-proxy implementing the interface specified by the argument <code>ejbRemoteInterface</code>.
	 */
	<T> T getRemoteBean(Class<T> ejbRemoteInterface);
	
	/**
	 * Get a local instance of an Enterprise Java Bean. This method currently does not yet cache
	 * anything, but it might do that soon. Hence, it might return the same EJB-proxy
	 * when called multiple times with the same EJB-interface.
	 * <p>
	 * <b>Important:</b> Though this method works with stateful session beans,
	 * you <b>must not</b> use it for them, because the result (multiple threads will use the same proxy)
	 * might lead to arbitrary errors. Since JFire uses only stateless session beans by convention, it is safe
	 * to use this util method with all JFire EJBs.
	 * </p>
	 * <p>
	 * Since there is no portable (i.e. functional on all JavaEE-servers) JNDI-location of EJBs, this method relies on
	 * <i>NightLabs' UnifiedEjbJndi</i>: All EJBs must be accessible in JNDI at the location
	 * <code>"ejb/byLocalInterface/${qualifiedLocalInterfaceName}"</code> (e.g.
	 * <code>"ejb/byLocalInterface/com.mycompany.myproject.MyLocalInterface"</code>).
	 * For JBoss, you can achieve this by simply deploying the <code>UnifiedEjbJndiJBoss.sar</code> MBean service archive.
	 * </p>
	 *
	 * @param <T> the type of the EJB-local-interface.
	 * @param ejbLocalInterface the local EJB-business-interface (e.g. {@link org.nightlabs.jfire.timer.TimerManagerLocal}) for which to get an instance.
	 * @return an instance of an EJB-proxy implementing the interface specified by the argument <code>ejbLocalInterface</code>.
	 */
	<T> T getLocalBean(Class<T> ejbLocalInterface);
}
