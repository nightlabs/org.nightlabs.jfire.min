package org.nightlabs.jfire.jdo.notification;

import java.util.Collection;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jdo.cache.DirtyObjectID;

public class SimpleLifecycleListenerFilter
		extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private Class[] candidateClasses;
	private boolean includeSubclasses;
	private DirtyObjectID.LifecycleStage[] lifecycleStages;

	public SimpleLifecycleListenerFilter(Class candidateClass, boolean includeSubclasses, DirtyObjectID.LifecycleStage ... lifecycleStages)
	{
		this(new Class[] {candidateClass} , includeSubclasses, lifecycleStages);
	}

	public SimpleLifecycleListenerFilter(Class[] candidateClasses, boolean includeSubclasses, DirtyObjectID.LifecycleStage ... lifecycleStages)
	{
		this.candidateClasses = candidateClasses;
		this.includeSubclasses = includeSubclasses;
		this.lifecycleStages = lifecycleStages;
	}

	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("*****************************************************************************");

		PersistenceManager pm = event.getPersistenceManager();
		for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
			System.out.println("");
			System.out.println("dirtyObjectID.serial=" + dirtyObjectID.getSerial());
			System.out.println("dirtyObjectID.lifecycleStage=" + dirtyObjectID.getLifecycleStage());
			System.out.println("dirtyObjectID.objectID=" + dirtyObjectID.getObjectID());
			try {
				Object obj = pm.getObjectById(dirtyObjectID.getObjectID());
				System.out.println("object=" + obj);
			} catch (JDOObjectNotFoundException x) {
				x.printStackTrace();
			}
		}

		System.out.println("*****************************************************************************");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		return event.getDirtyObjectIDs();
	}

	public Class[] getCandidateClasses()
	{
		return candidateClasses;
	}

	@Override
	public boolean includeSubclasses()
	{
		return includeSubclasses;
	}

	public DirtyObjectID.LifecycleStage[] getLifecycleStages()
	{
		return lifecycleStages;
	}
}
