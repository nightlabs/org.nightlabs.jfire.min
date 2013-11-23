package org.nightlabs.jfire.security.notification;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.AuthorizedObjectRefID;
import org.nightlabs.jfire.security.id.UserLocalID;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable identity-type="application" detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class AuthorityNotificationFilter extends NotificationFilter
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AuthorityNotificationFilter.class);

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected AuthorityNotificationFilter() { }

	public AuthorityNotificationFilter(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
		getLifecycleStates().add(JDOLifecycleState.NEW);
		getLifecycleStates().add(JDOLifecycleState.DIRTY);
		getLifecycleStates().add(JDOLifecycleState.DELETED);
		getCandidateClasses().add(Authority.class.getName());
		getCandidateClasses().add(AuthorizedObjectRef.class.getName());
		setIncludeSubclasses(false); // not to include subclasses is faster - and neither Authority nor AuthorizedObjectRef should be subclassed.
	}

	@Override
	public Collection<DirtyObjectID> filter(List<DirtyObjectID> dirtyObjectIDs) {
		String subscriberUserID = null;

		for (Iterator<DirtyObjectID> it = dirtyObjectIDs.iterator(); it.hasNext(); ) {
			DirtyObjectID dirtyObjectID = it.next();
			if (Authority.class.getName().equals(dirtyObjectID.getObjectClassName())) {
				AuthorityID authorityID = (AuthorityID) dirtyObjectID.getObjectID();
				if (!getOrganisationID().equals(authorityID.organisationID))
					it.remove(); // only local ones of this organisation! => remove the foreign one

				continue; // we need to notify about *ALL* Authority changes - but only local ones of this organisation
			}

			if (AuthorizedObjectRef.class.getName().equals(dirtyObjectID.getObjectClassName())) {
				AuthorizedObjectRefID authorizedObjectRefID = (AuthorizedObjectRefID) dirtyObjectID.getObjectID();
				AuthorizedObjectID authorizedObjectID = (AuthorizedObjectID) ObjectIDUtil.createObjectID(authorizedObjectRefID.authorizedObjectID);
				if (!(authorizedObjectID instanceof UserLocalID)) {
					it.remove(); // we only synchronize UserLocal - no groups!
					continue;
				}

				// we only notify about changes that affect the subscriber (i.e. its user)
				if (subscriberUserID == null)
					subscriberUserID = User.USER_ID_PREFIX_TYPE_ORGANISATION + getSubscriberID();

				UserLocalID userLocalID = (UserLocalID) authorizedObjectID;
				if (!(getOrganisationID().equals(userLocalID.organisationID) && subscriberUserID.equals(userLocalID.userID))) {
					it.remove(); // not affecting the subscriber
					continue;
				}
			}
			else
				throw new IllegalStateException("What the fuck! dirtyObjectID.getObjectClassName()=" + dirtyObjectID.getObjectClassName());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("filter: dirtyObjectIDs.size=" + dirtyObjectIDs.size());
			for (DirtyObjectID dirtyObjectID : dirtyObjectIDs)
				logger.debug("  * " + dirtyObjectID);
		}

		return dirtyObjectIDs;
	}

}
