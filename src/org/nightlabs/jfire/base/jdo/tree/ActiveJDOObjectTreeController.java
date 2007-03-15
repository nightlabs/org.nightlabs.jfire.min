package org.nightlabs.jfire.base.jdo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.annotation.Implement;
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

public abstract class ActiveJDOObjectTreeController<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectTreeNode>
{
	/**
	 * @param parent <code>null</code> for the root elements or the parent element for which to load the children.
	 */
	protected abstract Collection<JDOObject> retrieveChildren(JDOObjectID parentID, JDOObject parent, IProgressMonitor monitor);

	/**
	 * This method is called on a worker thread and must retrieve JDO objects for
	 * the given object-ids from the server.
	 *
	 * @param objectIDs The jdo object ids representing the desired objects.
	 * @param monitor The monitor.
	 * @return Returns the jdo objects that correspond to the requested <code>objectIDs</code>.
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, IProgressMonitor monitor);

	protected abstract TreeNode createNode();

	protected abstract void sortJDOObjects(List<JDOObject> objects);

	private List<TreeNode> rootElements = null;

	private Map<JDOObjectID, TreeNode> objectID2TreeNode = new HashMap<JDOObjectID, TreeNode>();

	/**
	 * This method is called by the default implementation of {@link #createJDOLifecycleListenerFilter()}.
	 */
	protected abstract TreeNodeParentResolver createTreeNodeParentResolver();

	private TreeNodeParentResolver treeNodeParentResolver = null;

	public TreeNodeParentResolver getTreeNodeParentResolver()
	{
		if (treeNodeParentResolver == null)
			treeNodeParentResolver = createTreeNodeParentResolver();

		return treeNodeParentResolver;
	}

	protected abstract Class getJDOObjectClass();

	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter(Set<? extends ObjectID> parentObjectIDs)
	{
		return new TreeLifecycleListenerFilter(
				getJDOObjectClass(), true,
				parentObjectIDs, getTreeNodeParentResolver(),
				new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	protected JDOLifecycleListener createJDOLifecycleListener(Set<? extends ObjectID> parentObjectIDs)
	{
		IJDOLifecycleListenerFilter filter = createJDOLifecycleListenerFilter(parentObjectIDs);
		return new LifecycleListener(filter);
	}

	private JDOLifecycleListener lifecycleListener = null;

	protected void registerJDOLifecycleListeners()
	{
		if (lifecycleListener != null) {
			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}

		lifecycleListener = createJDOLifecycleListener(activeParentObjectIDs);		
		JDOLifecycleManager.sharedInstance().addLifecycleListener(lifecycleListener);
	}

	public void close()
	{
		if (lifecycleListener != null) {
			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}
	}

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
					fireJDOObjectsChangedEvent(parentsToRefresh, loadedTreeNodes, null, null);
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
	public List<TreeNode> getNodes(final TreeNode parent)
	{
		List<TreeNode> nodes = null;
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
						fireJDOObjectsChangedEvent(parentsToRefresh, newNodes, null, null);
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

	private void fireJDOObjectsChangedEvent(Set<TreeNode> parentsToRefresh, List<TreeNode> loadedTreeNodes, Map<JDOObjectID, TreeNode> ignoredJDOObjects, Map<JDOObjectID, TreeNode> deletedJDOObjects)
	{
		onJDOObjectsChanged(parentsToRefresh, loadedTreeNodes);
	}

	protected abstract void onJDOObjectsChanged(Set<TreeNode> parentsToRefresh, List<TreeNode> loadedTreeNodes); // TODO this must get ONE Event parameter
}
