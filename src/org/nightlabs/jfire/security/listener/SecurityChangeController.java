package org.nightlabs.jfire.security.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserID;

/**
 * An instance of this class is used per thread in order to track modifications
 * of the security-related part of JFire's data model. In order to modify access
 * rights and similar (usually in your EJB), you have to call
 * {@link #beginChanging()} and (in a finally-block!) {@link #endChanging(boolean)} like this:
 * <p>
 * <pre>
 * public void myBeanMethod(...) {
 *		boolean successful = false;
 *		SecurityChangeController.beginChanging();
 *		try {
 *			// your code
 *			// ...
 *			// ...
 *			// ...
 *
 *			// Directly before the finally-block, we set successful to true, because
 *			// we only come here, if there was no exception thrown before.
 *			successful = true;
 *		} finally {
 *			SecurityChangeController.endChanging(successful);
 *		}
 * }
 * </pre>
 * </p>
 * <p>
 * Modifications to the security-related objects that occur outside the scope of such a demarcation
 * cause an {@link IllegalStateException}.
 * </p>
 * <p>
 * It is possible to nest multiple calls to <code>beginChanging()</code> and <code>endChanging()</code>, because
 * <code>SecurityChangeController</code> employs reference counting in order to
 * trigger the {@link SecurityChangeListener#on_SecurityChangeController_endChanging()}
 * method only at the last call of {@link #endChanging(boolean)} (when the reference counter
 * reaches 0).
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class SecurityChangeController
{
	private static ThreadLocal<SecurityChangeController> securityChangeControllerThreadLocal = new ThreadLocal<SecurityChangeController>();

	private SecurityChangeController() { }

	/**
	 * Get the instance of <code>SecurityChangeController</code> that is associated to the current {@link Thread}.
	 * If {@link #beginChanging()} was not called, yet, or {@link #endChanging(boolean)} was already called
	 * (symmetrically), this method causes an {@link IllegalStateException}.
	 *
	 * @return the instance associated to the current {@link Thread}.
	 */
	public static SecurityChangeController getInstance()
	{
		return getInstance(true);
	}
	private static SecurityChangeController getInstance(boolean assertIsChanging)
	{
		SecurityChangeController securityChangeController = securityChangeControllerThreadLocal.get();
		if (securityChangeController == null) {
			if (assertIsChanging)
				assertIsChanging();

			securityChangeController = new SecurityChangeController();
			securityChangeControllerThreadLocal.set(securityChangeController);
		}
		else if (securityChangeController.getReferenceCounter() < 1)
			assertIsChanging();
		return securityChangeController;
	}

	/**
	 * Mark the begin of a series of modifications (or only a single one) to the security-object-model.
	 * Every call to this method <b>must</b> be followed by one call to {@link #endChanging(boolean)}
	 * (use a finally-block!).
	 *
	 * @return the <code>SecurityChangeController</code> instance that has been allocated for tracking the current thread's
	 *		security-object-changes. From now on, this instance can be obtained by {@link #getInstance()} (when called on the same thread).
	 */
	public static SecurityChangeController beginChanging()
	{
		SecurityChangeController instance = getInstance(false);
		instance._beginChanging();
		return instance;
	}

	/**
	 * Mark the end of a series of modifications (or a single one) to the security-object-model.
	 * This method notifies all {@link SecurityChangeListener}s, if <code>successful == true</code>
	 * (in case of an error it is expected that the transaction is rolled back, thus calling
	 * the listeners is in vain).
	 * <p>
	 * If {@link #beginChanging()} was called multiple times, the listeners are only notified
	 * when <code>endChanging(boolean)</code> is called the same number of times (i.e. the thread-associated
	 * reference counter reaches 0). Additionally, the {@link #isChanging()} method only returns
	 * <code>false</code>, if the reference counter reached 0.
	 * </p>
	 * <p>
	 * It is essential to always ensure - by means of a try-finally-block - that for each {@link #beginChanging()}
	 * one call to {@link #endChanging(boolean)} happens!
	 * </p>
	 *
	 * @param successful whether the change operation was successful. <code>false</code> in case of a failure (i.e. an exception).
	 */
	public static void endChanging(boolean successful)
	{
		int refCount = getInstance(false)._endChanging(successful);
		if (refCount < 1)
			securityChangeControllerThreadLocal.remove();
	}

	private List<SecurityChangeListener> listenerCache = null;

	public List<SecurityChangeListener> getListeners() {
		if (listenerCache == null) {
			PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();
			List<SecurityChangeListener> listeners = new ArrayList<SecurityChangeListener>();
			for (Iterator<SecurityChangeListener> it = pm.getExtent(SecurityChangeListener.class).iterator(); it.hasNext(); )
				listeners.add(it.next());

			listenerCache = listeners;
		}
		return listenerCache;
	}

	public void fireSecurityChangeEvent_pre_Authority_createAuthorizedObjectRef(Authority authority, AuthorizedObject authorizedObject)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event = new SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef(
				authority, authorizedObject, null
		);

		for (SecurityChangeListener listener : getListeners())
			listener.pre_Authority_createAuthorizedObjectRef(event);
	}
	public void fireSecurityChangeEvent_post_Authority_createAuthorizedObjectRef(Authority authority, AuthorizedObject authorizedObject, AuthorizedObjectRef authorizedObjectRef)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event = new SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef(
				authority, authorizedObject, authorizedObjectRef
		);

		for (SecurityChangeListener listener : getListeners())
			listener.post_Authority_createAuthorizedObjectRef(event);
	}
	public void fireSecurityChangeEvent_pre_Authority_destroyAuthorizedObjectRef(Authority authority, AuthorizedObject authorizedObject, AuthorizedObjectRef authorizedObjectRef)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event = new SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef(
				authority, authorizedObject, authorizedObjectRef
		);

		for (SecurityChangeListener listener : getListeners())
			listener.pre_Authority_destroyAuthorizedObjectRef(event);
	}
	public void fireSecurityChangeEvent_post_Authority_destroyAuthorizedObjectRef(Authority authority, AuthorizedObject authorizedObject)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event = new SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef(
				authority, authorizedObject, null
		);

		for (SecurityChangeListener listener : getListeners())
			listener.post_Authority_destroyAuthorizedObjectRef(event);
	}

	public void fireSecurityChangeEvent_pre_AuthorizedObjectRef_addRole(AuthorizedObjectRef authorizedObjectRef, Role role, int incRefCount)
	{
		if (incRefCount == 0) // probably never happens, but still we check for it: nothing changed => no need to trigger listeners
			return;

		if (incRefCount < 0) { // should never happen, but to be really sure
			fireSecurityChangeEvent_pre_AuthorizedObjectRef_removeRole(authorizedObjectRef, role, -incRefCount);
			return;
		}

		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event = new SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole(
				authorizedObjectRef,
				role,
				incRefCount
		);

		for (SecurityChangeListener listener : getListeners())
			listener.pre_AuthorizedObjectRef_addRole(event);
	}

	public void fireSecurityChangeEvent_pre_AuthorizedObjectRef_removeRole(AuthorizedObjectRef authorizedObjectRef, Role role, int decRefCount)
	{
		if (decRefCount == 0) // probably never happens, but still we check for it: nothing changed => no need to trigger listeners
			return;

		if (decRefCount < 0) { // should never happen, but to be really sure
			fireSecurityChangeEvent_pre_AuthorizedObjectRef_addRole(authorizedObjectRef, role, -decRefCount);
			return;
		}

		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event = new SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole(
				authorizedObjectRef,
				role,
				decRefCount
		);

		for (SecurityChangeListener listener : getListeners())
			listener.pre_AuthorizedObjectRef_removeRole(event);
	}

	public void fireSecurityChangeEvent_post_AuthorizedObjectRef_addRole(AuthorizedObjectRef authorizedObjectRef, Role role, int incRefCount)
	{
		if (incRefCount == 0) // probably never happens, but still we check for it: nothing changed => no need to trigger listeners
			return;

		if (incRefCount < 0) { // should never happen, but to be really sure
			fireSecurityChangeEvent_post_AuthorizedObjectRef_removeRole(authorizedObjectRef, role, -incRefCount);
			return;
		}

		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event = new SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole(
				authorizedObjectRef,
				role,
				incRefCount
		);

		for (SecurityChangeListener listener : getListeners())
			listener.post_AuthorizedObjectRef_addRole(event);
	}

	public void fireSecurityChangeEvent_post_AuthorizedObjectRef_removeRole(AuthorizedObjectRef authorizedObjectRef, Role role, int decRefCount)
	{
		if (decRefCount == 0) // probably never happens, but still we check for it: nothing changed => no need to trigger listeners
			return;

		if (decRefCount < 0) { // should never happen, but to be really sure
			fireSecurityChangeEvent_post_AuthorizedObjectRef_addRole(authorizedObjectRef, role, -decRefCount);
			return;
		}

		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event = new SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole(
				authorizedObjectRef,
				role,
				decRefCount
		);

		for (SecurityChangeListener listener : getListeners())
			listener.post_AuthorizedObjectRef_removeRole(event);
	}


	public void fireSecurityChangeEvent_pre_AuthorizedObjectRef_addRoleGroupRef(AuthorizedObjectRef authorizedObjectRef, RoleGroupRef roleGroupRef)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef event = new SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef(
				authorizedObjectRef,
				roleGroupRef
		);

		for (SecurityChangeListener listener : getListeners())
			listener.pre_AuthorizedObjectRef_addRoleGroupRef(event);
	}

	public void fireSecurityChangeEvent_post_AuthorizedObjectRef_addRoleGroupRef(AuthorizedObjectRef authorizedObjectRef, RoleGroupRef roleGroupRef)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef event = new SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef(
				authorizedObjectRef,
				roleGroupRef
		);

		for (SecurityChangeListener listener : getListeners())
			listener.post_AuthorizedObjectRef_addRoleGroupRef(event);
	}

	public void fireSecurityChangeEvent_pre_AuthorizedObjectRef_removeRoleGroupRef(AuthorizedObjectRef authorizedObjectRef, RoleGroupRef roleGroupRef)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef event = new SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef(
				authorizedObjectRef,
				roleGroupRef
		);

		for (SecurityChangeListener listener : getListeners())
			listener.pre_AuthorizedObjectRef_removeRoleGroupRef(event);
	}

	public void fireSecurityChangeEvent_post_AuthorizedObjectRef_removeRoleGroupRef(AuthorizedObjectRef authorizedObjectRef, RoleGroupRef roleGroupRef)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef event = new SecurityChangeEvent_AuthorizedObjectRef_addRemoveRoleGroupRef(
				authorizedObjectRef,
				roleGroupRef
		);

		for (SecurityChangeListener listener : getListeners())
			listener.post_AuthorizedObjectRef_removeRoleGroupRef(event);
	}

	public void fireSecurityChangeEvent_pre_UserSecurityGroup_addMember(UserSecurityGroup userSecurityGroup, AuthorizedObject member)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_UserSecurityGroup_addRemoveMember event = new SecurityChangeEvent_UserSecurityGroup_addRemoveMember(
				userSecurityGroup,
				member
		);

		for (SecurityChangeListener listener : getListeners())
			listener.pre_UserSecurityGroup_addMember(event);
	}

	public void fireSecurityChangeEvent_post_UserSecurityGroup_addMember(UserSecurityGroup userSecurityGroup, AuthorizedObject member)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_UserSecurityGroup_addRemoveMember event = new SecurityChangeEvent_UserSecurityGroup_addRemoveMember(
				userSecurityGroup,
				member
		);

		for (SecurityChangeListener listener : getListeners())
			listener.post_UserSecurityGroup_addMember(event);
	}

	public void fireSecurityChangeEvent_pre_UserSecurityGroup_removeMember(UserSecurityGroup userSecurityGroup, AuthorizedObject member)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_UserSecurityGroup_addRemoveMember event = new SecurityChangeEvent_UserSecurityGroup_addRemoveMember(
				userSecurityGroup,
				member
		);

		for (SecurityChangeListener listener : getListeners())
			listener.pre_UserSecurityGroup_removeMember(event);
	}

	public void fireSecurityChangeEvent_post_UserSecurityGroup_removeMember(UserSecurityGroup userSecurityGroup, AuthorizedObject member)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_UserSecurityGroup_addRemoveMember event = new SecurityChangeEvent_UserSecurityGroup_addRemoveMember(
				userSecurityGroup,
				member
		);

		for (SecurityChangeListener listener : getListeners())
			listener.post_UserSecurityGroup_removeMember(event);
	}

	public void fireSecurityChangeEvent_on_UserLocal_passwordChanged(UserID userID, String newPassword)
	{
		if (getListeners().isEmpty())
			return;

		SecurityChangeEvent_UserLocal_passwordChanged event = new SecurityChangeEvent_UserLocal_passwordChanged(userID, newPassword);

		for (SecurityChangeListener listener : getListeners())
			listener.on_UserLocal_passwordChanged(event);
	}


	/**
	 * Find out, whether the current thread is currently marked for changing security-objects.
	 *
	 * @return <code>true</code> after the first call to {@link #beginChanging()} and <code>false</code>
	 *		after the last call to {@link #endChanging(boolean)}.
	 */
	public static boolean isChanging()
	{
		SecurityChangeController securityChangeController = securityChangeControllerThreadLocal.get();
		if (securityChangeController == null)
			return false;

		return securityChangeController.getReferenceCounter() > 0;
	}

	/**
	 * Assert that {@link #isChanging()} indicates <code>true</code>. If {@link #isChanging()}
	 * is <code>false</code>, an {@link IllegalStateException} is thrown.
	 */
	public static void assertIsChanging()
	{
		if (!isChanging())
			throw new IllegalStateException("SecurityChangeController.beginChanging() not called! Call this method before changing security objects!");
	}

	protected int getReferenceCounter() {
		return referenceCounter;
	}

	private int referenceCounter = 0;

	private int _beginChanging()
	{
		return ++referenceCounter;
	}
	private int _endChanging(boolean successful)
	{
		if (referenceCounter < 1)
			throw new IllegalStateException("referenceCounter < 1");

		if (--referenceCounter == 0) {
			if (successful) {
				for (SecurityChangeListener listener : getListeners())
					listener.on_SecurityChangeController_endChanging();
			}

			listenerCache = null;
		}

		return referenceCounter;
	}

}
