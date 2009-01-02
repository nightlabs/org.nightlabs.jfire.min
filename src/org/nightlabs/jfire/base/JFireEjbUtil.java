package org.nightlabs.jfire.base;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class JFireEjbUtil
{
	private static final long CACHE_LIFETIME_HOME = 2L * 60L * 60L * 1000L; // keep EJB homes for 2 hours
	private static final long CACHE_LIFETIME_INSTANCE = 3L * 60L * 1000L; // keep EJB proxies for 3 minutes

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
		private Object ejbInstance;

		public EjbInstanceWrapper(Object ejbInstance) {
			this.createTimestamp = System.currentTimeMillis();
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
	}

	private static Map<Class<?>, Map<Hashtable<?, ?>, EjbHomeWrapper>> ejbInterface2Environment2Home = Collections.synchronizedMap(
			new HashMap<Class<?>, Map<Hashtable<?,?>,EjbHomeWrapper>>()
	);

	private static Map<Class<?>, Map<Hashtable<?, ?>, EjbInstanceWrapper>> ejbInterface2Environment2Instance = Collections.synchronizedMap(
		new HashMap<Class<?>, Map<Hashtable<?,?>,EjbInstanceWrapper>>()
	);

	@SuppressWarnings("unchecked")
	private static <T> T castEjbInstance(Object ejbInstance)
	{
		return (T) ejbInstance;
	}

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
					if (!instanceWrapper.isExpired())
						return castEjbInstance(instanceWrapper.getEjbInstance());
				}

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

					homeWrapper = new EjbHomeWrapper(home);
					environment2Home.put(environment, homeWrapper);
				}

				Object homeRef = homeWrapper.getEjbHome();
				Method homeCreate = homeRef.getClass().getMethod("create", (Class[]) null);
				Object ejbInstance = homeCreate.invoke(homeRef, (Object[]) null);

				instanceWrapper = new EjbInstanceWrapper(ejbInstance);
				environment2Instance.put(environment, instanceWrapper);

				return castEjbInstance(ejbInstance);
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

}