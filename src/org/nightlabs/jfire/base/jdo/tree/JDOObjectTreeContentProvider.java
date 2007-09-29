/**
 * 
 */
package org.nightlabs.jfire.base.jdo.tree;

import java.util.List;

import org.nightlabs.base.ui.tree.TreeContentProvider;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * A ContentProvider that can be used with a TreeViewer that is driven by an {@link ActiveJDOObjectTreeController}.
 * It assumes the initial input of the ContentProvider is the {@link ActiveJDOObjectTreeController}
 * and uses the controller to serve element. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class JDOObjectTreeContentProvider<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectTreeNode> extends TreeContentProvider {

	public JDOObjectTreeContentProvider() {
	}
	
	/**
	 * Default implementation calls {@link #getChildren(Object)}
	 */
	public Object[] getElements(Object inputElement)
	{
		return getChildren(inputElement);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	public Object[] getChildren(Object parentElement)
	{
		TreeNode parent = null;
		ActiveJDOObjectTreeController<JDOObjectID, JDOObject, TreeNode> controller = null;

		if (parentElement instanceof ActiveJDOObjectTreeController) {
			controller = (ActiveJDOObjectTreeController)parentElement;
			parent = null;
		}
		else if (parentElement instanceof String) {
			return null;
		}
		else {
			parent = (TreeNode) parentElement;
			controller = parent.getActiveJDOObjectTreeController();
		}

		if (controller == null)
			return new Object[] { };

		List<TreeNode> res = controller.getNodes(parent);
		if (res == null)
			return new String[] { Messages.getString("org.nightlabs.jfire.base.jdo.tree.JDOObjectTreeContentProvider.loading") }; //$NON-NLS-1$
		else
			return res.toArray();
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof String)
			return false;
		TreeNode node = (TreeNode) element;
		return hasJDOObjectChildren((JDOObject) node.getJdoObject());
	}
	
	/**
	 * Implement this for custom checking whether the node/jdoObject has children.
	 * 
	 * @param jdoObject The {@link JDOObject} to check
	 * @return Whether the given {@link JDOObject} has children.
	 */
	public abstract boolean hasJDOObjectChildren(JDOObject jdoObject);
}
