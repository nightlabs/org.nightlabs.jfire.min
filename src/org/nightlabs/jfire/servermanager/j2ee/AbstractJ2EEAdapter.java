package org.nightlabs.jfire.servermanager.j2ee;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;


public abstract class AbstractJ2EEAdapter implements J2EEAdapter
{
	private static final long serialVersionUID = 1L;

	protected abstract Set<Class<?>> getAllEjb3Roles_getAllEjb3Classes() throws Exception;

	private void collectMethods(Collection<Method> methods, Class<?> clazz, Set<Class<?>> processedClasses)
	{
		if (!processedClasses.add(clazz)) // prevent processing the same interface twice
			return;

		while (clazz != null) {
			for (Method method : clazz.getDeclaredMethods())
				methods.add(method);

			for (Class<?> iface : clazz.getInterfaces())
				collectMethods(methods, iface, processedClasses);

			clazz = clazz.getSuperclass();
		}
	}

	@Override
	public Set<String> getAllEjb3Roles()
	throws J2EEAdapterException
	{
		try {
			Set<String> result = new HashSet<String>();

			for (Class<?> ejb3Class : getAllEjb3Roles_getAllEjb3Classes()) {
				for(Annotation annotation : ejb3Class.getAnnotations()) {
					if (annotation instanceof RolesAllowed) {
						RolesAllowed rolesAllowedAnnotation = (RolesAllowed) annotation;
						for (String role : rolesAllowedAnnotation.value())
							result.add(role);
					}
				}

				Collection<Method> methods = new LinkedList<Method>();
				collectMethods(methods, ejb3Class, new HashSet<Class<?>>());

				for (Method method : methods) {
					for (Annotation annotation : method.getAnnotations()) {
						if (annotation instanceof RolesAllowed) {
							RolesAllowed rolesAllowedAnnotation = (RolesAllowed) annotation;
							for (String role : rolesAllowedAnnotation.value())
								result.add(role);
						}
					}
				}
			}

			return result;
		} catch (J2EEAdapterException x) {
			throw x;
		} catch (Exception x) {
			throw new J2EEAdapterException(x);
		}
	}

	protected static class AuthCallbackHandler implements CallbackHandler
	{
		private String userName;
		private char[] password;

		public AuthCallbackHandler(String userName, char[] password) {
			this.userName = userName;
			this.password = password;
		}

		@Override
		public void handle(Callback[] callbacks)
		throws IOException,
		UnsupportedCallbackException
		{
			for (int i = 0; i < callbacks.length; ++i) {
				Callback cb = callbacks[i];
				if (cb instanceof NameCallback) {
					((NameCallback)cb).setName(userName);
				}
				else if (cb instanceof PasswordCallback) {
					((PasswordCallback)cb).setPassword(password);
				}
				else throw new UnsupportedCallbackException(cb);
			}
		}
	}

//	@Override
//	public LoginContext jms_createLoginContext()
//	throws J2EEAdapterException
//	{
//		try {
//			InitialContext localContext = new InitialContext();
//			JFireServerLocalLoginManager m = JFireServerLocalLoginManager.getJFireServerLocalLoginManager(localContext);
//
//			AuthCallbackHandler mqCallbackHandler = new AuthCallbackHandler(
//					JFireServerLocalLoginManager.PRINCIPAL_LOCALQUEUEWRITER,
//					m.getPrincipal(JFireServerLocalLoginManager.PRINCIPAL_LOCALQUEUEWRITER).getPassword().toCharArray());
//
//			LoginContext loginContext = new LoginContext("jfireLocal", mqCallbackHandler);
//			return loginContext;
//		} catch (NamingException x) {
//			throw new J2EEAdapterException(x);
//		} catch (LoginException x) {
//			throw new J2EEAdapterException(x);
//		}
//	}
}
