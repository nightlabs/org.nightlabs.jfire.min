package org.nightlabs.jfire.jdo.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;

public class TreeLifecycleListenerFilter
extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(TreeLifecycleListenerFilter.class);

	private Class<?>[] candidateClasses;
	private boolean includeSubclasses;
	private Set<? extends ObjectID> parentObjectIDs;
	private TreeNodeParentResolver parentResolver;
	private TreeNodeMultiParentResolver multiParentResolver;
	private JDOLifecycleState[] lifecycleStates;

	public TreeLifecycleListenerFilter(Class<?> candidateClass, boolean includeSubclasses, Set<? extends ObjectID> parentObjectIDs, TreeNodeParentResolver parentResolver, JDOLifecycleState ... lifecycleStates)
	{
		this(new Class[] {candidateClass} , includeSubclasses, parentObjectIDs, parentResolver, lifecycleStates);
	}

	public TreeLifecycleListenerFilter(Class<?>[] candidateClasses, boolean includeSubclasses, Set<? extends ObjectID> parentObjectIDs, TreeNodeParentResolver parentResolver, JDOLifecycleState ... lifecycleStates)
	{
		this.candidateClasses = candidateClasses;
		this.includeSubclasses = includeSubclasses;
		this.parentObjectIDs = parentObjectIDs;
		this.parentResolver = parentResolver;
		this.lifecycleStates = lifecycleStates;

		if (parentResolver == null && parentObjectIDs != null)
			throw new IllegalArgumentException("parentResolver == null && parentObjectIDs != null *** parentResolver may only be null, if parentObjectIDs is null!");
	}

	public TreeLifecycleListenerFilter(Class<?> candidateClass, boolean includeSubclasses, Set<? extends ObjectID> parentObjectIDs, TreeNodeMultiParentResolver parentResolver, JDOLifecycleState ... lifecycleStates)
	{
		this(new Class[] {candidateClass} , includeSubclasses, parentObjectIDs, parentResolver, lifecycleStates);
	}

	public TreeLifecycleListenerFilter(Class<?>[] candidateClasses, boolean includeSubclasses, Set<? extends ObjectID> parentObjectIDs, TreeNodeMultiParentResolver parentResolver, JDOLifecycleState ... lifecycleStates)
	{
		this.candidateClasses = candidateClasses;
		this.includeSubclasses = includeSubclasses;
		this.parentObjectIDs = parentObjectIDs;
		this.multiParentResolver = parentResolver;
		this.lifecycleStates = lifecycleStates;

		if (parentResolver == null && parentObjectIDs != null)
			throw new IllegalArgumentException("parentResolver == null && parentObjectIDs != null *** parentResolver may only be null, if parentObjectIDs is null!");
	}

	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("*** filter(...) enter");
			logger.debug("*****************************************************************************");
			logger.debug("this.class="+this.getClass());
			logger.debug("this.filterID="+this.getFilterID());
			logger.debug("this.candidateClasses="+this.getCandidateClasses());
			logger.debug("this.parentObjectIDs="+this.getParentObjectIDs());
			logger.debug("this.parentResolver="+this.getParentResolver());
			logger.debug("this.multiParentResolver="+this.getMultiParentResolver());
			logger.debug("this.lifecycleStages="+this.getLifecycleStates());
		}

		if (logger.isDebugEnabled() && parentObjectIDs == null) {
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

		if (parentObjectIDs == null)
			return event.getDirtyObjectIDs();

		ArrayList<DirtyObjectID> res = new ArrayList<DirtyObjectID>(event.getDirtyObjectIDs().size());

		PersistenceManager pm = event.getPersistenceManager();
		iterateDirtyObjectIDs: for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
			logger.debug("*");
			logger.debug("dirtyObjectID.serial=" + dirtyObjectID.getSerial());
			logger.debug("dirtyObjectID.lifecycleStage=" + dirtyObjectID.getLifecycleState());
			logger.debug("dirtyObjectID.objectID=" + dirtyObjectID.getObjectID());
			logger.debug("dirtyObjectID.objectVersion=" + dirtyObjectID.getObjectVersion());

			Object obj = null;
			try {
				obj = pm.getObjectById(dirtyObjectID.getObjectID());
			} catch (JDOObjectNotFoundException x) {
				logger.error("Loading object from datastore failed! objectID=" + dirtyObjectID.getObjectID(), x);
			}
			if (obj == null)
				continue iterateDirtyObjectIDs;

			if (parentResolver != null) {
				ObjectID parentID = parentResolver.getParentObjectID(obj);
				if (parentID == null) {
					res.add(dirtyObjectID);
					if (logger.isDebugEnabled()) {
						logger.debug("object added, because its parent is null and we want to be notified about top-level-objects:");
						logger.debug("    objectID: " + dirtyObjectID.getObjectID());
					}
				}
				else if (parentObjectIDs.contains(parentID)) {
					res.add(dirtyObjectID);
					if (logger.isDebugEnabled()) {
						logger.debug("object added, because parentObjectIDs does contain parent:");
						logger.debug("    objectID: " + dirtyObjectID.getObjectID());
						logger.debug("    parentID: " + parentID);
					}
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("ignoring object, because parentObjectIDs does not contain parent:");
						logger.debug("    objectID: " + dirtyObjectID.getObjectID());
						logger.debug("    parentID: " + parentID);
					}
				}
			}
			else if (multiParentResolver != null) {
				Collection<ObjectID> parentIDs = multiParentResolver.getParentObjectIDs(obj);
				for (ObjectID parentID : parentIDs) {
					if (parentID == null) {
						res.add(dirtyObjectID);
						if (logger.isDebugEnabled()) {
							logger.debug("object added, because its parent is null and we want to be notified about top-level-objects:");
							logger.debug("    objectID: " + dirtyObjectID.getObjectID());
						}
						continue iterateDirtyObjectIDs;
					}
					else if (parentObjectIDs.contains(parentID)) {
						res.add(dirtyObjectID);
						if (logger.isDebugEnabled()) {
							logger.debug("object added, because parentObjectIDs does contain parent:");
							logger.debug("    objectID: " + dirtyObjectID.getObjectID());
							logger.debug("    parentID: " + parentID);
						}
						continue iterateDirtyObjectIDs;
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("ignoring object, because parentObjectIDs does not contain any of the parents:");
					logger.debug("    objectID: " + dirtyObjectID.getObjectID());
					for (ObjectID parentID : parentIDs) {
						logger.debug("    parentID: " + parentID);
					}
				}
			}
			else
				throw new IllegalStateException("parentResolver and multiParentResolver are both null!");

		}
		return res;
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

	public Set<? extends ObjectID> getParentObjectIDs()
	{
		return parentObjectIDs;
	}

	public TreeNodeParentResolver getParentResolver()
	{
		return parentResolver;
	}

	public TreeNodeMultiParentResolver getMultiParentResolver() {
		return multiParentResolver;
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
			"lifecycleStates=[" + lifecycleStates + "]," +
			"parentResolver=[" + parentResolver + "]," +
			"parentObjectIDs=[" + parentObjectIDs + "]" +
			']';
	}
}
