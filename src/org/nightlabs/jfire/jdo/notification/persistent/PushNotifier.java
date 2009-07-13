package org.nightlabs.jfire.jdo.notification.persistent;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.jdo.notification.persistent.id.PushNotifierID;


/**
 * For every <code>subscriberType</code>, there can exist zero or one <code>PushNotifier</code>
 * in order to actively notify the subscriber.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jdo.notification.persistent.id.PushNotifierID"
 *		detachable="true"
 *		table="JFireBase_PushNotifier"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 */
@PersistenceCapable(
	objectIdClass=PushNotifierID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_PushNotifier")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class PushNotifier
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String subscriberType;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PushNotifier() { }

	public PushNotifier(String subscriberType) {
		this.subscriberType = subscriberType;
	}

	public String getSubscriberType()
	{
		return subscriberType;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager! This instance seems to be detached (or otherwise not persistent)!");

		return pm;
	}

	public abstract void push(NotificationBundle notificationBundle) // NotificationFilter subscription, Collection<DirtyObjectID> dirtyObjectIDs)
	throws Exception;
}
