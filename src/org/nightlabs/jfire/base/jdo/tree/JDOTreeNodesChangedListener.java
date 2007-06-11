/**
 * 
 */
package org.nightlabs.jfire.base.jdo.tree;

import org.nightlabs.jdo.ObjectID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface JDOTreeNodesChangedListener<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectTreeNode> {

	void onJDOObjectsChanged(JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent);
}
