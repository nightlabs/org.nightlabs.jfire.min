package org.nightlabs.jfire.base.jdo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.notification.NotificationAdapterJob;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.TreeLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

/**
 * A controller to be used as datasource for JDO tree datastructures. 
 * For example it could be used in a tree displaying this structure.
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
	private static final Logger logger = Logger.getLogger(ActiveJDOObjectTreeController.class);

	/**
	 * Retrieve the children of the given parent element.
	 * If the parent is null this method should return the root element of the tree structure. 
	 * 
	 * @param parent <code>null</code> for the root elements or the parent element for which to load the children.
	 * @return The children of the the JDOObject with the given parentID, but never <code>null</code>.
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

//	private List<TreeNode> rootElements = null;
	/**
	 * This pseudo-node is used to hold the real root elements. Its creation is synchronized via {@link #objectID2TreeNode} - ensuring
	 * it is not created twice.
	 */
	private TreeNode hiddenRootNode = null;

//	private Map<JDOObjectID, TreeNode> objectID2TreeNode = Collections.synchronizedMap(new HashMap<JDOObjectID, TreeNode>());
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
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected void handleChangeNotification(NotificationEvent notificationEvent, IProgressMonitor monitor) {
		synchronized (objectID2TreeNode) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();

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
					parentsToRefresh.add(parentNode == hiddenRootNode ? null : parentNode);
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
		} // synchronized (objectID2TreeNode) {
	}
	
	protected class ChangeListener extends NotificationAdapterJob {		
		
		public ChangeListener(String name) {
			super(name);
		}
		
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void notify(NotificationEvent notificationEvent) {
			handleChangeNotification(notificationEvent, getProgressMonitor());
		}
	};

	@SuppressWarnings("deprecation") //$NON-NLS-1$
	protected void registerJDOLifecycleListener()
	{
		registerJDOLifecycleListeners();
	}

	/**
	 * @deprecated Use {@link #registerJDOLifecycleListener()} instead! This method will soon be removed!
	 */
	protected void registerJDOLifecycleListeners()
	{
		if (lifecycleListener != null) {
			if (logger.isDebugEnabled())
				logger.debug("registerJDOLifecycleListeners: removing old listener"); //$NON-NLS-1$

			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}

		Set<JDOObjectID> activeParentObjectIDs = getActiveParentObjectIDs();

		if (logger.isDebugEnabled()) {
			logger.debug("registerJDOLifecycleListeners: creating and registering JDOLifecycleListener for " + activeParentObjectIDs.size() + " activeParentObjectIDs:"); //$NON-NLS-1$ //$NON-NLS-2$
			for (JDOObjectID jdoObjectID : activeParentObjectIDs)
				logger.debug("  - " + jdoObjectID); //$NON-NLS-1$
		}

		lifecycleListener = createJDOLifecycleListener(activeParentObjectIDs);		
		JDOLifecycleManager.sharedInstance().addLifecycleListener(lifecycleListener);
	}

	/**
	 * @deprecated Use {@link #registerChangeListener()} instead! This method will soon be removed!
	 */
	protected void createRegisterChangeListener() {
		if (changeListener == null) {
			changeListener = new ChangeListener(Messages.getString("org.nightlabs.jfire.base.jdo.tree.ActiveJDOObjectTreeController.loadingChanges")); //$NON-NLS-1$
			JDOLifecycleManager.sharedInstance().addNotificationListener(getJDOObjectClass(), changeListener);
		}
	}

	@SuppressWarnings("deprecation") //$NON-NLS-1$
	protected void registerChangeListener() {
		createRegisterChangeListener();
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

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void notify(JDOLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("LifecycleListener#notify: enter"); //$NON-NLS-1$

			synchronized (objectID2TreeNode) {
				if (hiddenRootNode == null)
					hiddenRootNode = createNode();
				
				Set<JDOObjectID> objectIDs = new HashSet<JDOObjectID>(event.getDirtyObjectIDs().size());
				final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
				final List<TreeNode> loadedTreeNodes = new ArrayList<TreeNode>();

				if (logger.isDebugEnabled())
					logger.debug("LifecycleListener#notify: got notification with " + event.getDirtyObjectIDs().size() + " DirtyObjectIDs"); //$NON-NLS-1$ //$NON-NLS-2$

				for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
					objectIDs.add((JDOObjectID) dirtyObjectID.getObjectID());

					if (logger.isDebugEnabled())
						logger.debug("LifecycleListener#notify:   - " + dirtyObjectID); //$NON-NLS-1$
				}

				Collection<JDOObject> objects = retrieveJDOObjects(objectIDs, getProgressMonitor());
				for (JDOObject object : objects) {
					TreeNode parentNode;
					boolean ignoreNodeBecauseParentUnknown = false;
					ObjectID parentID = getTreeNodeParentResolver().getParentObjectID(object);
					if (parentID == null) {
//						parentNode = null;
						parentNode = hiddenRootNode;
					}
					else {
						parentNode = objectID2TreeNode.get(parentID);
						if (parentNode == null)
							ignoreNodeBecauseParentUnknown = true;
					}

					if (ignoreNodeBecauseParentUnknown) {
						logger.warn("LifecycleListener#notify: ignoring new object, because its parent is unknown! objectID=\"" + JDOHelper.getObjectId(object) + "\" parentID=\"" + parentID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						continue;
					}

//					parentsToRefresh.add(null); // TODO what's this??? I think, this causes the whole tree to be refreshed. => Do we really need this???

					JDOObjectID objectID = (JDOObjectID) JDOHelper.getObjectId(object);
					TreeNode tn;
					tn = objectID2TreeNode.get(objectID);

					if (logger.isDebugEnabled())
						logger.debug("LifecycleListener#notify: treeNodeAlreadyExists=\"" + (tn != null) + "\" objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					if (tn != null && parentNode != tn.getParent()) { // parent changed, completely replace!
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: treeNode's parent changed! newParent=\"" + parentNode + "\" oldParent=\"" + tn.getParent() + "\" objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

						TreeNode p = (TreeNode) tn.getParent();
						parentsToRefresh.add(p == hiddenRootNode ? null : p);
						if (p == null) {
							throw new IllegalStateException("How the hell can TreeNode.getParent() return null?! If it is a root-node, it should have hiddenRootNode as its parent-node!"); //$NON-NLS-1$
//							if (rootElements != null) {
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: removing TreeNode from rootElements (for replacement)! objectID=\"" + objectID + "\"");
//
//								if (!rootElements.remove(p))
//									logger.warn("LifecycleListener#notify: removing TreeNode from rootElements (for replacement) failed - the TreeNode was not found in the rootElements! objectID=\"" + objectID + "\"");
//							}
//							else {
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: rootElements is null! Cannot remove old TreeNode! objectID=\"" + objectID + "\"");
//							}
						}
						else
							p.removeChildNode(tn);

						objectID2TreeNode.remove(objectID);
						tn = null;
					}

					if (tn == null) {
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: creating TreeNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$

						tn = createNode();
						tn.setActiveJDOObjectTreeController(ActiveJDOObjectTreeController.this);
					}
					else {
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: reusing existing TreeNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}

					tn.setJdoObject(object);
					if (tn.getParent() != parentNode) {
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: tn.getParent() != parentNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$

						tn.setParent(parentNode);
//						if (parentNode != null) // should never be null now - we have introduced hiddenRootNode!
						parentNode.addChildNode(tn);
					}
					else {
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: tn.getParent() == parentNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}

					parentsToRefresh.add(parentNode == hiddenRootNode ? null : parentNode);
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
		} // synchronized (objectID2TreeNode) {
	}

	/**
	 * These objects will be watched for new children to pop up. May contain <code>null</code> for root-elements
	 * (which is very likely).
	 */
	private Set<JDOObjectID> _activeParentObjectIDs = new HashSet<JDOObjectID>();
	private Set<JDOObjectID> _activeParentObjectIDs_ro = null;

	protected Set<JDOObjectID> getActiveParentObjectIDs() {
		synchronized (_activeParentObjectIDs) {
			if (_activeParentObjectIDs_ro == null)
				_activeParentObjectIDs_ro = Collections.unmodifiableSet(new HashSet<JDOObjectID>(_activeParentObjectIDs));

			return _activeParentObjectIDs_ro;
		}
	}

	/**
	 * @param jdoObjectID The OID of the parent-object that should be surveilled for newly created children.
	 * @param autoReregister If <code>true</code>, the method {@link #registerJDOLifecycleListener()} will automatically be called
	 *		if necessary. If <code>false</code>, this method triggered, even if a truly new <code>jdoObjectID</code> has been added.
	 *
	 * @return <code>false</code>, if the given <code>jdoObjectID</code> was already previously surveilled; <code>true</code> if it
	 *		has been added.
	 */
	protected boolean addActiveParentObjectID(JDOObjectID jdoObjectID, boolean autoReregister) {
		synchronized (_activeParentObjectIDs) {
			if (_activeParentObjectIDs.contains(jdoObjectID))
				return false;

			_activeParentObjectIDs.add(jdoObjectID);
			_activeParentObjectIDs_ro = null;
		}

		if (autoReregister)
			registerJDOLifecycleListener();

		return true;
	}

	/**
	 * This method returns either root-nodes, if <code>parent == null</code> or children of the given
	 * <code>parent</code> (if non-<code>null</code>). Alternatively, this method can return <code>null</code>,
	 * if the data is not yet available. In this case, a new {@link Job} will be spawned to load the data.
	 *
	 * @param parent The parent node or <code>null</code>.
	 * @return A list of {@link TreeNode}s or <code>null</code>, if data is not yet ready.
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public List<TreeNode> getNodes(TreeNode _parent)
	{
		if (_parent != null && _parent == hiddenRootNode)
			throw new IllegalArgumentException("Why the hell is the hiddenRootNode passed to this method?! If this ever happens - maybe we should map it to null here?"); //$NON-NLS-1$

		if (logger.isDebugEnabled())
			logger.debug("getNodes: entered for parentTreeNode.jdoObjectID=\"" + (_parent == null ? null : JDOHelper.getObjectId(_parent.getJdoObject())) + "\""); //$NON-NLS-1$ //$NON-NLS-2$

		synchronized (objectID2TreeNode) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();
		}

		List<TreeNode> nodes = null;

		registerChangeListener();

		if (_parent == null) {
//			addActiveParentObjectID(null, true);
//			nodes = rootElements;
			_parent = hiddenRootNode;
		}
//		else {
//			addActiveParentObjectID((JDOObjectID)JDOHelper.getObjectId(_parent.getJdoObject()), true);
//			nodes = parent.getChildNodes();
//		}

		addActiveParentObjectID((JDOObjectID)JDOHelper.getObjectId(_parent.getJdoObject()), true);
		nodes = _parent.getChildNodes();

		if (nodes != null) {
			if (logger.isDebugEnabled())
				logger.debug("getNodes: returning previously loaded child-nodes."); //$NON-NLS-1$

			return nodes;
		}

		final TreeNode parent = _parent;

		if (logger.isDebugEnabled())
			logger.debug("getNodes: returning null and spawning Job."); //$NON-NLS-1$

		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.jdo.tree.ActiveJDOObjectTreeController.loadingDataJob")) { //$NON-NLS-1$
			@Implement
			protected IStatus run(IProgressMonitor monitor)
			{
				if (logger.isDebugEnabled())
					logger.debug("getNodes.Job#run: entered for parentTreeNode.jdoObjectID=\"" + (parent == null ? null : JDOHelper.getObjectId(parent.getJdoObject())) + "\""); //$NON-NLS-1$ //$NON-NLS-2$

				synchronized (objectID2TreeNode) {
					JDOObject parentJDO = parent == null ? null : (JDOObject) parent.getJdoObject();
					JDOObjectID parentJDOID = (JDOObjectID) JDOHelper.getObjectId(parentJDO);

					if (logger.isDebugEnabled())
						logger.debug("getNodes.Job#run: retrieving children for parentTreeNode.jdoObjectID=\"" + (parent == null ? null : JDOHelper.getObjectId(parent.getJdoObject())) + "\""); //$NON-NLS-1$ //$NON-NLS-2$

					Collection<JDOObject> jdoObjects = retrieveChildren(parentJDOID, parentJDO, monitor);

					if (jdoObjects == null)
						throw new IllegalStateException("Your implementation of retrieveChildren(...) returned null! The error is probably in class " + ActiveJDOObjectTreeController.this.getClass().getName()); //$NON-NLS-1$

					List<JDOObject> jdoObjectList;
					if (jdoObjects instanceof List)
						jdoObjectList = (List<JDOObject>) jdoObjects;
					else
						jdoObjectList = new ArrayList<JDOObject>(jdoObjects);

					sortJDOObjects(jdoObjectList);

					final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
					parentsToRefresh.add(parent == hiddenRootNode ? null : parent);

					final List<TreeNode> loadedNodes = new ArrayList<TreeNode>(jdoObjectList.size());
					for (JDOObject jdoObject : jdoObjectList) {
						JDOObjectID objectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
						TreeNode tn = objectID2TreeNode.get(objectID);
						if (tn != null && parent != tn.getParent()) { // parent changed, completely replace!
							if (logger.isDebugEnabled())
								logger.debug("getNodes.Job#run: treeNode's parent changed! objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$

							TreeNode p = (TreeNode) tn.getParent();
							parentsToRefresh.add(p == hiddenRootNode ? null : p);
							if (p != null)
								p.removeChildNode(tn);

							objectID2TreeNode.remove(objectID);
							tn = null;
						}

						if (tn == null) {
							if (logger.isDebugEnabled())
								logger.debug("getNodes.Job#run: creating node for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$

							tn = createNode();
							tn.setActiveJDOObjectTreeController(ActiveJDOObjectTreeController.this);
						}
						else {
							if (logger.isDebugEnabled())
								logger.debug("getNodes.Job#run: reusing existing node for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
						}

						tn.setJdoObject(jdoObject);
						tn.setParent(parent);
						objectID2TreeNode.put(objectID, tn);
						loadedNodes.add(tn);
					}

//					if (parent == null)
//						rootElements = loadedNodes;
//					else
//						parent.setChildNodes(loadedNodes);

					parent.setChildNodes(loadedNodes);

					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							fireJDOObjectsChangedEvent(new JDOTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh, loadedNodes));
						}
					});
				} // synchronized (objectID2TreeNode) {

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
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void fireJDOObjectsChangedEvent(JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent)
	{
		if (logger.isDebugEnabled()) {
			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.parentsToRefresh.size()=" + //$NON-NLS-1$
					(changedEvent.getParentsToRefresh() == null ? null : changedEvent.getParentsToRefresh().size()));
			if (changedEvent.getParentsToRefresh() != null) {
				for (TreeNode treeNode : changedEvent.getParentsToRefresh()) {
					if (treeNode == null)
						logger.debug("    parentTreeNode=null"); //$NON-NLS-1$
					else
						logger.debug("    parentTreeNode.jdoObjectID=" + JDOHelper.getObjectId(treeNode.getJdoObject())); //$NON-NLS-1$
				}
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.ignoredJDOObjects.size()=" + //$NON-NLS-1$
					(changedEvent.getIgnoredJDOObjects() == null ? null : changedEvent.getIgnoredJDOObjects().size()));
			if (changedEvent.getIgnoredJDOObjects() != null) {
				for (Map.Entry<JDOObjectID, TreeNode> me : changedEvent.getIgnoredJDOObjects().entrySet())
					logger.debug("    " + me.getKey()); //$NON-NLS-1$
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.loadedTreeNodes.size()=" + //$NON-NLS-1$
					(changedEvent.getLoadedTreeNodes() == null ? null : changedEvent.getLoadedTreeNodes().size()));
			if (changedEvent.getLoadedTreeNodes() != null) {
				for (TreeNode treeNode : changedEvent.getLoadedTreeNodes())
					logger.debug("    " + JDOHelper.getObjectId(treeNode.getJdoObject())); //$NON-NLS-1$
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.deletedJDOObjects.size()=" + //$NON-NLS-1$
					(changedEvent.getDeletedJDOObjects() == null ? null : changedEvent.getDeletedJDOObjects().size()));
			if (changedEvent.getDeletedJDOObjects() != null) {
				for (Map.Entry<JDOObjectID, TreeNode> me : changedEvent.getDeletedJDOObjects().entrySet())
					logger.debug("    " + me.getKey()); //$NON-NLS-1$
			}
		}

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
