package org.nightlabs.jfire.security;

import java.util.ArrayList;
import java.util.Collection;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.id.UserID;

public class UserLifecycleListenerFilter
extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private String userType;
	private JDOLifecycleState[] lifecycleStates;

	/**
	 * @param userType Either <code>null</code> in order to react on all types of users
	 *		or one of the <code>User.USERTYPE_*</code> constants (see {@link User}).
	 */
	public UserLifecycleListenerFilter(String userType, JDOLifecycleState[] lifecycleStates)
	{
		if (userType != null &&
				!User.USER_TYPE_ORGANISATION.equals(userType) &&
				!User.USER_TYPE_USER.equals(userType)
//				&&
//				!User.USERTYPE_USERGROUP.equals(userType)
		)
			throw new IllegalArgumentException("Unknown userType: " + userType);

		this.userType = userType;
		this.lifecycleStates = lifecycleStates;
	}

	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		if (userType == null)
			return event.getDirtyObjectIDs();

// TODO what is event.getDirtyObjectIDs() usually? Is a new ArrayList good?
// we should document in IJDOLifecycleListenerFilter, what kind of collection is preferred here!!!
		Collection<DirtyObjectID> res = new ArrayList<DirtyObjectID>(event.getDirtyObjectIDs().size());
		for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
			UserID userID = (UserID) dirtyObjectID.getObjectID();
//			if (userID.userID.startsWith(User.USERID_PREFIX_TYPE_USERGROUP)) {
//				if (User.USERTYPE_USERGROUP.equals(userType))
//					res.add(dirtyObjectID);
//			}
//			else
			if (userID.userID.startsWith(User.USER_ID_PREFIX_TYPE_ORGANISATION)) {
				if (User.USER_TYPE_ORGANISATION.equals(userType))
					res.add(dirtyObjectID);
			}
			else {
				if (User.USER_TYPE_USER.equals(userType))
					res.add(dirtyObjectID);
			}
		}
		return res;
	}

	private static Class<?>[] candidateClasses_user = new Class[] { User.class };
//	private static Class<?>[] candidateClasses_userGroup = new Class[] { UserGroup.class };

	public Class<?>[] getCandidateClasses()
	{
//		if (User.USERTYPE_USERGROUP.equals(userType))
//			return candidateClasses_userGroup;

		return candidateClasses_user;
	}

	@Override
	public boolean includeSubclasses()
	{
//		if (userType == null)
//			return true;

		return false; // more efficient not to react on subclasses, if it's not necessary
	}

	public JDOLifecycleState[] getLifecycleStates()
	{
		return lifecycleStates;
	}

	@Override
	public String toString()
	{
		return
			this.getClass().getName() + '@' + System.identityHashCode(this) +
			'[' +
			"filterID=[" + getFilterID() + "]," +
			"userType=[" + userType+ "]" +
			']';
	}
}
