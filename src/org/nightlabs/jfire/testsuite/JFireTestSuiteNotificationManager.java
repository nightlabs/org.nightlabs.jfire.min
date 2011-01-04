/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuiteNotification.NotificationType;

/**
 * This NotificationManager is used to store notifications for server-side run
 * tests. It is bound to JNDI and accessed from async running tests to store
 * notifications. Additionally clients poll those notifications via a bean
 * method.
 * 
 * @author alex
 */
public class JFireTestSuiteNotificationManager implements Serializable {

	private static final long serialVersionUID = 20101208L;

	class NotificationContainer implements Serializable {
		private static final long serialVersionUID = 20101208L;
		
		private String identifier;
		private boolean ended = false;
		private Class<?> testClass;
		private List<JFireTestSuiteNotification> notifications = new CopyOnWriteArrayList<JFireTestSuiteNotification>();
		
		public NotificationContainer(String identifier, Class<?> testClass) {
			this.identifier = identifier;
			this.testClass = testClass;
			notifications.add(new JFireTestSuiteNotification(testClass, null, NotificationType.notificationStarted, null));
		}
		
		public void waitForNotification() {
			if (!ended) {
				while (notifications.size() <= 0 && !ended) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}
		}
		
		public List<JFireTestSuiteNotification> pop() {
			ArrayList<JFireTestSuiteNotification> notificationsCopy = new ArrayList<JFireTestSuiteNotification>(notifications);
			notifications.clear();
			if (ended) {
				containers.remove(identifier);
			}
			return notificationsCopy;
		}

		public void addNotification(JFireTestSuiteNotification notification) {
			notifications.add(notification);
		}
		
		public void end() {
			notifications.add(new JFireTestSuiteNotification(testClass, null, NotificationType.notificationEnded, null));
			ended = true;
		}
	}
	
	private Map<String, NotificationContainer> containers = new HashMap<String, NotificationContainer>();
	
	/**
	 * 
	 */
	public JFireTestSuiteNotificationManager() {
	}
	
	public void startNotification(String identifier, Class<?> testClass) {
		synchronized (containers) {
			if (containers.containsValue(identifier)) {
				throw new IllegalStateException("A Notification with the given id already exists");
			}
			NotificationContainer container = new NotificationContainer(identifier, testClass);
			containers.put(identifier, container);
		}
	}
	
	public void addNotification(String identifier, JFireTestSuiteNotification notification) {
		synchronized (containers) {
			NotificationContainer container = containers.get(identifier);
			if (container == null) {
				throw new IllegalStateException("No Notification with the given id exists");
			}
			container.addNotification(notification);
		}
	}

	public List<JFireTestSuiteNotification> popNotifications(String identifier) {
		NotificationContainer container = containers.get(identifier);
		if (container == null) {
			throw new IllegalStateException("No Notification with the given id exists");
		}
		container.waitForNotification();
		synchronized (containers) {
			return container.pop();
		}
	}
	
	public void endNotification(String identifier, Class<?> testClass) {
		synchronized (containers) {
			NotificationContainer container = containers.get(identifier);
			if (container == null) {
				throw new IllegalStateException("No Notification with the given id exists");
			}
			container.end();
		}
	}
	
	protected static void bindToJNDI()
	{
		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		String jndiName = getJndiName(organisationID);
		try {
			InitialContext initialContext = new InitialContext();
			try {
				try {
					initialContext.createSubcontext(JNDI_PREFIX);
				} catch (NameAlreadyBoundException e) {
					// ignore
				}

				initialContext.bind(jndiName, new JFireTestSuiteNotificationManager());
			} finally {
				initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException("Could not bind JFireTestSuiteNotificationManager \""+jndiName+"\" into JNDI!", x);
		}
	}

	public static JFireTestSuiteNotificationManager getNotificationManager() {
		JFireTestSuiteNotificationManager manager = getNotificationManagerInternal();
		if (manager == null) {
			bindToJNDI();
			return getNotificationManagerInternal();
		} else {
			return manager;
		}
	}
	
	private static JFireTestSuiteNotificationManager getNotificationManagerInternal()
	{
		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		String jndiName = getJndiName(organisationID);
		try {
			InitialContext initialContext = new InitialContext();
			try {
				JFireTestSuiteNotificationManager manager = (JFireTestSuiteNotificationManager) initialContext.lookup(jndiName);
				if (manager == null)
					throw new IllegalStateException("JbpmConfigurationCarrier.jbpmConfiguration is null!");
				return manager;
			} finally {
//				initialContext.close(); // https://www.jfire.org/modules/bugs/view.php?id=1178
			}
		} catch (NamingException x) {
			return null;
		}
	}
	
	private static final String JNDI_PREFIX = "java:/jfire/JFireTestSuiteNotificationManager";

	private static String getJndiName(String organisationID)
	{
		return JNDI_PREFIX + '/' + organisationID;
	}
	
	
}
