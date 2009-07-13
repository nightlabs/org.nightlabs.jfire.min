package org.nightlabs.jfire.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.SimpleLifecycleListenerFilter;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.AuthorizedObjectRefID;

public class AuthorizedObjectRefLifecycleListenerFilter
extends SimpleLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private AuthorizedObjectID authorizedObjectID;
	private transient String authorizedObjectIDString;

	public AuthorizedObjectRefLifecycleListenerFilter(AuthorizedObjectID authorizedObjectID, JDOLifecycleState... lifecycleStates)
	{
		super(AuthorizedObjectRef.class, false, lifecycleStates);
		this.authorizedObjectID = authorizedObjectID;
	}

	@Override
	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		if (authorizedObjectID == null)
			return event.getDirtyObjectIDs();

		if (authorizedObjectIDString == null)
			authorizedObjectIDString = authorizedObjectID.toString();

		List<DirtyObjectID> result = null;
		for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
			AuthorizedObjectRefID authorizedObjectRefID = (AuthorizedObjectRefID) dirtyObjectID.getObjectID();
			if (authorizedObjectRefID.authorizedObjectID.equals(authorizedObjectIDString)) {
				if (result == null)
					result = new ArrayList<DirtyObjectID>();

				result.add(dirtyObjectID);
			}
		}
		return result;
	}
}
