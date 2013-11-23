package org.nightlabs.jfire.jdo.notification;

import java.util.Collection;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;

public class SimpleLifecycleListenerFilter
		extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(SimpleLifecycleListenerFilter.class);

	private Class<?>[] candidateClasses;
	private boolean includeSubclasses;
	private JDOLifecycleState[] lifecycleStates;

	public SimpleLifecycleListenerFilter(Class<?> candidateClass, boolean includeSubclasses, JDOLifecycleState ... lifecycleStates)
	{
		this(new Class[] {candidateClass} , includeSubclasses, lifecycleStates);
	}

	public SimpleLifecycleListenerFilter(Class<?>[] candidateClasses, boolean includeSubclasses, JDOLifecycleState ... lifecycleStates)
	{
		this.candidateClasses = candidateClasses;
		this.includeSubclasses = includeSubclasses;
		this.lifecycleStates = lifecycleStates;
	}

	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("*** filter(...) enter");
			logger.debug("*****************************************************************************");
			logger.debug("this.class="+this.getClass());
			logger.debug("this.filterID="+this.getFilterID());
			logger.debug("this.candidateClasses="+this.getCandidateClasses());
			logger.debug("this.lifecycleStages="+this.getLifecycleStates());
		}

		if (logger.isDebugEnabled()) {
			PersistenceManager pm = event.getPersistenceManager();
			for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
				logger.debug("*");
				logger.debug("dirtyObjectID.serial=" + dirtyObjectID.getSerial());
				logger.debug("dirtyObjectID.lifecycleStage=" + dirtyObjectID.getLifecycleState());
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

	public Class<?>[] getCandidateClasses()
	{
		return candidateClasses;
	}

	@Override
	public boolean includeSubclasses()
	{
		return includeSubclasses;
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
			"candidateClasses=[" + String.valueOf(candidateClasses)+ "]," +
			"includeSubclasses=[" + includeSubclasses + "]," +
			"lifecycleStates=[" + lifecycleStates + ']' +
			']';
	}
}
