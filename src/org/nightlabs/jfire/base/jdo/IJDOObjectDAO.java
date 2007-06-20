/**
 * 
 */
package org.nightlabs.jfire.base.jdo;

import org.nightlabs.progress.ProgressMonitor;

/**
 * Interface that should be implemented by subclasses of {@link BaseJDOObjectDAO}
 * that are also capable of saving the objects they retrieve.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IJDOObjectDAO<JDOObject> {

	/**
	 * Store the given JDOObject
	 * 
	 * @param jdoObject The object to store.
	 * @param get Whether a detached copy should be returned after storing the object.
	 * @param fetchGroups The fetch-groups to detach the copy with if get is true. 
	 * @param maxFetchDepth The maximum fetch-depth while detaching.
	 * @param monitor A monitor to report progress to.
	 * @return A detached copy of the newly stored object or <code>null</code> if get is <code>false</code>.
	 */
	public JDOObject storeJDOObject(
			JDOObject jdoObject, 
			boolean get, 
			String[] fetchGroups, 
			int maxFetchDepth, 
			ProgressMonitor monitor
		);
}
