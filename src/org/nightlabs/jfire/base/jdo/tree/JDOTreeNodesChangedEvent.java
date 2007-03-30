/**
 * 
 */
package org.nightlabs.jfire.base.jdo.tree;

import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Event that is passed the callback method of {@link ActiveJDOObjectTreeController}s when changes in the tree structures are tracked.
 * <p>
 * Depending on the type of change tracked the appropriate members are filled:
 * <ul>
 * <li>One or more new objects were tracked for loaded parents. 
 * 	Then {@link #getParentsToRefresh()} contains the parent nodes of the new ojbects and
 * 	{@link #getLoadedTreeNodes()} contains the newly created nodes for the new objects.
 * </li>
 * <li>Changes were tracked to loaded objects. 
 * 	Then {@link #getLoadedTreeNodes()} contains the nodes of the changed objects with the newly retieved JDOObjects.
 * </li>
 * <li>Deletion of objects was tracked.
 * 	Then {@link #getDeletedJDOObjects()}} contains the nodes with the formerly loaded and now deleted Objects. 
 * 	Also {@link #getParentsToRefresh()} contains their parent nodes. 
 * </li>
 * </ul>
 *  
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private Set<TreeNode> parentsToRefresh;
	private List<TreeNode> loadedTreeNodes;
	private Map<JDOObjectID, TreeNode> ignoredJDOObjects;
	private Map<JDOObjectID, TreeNode> deletedJDOObjects;

	/**
	 * 
	 */
	public JDOTreeNodesChangedEvent(Object source) {
		super(source);
	}

	
	public JDOTreeNodesChangedEvent(Object source, Set<TreeNode> parentsToRefresh, List<TreeNode> loadedTreeNodes) {
		this(source, parentsToRefresh, loadedTreeNodes, null, null);
	}
	
	public JDOTreeNodesChangedEvent(Object source, List<TreeNode> loadedTreeNodes, Map<JDOObjectID, TreeNode> ignoredJDOObjects, Map<JDOObjectID, TreeNode> deletedJDOObjects) {
		this(source, null, loadedTreeNodes, ignoredJDOObjects, deletedJDOObjects);
	}
	
	public JDOTreeNodesChangedEvent(Object source, Set<TreeNode> parentsToRefresh, List<TreeNode> loadedTreeNodes, Map<JDOObjectID, TreeNode> ignoredJDOObjects, Map<JDOObjectID, TreeNode> deletedJDOObjects) {
		super(source);
		
		if (parentsToRefresh == null)
			this.parentsToRefresh = Collections.emptySet();
		else
			this.parentsToRefresh = Collections.unmodifiableSet(parentsToRefresh);
		
		if (loadedTreeNodes == null)
			this.loadedTreeNodes = Collections.emptyList();
		else
			this.loadedTreeNodes = Collections.unmodifiableList(loadedTreeNodes);
		
		if (ignoredJDOObjects == null)
			this.ignoredJDOObjects = Collections.emptyMap();
		else
			this.ignoredJDOObjects = Collections.unmodifiableMap(ignoredJDOObjects);
		
		if (deletedJDOObjects == null)
			this.deletedJDOObjects = Collections.emptyMap();
		else
			this.deletedJDOObjects = Collections.unmodifiableMap(deletedJDOObjects);			
	}

	/**
	 * @return the deletedJDOObjects
	 */
	public Map<JDOObjectID, TreeNode> getDeletedJDOObjects() {
		return deletedJDOObjects;
	}

	/**
	 * @param deletedJDOObjects the deletedJDOObjects to set
	 */
	public void setDeletedJDOObjects(Map<JDOObjectID, TreeNode> deletedJDOObjects) {
		this.deletedJDOObjects = deletedJDOObjects;
	}

	/**
	 * @return the ignoredJDOObjects
	 */
	public Map<JDOObjectID, TreeNode> getIgnoredJDOObjects() {
		return ignoredJDOObjects;
	}

	/**
	 * @param ignoredJDOObjects the ignoredJDOObjects to set
	 */
	public void setIgnoredJDOObjects(Map<JDOObjectID, TreeNode> ignoredJDOObjects) {
		this.ignoredJDOObjects = ignoredJDOObjects;
	}

	/**
	 * @return the loadedTreeNodes
	 */
	public List<TreeNode> getLoadedTreeNodes() {
		return loadedTreeNodes;
	}

	/**
	 * @param loadedTreeNodes the loadedTreeNodes to set
	 */
	public void setLoadedTreeNodes(List<TreeNode> loadedTreeNodes) {
		this.loadedTreeNodes = loadedTreeNodes;
	}

	/**
	 * @return the parentsToRefresh
	 */
	public Set<TreeNode> getParentsToRefresh() {
		return parentsToRefresh;
	}

	/**
	 * @param parentsToRefresh the parentsToRefresh to set
	 */
	public void setParentsToRefresh(Set<TreeNode> parentsToRefresh) {
		this.parentsToRefresh = parentsToRefresh;
	}
	
	

}
