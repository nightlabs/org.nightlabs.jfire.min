package org.nightlabs.jfire.jdo.notification.persistent;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationReceiverID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Inheritance;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jdo.notification.persistent.id.NotificationReceiverID"
 *		detachable="true"
 *		table="JFireBase_NotificationReceiver"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, subscriberType, subscriberID, subscriptionID"
 */
@PersistenceCapable(
	objectIdClass=NotificationReceiverID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_NotificationReceiver")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class NotificationReceiver
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String subscriberType;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String subscriberID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String subscriptionID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected NotificationReceiver() { }

	/**
	 * This is a convenience constructor which simply passes the primary key fields in the correct order
	 * to {@link #NotificationReceiver(String, String, String, String)}.
	 */
	public NotificationReceiver(NotificationFilter notificationFilter)
	{
		this(
				notificationFilter.getOrganisationID(),
				notificationFilter.getSubscriberType(),
				notificationFilter.getSubscriberID(),
				notificationFilter.getSubscriptionID());
	}

	public NotificationReceiver(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
	{
		this.organisationID = organisationID;
		this.subscriberType = subscriberType;
		this.subscriberID = subscriberID;
		this.subscriptionID = subscriptionID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
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

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager! This instance seems to be detached (or otherwise not persistent)!");

		return pm;
	}

	public abstract void onReceiveNotificationBundle(NotificationBundle notificationBundle)
	throws Exception;
}
