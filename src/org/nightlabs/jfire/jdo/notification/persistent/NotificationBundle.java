package org.nightlabs.jfire.jdo.notification.persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationFilterID;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationBundleID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jdo.notification.persistent.id.NotificationBundleID"
 *		detachable="true"
 *		table="JFireBase_NotificationBundle"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, notificationBundleID"
 *
 * @jdo.query name="getNotificationBundlesForSubscription" query="SELECT
 *		WHERE
 *			this.organisationID == :organisationID &&
 *			this.subscriberType == :subscriberType &&
 *			this.subscriberID == :subscriberID &&
 *			this.subscriptionID == :subscriptionID
 *		ORDER BY this.notificationBundleID ASC"
 */
@PersistenceCapable(
	objectIdClass=NotificationBundleID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_NotificationBundle")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries(
	@javax.jdo.annotations.Query(
		name="getNotificationBundlesForSubscription",
		value="SELECT WHERE this.organisationID == :organisationID && this.subscriberType == :subscriberType && this.subscriberID == :subscriberID && this.subscriptionID == :subscriptionID ORDER BY this.notificationBundleID ASC")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class NotificationBundle
implements Serializable
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public static List<NotificationBundle> getNotificationBundles(PersistenceManager pm, NotificationFilterID notificationFilterID)
	{
		pm.getExtent(NotificationFilter.class);
//		NotificationFilter subscription;
//		try {
//			subscription = (NotificationFilter) pm.getObjectById(subscriptionID);
//		} catch (JDOObjectNotFoundException x) {
//			return new ArrayList<NotificationBundle>();
//		}
		HashMap params = new HashMap(4);
		params.put("organisationID", notificationFilterID.organisationID);
		Query q = pm.newNamedQuery(NotificationBundle.class, "getNotificationBundlesForSubscription");
		return (List<NotificationBundle>) q.executeWithMap(params);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true" value-strategy="native"
	 */
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long notificationBundleID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String subscriberType;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String subscriberID;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String subscriptionID;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private NotificationFilter notificationFilter;

	/**
	 * @jdo.field persistence-modifier="persistent" serialized="true" embedded="true"
	 */
@Persistent(
	serialized="true",
	persistenceModifier=PersistenceModifier.PERSISTENT,
	embedded="true")
	private Object dirtyObjectIDs;

	/**
	 * @deprecated
	 */
	@Deprecated
	protected NotificationBundle()
	{
	}

	public NotificationBundle(/* String organisationID, long notificationBundleID, */ NotificationFilter notificationFilter, Collection<DirtyObjectID> dirtyObjectIDs)
	{
//		this.organisationID = organisationID;
//		this.notificationBundleID = notificationBundleID;
//
//		if (!this.organisationID.equals(notificationFilter.getOrganisationID()))
//			throw new IllegalArgumentException("Organisation mismatch between NotificationBundle.organisationID ("+organisationID+") and NotificationFilter.organisationID ("+notificationFilter.getOrganisationID()+")!");

		this.organisationID = notificationFilter.getOrganisationID();
		this.subscriberType = notificationFilter.getSubscriberType();
		this.subscriberID = notificationFilter.getSubscriberID();
		this.subscriptionID = notificationFilter.getSubscriptionID();
//		this.notificationFilter = notificationFilter;
		this.dirtyObjectIDs = new ArrayList<DirtyObjectID>(dirtyObjectIDs); // it seems not more expensive than a call to "trimToSize()"
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getNotificationBundleID()
	{
		return notificationBundleID;
	}

//	public NotificationFilter getSubscription()
//	{
//		return notificationFilter;
//	}

	public String getSubscriberType()
	{
		return subscriberType;
	}
	public String getSubscriberID()
	{
		return subscriberID;
	}
	public String getSubscriptionID()
	{
		return subscriptionID;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<DirtyObjectID> getDirtyObjectIDs()
	{
		return (ArrayList<DirtyObjectID>) dirtyObjectIDs;
	}
}
