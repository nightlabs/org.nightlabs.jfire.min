package org.nightlabs.jfire.base.jdo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.exceptionhandler.ErrorTable.ContentProvider;
import org.nightlabs.base.notification.NotificationAdapterJob;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.TreeLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

/**
 * A controller to be used as datasource for JDO tree datastructures. 
 * For example it could be used as {@link ContentProvider} in a tree displaying this structure.
 * The controller is <em>active</em> as it tracks changes to the structure (new/deleted objects, changed objects)
 * keeps the data up-to-date and uses a callback to notify the user of the changes (see {@link #onJDOObjectsChanged(JDOTreeNodesChangedEvent)}). 
 * <p> 
 * The controller could be used
 * 
 * @author Marco Schulze 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @param <JDOObjectID> The type of the {@link ObjectID} the tree sturcture uses
 * @param <JDOObject> The type of the JDO object used
 * @param <TreeNode> The type of {@link JDOObjectTreeNode} used to hold the data
 */
public abstract class ActiveJDOObjectTreeController<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectTreeNode>
{
	/**
	 * Retrieve the children of the given parent element.
	 * If the parent is null this method should return the root element of the tree structure. 
	 * 
	 * @param parent <code>null</code> for the root elements or the parent element for which to load the children.
	 */
	protected abstract Collection<JDOObject> retrieveChildren(JDOObjectID parentID, JDOObject parent, IProgressMonitor monitor);

	/**
	 * This method is called on a worker thread and must retrieve JDO objects for
	 * the given object-ids from the server. It is called when changes to the structure were tracked.
	 *
	 * @param objectIDs The jdo object ids representing the desired objects.
	 * @param monitor The monitor.
	 * @return Returns the jdo objects that correspond to the requested <code>objectIDs</code>.
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, IProgressMonitor monitor);

	protected abstract TreeNode createNode();

	/**
	 * Sort the retrieved JDOObjects in a custom manner. (Manipulate the list)
	 * 
	 * @param objects The objects to sort
	 */
	protected abstract void sortJDOObjects(List<JDOObject> objects);

	private List<TreeNode> rootElements = null;

	private Map<JDOObjectID, TreeNode> objectID2TreeNode = new HashMap<JDOObjectID, TreeNode>();

	/**
	 * This method is called by the default implementation of {@link #createJDOLifecycleListenerFilter()}.
	 * It is responsible for creating a {@link TreeNodeParentResolver} for the actual
	 * type of JDOObject.
	 */
	protected abstract TreeNodeParentResolver createTreeNodeParentResolver();

	private TreeNodeParentResolver treeNodeParentResolver = null;

	/**
	 * Get the {@link TreeNodeParentResolver} for this controller.
	 * It will be created lazily by a call to {@link #createTreeNodeParentResolver()}.
	 * 
	 * @return The {@link TreeNodeParentResolver} for this controller.
	 */
	public TreeNodeParentResolver getTreeNodeParentResolver()
	{
		if (treeNodeParentResolver == null)
			treeNodeParentResolver = createTreeNodeParentResolver();

		return treeNodeParentResolver;
	}

	/**
	 * Get the {@link Class} (type) of the JDO object this controller is for.
	 * Should be the same this controller was typed with.
	 * 
	 * @return The {@link Class} (type) of the JDO object this controller is for.
	 */
	protected abstract Class getJDOObjectClass();

	/**
	 * Creates an {@link IJDOLifecycleListenerFilter} that will be used to
	 * track new objects that are children of one of the objects referenced by
	 * the given parentObjectIDs. 
	 * By default this will create a {@link TreeLifecycleListenerFilter}
	 * for {@link JDOLifecycleState#NEW}.  
	 * 
	 * @param parentObjectIDs The {@link ObjectID}s of the parent objects new children should be tracked for.
	 * @return A new {@link IJDOLifecycleListenerFilter}
	 */
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter(Set<? extends ObjectID> parentObjectIDs)
	{
		return new TreeLifecycleListenerFilter(
				getJDOObjectClass(), true,
				parentObjectIDs, getTreeNodeParentResolver(),
				new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	/**
	 * Creates a {@link JDOLifecycleListener} with the {@link IJDOLifecycleListenerFilter} obtained
	 * by {@link #createJDOLifecycleListenerFilter(Set)}.
	 * 
	 * @param parentObjectIDs The {@link ObjectID}s of the parent objects new children should be tracked for.
	 * @return  A new {@link JDOLifecycleListener}
	 */
	protected JDOLifecycleListener createJDOLifecycleListener(Set<? extends ObjectID> parentObjectIDs)
	{
		IJDOLifecycleListenerFilter filter = createJDOLifecycleListenerFilter(parentObjectIDs);
		return new LifecycleListener(filter);
	}

	/**
	 * This will be called when a change in the tree structure was tracked and after the changes
	 * were retrieved. The {@link JDOTreeNodesChangedEvent} contains references to the
	 * {@link TreeNode}s that need update or were removed.
	 * 
	 * @param changedEvent The {@link JDOTreeNodesChangedEvent} containing references to changed/new and deleted {@link TreeNode}s
	 */
	protected abstract void onJDOObjectsChanged(JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent);	
	
	private NotificationListener changeListener;
	
	protected void handleChangeNotification(NotificationEvent notificationEvent, IProgressMonitor monitor) {
		Collection<DirtyObjectID> dirtyObjectIDs = notificationEvent.getSubjects();
		final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
		final Map<JDOObjectID, TreeNode> dirtyNodes = new HashMap<JDOObjectID, TreeNode>();
		final Map<JDOObjectID, TreeNode> deletedNodes = new HashMap<JDOObjectID, TreeNode>();
		for (DirtyObjectID objectID : dirtyObjectIDs) {				
			TreeNode dirtyNode = objectID2TreeNode.get(objectID.getObjectID());
			if (dirtyNode == null)
				continue;
			JDOObjectID jdoObjectID = (JDOObjectID) objectID.getObjectID();
			switch (objectID.getLifecycleState()) {
				case DIRTY: dirtyNodes.put(jdoObjectID, dirtyNode); break;
				case DELETED: deletedNodes.put(jdoObjectID, dirtyNode); break;
			}
		}
		final Map<JDOObjectID, TreeNode> ignoredNodes = new HashMap<JDOObjectID, TreeNode>();
		ignoredNodes.putAll(dirtyNodes);
		Collection<JDOObject> retrievedObjects = retrieveJDOObjects(dirtyNodes.keySet(), monitor);
		for (JDOObject retrievedObject : retrievedObjects) {
			JDOObjectID retrievedID = (JDOObjectID) JDOHelper.getObjectId(retrievedObject);
			ignoredNodes.remove(retrievedID);
			TreeNode node = dirtyNodes.get(retrievedID);
			node.setJdoObject(retrievedObject);
		}
		for (Entry<JDOObjectID, TreeNode> deletedEntry : deletedNodes.entrySet()) {
			JDOObjectID parentID = (JDOObjectID) treeNodeParentResolver.getParentObjectID(deletedEntry.getKey());
			TreeNode parentNode = objectID2TreeNode.get(parentID);
			objectID2TreeNode.remove(deletedEntry.getKey());
			if (parentNode != null) {
				parentNode.getChildNodes().remove(deletedEntry.getValue());
				if (!parentsToRefresh.contains(parentNode)) {
					parentsToRefresh.add(parentNode);
				}
			}
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireJDOObjectsChangedEvent(new JDOTreeNodesChangedEvent<JDOObjectID, TreeNode>(
						ActiveJDOObjectTreeController.this,
						parentsToRefresh,
						new ArrayList<TreeNode>(dirtyNodes.values()), 
						ignoredNodes, 
						deletedNodes
					)
				);
			}
		});
	}
	
	protected class ChangeListener extends NotificationAdapterJob {		
		
		public ChangeListener(String name) {
			super(name);
		}
		
		@SuppressWarnings("unchecked")
		public void notify(NotificationEvent notificationEvent) {
			handleChangeNotification(notificationEvent, getProgressMonitor());
		}
	};

	protected void registerJDOLifecycleListeners()
	{
		if (lifecycleListener != null) {
			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}

		lifecycleListener = createJDOLifecycleListener(activeParentObjectIDs);		
		JDOLifecycleManager.sharedInstance().addLifecycleListener(lifecycleListener);
				
	}
	
	protected void createRegisterChangeListener() {
		if (changeListener == null) {
			changeListener = new ChangeListener("Loading changes ...");
			JDOLifecycleManager.sharedInstance().addNotificationListener(getJDOObjectClass(), changeListener);
		}
	}
	
	protected void unregisterChangeListener() {
		if (changeListener != null) {
			JDOLifecycleManager.sharedInstance().removeNotificationListener(getJDOObjectClass(), changeListener);
			changeListener = null;
		}
	}

	public void close()
	{
		if (lifecycleListener != null) {
			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}
		unregisterChangeListener();
	}

	private JDOLifecycleListener lifecycleListener = null;
	protected class LifecycleListener extends JDOLifecycleAdapterJob
	{
		private IJDOLifecycleListenerFilter filter;

		public LifecycleListener(IJDOLifecycleListenerFilter filter)
		{
			this.filter = filter;
		}

		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
		{
			return filter;
		}

		@SuppressWarnings("unchecked")
		public void notify(JDOLifecycleEvent event)
		{
			Set<JDOObjectID> objectIDs = new HashSet<JDOObjectID>(event.getDirtyObjectIDs().size());
			final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
			final List<TreeNode> loadedTreeNodes = new ArrayList<TreeNode>();
			for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
				objectIDs.add((JDOObjectID) dirtyObjectID.getObjectID());
			}

			Collection<JDOObject> objects = retrieveJDOObjects(objectIDs, getProgressMonitor());
			for (JDOObject object : objects) {
				TreeNode parentNode;
				boolean ignoreNodeBecauseParentUnknown = false;
				ObjectID parentID = getTreeNodeParentResolver().getParentObjectID(object);
				if (parentID == null) {
					parentNode = null;
				}
				else {
					parentNode = objectID2TreeNode.get(parentID);
					if (parentNode == null)
						ignoreNodeBecauseParentUnknown = true;
				}

				if (ignoreNodeBecauseParentUnknown)
					continue;

				parentsToRefresh.add(null);

				JDOObjectID objectID = (JDOObjectID) JDOHelper.getObjectId(object);
				TreeNode tn = objectID2TreeNode.get(objectID);
				if (tn != null && parentNode != tn.getParent()) { // parent changed, completely replace!
					TreeNode p = (TreeNode) tn.getParent();
					parentsToRefresh.add(p);
					if (p != null) {
						List<TreeNode> cn = p.getChildNodes();
						if (cn != null)
							cn.remove(tn);
					}
					objectID2TreeNode.remove(objectID);
					tn = null;
				}
				if (tn == null) {
					tn = createNode();
					tn.setActiveJDOObjectTreeController(ActiveJDOObjectTreeController.this);
				}
				tn.setJdoObject(object);
				if (tn.getParent() != parentNode) {
					tn.setParent(parentNode);
					if (parentNode != null) {
						List<TreeNode> cn = parentNode.getChildNodes();
						if (cn != null)
							cn.add(tn);
					}
				}
				objectID2TreeNode.put(objectID, tn);
				loadedTreeNodes.add(tn);
			}

			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					fireJDOObjectsChangedEvent(new JDOTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh, loadedTreeNodes));
				}
			});
		}
	}

	/**
	 * These objects will be watched for new children to pop up. May contain <code>null</code> for root-elements
	 * (which is very likely).
	 */
	private Set<JDOObjectID> activeParentObjectIDs = new HashSet<JDOObjectID>();

	/**
	 * This method returns either root-nodes, if <code>parent == null</code> or children of the given
	 * <code>parent</code> (if non-<code>null</code>). Alternatively, this method can return <code>null</code>,
	 * if the data is not yet available. In this case, a new {@link Job} will be spawned to load the data.
	 *
	 * @param parent The parent node or <code>null</code>.
	 * @return A list of {@link TreeNode}s or <code>null</code>, if data is not yet ready.
	 */
	@SuppressWarnings("unchecked")
	public List<TreeNode> getNodes(final TreeNode parent)
	{
		List<TreeNode> nodes = null;
		
		createRegisterChangeListener();
		
		if (parent == null) {
			if (activeParentObjectIDs.add(null))
				registerJDOLifecycleListeners();

			nodes = rootElements;
		}
		else {
			if (activeParentObjectIDs.add((JDOObjectID)JDOHelper.getObjectId(parent.getJdoObject())))
				registerJDOLifecycleListeners();

			nodes = parent.getChildNodes();
		}

		if (nodes != null)
			return nodes;

		Job job = new Job("Loading Data") {
			@Implement
			protected IStatus run(IProgressMonitor monitor)
			{
				JDOObject parentJDO = parent == null ? null : (JDOObject) parent.getJdoObject();
				JDOObjectID parentJDOID = (JDOObjectID) JDOHelper.getObjectId(parentJDO);

				Collection<JDOObject> jdoObjects = retrieveChildren(parentJDOID, parentJDO, monitor);

				if (jdoObjects == null)
					throw new IllegalStateException("Your implementation of retrieveChildren(...) returned null! The error is probably in class " + ActiveJDOObjectTreeController.this.getClass().getName());

				List<JDOObject> jdoObjectList;
				if (jdoObjects instanceof List)
					jdoObjectList = (List<JDOObject>) jdoObjects;
				else
					jdoObjectList = new ArrayList<JDOObject>(jdoObjects);

				sortJDOObjects(jdoObjectList);

				final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
				parentsToRefresh.add(parent);

				final List<TreeNode> newNodes = new ArrayList<TreeNode>(jdoObjectList.size());
				for (JDOObject jdoObject : jdoObjectList) {
					JDOObjectID objectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
					TreeNode tn = objectID2TreeNode.get(objectID);
					if (tn != null && parent != tn.getParent()) { // parent changed, completely replace!
						TreeNode p = (TreeNode) tn.getParent();
						parentsToRefresh.add(p);
						if (p != null) {
							List<TreeNode> cn = p.getChildNodes();
							if (cn != null)
								cn.remove(tn);
						}
						objectID2TreeNode.remove(objectID);
						tn = null;
					}
					if (tn == null) {
						tn = createNode();
						tn.setActiveJDOObjectTreeController(ActiveJDOObjectTreeController.this);
					}
					tn.setJdoObject(jdoObject);
					objectID2TreeNode.put(objectID, tn);
					newNodes.add(tn);
				}

				if (parent == null)
					rootElements = newNodes;
				else
					parent.setChildNodes(newNodes);

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						fireJDOObjectsChangedEvent(new JDOTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh, newNodes));
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

	private ListenerList treeNodesChangedListeners;
	
	public void addJDOTreeNodesChangedListener(JDOTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> listener) {
		if (treeNodesChangedListeners == null)
			treeNodesChangedListeners = new ListenerList();
		treeNodesChangedListeners.add(listener);
	}
	
	public void removeJDOTreeNodesChangedListener(JDOTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> listener) {
		if (treeNodesChangedListeners == null)
			return;
		treeNodesChangedListeners.remove(listener);
	}
	
	@SuppressWarnings("unchecked")
	private void fireJDOObjectsChangedEvent(JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent)
	{
		onJDOObjectsChanged(changedEvent);
		
		if (treeNodesChangedListeners != null) {
			Object[] listeners = treeNodesChangedListeners.getListeners();
			for (Object listener : listeners) {
				JDOTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> l = (JDOTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode>) listener;
				l.onJDOObjectsChanged(changedEvent);
			}
		}
		
	}

}
