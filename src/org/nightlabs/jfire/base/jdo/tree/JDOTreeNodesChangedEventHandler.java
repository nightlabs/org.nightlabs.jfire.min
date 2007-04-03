/**
 * 
 */
package org.nightlabs.jfire.base.jdo.tree;

import java.util.HashSet;

import org.eclipse.jface.viewers.TreeViewer;
import org.nightlabs.jdo.ObjectID;

/**
 * Event handler that applies the changes found in an {@link JDOTreeNodesChangedEvent} to a {@link TreeViewer}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JDOTreeNodesChangedEventHandler {

	public JDOTreeNodesChangedEventHandler() {
	}
	
	public static void handle(TreeViewer treeViewer, JDOTreeNodesChangedEvent<? extends ObjectID, ? extends JDOObjectTreeNode> changedEvent) {
		if (treeViewer.getTree().isDisposed())
			return;
		HashSet<JDOObjectTreeNode> refreshed = new HashSet<JDOObjectTreeNode>();
		boolean refreshAll = false;
		if (changedEvent.getParentsToRefresh() != null) {
			for (JDOObjectTreeNode node : changedEvent.getParentsToRefresh()) {
				if (node == null) {
					refreshAll = true;
					break;
				}

				if (refreshed.add(node))
					treeViewer.refresh(node);
			}
		}

		if (refreshAll)
			treeViewer.refresh();
		else {
			for (JDOObjectTreeNode node : changedEvent.getLoadedTreeNodes()) {
				if (refreshed.add(node))
					treeViewer.refresh(node);
			}
		}
	}

}
