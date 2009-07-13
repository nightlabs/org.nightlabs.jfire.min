package org.nightlabs.jfire.editlock;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.editlock.id.EditLockTypeID"
 *		detachable="true"
 *		table="JFireBase_EditLockType"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, editLockTypeID"
 */
@PersistenceCapable(
	objectIdClass=EditLockTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_EditLockType")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class EditLockType
implements Serializable
{
	private static final long serialVersionUID = 1L;

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
	private String editLockTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long editLockExpiryClientLostMSec;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long editLockExpiryUserInactivityMSec;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EditLockType() { }

	public EditLockType(EditLockTypeID editLockTypeID)
	{
		this(editLockTypeID.organisationID, editLockTypeID.editLockTypeID);
	}
	public EditLockType(String organisationID, String editLockTypeID)
	{
		ObjectIDUtil.assertValidIDString(organisationID);
		ObjectIDUtil.assertValidIDString(editLockTypeID);
		this.organisationID = organisationID;
		this.editLockTypeID = editLockTypeID;
		this.editLockExpiryClientLostMSec = 1000L * 60L * 15L; // 15 minutes
		this.editLockExpiryUserInactivityMSec = 1000L * 60L * 30L; // 30 minutes
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getEditLockTypeID()
	{
		return editLockTypeID;
	}

	/**
	 * It might happen that a client simply disappears. For example because of an interruption of the power supply
	 * or a network problem. In order to find out, whether a client disappeared, a {@link EditLock} needs to be
	 * refreshed periodically. If the client does not reacquire a <code>EditLock</code> after the time defined
	 * by this property, it will be released.
	 *
	 * @return the time in milliseconds after which the editLock will be release if not re-acquired.
	 */
	public long getEditLockExpiryClientLostMSec()
	{
		return editLockExpiryClientLostMSec;
	}
	/**
	 * @param editLockExpiryClientLostMSec the new value
	 * @see #getEditLockExpiryClientLostMSec()
	 */
	public void setEditLockExpiryClientLostMSec(long editLockExpiryClientLostMSec)
	{
		this.editLockExpiryClientLostMSec = editLockExpiryClientLostMSec;
	}

	/**
	 * Depending on the use-case - for example if there's an exclusive lock existing additionally to the {@link EditLock} -
	 * you might want to ensure that a user who left his workstation does not block other people. Hence, you can define
	 * after what time an inactive user will be "kicked out".
	 * <p>
	 * This is a client functionality which is implemented solely in the client. After the client didn't refresh its <code>EditLock</code>
	 * locally for the time specified by this property, the user should receive a message. If the user doesn't react (i.e. click a button)
	 * within a certain time (e.g. 30 sec), the client should release the <code>EditLock</code> (and close the related UI or make it read-only).
	 * </p>
	 *
	 * @return the time in milliseconds in which a user should either do sth or release the <code>EditLock</code>.
	 */
	public long getEditLockExpiryUserInactivityMSec()
	{
		return editLockExpiryUserInactivityMSec;
	}
	public void setEditLockExpiryUserInactivityMSec(
			long editLockExpiryUserInactivityMSec)
	{
		this.editLockExpiryUserInactivityMSec = editLockExpiryUserInactivityMSec;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof EditLockType)) return false;
		EditLockType o = (EditLockType) obj;
		return Util.equals(o.organisationID, this.organisationID) && Util.equals(o.editLockTypeID, this.editLockTypeID);
	}
	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(editLockTypeID);
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of EditLockType is currently not persistent! Cannot obtain PersistenceManager!");

		return pm;
	}

	/**
	 * This is a callback which is triggered by
	 * {@link EditLock#acquireEditLock(PersistenceManager, org.nightlabs.jfire.security.id.UserID, String, EditLockTypeID, org.nightlabs.jdo.ObjectID, String)}
	 * every time an <code>EditLock</code> is acquired.
	 * You can override this method in your sub-class of {@link EditLockType}. The default implementation in {@link EditLockType} is empty.
	 * @param acquireEditLockResult TODO
	 * @param refresh <code>false</code> if the acquisition is the first, <code>true</code> if the user refreshs it (and it already existed before).
	 */
	public void onAcquireEditLock(AcquireEditLockResult acquireEditLockResult, boolean refresh)
	{
	}

	/**
	 * This is a callback which is triggered every time an <code>EditLock</code> is released. You can override this method
	 * in your sub-class of {@link EditLockType}. The default implementation in {@link EditLockType} is empty.
	 *
	 * @param editLock the editLock that is being released (and thus deleted after this method has finished)
	 * @param releaseReason why is <code>editLock</code> released.
	 */
	public void onReleaseEditLock(EditLock editLock, ReleaseReason releaseReason)
	{
	}
}
