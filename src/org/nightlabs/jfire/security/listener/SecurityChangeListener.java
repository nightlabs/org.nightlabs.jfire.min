package org.nightlabs.jfire.security.listener;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.util.Util;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.security.listener.id.SecurityChangeListenerID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Discriminator;


/**
 * A listener that is notified whenever access rights are changed. Users of this listener
 * framework should subclass <code>SecurityChangeListener</code> and override
 * all appropriate callback-methods (the ones beginning with "on_", "pre_" and "post_").
 * Whenever a modification occurs, all persistent instances (the complete extent) are iterated
 * and triggered.
 * <p>
 * Note, that there might occur many modifications in the same transaction. In order to reduce
 * workload, you should track all changes (preferably in a non-persistent non-static field of your
 * subclass) until {@link #on_SecurityChangeController_endChanging()} is called. This marks the
 * end of such a series of modifications. It is recommended to - if possible - perform all expensive
 * work in an <a href="https://www.jfire.org/modules/phpwiki/index.php/Framework%20AsyncInvoke">asynchronous invocation</a>
 * that you spawn in your implementation of <code>on_SecurityChangeController_endChanging()</code>.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.listener.id.SecurityChangeListenerID"
 *		table="JFireBase_SecurityChangeListener"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, securityChangeListenerID"
 */
@PersistenceCapable(
	objectIdClass=SecurityChangeListenerID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_SecurityChangeListener")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class SecurityChangeListener
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
	private String securityChangeListenerID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SecurityChangeListener() { }

	public SecurityChangeListener(String organisationID, String securityChangeListenerID) {
		this.organisationID = organisationID;
		this.securityChangeListenerID = securityChangeListenerID;
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public String getSecurityChangeListenerID() {
		return securityChangeListenerID;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Could not get PersistenceManager from this: " + this);

		return pm;
	}


	/**
	 * Called when a series of modifications (usually performed in one transaction) is completed.
	 * Expensive operations (like recalculations of caches/indexes) should only be done for those
	 * entities that were affected by the modifications performed before. Additionally, it is recommended
	 * to perform all expensive work in an
	 * <a href="https://www.jfire.org/modules/phpwiki/index.php/Framework%20AsyncInvoke">asynchronous invocation</a>
	 * that you spawn in your implementation of this method.
	 */
	public void on_SecurityChangeController_endChanging()
	{
		// override to do sth.
	}

	/**
	 * Called when a new password is set to a {@link User} (i.e. via {@link UserLocal#setPasswordPlain(String)}).
	 * 
	 * @param event the event object containing some details.
	 */
	public void on_UserLocal_passwordChanged(SecurityChangeEvent_UserLocal_passwordChanged event)
	{
		// override to do sth.
	}

	/**
	 * Called before an {@link AuthorizedObjectRef} is created. Since it is not yet created,
	 * {@link SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef#getAuthorizedObjectRef()}
	 * returns <code>null</code>.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void pre_Authority_createAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event)
	{
		// override to do sth.
	}
	/**
	 * Called after an {@link AuthorizedObjectRef} was created. The new <code>AuthorizedObjectRef</code>
	 * can be obtained via
	 * {@link SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef#getAuthorizedObjectRef()}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void post_Authority_createAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event)
	{
		// override to do sth.
	}
	/**
	 * Called before an {@link AuthorizedObjectRef} is destroyed (and thus deleted from the datastore).
	 * The <code>AuthorizedObjectRef</code> about to be deleted can be obtained via
	 * {@link SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef#getAuthorizedObjectRef()}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void pre_Authority_destroyAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event)
	{
		// override to do sth.
	}
	/**
	 * Called after an {@link AuthorizedObjectRef} was destroyed (and deleted from the datastore).
	 * Since it is not existing anymore,
	 * {@link SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef#getAuthorizedObjectRef()}
	 * returns <code>null</code>.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void post_Authority_destroyAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event)
	{
		// override to do sth.
	}


	/**
	 * Called before a {@link Role} is added to a certain {@link AuthorizedObjectRef}.
	 * <p>
	 * Note, that adding roles with a {@link SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole#getDiffRefCount() diffRefCount}
	 * &lt; 0 is semantically equal to removing roles with a
	 * {@link SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole#getDiffRefCount() diffRefCount} &gt; 0.
	 * </p>
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void pre_AuthorizedObjectRef_addRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event)
	{
		// override to do sth.
	}
	/**
	 * Called after a {@link Role} was added to a certain {@link AuthorizedObjectRef}.
	 * <p>
	 * Note, that adding roles with a {@link SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole#getDiffRefCount() diffRefCount}
	 * &lt; 0 is semantically equal to removing roles with a
	 * {@link SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole#getDiffRefCount() diffRefCount} &gt; 0.
	 * </p>
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void post_AuthorizedObjectRef_addRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event)
	{
		// override to do sth.
	}
	/**
	 * Called before a {@link Role} is removed from a certain {@link AuthorizedObjectRef}.
	 * <p>
	 * Note, that adding roles with a {@link SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole#getDiffRefCount() diffRefCount}
	 * &lt; 0 is semantically equal to removing roles with a
	 * {@link SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole#getDiffRefCount() diffRefCount} &gt; 0.
	 * </p>
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void pre_AuthorizedObjectRef_removeRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event)
	{
		// override to do sth.
	}
	/**
	 * Called after a {@link Role} was removed from a certain {@link AuthorizedObjectRef}.
	 * <p>
	 * Note, that adding roles with a {@link SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole#getDiffRefCount() diffRefCount}
	 * &lt; 0 is semantically equal to removing roles with a
	 * {@link SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole#getDiffRefCount() diffRefCount} &gt; 0.
	 * </p>
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void post_AuthorizedObjectRef_removeRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event)
	{
		// override to do sth.
	}


	/**
	 * Called before a {@link RoleGroupRef} is added to an {@link AuthorizedObjectRef}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void pre_AuthorizedObjectRef_addRoleGroupRef(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef event)
	{
		// override to do sth.
	}
	/**
	 * Called after a {@link RoleGroupRef} was added to an {@link AuthorizedObjectRef}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void post_AuthorizedObjectRef_addRoleGroupRef(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef event)
	{
		// override to do sth.
	}
	/**
	 * Called before a {@link RoleGroupRef} is removed from an {@link AuthorizedObjectRef}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void pre_AuthorizedObjectRef_removeRoleGroupRef(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef event)
	{
		// override to do sth.
	}
	/**
	 * Called after a {@link RoleGroupRef} was removed from an {@link AuthorizedObjectRef}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void post_AuthorizedObjectRef_removeRoleGroupRef(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef event)
	{
		// override to do sth.
	}


	/**
	 * Called before a member (usually a {@link UserLocal}) is added to an {@link UserSecurityGroup}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void pre_UserSecurityGroup_addMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event)
	{
		// override to do sth.
	}
	/**
	 * Called after a member (usually a {@link UserLocal}) was added to an {@link UserSecurityGroup}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void post_UserSecurityGroup_addMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event)
	{
		// override to do sth.
	}
	/**
	 * Called before a member (usually a {@link UserLocal}) is removed from an {@link UserSecurityGroup}.
	 *
	 * @param event the event object containing some details.
	 */
	public void pre_UserSecurityGroup_removeMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event)
	{
		// override to do sth.
	}
	/**
	 * Called after a member (usually a {@link UserLocal}) was removed from an {@link UserSecurityGroup}.
	 * <p>
	 * Recommendation: In your implementation of this method, you should only register the current event and
	 * perform all resulting operations in {@link #on_SecurityChangeController_endChanging()}. This should
	 * be done after consolidating all events that happened so far; or by simply comparing some backup data (saved
	 * in your "pre_"-methods) with the new situation.
	 * This is especially important, because other modifications might occur within the same transaction
	 * (before <code>on_SecurityChangeController_endChanging()</code>) that reverse the current event
	 * (e.g. some lists might first be cleared and then re-populated with mostly the same objects).
	 * </p>
	 *
	 * @param event the event object containing some details.
	 */
	public void post_UserSecurityGroup_removeMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event)
	{
		// override to do sth.
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((securityChangeListenerID == null) ? 0 : securityChangeListenerID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final SecurityChangeListener other = (SecurityChangeListener) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.securityChangeListenerID, other.securityChangeListenerID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + securityChangeListenerID + ']';
	}
}
