package org.nightlabs.jfire.base.jdo;

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

public abstract class ActiveJDOObjectTreeController<JDOObjectID, JDOObject, TreeNode extends JDOObjectTreeNode>
{
	/**
	 * @param parent <code>null</code> for the root elements or the parent element for which to load the children.
	 */
	protected abstract Collection<JDOObject> retrieveChildren(JDOObjectID parentID, JDOObject parent, IProgressMonitor monitor);

	protected abstract TreeNode createNode();

	protected abstract void sortJDOObjects(List<JDOObject> objects);

	private List<TreeNode> rootElements = null;

	private Map<JDOObjectID, TreeNode> objectID2TreeNode = new HashMap<JDOObjectID, TreeNode>();

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
		if (parent == null)
			nodes = rootElements;
		else
			nodes = parent.getChildNodes();

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
