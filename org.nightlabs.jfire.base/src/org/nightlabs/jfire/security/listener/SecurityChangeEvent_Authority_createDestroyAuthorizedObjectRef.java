package org.nightlabs.jfire.security.listener;

import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;

/**
 * Instances of this event class are propagated to
 * {@link SecurityChangeListener#pre_Authority_createAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef)},
 * {@link SecurityChangeListener#post_Authority_createAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef)},
 * {@link SecurityChangeListener#pre_Authority_destroyAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef)}
 * and
 * {@link SecurityChangeListener#post_Authority_destroyAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef)}.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef
extends SecurityChangeEvent
{
	private Authority authority;
	private AuthorizedObject authorizedObject;
	private AuthorizedObjectRef authorizedObjectRef;

	/**
	 * Create an instance of this event.
	 *
	 * @param authority the affected {@link Authority}.
	 * @param authorizedObject the affected {@link AuthorizedObject} - usually a {@link UserLocal} or {@link UserSecurityGroup}.
	 * @param authorizedObjectRef the reference created/destroyed or <code>null</code> if not yet / no more existing.
	 */
	public SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef(
			Authority authority,
			AuthorizedObject authorizedObject,
			AuthorizedObjectRef authorizedObjectRef
	)
	{
		if (authority == null)
			throw new IllegalArgumentException("authority is null!");

		if (authorizedObject == null)
			throw new IllegalArgumentException("authorizedObject is null!");

		this.authority = authority;
		this.authorizedObject = authorizedObject;
		this.authorizedObjectRef = authorizedObjectRef;
	}

	/**
	 * Get the affected {@link Authority}.
	 *
	 * @return the affected {@link Authority}.
	 */
	public Authority getAuthority() {
		return authority;
	}

	/**
	 * Get the affected {@link AuthorizedObject}.
	 *
	 * @return the affected {@link AuthorizedObject}.
	 */
	public AuthorizedObject getAuthorizedObject() {
		return authorizedObject;
	}

	/**
	 * Get the {@link AuthorizedObjectRef} if it exists (otherwise this is <code>null</code>). The
	 * <code>AuthorizedObjectRef</code> does not yet exist in
	 * {@link SecurityChangeListener#pre_Authority_createAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef)}
	 * and it does not exist anymore in
	 * {@link SecurityChangeListener#post_Authority_destroyAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef)}.
	 *
	 * @return the {@link AuthorizedObjectRef} or <code>null</code>.
	 */
	public AuthorizedObjectRef getAuthorizedObjectRef() {
		return authorizedObjectRef;
	}
}
