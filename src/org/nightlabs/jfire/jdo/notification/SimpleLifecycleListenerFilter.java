package org.nightlabs.jfire.jdo.notification;

import java.util.Collection;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.jdo.cache.DirtyObjectID;

public class SimpleLifecycleListenerFilter
		extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(SimpleLifecycleListenerFilter.class);

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
		if (logger.isDebugEnabled()) {
			logger.debug("*** filter(...) enter");
			logger.debug("*****************************************************************************");
			logger.debug("this.class="+this.getClass());
			logger.debug("this.filterID="+this.getFilterID());
			logger.debug("this.candidateClasses="+this.getCandidateClasses());
			logger.debug("this.lifecycleStages="+this.getLifecycleStages());
		}

		if (logger.isDebugEnabled()) {
			PersistenceManager pm = event.getPersistenceManager();
			for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
				logger.debug("*");
				logger.debug("dirtyObjectID.serial=" + dirtyObjectID.getSerial());
				logger.debug("dirtyObjectID.lifecycleStage=" + dirtyObjectID.getLifecycleStage());
				logger.debug("dirtyObjectID.objectID=" + dirtyObjectID.getObjectID());
				logger.debug("dirtyObjectID.objectVersion=" + dirtyObjectID.getObjectVersion());

				try {
					Object obj = pm.getObjectById(dirtyObjectID.getObjectID());
					logger.debug("object=" + obj);
				} catch (JDOObjectNotFoundException x) {
					logger.error("Loading object from datastore failed! objectID=" + dirtyObjectID.getObjectID(), x);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("*****************************************************************************");
			logger.debug("*** filter(...) exit");
		}
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
