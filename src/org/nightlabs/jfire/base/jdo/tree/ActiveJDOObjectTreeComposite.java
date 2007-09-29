/**
 * 
 */
package org.nightlabs.jfire.base.jdo.tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.tree.AbstractTreeComposite;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.util.Utils;

/**
 * {@link AbstractTreeComposite} to be used with an {@link ActiveJDOObjectTreeController}.
 * It enables the programatical expansion of active trees (trees that get their data from an {@link ActiveJDOObjectTreeController}).
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class ActiveJDOObjectTreeComposite<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectTreeNode> 
extends AbstractTreeComposite<JDOObject> 
{

	private static final Logger logger = Logger.getLogger(ActiveJDOObjectTreeComposite.class);
	
	/**
	 * @param parent
	 */
	public ActiveJDOObjectTreeComposite(Composite parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param init
	 */
	public ActiveJDOObjectTreeComposite(Composite parent, boolean init) {
		super(parent, init);
	}

	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 * @param init
	 * @param headerVisible
	 */
	public ActiveJDOObjectTreeComposite(Composite parent, int style,
			boolean setLayoutData, boolean init, boolean headerVisible) {
		super(parent, style, setLayoutData, init, headerVisible);
	}

	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 * @param init
	 * @param headerVisible
	 * @param sortColumns
	 */
	public ActiveJDOObjectTreeComposite(Composite parent, int style,
			boolean setLayoutData, boolean init, boolean headerVisible,
			boolean sortColumns) {
		super(parent, style, setLayoutData, init, headerVisible, sortColumns);
	}

	
	@Override
	protected JDOObject getSelectionObject(Object obj) {
		if (!(obj instanceof JDOObjectTreeNode))
			return null;
		return (JDOObject) ((TreeNode)obj).getJdoObject();
	}
	
	
	protected abstract ActiveJDOObjectTreeController<JDOObjectID, JDOObject, TreeNode> getJDOObjectTreeController();
	
	@Override
	protected TreeViewer createTreeViewer(int style) {
		return new ActiveTreeViewer(this, style);
	}
	
	protected ActiveTreeViewer getActiveTreeViewer() {
		return (ActiveTreeViewer) getTreeViewer();
	}
	/**
	 * Currently used for enabling programatical expansion of active trees.
	 */
	private class ActiveTreeViewer extends TreeViewer {

		public ActiveTreeViewer(Composite parent, int style) {
			super(parent, style);
		}
		
		/**
		 * {@inheritDoc}
		 * Overwrites and calls {@link #internalExpand(Object, Object, int, int, Set)}.
		 */
		@Override
		public void expandToLevel(Object elementOrTreePath, int level) {
			int startLevel = 0;
			if (elementOrTreePath instanceof ActiveJDOObjectTreeController)
				startLevel--;
			internalExpand(elementOrTreePath, elementOrTreePath, startLevel, level, new HashSet<LoadListener>());
		}
		
		/**
		 * Uses the {@link ActiveJDOObjectTreeController} of this tree 
		 * to trigger/get data that needs to be expanded.
		 * 
		 * The {@link LoadListener} created here recurses into this method. 
		 */
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		private void internalExpand(Object root, Object elementOrTreePath, int level, int totalLevel, Set<LoadListener> listenerStack) {
			LoadListener listener = new LoadListener(root, elementOrTreePath, level, totalLevel, listenerStack);
			getJDOObjectTreeController().addJDOTreeNodesChangedListener(listener);
			List<TreeNode> childNodes = null;
			if (elementOrTreePath instanceof ActiveJDOObjectTreeController) {
				childNodes = getJDOObjectTreeController().getNodes(null);
			}
			else {
				childNodes = getJDOObjectTreeController().getNodes((TreeNode) elementOrTreePath);
			}
			
			if (childNodes != null) {
				listener.handleLoad(childNodes);
			}
		}
		
		/**
		 * Makes the super expand method accessible 
		 */
		private void superExpandToLevel(Object elementOrTreePath, int level) {
			super.expandToLevel(elementOrTreePath, level);
		}
	}
	
	/**
	 * Handles the successful loading of data to expand.
	 */
	private class LoadListener implements JDOTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> {
		
		private Object root;
		private Object element;
		private int expandLevel;
		private int totalLevel;
		private Set<LoadListener> listenerStack;
		
		public LoadListener(
				Object root,
				Object element, 
				int expandLevel, int totalLevel,
				Set<LoadListener> listenerStack
			) 
		{
			this.root = root;
			this.element = element;
			this.expandLevel = expandLevel;
			this.totalLevel = totalLevel;
			this.listenerStack = listenerStack;
			listenerStack.add(this);
		}

		/**
		 * Checks if theres need to recurse further and call {@link ActiveTreeViewer#internalExpand(Object, Object, int, int, Set)} if so.
		 * If this listener was the last waiting for data it will trigger {@link ActiveTreeViewer#superExpandToLevel(Object, int)} 
		 * with the original parameters.  
		 */
		public void handleLoad(final List<TreeNode> children) {
			getJDOObjectTreeController().removeJDOTreeNodesChangedListener(this);
			if (expandLevel + 1 <= totalLevel) {
				logger.info(Utils.addLeadingChars(element.toString(), element.toString().length() + expandLevel + 1, ' '));
				for (TreeNode childNode : children) {
					getActiveTreeViewer().internalExpand(root, childNode, expandLevel+1, totalLevel, listenerStack);
				}
			}
			listenerStack.remove(this);
			if (listenerStack.isEmpty())
				getActiveTreeViewer().superExpandToLevel(root, totalLevel);
		}
		
		public void onJDOObjectsChanged(JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent) {
			if (changedEvent.getParentsToRefresh().contains(element) || (element instanceof ActiveJDOObjectTreeController)) {
				handleLoad(changedEvent.getLoadedTreeNodes());
			}
		}
	}
}
