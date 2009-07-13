package org.nightlabs.jfire.security.listener;

import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.Role;

/**
 * Instances of this event class are propagated to
 * {@link SecurityChangeListener#pre_AuthorizedObjectRef_addRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole)},
 * {@link SecurityChangeListener#post_AuthorizedObjectRef_addRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole)},
 * {@link SecurityChangeListener#pre_AuthorizedObjectRef_removeRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole)}
 * and
 * {@link SecurityChangeListener#post_AuthorizedObjectRef_removeRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole)}.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole
extends SecurityChangeEvent
{
	private AuthorizedObjectRef authorizedObjectRef;
	private Role role;
	private int diffRefCount;

	/**
	 * Create an instance of this event.
	 *
	 * @param authorizedObjectRef the affected {@link AuthorizedObjectRef}.
	 * @param role the role which was (or is about to be) added or removed
	 * @param diffRefCount the positive (&gt; 0) number of role-references added or removed (i.e. the reference counter incremented/decremented).
	 */
	public SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole(
			AuthorizedObjectRef authorizedObjectRef,
			Role role,
			int diffRefCount
	)
	{
		if (authorizedObjectRef == null)
			throw new IllegalArgumentException("authorizedObjectRef is null!");

		if (role == null)
			throw new IllegalArgumentException("role is null!");

		if (diffRefCount <= 0)
			throw new IllegalArgumentException("diffRefCount <= 0");

		this.authorizedObjectRef = authorizedObjectRef;
		this.role = role;
		this.diffRefCount = diffRefCount;
	}

	/**
	 * Get the affected {@link AuthorizedObjectRef}.
	 *
	 * @return the affected {@link AuthorizedObjectRef}; never <code>null</code>.
	 */
	public AuthorizedObjectRef getAuthorizedObjectRef() {
		return authorizedObjectRef;
	}
	/**
	 * Get the affected {@link Role}.
	 *
	 * @return the affected {@link Role}; never <code>null</code>.
	 */
	public Role getRole() {
		return role;
	}
	/**
	 * The number of references added or removed (or to be, if "pre_" event). This is guaranteed to be &gt; 0.
	 * <p>
	 * If the method {@link SecurityChangeListener#pre_AuthorizedObjectRef_addRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole)}
	 * or {@link SecurityChangeListener#post_AuthorizedObjectRef_addRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole)}
	 * is called with this event, the <code>diffRefCount</code> is the number of role references to be <b>added</b> (increased).
	 * </p>
	 * <p>
	 * If the method {@link SecurityChangeListener#pre_AuthorizedObjectRef_removeRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole)}
	 * or {@link SecurityChangeListener#post_AuthorizedObjectRef_removeRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole)}
	 * is called with this event, the <code>diffRefCount</code> is the number of role references to be <b>removed</b> (decreased).
	 * </p>
	 *
	 * @return the change to the reference counter (relative to the method).
	 */
	public int getDiffRefCount() {
		return diffRefCount;
	}
}
