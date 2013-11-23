package org.nightlabs.jfire.prop;

import java.util.Collection;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;

public class StructLocalLifecycleListenerFilter
extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private JDOLifecycleState[] lifecycleStates;

	public StructLocalLifecycleListenerFilter(JDOLifecycleState[] lifecycleStates)
	{
		this.lifecycleStates = lifecycleStates;
	}

	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		return event.getDirtyObjectIDs();
	}

	private static Class<?>[] candidateClasses = new Class[] {StructLocal.class};

	public Class<?>[] getCandidateClasses()
	{
		return candidateClasses;
	}

	@Override
	public boolean includeSubclasses()
	{
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
			"filterID=[" + getFilterID() + "]" +
			']';
	}
}
