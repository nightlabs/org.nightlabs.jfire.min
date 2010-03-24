package org.nightlabs.initialisejdo;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.util.reflect.ReflectUtil;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class InitialiseJDOBean
extends BaseSessionBeanImpl
implements InitialiseJDORemote
{
	private static final Logger logger = Logger.getLogger(InitialiseJDOBean.class);

	@RolesAllowed("_System_")
	@Override
	public void initialise()
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			// added 2009-12-07 for PostgreSQL (complete initialisation of all meta-data)
			Set<Class<?>> classesInPackage = new HashSet<Class<?>>();
			Collection<Class<?>> c;
			c = ReflectUtil.listClassesInPackage("org.nightlabs.jfire", true);
			classesInPackage.addAll(c);

			for (Class<?> clazz : classesInPackage) {
				boolean isPersistenceCapable = false;
				Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
				for (Annotation annotation : declaredAnnotations) {
					if (javax.jdo.annotations.PersistenceCapable.class == annotation.annotationType())
						isPersistenceCapable = true;
				}

				if (!isPersistenceCapable) {
					if(logger.isDebugEnabled())
						logger.debug("initialise: Ignoring non-persistence-capable class: " + clazz.getName());

					continue;
				}

				if(logger.isDebugEnabled())
					logger.debug("initialise: Initializing meta-data for class: " + clazz.getName());

				try {
					pm.getExtent(clazz);
				} catch (Exception x) {
					logger.warn("initialise: Initializing meta-data for class \"" + clazz.getName() + "\" failed: " + x.getClass().getName() + ": " + x.getMessage(), x);
				}
			}
			// END added 2009-12-07 for PostgreSQL (complete initialisation of all meta-data)
		} finally {
			pm.close();
		}
	}

}
