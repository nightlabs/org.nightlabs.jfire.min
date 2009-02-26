package org.nightlabs.jfire.base;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.nightlabs.jfire.security.JFireSecurityManager;
import org.nightlabs.jfire.security.JFireSecurityManagerHome;

/**
 * Util class for obtaining instances of stateless JavaEE session beans (EJBs).
 * Use the method {@link #getBean(Class, Hashtable)} to obtain EJB proxies for
 * your stateless EJBs.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class JFireEjbFactory
{
	private static final long CACHE_LIFETIME_HOME = 2L * 60L * 60L * 1000L; // keep EJB homes for 2 hours
	private static final long CACHE_LIFETIME_INSTANCE = 30L * 60L * 1000L; // keep EJB proxies for 30 minutes
	private static final long PING_PERIOD = 30L * 1000L; // execute a ping whenever the EJB proxy is older than that time or the last ping is longer ago that this.

	private static class EjbHomeWrapper
	{
		private long createTimestamp;
		private Object ejbHome;

		public EjbHomeWrapper(Object ejbHome) {
			this.createTimestamp = System.currentTimeMillis();
			this.ejbHome = ejbHome;
		}

		public long getCreateTimestamp() {
			return createTimestamp;
		}
		public Object getEjbHome() {
			return ejbHome;
		}

		public boolean isExpired()
		{
			return (System.currentTimeMillis() - createTimestamp) > CACHE_LIFETIME_HOME;
		}
	}

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

	private static Map<Class<?>, Map<Hashtable<?, ?>, EjbHomeWrapper>> ejbInterface2Environment2Home = Collections.synchronizedMap(
			new HashMap<Class<?>, Map<Hashtable<?,?>,EjbHomeWrapper>>()
	);

	private static Map<Class<?>, Map<Hashtable<?, ?>, EjbInstanceWrapper>> ejbInterface2Environment2Instance = Collections.synchronizedMap(
		new HashMap<Class<?>, Map<Hashtable<?,?>,EjbInstanceWrapper>>()
	);

	/**
	 * Get an instance of an Enterprise Java Bean. This method returns the same EJB-proxy
	 * when called multiple times with the same EJB-interface, because it caches the instances
	 * (with the arguments of this method used as key).
	 * <p>
	 * <b>Important:</b> Though this method works with stateful session beans,
	 * you <b>must not</b> use it for them, because the result (multiple threads will use the same proxy)
	 * might lead to arbitrary errors. Since JFire uses only stateless session beans by convention, it is safe
	 * to use this util method with all JFire EJBs.
	 * </p>
	 * <p>
	 * This method first generates the name of the EJB's home interface by appending the suffix <code>"Home"</code> to the name
	 * of the specified <code>ejbInterface</code>. The EJB's home interface is then loaded with this generated name
	 * via {@link Class#forName(String)}. In order to find out the JNDI name of the bean, a static field named <code>"JNDI_NAME"</code>
	 * must exist with the type {@link String} (see {@link JFireSecurityManagerHome#JNDI_NAME} as example).
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
	 * @param <T> the type of the EJB-interface.
	 * @param ejbInterface the EJB-interface (e.g. {@link JFireSecurityManager}) for which to get an instance.
	 * @param environment the environment (aka. JNDI initial context properties) specifying the coordinates where to get the bean from (e.g. which server to connect to, which user to authenticate as etc.).
	 * @return an instance of an EJB-proxy implementing the interface specified by the argument <code>ejbInterface</code>.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> ejbInterface, Hashtable<?, ?> environment)
	{
		try {
			synchronized (ejbInterface) {
				Map<Hashtable<?, ?>, EjbInstanceWrapper> environment2Instance = ejbInterface2Environment2Instance.get(ejbInterface);
				if (environment2Instance == null) {
					environment2Instance = Collections.synchronizedMap(new HashMap<Hashtable<?,?>, EjbInstanceWrapper>());
					ejbInterface2Environment2Instance.put(ejbInterface, environment2Instance);
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
					Map<Hashtable<?, ?>, EjbHomeWrapper> environment2Home = ejbInterface2Environment2Home.get(ejbInterface);
					if (environment2Home == null) {
						environment2Home = Collections.synchronizedMap(new HashMap<Hashtable<?,?>, EjbHomeWrapper>());
						ejbInterface2Environment2Home.put(ejbInterface, environment2Home);
					}

					EjbHomeWrapper homeWrapper = environment2Home.get(environment);
					if (homeWrapper != null) {
						if (homeWrapper.isExpired())
							homeWrapper = null;
					}

					if (homeWrapper == null) {
						Object home = lookupHome(ejbInterface, environment);
						homeWrapper = new EjbHomeWrapper(home);
						environment2Home.put(environment, homeWrapper);
					}

					Object ejbInstance;
					try {
						Object homeRef = homeWrapper.getEjbHome();
						Method homeCreate = homeRef.getClass().getMethod("create", (Class[]) null);
						ejbInstance = homeCreate.invoke(homeRef, (Object[]) null);
					} catch (Exception x) {
						ejbInstance = null; // maybe the old home somehow broke - ignore and try it again
					}

					if (ejbInstance == null) { // try it again
						Object homeRef = lookupHome(ejbInterface, environment);
						homeWrapper = new EjbHomeWrapper(homeRef);
						environment2Home.put(environment, homeWrapper);
						Method homeCreate = homeRef.getClass().getMethod("create", (Class[]) null);
						ejbInstance = homeCreate.invoke(homeRef, (Object[]) null);
					}

					instanceWrapper = new EjbInstanceWrapper(ejbInstance);
					environment2Instance.put(environment, instanceWrapper);
				}

				return (T) instanceWrapper.getEjbInstance();
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	private static <T> Object lookupHome(Class<T> ejbInterface, Hashtable<?, ?> environment)
	throws Exception
	{
		String homeClassName = ejbInterface.getName() + "Home";
		Class<?> homeClass = Class.forName(homeClassName);
		String jndiName = (String) homeClass.getField("JNDI_NAME").get(null);

		Object home;
		javax.naming.InitialContext initialContext = new javax.naming.InitialContext(environment);
		try {
			Object objRef = initialContext.lookup(jndiName);
			// only narrow if necessary
			if (java.rmi.Remote.class.isAssignableFrom(homeClass))
				home = javax.rmi.PortableRemoteObject.narrow(objRef, homeClass);
			else
				home = objRef;
		} finally {
			initialContext.close();
		}
		return home;
	}

}
