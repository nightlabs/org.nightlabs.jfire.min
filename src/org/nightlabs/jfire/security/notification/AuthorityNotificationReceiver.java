package org.nightlabs.jfire.security.notification;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.JFireSecurityManager;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorizedObjectRefID;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable identity-type="application" detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class AuthorityNotificationReceiver extends NotificationReceiver
{
	private static final Logger logger = Logger.getLogger(AuthorityNotificationReceiver.class);

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected AuthorityNotificationReceiver() { }

	public AuthorityNotificationReceiver(NotificationFilter notificationFilter) {
		super(notificationFilter);
	}

	public AuthorityNotificationReceiver(String organisationID, String subscriberType, String subscriberID, String subscriptionID) {
		super(organisationID, subscriberType, subscriberID, subscriptionID);
	}

	@Override
	public void onReceiveNotificationBundle(NotificationBundle notificationBundle)
	throws Exception
	{
		Set<AuthorityID> authorityIDs = null;
		Set<AuthorizedObjectRefID> authorizedObjectRefIDs = null;
		for (DirtyObjectID dirtyObjectID : notificationBundle.getDirtyObjectIDs()) {
			if (dirtyObjectID.getLifecycleState() == JDOLifecycleState.DELETED) {
				logger.error("onReceiveNotificationBundle: JDOLifecycleState.DELETED not yet supported!", new UnsupportedOperationException("JDOLifecycleState.DELETED not yet supported!"));
				continue;
			}

			if (Authority.class.getName().equals(dirtyObjectID.getObjectClassName())) {
				if (authorityIDs == null)
					authorityIDs = new HashSet<AuthorityID>();

				authorityIDs.add((AuthorityID) dirtyObjectID.getObjectID());
			}
			else if (AuthorizedObjectRef.class.getName().equals(dirtyObjectID.getObjectClassName())) {
				if (authorizedObjectRefIDs == null)
					authorizedObjectRefIDs = new HashSet<AuthorizedObjectRefID>();

				authorizedObjectRefIDs.add((AuthorizedObjectRefID) dirtyObjectID.getObjectID());
			}
			else
				throw new IllegalStateException("What the fuck! dirtyObjectID.getObjectClassName()=" + dirtyObjectID.getObjectClassName());
		}

		replicateAuthorities(notificationBundle.getOrganisationID(), authorityIDs, authorizedObjectRefIDs);
	}

	@SuppressWarnings("unchecked")
	public void replicateAuthorities(String emitterOrganisationID, Set<AuthorityID> authorityIDs, Set<AuthorizedObjectRefID> authorizedObjectRefIDs)
	throws RemoteException, CreateException, NamingException
	{
		JFireSecurityManager m = JFireEjbFactory.getBean(JFireSecurityManager.class, Lookup.getInitialContextProperties(getPersistenceManager(), emitterOrganisationID));
		Collection<Authority> authorities = m.getAuthoritiesSelfInformation(authorityIDs, authorizedObjectRefIDs);

		if (logger.isDebugEnabled()) {
			logger.debug("replicateAuthorities: replicating " + authorities.size() + " authorities from orga " + emitterOrganisationID + " to local orga " + getSubscriberID() + ":");

			for (Authority authority : authorities) {
				logger.debug("  * Authority " + authority.getOrganisationID() + '/' + authority.getAuthorityID());
				for (AuthorizedObjectRef authorizedObjectRef : authority.getAuthorizedObjectRefs())
					logger.debug("    o AuthorizedObjectRef.authorizedObject " + authorizedObjectRef.getAuthorizedObject());
			}
		}

		User.disableAttachUserLocalCheck(true);
		try {
			getPersistenceManager().makePersistentAll(authorities);
		} finally {
			User.disableAttachUserLocalCheck(false);
		}
	}

}
