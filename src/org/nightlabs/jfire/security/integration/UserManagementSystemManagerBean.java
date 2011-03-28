package org.nightlabs.jfire.security.integration;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJB for generic operations with UserManagementSystems.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless(
		mappedName="jfire/ejb/JFireBaseBean/UserManagementSystemManager", 
		name="jfire/ejb/JFireBaseBean/UserManagementSystemManager"
			)
public class UserManagementSystemManagerBean extends BaseSessionBeanImpl implements UserManagementSystemManagerRemote{
	
	private static final Logger logger = LoggerFactory.getLogger(UserManagementSystemManagerBean.class);

	
	private static CreateLifecycleListener createListener = new ForbidUserCreationLyfecycleListener(); 
	
	public static class ForbidUserCreationLyfecycleListener implements CreateLifecycleListener{

		private static ThreadLocal<Boolean> isEnabledTL = new ThreadLocal<Boolean>(){
			protected Boolean initialValue() {
				return true;
			};
		};

		/**
		 * Enable/disable this listener. If it's disabled than it will not throw an exception when new User is created.
		 * 
		 * @param isEnabled
		 */
		public static void setEnabled(boolean isEnabled) {
			isEnabledTL.set(isEnabled);
		}
		
		/**
		 * 
		 * @return if this listener is enabled and will throw an exception if new User is created
		 */
		public static boolean isEnabled(){
			return isEnabledTL.get();
		}

		@Override
		public void postCreate(InstanceLifecycleEvent event) {
			
			if (!isEnabled()){
				return;
			}
			
			PersistenceManager pm = null;
			try{
				pm = JDOHelper.getPersistenceManager(event.getPersistentInstance());
			}catch(Exception e){
				logger.error("Can't get PersistenceManager!", e);
			}
			if (pm != null){
				Collection<UserManagementSystem> leadingSystems = UserManagementSystem.getUserManagementSystemsByLeading(
						pm, true, UserManagementSystem.class
						);
				if (!leadingSystems.isEmpty()){	// forbid User creation
					logger.info("Forbidding new User objects creation because at least one leading UserManagementSystem exists.");
					throw new JDOFatalUserException("Creation of a new User is forbidden because at least one leading UserManagementSystem exists!");
				}
			}
		}
		
	};

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() {
		
		final PersistenceManager pm = createPersistenceManager();

		try{
			
			// add JDO lifecycle listener to User object which forbids creation of a new User object
			// by throwing and exception if at least one leading UserManagementSystem exists
			pm.getPersistenceManagerFactory().addInstanceLifecycleListener(
					createListener, new Class[]{User.class}
					);
			
		}finally{
			pm.close();
		}
		
	}

}
