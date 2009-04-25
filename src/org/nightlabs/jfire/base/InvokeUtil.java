package org.nightlabs.jfire.base;

import java.util.Properties;

import javax.naming.Context;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerImpl;
import org.nightlabs.util.ParameterMap;

public class InvokeUtil
{
	public static final String JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE = "ejb/byRemoteInterface/";
	public static final String JNDI_PREFIX_EJB_BY_LOCAL_INTERFACE = "ejb/byLocalInterface/";

	public static Properties getInitialContextProperties(
			String initialContextFactory, String initialContextURL,
			String organisationID, String userID, String password)
	{
		if ((User.USER_ID_PREFIX_TYPE_ORGANISATION + organisationID).equals(userID)) // we login to ourselves - no properties
			return null;

//		String username = userID + User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + organisationID;
		String username = (
				userID
				+ User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID
				+ organisationID
				+ User.SEPARATOR_BETWEEN_ORGANISATION_ID_AND_REST
				+ JFireServerManagerImpl.LOGIN_PARAM_ALLOW_EARLY_LOGIN
				+ ParameterMap.DEFAULT_KEY_VALUE_SEPARATOR
				+ "true"
//				+ ParameterMap.DEFAULT_ENTRY_SEPARATOR
		);
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		props.put(Context.PROVIDER_URL, initialContextURL);
		props.put(Context.SECURITY_PRINCIPAL, username);
		props.put(Context.SECURITY_CREDENTIALS, password);
		props.put(Context.SECURITY_PROTOCOL, "jfire");

//		props.setProperty("jnp.multi-threaded", String.valueOf(true));
//		props.setProperty("jnp.restoreLoginIdentity", String.valueOf(true));

		return props;
	}

	public static Properties getInitialContextProperties(
			JFireServerManagerFactory jFireServerManagerFactory,
			ServerCf serverCf, String organisationID, String userID, String password)
	{
		return getInitialContextProperties(
				jFireServerManagerFactory.getInitialContextFactory(serverCf.getJ2eeServerType(), true),
				serverCf.getInitialContextURL(),
				organisationID, userID, password);
////		String username = User.USER_ID_SYSTEM + '@' + organisationID;
//		String username = userID + '@' + organisationID;
//		Properties props = new Properties();
//		String initialContextFactory = jFireServerManagerFactory.getInitialContextFactory(serverCf.getJ2eeServerType(), true);
//		props.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
//		props.put(InitialContext.PROVIDER_URL, serverCf.getInitialContextURL());
//		props.put(InitialContext.SECURITY_PRINCIPAL, username);
//		props.put(InitialContext.SECURITY_CREDENTIALS, password);
//		props.put(InitialContext.SECURITY_PROTOCOL, "jfire");
//		return props;
	}

//	public static Object createBean(InitialContext initialContext, String bean)
//	throws NamingException, SecurityException, NoSuchMethodException,
//		IllegalArgumentException, IllegalAccessException, InvocationTargetException
//	{
//		Object homeRef = initialContext.lookup(bean);
//		Method homeCreate = homeRef.getClass().getMethod("create", (Class[]) null);
//		return homeCreate.invoke(homeRef, (Object[]) null);
//	}
//
//	public static void removeBean(Object bean)
//	{
//		try {
//			if (bean instanceof EJBObject)
//				((EJBObject)bean).remove();
//
//			if (bean instanceof EJBLocalObject)
//				((EJBLocalObject)bean).remove();
//		} catch (Exception x) {
//			Logger.getLogger(InvokeUtil.class).warn("removeBean: Could not remove bean!", x);
//		}
//	}
}
