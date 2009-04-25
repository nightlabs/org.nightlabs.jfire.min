package org.nightlabs.jfire.base;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.timer.TimerManagerLocal;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author marco schulze - marco at nightlabs dot de
 */
public class JFireEjb3Factory
{
	private static final long CACHE_LIFETIME_INSTANCE = 30L * 60L * 1000L; // keep EJB proxies for 30 minutes
	private static final long PING_PERIOD = 30L * 1000L; // execute a ping whenever the EJB proxy is older than that time or the last ping is longer ago that this.

	private static class EjbInstanceWrapper
	{
		private long createTimestamp;
		private long lastPingTimestamp;
		private Object ejbInstance;

		public EjbInstanceWrapper(Object ejbInstance) {
			this.createTimestamp = System.currentTimeMillis();
			this.lastPingTimestamp = System.currentTimeMillis();
			this.ejbInstance = ejbInstance;
		}

		public long getCreateTimestamp() {
			return createTimestamp;
		}
		public Object getEjbInstance() {
			return ejbInstance;
		}

		public boolean isExpired()
		{
			return (System.currentTimeMillis() - createTimestamp) > CACHE_LIFETIME_INSTANCE;
		}

		public boolean isPingRequired()
		{
			if (lastPingTimestamp == Long.MIN_VALUE)
				return false;

			return (System.currentTimeMillis() - lastPingTimestamp) > PING_PERIOD;
		}

		public void updatePingTimestamp()
		{
			lastPingTimestamp = System.currentTimeMillis();
		}

		public void deactivatePing() {
			lastPingTimestamp = Long.MIN_VALUE;
		}
	}

	private static Map<Class<?>, Map<Hashtable<?, ?>, EjbInstanceWrapper>> ejbInterface2Environment2Instance = Collections.synchronizedMap(
		new HashMap<Class<?>, Map<Hashtable<?,?>,EjbInstanceWrapper>>()
	);

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
	 * @param ejbRemoteInterface the remote EJB-business-interface (e.g. {@link JFireSecurityManagerRemote}) for which to get an instance.
	 * @param environment the environment (aka. JNDI initial context properties) specifying the coordinates where to get the bean from (e.g. which server to connect to, which user to authenticate as etc.).
	 * @return an instance of an EJB-proxy implementing the interface specified by the argument <code>ejbRemoteInterface</code>.
	 */
	public static <T> T getRemoteBean(Class<T> ejbRemoteInterface, Hashtable<?, ?> environment)
	{
		try {
			synchronized (ejbRemoteInterface) {
				Map<Hashtable<?, ?>, EjbInstanceWrapper> environment2Instance = ejbInterface2Environment2Instance.get(ejbRemoteInterface);
				if (environment2Instance == null) {
					environment2Instance = Collections.synchronizedMap(new HashMap<Hashtable<?,?>, EjbInstanceWrapper>());
					ejbInterface2Environment2Instance.put(ejbRemoteInterface, environment2Instance);
				}

				EjbInstanceWrapper instanceWrapper = environment2Instance.get(environment);
				if (instanceWrapper != null) {
					if (instanceWrapper.isExpired())
						instanceWrapper = null;
				}

				if (instanceWrapper != null) {
					if (instanceWrapper.isPingRequired()) {
						Object ejb = instanceWrapper.getEjbInstance();
						Method pingMethod;
						try {
							pingMethod = ejb.getClass().getMethod("ping", new Class[] { String.class });
						} catch (NoSuchMethodException x) {
							instanceWrapper.deactivatePing();
							pingMethod = null; // not supported
						}
						if (pingMethod != null) {
							try {
								pingMethod.invoke(ejb, new Object[] { null });
								instanceWrapper.updatePingTimestamp();
							} catch (Throwable t) {
								instanceWrapper = null;
							}
						}
					} // if (instanceWrapper.isPingRequired()) {
				} // if (instanceWrapper != null) {

				if (instanceWrapper == null) {
					T ejbInstance = lookupEjbByRemoteInterface(ejbRemoteInterface, environment);
					instanceWrapper = new EjbInstanceWrapper(ejbInstance);
					environment2Instance.put(environment, instanceWrapper);
				}

				return ejbRemoteInterface.cast(instanceWrapper.getEjbInstance());
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public static final String JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE = InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE;
	public static final String JNDI_PREFIX_EJB_BY_LOCAL_INTERFACE = InvokeUtil.JNDI_PREFIX_EJB_BY_LOCAL_INTERFACE;

	private static <T> T lookupEjbByRemoteInterface(Class<T> ejbRemoteInterface, Hashtable<?, ?> environment)
	throws Exception
	{
		String jndiName = JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + ejbRemoteInterface.getName();

		javax.naming.InitialContext initialContext = new javax.naming.InitialContext(environment);
		try {
			Object objRef = initialContext.lookup(jndiName);
			return ejbRemoteInterface.cast(objRef);
		} finally {
			initialContext.close();
		}
	}

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
	 * @param ejbLocalInterface the local EJB-business-interface (e.g. {@link TimerManagerLocal}) for which to get an instance.
	 * @return an instance of an EJB-proxy implementing the interface specified by the argument <code>ejbLocalInterface</code>.
	 */
	public static <T> T getLocalBean(Class<T> ejbLocalInterface)
	{
		// Unfortunately, accessing local EJBs is even less portable across JavaEE servers than accessing remote
		// EJBs. In JBoss, the local EJBs are registered in JNDI and we expect them to be unified at the location
		try {
			InitialContext initialContext = new InitialContext();
			try {
				return ejbLocalInterface.cast(initialContext.lookup(JNDI_PREFIX_EJB_BY_LOCAL_INTERFACE + ejbLocalInterface.getName()));
			} finally {
				initialContext.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}
