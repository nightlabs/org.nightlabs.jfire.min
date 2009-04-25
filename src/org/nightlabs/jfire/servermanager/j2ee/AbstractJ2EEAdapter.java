package org.nightlabs.jfire.servermanager.j2ee;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.security.RolesAllowed;


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
}
