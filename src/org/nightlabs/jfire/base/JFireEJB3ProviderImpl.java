package org.nightlabs.jfire.base;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JFireEJB3ProviderImpl implements JFireEJB3Provider {
	public static final String JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE = InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE;
	public static final String JNDI_PREFIX_EJB_BY_LOCAL_INTERFACE = InvokeUtil.JNDI_PREFIX_EJB_BY_LOCAL_INTERFACE;
	
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

	private Map<Class<?>, Map<Hashtable<?, ?>, EjbInstanceWrapper>> ejbInterface2Environment2Instance = Collections.synchronizedMap(
		new HashMap<Class<?>, Map<Hashtable<?,?>,EjbInstanceWrapper>>()
	);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.JFireEJB3Provider#getRemoteBean(java.lang.Class, java.util.Hashtable)
	 */
	@Override
	public <T> T getRemoteBean(Class<T> ejbRemoteInterface, Hashtable<?, ?> environment)
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

	/**
	 * Lookup the given EJB (specified by its remote-interface) in the JNDI.
	 * <p>
	 * Since I had strange class-loader-problems (failing to load class Proxy$123), I made this method
	 * synchronized from 2009-05-06 on.
	 * </p>
	 */
	private synchronized <T> T lookupEjbByRemoteInterface(Class<T> ejbRemoteInterface, Hashtable<?, ?> environment)
	throws Exception
	{
		String jndiName = JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + ejbRemoteInterface.getName();

		javax.naming.InitialContext initialContext = new javax.naming.InitialContext(environment);
		try {
			Object objRef = initialContext.lookup(jndiName);
			return ejbRemoteInterface.cast(objRef);
		} finally {
//			initialContext.close(); // https://www.jfire.org/modules/bugs/view.php?id=1178
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.JFireEJB3Provider#getLocalBean(java.lang.Class)
	 */
	@Override
	public <T> T getLocalBean(Class<T> ejbLocalInterface)
	{
		// Unfortunately, accessing local EJBs is even less portable across JavaEE servers than accessing remote
		// EJBs. In JBoss, the local EJBs are registered in JNDI and we expect them to be unified at the location
		try {
			InitialContext initialContext = new InitialContext();
			try {
				return ejbLocalInterface.cast(initialContext.lookup(JNDI_PREFIX_EJB_BY_LOCAL_INTERFACE + ejbLocalInterface.getName()));
			} finally {
//				initialContext.close(); // https://www.jfire.org/modules/bugs/view.php?id=1178
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}
